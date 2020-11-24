(ns emerald-server.core
  (:gen-class)
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword ->snake_case]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [compojure.core :as c]
            [compojure.handler :as handler]
            [emerald-server.handler.cbb-data :as cbb-data]
            [emerald-server.handler.character-data :as character-data]
            [ring.adapter.jetty :as server]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def port 3000)

;;; Middlewares

(defn wrap-kebab-case-keys [handler]
  (fn [request]
    (let [response  (->> request
                         (transform-keys #(->kebab-case-keyword % :separator \_) ,,)
                         handler)]
      (transform-keys #(->snake_case % :separator \-) response))))

;;; App

(def app
  (-> (c/routes
       character-data/character-data-routes
       cbb-data/cbb-data-routes)
      handler/site
      wrap-kebab-case-keys
      wrap-json-body
      wrap-json-response
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))

;;; Server

(defonce server (atom nil))

(defn start-server []
  (when-not @server
    (reset! server (server/run-jetty #'app {:port port :join? false}))))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn restart-server []
  (when @server
    (stop-server)
    (start-server)))

(defn -main []
  (start-server))
