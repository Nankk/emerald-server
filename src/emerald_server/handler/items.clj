(ns emerald-server.handler.items
  (:require [emerald-server.db :as db]
            [emerald-server.parser.excel :as excel-parser]
            [spec-tools.spec :as spec]
            reitit.coercion.spec))

(def item-categories
  ["equipments"
   "items"
   "costs"])

(defn item-show [category]
  (fn [{{:keys [id]} :path-params}]
    (if-let [item (get-in @db/*data [(keyword (str category "-map")) (keyword id)])]
      {:status 200 :body item}
      {:status 400 :body {:errors [(str "No item found with id " id ".")]}})))

(def items-routes
  ["/items" {:swagger {:tags ["items"]}}
   (for [category item-categories]
     [(str "/" category)
      [""
       {:get {:summary (str "Returns " category " data.")
              :handler (fn [_]
                         {:status 200
                          :body   (get @db/*data (keyword category))})}}]
      ["/:id"
       {:get {:summary (str "Returns a single data from " category)
              :parameters {:path {:id spec/string?}}
              :handler (item-show category)}}]])])
