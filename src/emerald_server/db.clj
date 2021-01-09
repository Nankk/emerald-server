(ns emerald-server.db
  (:require [emerald-server.parser.excel :as excel-parser]
            [clojure.walk :as w]
            [clojure.string :as str]))

;; State

(def *data (atom {}))

(defn- read-data [book-relpos]
  (println book-relpos)
  (let [path      (str "data/" book-relpos ".xlsx")
        data (excel-parser/read-workbook-from-resource path)]
    data))

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

(def book-relposs
  ["cbb/adolescence"
   "cbb/born"
   "cbb/childhood"
   "cbb/departure-reason"
   "cbb/happening"
   "cbb/infancy"
   "cbb/name"
   "items"])

(defn load-all-data [& _]
  (println "load-all-data")
  (let [*raw (atom {})]
    (doseq [relpos book-relposs]
      (swap! *raw assoc-in ,,
             (map keyword (str/split relpos #"/"))
             (read-data relpos)))
    (reset! *data (-> @*raw
                      (arrange-subtables)
                      (arrange-redirects)))))
