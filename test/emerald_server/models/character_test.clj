(ns emerald-server.models.character-test
  (:use [clojure.test])
  (:require [emerald-server.models.character :as sut]
            [emerald-server.data.sample :as sample]))

(deftest character-full-test
  (is (= (sut/character-full sample/character)
         sample/character-full)))
