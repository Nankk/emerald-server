(ns emerald-server.handler.tables
  (:require [emerald-server.db :as db]
            [emerald-server.parser.excel :as excel-parser]
            reitit.coercion.spec))

(def table-categories
  ["exp"
   "high-level-character"])

(def tables-routes
  (into ["/tables" {:swagger {:tags ["tables"]}}]
        (for [category table-categories]
          [(str "/" category)
           {:get {:summary (str "Returns " category " table.")
                  :handler (fn [_]
                             {:status 200
                              :body   (get-in @db/*data [:tables (keyword category)])})}}])))
