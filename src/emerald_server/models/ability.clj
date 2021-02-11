(ns emerald-server.models.ability)

(def abilities
  {"ファイター"         #{:warrior :cqc}
   "グラップラー"       #{:warrior :cqc}
   "フェンサー"         #{:warrior :cqc}
   "シューター"         #{:warrior}
   "ソーサラー"         #{:magician}
   "コンジャラー"       #{:magician}
   "プリースト"         #{:magician}
   "フェアリーテイマー" #{:magician}
   "マギテック"         #{:magician}
   "デーモンルーラー"   #{:magician}
   "ドルイド"           #{:magician}
   "グリモワール"       #{:magician}
   "スカウト"           #{:other}
   "レンジャー"         #{:other}
   "セージ"             #{:other}
   "エンハンサー"       #{:other}
   "バード"             #{:other}
   "ライダー"           #{:other}
   "アルケミスト"       #{:other}
   "ウォーリーダー"     #{:other}
   "ミスティック"       #{:other}})

(def magical-key->str
  {:sorcerer    "ソーサラー"
   :conjurer    "コンジャラー"
   :priest      "プリースト"
   :fairy-tamer "フェアリーテイマー"
   :magitech    "マギテック"
   :demon-ruler "デーモンルーラー"
   :druid       "ドルイド"
   :grimoire    "グリモワール"})

(def str->key
  {"ファイター"         :fighter
   "グラップラー"       :grappler
   "フェンサー"         :fencer
   "シューター"         :shooter
   "ソーサラー"         :sorcerer
   "コンジャラー"       :conjurer
   "プリースト"         :priest
   "フェアリーテイマー" :fairy-tamer
   "マギテック"         :magitech
   "デーモンルーラー"   :demon-ruler
   "ドルイド"           :druid
   "グリモワール"       :grimoire
   "スカウト"           :scout
   "レンジャー"         :ranger
   "セージ"             :sage
   "エンハンサー"       :enhancer
   "バード"             :bird
   "ライダー"           :rider
   "アルケミスト"       :alchemist
   "ウォーリーダー"     :war-leader
   "ミスティック"       :mistic})

(def key->str
  {:fighter    "ファイター"
   :grappler    "グラップラー"
   :fencer      "フェンサー"
   :shooter     "シューター"
   :sorcerer    "ソーサラー"
   :conjurer    "コンジャラー"
   :priest      "プリースト"
   :fairy-tamer "フェアリーテイマー"
   :magitech    "マギテック"
   :demon-ruler "デーモンルーラー"
   :druid       "ドルイド"
   :grimoire    "グリモワール"
   :scout       "スカウト"
   :ranger      "レンジャー"
   :sage        "セージ"
   :enhancer    "エンハンサー"
   :bird        "バード"
   :rider       "ライダー"
   :alchemist   "アルケミスト"
   :war-leader  "ウォーリーダー"
   :mistic      "ミスティック"})

(defn level-sum
  "Returns summation of levels specified by category of character."
  [character category]
  (reduce + (map :level (filter #(get-in abilities [(% :name) category]) (character :abilities)))))

(defn level-if
  "Returns ability level of character specified by ability-name.
  Returns nil if the character does not have it."
  [character ability-name]
  (some-> (filter #(= (% :name) ability-name) (character :abilities))
          (first)
          (:level)))
