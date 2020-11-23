(ns emerald-server.handler.duratom
  (:require [duratom.core :refer [duratom destroy]]))

;;; Public ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn bind-duratom [row]
  (duratom :postgres-db
           :db-config "postgresql://test:test@localhost:5432/mydb"
           :table-name "mytable"
           :row-id row))

(defn create-character-duratom [id row-idx character]
  (duratom :postgres-db
           :db-config "postgresql://test:test@localhost:5432/mydb"
           :table-name "mytable"
           :row-id row-idx
           :init (assoc {:uuid id} :data character)))
