(ns emerald-server.handler.character-data
  (:require [compojure.core :refer [context defroutes GET POST PUT DELETE]]
            [duratom.core :refer [destroy]]
            [emerald-server.handler.duratom :refer [bind-duratom create-character-duratom]]
            [ring.util.response :refer [resource-response response]]))

(defonce uuid->rowidx! (bind-duratom 0)) ; **Row index 0 is dedicated for binding information**
(defonce characters!
  (atom (into {} (for [rowidx (vals @uuid->rowidx!)] [rowidx (bind-duratom rowidx)])))
  #_{1 character-data1!
     2 character-data2!})

(defn- rowidx->uuid [idx]
  (@(@characters! idx) :uuid))

(defn- uuid-and-rowidx
  "Generate [uuid rowidx] from identifier which can be uuid or rowidx."
  [identifier]
  (println "uuid-and-rowidx")
  (let [parsed (if (every? #(Character/isDigit %) identifier) (Integer. identifier) identifier)
        _ (println "parsed" parsed)
        uuid   (if (integer? parsed) (rowidx->uuid parsed) parsed)
        rowidx (if (integer? parsed) parsed (@uuid->rowidx! parsed))]
    (println "[parsed uuid rowidx]" [parsed uuid rowidx])
    [uuid rowidx]))

(defn- next-row-available
  "Extract the minimum available row index of duratom database."
  [characters!]
  (let [row-indices (keys @characters!)]
    (println "row-indices" row-indices)
    (if (empty? row-indices)
      1
      ;; Be aware that character data index starts from 1.
      (or (first (keep-indexed #(when (not= (inc %1) %2) (inc %1)) (sort row-indices)))
          (inc (count row-indices))))))

(defn add-character
  "Register character data to duratom database and returns {:uuid 'uuid :idx 'int}.
  This data can be used to access the registered data."
  [character]
  (let [uuid   (str (java.util.UUID/randomUUID))
        rowidx (next-row-available characters!)
        data!  (create-character-duratom uuid rowidx character)]
    (swap! uuid->rowidx! #(assoc % uuid rowidx))
    (swap! characters! #(assoc % rowidx data!))
    (println {:uuid uuid :idx rowidx})
    {:uuid uuid :idx rowidx}))

(defn delete-character [identifier]
  (let [[uuid rowidx] (uuid-and-rowidx identifier)]
    (if (and uuid rowidx)
      (do(destroy (bind-duratom rowidx))
         (swap! characters! #(dissoc % rowidx))
         (swap! uuid->rowidx! #(dissoc % uuid))
         true)
      false)))

;;; Handlers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn characters-add [{:keys [body]}]
  (println "characters-add")
  (response (add-character body)))

(defn characters-show [_]
  (response (map #((deref %) :data) (vals @characters!))))

(defn character-show [{:keys [params]}]
  (let [[_ rowidx] (uuid-and-rowidx (params :character-identifier))]
    (response (@(@characters! rowidx) :data))))

(defn character-update [{:keys [params body]}]
  (println "character-update")
  (println body)
  (let [[uuid rowidx] (uuid-and-rowidx (params :character-identifier))]
    (when rowidx
      (println rowidx)
      (let [character-duratom! (@characters! rowidx)]
        (println @character-duratom!)
        (reset! character-duratom! {:uuid uuid :data body})))))

(defn character-delete [{:keys [params]}]
  (let [identifier (params :character-identifier)]
    (delete-character identifier)))

;;; Router ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes character-data-routes
  (context "/characters" _
           (GET "/" _ characters-show)
           (POST "/add" _ characters-add)
           (context "/:character-identifier" _
                    (GET "/" _ character-show)
                    (PUT "/update" _ character-update)
                    (DELETE "/delete" _ character-delete)
                    )))
