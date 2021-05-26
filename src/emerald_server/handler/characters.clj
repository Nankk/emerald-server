(ns emerald-server.handler.characters
  (:require [clojure.data.codec.base64 :as b64-codec]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as sql]
            [clojure.pprint :as pp]
            [clojure.spec.alpha :as s]
            [duratom.core :refer [destroy]]
            [emerald-server.models.character :as character]
            [emerald-server.handler.duratom :as emerald.duratom :refer [bind-duratom create-character-duratom]]
            [environ.core :refer [env]]))

(declare *uuid->rowidx)
(declare *characters)

(defn init-duratom-bindings
  [db-host db-port]
  ;; TODO Do something to this insanely dirty point
  (reset! emerald.duratom/*db-config (str "postgresql://test:test@" db-host ":" db-port "/mydb"))
  ;; Check connection
  (loop [retry 0]
    (if (< retry 10)
      (let [db (try (sql/get-connection @emerald.duratom/*db-config) (catch Exception _ false))]
        (if db
          (.close db)
          (do (println "Connection " @emerald.duratom/*db-config " failed. Retrying...")
              (Thread/sleep 5000)
              (recur (inc retry)))))
      (throw (Exception. (str "Failed to connect to " @emerald.duratom/*db-config " after " retry " retrying.")))))
  ;; Duratom binding
  (def *uuid->rowidx (bind-duratom 0)) ; **Row index 0 is dedicated for binding information**
  (def *characters
    (atom (into {} (for [rowidx (vals @*uuid->rowidx)] [rowidx (bind-duratom rowidx)])))
    #_{1 character-data1!
       2 character-data2!}))

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
  [char-raw uuid]
  (println "add-character")
  (let [rowidx     (next-row-available *characters)
        char-nopic (if (get-in char-raw [:options :image :base64])
                     (update-in char-raw [:options :image] dissoc :base64)
                     char-raw)
        ;; character-full must be called here: Spec check before registration
        char-full  (character/character-full (assoc char-nopic :uuid uuid :idx rowidx))
        data!      (create-character-duratom uuid rowidx char-nopic)]
    (swap! *uuid->rowidx #(assoc % uuid rowidx))
    (swap! *characters #(assoc % rowidx data!))
    char-full))

(def local-dir (str (env :home) "/.emerald"))

(defn save-image [{{{:keys [base64 extension]} :image} :options} uuid]
  (println "save-image")
  (when (and base64 extension)
    (let [file-path (str local-dir "/img/profile/" uuid "." extension)]
      (println "saving image to " file-path "...")
      (io/make-parents file-path)
      (io/copy (b64-codec/decode (.getBytes base64))
               (io/file file-path)))))

(defn copy-image [char-from char-to]
  (println "copy-image")
  (let [ext       (get-in char-from [:options :image :extension])
        path-from (str local-dir "/img/profile/" (char-from :uuid) "." ext)
        path-to   (str local-dir "/img/profile/" (char-to :uuid) "." ext)]
    (when (.exists (io/file path-from))
      (println "Copying " path-from "...")
      (io/copy (io/file path-from) (io/file path-to)))))

(defn delete-file-if [path]
  (println "delete-file-if")
  (when (.exists (io/file path))
    (println "deleting " path "...")
    (io/delete-file path)))

(defn delete-character [uuid rowidx]
  (let [extension (get-in (deref (@*characters rowidx)) [:data :options :image :extension])]
    (when extension
      (delete-file-if (str local-dir "/img/profile/" uuid "." extension)))
    (destroy (bind-duratom rowidx))
    (swap! *characters #(dissoc % rowidx))
    (swap! *uuid->rowidx #(dissoc % uuid))))

;; Handlers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn characters-show [_]
  {:status 200 :body (mapv #(character/character-full ((deref %) :data)) (vals @*characters))})

(defn characters-add [{{:keys [body]} :parameters}]
  (println "characters-add")
  (let [uuid (str (java.util.UUID/randomUUID))]
    (try
      (let [char-full (add-character body uuid)]
        (save-image body uuid)
        {:status 200 :body char-full})
      (catch Exception e
        (let [{{{:keys [extension]} :image} :options} body
              file-path (str local-dir "/img/profile/" uuid "." extension)]
          (delete-file-if file-path))
        (println "Returning error message...")
        {:status 500 :body (Throwable->map e)}))))

(defn characters-summary [_]
  (println "characters-summary")
  (let [summary (for [[k v] @*characters
                      :let  [{:keys [uuid data]} (deref v)
                             {{:keys [name age species]} :personality
                              {:keys [image]}            :options} data]]
                  (into {} (remove (comp nil? second)
                                   (merge {:uuid uuid :idx k}
                                          {:name name :age age :species species :image image}))))]
    {:status 200 :body summary}))

(defn character-show [{{:keys [id]} :path-params}]
  (println "character-show")
  (println "id: " id)
  (let [[_ rowidx] (uuid-and-rowidx id)]
    {:status 200 :body (character/character-full (@(@*characters rowidx) :data))}))

(defn character-copy [{{:keys [id]} :path-params}]
  (println "character-copy [" id "]")
  (let [[uuid rowidx] (uuid-and-rowidx id)]
    (let [original    (@(@*characters rowidx) :data)
          new-uuid    (str (java.util.UUID/randomUUID))
          copied-full (add-character original new-uuid)]
      (copy-image original copied-full)
      {:status 200 :body copied-full})))

(defn character-update [{{:keys [id]} :path-params {:keys [body]} :parameters}]
  (println "character-update")
  (let [[uuid rowidx] (uuid-and-rowidx id)]
    (if rowidx
      (let [character-duratom! (@*characters rowidx)
            char-data (assoc body :uuid uuid :idx rowidx)]
        (reset! character-duratom! {:uuid uuid :data char-data})
        {:status 200 :body (character/character-full char-data)})
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
        extension (get-in (deref (@*characters rowidx)) [:data :options :image :extension])]
    (println extension)
    {:status 200
     :headers {"Content-Type" (str "image/" (or extension "png"))
               "Cache-Control" "no-store"}
     :body (io/input-stream (let [path (str local-dir "/img/profile/" uuid "." extension)]
                              (if (.exists (io/file path))
                                (io/file path)
                                (io/resource "img/character/default.png"))))}))

;; Router ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def characters-routes
  ["/characters"
   {:swagger {:tags ["characters"]}}
   [""
    {:get {:summary   "Returns all characters."
           :responses {200 {:body ::character/characters}}
           :handler   characters-show}
     :post {:summary   "Registers a character and returns full data with index and uuid."
            :parameters {:body :character.core/character}
            :responses {200 {:body ::character/character}}
            :handler   characters-add}}]
   ["/summary"
    {:get {:summary   "Returns all characters' summary list."
           :responses {200 {:body ::character/characters-summary}}
           :handler   characters-summary}}]
   ["/:id"
    [""
     {:get {:summary   "Returns a character specified by id."
            :parameters {:path {:id ::character/id}}
            :responses {200 {:body ::character/character}}
            :handler   character-show}
      :post {:summary   "Copies the existing character specified by id and returns full data."
             :parameters {:path {:id ::character/id}}
             :responses {200 {:body ::character/character}}
             :handler   character-copy}
      :put {:summary   "Updates a character specified by id and returns full data."
            :parameters {:path {:id ::character/id}
                         :body :character.core/character}
            :responses {200 {:body ::character/character}
                        400 {:body (s/keys :req-un [::character/errors])}}
            :handler   character-update}
      :delete {:summary   "Deletes a character specified by id."
               :parameters {:path {:id ::character/id}}
               :responses {200 {:body (s/keys :req-un [::character/message])}
                           400 {:body (s/keys :req-un [::character/errors])}}
               :handler   character-delete}}]
    ["/image"
     {:get {:summary "Downloads a profile image file."
            :parameters {:path {:id ::character/id}}
            :handler character-image-show}}]
    ]])
