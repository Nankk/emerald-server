(ns emerald-server.handler.cbb-data
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [emerald-server.parser.excel :as excel-parser]
            [ring.util.response :refer [resource-response response]]))

(defn- born-data [_]
  (let [path      (.getPath (io/resource "data/cbb/born.xlsx"))
        born-data (excel-parser/read-workbook path)]
    (response born-data)))

(defroutes cbb-data-routes
  (GET "/cbb/born" [] born-data)
  (route/resources "/")
  (route/not-found "Not Found"))
