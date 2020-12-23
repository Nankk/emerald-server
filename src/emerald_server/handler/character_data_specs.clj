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
(s/def :name/first spec/string?)
(s/def :name/middle spec/string?)
(s/def :name/last spec/string?)
(s/def ::name (s/keys :req-un [:name/first] :opt-un [:name/middle :name/last]))
(s/def ::age spec/nat-int?)
(s/def ::gender #{"male" "female" "lgbttiqq2sa"})
(s/def ::species spec/string?)
(s/def ::job spec/string?)
(s/def ::exp spec/nat-int?)
(s/def :image/base64 spec/string?)
(s/def :image/size spec/pos-int?)
(s/def :image/extension spec/string?)
(s/def ::image (s/keys :req-un [:image/size :image/extension]
                       :opt-un [:image/base64]))
(s/def ::options spec/map?)

;; Parameters
(s/def :parameters/physique spec/nat-int?)
(s/def :parameters/technique spec/nat-int?)
(s/def :parameters/mind spec/nat-int?)
(s/def :parameters/base (s/keys :req-un [:parameters/physique :parameters/technique :parameters/mind]))
(s/def :parameters/dex spec/nat-int?)
(s/def :parameters/agi spec/nat-int?)
(s/def :parameters/str spec/nat-int?)
(s/def :parameters/con spec/nat-int?)
(s/def :parameters/int spec/nat-int?)
(s/def :parameters/pow spec/nat-int?)
(s/def :parameters/initial (s/keys :req-un [:parameters/dex :parameters/agi :parameters/str :parameters/con :parameters/int :parameters/pow]))
(s/def :parameters/growth (s/keys :req-un [:parameters/dex :parameters/agi :parameters/str :parameters/con :parameters/int :parameters/pow]))
(s/def ::parameters (s/keys :req-un [:parameters/base :parameters/initial] :opt-un [:parameters/growth]))

;; Abilities
(s/def :abilities/ability spec/string?)
(s/def :abilities/ability-level spec/nat-int?)
(s/def ::abilities (s/map-of :abilities/ability :abilities/ability-level))

;; Skills
(s/def ::skills (s/coll-of spec/string?))

;; Languages
(s/def :languages/title spec/string?)
(s/def :languages/oral spec/boolean?)
(s/def :languages/literal spec/boolean?)
(s/def :languages/language (s/keys :req-un [:languages/title :languages/oral :languages/literal]))
(s/def ::languages (s/coll-of :languages/language))

;; Nature
(s/def :nature/title spec/string?)
(s/def :nature/description spec/string?)
(s/def :nature/born (s/keys :req-un [:nature/title :nature/description]))
(s/def :nature/event (s/keys :req-un [:nature/title :nature/description]))
(s/def :nature/happening (s/keys :req-un [:nature/title]))
(s/def :nature/infancy (s/keys :req-un [:nature/event :nature/happening]))
(s/def :nature/childhood (s/keys :req-un [:nature/event :nature/happening]))
(s/def :nature/departure-reason (s/keys :req-un [:nature/title]))
(s/def :nature/adolescence (s/keys :req-un [:nature/event :nature/departure-reason]))
(s/def ::nature (s/keys :req-un [:nature/born :nature/infancy :nature/childhood :nature/adolescence]))

;; Character
;; (s/def ::character (s/keys :req-un [::first]))
(s/def ::character (s/keys :req-un [::name ::age ::gender ::species ::job ::exp
                                    ::parameters ::abilities ::skills ::languages]
                           :opt-un [::nature ::image ::options]))
(s/def ::character-summary (s/keys :req-un [::uuid ::idx ::name ::age ::job ::species]
                                   :opt-un [::image]))

;;; Characters ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::characters (s/coll-of ::character))
(s/def ::characters-summary (s/coll-of ::character-summary))
