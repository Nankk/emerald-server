(ns emerald-server.handler.character-data
  (:require [clojure.pprint :as pp]
            [duratom.core :refer [destroy]]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]
            [clojure.data.codec.base64 :as b64-codec]
            [emerald-server.handler.duratom
             :refer
             [bind-duratom create-character-duratom]]
            [clojure.java.io :as io]))

(load "character_data_specs")

(defonce *uuid->rowidx (bind-duratom 0)) ; **Row index 0 is dedicated for binding information**
(defonce *characters
  (atom (into {} (for [rowidx (vals @*uuid->rowidx)] [rowidx (bind-duratom rowidx)])))
  #_{1 character-data1!
     2 character-data2!})

(defn- rowidx->uuid [idx]
  (when-let [character (@*characters idx)]
    (@character :uuid)))

(defn- uuid-and-rowidx
  "Generate [uuid rowidx] from identifier which can be uuid or rowidx."
  [identifier]
  (let [parsed (if (every? #(Character/isDigit %) identifier) (Integer. identifier) identifier)
        uuid   (if (integer? parsed) (rowidx->uuid parsed) parsed)
        rowidx (if (integer? parsed) parsed (@*uuid->rowidx parsed))]
    (println "[parsed uuid rowidx]" [parsed uuid rowidx])
    (if (and uuid rowidx)
      [uuid rowidx]
      [nil nil])))

(defn- next-row-available
  "Extract the minimum available row index of duratom database."
  [*characters]
  (let [row-indices (keys @*characters)]
    (if (empty? row-indices)
      1
      ;; Be aware that character data index starts from 1.
      (or (first (keep-indexed #(when (not= (inc %1) %2) (inc %1)) (sort row-indices)))
          (inc (count row-indices))))))

(defn add-character
  "Register character data to duratom database and returns {:uuid 'uuid :idx 'int}.
  This data can be used to access the registered data."
  [character uuid]
  (println "add-character")
  (let [rowidx (next-row-available *characters)
        data!  (create-character-duratom uuid rowidx
                                         (if (get-in character [:image :base64])
                                           (update character :image dissoc :base64)
                                           character))]
    (swap! *uuid->rowidx #(assoc % uuid rowidx))
    (swap! *characters #(assoc % rowidx data!))
    {:uuid uuid :idx rowidx}))

(defn save-image [{{:keys [base64 extension]} :image} uuid]
  (println "save-image")
  (when (and base64 extension)
    (println "saving image..." base64 extension)
    (io/copy (b64-codec/decode (.getBytes base64))
             (java.io.File. (str (.getPath (io/resource (str "img/profile"))) "/" uuid "." extension)))))

(defn delete-character [uuid rowidx]
  (let [extension (get-in (deref (@*characters rowidx)) [:data :image :extension])]
    (when extension
      (io/delete-file (str (.getPath (io/resource (str "img/profile"))) "/" uuid "." extension) true))
    (destroy (bind-duratom rowidx))
    (swap! *characters #(dissoc % rowidx))
    (swap! *uuid->rowidx #(dissoc % uuid))))

;; Handlers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn characters-show [_]
  {:status 200 :body (mapv #((deref %) :data) (vals @*characters))})

(defn characters-add [{{:keys [body]} :parameters}]
  (println "characters-add")
  (let [uuid (str (java.util.UUID/randomUUID))]
    (save-image body uuid)
    {:status 200 :body (assoc (add-character body uuid) :message "Character successfully added.")}))

(defn characters-summary [_]
  (println "characters-summary")
  (let [summary (for [[k v] @*characters
                      :let  [{:keys [uuid data]} (deref v)
                             {{:keys [name age species]} :personality
                              {:keys [image]}            :options} data]]
                  (merge {:uuid uuid :idx k} {:name name :age age :species species :image image}))]
    {:status 200 :body summary}))

(defn character-show [{{:keys [id]} :path-params}]
  (println "character-show")
  (println "id: " id)
  (let [[_ rowidx] (uuid-and-rowidx id)]
    {:status 200 :body (@(@*characters rowidx) :data)}))

(defn character-update [{{:keys [id]} :path-params {:keys [body]} :parameters}]
  (println "character-update")
  (let [[uuid rowidx] (uuid-and-rowidx id)]
    (if rowidx
      (let [character-duratom! (@*characters rowidx)]
        (reset! character-duratom! {:uuid uuid :data body})
        {:status 200 :body {:message (str "Character " id " updated.")}})
      {:status 400 :body {:errors ["Character not found."]}})))

(defn character-delete [{{:keys [id]} :path-params}]
  (let [[uuid rowidx] (uuid-and-rowidx id)]
    (println "delete, rowidx" rowidx)
    (if rowidx
      (do
        (delete-character uuid rowidx)
        {:status 200 :body {:message (str "Character " id " deleted.")}})
      {:status 400 :body {:errors ["Character not found."]}})))

(defn character-image-show [{{:keys [id]} :path-params}]
  (println "character-image-show")
  (let [[uuid rowidx] (uuid-and-rowidx id)
        extension (get-in (deref (@*characters rowidx)) [:data :image :extension])
        file (io/resource (str "img/profile/" uuid "." extension))]
    {:status 200
     :headers {"Content-Type" (str "image/" extension)}
     :body (io/input-stream file)}))

;; Router ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def character-data-routes
  ["/characters"
   {:swagger {:tags ["characters"]}}
   [""
    {:get {:summary   "Returns all characters."
           :responses {200 {:body ::characters}}
           :handler   characters-show}
     :post {:summary   "Register a character and returns its id."
            :parameters {:body ::character}
            :responses {200 {:body (s/keys :req-un [::message ::uuid ::idx])}}
            :handler   characters-add}}]
   ["/summary"
    {:get {:summary   "Returns all characters' summary list."
           :responses {200 {:body ::characters-summary}}
           :handler   characters-summary}}]
   ["/:id"
    [""
     {:get {:summary   "Returns a character specified by id."
            :parameters {:path {:id ::id}}
            :responses {200 {:body ::character}}
            :handler   character-show}
      :put {:summary   "Update a character specified by id."
            :parameters {:path {:id ::id}
                         :body ::character}
            :responses {200 {:body (s/keys :req-un [::message])}
                        400 {:body (s/keys :req-un [::errors])}}
            :handler   character-update}
      :delete {:summary   "Delete a character specified by id."
               :parameters {:path {:id ::id}}
               :responses {200 {:body (s/keys :req-un [::message])}
                           400 {:body (s/keys :req-un [::errors])}}
               :handler   character-delete}}]
    ["/image"
     {:get {:summary "Downloads a profile image file."
            :parameters {:path {:id ::id}}
            :handler character-image-show}}]]])
