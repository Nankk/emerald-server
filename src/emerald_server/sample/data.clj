(ns emerald-server.sample.data)

(def character
  {:name      "ディミトリ・エディノフ"
   :age       29
   :gender    :male
   :species   "人間"
   :role      "戦士"
   :parameter {:base    {:physical 10 :talent 7 :mental 4}
               :initial {:dex 11 :agi 5 :str 7 :con 7 :int 4 :pow 4}
               :growth  {:dex 0 :agi 0 :str 0 :con 0 :int 0 :pow 0}}
   :abilities {"ファイター" 2
               "ソーサラー" 1
               "スカウト"   2}
   :skills    ["武器習熟A/ソード"
               "全力攻撃"]
   :language  {"交易共通語"   {:oral true :literal true}
               "エルフ語"     {:oral true :literal false}
               "ドワーフ後"   {:oral true :literal false}
               "魔動機文明語" {:oral false :literal true}}
   :nature    {:born        {:name        "のどかな農村"
                             :description "土壌もよく、云々"}
               :infancy     {:event     {:name        "狩人として生きる(命中重視)"
                                         :description "生活のためか、云々"}
                             :happening {:name "戦いや事故に巻き込まれたが、無傷だった"}}
               :childhood   {:event     {:name        "自然の中で学ぶ"
                                         :description "自然の中で生き、云々"}
                             :happening {:name "海を漂流した"}}
               :adolescence {:event            {:name        "一撃必殺への道"
                                                :description "ただひたすらに、云々"}
                             :departure-reason {:name "なりゆき"}}}})
