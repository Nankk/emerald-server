(ns emerald-server.handler.duratom
  (:require [duratom.core :refer [duratom]]))

(def *db-config (atom "postgresql://test:test@localhost:5432/mydb"))

(def table-name "mytable")

(defn bind-duratom [row]
  (duratom :postgres-db
           :db-config @*db-config
           :table-name table-name
           :row-id row))

(defn create-character-duratom [id row-idx character]
  (duratom :postgres-db
           :db-config @*db-config
           :table-name table-name
           :row-id row-idx
           :init {:uuid id
                  :data (assoc character
                               :uuid id
                               :idx row-idx)}))
