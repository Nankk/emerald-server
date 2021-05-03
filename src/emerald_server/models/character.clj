(ns emerald-server.models.character
  (:require [clojure.pprint :as pprint]
            [clojure.spec.alpha :as s]
            [clojure.walk :as walk]
            [emerald-server.models.ability :as ability]
            [emerald-server.db :as db]
            [spec-tools.spec :as spec]))

(load "character_specs")

(defn char-buffs-summarized [char-core]
  (let [buffs-summary
        (vec (let [*gathered (atom [])]
               (doseq [k [:equipments :status]]
                 (walk/postwalk
                  #(do
                     (when (and (map? %)
                                ;; temporary buffs are not counted for refvals
                                (not (or (get-in % [:buffs :remaining-rounds])
                                         (get-in % [:buffs :valid-to])
                                         (get-in % [:buffs :when]))))
                       (cond
                         (% :buffs) (swap! *gathered concat ,, (% :buffs))
                         (% :uuid)  (let [eqps  (@db/*data :equipments-map)
                                          buffs (get-in eqps [(keyword (% :uuid)) :buffs])]
                                      (swap! *gathered concat ,, buffs))))
                     %)
                  (get char-core k)))
               @*gathered))]
    (assoc char-core :buffs-summary buffs-summary)))

(def buff-str->key
  {"筋力" :str "生命力" :con "知力" :int "精神力" :pow "器用度" :dex "敏捷度" :agi})

(def params-default
  {:str 0 :con 0 :dex 0 :agi 0 :int 0 :pow 0})

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
        bonus-raw    (walk/postwalk #(if (integer? %) (int (/ % 6)) %) total-raw)
        pbuffs       (filter #(some #{(% :target)} (keys buff-str->key)) buffs)
        pbuffvals    (into {} (for [{k :target v :value} pbuffs] [(buff-str->key k) v]))
        buff         (merge-with + params-default pbuffvals)
        total-buffed (merge-with #(max 0 (+ %1 %2)) total-raw buff)
        bonus-buffed (walk/postwalk #(if (integer? %) (int (/ % 6)) %) total-buffed)
        new-params   (assoc params
                            :buff  buff
                            :total total-raw
                            :total-buffed total-buffed
                            :bonus bonus-raw
                            :bonus-buffed bonus-buffed)]
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
  (let [level             (apply max (map :level (char-buffedparam :abilities)))
        max-hp            (+ (* 3 level) (get-in char-buffedparam [:parameters :total :con]))
        max-mp            (+ (* 3 (ability/level-sum char-buffedparam :magician))
                             (get-in char-buffedparam [:parameters :total :pow]))
        exp               (get-in char-buffedparam [:status :exp])
        exp-free          (- (exp :total) (exp :used))
        achievements      (get-in char-buffedparam [:status :achievements])
        achievements-free (- (achievements :total) (achievements :used))
        status            {:level level :max-hp max-hp :max-mp max-mp}
        status-default    {:hp max-hp :mp max-mp :honor 0 :g 0 :impurity 0}]
    ;; Keep merge order: Don't overwrite the existing values
    (update char-buffedparam :status #(merge status status-default %
                                             {:exp (assoc exp :free exp-free)
                                              :achievements (assoc achievements :free achievements-free)}))))

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
        damage-bonus  (+ (if-not (zero? warrior-sum) (+ warrior-sum str) 0)
                         (reduce + (map :value (filter #(and (= (set (keys %)) #{:target :value :attack-type})
                                                             (= (% :target) "ダメージ上昇")
                                                             (= (% :attack-type) "物理")) buffs))))
        defense       (reduce + (map :value (filter #(and (= (set (keys %)) #{:target :value :attack-type})
                                                          (= (% :target) "ダメージ軽減")
                                                          (= (% :attack-type) "物理")) buffs)))
        hit           (if-not (zero? warrior-sum) (+ (+ warrior-sum dex) (buff-sum buffs "命中力判定")) 0)
        dodge         (if-not (zero? cqc-sum) (+ (+ cqc-sum agi) (buff-sum buffs "回避力判定")) 0)
        resistance    {:con (+ (+ level con) (buff-sum buffs "生命抵抗力判定"))
                       :pow (+ (+ level pow) (buff-sum buffs "精神抵抗力判定"))}
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
  {:pre  [(s/assert :character.core/character char-core)]
   :post [(s/assert ::character %)]}
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
