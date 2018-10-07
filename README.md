** note ** the build file included is a stub and does not do anything at this point. I'm still working on figuring out a method to build the rules api files via gradle as the original system was 100% ant based. I'll update the readme when the build system is complete. If you have the skills and wanted to build a gradle build script for this in the meantime, well, it'd be awesome if you shared it back to the project!

# Project Brief
The D20PRO Rules API provides a mechanism for customization of the internal rules behaviors of D20PRO's. The results of a specific rules api build should be a rules.jar file named according to the system it provides. These files are then pushed to D20PRO's Judge/AddOns/Rules directory and will be available as a selectable rule(s) during campaign creation. For existing campaigns, the added rules will be available via the Options->Rules->Game System drop down selection.

To help folks get a sense of what we're doing and where we're at on this API, I've left the commented out code in place. While this is not the prettiest code I've worked on, it does the job well and will get a beautification/orginazation clean up pass in the relatively near future -- time permitting.

For your benefit, we've opted to include the full set of available rules currently provided to D20PRO. This includes the following definitions:

* Dungeons & Dragons (3.5)
* Dungeons & Dragons (4.0)
* Dungeons & Dragons (5.0)
* Esper Genesis (D&D5e based)
* Pathfinder (3.5 OGL)
* Pathfinder 2.0 (very, very loosely built at this point)
* Starfinder

# Project Rules
First of all, we are extremely open to contributions. If you build out a system spec and would like to include it in the repository, please feel free to contact us or request a merge (if you forked the project).

Other than that, we ask that you are polite and courteous to the community in any comments or communications. The code provided is for a game project and some of it may not follow todays standards or best practices. We're happy to update the code accordingly, however, please keep in mind that this work is the culmination of many years of development (on the core D20PRO application) and standards change more often then most folks realize!

Secondly, if you wish to publically share your rules, please be courteous to the content creators. If you're building out a new game system, you should not include proprietary information in the public rules which would remove the need for a player of that system to purchase the content from the creator. We are not trying take funds from the folks who are making the content we love, but rather provide tools to play that content online and with like minded gamers.

So all this boils down to, "don't be a jerk." That pretty much sums up the rules!  The rest is in the usage license.

# D20PRO-Rules-API Class/Feature Overview
This includes the following main areas of interest:

* Abilities
  * The **Ability** class is repsonible for definiting creature statistics as well as short names, index id's and various helper methods
* Armor Class
  * The **AC** class is responsible for managing Amror Class for definition. Currently the system has presets for a single armor class configuration, however, you can provide content here to build out pre-built "Custom AC's" -- a feature in D20PRO which allows for numeric AC values. With some scripting and what not, this system can be used to build out multiple AC types which are calculated.
* Attack
  * The **Attack** class is used to define how attack actions are resolved. Currently, the Attack method uses a boolean signature for behaviors. This will get updated to provide an overloaded method which uses boolean flags to configure options. As well as an options map to determine what the values translate to. Attack types are also defined here, along with the damage multipliers by type. Finally, logic to handle attack description formating is included as a means to allow full thematic overhauls.
* Actions
  * The **Actions** class provides access to configuration of the Action Map and some text elements which are system specific for action types, such as Attack or Ability usage.
* Attack Qualities
  * The **ATTACK_QUALITIES** class is a map class which provices a combined list, burst qualities, physical qualities and conditions for use in attacks, spells, and other abilities usage.
* Creature Class
  * The **CreatureClass** class controls the max level by rule set as well as providing the hook for the upcoming custome sheet view. CreatureClass also defines the base values for a creature class template (class library item). Oddly, this is also where you'll find the calculation for Base Attack Bonus / Proficience Bonus (basically, the what do I add to my action attempt based on my class and level). We'll also find helper methods for hit points, saves, and more as they related to the Creature Class Template model. The most important bit is that the ability score conversion and calculate code is located in this class. This is used when moving creatures between game system and/or importing from legacy or external sources.
* D20Preferences Model Add-ons
  * The **D20PreferencesModel_AddOn** class provides access to rules specific preference configuration options. This is used in conjunction with the **UI** class to provide custom rules UI.
* Damage Resistence
  * The **DR** class simply provides access to the ATTACK_QUALITIES which are applicable to Damage Resistence
* Duration
  * The **Duration** class supplies the visualization and conseptual logic for time in the specific rule set
* Energy Resistence
  * The **ER** class simply provides access to the ATTACK_QUALITIES which are applicable to Energy Resistence, as well as a formating method for these properties.
* Feature Recharge
  * [work-in-progress] The **FeatureRecharge** class is a stub for the recharge ability system
* Features
  * The **Features** class provides naming conventions for feature/spell definitions in the Flow Diagram editor. I would advice against changing these elements at this time.
* Game System
  * The **GameSystem** class provides legacy information on possible plugins. The element that is still in use is the getSystem() method which should return the current system name.
* Grapple
  * The **Grapple** class is for handling grapple rules based on your current game system
* Hit Points
  * The **HP** class provives all the HP related things you could need. However, logic of hit points should be resolved in the *CreatureClass* class first.
* Modify Groups and Targets
  * The **ModifyGroupsAndTargets** class definies which feature flow options are avialable for the given rule set. By default, we enable all feature flow options.
* Modify Type
  * The **ModifyType** class defines which modifications (from feature/spells/etc.) can stack and should count as Modifiers.  
* Money
  * The **Money** class provides configuration options for the look and feel and function of currency on a creature
* Movement Type
  * The **MovementType** class provides maps for movement action types, log and overlay text as well as single character short-hand for the movement types.
* Preferences
  * The **Prefs** class provides a map used with the **D20PreferencesModel_AddOn** class to set default values for specific preferences
* Save
  * The **Save** class is where you define saving throw types and assign hot-keys. This can be overridden to provide a number of challenge options depending on the system.
* Size
  * The **Size** class defines what creature sizes are available and what impact being a specific size will have on various elements on the game.
* Skill
  * The **Skill** class defines max ranks, skill table headers and creature sheet UI labels. Skill modifiers are calculated in the **CreatureClass** class.
* Spell
  * The **Spell** class defines how the spell interface is presented for each class. If a class is added to the system without a presentation definition, the class will use the GENERIC_SPELL_<> options.  Additionally, this is where you set max spell level and a pile of other options!
* Stabilization Rules
  * The **Stabilization** class is a stub for future stabilization logic. This can be expanded to override the default behaviors.
* Template
  * The **Template** class contains the mapping for map templates. These are the Game Tool->Templates for area effects and the like.
* Trait
  * Currently, the **Trait** class provides the map of pre-defined "sources". This is primarily a game developer tool, to allow for more rapid creation of traits in the Rules Library.
* Type
  * The **Type** class defines the core creature types and subtypes available to the Creature Class Templates. As much as possible, this system is being deprecated. However, for now, it is required for the system to function.
* Usage
  * The **Usage** class defines the base usage types. These are for use with the legacy abilities system while the new feature system provides usage templates via the Rules Template Library.
* User Interface 
  * The **UI** class provides the ability to build out new user interface functionality. Primarily this system is designed to provide new top-bar buttons. Look at the 5th edition rules for an example of usage.
