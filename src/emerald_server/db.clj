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

(defn- remove-comment-column [data]
  (println "remove-comment-column")
  (walk/postwalk
   (fn [item]
     (if (map? item)
       (into {} (filter #(not= (ffirst %) \#) item))
       item))
   data))

(defn- parse-json-record
  "Parse json string corresponding to the key with `-json` postfix in map data."
  [data]
  (println "parse-json-record")
  (walk/postwalk
   (fn [x]
     (let [pattern #"([A-z-_]+)-json"]
       (if (and (map? x) (some #(re-find pattern %) (keys x)))
         (let [json-keys  (filter identity (map #(re-find pattern %) (keys x)))
               parsed-map (into {} (for [[cur-key new-key] json-keys]
                                     [new-key (when-not (str/blank? (x cur-key))
                                                (json/read-str (x cur-key)))]))]
           (merge (apply dissoc x (map first json-keys)) parsed-map))
         x)))
   data))

(defn- parse-list-record
  "Parse list items string corresponding to the key with `-list` postfix in map data."
  [data]
  (println "parse-list-record")
  (walk/postwalk
   (fn [x]
     (let [pattern #"([A-z-_]+)-list"]
       (if (and (map? x) (some #(re-find pattern %) (keys x)))
         (let [list-keys  (filter identity (map #(re-find pattern %) (keys x)))
               parsed-map (into {} (for [[cur-key new-key] list-keys]
                                     [new-key (let [s (x cur-key)]
                                                (when-not (str/blank? s)
                                                  (str/split s #",")))]))]
           (merge (apply dissoc x (map first list-keys)) parsed-map))
         x)))
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
   "items"
   "magics"
   "equipments"
   "costs"
   "tables"])

(def mappify-targets
  ["items" "equipments" "costs"])

(defn mappify-sheet [data-sheetwise]
  ;; Simple loop is better than group-by here because uuids should be unique
  (into {} (for [item data-sheetwise]
             [(item "uuid") item])))

(defn mappify-book [data-bookwise]
  (into {} (apply merge (for [[k data-sheetwise] data-bookwise]
                          (mappify-sheet data-sheetwise)))))

(defn add-uuid-based-map [data targets]
  (let [transient (atom data)]
    (doseq [target targets]
      (let [ks (keys (data target))]
        (swap! transient assoc ,,
               (str (name target) "-map")
               (mappify-book (data target)))))
    @transient))

(defn load-all-data [& _]
  (println "load-all-data")
  (let [*all-books-data (atom {})]
    (doseq [relpos book-relposs]
      (swap! *all-books-data assoc-in ,,
             (str/split relpos #"/")
             (-> (read-data relpos)
                 (remove-comment-column)
                 (parse-json-record)
                 (parse-list-record)
                 (arrange-subtables)
                 (arrange-redirects)
                 (remove-nil-values))))
    (let [mappified (add-uuid-based-map
                     @*all-books-data
                     mappify-targets)]
      (reset! *data (walk/keywordize-keys mappified)))))
