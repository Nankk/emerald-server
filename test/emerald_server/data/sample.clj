(ns emerald-server.data.sample)

(def character
  {:uuid        "sample"
   :idx         999
   :personality {:name    {:first "サンプルマン" :middle "string" :last "string"}
                 :age     999
                 :gender  "male"
                 :species {:name "人間"}}
   :status      {:hp           20
                 :mp           10
                 :g            1000
                 :exp          {:total 5000 :used 3500}
                 :achievements {:total 10 :used 2}
                 :honor        20
                 :impurity     0}
   :parameters  {:base    {:physique 15 :technique 2 :mind 7}
                 :initial {:dex 8 :agi 7 :str 15 :con 17 :int 1 :pow 2}
                 :growth  {:dex 1 :agi 1 :str 1 :con 1 :int 1 :pow 1}}
   :equipments  {}
   :accessories {:head       []
                 :foot       []
                 :neck       []
                 :face       []
                 :back       []
                 :waist      []
                 :others     []
                 :left-hand  []
                 :ear        []
                 :right-hand []}
   :items       []
   :languages   [{:name "交易共通語" :oral true :literal true}]
   :abilities   [{:name "ファイター" :level 2}]
   :skills      {:general {:auto [] :manual ["全力攻撃"]}}
   :options     {:notes  {:title "ノート1" :body "1つ目のノート"}
                 :nature {:born        {:abstract "string" :detail "string"}
                          :infancy     {:event     {:abstract "string" :detail "string"}
                                        :happening {:abstract "string"}}
                          :childhood   {:event     {:abstract "string" :detail "string"}
                                        :happening {:abstract "string"}}
                          :adolescence {:event            {:abstract "string" :detail "string"}
                                        :departure-reason {:abstract "string"}}}}})

(def character-full
  {:uuid             "sample"
   :idx              999
   :personality      {:name    {:first "サンプルマン" :middle "string" :last
                                "string"}
                      :age     999
                      :gender  "male"
                      :species {:name "人間"}}
   :status           {:impurity 0
                      :max-hp   39
                      :max-mp   10
                      :exp      {:free 1500 :total 5000 :used 3500}
                      :achievements {:total 10 :used 2 :free 8}
                      :honor    20
                      :level    2
                      :g        1000
                      :hp       20
                      :mp       10}
   :parameters       {:base    {:physique 15 :technique 2 :mind 7}
                      :initial {:dex 8 :agi 7 :str 15 :con 17 :int 1 :pow 2}
                      :growth  {:dex 1 :agi 1 :str 1 :con 1 :int 1 :pow 1}
                      :total   {:dex 11 :agi 10 :str 31 :con 33 :int 9 :pow 10}
                      :bonus   {:dex 1 :agi 1 :str 5 :con 5 :int 1 :pow 1}}
   :equipments       {}
   :accessories      {:head       []
                      :foot       []
                      :neck       []
                      :face       []
                      :back       []
                      :waist      []
                      :others     []
                      :left-hand  []
                      :ear        []
                      :right-hand []}
   :items            []
   :languages        [{:name "交易共通語" :oral true :literal true}]
   :abilities        [{:name "ファイター" :level 2}]
   :reference-values {:damage-bonus  7
                      :defense       0
                      :hit           3
                      :dodge         3
                      :resistance    {:con 7 :pow 3}
                      :magical-power {}
                      :mobility      {:limited 3 :normal 10 :sprint 30}
                      :packages
                      {:scout  {:dex 0 :agi 0 :int 0}
                       :ranger {:dex 0 :agi 0 :int 0}
                       :sage   {:int 0}}}
   :skills           {:general {:auto [] :manual ["全力攻撃"]}}
   :options          {:notes  {:title "ノート1" :body "1つ目のノート"}
                      :nature {:born {:abstract "string" :detail "string"}
                               :infancy
                               {:event     {:abstract "string" :detail "string"}
                                :happening {:abstract "string"}}
                               :childhood
                               {:event     {:abstract "string" :detail "string"}
                                :happening {:abstract "string"}}
                               :adolescence
                               {:event            {:abstract "string" :detail "string"}
                                :departure-reason {:abstract "string"}}}}})
