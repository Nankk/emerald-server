;;; https://www.spantree.net/blog/2015/03/06/clojure-read-spreadsheet.html

(ns emerald-server.parser.excel
  (:require [dk.ative.docjure.spreadsheet :as sp]))

(defn- read-sheet [sht]
  (let [rows      (sp/row-seq sht)
        col-names (for [c (sp/cell-seq (first rows))] (sp/read-cell c))]
    (mapv #(zipmap col-names %) (for [r (rest rows)]
                                  (for [c (sp/cell-seq r)]
                                    (sp/read-cell c))))))

(defn read-workbook-from-resource [path]
  (let [wb        (sp/load-workbook-from-resource path)
        shts      (sp/sheet-seq wb)
        sht-names (for [sht shts] (sp/sheet-name sht))
        sht-data  (for [sht shts] (read-sheet sht))]
    (zipmap sht-names sht-data)))
