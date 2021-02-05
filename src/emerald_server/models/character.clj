(ns emerald-server.models.character
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :as walk]
            [emerald-server.models.ability :as ability]
            [spec-tools.spec :as spec]))

(load "character_specs")

(defn char-buffes-summarized [char-core]
  (let [buffs-summary (let [*gathered (atom [])]
                        (doseq [k [:equipments :status]]
                          (walk/postwalk
                           #(when (and (map? %) (% :buffs))
                              (swap! *gathered concat ,, (% :buffs)))
                           (get-in char-core k)))
                        @*gathered)]
    (assoc char-core :buffs-summary buffs-summary)))

(def buff-str->key
  {"筋力" :str "生命力" :con "知力" :int "精神力" :pow "器用度" :dex "敏捷度" :agi})

(defn char-buffedparam
  "Completes secondary parameter data applying buffs."
  [{{:keys [base initial growth] :as params} :parameters
    buffs                                    :buffs-summary
    :as                                      char-buffes-summarized}]
  (let [inherent     (for [[k v] initial]
                       (cond (or (= k :str) (= k :con)) {k (+ v (base :physique))}
                             (or (= k :dex) (= k :agi)) {k (+ v (base :technique))}
                             (or (= k :int) (= k :pow)) {k (+ v (base :mind))}))
        total-raw    (merge-with + inherent growth)
        pbuffs       (filter #(some #{(% :target)} (keys buff-str->key)) buffs)
        pbuffvals    (for [[{k :target v :value}] pbuffs] {k v})
        total-buffed (merge-with + total-raw pbuffvals)
        bonus        (walk/postwalk #(if (integer? %) (int (/ % 6)) %) total-buffed)
        new-params   (assoc params :total total-buffed :bonus bonus)]
    (assoc char-buffes-summarized :parameters new-params)))

(defn buff-sum
  "buffs: buffs-summary, target: 'string"
  [buffs target]
  (reduce + (map :value (filter #(= (% :target) target) buffs))))

(defn reference-values
  "Generate reference values from characted data with buffed parameters."
  [{{:keys [level]}                        :status
    {{agi-total :agi}              :total
     {:keys [dex agi str con pow]} :bonus} :parameters
    buffs                                  :buffs-summary
    :as                                    char-buffedparam}]
  (let [warrior-sum (ability/level-sum char-buffedparam :warrior)
        cqc-sum     (ability/level-sum char-buffedparam :cqc)]
    {:damage-bonus  (if-not (zero? warrior-sum) (+ warrior-sum str) 0)
     :defense       (reduce + (map :value (filter #(and (= (set (% :keys)) #{:target :value :attack-type})
                                                        (= (% :target) "ダメージ軽減")
                                                        (= (% :attack-type) "物理")) buffs)))
     :hit           (if-not (zero? warrior-sum) (+ (+ warrior-sum dex) (buff-sum buffs "命中")) 0)
     :dodge         (if-not (zero? cqc-sum) (+ (+ cqc-sum agi) (buff-sum buffs "回避")) 0)
     :resistance    {:con (+ (+ level con) (buff-sum buffs "生命抵抗"))
                     :pow (+ (+ level pow) (buff-sum buffs "精神抵抗"))}
     ;; TODO 魔力は個別？
     :magical-power (into {} (for [[k name] ability/key->name]
                               [k (+ ((first (filter #(= (% :name) name) (char-buffedparam :abilities))) :level)
                                     (buff-sum buffs "魔力"))]))
     :mobility      {:limited 3 :normal agi-total :full-power (* 3 agi-total)}
     :packages      (for [skill [:scout :ranger :sage]
                          bonus [:dex :agi :int]
                          :when (not (and (= skill :sage) (or (= bonus :dex) (= bonus :agi))))]
                      {skill {bonus (if-not (zero? skill) (+ skill bonus) 0)}})}))

(defn char-full-buffed
  "Returns fully specified character data."
  [{params :parameters :as char-buffedparam}]
  (let [level     (apply max (map :level (char-buffedparam :abilities)))
        max-hp    (+ (* 3 level) (get-in params [:total :con]))
        max-mp    (+ (* 3 (ability/level-sum char-buffedparam :magician)) (get-in params [:total :pow]))
        secondary {:status           {:level  level
                                      :max-hp max-hp
                                      :max-mp max-mp}
                   :reference-values (reference-values char-buffedparam)}]
    (merge char-buffedparam secondary)))

(defn character-full [char-core]
  {:pre  [(s/valid? :character.core/character char-core)]
   :post [(s/valid? ::character %)]}
  ;; TODO :hp :mp がない場合の処理も
  (-> char-core
      (char-buffes-summarized)
      (char-buffedparam)
      (char-full-buffed)))
