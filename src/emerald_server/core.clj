(ns emerald-server.core
  (:gen-class)
  (:require [emerald-server.db :as db]
            [emerald-server.handler.cbb :as cbb]
            [emerald-server.handler.characters :as characters]
            [emerald-server.handler.items :as items]
            [muuntaja.core :as m]
            reitit.coercion.spec
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]]))

;; App

(def app
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc  true
             :swagger {:info {:title "sword-world-api-docs"}}
             :handler (swagger/create-swagger-handler)}}]
     ["/"
      ;; Only for development; reloads all data from xlsx files
      ["reload-data" {:no-doc true
                      :get db/load-all-data}]]
     characters/characters-routes
     cbb/cbb-routes
     items/items-routes]
    {:conflicts nil
     :exception pretty/exception
     :data      {:coercion   reitit.coercion.spec/coercion
                 :muuntaja   m/instance
                 :middleware [swagger/swagger-feature
                              parameters/parameters-middleware
                              muuntaja/format-negotiate-middleware
                              muuntaja/format-response-middleware
                              #(wrap-cors % :access-control-allow-origin [#".*"]
                                          :access-control-allow-methods [:get :put :post :delete])
                              exception/exception-middleware
                              muuntaja/format-request-middleware
                              coercion/coerce-response-middleware
                              coercion/coerce-request-middleware
                              multipart/multipart-middleware]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path   "/"
      :url    "./swagger.json"
      :config {:validatorUrl     nil
               :operationsSorter "alpha"}})
    (ring/create-default-handler))))

;; Server

(def port 3000)

(defn start []
  (jetty/run-jetty #'app {:port port :join? false})
  (println "server running in port " port))

(defn -main [& {:strs [db-host db-port]
                :or   {db-host "localhost" db-port 5432}}]
  (println "db-host: " db-host ", db-port: " db-port)
  (characters/init-duratom-bindings db-host db-port)
  (db/load-all-data)
  (start))

(comment
  (-main))
