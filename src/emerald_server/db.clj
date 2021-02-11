(ns emerald-server.db
  (:require [emerald-server.parser.excel :as excel-parser]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [clojure.data.json :as json]))

;; State

(def *data (atom {}))

(defn- read-data [book-relpos]
  (println book-relpos)
  (let [path      (str "data/" book-relpos ".xlsx")
        data (excel-parser/read-workbook-from-resource path)]
    data))

(defn- parse-json-record [data]
  (println "parse-json-record")
  (walk/postwalk
   (fn [x]
     (if (and (map? x) (some #(re-find #"([A-z]+)-json" %) (keys x)))
       (let [json-keys    (filter identity (map #(re-find #"([A-z]+)-json" %) (keys x)))
             parsed-pairs (into {} (for [[cur-key new-key] json-keys]
                                     [new-key (when-not (str/blank? (x cur-key))
                                                (json/read-str (x cur-key)))]))]
         (merge (apply dissoc x (map first json-keys)) parsed-pairs))
       x))
   data))

(defn- arrange-subtables [data]
  (let [*sub-tables (atom #{})
        included    (walk/postwalk
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
        redirected    (walk/postwalk
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

(defn- remove-nil-values
  "Dissocs keys binded by nil recursively."
  [data]
  (println "remove-nil-values")
  (walk/postwalk
   (fn [item]
     (if (map? item)
       (into {} (filter #(some? (second %)) item))
       item))
   data))

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
  (let [*all-books-data (atom {})]
    (doseq [relpos book-relposs]
      (swap! *all-books-data assoc-in ,,
             (map keyword (str/split relpos #"/"))
             (-> (read-data relpos)
                 (parse-json-record)
                 (arrange-subtables)
                 (arrange-redirects)
                 (remove-nil-values))))
    (reset! *data @*all-books-data)))
