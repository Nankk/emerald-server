(ns emerald-server.handler.items
  (:require [emerald-server.db :as db]
            [emerald-server.parser.excel :as excel-parser]
            reitit.coercion.spec))

(def item-categories
  ["weapons" "armors"])

(def items-routes
  (into ["/items" {:swagger {:tags ["items"]}}]
        (for [category item-categories]
          [(str "/" category)
           {:get {:summary    (str "Returns " category " data.")
                  :handler    (fn [_]
                                {:status 200
                                 :body   (get-in @db/*data [:items category])})}}])))
