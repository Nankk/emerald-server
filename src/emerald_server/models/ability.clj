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
   "スカウト"           #{:other}
   "レンジャー"         #{:other}
   "セージ"             #{:other}
   "エンハンサー"       #{:other}
   "バード"             #{:other}
   "ライダー"           #{:other}
   "アルケミスト"       #{:other}})

(def magical-key->str
  {:sorcerer    "ソーサラー"
   :conjurer    "コンジャラー"
   :priest      "プリースト"
   :fairy-tamer "フェアリーテイマー"
   :magitech    "マギテック"
   :demon-ruler "デーモンルーラー"
   :druid       "ドルイド"})

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
   "スカウト"           :scout
   "レンジャー"         :ranger
   "セージ"             :sage
   "エンハンサー"       :enhancer
   "バード"             :bird
   "ライダー"           :rider
   "アルケミスト"       :alchemist})

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
   :scout       "スカウト"
   :ranger      "レンジャー"
   :sage        "セージ"
   :enhancer    "エンハンサー"
   :bird        "バード"
   :rider       "ライダー"
   :alchemist   "アルケミスト"})

(defn level-sum
  "Returns summation of levels specified by category of character."
  [character category]
  (reduce + (vals (filter #(get-in abilities [(% 0) category]) (character :abilities)))))

(defn level-if
  "Returns ability level of character specified by ability-name.
  Returns nil if the character does not have it."
  [character ability-name]
  (some-> (filter #(= (% 0) ability-name) (character :abilities))
          (first)
          (nth ,, 1)))
