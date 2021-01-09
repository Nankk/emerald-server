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
   "レンジャーセージ"   #{:other}
   "エンハンサー"       #{:other}
   "バード"             #{:other}
   "ライダー"           #{:other}
   "アルケミスト"       #{:other}
   "ウォーリーダー"     #{:other}
   "ミスティック"       #{:other}})

(def key->name
  {:sorcerer    "ソーサラー"
   :conjurer    "コンジャラー"
   :priest      "プリースト"
   :fairy-tamer "フェアリーテイマー"
   :magitech    "マギテック"
   :demon-ruler "デーモンルーラー"
   :druid       "ドルイド"
   :grimoire    "グリモワール"})

(defn level-sum
  "Returns summation of levels specified by category of character."
  [character category]
  (reduce + (map :level (filter #(get-in abilities [(% :name) category]) (character :abilities)))))
