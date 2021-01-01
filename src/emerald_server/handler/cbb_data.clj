(ns emerald-server.handler.cbb-data
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.walk :as w]
            [emerald-server.parser.excel :as excel-parser]
            reitit.coercion.spec
            [spec-tools.spec :as spec]))

(defn- read-data [book-name]
  (println book-name)
  (let [path      (str "data/cbb/" book-name ".xlsx")
        born-data (excel-parser/read-workbook-from-resource path)]
    born-data))

(defn- arrange-subtables [data]
  (let [*sub-tables (atom #{})
        included    (w/postwalk
                     #(if (and (map? %) (% "sub-table"))
                        (let [sub-key  (% "sub-table")
                              sub-data (data sub-key)]
                          (swap! *sub-tables conj ,, sub-key)
                          (-> % (dissoc ,, "sub-table") (assoc ,, sub-key sub-data)))
                        %)
                     data)]
    (apply dissoc included @*sub-tables)))

(defn- arrange-redirects [data]
  (let [*destinations (atom #{})
        redirected    (w/postwalk
                       #(if (and (coll? %)
                                 (not-empty %)
                                 (map? (% 0))
                                 (some #{"redirect-from"} (keys (% 0))))
                          (into {} (for [item %]
                                     (do
                                       (swap! *destinations conj ,, (item "redirect-to"))
                                       [(item "redirect-from") (data (item "redirect-to"))])))
                          %)
                       data)]
    (apply dissoc redirected @*destinations)))

;; Routes

(def cbb-categories
  ["born" "infancy" "childhood" "adolescence" "happening" "name" "departure-reason"])

(def cbb-data-routes
  (into ["/cbb" {:swagger {:tags ["cbb"]}}]
        (for [category cbb-categories]
          [(str "/" category)
           {:get {:summary    (str "Returns character " category " data from character building book.")
                  :handler    (fn [_]
                                {:status 200
                                 :body   (-> (read-data category)
                                             (arrange-subtables ,,)
                                             (arrange-redirects ,,)
                                             )})}}])))
