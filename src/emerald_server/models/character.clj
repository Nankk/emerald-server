(ns emerald-server.models.character
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :as walk]
            [emerald-server.models.ability :as ability]
            [spec-tools.spec :as spec]))

(load "character_specs")

(defn buff->keys [buff]
  (case (buff :target)
    :命中 [:reference-values :hit]
    :回避 [:reference-values :dodge]
    :知力 [:parameters :total :int]
    :精神力 [:parameters :total :pow]
    :器用度 [:parameters :total :dex]
    :敏捷度 [:parameters :total :agi]
    :筋力 [:parameters :total :str]
    :生命力 [:parameters :total :con]
    ))

(defn apply-buffs [data buffs]
  (for [buff buffs]
    ()))

(defn packages []
  )

(defn full-parameters
  "Returns full parameters set. Pass character core data."
  [{:keys [base initial growth] :as params}]
  (let [inherent (for [[k v] initial]
                   (cond (or (= k :str) (= k :con)) {k (+ v (base :physique))}
                         (or (= k :dex) (= k :agi)) {k (+ v (base :technique))}
                         (or (= k :int) (= k :pow)) {k (+ v (base :mind))}))
        total    (merge-with + inherent growth)
        bonus    (walk/postwalk #(if (integer? %) (int (/ % 6)) %) total)]
    (merge params total bonus)))

(defn summarized-buffs [character keys-coll]
  (let [*gathered (atom [])]
    (doseq [ks keys-coll]
      (walk/postwalk #(when (and (map? %) (% :buffs))
                        (swap! *gathered concat ,, (% :buffs)))
                     (get-in character ks)))
    @*gathered))

(defn reference-values
  "Generate reference values based on character data.
  Arguments: {c             'character-data-core
              level         'character-level
              buffs         'buffs-summary
              map           'full-parameters}"
  [c level buffs {{agi-raw :agi} :total {:keys [dex agi str con int pow]} :bonus}]
  (let [warrior-sum (ability/level-sum c :warrior)
        cqc-sum     (ability/level-sum c :cqc)]
    {:damage-bonus  (if-not (zero? warrior-sum) (+ warrior-sum str) 0)
     :defense       (reduce + (map :value (filter #(and (= (set (% :keys)) #{:target :value :attack-type})
                                                        (= (% :target) "ダメージ軽減")
                                                        (= (% :attack-type) "物理")) buffs)))
     :hit           (if-not (zero? warrior-sum) (+ (+ warrior-sum dex) 999) 0)
     :dodge         (if-not (zero? cqc-sum) (+ cqc-sum agi) 0)
     :resistance    {:con (+ level con)
                     :pow (+ level pow)}
     :magical-power (into {} (for [[k name] ability/key->name]
                               [k ((first (filter #(= (% :name) name) (c :abilities))) :level)]))
     :mobility      {:limited 3 :normal agi-raw :full-power (* 3 agi-raw)}
     :packages      (for [skill [:scout :ranger :sage]
                          bonus [:dex :agi :int]
                          :when (not (and (= skill :sage) (or (= bonus :dex) (= bonus :agi))))]
                      {skill {bonus (if-not (zero? skill) (+ skill bonus) 0)}})}))

(defn secondary-variable [c]
  (let [buffs-summary    (summarized-buffs c [[:equipments] [:skills]])
        level            (apply max (map :level (c :abilities)))
        parameters       (full-parameters (c :parameters))
        reference-values (reference-values c level buffs-summary parameters)
        max-hp           (+ (* 3 level) (get-in parameters [:total :con]))
        max-mp           (+ (* 3 (ability/level-sum c :magician)) (get-in parameters [:total :pow]))
        secondary-raw {:level level
                       :max-hp max-hp
                       :max-mp max-mp
                       :parameters parameters
                       :reference-values reference-values}]
    (assoc (apply-buffs secondary-raw) :buffs-summary buffs-summary)))

(defn fully-specified-character [character]
  {:pre  [(s/valid? :character.core/character character)]
   :post [(s/valid? ::character %)]}
  ;; TODO :hp :mp がない場合の処理も
  (merge character (secondary-variable character)))
