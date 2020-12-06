(in-ns 'emerald-server.handler.character-data)
(require '[clojure.spec.alpha :as s]
         '[spec-tools.spec :as spec])

;;; General ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::message spec/string?)
(s/def ::errors (s/coll-of spec/string?))
(s/def ::uuid spec/string?)
(s/def ::idx spec/pos-int?)
(s/def ::id (s/or ::uuid ::idx))

;;; Character ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Basic info
(s/def ::first spec/string?)
(s/def ::middle spec/string?)
(s/def ::last spec/string?)
(s/def ::name (s/keys :req-un [::first] :opt-un [::middle ::last]))
(s/def ::age spec/nat-int?)
(s/def ::gender #{"male" "female" "lgbttqq2sa"})
(s/def ::species spec/string?)
(s/def ::job spec/string?)

;; Parameters
(s/def ::physique spec/nat-int?)
(s/def ::technique spec/nat-int?)
(s/def ::mind spec/nat-int?)
(s/def ::base (s/keys :req-un [::physique ::technique ::mind]))
(s/def ::dex spec/nat-int?)
(s/def ::agi spec/nat-int?)
(s/def ::str spec/nat-int?)
(s/def ::con spec/nat-int?)
(s/def ::int spec/nat-int?)
(s/def ::pow spec/nat-int?)
(s/def ::initial (s/keys :req-un [::dex ::agi ::str ::con ::int ::pow]))
(s/def ::growth (s/keys :req-un [::dex ::agi ::str ::con ::int ::pow]))
(s/def ::parameters (s/keys :req-un [::base ::initial ::growth]))

;; Abilities
(s/def ::ability spec/string?)
(s/def ::ability-level spec/nat-int?)
(s/def ::abilities (s/map-of ::ability ::ability-level))

;; Skills
(s/def ::skills (s/coll-of spec/string?))

;; Languages
(s/def ::oral spec/boolean?)
(s/def ::literal spec/boolean?)
(s/def ::language-name spec/string?)
(s/def ::language-faculty (s/keys :req-un [::oral ::literal]))
(s/def ::languages (s/map-of ::language-name ::language-faculty))

;; Nature
(s/def ::title spec/string?)
(s/def ::description spec/string?)
(s/def ::born (s/keys :req-un [::title ::description]))
(s/def ::event (s/keys :req-un [::title ::description]))
(s/def ::happening (s/keys :req-un [::title]))
(s/def ::infancy (s/keys :req-un [::event ::happening]))
(s/def ::childhood (s/keys :req-un [::event ::happening]))
(s/def ::departure-reason spec/string?)
(s/def ::adolescence (s/keys :req-un [::event ::departure-reason]))
(s/def ::nature (s/keys :req-un [::born ::infancy ::childhood ::adolescence]))

;; Character
;; (s/def ::character (s/keys :req-un [::first]))
(s/def ::character (s/keys :req-un [::name ::age ::gender ::species ::job
                                    ::parameters ::abilities ::skills ::languages]
                           :opt-un [::nature]))

;;; Characters ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::characters (s/coll-of ::character))
