(ns emerald-server.handler.cbb-data
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes context GET POST]]
            [compojure.route :as route]
            [emerald-server.parser.excel :as excel-parser]
            [ring.util.response :refer [resource-response response]]))

(defn- read-data [book-name]
  (println book-name)
  (let [path      (.getPath (io/resource (str "data/cbb/" book-name ".xlsx")))
        born-data (excel-parser/read-workbook path)]
    (response born-data)))

(defroutes cbb-data-routes
  (context "/cbb" _
           (GET "/born" _ (read-data "born"))
           (GET "/infancy" _ (read-data "infancy"))
           (GET "/childhood" _ (read-data "childhood"))
           (GET "/adolescence" _ (read-data "adolescence"))
           (GET "/happening" _ (read-data "happening"))
           )
  (route/not-found "Not Found..."))
