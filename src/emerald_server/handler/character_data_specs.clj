(in-ns 'emerald-server.handler.character-data)
(require '[clojure.spec.alpha :as s]
         '[spec-tools.spec :as spec])

(do ;; General
  (s/def ::message spec/string?)
  (s/def ::errors (s/coll-of spec/string?))
  (s/def ::uuid spec/string?)
  (s/def ::idx spec/pos-int?)
  (s/def ::id (s/or ::uuid ::idx)))

;; Characters ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do ;; Personality
  (s/def :personality.name/first spec/string?)
  (s/def :personality.name/middle spec/string?)
  (s/def :personality.name/last spec/string?)
  (s/def :personality/name (s/keys :req-un [:personality.name/first]
                                   :opt-un [:personality.name/middle
                                            :personality.name/last]))
  (s/def :personality/age spec/nat-int?)
  (s/def :personality/gender #{"male" "female" "lgbttiqq2sa"})
  (s/def :personality.species/name spec/string?)
  (s/def :personality.species/origin spec/string?) ; For nightmare
  (s/def :personality.species/head spec/string?) ; For lycant
  (s/def :personality/species (s/keys :req-un [:personality.species/name]
                                      :opt-un [:personality.species/origin
                                               :personality.species/head]))
  (s/def ::personality (s/keys :req-un [:personality/name
                                        :personality/age
                                        :personality/gender
                                        :personality/species])))

(do ;; Status
  (s/def :status/hp spec/nat-int?)
  (s/def :status/mp spec/nat-int?)
  (s/def :status/exp spec/nat-int?)
  (s/def :status/honor spec/nat-int?)
  (s/def :status.buffs.buff/target spec/string?)
  (s/def :status.buffs.buff/value spec/int?)
  (s/def :status.buffs/buff (s/keys :req-un [:status.buffs.buff/target
                                             :status.buffs.buff/value]))
  (s/def :status/buffs (s/coll-of :status.buffs/buff))
  (s/def ::status (s/keys :req-un [:status/hp
                                   :status/mp
                                   :status/exp
                                   :status/honor]
                          :opt-un [:status/buffs])))

(do ;; Parameters
  (s/def :parameters.base/physique spec/nat-int?)
  (s/def :parameters.base/technique spec/nat-int?)
  (s/def :parameters.base/mind spec/nat-int?)
  (s/def :parameters/base (s/keys :req-un [:parameters.base/physique
                                           :parameters.base/technique
                                           :parameters.base/mind]))
  (s/def :parameters/dex spec/nat-int?)
  (s/def :parameters/agi spec/nat-int?)
  (s/def :parameters/str spec/nat-int?)
  (s/def :parameters/con spec/nat-int?)
  (s/def :parameters/int spec/nat-int?)
  (s/def :parameters/pow spec/nat-int?)
  (s/def :parameters/initial (s/keys :req-un [:parameters/dex
                                              :parameters/agi
                                              :parameters/str
                                              :parameters/con
                                              :parameters/int
                                              :parameters/pow]))
  (s/def :parameters/growth (s/keys :req-un [:parameters/dex
                                             :parameters/agi
                                             :parameters/str
                                             :parameters/con
                                             :parameters/int
                                             :parameters/pow]))
  (s/def ::parameters (s/keys :req-un [:parameters/base
                                       :parameters/initial]
                              :opt-un [:parameters/growth])))

(do ;; Items
  (s/def :items.item/uuid spec/string?)
  (s/def :items.item/name spec/string?)
  (s/def :items.item/count spec/pos-int?)
  (s/def :items/item (s/keys :req-un [:items.item/uuid
                                      :items.item/name
                                      :items.item/count]))
  (s/def ::items (s/coll-of :items/item)))

(do ;; Equipments
  (s/def :equipments/uuid spec/string?)
  (s/def :equipments/name spec/string?)
  (s/def :equipments.weapons/weapon (s/keys :req-un [:equipments/uuid
                                                     :equipments/name]))
  (s/def :equipments/weapons (s/coll-of :equipments.weapons/weapon))
  (s/def :equipments/shield (s/keys :req-un [:equipments/uuid
                                             :equipments/name]))
  (s/def :equipments/armor (s/keys :req-un [:equipments/uuid
                                            :equipments/name]))
  (s/def ::equipments (s/keys :opt-un [:equipments/weapons
                                       :equipments/shield
                                       :equipments/armor])))

(do ;; Accessories
  (s/def :accessories/uuid spec/string?)
  (s/def :accessories/name spec/string?)
  (s/def :accessories/accessory  (s/keys :req-un [:accessories/uuid :accessories/name]))
  (s/def :accessories/head       (s/coll-of :accessories/accessory))
  (s/def :accessories/ear        (s/coll-of :accessories/accessory))
  (s/def :accessories/face       (s/coll-of :accessories/accessory))
  (s/def :accessories/neck       (s/coll-of :accessories/accessory))
  (s/def :accessories/back       (s/coll-of :accessories/accessory))
  (s/def :accessories/right-hand (s/coll-of :accessories/accessory))
  (s/def :accessories/left-hand  (s/coll-of :accessories/accessory))
  (s/def :accessories/waist      (s/coll-of :accessories/accessory))
  (s/def :accessories/foot       (s/coll-of :accessories/accessory))
  (s/def :accessories/others     (s/coll-of :accessories/accessory))
  (s/def ::accessories (s/keys :req-un [:accessories/head
                                        :accessories/ear
                                        :accessories/face
                                        :accessories/neck
                                        :accessories/back
                                        :accessories/right-hand
                                        :accessories/left-hand
                                        :accessories/waist
                                        :accessories/foot
                                        :accessories/others])))

(do ;; Languages
  (s/def :languages.language/name spec/string?)
  (s/def :languages.language/oral spec/boolean?)
  (s/def :languages.language/literal spec/boolean?)
  (s/def :languages/language (s/keys :req-un [:languages.language/name
                                              :languages.language/oral
                                              :languages.language/literal]))
  (s/def ::languages (s/coll-of :languages/language)))

(do ;; Abilities
  (s/def :abilities.ability/name spec/string?)
  (s/def :abilities.ability/level spec/nat-int?)
  (s/def :abilities/ability (s/keys :req-un [:abilities.ability/name :abilities.ability/level]))
  (s/def ::abilities (s/coll-of :abilities/ability)))

(do ;; Skills
  (s/def :skills/general      (s/coll-of spec/string?))
  (s/def :skills/enhancer     (s/coll-of spec/string?))
  (s/def :skills.bird/songs   (s/coll-of spec/string?))
  (s/def :skills.bird/finales (s/coll-of spec/string?))
  (s/def :skills/bird         (s/keys :req-un [:skills.bird/songs
                                               :skills.bird/finales]))
  (s/def :skills/rider        (s/coll-of spec/string?))
  (s/def :skills/alchemist    (s/coll-of spec/string?))
  (s/def :skills/war-leader   (s/coll-of spec/string?))
  (s/def :skills/mistic       (s/coll-of spec/string?))
  (s/def :skills/artisan      (s/coll-of spec/string?))
  (s/def ::skills (s/keys :opt-un [:skills/general :skills/enhancer :skills/bird :skills/rider
                                   :skills/alchemist :skills/war-leader :skills/mistic :skills/artisan])))

(do ;; Options
  (s/def :image/base64 spec/string?)
  (s/def :image/size spec/pos-int?)
  (s/def :image/extension spec/string?)
  (s/def ::image (s/keys :req-un [:image/size
                                  :image/extension]
                         :opt-un [:image/base64]))
  (s/def :notes/title spec/string?)
  (s/def :notes/body spec/string?)
  (s/def ::notes (s/keys :req-un [:notes/title
                                  :notes/body]))
  (s/def :nature/abstract spec/string?)
  (s/def :nature/detail spec/string?)
  (s/def :nature/born (s/keys :req-un [:nature/abstract :nature/detail]))
  (s/def :nature/event (s/keys :req-un [:nature/abstract :nature/detail]))
  (s/def :nature/happening (s/keys :req-un [:nature/abstract]))
  (s/def :nature/infancy (s/keys :req-un [:nature/event :nature/happening]))
  (s/def :nature/childhood (s/keys :req-un [:nature/event :nature/happening]))
  (s/def :nature/departure-reason (s/keys :req-un [:nature/abstract]))
  (s/def :nature/adolescence (s/keys :req-un [:nature/event :nature/departure-reason]))
  (s/def ::nature (s/keys :req-un [:nature/born :nature/infancy :nature/childhood :nature/adolescence]))
  (s/def ::options (s/keys :opt-un [::image ::notes ::nature])))

(do ;; Character
  (s/def ::character (s/keys :req-un [::personality
                                      ::status
                                      ::parameters
                                      ::items
                                      ::equipments
                                      ::accessories
                                      ::languages
                                      ::abilities
                                      ::skills]
                             :opt-un [::options]))
  (s/def ::character-summary (s/keys :req-un [::uuid
                                              ::idx
                                              :personality/name
                                              :personality/age
                                              :personality/species]
                                     :opt-un [::image])))

;;; Characters ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::characters (s/coll-of ::character))
(s/def ::characters-summary (s/coll-of ::character-summary))
