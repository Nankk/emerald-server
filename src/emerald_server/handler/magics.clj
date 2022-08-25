(ns emerald-server.handler.magics
  (:require [emerald-server.db :as db]
            [emerald-server.parser.excel :as excel-parser]
            [spec-tools.spec :as spec]
            reitit.coercion.spec))

;; (def magics-routes
;;   ["/magics" {:swagger {:tags ["magics"]}}
;;    [""
;;     {:get {:summary (str "Returns magics data.")
;;            :handler (fn [_]
;;                       {:status 200
;;                        :body   (get @db/*data :magics)})}}]])
