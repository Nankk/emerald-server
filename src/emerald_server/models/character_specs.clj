(in-ns 'emerald-server.models.character)
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

(do ;; Buffs
  (s/def :buffs.buff/target spec/string?)
  (s/def :buffs.buff/value spec/nat-int?)
  (s/def :buffs.buff/remaining-rounds spec/nat-int?)
  (s/def :buffs.buff.attack-type/types #{"物理" "武器" "近接" "射撃" "魔法"
                                         "地" "水・氷" "炎" "風" "雷" "純エネルギー" "断空" "衝撃"
                                         "毒" "病気" "精神効果" "精神効果(弱)" "呪い"})
  (s/def :buffs.buff/attack-type (s/coll-of :buffs.buff.attack-type/types))
  (s/def :buffs.buff.valid-to/types #{"蛮族" "アンデッド"}) ; TODO Fill this
  (s/def :buffs.buff/valid-to (s/coll-of :buffs.buff.valid-to/types))
  (s/def :buffs.buff.when/occation #{"騎乗"}) ; TODO Fill this
  (s/def :buffs.buff.when/and (s/coll-of :buffs.buff.when/occation))
  (s/def :buffs.buff.when/or (s/coll-of :buffs.buff.when/occation))
  (s/def :buffs.buff/when (s/keys :opt-un [:buffs.buff.when/and :buffs.buff.when/or]))
  (s/def :buffs/buff (s/keys :req-un [:buffs.buff/target :buffs.buff/value]
                             :opt-un [:buffs.buff/remaining-rounds :buffs.buff/attack-type
                                      :buffs.buff/valid-to :buffs.buff/when]))
  (s/def ::buffs (s/coll-of :buffs/buff))
  (s/def ::buffs-summary (s/coll-of :buffs/buff)))

(do ;; Status (core & full-specification)
  (s/def :status/hp spec/int?)
  (s/def :status/mp spec/int?)
  (s/def :status/g spec/nat-int?)
  (s/def :status.exp/used spec/nat-int?)
  (s/def :status.exp/total spec/nat-int?)
  (s/def :status.exp/free spec/nat-int?)
  (s/def :status.core/exp (s/keys :req-un [:status.exp/used :status.exp/total]))
  (s/def :status/exp (s/keys :req-un [:status.exp/used :status.exp/total :status.exp/free]))
  (s/def :status.achievements/used spec/nat-int?)
  (s/def :status.achievements/total spec/nat-int?)
  (s/def :status.achievements/free spec/nat-int?)
  (s/def :status.core/achievements (s/keys :req-un [:status.achievements/used :status.achievements/total]))
  (s/def :status/achievements (s/keys :req-un [:status.achievements/used :status.achievements/total :status.achievements/free]))
  (s/def :status/honor spec/nat-int?)
  (s/def :status/impurity spec/nat-int?)

  ;; Core
  ;; If posted with some values missing, default values must be set in server side
  (s/def :status.core/status (s/keys :opt-un [:status/hp
                                              :status/mp
                                              :status/g
                                              :status.core/exp
                                              :status.core/achievements
                                              :status/honor
                                              :status/impurity
                                              ::buffs]))

  ;; Secondary
  (s/def :status/level spec/pos-int?)
  (s/def :status/max-hp spec/pos-int?)
  (s/def :status/max-mp spec/pos-int?)
  ;; Full
  (s/def ::status (s/keys :req-un [:status/hp
                                   :status/mp
                                   :status/g
                                   :status/exp
                                   :status/achievements
                                   :status/honor
                                   :status/impurity
                                   :status/level
                                   :status/max-hp
                                   :status/max-mp]
                          :opt-un [::buffs])))

(do ;; Parameters (core & full-specification)
  (s/def :parameters.base/physique spec/pos-int?)
  (s/def :parameters.base/technique spec/pos-int?)
  (s/def :parameters.base/mind spec/pos-int?)
  (s/def :parameters/base (s/keys :req-un [:parameters.base/physique
                                           :parameters.base/technique
                                           :parameters.base/mind]))
  (s/def :parameters/dex spec/nat-int?)
  (s/def :parameters/agi spec/nat-int?)
  (s/def :parameters/str spec/nat-int?)
  (s/def :parameters/con spec/nat-int?)
  (s/def :parameters/int spec/nat-int?)
  (s/def :parameters/pow spec/nat-int?)
  (s/def :parameters/initial (s/keys :req-un [:parameters/dex :parameters/agi :parameters/str
                                              :parameters/con :parameters/int :parameters/pow]))
  (s/def :parameters/growth (s/keys :req-un [:parameters/dex :parameters/agi :parameters/str
                                             :parameters/con :parameters/int :parameters/pow]))
  (s/def :parameters.buff/dex spec/int?)
  (s/def :parameters.buff/agi spec/int?)
  (s/def :parameters.buff/str spec/int?)
  (s/def :parameters.buff/con spec/int?)
  (s/def :parameters.buff/int spec/int?)
  (s/def :parameters.buff/pow spec/int?)
  (s/def :parameters/buff (s/keys :req-un [:parameters.buff/dex :parameters.buff/agi :parameters.buff/str
                                           :parameters.buff/con :parameters.buff/int :parameters.buff/pow]))

  ;; Core
  (s/def :parameters.core/parameters (s/keys :req-un [:parameters/base
                                                      :parameters/initial]
                                             :opt-un [:parameters/growth]))

  ;; Full
  (s/def :parameters/total (s/keys :req-un [:parameters/dex :parameters/agi :parameters/str
                                            :parameters/con :parameters/int :parameters/pow]))
  (s/def :parameters/total-buffed (s/keys :req-un [:parameters/dex :parameters/agi :parameters/str
                                                   :parameters/con :parameters/int :parameters/pow]))
  (s/def :parameters/bonus (s/keys :req-un [:parameters/dex :parameters/agi :parameters/str
                                            :parameters/con :parameters/int :parameters/pow]))
  (s/def :parameters/bonus-buffed (s/keys :req-un [:parameters/dex :parameters/agi :parameters/str
                                                   :parameters/con :parameters/int :parameters/pow]))
  (s/def :parameters.secondary/parameters (s/keys :req-un [:parameters/buff
                                                           :parameters/total
                                                           :parameters/total-buffed
                                                           :parameters/bonus
                                                           :parameters/bonus-buffed]))
  (s/def ::parameters (s/merge :parameters.core/parameters
                               :parameters.secondary/parameters)))

(do ;; Reference values (full-specification only)
  (s/def :reference-values/damage-bonus spec/nat-int?)
  (s/def :reference-values/defense spec/nat-int?)
  (s/def :reference-values.resistance/con spec/nat-int?)
  (s/def :reference-values.resistance/pow spec/nat-int?)
  (s/def :reference-values/resistance (s/keys :req-un [:reference-values.resistance/con
                                                       :reference-values.resistance/pow]))
  (s/def :reference-values/hit spec/nat-int?)
  (s/def :reference-values/dodge spec/nat-int?)
  (s/def :reference-values.magical-power/sorcerer spec/nat-int?)
  (s/def :reference-values.magical-power/conjurer spec/nat-int?)
  (s/def :reference-values.magical-power/priest spec/nat-int?)
  (s/def :reference-values.magical-power/fairy-tamer spec/nat-int?)
  (s/def :reference-values.magical-power/magitech spec/nat-int?)
  (s/def :reference-values.magical-power/demon-ruler spec/nat-int?)
  (s/def :reference-values.magical-power/druid spec/nat-int?)
  (s/def :reference-values.magical-power/grimoire spec/nat-int?)
  (s/def :reference-values/magical-power (s/keys :opt-un [:reference-values.magical-power/sorcerer
                                                          :reference-values.magical-power/conjurer
                                                          :reference-values.magical-power/priest
                                                          :reference-values.magical-power/fairy-tamer
                                                          :reference-values.magical-power/magitech
                                                          :reference-values.magical-power/demon-ruler
                                                          :reference-values.magical-power/druid
                                                          :reference-values.magical-power/grimoire]))
  (s/def :reference-values.mobility/limited spec/pos-int?)
  (s/def :reference-values.mobility/normal spec/pos-int?)
  (s/def :reference-values.mobility/sprint spec/pos-int?)
  (s/def :reference-values/mobility (s/keys :req-un [:reference-values.mobility/limited
                                                     :reference-values.mobility/normal
                                                     :reference-values.mobility/sprint]))
  (s/def :reference-values.packages/dex spec/nat-int?)
  (s/def :reference-values.packages/agi spec/nat-int?)
  (s/def :reference-values.packages/int spec/nat-int?)
  (s/def :reference-values.packages/scout (s/keys :req-un [:reference-values.packages/dex
                                                           :reference-values.packages/agi
                                                           :reference-values.packages/int]))
  (s/def :reference-values.packages/ranger (s/keys :req-un [:reference-values.packages/dex
                                                            :reference-values.packages/agi
                                                            :reference-values.packages/int]))
  (s/def :reference-values.packages/sage (s/keys :req-un [:reference-values.packages/int]))
  (s/def :reference-values/packages (s/keys :req-un [:reference-values.packages/scout
                                                     :reference-values.packages/ranger
                                                     :reference-values.packages/sage]))
  (s/def ::reference-values (s/keys :req-un [:reference-values/damage-bonus
                                             :reference-values/defense
                                             :reference-values/resistance
                                             :reference-values/hit
                                             :reference-values/dodge
                                             :reference-values/magical-power
                                             :reference-values/mobility
                                             :reference-values/packages])))

(do ;; Items
  (s/def :items.item/uuid spec/string?)
  (s/def :items.item/name spec/string?)
  (s/def :items.item/count spec/pos-int?)
  (s/def :items/item (s/keys :req-un [:items.item/uuid
                                      :items.item/name
                                      :items.item/count]))
  (s/def ::items (s/map-of :items.item/uuid :items/item)))

(do ;; Equipments
  (s/def :equipments/uuid spec/string?)
  (s/def :equipments/name spec/string?)
  (s/def :equipments.weapons/weapon (s/keys :req-un [:equipments/uuid
                                                     :equipments/name]))
  (s/def :equipments/weapons (s/coll-of :equipments.weapons/weapon))
  (s/def :equipments/shield (s/keys :req-un [:equipments/uuid
                                             :equipments/name]))
  (s/def :equipments/armor (s/keys :req-un [:equipments/name
                                            :equipments/uuid]))
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
  (s/def :languages/name spec/string?)
  (s/def :languages.language/oral spec/boolean?)
  (s/def :languages.language/literal spec/boolean?)
  (s/def :languages/language (s/keys :req-un [:languages.language/oral
                                              :languages.language/literal]))
  (s/def ::languages (s/map-of :languages/name :languages/language)))

(do ;; Abilities
  (s/def :abilities/name spec/string?)
  (s/def :abilities/level spec/nat-int?)
  (s/def ::abilities (s/map-of :abilities/name :abilities/level)))

(do ;; Skills
  (s/def :skills.general/auto   (s/coll-of spec/string?))
  (s/def :skills.general/manual (s/coll-of spec/string?))
  (s/def :skills/general        (s/keys :opt-un [:skills.general/auto :skills.general/manual]))
  (s/def :skills/sorcerer       (s/coll-of spec/string?))
  (s/def :skills/conjurer       (s/coll-of spec/string?))
  (s/def :skills/priest         (s/coll-of spec/string?))
  (s/def :skills/fairy-tamer    (s/coll-of spec/string?))
  (s/def :skills/enhancer       (s/coll-of spec/string?))
  (s/def :skills.bird/songs     (s/coll-of spec/string?))
  (s/def :skills.bird/finales   (s/coll-of spec/string?))
  (s/def :skills/bird           (s/keys :req-un [:skills.bird/songs :skills.bird/finales]))
  (s/def :skills/rider          (s/coll-of spec/string?))
  (s/def :skills/alchemist      (s/coll-of spec/string?))
  (s/def :skills/war-leader     (s/coll-of spec/string?))
  (s/def :skills/mistic         (s/coll-of spec/string?))
  (s/def :skills/artisan        (s/coll-of spec/string?))
  (s/def ::skills (s/keys :opt-un [:skills/general
                                   :skills/sorcerer
                                   :skills/conjurer
                                   :skills/priest
                                   :skills/fairy-tamer
                                   :skills/enhancer
                                   :skills/bird
                                   :skills/rider
                                   :skills/alchemist
                                   :skills/war-leader
                                   :skills/mistic
                                   :skills/artisan])))

(do ;; Options
  (s/def :image/base64 spec/string?)
  (s/def :image/size spec/pos-int?)
  (s/def :image/extension spec/string?)
  (s/def :image/width spec/pos-int?)
  (s/def :image/height spec/pos-int?)
  (s/def ::image (s/keys :req-un [:image/size
                                  :image/extension
                                  :image/width
                                  :image/height]
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

  ;; ::character-core only contains primary variables;
  ;; no other information is needed for registration and update
  (s/def :character.core/character (s/keys :req-un [::personality
                                                    :status.core/status
                                                    :parameters.core/parameters
                                                    ::items
                                                    ::equipments
                                                    ::accessories
                                                    ::languages
                                                    ::abilities
                                                    ::skills]
                                           :opt-un [::uuid
                                                    ::idx
                                                    ::options]))

  ;; ::character is full specification of character;
  ;; contains both primary and secondary variables
  (s/def ::character (s/keys :req-un [::uuid
                                      ::idx
                                      ::personality
                                      ::status
                                      ::parameters
                                      ::reference-values
                                      ::items
                                      ::equipments
                                      ::accessories
                                      ::languages
                                      ::abilities
                                      ::skills]
                             :opt-un [::options]))

  ;; ::character-summary contains minimum info;
  ;; provided e.g. for listing characters
  (s/def ::character-summary (s/keys :req-un [::uuid
                                              ::idx
                                              :personality/name
                                              :personality/age
                                              :personality/species]
                                     :opt-un [::image])))

;;; Characters ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::characters (s/coll-of ::character))
(s/def ::characters-summary (s/coll-of ::character-summary))
