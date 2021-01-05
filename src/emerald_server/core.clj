(ns emerald-server.core
  (:gen-class)
  (:require [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.adapter.jetty :as jetty]
            [muuntaja.core :as m]
            [emerald-server.handler.cbb-data :as cbb-data]
            [emerald-server.handler.character-data :as character-data]
            [ring.middleware.cors :refer [wrap-cors]]))

;; App

(def app
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc  true
             :swagger {:info {:title "sword-world-api-docs"}}
             :handler (swagger/create-swagger-handler)}}]
     character-data/character-data-routes
     cbb-data/cbb-data-routes]
    {:conflicts nil
     :exception pretty/exception
     :data      {:coercion   reitit.coercion.spec/coercion
                 :muuntaja   m/instance
                 :middleware [swagger/swagger-feature
                              parameters/parameters-middleware
                              muuntaja/format-negotiate-middleware
                              muuntaja/format-response-middleware
                              exception/exception-middleware
                              muuntaja/format-request-middleware
                              coercion/coerce-response-middleware
                              coercion/coerce-request-middleware
                              multipart/multipart-middleware
                              #(wrap-cors % :access-control-allow-origin [#".*"]
                                          :access-control-allow-methods [:get :put :post :delete])]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path   "/"
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
  (character-data/init-duratom-bindings db-host db-port)
  (start))

(comment
  (-main))
