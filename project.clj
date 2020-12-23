(defproject emerald-server "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring-cors "0.1.13"]
                 [dk.ative/docjure "1.14.0"]
                 [duratom "0.5.2"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "9.4-1206-jdbc42"]
                 [metosin/reitit "0.5.10"]
                 [org.clojure/data.codec "0.1.1"]]
  :resource-paths ["resources"]
  :main ^:skip-aot emerald-server.core
  :target-path "target/%s"
  :jvm-opts ["-Xmx512M"]
  :profiles {:dev {:dependencies [[ring/ring-mock "0.3.2"]]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
