(ns emerald-server.models.character
  (:require [clojure.pprint :as pprint]
            [clojure.spec.alpha :as s]
            [clojure.walk :as walk]
            [emerald-server.models.ability :as ability]
            [spec-tools.spec :as spec]))

(load "character_specs")

(defn char-buffs-summarized [char-core]
  (let [buffs-summary (vec (let [*gathered (atom [])]
                             (doseq [k [:equipments :status]]
                               (walk/postwalk
                                #(do (when (and (map? %) (% :buffs)
                                                ;; temporary buffs are not counted for refvals
                                                (not (or (get-in % [:buffs :remaining-rounds])
                                                         (get-in % [:buffs :valid-to])
                                                         (get-in % [:buffs :when]))))
                                       (swap! *gathered concat ,, (% :buffs)))
                                     %)
                                (get char-core k)))
                             @*gathered))]
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
  (println "char-full-buffedstatus")
  (let [level            (apply max (map :level (char-buffedparam :abilities)))
        max-hp           (+ (* 3 level) (get-in char-buffedparam [:parameters :total :con]))
        max-mp           (+ (* 3 (ability/level-sum char-buffedparam :magician))
                            (get-in char-buffedparam [:parameters :total :pow]))
        exp              (get-in char-buffedparam [:status :exp])
        exp-free         (- (exp :total) (exp :used))
        status           {:level level :max-hp max-hp :max-mp max-mp}
        status-default   {:hp max-hp :mp max-mp :honor 0 :g 0 :impurity 0}]
    ;; Keep merge order: Don't overwrite the existing values
    (update char-buffedparam :status #(merge status status-default % {:exp (assoc exp :free exp-free)}))))

(defn char-full-buffed
  "Generate reference values from characted data with buffed parameters."
  [{{:keys [level]}                            :status
    {{agi-total :agi}                  :total
     {:keys [dex agi str con pow int]} :bonus} :parameters
    buffs                                      :buffs-summary
    :as                                        char-buffedstatus}]
  (println "char-full-buffed")
  (let [warrior-sum   (ability/level-sum char-buffedstatus :warrior)
        cqc-sum       (ability/level-sum char-buffedstatus :cqc)
        damage-bonus  (if-not (zero? warrior-sum) (+ warrior-sum str) 0)
        defense       (reduce + (map :value (filter #(and (= (set (keys %)) #{:target :value :attack-type})
                                                          (= (% :target) "ダメージ軽減")
                                                          (= (% :attack-type) "物理")) buffs)))
        hit           (if-not (zero? warrior-sum) (+ (+ warrior-sum dex) (buff-sum buffs "命中")) 0)
        dodge         (if-not (zero? cqc-sum) (+ (+ cqc-sum agi) (buff-sum buffs "回避")) 0)
        resistance    {:con (+ (+ level con) (buff-sum buffs "生命抵抗"))
                       :pow (+ (+ level pow) (buff-sum buffs "精神抵抗"))}
        magical-power (into {} (for [[k aname] ability/magical-key->str
                                     :let      [sum (ability/level-if char-buffedstatus aname)]
                                     :when     sum]
                                 [k (+ sum (buff-sum buffs "魔力"))]))
        mobility      {:limited 3 :normal agi-total :sprint (* 3 agi-total)}
        packages      (->> (for [skill [:scout :ranger :sage]]
                             {skill
                              (->> (for [bonus [:dex :agi :int]
                                         :when (not (and (= skill :sage)
                                                         (or (= bonus :dex) (= bonus :agi))))]
                                     (let [skill-level (ability/level-if char-buffedstatus (ability/key->str skill))
                                           bonus-value (case bonus :dex dex :agi agi :int int)]
                                       [bonus (if (pos-int? skill-level) (+ skill-level bonus-value) 0)]))
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
  (let [c char-core
        c (char-buffs-summarized c)
        c (char-buffedparam c)
        c (char-buffedstatus c)
        c (char-full-buffed c)]
    (dissoc c :buffs-summary)))

;; Debug

(comment
  (require '[emerald-server.handler.characters :as chandler])
  (def char-debug (@(get @chandler/*characters 1) :data))
  (character-full char-debug)
  (println char-debug))
