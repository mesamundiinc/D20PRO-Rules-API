package com.d20pro.plugin.rules.fivee;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import com.d20pro.temp_extraction.plugin.feature.model.Feature;
import com.d20pro.temp_extraction.plugin.feature.model.trigger.FeatureTrigger;
import com.mindgene.d20.LAF;
import com.mindgene.d20.common.*;
import com.mindgene.d20.common.console.ValueWithUnit;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.creature.CreatureTemplate_Classes;
import com.mindgene.d20.common.creature.ResistanceModel;
import com.mindgene.d20.common.creature.attack.AttackOptionsMemory;
import com.mindgene.d20.common.creature.attack.DeclaredCreatureAttack;
import com.mindgene.d20.common.dice.Dice;
import com.mindgene.d20.common.dice.DiceFormatException;
import com.mindgene.d20.common.game.AbstractCreatureInPlay;
import com.mindgene.d20.common.game.GenericEffectModel;
import com.mindgene.d20.common.game.creatureclass.CreatureClassTemplate;
import com.mindgene.d20.common.game.creatureclass.GenericCreatureClass;
import com.mindgene.d20.common.game.spell.GenericSpell;
import com.mindgene.d20.common.options.D20PreferencesModel;
import com.mindgene.d20.common.rules.Constants_ArmorClass;

import org.apache.commons.lang.StringUtils;

import com.sengent.common.logging.LoggingManager;
import org.apache.log4j.Logger;

/**
 * 5th Ed rules specific lookup methods and other routines that define the game.
 *
 * This class extends through class substitution, the D20Rules class. It allows
 * for loading of replacement rules for 5e
 *
 * @author owlbear, alexey
 */
@SuppressWarnings({ "rawtypes" })
public class FiveERules extends AbstractRules
{
  private static final Logger lg = Logger.getLogger(FiveERules.class);

  protected FiveERules()
  {
    super();
  }

  public static class Stabilization{
    public static final boolean useStabilization = true;

    public static List<FeatureTrigger> getStabilizationTriggers(){
      return new ArrayList<>();
    }
    public static boolean isStabilizationRequired(AbstractCreatureInPlay creatureInPlay){
      return true;
    }


  }
  
  public static class Ability
  {
    public static final byte STR = 0;
    public static final byte DEX = 1;
    public static final byte CON = 2;
    public static final byte INT = 3;
    public static final byte WIS = 4;
    public static final byte CHA = 5;
    public static final byte NONE = -127;

    public static final byte[] IDS = { STR, DEX, CON, INT, WIS, CHA };

    public static final String[] NAMES = { "STR", "DEX", "CON", "INT", "WIS", "CHA" };

    public static final String[] FULL_NAMES = { "Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom",
        "Charisma" };

    public static String getName(byte idStat)
    {
      if (idStat == NONE)
        return "None";
      return NAMES[idStat];
    }

    public static String getFullName(byte idStat)
    {
      if (idStat == NONE)
        return "None";
      return FULL_NAMES[idStat];
    }

    /**
     * Gets the name of the ability by it's ID (or index, see above, e.g. STR).
     *
     * @param name
     * @return
     */
    public static byte getID(String name)
    {
      for (byte i = 0; i < NAMES.length; i++)

        if (NAMES[i].equalsIgnoreCase(name.trim()))
          return i;

      // System.out.println("[Rules.getID] Attempted to fetch " + name);
      throw new IllegalArgumentException(name + " not a valid Ability");
    }

    public static boolean contains(String name)
    {      
      for (byte i = 0; i < NAMES.length; i++)
        if (NAMES[i].equalsIgnoreCase(name.trim()))
          return true;

      // System.out.println("[Rules.contains] Attempted to check " + name);
      return false;
    }

    public static String getAbl(String fullname)
    {
      for (byte i = 0; i < FULL_NAMES.length; i++)

        if (FULL_NAMES[i].equalsIgnoreCase(fullname.trim()))
          return NAMES[i];

      // System.out.println("[Rules.getID] Attempted to fetch " + fullname);
      throw new IllegalArgumentException(fullname + " not a valid Ability");

    }

    public static int getMod(int score)
    {
      if (score >= 10)
        return (score - 10) / 2;
      return (11 - score) / -2;
    }

    public static int getPoints(int score)
    {
      if (score <= 8)
        return 0;
      if (score <= 14)
        return score - 8;
      if (score == 15)
        return 8;
      if (score == 16)
        return 10;
      if (score == 17)
        return 13;
      if (score == 18)
        return 16;
      return 16 + (score - 18) * 4;
    }
  }


  public static class AC // implements Constants_ArmorClass
  {
    public static final short NO_MAX_DEX = -1;
    
    public static final int BASE_AC = 10;
    public static final boolean REQUIRE_BASE_AC = false;
    
    public static final byte NATURAL = 0; // natural
    public static final byte ARMOR = 1;   // armor
    public static final byte SHIELD = 2;  // shield
    public static final byte DEFLECT = 3; // magic
    public static final byte UNNAMED = 4; // unnamed
    public static final byte DODGE = 5;   // dodge
    public static final byte LUCK = 6;    // luck

    public static final String[] NAMES = { "Natural", "Armor", "Shield", "Magic", "Unnamed", "Dodge" }; // luck

    public static final String[] LABELS = { "Base", "or", "Natural", "or", "Armor", "plus", "Dex", "plus", "Shield", "plus", "Magic", "plus", "Dodge", "plus", "Unnamed", "and", "Max Dex" };
    
    public static final String[] SYMBOLS = { "\u2192", "\u2192", "\u2192" };  // eventually ->, &, +
    
    public static final String[] OVERRIDE = { "Armor", "Natural", "Base"};
    
    public static String getName(byte idStat)
    {
      return NAMES[idStat];
    }

    public static byte getID(String name)
    {
      for (byte i = 0; i < NAMES.length; i++)

        if (NAMES[i].equals(name))
          return i;

      throw new IllegalArgumentException(name + " not a valid AC Mod");
    }
    
    public static String totalArmorClass(String natural, String armor, String shield, String deflect,
                                         String dodge, String luck, String unnamed, String size, 
                                         String dexText, int maxDexBonus)
    {
      boolean useBase = true;
      
      try
      {
        int total = 0;
                
        if (Integer.parseInt(armor) == 0 && Integer.parseInt( natural ) == 0)
          total = BASE_AC;
        
        total += Integer.parseInt( armor );      
        total += Integer.parseInt( deflect );
        total += Integer.parseInt( dodge );
        total += Integer.parseInt( unnamed );
        total += (Integer.parseInt( armor ) != 0) ? 0 : Integer.parseInt( natural );
        total += Integer.parseInt( shield );
        total += Integer.parseInt( size );

        int dex = 0;
        if( !dexText.isEmpty() )
        {
          dex = Integer.parseInt( dexText );
          if( maxDexBonus > Constants_ArmorClass.NO_MAX_DEX ) // Has a Max Dex Bonus
            if( dex > maxDexBonus ) // Dex bonus is greater then max
              dex = maxDexBonus;

          if (maxDexBonus == 0)
            dex = 0;
        }

        total += dex; // apply modified or unmodified dex bonus

        return Integer.toString( total );
      }
      catch( NumberFormatException nfe )
      {
        return "?";
      }
    }
    
    public static int resolveAC(int natural, int armor, int shield, int deflect, int dodge, int luck, int noname, int size, int dex)
    {
      int total = 0;
      
      if (armor == 0 && natural == 0)
        total = BASE_AC + shield + deflect + dodge + luck + noname + size + dex;
      else if (armor != 0)
        total = armor + shield + deflect + dodge + luck + noname + size + dex;
      else
        total = natural + shield + deflect + dodge + luck + noname + size + dex;
      
      return total;
    }

    public static int getDexBonus( int dex, int maxdex )
    {
      if (maxdex == 0)
        dex = 0;

      return dex;
    }
  }

  public static class Size
  {
    // place holders - required for token drawing logic
    public static final byte FINE = 0;
    public static final byte DIMINUTIVE = 1;
    public static final byte TINY = 2;
    public static final byte SMALL = 3;
    public static final byte MEDIUM = 4;
    public static final byte LARGE = 5;
    public static final byte HUGE = 6;
    public static final byte GARGANTUAN = 7;

    // place holders - required for token drawing logic
    public static final byte COLOSSAL = 8;
    public static final byte COLOSSAL_PLUS = 9;

    public static final String[] NAMES = { "Fine", "Diminutive", "Tiny", "Small", "Medium", "Large", "Huge",
        "Gargantuan", "Colossal", "Colossal Plus" };

    //public static byte[] MODS = { 8, 4, 2, 1, 0, -1, -2, -4, -8, -12 };
    public static byte[] MODS = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    
    // Used by AbstractCreatureInPlay to manage border size
    public static int[] PIXELS_PER_CELL = { 32, 32, 32, 24, 16 };

    // Used by GenericMapView to manage bounds
    public static double[] RESIZE_BOUNDS = { .35, .5, .65, .85, 1 };

    public static byte getID(String name)
    {
      for (byte i = 0; i < NAMES.length; i++)

        if (NAMES[i].equals(name))
          return i;

      throw new IllegalArgumentException(name + " not a valid Size");
    }

    public static String getName(byte idSize)
    {
      return NAMES[idSize];
    }

    public static byte getMod(byte idSize)
    {
      return MODS[idSize];
    }

    public static int resolveSpace(byte idSize)
    {
      final int idMedium = MEDIUM;
      if (idSize <= idMedium)
        return 1;
      return idSize - idMedium + 1;
    }

    public static int accessGrappleMod(byte idSize)
    {
      int mod = idSize - MEDIUM;
      return mod * MEDIUM;
    }
  }

  public static class Skill
  {
    public static final byte MAX_RANKS = 5;
    public static final byte MAX_BONUS = 99;
    public static final byte PASSIVE = 10;
    
    public static final String[] HEADERS = { "", "Mod", "Skill Name", "Abil", "#Prof", "Misc" };

    public static String SHOW_ALL = "Show All";
    public static String RANKED = "Proficient";
    public static String UNRANKED = "Nonproficient";
    public static String TOTAL_RANKS = "Proficiencies";

    public int getSkillBonus(CreatureTemplate template, int ranks)
    {
      // allow adding of skills without marking proficiency
      if (ranks == 0) return ranks;

      CreatureClass classtemp = new CreatureClass();
      int profb = classtemp.resolveBAB(template);
      int retv = profb * ranks;
      return retv;
    }
  }

  public static class Save
  {
    public static final int MINIMAL_DAMAGE_FOR_HALF_SAVE = 1;// 0 or 1

    public static final byte STR = 0;
    public static final byte DEX = 1;
    public static final byte CON = 2;
    public static final byte INT = 3;
    public static final byte WIS = 4;
    public static final byte CHA = 5;
    public static final byte DEATH = 6;
    
    public static final byte FORT = CON;  // should this still be 0, 1, 2?
    public static final byte REF = DEX;
    public static final byte WILL = WIS;

    public static final byte[] IDS = { STR, DEX, CON, INT, WIS, CHA, DEATH };
    
    public static final byte EVASION_SAVE = REF; // the saving throw index to check for evasion 

    // array representing the Abilities (in byte) to use when calculating saving
    // throws and modifiers
    // ORDER MATTERS!
    public static final byte[] MAP = { Ability.STR, Ability.DEX, Ability.CON, Ability.INT, Ability.WIS, Ability.CHA, Ability.NONE };
    // NOTE death saves (last position) needs to ignore stat relationships.

    public static final String[] NAMES = { "Str", "Dex", "Con", "Int", "Wis", "Cha", "Death" };

    public static final String[] FULL_NAMES = { "Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom",
        "Charisma", "Death" };

    public static final String NEGATES = "negates";
    public static final String HALF = "half";
    public static final String PARTIAL = "partial";

    public static final String[] IF_SAVED = { NEGATES, HALF };

    public static final String FAIL = "fail";
    public static final String SAVE = "save";
    public static final String IMMUNE = "immune";

    public static final String[] STATUS_CODES = { FAIL, SAVE, IMMUNE };

    public static String getName(byte idSave)
    {
      return NAMES[idSave];
    }

    public static String getFullName(byte idSave)
    {
      return FULL_NAMES[idSave];
    }

    public static void refreshSaves(CreatureTemplate t)
    {
      //noop
    }
    
//    public static void handleDeathSave( CreatureTemplate creature, int roll, int result )
//    {
//      byte success = creature.getDeathSavesSuccess();
//      byte failure = creature.getDeathSavesFailure();
//      
//      if (roll == 1) // double fail
//      {
//        failure += 2;
//      }
//      else if (roll == 20) // insta success
//      {
//        success = 0;
//        failure = 0;
//        
//        if (creature.getHP() <= 0)
//          creature.heal(Math.abs(creature.getHP() +1), false);
//      }
//      else if (result > 10) // success
//      {
//        success += 1;
//      }
//      else // failure < 10
//      {
//        failure += 1;
//      }
//      
//      creature.setDeathSavesFailure(failure);
//      creature.setDeathSavesSuccess(success);
//    }

    public static KeyStroke getHotKey(byte idSave)
    {
      switch (idSave)
      {
      case STR:
        return CommonHotKeys.Creature.SAVE_STR;
      case DEX:
        return CommonHotKeys.Creature.SAVE_DEX;
      case CON:
        return CommonHotKeys.Creature.SAVE_CON;
      case INT:
        return CommonHotKeys.Creature.SAVE_INT;
      case WIS:
        return CommonHotKeys.Creature.SAVE_WIS;
      case CHA:
        return CommonHotKeys.Creature.SAVE_CHA;
      case DEATH:
        return CommonHotKeys.Creature.SAVE_NUM7;
      }
      return null;
    }

    public static byte getID(String name)
    {
      for (byte i = 0; i < NAMES.length; i++)

        if (NAMES[i].equalsIgnoreCase(name))
          return i;

      throw new IllegalArgumentException(name + " not a valid Save ID");
    }

    public static boolean isNegates(Object o)
    {
      return NEGATES.equals(o);
    }
  }

  public static class Money
  {
    public static final byte PP = 0;
    public static final byte GP = 1;
    public static final byte EP = 2;
    public static final byte SP = 3;
    public static final byte CP = 4;

    public static final String[] NAMES = { "PP", "GP", "EP", "SP", "CP" };

    public static final String[] FULL_NAMES = { "Platinum", "Gold", "Electrum", "Silver", "Copper" };

    public static String getName(byte idSave)
    {
      return NAMES[idSave];
    }

    public static String getFullName(byte idSave)
    {
      return FULL_NAMES[idSave];
    }

    public static byte getID(String name)
    {
      for (byte i = 0; i < NAMES.length; i++)

        if (NAMES[i].equalsIgnoreCase(name))
          return i;

      throw new IllegalArgumentException(name + " not a valid Money");
    }
  }

  public static class Trait
  {
    public static final Map<String, Integer> SOURCES;
    static{
      SOURCES = new HashMap<>();
      /* Define sources for traits which are shared between class/creature types.
         sources are priotized from 0 .. # where a higher number is matched first when synchronizing.
       */
      // SOURCES.put("string", 0..9);
      SOURCES.put("Movement", 12);
      SOURCES.put("Senses", 11);
      SOURCES.put("Spells", 10);
      SOURCES.put("Abilities", 9);
      SOURCES.put("Languages", 8);
      SOURCES.put("Skills", 7);
      SOURCES.put("Special", 6);
      SOURCES.put("Feats", 5);
      SOURCES.put("Features", 4);
      SOURCES.put("Creature", 3);
      SOURCES.put("General", 2);
      SOURCES.put("Source", 1);
    }
  }

  public static class Type
  {
    public static final Map<String, Object> TYPES;
    static {
      TYPES = new HashMap<String, Object>();
      TYPES.put("Unknown", "");
      TYPES.put("Abberation", "");
      TYPES.put("Beast", "");
      TYPES.put("Celestial", "");
      TYPES.put("Construct", "");
      TYPES.put("Dragon", "");
      TYPES.put("Elemental", "");
      TYPES.put("Fey", "");
      TYPES.put("Fiend", "");
      TYPES.put("Fiend (demon)", "");
      TYPES.put("Fiend (devil)", "");
      TYPES.put("Fiend (shapechanger)", "");
      TYPES.put("Giant", "");
      TYPES.put("Humanoid", "");
      TYPES.put("Humanoid (any race)", "");
      TYPES.put("Humanoid (dwarf)", "");
      TYPES.put("Humanoid (elf)", "");
      TYPES.put("Humanoid (gnoll)", "");
      TYPES.put("Humanoid (gnome)", "");
      TYPES.put("Humanoid (goblinoid)", "");
      TYPES.put("Humanoid (grimlock)", "");
      TYPES.put("Humanoid (human)", "");
      TYPES.put("Humanoid (human, shapechanger)", "");
      TYPES.put("Humanoid (kobold)", "");
      TYPES.put("Humanoid (lizardfolk)", "");
      TYPES.put("Humanoid (merfolk)", "");
      TYPES.put("Humanoid (orc)", "");
      TYPES.put("Humanoid (sahuagin)", "");
      TYPES.put("Monstrosity", "");
      TYPES.put("Monstrosity (shapechanger)", "");
      TYPES.put("Monstrosity (titan)", "");
      TYPES.put("Ooze", "");
      TYPES.put("Plant", "");
      TYPES.put("Swarm of tiny beasts", "");
      TYPES.put("Undead", "");
      TYPES.put("Undead (shapechanger)", "");
      TYPES.put("Zombie", "");
    }
    
    public static final String[] NAMES;
    static {
      NAMES = TYPES.keySet().toArray(new String[TYPES.size()]);
    }

    public static final byte[] AttackRate = { 1 };

    public static final String[] HD = { "d8" };

    public static final String[] GoodSaves = { "Str, Dex, Con, Int, Will, Cha" };
  }
  
  public static class Template
  {
    // static managed in MapTemplate (do not change)
    public static final byte BURST = 0;
    public static final byte CONE = 1;
    public static final byte SQUARE = 2;
    public static final byte BOX = 3;
    public static final byte LINE = 4;
    public static final byte SQUARE_BLAST = 5;
    public static final byte SQUARE_BURST = 6;
    
    public static final byte NONE = -127;
    
    public static final int TEMPLATE_STEP = 1;
    public static final int TEMPLATE_MAX_UNITS = 80;
    
    public static final Map<Byte, String> TEMPLATE_MAP;
    static
    {
      TEMPLATE_MAP = new HashMap<Byte, String>();
      TEMPLATE_MAP.put(BURST, "Burst");
      TEMPLATE_MAP.put(CONE, "Cone");
      TEMPLATE_MAP.put(SQUARE, "Square");
      TEMPLATE_MAP.put(BOX, "Box");
      TEMPLATE_MAP.put(LINE, "Line");
      TEMPLATE_MAP.put(SQUARE_BLAST, "Square Blast");
      TEMPLATE_MAP.put(SQUARE_BURST, "Square Burst");
    };
    
    public ValueWithUnit[] convertToUnits(AbstractApp app)
    {
      ValueWithUnit[] items = new ValueWithUnit[ TEMPLATE_MAX_UNITS ];
      for( int i = 0; i < TEMPLATE_MAX_UNITS; i++ )
      {
        int value = i + TEMPLATE_STEP;
        items[i] = new ValueWithUnit( value, app.formatUnits( value ) );
      }
      return items;
    }
    
    // return ID list
    public Byte[] getTemplateIDs() 
    {
      return (Byte[]) TEMPLATE_MAP.keySet().toArray();
    }
    
    // return Name list
    public String[] getTemplateNames()
    {
      return (String[]) TEMPLATE_MAP.values().toArray();
    }
    
    public String getTemplateName(byte id)
    {
      if (TEMPLATE_MAP.containsKey(id))
        return TEMPLATE_MAP.get(id);
      else
        return "";
    }
    
    public byte getTemplateID(String name)
    {
      for(Map.Entry<Byte, String> e : TEMPLATE_MAP.entrySet())
      {
        if (e.getValue() == name)
          return e.getKey();
      }
      return NONE;
    }
    
  }

  public static class CreatureClass
  {
    public static final byte MAX_CLASS_LEVEL = 20;

    public static final String SHEET_TYPE = "FiveE";

    // can host hard coded class lists here.
    public static final String[] NAMES = { "Unknown" };
    public static final String[] HD = { "d8" };
    public static final byte[] AttackRate = { 1 };
    public static final byte[] GoodSaves = { 0, 0, 0, 0, 0, 0 };
    public static final String BABNAME = "Prof";

    private CreatureTemplate_Classes _classes;

    public CreatureClass()
    {
      _classes = new CreatureTemplate_Classes();
    }

    public CreatureTemplate_Classes accessClasses()
    {
      return _classes;
    }

    /**
     * For 5th Edition BAB is going to be used in more places than simply
     * attacks. BAB becomes Proficiency Bonus and is applied to certain Saves,
     * Skills, and Actions.
     *
     * @param app
     * @param creature
     * @param classes
     * @return
     */

    public int resolveBAB(CreatureTemplate creature)
    {
      CreatureTemplate_Classes classList = creature.getClasses();
      ArrayList<GenericCreatureClass> classes = classList.accessClasses();

      if (creature.hasOverrideBAB())
        return creature.getOverrideBAB().intValue();

      if (classes == null)
        return 0;

      // ArrayList<GenericCreatureClass> cls =
      // creature.getClasses().accessClasses();
      int level = 0;
      for (GenericCreatureClass c : classes)
      {
        level += (int) c.getLevel();
      }

      int BAB = 0;
      for (GenericCreatureClass creatureClass : classes)
      {
        double base = 0;
        CreatureClassTemplate classTemplate = creatureClass.accessClassTemplate();
        if (classTemplate == null)
        {
          String text = "Class Template Error: " + creatureClass.getName() + " NOT in Classes.txt  Setting ProfBonus to level * .25.";
          LoggingManager.info(CreatureTemplate_Classes.class, text);
          if (!creature.accessCR().isEmpty() && !creature.accessCR().equals("-"))
            return resolveBABbyCR(creature);
          else 
            base = Math.ceil(level * .25);
        }
        else
        {
          if (classTemplate.accessAttackRate() == 0) 
          {
            base = resolveBABbyCR(creature);
          }
          else
            base = Math.ceil(level * .25);
        }
//        base = Math.ceil(level * .25);
        BAB = (int) (1 + base); // +1 for 4 levels;
      }

      // 5e specifically
      creature.setProficiencyBonus((byte) BAB);

      return BAB;
    }
    
    // helper function to handle creature classes which use CR
    public int resolveBABbyCR(CreatureTemplate creature)
    {
      if (creature.hasOverrideBAB())
        return creature.getOverrideBAB().intValue();
              
      int CR;
      try 
      {
        CR = StringUtils.isNotBlank(creature.accessCR()) ? Integer.parseInt(creature.accessCR()) : 0;
      } 
      catch (Exception e)
      {
        CR = 1;
      }
      
      double base = Math.ceil(CR * .25);
      //      int BAB = (int) (1 + base);
      int BAB = (int) base;
      
      // 5e specifically
      //      creature.setProficiencyBonus((byte) BAB);
  
      return BAB;
    }

    public String resolveHitDie(CreatureTemplate creature)
    {
      CreatureTemplate_Classes classList = creature.getClasses();
      ArrayList<GenericCreatureClass> classes = classList.accessClasses();

      String hitDie = "d8";

      if (classes == null)
        return hitDie;

      for (GenericCreatureClass creatureClass : classes)
      {
        CreatureClassTemplate classTemplate = creatureClass.accessClassTemplate();
        if (classTemplate == null)
        {
          String text = "Class Template Error: " + creatureClass.getName()
              + " NOT in Classes.txt  Setting Hit Die to d8.";
          LoggingManager.info(CreatureTemplate_Classes.class, text);
          return hitDie;
        }
        hitDie = classTemplate.accessHitDie();
      }

      return hitDie;
    }

    public int resolveLevel(LinkedHashMap<String, GenericCreatureClass> classes)
    {
      int level = 0;

      if (classes != null)
      {
        for (GenericCreatureClass creatureClass : classes.values())
        {
          level += creatureClass.getLevel();
        }
      }

      return level;
    }

    public void calculateSaves(CreatureTemplate creature)
    {
      if (creature._calculating)
        return;
      else
        creature._calculating = true;
      
      checkHistorical(creature);

      byte[] map = Save.MAP;
      byte[] newsaves = new byte[map.length];
      byte[] abilities = creature.getAbilities();

      byte[] profsave = new byte[map.length];
      Arrays.fill(profsave, (byte) 0);

      CreatureTemplate_Classes classList = creature.getClasses();

      ArrayList<GenericCreatureClass> classes = classList.accessClasses();

      byte profbonus = (byte) resolveBAB(creature);

      if (classes.toArray().length > 0)
      {
        byte id = 0;
        // fetch proficient saves for all classes.. will need to be changed to
        // manage multiclass logic.
        String[] named = null;
        for (GenericCreatureClass c : classes)
        {
          named = new String[] {};
          
          if ( null != c.accessClassTemplate() )
            named = c.accessClassTemplate().accessSavingThrows();
          
          if (named.length > 0)
          {
            for (int i = 0; i < named.length; i++)
            {
              if (named[i].length() != 0)
              {
                if ( Ability.contains(named[i].toUpperCase()) )
                {
                  id = Ability.getID(named[i]);
                  profsave[id] = profbonus;
                }
                else
                {
                  // noop
                }
              }
            }
          } 
        }
      }

      for (byte i = 0; i < map.length; i++)
      {
        // Ability Modifier + Proficiency Bonus + Other modifiers (TODO)
        /** to extend this; add + Feature Modifiers here **/
        newsaves[i] = (byte) (creature.getSaveBase(i) + creature.accessAbilityScoreMod(map[i]) + profsave[i]+creature.getSaveMod(i));
        //newsaves[i] = (byte) (creature.getSaveBase(i) + Ability.getMod(abilities[i]) + profsave[i]);
      }

      // replace with a setSavesBase function to allow for specific modifiers to
      // saves...
      creature.setSaves(newsaves);
     
    }

    public void calculateAbilityScore(CreatureTemplate template, byte id)
    {
      if (template._calculating) {
        return;
      }
      else
        template.isCalculating(true);

      byte base = template.getAbilityScoreBase(id);
      byte bonus = template.getAbilityScoreMods(id);
      byte modified = 0;

      if (base == 127) {
        modified = (byte) -127;
        template.setAbilityScoreBase(id, modified, false);
      } else
        modified = (byte) (base + bonus);

      template.setAbilityScore(id, modified);
      template.isCalculating(false);
    }

    public void calculateAbilityScores(CreatureTemplate template)
    {
      checkHistorical(template);
      
      for(byte i : Ability.IDS)
      {
        calculateAbilityScore(template, i);
      }
      
    }
    
    // Backward compatibility check to see if a creature is sporting legacy stats.
    public void checkHistorical(CreatureTemplate template)
    {
      if (!template.isHistorical()) 
      {
        return;
      }
 
      // historical is defined as a creature made in a version older then 3.6.6.x
      // Attempt to use embedded version numbers in creatures to detected if a ctr is historical
      if (template.getVersion().contains("RC")) return;
      
      String version[] = template.getVersion().split("\\.");
      String major = (!version[0].isEmpty()) ? version[0] : "0";
      String minor = (!version[1].isEmpty()) ? version[1] : "0";
      String maint = "0";
      if (version.length > 2)
          maint = (!version[2].isEmpty()) ? version[2] : "0";

      if (Integer.valueOf(major) <= 3 && Integer.valueOf(minor) <= 6 && Integer.valueOf(maint) <= 6)
        template.isHistorical(true);
      else
        template.isHistorical(false);
      
      if( template.isHistorical() )
      {
        // System.out.println("Handling Historical Abilities");
        byte[] abilities = template.getAbilities();
        for(byte i = 0; i < abilities.length; i++ )
        {
          if ((abilities[i] == Ability.NONE) || (abilities[i] == 0))
            template.setAbilityScoreBase(i, (byte) (Ability.NONE * -1), false);
          else
            template.setAbilityScoreBase(i, abilities[i], false);
        }
        
        byte[] saves = template.getSaves();
        for(byte i = 0; i < saves.length; i++)
        {
          template.setSaveBase(i, saves[i], false);
        }
        
        template.isHistorical(false);
      }
    }
  }

  public static class Spell
  {
    public static final byte MAX_SPELL_LEVEL = 9;
    public static final byte KNOWN_SPELL_CATEGORIES = 2; // Cantrips &
                                                         // Non-Cantrips

    public static final byte MAX_DOMAIN_SPELLS = 18;
    public static final float DOMAIN_SPELL_RATE = 0.5f;
    public static final byte DOMAIN_SPELLS_PER_LEVEL = 2;

    // Note: Spell levels start at Zero so no reason to offset a check against
    // this array
    public static final int[] OVERRIDE_DEBIT_SPELLS = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    
    public static final boolean OVERRIDE_SPELL_REMOVE_FROM_MEMORY = true;
    public static final boolean USE_SECONDARY_DOMAIN = false;
    public static final boolean USE_ALL_SPELLS_IN_LINE = true;

    // on the fence about placing these in spell or creatureclass...

    public static final byte PREPARED = 0; // available spells to cast
    public static final byte KNOWN = 1; // standard spell book style
    public static final byte SPECIAL = 2; // domain/cicle/etc.
    public static final byte SLOTS = 3; // track available spell casting slots
    public static final byte POOL = 4; // all spells use a single spell pool to track usage

    public static final String[] BARD_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final boolean BARD_5E_PREPARED = true;
    public static final byte[] BARD_SPELL_UI = { PREPARED, KNOWN, SLOTS };

    public static final String[] CLERIC_SPELL_TABS = { "Spells Prepared", "Domain" };
    public static final byte[] CLERIC_SPELL_UI = { PREPARED, SPECIAL, SLOTS };
    public static final boolean CLERIC_5E_PREPARED = true;
    public static final byte CLERIC_MAX_DOMAIN_SPELLS = 18;
    public static final float CLERIC_DOMAIN_SPELL_RATE = 0.5f;
    public static final byte CLERIC_DOMAIN_SPELLS_PER_LEVEL = 2;
    public static final byte CLERIC_DOMAIN_MIN_LEVEL = 1;
    public static final byte CLERIC_DOMAIN_MAX_LEVEL = 5;

    public static final String[] DRUID_SPELL_TABS = { "Spells Prepared", "Circle" };
    public static final byte[] DRUID_SPELL_UI = { PREPARED, SPECIAL, SLOTS };
    public static final boolean DRUID_5E_PREPARED = true;
    public static final byte DRUID_MAX_DOMAIN_SPELLS = 8;
    public static final float DRUID_DOMAIN_SPELL_RATE = 0.5f;
    public static final byte DRUID_DOMAIN_SPELLS_PER_LEVEL = 2;
    public static final byte DRUID_DOMAIN_MIN_LEVEL = 2;
    public static final byte DRUID_DOMAIN_MAX_LEVEL = 5;

    public static final String[] PALADIN_SPELL_TABS = { "Spells Prepared", "Oath" };
    public static final byte[] PALADIN_SPELL_UI = { PREPARED, SPECIAL, SLOTS };
    public static final boolean PALADIN_5E_PREPARED = true;
    public static final byte PALADIN_MAX_DOMAIN_SPELLS = 18;
    public static final float PALADIN_DOMAIN_SPELL_RATE = 0.5f;
    public static final byte PALADIN_DOMAIN_SPELLS_PER_LEVEL = 2;
    public static final byte PALADIN_DOMAIN_MIN_LEVEL = 1;
    public static final byte PALADIN_DOMAIN_MAX_LEVEL = 5;

    public static final String[] RANGER_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final boolean RANGER_5E_PREPARED = true;
    public static final byte[] RANGER_SPELL_UI = { PREPARED, KNOWN, SLOTS };

    public static final String[] ROGUE_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final byte[] ROGUE_SPELL_UI = { PREPARED, KNOWN, SLOTS };
    
    public static final String[] SORCERER_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final boolean SORCERER_5E_PREPARED = true;
    public static final byte[] SORCERER_SPELL_UI = { PREPARED, KNOWN, SLOTS };

    public static final String[] WARLOCK_SPELL_TABS = { "Spells Known" };
    public static final byte[]   WARLOCK_SPELL_UI = { KNOWN, SLOTS, POOL };

    public static final String[] WIZARD_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final boolean WIZARD_5E_PREPARED = true;
    public static final byte[] WIZARD_SPELL_UI = { PREPARED, KNOWN, SLOTS };

    public static final String[] GENERIC_SPELL_TABS = { "Spells Prepared", "Spells Known", "Special" };
    public static final boolean GENERIC_5E_PREPARED = true;
    public static final byte[] GENERIC_SPELL_UI = { PREPARED, KNOWN, SPECIAL, SLOTS };
    
    /**
     * Override spell debit at level
     */
    public static final boolean overrideDebitSpellAtLevel(int spellLevel)
    {
      if (spellLevel < OVERRIDE_DEBIT_SPELLS.length)
        if (OVERRIDE_DEBIT_SPELLS[spellLevel] > 0)
          return true;
        
      return false;
    }
    
    public static final int getOverrideDebitSpellAtLevel(int spellLevel)
    {
      if (spellLevel < OVERRIDE_DEBIT_SPELLS.length)
        return OVERRIDE_DEBIT_SPELLS[spellLevel];
      else
        return Ability.NONE;
    }

    /**
     * Calculates the number of bonus spells applicable for the given level for
     * the given ability score.
     */
    public static final int getBonusSpells(int spellLevel, int abilityScore)
    {
      if (Rules.getInstance().getAbstractApp().accessPreferences().accessBoolean(D20PreferencesModel.KEY_FORCE_BONUS_SPELLS))
      {
        if (spellLevel < 1 || spellLevel > MAX_SPELL_LEVEL)
          return 0;

        int adjustedScore = abilityScore - (spellLevel - 1) * 2 - 12;

        if (adjustedScore < 0)
          return 0;

        return adjustedScore / 8 + 1;
      }
      return 0;
    }
    
    /**
     * Handle advancing the effective caster level of a cast feature based on using a higher level spell slot
     * @param caster - the creature casting the spell
     * @param spellSlot - the original spell slot for the selected spell
     * @param spellSlotUsed - the spell slot selected to use to cast the spell
     * @return int effective caster level based on the casting class and the spell slot selected to cast the spell
     */
    public static final int getOvercastEffectiveLevel(CreatureTemplate caster, int spellSlot, int spellSlotUsed)
    {
      return 0;
    }
  }
  
  public static class Duration
  {
    public static final String INSTANT = "instant";
    public static final String ROUND = "round(s)";
    public static final String MINUTE = "minute(s)";
    public static final String HOUR = "hour(s)";
    public static final String DAY = "day(s)";
    public static final String INFINITE = "infinite";

    public static final String[] ALL = { INSTANT, ROUND, MINUTE, HOUR, DAY, INFINITE };

    public static int convertToRounds(int rawDuration, String mode)
    {
      // // System.out.println(">>>>>>>>>>>checking " + mode);
      if (INSTANT.equals(mode))
        return 0;
      else if (ROUND.equals(mode))
        return rawDuration;
      else if (MINUTE.equals(mode))
        return rawDuration * 10;
      else if (HOUR.equals(mode))
        return rawDuration * 10 * 60;
      else if (DAY.equals(mode))
        return rawDuration * 10 * 60 * 24;
      else if (INFINITE.equals(mode))
        return GenericEffectModel.UNLIMITED;
      else
        throw new IllegalArgumentException("unsupported mode: " + mode);
    }

    private static final int _MINUTE = 10;
    private static final int _HOUR = _MINUTE * 60;
    private static final int _DAY = _HOUR * 24;

    public static String formatRounds(int rounds)
    {
      if (rounds < 1)
        return "instant";

      if (rounds == GenericEffectModel.UNLIMITED)
        return "infinite";

      StringBuffer msg = new StringBuffer();
      boolean needsSpace = false;

      int days = rounds / _DAY;
      if (days > 0)
      {
        msg.append(days).append(" day");
        if (days > 1)
          msg.append('s');

        rounds -= days * _DAY;
        needsSpace = true;
      }

      int hours = rounds / _HOUR;
      if (hours > 0)
      {
        if (needsSpace)
          msg.append(' ');
        msg.append(hours).append(" hour");
        if (hours > 1)
          msg.append('s');

        rounds -= hours * _HOUR;
        needsSpace = true;
      }

      int minutes = rounds / _MINUTE;
      if (minutes > 0)
      {
        if (needsSpace)
          msg.append(' ');
        msg.append(minutes).append(" minute");
        if (minutes > 1)
          msg.append('s');

        rounds -= minutes * _MINUTE;
        needsSpace = true;
      }

      if (rounds > 0)
      {
        if (needsSpace)
          msg.append(' ');
        msg.append(rounds).append(" round");
        if (rounds > 1)
          msg.append('s');
      }

      return new String(msg);
    }
  }

  public class Grapple
  {
    Grapple()
    {
      // noop;
    }

    public int resolveGrappleModifier(AbstractApp app, byte size, int[] mods)
    {
      int grappleModFromEffects = 0;
      // Access effects to set above number goes here....

      byte STR = Ability.STR;
      byte SIZE = (byte) Size.accessGrappleMod(size);

      int total = 0; // storage for the total modifier from mods

      if (mods.length > 0)
      {
        for (int mod : mods)
        {
          total += mod;
        }
      }

      return SIZE + total + grappleModFromEffects;
    }
  }

  public enum GameSystem
  {
    Default("Default"), Custom("Custom"), FiveE("5E"), FourE("4E"), ThreeFive("3.5"),  EsperGenesis("EsperGenesis"), Pathfinder("Pathfinder"),
    Starfinder("Starfinder"), NA("N/A");

    public String name;

    private GameSystem(String name)
    {
      this.name = name;
    }

    @Override
    public String toString()
    {
      return name;
    }

    public GameSystem getSystem()
    {
      return GameSystem.FiveE;
    }
  }

  // public enum PowerAttack
  // {
  // NA("N/A"), FourE("4E"), ThreeFive("3.5"), Pathfinder("Pathfinder");
  //
  // public final String name;
  //
  // private PowerAttack(String name)
  // {
  // this.name = name;
  // }
  //
  // @Override
  // public String toString()
  // {
  // return name;
  // }
  // }
  //

  public static class Attack implements java.io.Serializable
  {
    private static final long serialVersionUID = -8209315729042743628L;
    
    public static final String AC = "AC";
    public static final String FLAT = "Flat";
    public static final String TOUCH = "Touch"; // not used in this rule set
    public static final String CUSTOM_DEFENSE = "Custom";

    public static String[] NAMES = { AC, CUSTOM_DEFENSE };
    
    public static final String FLANK = "Roll with A/D";
    
    public static final String[] TEXT = { FLANK };
    
    public static final String CRIT_TYPE = "ct_dice";

    public final int _toHit;

    public final int _toDamage;

    public final boolean _isFlank;
    
    public final boolean _isAD;
    
    public final boolean _isAdvantage;
    
    public final boolean _isDisadvantage;

    public final boolean _isSneak;

    public final Dice _diceSneak;

    public final boolean _isPowerAttack;

    public final int _powerAttack;

    /** The last attacks this creature declared */
    public DeclaredCreatureAttack[] _attacks;

    public Attack()
    {
      this(0, 0, false, false, new Dice(1, 6), false, 0);
    }

    // TODO
    // change boolean sig to options map { option : value }
    public Attack(int toHit, int toDamage, boolean isFlank, boolean isSneak, Dice diceSneak, boolean isPowerAttack,
        int powerAttack)
    {
      _toHit = toHit;
      _toDamage = toDamage;
      _isAD = false;
      _isAdvantage = false;
      _isDisadvantage = false;
      _isFlank = isFlank;
      _isSneak = isSneak;
      _diceSneak = diceSneak;
      _isPowerAttack = isPowerAttack;
      _powerAttack = powerAttack;
      _attacks = null;
    }

    public static final Map<String, Float> damageMultiplierByType;
    static {
      damageMultiplierByType = new HashMap<>();
      damageMultiplierByType.put("1-hand", 1f);
      damageMultiplierByType.put("2-hand", 1f);
      damageMultiplierByType.put("bow", 1f);
      damageMultiplierByType.put("bullet", 1f);
      damageMultiplierByType.put("crossbow", 1f);
      damageMultiplierByType.put("offhand", 0f);
      damageMultiplierByType.put("off-hand", 0f);
      damageMultiplierByType.put("ray", 1f);
      damageMultiplierByType.put("thrown", 1f);
      damageMultiplierByType.put("touch", 1f);
    }

    public static float getDamageMultiplierByType(String type) {
      return (damageMultiplierByType.get(type.toLowerCase()) == null) ? 1f : damageMultiplierByType.get(type.toLowerCase());
    }

    public static String[] getDefenses()
    {
      // for now, just return NAMES
      return NAMES;
    }
    
    public static String[] buildDefenseChoices(AbstractApp app)
    {
      String[] defenses;
      String[] customDefenses = app.accessCustomDefense();
      if (null != customDefenses)
      {
        defenses = new String[AttackOptionsMemory.NAMES.length + customDefenses.length];
        System.arraycopy(AttackOptionsMemory.NAMES, 0, defenses, 0, AttackOptionsMemory.NAMES.length);
        System.arraycopy(customDefenses, 0, defenses, AttackOptionsMemory.NAMES.length, customDefenses.length);
      }
      else {
//        AttackOptionsMemory.NAMES = NAMES;
        defenses = AttackOptionsMemory.NAMES;
      }

      NAMES = defenses;
      return defenses;
    }

    public void setAttacks(DeclaredCreatureAttack[] attacks)
    {
      _attacks = attacks;
    }

    public DeclaredCreatureAttack[] getAttacks()
    {
      return _attacks;
    }

    public int getToHit()
    {
      return _toHit;
    }

    public int getToDamage()
    {
      return _toDamage;
    }
    
    public boolean isFlank()
    {
      return _isFlank;
    }

    public boolean isSneak()
    {
      return _isSneak;
    }

    public Dice getSneakDice()
    {
      return _diceSneak;
    }

    public boolean isPowerAttack()
    {
      return _isPowerAttack;
    }

    public int getPowerAttack()
    {
      return _powerAttack;
    }

    /**
     *  Eventually we may want to provide access to a rules based override for CreatureAttackDamage and ResolveDamageCard class methods
     *  This would allow for customization of attack resolution and damage resolution.
     *
     *  CreatureAttackDamage.rollRawDamage
     *  ResolveDamageCard (base class)
     */

    /**
     *
     * @return
     */
    public String formatDescription()
    {
      ArrayList tokens = new ArrayList();

      if (_toHit != 0)
        tokens.add(D20LF.Game.formatMod(_toHit) + " to hit");
      if (_toDamage != 0)
        tokens.add(D20LF.Game.formatMod(_toDamage) + " to dmg");
      if (_isFlank)
        tokens.add(" with A/D ");
      if (_isSneak)
        tokens.add("sneak " + _diceSneak);
      if (_isPowerAttack)
        tokens.add("power attack " + _powerAttack);

      if (tokens.isEmpty())
        return null;

      StringBuffer desc = new StringBuffer();
      for (Iterator i = tokens.iterator(); i.hasNext();)
      {
        desc.append(i.next());
        if (i.hasNext())
          desc.append(", ");
      }
      return desc.toString();
    }
  }

  public static class HP
  {
    public short rollHP(CreatureTemplate t, String dieType, byte level, boolean max) throws DiceFormatException
    {
      Dice die = new Dice(dieType);
      int result = 0;
      for (byte i = 0; i < level; i++)
      {
        // handle max HP's at first level.
        if (max && i == 0)
          result = die.getSides();
        else
          result += die.simpleRoll();

        result += t.accessAbilityScoreMod((byte) 2); // CON
      }

      return (short) result;
    }

    public short getHPModifiers()
    {

      return 0;
    }
  }

  public static class FeatureRecharge
  {
    public static final int RECHARGE_MAX = 100;
    public static final int RECHARGE_MIN = 1;
    // TODO add conditions
    public static final String ON_ROLL = "On roll";
    public static final String ON_REST = "On rest";
    public static final String[] CONDITIONS = { ON_REST, ON_ROLL };
  }

  public static class MovementType
  {
    
    public static final Map<String, String> ACTIONS = new HashMap<>();
    static
    { 
      ACTIONS.put("Prior", "moves");
      ACTIONS.put("Normal", "moves");
      ACTIONS.put("Walk", "walks");
      ACTIONS.put("Burrow", "burrows");
      ACTIONS.put("Climb", "climbs");
      ACTIONS.put("Fly", "flies");
      ACTIONS.put("Swim", "swims");
      ACTIONS.put("Grappled", "grappled");
    };
    
    public static final String[] FULLNAMES = ACTIONS.keySet().toArray(new String[ACTIONS.size()]);
    
    // add x movement type name mods (walk, strut, run, hussle, etc)
    // fly hover might be an option we wanna do.
    public static final String[] NAMES = { "N", "W", "B", "C", "F", "S", "G" };
  }
  
  // ------- FEATURE STUFF--------

  /**
   * classes from here will be displayed in feature library as options of
   * feature type
   */
  public static class Features
  {
    // Each class must extend Feature class
    public static final Map<String, String> CLASSES_MAP;
    static 
    {
      CLASSES_MAP = new HashMap<>();

      CLASSES_MAP.put("Feature", Feature.class.getCanonicalName());
      CLASSES_MAP.put("Spell", GenericSpell.class.getCanonicalName());
    }
  }

  public static class Usage
  {
    public static final byte AT_WILL = 0;
    public static final byte PER_DAY = 1;
    public static final byte PER_INIT = 2;
    public static final byte CHARGE = 3;
    public static final byte RECHARGE = 4;
    public static final byte SPELL_POOL = 5; // handler for spell slots

    public static final String[] NAMES = { "At Will", "Per Day", "Per Init", "Charge", "Recharge", "Spell Pool" };
  }

  public static class ModifyGroupsAndTargets
  {

    public static final  Map<String, List<String>> GROUP_TO_TARGET = new HashMap<>();
    static {
      //-----------------------------------------------------
      List<String> stub = new ArrayList<>();
      stub.add("None");
      GROUP_TO_TARGET.put("Inert", stub);
      //-----------------------------------------------------
      List<String> ac = new ArrayList<>();
      ac.addAll(Arrays.asList(AC.NAMES));
      ac.add("Max Dex");
      GROUP_TO_TARGET.put("AC", ac);
      //-----------------------------------------------------
      List<String> ER = new ArrayList<>();
      ER.add("Resistance");
      ER.add("Immunity");
      ER.add("Vulnerability");
      GROUP_TO_TARGET.put("ER", ER);
      //-----------------------------------------------------
      List<String> attackBonus = new ArrayList<>();
      attackBonus.add("To Hit");
      attackBonus.add("To Damage");
      attackBonus.add("Grapple");
      GROUP_TO_TARGET.put("Attack Bonus", attackBonus);
      //-----------------------------------------------------
      List<String> HP = new ArrayList<>();
      HP.add("HP");
      HP.add("Temp HP");
      HP.add("Fast Healing");
      GROUP_TO_TARGET.put("HP", HP);
      //-----------------------------------------------------
      List<String> DR = new ArrayList<>();
      DR.add("DR and");
      DR.add("DR or");
      GROUP_TO_TARGET.put("DR", DR);
      //-----------------------------------------------------
      List<String> stubRank = new ArrayList<>();
      stubRank.add("None");
      List<String> stubMisc = new ArrayList<>();
      stubMisc.add("None");
      GROUP_TO_TARGET.put("Skill Rank", stubRank); // number of times to apply proficiency bonus
      GROUP_TO_TARGET.put("Skill Misc", stubMisc); // additional bonus to skills
      //-----------------------------------------------------
      GROUP_TO_TARGET.put("Speed",Arrays.asList(MovementType.FULLNAMES));
      //-----------------------------------------------------
      GROUP_TO_TARGET.put("Ability", Arrays.asList(Ability.NAMES));
      GROUP_TO_TARGET.put("Saving Throw", Arrays.asList(Save.NAMES));
      //-----------------------------------------------------
    }
    
    public static List getGroup(String group)
    {
      return GROUP_TO_TARGET.get(group);
    }
  }
  
  // eventually we'll want to blend ModifyGroupsAndTargets wth ModifyTypes
  
  public static class ModifyType
  {
    // STRING = Name of Type
    // BOOLEAN = True - Stack, False - Dont Stack
    public static final Map<String, Boolean> MODIFY_TYPES;
    /**
     * types with unique behavior is here
     */
    public static final String[] UNIQUE = { "Dodge" };
    public static List<String> DONT_STACK;
    static
    {
      MODIFY_TYPES = new HashMap<String, Boolean>();
      MODIFY_TYPES.put("None", true);
      MODIFY_TYPES.put("Armor", false);
      MODIFY_TYPES.put("Circumstance", true);
      MODIFY_TYPES.put("Competence", true);
      MODIFY_TYPES.put("Deflection", true);
      MODIFY_TYPES.put("Dodge", true);
      MODIFY_TYPES.put("Enhancement", true);
      MODIFY_TYPES.put("Natural", false);
      MODIFY_TYPES.put("Shield", false);
      
      // Add attack qualities to available Types
      ATTACK_QUALITIES.LIST.forEach( name -> MODIFY_TYPES.put((String) name, true) );
      ATTACK_QUALITIES.PHYSICAL.forEach( name -> MODIFY_TYPES.put((String) name, true) );
      ATTACK_QUALITIES.CONDITIONS.forEach( name -> MODIFY_TYPES.put((String) name, true));
    }

    public static Set<String> getBasic()
    {
      return  MODIFY_TYPES.keySet();
    }

    public static boolean isStack(String modType)
    {
      return MODIFY_TYPES.containsKey(modType);
    }

    public static List<String> getDontStack() 
    {
      if (DONT_STACK != null) 
        return DONT_STACK;
      else 
        DONT_STACK = new ArrayList<>();
      
      for (Entry<String, Boolean> s : MODIFY_TYPES.entrySet()) 
      {
        if (!s.getValue()) 
        {
          DONT_STACK.add(s.getKey());
        }
      }
      return DONT_STACK;
    }
  }

  public static class DR 
  {
    public static List<String> LIST;
    public static boolean USE_ER = true;

    static 
    {
      LIST = new ArrayList<>(); //inital capacity (to prevent resizing on each add)
      ATTACK_QUALITIES.LIST.forEach( q->LIST.add( ((String) q) ) );
      Collections.sort(LIST);
    }
  }

  public static class ER
  {
    public static List<String> LIST;

    static {
      LIST = new ArrayList<>();
      ATTACK_QUALITIES.LIST.forEach( quality -> LIST.add(quality) );
      ATTACK_QUALITIES.CONDITIONS.forEach( condition -> LIST.add(condition) );
      
      Collections.sort(LIST);
    }
    
    public static int applyResistance( int damageDealt, ResistanceModel model )
    {
      // Apply resistance if 0 no effect to damage else reduce by half      
      if ( model.isResist() )
        damageDealt = damageDealt / 2;
  
      // If creature is vulnerable to the damage increase damage by half
      if( model.isVulnerability() )
        damageDealt = damageDealt + damageDealt / 2;
  
      // if Creature is immune of damage is no negative(do to resistance) set
      // damageDealt to 0
      if( model.isImmunity() )// || damageDealt < 0 )
        damageDealt = 0;
      
      return damageDealt;
    }
    
    public void formatResistance( String energyName, ResistanceModel model, StringBuffer buf )
    {
      if( model.isImmunity() )
      {
        formatDivider( buf );
        buf.append( "Immune " ).append( energyName );
      }
      if( model.isVulnerability() )
      {
        formatDivider( buf );
        buf.append( "Vulnerable " ).append( energyName );
      }

      if ( model.isResist() )
      {
        formatDivider( buf );
        buf.append( "Resist " ).append( energyName );
      }
    }
    
    private void formatDivider( StringBuffer buf )
    {
      if( buf.length() > 0 )
        buf.append( "; " );
    }
    
  }

  public static class ATTACK_QUALITIES {

    public static List<String> LIST;
    public static List<String> BURST_QUALITIES;
    public static List<String> PHYSICAL;
    public static List<String> CONDITIONS;

    static {
      LIST = new ArrayList<>(28);
      LIST.add("Slashing");
      LIST.add("Bludgeoning");
      LIST.add("Piercing");
      LIST.add("Magic");
      LIST.add("Fire");
      LIST.add("Lightning");
      LIST.add("Cold");
      LIST.add("Acid");
      LIST.add("Sonic");
      LIST.add("Burst");
      LIST.add("Critical");
      LIST.add("Nonlethal");
      LIST.add("Good");
      LIST.add("Evil");
      LIST.add("Lawful");
      LIST.add("Chaotic");
      LIST.add("Epic");
      LIST.add("Silver");
      LIST.add("Cold Iron");
      LIST.add("Adamantine");
      LIST.add("Mithral");
      LIST.add("Force");
      LIST.add("Arcane");
      LIST.add("Necrotic");
      LIST.add("Poison");
      LIST.add("Psychic");
      LIST.add("Radiant");
      LIST.add("Thunder");
      LIST.add("Sneak");
      Collections.sort(LIST);

      BURST_QUALITIES = new ArrayList<>();
      BURST_QUALITIES.add("Acid");
      BURST_QUALITIES.add("Arcane");
      BURST_QUALITIES.add("Cold");
      BURST_QUALITIES.add("Lightning");
      BURST_QUALITIES.add("Necrotic");
      BURST_QUALITIES.add("Poison");
      BURST_QUALITIES.add("Psychic");
      BURST_QUALITIES.add("Radiant");
      BURST_QUALITIES.add("Thunder");
      Collections.sort(BURST_QUALITIES);

      PHYSICAL = new ArrayList<>(3);
      PHYSICAL.add("Slashing");
      PHYSICAL.add("Bludgeoning");
      PHYSICAL.add("Piercing");
      Collections.sort(PHYSICAL);
      
      CONDITIONS = new ArrayList<>();
      CONDITIONS.add("Blinded");
      CONDITIONS.add("Charmed");
      CONDITIONS.add("Deafened");
      CONDITIONS.add("Exhaustion");
      CONDITIONS.add("Frightened");
      CONDITIONS.add("Grappled");
      CONDITIONS.add("Incapacitated");
      CONDITIONS.add("Invisible");
      CONDITIONS.add("Paralyzed");
      CONDITIONS.add("Petrified");
      CONDITIONS.add("Poisoned");
      CONDITIONS.add("Prone");
      CONDITIONS.add("Restrained");
      CONDITIONS.add("Stunned");
      CONDITIONS.add("Unconscious");
      Collections.sort(CONDITIONS);
    }
    public static boolean isBurst(String attack){
      return BURST_QUALITIES.contains(capitalize(attack)) || BURST_QUALITIES.contains(attack);
    }

    public static boolean isPhysical(String attack){
      return getAttackQualities().contains(capitalize(attack)) || getAttackQualities().contains(attack);
//      return PHYSICAL.contains(capitalize(attack)) || PHYSICAL.contains(attack);
    }

    public static LinkedHashSet<String> getAttackQualities( )
    {
      LinkedHashSet<String> hashSet = new LinkedHashSet<String>(LIST);
      hashSet.addAll(PHYSICAL);
      hashSet.addAll(CONDITIONS);

      return hashSet;
    }
  }
  
  public static class Actions
  {
    // Action capabilities
    public static final boolean HAS_ATTACK_QUALITIES = true;
    public static final boolean HAS_CONDITIONS = true;
    public static final boolean HAS_CHALLENGES = true;
    public static final boolean USE_POWERATTACK = false;

    // Action (attack) text strings
    public static final String TEXT_ATTACK_ROLL = "ATTACK ROLL";
    public static final String TEXT_CONFIRM_ROLL = "ADVANTAGE/DISADVANTAGE ROLL";

    /**
     * basic available actions and action types
     */
    public static final Map<String, List<String>> ACTIONS_MAP;
    static
    {
      ACTIONS_MAP = new HashMap<String, List<String>>();
      ArrayList<String> Input = new ArrayList<String>();

      Input.add("none");
      Input.add("acid");
      Input.add("bludgeoning");
      Input.add("cold");
      Input.add("fire");
      Input.add("force");
      Input.add("lightning");
      Input.add("necrotic");
      Input.add("piercing");
      Input.add("psychic");
      Input.add("radiant");
      Input.add("slashing");
      Input.add("thunder");
      Collections.sort(Input);

      ACTIONS_MAP.put("Effect", Input);
      ArrayList<String> Input1 = new ArrayList<String>();

      Input1.add("None");
      Input1.add("Blinded");
      Input1.add("Charmed");
      Input1.add("Deafened");
      Input1.add("Exhaustion");
      Input1.add("Frightened");
      Input1.add("Grappled");
      Input1.add("Incapacitated");
      Input1.add("Invisible");
      Input1.add("Paralyzed");
      Input1.add("Petrified");
      Input1.add("Poisoned");
      Input1.add("Prone");
      Input1.add("Restrained");
      Input1.add("Stunned");
      Input1.add("Unconscious");
      Collections.sort(Input1);

      ACTIONS_MAP.put("Condition", Input1);
      ArrayList<String> Input2 = new ArrayList<String>();

      Input2.add("None");
      Input2.add("Strength");
      Input2.add("Dexterity");
      Input2.add("Constitution");
      Input2.add("Intelligence");
      Input2.add("Wisdom");
      Input2.add("Charisma");
      Input2.add("Misc");
      Collections.sort(Input2);

      ACTIONS_MAP.put("Challenge", Input2);
    }
  }

  public static class UI
  {
    public static final Map<String, Object> ADDON = new HashMap<>();
    static {
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_CUSTOM_DICE_HANDLER, true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_REQUIRES_RESTART, true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_AD,  true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_HAS_ADVANTAGE, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_HAS_DISADVANTAGE, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_OVERCAST,  true);
    };
    
    private JToggleButton _toggleAD;
    private JToggleButton _toggleA;
    private JToggleButton _toggleD;

    private JToggleButton _ta, _td, _tad;
    
    public static boolean USE_ALT = true;
    public static boolean USE_DEATH_SAVES = true;
    public static boolean USE_INPSIRATION = true;

    public JToggleButton[] accessCustomTopRightControls()
    {
      // declare three phase toggle buttons
      _tad = LAF.ToggleButton.png("toggleAD", null);
      _tad.setToolTipText("Enable and toggle Advantage/Disadvantage");
      _tad.setVisible(false);
      _tad.setEnabled(false);

      _toggleD = LAF.ToggleButton.png("toggleD", new ABToggleButtonAction(_tad, new ToggleADDisableAction()));
      _toggleD.setToolTipText("Enable and toggle Advantage/Disadvantage");
      _toggleD.setVisible(false);
      _toggleD.setEnabled(false);

      _toggleA = LAF.ToggleButton.png("toggleA", new ABToggleButtonAction(_toggleD, new ToggleDisadvantageAction()));
      _toggleA.setToolTipText("Enable and toggle Advantage/Disadvantage");
      _toggleA.setVisible(false);
      _toggleA.setEnabled(false);

      _toggleAD = LAF.ToggleButton.png("toggleAD", new ABToggleButtonAction(_toggleA, new ToggleAdvantageAction()));
      _toggleAD.setToolTipText("Enable and toggle Advantage/Disadvantage");
      _toggleAD.setVisible(true);
      _toggleAD.setEnabled(true);

      JToggleButton[] buttons = { _toggleAD, _toggleA, _toggleD };

      return buttons;
    }
    
    private class ToggleADDisableAction extends AbstractAction
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        // System.out.println("AD disabled");

        // accessPreferences().accessBoolean( KEY_HAS_ADVANTAGE, false);
        // accessPreferences().accessBoolean( KEY_HAS_DISADVANTAGE, false);

        JToggleButton button = (JToggleButton) e.getSource();
        button.setEnabled(false);
        button.setVisible(false);

        _toggleAD.setEnabled(true);
        _toggleAD.setVisible(true);
        
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_USE_AD, true);
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_HAS_ADVANTAGE, false);
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_HAS_DISADVANTAGE, false);
      }

    }

    private class ToggleDisadvantageAction extends AbstractAction
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        // System.out.println("You have disadvantage");
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_USE_AD, true);
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_HAS_ADVANTAGE, false);
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_HAS_DISADVANTAGE, true);
      }

    }

    private class ToggleAdvantageAction extends AbstractAction
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        // System.out.println("You have advantage");
        
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_USE_AD, true);
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_HAS_ADVANTAGE, true);
        Rules.getInstance().getAbstractApp().accessPreferences().assignObject(D20PreferencesModel_AddOn.KEY_HAS_DISADVANTAGE, false);
      }

    }

    private class ABToggleButtonAction extends AbstractAction
    {
      // private JToggleButton _buttonA;
      private JToggleButton _nextButton;
      private Action _action;

      private ABToggleButtonAction(JToggleButton nextButton, Action action)
      {
        _nextButton = nextButton;
        _action = action;
      }

      @Override
      public void actionPerformed(ActionEvent e)
      {
        JToggleButton button = (JToggleButton) e.getSource();
        button.setVisible(false);
        button.setEnabled(false);
        button.setSelected(false);
        _nextButton.setEnabled(true);
        _nextButton.setVisible(true);
        _nextButton.setSelected(true);

        // perform action for "next" button
        JToggleButton actor = new JToggleButton(_action);
        actor.doClick();
        actor.setEnabled(false);
        actor.setVisible(false);

      }
    }
    
    

    private class CharacterSheet
    {
       // noop
    }    
  }
  
  public static class Prefs
  {
    public static final Map<String, Object> ADDON = new HashMap<>();
    static {
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_CUSTOM_DICE_HANDLER, true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_REQUIRES_RESTART, true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_AD,  true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_HAS_ADVANTAGE, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_HAS_DISADVANTAGE, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_OVERCAST,  true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_CRIT_TYPE,  true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_FLAT_AC, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_TOUCH_AC, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_UI_ALT,  true);
    };
  }

  public final class D20PreferencesModel_AddOn extends D20PreferencesModel
  {
    // Common custom hooks

    // Flag to set if this Rule set uses a custom dice handler.
    public static final String KEY_USE_CUSTOM_DICE_HANDLER = "useCustomDiceHandler";

    public static final String KEY_REQUIRES_RESTART = "requiresRestart";

    public static final String KEY_USE_AD = "useAD";
    public static final String KEY_HAS_ADVANTAGE = "hasAdvantage";
    public static final String KEY_HAS_DISADVANTAGE = "hasDisadvantage";
    
    public static final String KEY_USE_OVERCAST = "useOvercast";
    
    public static final String KEY_CRIT_TYPE = "customCritType";
    
    public static final String KEY_USE_FLAT_AC = "useFlatAC";
    public static final String KEY_USE_TOUCH_AC = "useTouchAC";
    
    public static final String KEY_USE_UI_ALT = "useUIAlt";
    
    // handle the default size of the mini view icon
//    public static final String KEY_MINI_SIZE = "miniSize";
    
    public D20PreferencesModel_AddOn()
    {
      // requires a restart due to UI changes which will not load unless the app
      // is restarted
      // will look into scripting the restart
      assignBoolean(KEY_REQUIRES_RESTART, true);

      // we'll be checking for A/D status on all d20 rolls
      assignBoolean(KEY_USE_CUSTOM_DICE_HANDLER, true);

      // note that we want AD usage available
      assignBoolean(KEY_USE_AD, true);

      // usage flags which will be controlled from the AD toggle UI button
      assignBoolean(KEY_HAS_ADVANTAGE, false);
      assignBoolean(KEY_HAS_DISADVANTAGE, false);
      
      // provide UI for overcasting
      assignBoolean(KEY_USE_OVERCAST, true);
      
      assignBoolean(KEY_USE_UI_ALT, true);
    }

    // need to specify a custom dice handler and check for common Preference

    public D20PreferencesModel_AddOn accessPrefs()
    {
      return new D20PreferencesModel_AddOn();
    }
  
  }
  
  /** UTILITY FUNCTIONS **/
  private static String capitalize(String s) {
    s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
    return s;
  }
}
