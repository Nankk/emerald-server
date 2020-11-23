(defproject emerald-server "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring "1.8.1"]
                 [ring/ring-json "0.2.0"]
                 [hiccup "1.0.5"]
                 [compojure "1.6.2"]
                 [environ "1.2.0"]
                 [camel-snake-kebab "0.4.2"]
                 [dk.ative/docjure "1.14.0"]
                 [duratom "0.5.2"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "9.4-1206-jdbc42"]
                 [ring-cors "0.1.13"]]
  :resource-paths ["resources"]
  :main ^:skip-aot emerald-server.core
  :target-path "target/%s"
  :jvm-opts ["-Xmx768M"]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
