(ns emerald-server.handler.cbb
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [emerald-server.db :as db]
            reitit.coercion.spec
            [spec-tools.spec :as spec]))

;; Routes

(def cbb-categories
  ["born" "infancy" "childhood" "adolescence" "happening" "name" "departure-reason"])

(def cbb-routes
  (into ["/cbb" {:swagger {:tags ["cbb"]}}]
        (for [category cbb-categories]
          [(str "/" category)
           {:get {:summary    (str "Returns character " category " data from character building book.")
                  :handler    (fn [_]
                                {:status 200
                                 :body   (get-in @db/*data [:cbb (keyword category)])})}}])))
