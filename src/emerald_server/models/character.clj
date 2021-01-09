(ns emerald-server.models.character
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :as walk]
            [emerald-server.models.ability :as ability]
            [spec-tools.spec :as spec]))

(load "character_specs")

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

(defn reference-values
  "Generate reference values based on character data.
  Arguments: {c 'character-data-core level 'character-level map 'full-parameters}"
  [c level {{agi-raw :agi} :total {:keys [dex agi str con int pow]} :bonus}]
  (let [warrior-sum (ability/level-sum c :warrior)
        cqc-sum     (ability/level-sum c :cqc)]
    {:damage-bonus  (if-not (zero? warrior-sum) (+ warrior-sum str) 0)
     :defence       '装備
     :hit           (if-not (zero? warrior-sum) (+ (+ warrior-sum dex) '装備) 0)
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
  (let [level            (apply max (map :level (c :abilities)))
        parameters       (full-parameters (c :parameters))
        reference-values (reference-values c level parameters)
        max-hp           (+ (* 3 level) (get-in parameters [:total :con]))
        max-mp           (+ (* 3 (ability/level-sum c :magician)) (get-in parameters [:total :pow]))]
    {:level level
     :max-hp max-hp
     :max-mp max-mp
     :parameters parameters
     :reference-values reference-values}))

(defn fully-specified-character [character]
  {:pre  [(s/valid? :character.core/character character)]
   :post [(s/valid? ::character %)]}
  ;; TODO :hp :mp がない場合の処理も
  (merge character (secondary-variable character)))
