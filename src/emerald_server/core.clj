(ns emerald-server.core
  (:gen-class)
  (:require [emerald-server.db :as db]
            [emerald-server.handler.cbb :as cbb]
            [emerald-server.handler.characters :as characters]
            [emerald-server.handler.items :as items]
            [emerald-server.handler.tables :as tables]
            [clojure.spec.alpha :as s]
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
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.walk :as walk]))

(defonce options (atom {:base-path "/"}))

;; App

;; If app is defined by `def`, `options` above will not be reflected
(defn app []
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc  true
             :swagger {:info     {:title "sword-world-api-docs"}
                       :basePath (get @options :base-path)}
             :handler (swagger/create-swagger-handler)}}]
     ["/"
      ;; Only for development; reloads all data from xlsx files
      ["reload-data" {:no-doc true
                      :get    db/load-all-data}]]
     characters/characters-routes
     cbb/cbb-routes
     items/items-routes
     tables/tables-routes]
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

(defonce server (atom nil))
(def port 3000)

(defn stop []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn start []
  (stop)
  (reset! server (jetty/run-jetty (app) {:port port :join? false}))
  (println "server running in port " port))

(defn -main [& {:strs [db-host db-port base-path]
                :or   {db-host "localhost" db-port 5432}
                :as   opts}]
  (println "db-host: " db-host ", db-port: " db-port ", options: " opts)
  (s/check-asserts true)
  (reset! options (walk/keywordize-keys opts))
  (characters/init-duratom-bindings db-host db-port)
  (db/load-all-data)
  (start))

(comment
  (-main)
  (-main "base-path" "/sw-tools-api/v1"))
