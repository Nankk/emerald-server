(ns emerald-server.models.character
  (:require [clojure.pprint :as pprint]
            [clojure.spec.alpha :as s]
            [clojure.walk :as walk]
            [emerald-server.models.ability :as ability]
            [spec-tools.spec :as spec]))

(load "character_specs")

(defn char-buffs-summarized [char-core]
  (let [buffs-summary (let [*gathered (atom [])]
                        (doseq [k [:equipments :status]]
                          (walk/postwalk
                           #(when (and (map? %) (% :buffs))
                              (swap! *gathered concat ,, (% :buffs)))
                           (get char-core k)))
                        @*gathered)]
    (assoc char-core :buffs-summary buffs-summary)))

(def buff-str->key
  {"筋力" :str "生命力" :con "知力" :int "精神力" :pow "器用度" :dex "敏捷度" :agi})

(defn char-buffedparam
  "Completes secondary parameter data applying buffs."
  [{{:keys [base initial growth] :as params} :parameters
    buffs                                    :buffs-summary
    :as                                      char-buffs-summarized}]
  (let [inherent     (into {} (for [[k v] initial]
                                (cond (or (= k :str) (= k :con)) [k (+ v (base :physique))]
                                      (or (= k :dex) (= k :agi)) [k (+ v (base :technique))]
                                      (or (= k :int) (= k :pow)) [k (+ v (base :mind))])))
        total-raw    (merge-with + inherent growth)
        pbuffs       (filter #(some #{(% :target)} (keys buff-str->key)) buffs)
        pbuffvals    (into {} (for [[{k :target v :value}] pbuffs] [k v]))
        total-buffed (merge-with + total-raw pbuffvals)
        bonus        (walk/postwalk #(if (integer? %) (int (/ % 6)) %) total-buffed)
        new-params   (assoc params :total total-buffed :bonus bonus)]
    (assoc char-buffs-summarized :parameters new-params)))

(defn buff-sum
  "buffs: buffs-summary, target: 'string"
  [buffs target]
  (println "buff-sum [buffs " buffs " target " target "]")
  (reduce + (map :value (filter #(= (% :target) target) buffs))))

(defn char-buffedstatus
  "Returns character data with buffed status."
  [char-buffedparam]
  (println "char-full-buffed")
  (let [level            (apply max (map :level (char-buffedparam :abilities)))
        max-hp           (+ (* 3 level) (get-in char-buffedparam [:parameters :total :con]))
        max-mp           (+ (* 3 (ability/level-sum char-buffedparam :magician))
                            (get-in char-buffedparam [:parameters :total :pow]))
        status           {:level level :max-hp max-hp :max-mp max-mp :hp max-hp :mp max-mp}]
    (update char-buffedparam :status #(merge % status))))

(defn char-full-buffed
  "Generate reference values from characted data with buffed parameters."
  [{{:keys [level]}                            :status
    {{agi-total :agi}                  :total
     {:keys [dex agi str con pow int]} :bonus} :parameters
    buffs                                      :buffs-summary
    :as                                        char-buffedstatus}]
  (let [warrior-sum   (ability/level-sum char-buffedstatus :warrior)
        cqc-sum       (ability/level-sum char-buffedstatus :cqc)
        damage-bonus  (if-not (zero? warrior-sum) (+ warrior-sum str) 0)
        defense       (reduce + (map :value (filter #(and (= (set (% :keys)) #{:target :value :attack-type})
                                                          (= (% :target) "ダメージ軽減")
                                                          (= (% :attack-type) "物理")) buffs)))
        hit           (if-not (zero? warrior-sum) (+ (+ warrior-sum dex) (buff-sum buffs "命中")) 0)
        dodge         (if-not (zero? cqc-sum) (+ (+ cqc-sum agi) (buff-sum buffs "回避")) 0)
        resistance    {:con (+ (+ level con) (buff-sum buffs "生命抵抗"))
                       :pow (+ (+ level pow) (buff-sum buffs "精神抵抗"))}
        _             (println resistance)
        magical-power (into {} (for [[k aname] ability/magical-key->str
                                     :let      [sum (ability/level-if char-buffedstatus aname)]
                                     :when     sum]
                                 [k (+ sum (buff-sum buffs "魔力"))]))
        mobility      {:limited 3 :normal agi-total :full-power (* 3 agi-total)}
        packages      (->> (for [skill [:scout :ranger :sage]]
                             {skill
                              (->> (for [bonus [:dex :agi :int]
                                         :when (not (and (= skill :sage)
                                                         (or (= bonus :dex) (= bonus :agi))))]
                                     (let [skill-level (ability/level-if char-buffedstatus ability/key->str)
                                           bonus-value (case bonus :dex dex :agi agi :int int)]
                                       [bonus (if (pos-int? skill-level) (+ skill bonus-value) 0)]))
                                   (into {} ,,))})
                           (into {} ,,))]
    (assoc char-buffedstatus :reference-values
           {:damage-bonus  damage-bonus
            :defense       defense
            :hit           hit
            :dodge         dodge
            :resistance    resistance
            :magical-power magical-power ; TODO 魔力バフに個別のやつある？
            :mobility      mobility
            :packages      packages})))

(defn character-full [char-core]
  {:pre  [(s/valid? :character.core/character char-core)]
   :post [(s/valid? ::character %)]}
  ;; TODO :hp :mp :exp :honor がない場合の処理も
  (let [full (-> char-core
                 (char-buffs-summarized)
                 (char-buffedparam)
                 (char-buffedstatus)
                 (char-full-buffed))]
    (pprint/pprint full)
    full))

;; Debug

(comment
  (require '[emerald-server.handler.characters :as chandler])
  (def char-debug (@(get @chandler/*characters 2) :data))
  (character-full char-debug))
