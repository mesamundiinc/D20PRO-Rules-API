package com.d20pro.plugin.rules.pathfinderTwoZero;

import com.d20pro.temp_extraction.plugin.feature.model.Feature;
import com.d20pro.temp_extraction.plugin.feature.model.trigger.FeatureTrigger;
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
import com.sengent.common.logging.LoggingManager;

import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;


/**
 * D20 rules specific lookup methods and other routines that define the game.
 *
 * @author saethi, owlbear, alexey
 */
//@SuppressWarnings({ "rawtypes" })
public class PathfinderTwoZeroRules extends AbstractRules
{
  public static final byte UNTRAINED = 0;
  public static final byte TRAINED = 1;
  public static final byte EXPERT = 2;
  public static final byte MASTER = 3;
  public static final byte LEGENDARY = 4;

  public PathfinderTwoZeroRules()
    {
        super();
    }

  public static enum ProficiencyRank {
    UNTRAINED("Untrained"),TRAINED("Trained"), EXPERT("Expert"), MASTER("Master"), LEGENDARY("Legendary");

    public String rank;

    private ProficiencyRank(String rank)
    {
      this.rank = rank;
    }

    @Override
    public String toString()
    {
      return rank;
    }

    public ProficiencyRank getRank()
    {
      return ProficiencyRank.valueOf(rank);
    }
  }

  public static class Stabilization {

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

        if (NAMES[i].equalsIgnoreCase(name))
          return i;
      if (name.equalsIgnoreCase("None"))
        return NONE;
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
   
  
  public static class AC implements Constants_ArmorClass
  {
    public static final short NO_MAX_DEX = -1;
    
    public static final int BASE_AC = 10;
    public static final boolean REQUIRE_BASE_AC = true;

    public static final byte NATURAL = 0;
    public static final byte ARMOR = 1;
    public static final byte SHIELD = 2;
    public static final byte DEFLECT = 3;
    public static final byte UNNAMED = 4;
    public static final byte DODGE = 5;
//    public static final byte ENHANCE = 6;

    public static final String[] NAMES = { "Natural", "Armor", "Shield", "Deflect", "Unnamed", "Dodge", "Enhancement"}; //, "Luck" };
    
    public static final String[] LABELS = { "Base", "plus", "Armor", "plus", "Shield", "plus", "Natural", "plus", "Dex", "plus", "Size", "plus", "Deflect", "plus", "Unnamed", "plus", "Dodge", "and", "Max Dex"};
    // "Enhancement", "plus",
    public static final String[] SYMBOLS = { "\u2192", "\u2192", "\u2192" };  // eventually ->, &, +
    
    public static final String[] OVERRIDE = { };

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
    
    public static String totalArmorClass(String natural, String armor, String shield, String deflect, String dodge,
        String luck, String unnamed, String size, String dexText, int maxDexBonus)
    {
      try
      {
        int total = 0;

        total = BASE_AC;

        total += Integer.parseInt(armor);
        total += Integer.parseInt(deflect);
        total += Integer.parseInt(dodge);
        total += Integer.parseInt(unnamed);
        total += Integer.parseInt(natural);
        total += Integer.parseInt(shield);
        total += Integer.parseInt(size);

        int dex = 0;
        if (!dexText.isEmpty())
        {
          dex = Integer.parseInt(dexText);
          if (maxDexBonus > Constants_ArmorClass.NO_MAX_DEX) // Has a Max Dex
                                                             // Bonus
            if (dex > maxDexBonus) // Dex bonus is greater then max
              dex = maxDexBonus;
        }

        total += dex; // apply modified or unmodified dex bonus

        return Integer.toString(total);
      }
      catch (NumberFormatException nfe)
      {
        return "?";
      }
    }

    public static int resolveAC(int natural, int armor, int shield, int deflect, int dodge, int luck, int noname,
        int size, int dex)
    {
      int total = 0;

      total = BASE_AC + natural + armor + shield + deflect + dodge + luck + noname + size + dex;

      return total;
    }
  }
  
  public static class Attack implements java.io.Serializable
  {
    private static final long serialVersionUID = -8209315729042743628L;
    
    public static final String AC = "AC";
    public static final String FLAT = "Flat";
    public static final String TOUCH = "Touch";
    public static final String CUSTOM_DEFENSE = "Custom";

    public static String[] NAMES = { AC, FLAT, TOUCH, CUSTOM_DEFENSE };
    
    public static final String FLANK = "Flank";
    
    public static final String[] TEXT = { FLANK };
    
    public static final String CRIT_TYPE = ""; // use standard 3.5 style crits

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
      damageMultiplierByType.put("2-hand", 1.5f);
      damageMultiplierByType.put("bow", 1f);
      damageMultiplierByType.put("bullet", 1f);
      damageMultiplierByType.put("crossbow", 1f);
      damageMultiplierByType.put("offhand", .5f);
      damageMultiplierByType.put("off-hand", .5f);
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
      else
        defenses = AttackOptionsMemory.NAMES;

      NAMES = defenses;
      return defenses;
    }

    public DeclaredCreatureAttack[] getAttacks()
    {
      return _attacks;
    }
    
    public void setAttacks(DeclaredCreatureAttack[] attacks)
    {
      _attacks = attacks;
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
    
    public boolean isAD()
    {
      return _isAD;
    }

    public boolean isAdvantage()
    {
      return _isAdvantage;
    }
    
    public boolean isDisadvantage()
    {
      return _isDisadvantage;
    }
    
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
        if (_isAD)
          tokens.add(" with A/D ");
        else
          tokens.add(" with flank ");
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

  public static class Size
  {
    public static final byte FINE = 0;
    public static final byte DIMINUTIVE = 1;
    public static final byte TINY = 2;
    public static final byte SMALL = 3;
    public static final byte MEDIUM = 4;
    public static final byte LARGE = 5;
    public static final byte HUGE = 6;
    public static final byte GARGANTUAN = 7;
    public static final byte COLOSSAL = 8;
    public static final byte COLOSSAL_PLUS = 9;

    public static final String[] NAMES = { "Fine", "Diminutive", "Tiny", "Small", "Medium", "Large", "Huge",
        "Gargantuan", "Colossal", "Colossal+" };

    public static byte[] MODS = { 8, 4, 2, 1, 0, -1, -2, -4, -8, -12 };

    // Used by AbstractCreatureInPlay to manage border size
    public static int[] PIXELS_PER_CELL = { 48, 40, 32, 24, 16 };

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
      final int idMedium = 4;
      if (idSize <= idMedium)
        return 1;
      return idSize - idMedium + 1;
    }

    public static int accessGrappleMod(byte idSize)
    {
      int mod = idSize - MEDIUM;
      return mod * 4;
    }
  }

  public static class Skill
  {
    public static final byte MAX_RANKS = 5;
    public static final byte MAX_BONUS = 99;
    public static final String[] HEADERS = {"", "Mod", "Skill Name", "Abil", "Rank", "Misc"};
    
    public static String SHOW_ALL = "Show All";
    public static String RANKED = "Ranked";
    public static String UNRANKED = "Unranked";
    public static String TOTAL_RANKS = "Ranks";
    
    public int getSkillBonus(CreatureTemplate template, int ranks)
    {
      // do math on ranks before returning if ranks not 1 to 1 (i.e. 5e)
      switch(ranks) {
        case UNTRAINED:
          return template.getLevel() - 2;
        case TRAINED:
          return template.getLevel();
        case EXPERT:
          return template.getLevel() + 1;
        case MASTER:
          return template.getLevel() + 2;
        case LEGENDARY:
          return template.getLevel() + 3;
        default:
          return ranks;
      }
    }
  }
  
  public static class Save
  {
    public static final int MINIMAL_DAMAGE_FOR_HALF_SAVE = 0;// 0 or 1
    
    public static final byte FORT = 0;
    public static final byte REF = 1;
    public static final byte WILL = 2;

    public static final byte[] IDS = { FORT, REF, WILL };
    
    public static final byte EVASION_SAVE = REF; // the saving throw index to check for evasion 

    // array representing the Abilities (in byte) to use when calculating saving
    // throws and modifiers
    // ORDER MATTERS!
    public static final byte[] MAP = { Ability.CON, Ability.DEX, Ability.WIS };
    // public static final byte[] MAP = { 0, 1, 2 };

    public static final String[] NAMES = { "Fort", "Ref", "Will" };

    public static final String[] FULL_NAMES = { "Fortitude", "Reflex", "Will" };

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
      // noop
    }

    public static KeyStroke getHotKey(byte idSave)
    {
      switch (idSave)
      {
      case FORT:
        return CommonHotKeys.Creature.SAVE_FORT;
      case REF:
        return CommonHotKeys.Creature.SAVE_REF;
      case WILL:
        return CommonHotKeys.Creature.SAVE_WILL;
      }
      throw new UnsupportedOperationException("Unknown save: " + idSave);
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
    public static final byte SP = 2;
    public static final byte CP = 3;

    public static final String[] NAMES = { "PP", "GP", "SP", "CP" };

    public static final String[] FULL_NAMES = { "Platinum", "Gold", "Silver", "Copper" };

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
         sources are priotized from 0 .. # where a0 higher number is matched first when synchronizing.
       */
      // SOURCES.put("string", 0..9);
      SOURCES.put("Racial", 13);
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
    public static final String[] NAMES = { "Unknown", "Aberration", "Animal", "Construct", "Dragon", "Elemental", "Fey",
        "Giant", "Humanoid", "Magical Beast", "M. Humanoid", "Ooze", "Outsider", "Plant", "Undead", "Vermin" };

    public static final String[] SUBTYPENAMES = { "Air", "Angel", "Aquatic", "Archon", "Augmented", "Chaotic", "Cold",
        "Earth", "Evil", "Extraplanar", "Fire", "Goblinoid", "Good", "Incorporeal", "Lawful", "Native", "Reptilian",
        "Shapechanger", "Swarm", "Water" };
    
    public static final byte[] AttackRate = { 1 };

    public static final String[] HD = { "d8" };

    public static final String[] GoodSaves = { "Fort, Ref, Will" };
  }

  public static class CreatureClass
  {
    public static final byte MAX_CLASS_LEVEL = 20;

    public static final String SHEET_TYPE = "D20";

    // can host hard coded class lists here.
    public static final String[] NAMES = { "Unknown" };
    public static final String[] HD = { "d8" };
    public static final byte[] AttackRate = { 1 };
    public static final byte[] GoodSaves = { 0, 0, 0 };
    public static final String BABNAME = "BAB";

    private CreatureTemplate_Classes _classes;

    public CreatureClass()
    {
      _classes = new CreatureTemplate_Classes();
    }

    public CreatureTemplate_Classes accessClasses()
    {
      return _classes;
    }

    public int resolveBAB(CreatureTemplate creature)
    {
      CreatureTemplate_Classes classList = creature.getClasses();
      ArrayList<GenericCreatureClass> classes = classList.accessClasses();

      if (creature.hasOverrideBAB())
        return creature.getOverrideBAB().intValue();

      if (classes == null)
        return 0;
      
      int level = 0;
      for (GenericCreatureClass c : classes)
      {
        level += (int) c.getLevel();
      }

      int BAB = 0;
      for (GenericCreatureClass creatureClass : classes)
      {
        CreatureClassTemplate classTemplate = creatureClass.accessClassTemplate();
        if (classTemplate == null)
        {
          String text = "Class Template Error: " + creatureClass.getName() + " NOT in Classes.txt  Setting BAB to 0.";
          LoggingManager.info(CreatureTemplate_Classes.class, text);
          return 0;
        }
        
        if (classTemplate.accessAttackRate() == 0) 
          BAB += classTemplate.resolveBABbyCR(creature, .25);
        else
          BAB += classTemplate.resolveBAB(creatureClass.getLevel());
      }

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
      String[] names = Save.NAMES;
      byte[] newsaves = new byte[map.length];
      byte[] abilities = creature.getAbilities();

      // Handle good saves
      byte[] classmod = new byte[names.length];
      Arrays.fill(classmod, (byte) 0);

      CreatureTemplate_Classes classList = creature.getClasses();

      ArrayList<GenericCreatureClass> classes = classList.accessClasses();

      for (GenericCreatureClass c : classes)
      {
        String[] st = c.accessClassTemplate().accessSavingThrows();

        byte[] cmods = new byte[names.length];
        Arrays.fill(cmods, (byte) Math.ceil((c.getLevel() / 3 ) ) );

        for (byte s = 0; s < st.length; s++ )
        {
          for (byte i = 0; i < names.length; i++)
          {
            if ( st[s].equalsIgnoreCase(names[i]) )
              cmods[i] = (byte) Math.ceil((c.getLevel() / 2) + 2);
          }
        }

        for(byte m = 0; m < cmods.length; m++)
        {
          classmod[m] += cmods[m];
        }
      }

      for (byte i = 0; i < map.length; i++)
      {
        // Ability Modifier + Proficiency Bonus + Other modifiers (TODO)
        /** to extend this; add + Feature Modifiers here **/
        newsaves[i] = (byte) (creature.getSaveBase(i) + creature.accessAbilityScoreMod(map[i]) + classmod[i]+creature.getSaveMod(i));
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
    public static final byte KNOWN_SPELL_CATEGORIES = 2; // Cantrips & Non-Cantrips

    public static final byte MAX_DOMAIN_SPELLS = 18;
    public static final float DOMAIN_SPELL_RATE = 1f;
    public static final byte DOMAIN_SPELLS_PER_LEVEL = 1;

    // Note: Spell levels start at Zero so no reason to offset a check against
    // this array
    public static final int[] OVERRIDE_DEBIT_SPELLS = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
//
    public static final boolean OVERRIDE_SPELL_REMOVE_FROM_MEMORY = false;
    public static final boolean USE_SECONDARY_DOMAIN = true;
    public static final boolean USE_ALL_SPELLS_IN_LINE = false;
//
//    // on the fence about placing these in spell or creatureclass...
//
    public static final byte PREPARED = 0; // available spells to cast
    public static final byte KNOWN = 1; // standard spell book style
    public static final byte SPECIAL = 2; // domain/cicle/etc.
    public static final byte SLOTS = 3; // track available spell casting slots
    public static final byte POOL = 4; // all spells use a single spell pool to
                                       // track usage

    public static final String[] BARD_SPELL_TABS = { "Spells Known" };
    public static final byte[] BARD_SPELL_UI = { KNOWN, SLOTS };

    public static final String[] CLERIC_SPELL_TABS = { "Spells Prepared", "Domain" };
    public static final byte[] CLERIC_SPELL_UI = { PREPARED, SPECIAL };
    public static final byte CLERIC_MAX_DOMAIN_SPELLS = 18;
    public static final float CLERIC_DOMAIN_SPELL_RATE = 1f;
    public static final byte CLERIC_DOMAIN_SPELLS_PER_LEVEL = 1;
    public static final byte CLERIC_DOMAIN_MIN_LEVEL = 1;
    public static final byte CLERIC_DOMAIN_MAX_LEVEL = 9;

    public static final String[] DRUID_SPELL_TABS = { "Spells Prepared" };
    public static final byte[] DRUID_SPELL_UI = { PREPARED };
    public static final byte DRUID_MAX_DOMAIN_SPELLS = 8;
    public static final float DRUID_DOMAIN_SPELL_RATE = 1f;
    public static final byte DRUID_DOMAIN_SPELLS_PER_LEVEL = 1;
    public static final byte DRUID_DOMAIN_MIN_LEVEL = 2;
    public static final byte DRUID_DOMAIN_MAX_LEVEL = 5;

    public static final String[] PALADIN_SPELL_TABS = { "Spells Prepared" };
    public static final byte[] PALADIN_SPELL_UI = { PREPARED };
    public static final byte PALADIN_MAX_DOMAIN_SPELLS = 18;
    public static final float PALADIN_DOMAIN_SPELL_RATE = 1f;
    public static final byte PALADIN_DOMAIN_SPELLS_PER_LEVEL = 1;
    public static final byte PALADIN_DOMAIN_MIN_LEVEL = 1;
    public static final byte PALADIN_DOMAIN_MAX_LEVEL = 5;

    public static final String[] RANGER_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final byte[] RANGER_SPELL_UI = { PREPARED, KNOWN };

    public static final String[] ROGUE_SPELL_TABS = { "Spells Known" };
    public static final byte[] ROGUE_SPELL_UI = { KNOWN, SLOTS };

    public static final String[] SORCERER_SPELL_TABS = { "Spells Prepared", "Spells Known", "Bloodline" };
    public static final byte[] SORCERER_SPELL_UI = { PREPARED, KNOWN, SPECIAL, SLOTS };
    public static final boolean SORCERER_OVERRIDE_SPELL_REMOVE_FROM_MEMORY = true;
    public static final boolean SORCERER_5E_PREPARED = true;
    public static final boolean SORCERER_USE_SPELLS_IN_LINE = true;
    public static final byte SORCERER_MAX_DOMAIN_SPELLS = 19;
    public static final float SORCERER_DOMAIN_SPELL_RATE = .5f;
    public static final byte SORCERER_DOMAIN_SPELLS_PER_LEVEL = 1;
    public static final byte SORCERER_DOMAIN_MIN_CLASS_LEVEL = 3;
    public static final byte SORCERER_DOMAIN_MIN_LEVEL = 1;
    public static final byte SORCERER_DOMAIN_MAX_LEVEL = 9;

    public static final String[] WARLOCK_SPELL_TABS = { "Spells Known" };
    public static final byte[] WARLOCK_SPELL_UI = { KNOWN, SLOTS, POOL };

    public static final String[] WIZARD_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final byte[] WIZARD_SPELL_UI = { PREPARED, KNOWN};

    public static final String[] GENERIC_SPELL_TABS = { "Spells Prepared", "Spells Known", "Special" };
    public static final byte[] GENERIC_SPELL_UI = { PREPARED, KNOWN, SPECIAL };

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
      if (spellLevel < 1 || spellLevel > MAX_SPELL_LEVEL)
        return 0;

      int adjustedScore = abilityScore - (spellLevel - 1) * 2 - 12;

      if (adjustedScore < 0)
        return 0;

      return adjustedScore / 8 + 1;
    }

//    /**
//     * Calculates the number of bonus spells applicable for the given level for
//     * the given ability score.
//     *
//     */
//    public static final int getBonusSpells(int spellLevel, int abilityScore)
//    {
//      if (spellLevel < 1 || spellLevel > MAX_SPELL_LEVEL)
//        return 0;
//
//      int adjustedScore = abilityScore - (spellLevel - 1) * 2 - 12;
//
//      if (adjustedScore < 0)
//        return 0;
//
//      return adjustedScore / 8 + 1;
//    }

    /**
     * Handle advancing the effective caster level of a cast feature based on
     * using a higher level spell slot
     *
     * @param caster
     *          - the creature casting the spell
     * @param spellSlot
     *          - the original spell slot for the selected spell
     * @param spellSlotUsed
     *          - the spell slot selected to use to cast the spell
     * @return int effective caster level based on the casting class and the
     *         spell slot selected to cast the spell
     */
    public static final int getOvercastEffectiveLevel(CreatureTemplate caster, int spellSlot, int spellSlotUsed)
    {
      return 0;
    }
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
    public static final int TEMPLATE_MAX_UNITS = 40;

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
      ValueWithUnit[] items = new ValueWithUnit[TEMPLATE_MAX_UNITS];
      for (int i = 0; i < TEMPLATE_MAX_UNITS; i++)
      {
        int value = i + TEMPLATE_STEP;
        items[i] = new ValueWithUnit(value, app.formatUnits(value));
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
      for (Map.Entry<Byte, String> e : TEMPLATE_MAP.entrySet())
      {
        if (e.getValue() == name)
          return e.getKey();
      }
      return NONE;
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
    private static final int _MINUTE = 10;
    private static final int _HOUR = _MINUTE * 60;
    private static final int _DAY = _HOUR * 24;

    public static int convertToRounds(int rawDuration, String mode)
    {
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
    Default("Default"), Custom("Custom"), FiveE("5E"), FourE("4E"), ThreeFive("3.5"), Pathfinder("Pathfinder"),
    PathfinderTwoZero("Pathfinder 2.0"), Starfinder("Starfinder"), NA("N/A");

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
      return GameSystem.PathfinderTwoZero;
    }
  }

//  public enum PowerAttack
//  {
//    NA("N/A"), FiveE("5E"), FourE("4E"), ThreeFive("3.5"), Pathfinder("Pathfinder");
//
//    public final String name;
//
//    private PowerAttack(String name)
//    {
//      this.name = name;
//    }
//
//    @Override
//    public String toString()
//    {
//      return name;
//    }
//  }

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

  public static class Actions
  {
    // Action capabilities
    public static final boolean HAS_ATTACK_QUALITIES = true;
    public static final boolean HAS_CONDITIONS = true;
    public static final boolean HAS_CHALLENGES = true;
    public static final boolean USE_POWERATTACK = true;

    // Action (attack) text strings
    public static final String TEXT_ATTACK_ROLL = "ATTACK ROLL";
    public static final String TEXT_CONFIRM_ROLL = "CONFIRMATION";
    
    /**
     * basic available actions and action types
     */
    public static final Map<String, List<String>> ACTIONS_MAP;
    static
    {
      ACTIONS_MAP = new HashMap<String, List<String>>();

      ArrayList<String> effect = new ArrayList<String>();
      effect.add("none");
      
      ATTACK_QUALITIES.LIST.forEach( q->effect.add( (String) q ) );
      Collections.sort(effect);
      ACTIONS_MAP.put("Effect", effect);
      
      ArrayList<String> condition = new ArrayList<String>();
      condition.add("None");
      ATTACK_QUALITIES.CONDITIONS.forEach( q->condition.add( (String) q) ) ;
      Collections.sort(condition);   
      ACTIONS_MAP.put("Condition", condition);
      
      ArrayList<String> challenge = new ArrayList<String>();

      challenge.add("None");
      challenge.add("Strength");
      challenge.add("Dexterity");
      challenge.add("Constitution");
      challenge.add("Intelligence");
      challenge.add("Wisdom");
      challenge.add("Charisma");
      challenge.add("Fortitude");
      challenge.add("Reflex");
      challenge.add("Will");
      challenge.add("Misc");
      Collections.sort(challenge);
      
      ACTIONS_MAP.put("Challenge", challenge);
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
          result = die.getSides() * die.getNum() + die.getMod();
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

//    public static final String[] FULLNAMES = { "Walk", "Hussle", "Run x3" , "Run x4", "Burrow", "Climb", "Fly", "Fly (Hover)", "Swim" };
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
      List<String> SAVES = new ArrayList<>();
      SAVES.add("Fortitude");
      SAVES.add("Reflex");
      SAVES.add("Will");
      GROUP_TO_TARGET.put("Saves", SAVES);
      //-----------------------------------------------------
      List<String> stubRank = new ArrayList<>();
      stubRank.add("None");
      List<String> stubMisc = new ArrayList<>();
      stubMisc.add("None");
      GROUP_TO_TARGET.put("Skill Rank", stubRank);//filling up on game start
      GROUP_TO_TARGET.put("Skill Misc", stubMisc);
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

  public static class DR {
    public static List<String> LIST;

    static {
      LIST = new ArrayList<>(); //inital capacity (to prevent resizing on each add)
      ATTACK_QUALITIES.LIST.forEach( q->LIST.add( ((String) q) ) );
      Collections.sort(LIST);
    }
  }

  public static class ER{
    public static List<String> LIST;
    static {
      LIST = new ArrayList<>();
      ATTACK_QUALITIES.LIST.forEach( quality -> LIST.add( quality ));
//      ATTACK_QUALITIES.CONDITIONS.forEach( condition -> LIST.add( condition ));
      
      Collections.sort(LIST);
    }
      
    public void formatResistance( String energyName, ResistanceModel model, StringBuffer buf)
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
  
      int resistance = model.getResistance();
  
      if( resistance != 0 )
      {
        formatDivider( buf );
        buf.append( "Resist " ).append( energyName ).append( ' ' ).append( resistance );
      }
    }
    
    private void formatDivider( StringBuffer buf )
    {
      if( buf.length() > 0 )
        buf.append( "; ");
    }
    
//    static {
//      LIST = new ArrayList<>();
//      LIST.add("Magic");
//      LIST.add("Fire");
//      LIST.add("Electricity");
//      LIST.add("Cold");
//      LIST.add("Acid");
//      LIST.add("Sonic");
//      LIST.add("Good");
//      LIST.add("Evil");
//      LIST.add("Lawful");
//      LIST.add("Chaotic");
//      LIST.add("Force");
//      LIST.add("Arcane");
//      LIST.add("Necrotic");
//      LIST.add("Poison");
//      LIST.add("Psychic");
//      LIST.add("Radiant");
//      LIST.add("Thunder");
//      // Pathfinder Specific
//      LIST.add("Air");
//      LIST.add("Curse");
//      LIST.add("Darkness");
//      LIST.add("Death");
//      LIST.add("Disease");
//      LIST.add("Draconic");
//      LIST.add("Earth");
//      LIST.add("Emotion");
//      LIST.add("Fear");
//      LIST.add("Language-dependent");
//      LIST.add("Light");
//      LIST.add("Meditative");
//      LIST.add("Mind-affecting");
//      LIST.add("Pain");
//      LIST.add("Ruse");
//      LIST.add("Shadow");
//      LIST.add("Water");
//      Collections.sort(LIST);
//    }
    
    
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
      LIST.add("Electricity");
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
      LIST.add("Luck");
      LIST.add("Morale");
      LIST.add("Mithral");
      LIST.add("Force");
      LIST.add("Arcane");
      LIST.add("Necrotic");
      LIST.add("Poison");
      LIST.add("Psychic");
      LIST.add("Radiant");
      LIST.add("Thunder");
      LIST.add("Sneak");
      // Pathfinder Specific
      LIST.add("Air");
      LIST.add("Bleed");
      LIST.add("Curse");
      LIST.add("Darkness");
      LIST.add("Death");
      LIST.add("Disease");
      LIST.add("Draconic");
      LIST.add("Earth");
      LIST.add("Emotion");
      LIST.add("Fear");
      LIST.add("Language-dependent");
      LIST.add("Light");
      LIST.add("Meditative");
      LIST.add("Mind-affecting");
      LIST.add("Pain");
      LIST.add("Ruse");
      LIST.add("Shadow");
      LIST.add("Water");
      LIST.add("Phantasm");
      LIST.add("Insight");
      LIST.add("Charm");
      LIST.add("Petrification");
      LIST.add("Paralysis");
      LIST.add("Phantasms");
      LIST.add("Sleep");
      LIST.add("Compulsion");
      LIST.add("Stun");
      LIST.add("Polymorph");
      LIST.add("Ability Drain");
      LIST.add("Energy Drain");
      Collections.sort(LIST);

      BURST_QUALITIES = new ArrayList<>();
      BURST_QUALITIES.add("Magic");
      BURST_QUALITIES.add("Fire");
      BURST_QUALITIES.add("Electricity");
      BURST_QUALITIES.add("Cold");
      BURST_QUALITIES.add("Acid");
      BURST_QUALITIES.add("Sonic");
      BURST_QUALITIES.add("Good");
      BURST_QUALITIES.add("Evil");
      BURST_QUALITIES.add("Lawful");
      BURST_QUALITIES.add("Chaotic");
      BURST_QUALITIES.add("Force");
      BURST_QUALITIES.add("Arcane");
      BURST_QUALITIES.add("Necrotic");
      BURST_QUALITIES.add("Poison");
      BURST_QUALITIES.add("Psychic");
      BURST_QUALITIES.add("Radiant");
      BURST_QUALITIES.add("Thunder");
      BURST_QUALITIES.add("Neutral");
      // Pathfinder Specific
      BURST_QUALITIES.add("Air");
      BURST_QUALITIES.add("Curse");
      BURST_QUALITIES.add("Darkness");
      BURST_QUALITIES.add("Death");
      BURST_QUALITIES.add("Disease");
      BURST_QUALITIES.add("Draconic");
      BURST_QUALITIES.add("Earth");
      BURST_QUALITIES.add("Emotion");
      BURST_QUALITIES.add("Fear");
      BURST_QUALITIES.add("Language-dependent");
      BURST_QUALITIES.add("Light");
      BURST_QUALITIES.add("Meditative");
      BURST_QUALITIES.add("Mind-affecting");
      BURST_QUALITIES.add("Pain");
      BURST_QUALITIES.add("Ruse");
      BURST_QUALITIES.add("Shadow");
      BURST_QUALITIES.add("Water");
      Collections.sort(BURST_QUALITIES);

      PHYSICAL = new ArrayList<>(3);
      PHYSICAL.add("Slashing");
      PHYSICAL.add("Bludgeoning");
      PHYSICAL.add("Piercing");
      Collections.sort(PHYSICAL);
      
      CONDITIONS = new ArrayList<>();      
      CONDITIONS.add("Antagonized");
      CONDITIONS.add("Ability Damaged");
      CONDITIONS.add("Ability Drained");
      CONDITIONS.add("Bleed");
      CONDITIONS.add("Blinded");
      CONDITIONS.add("Blown Away");
      CONDITIONS.add("Broken");
      CONDITIONS.add("Checked");
      CONDITIONS.add("Confused");
      CONDITIONS.add("Cowering");
      CONDITIONS.add("Charmed");
      CONDITIONS.add("Dazed");
      CONDITIONS.add("Dazzled");
      CONDITIONS.add("Dead");
      CONDITIONS.add("Deafened");
      CONDITIONS.add("Disabled");
      CONDITIONS.add("Dying");
      CONDITIONS.add("Energy Drained");
      CONDITIONS.add("Entangled");
      CONDITIONS.add("Exhausted");
      CONDITIONS.add("Fascinated");
      CONDITIONS.add("Fatigued");
      CONDITIONS.add("Flat-footed");
      CONDITIONS.add("Frightened");
      CONDITIONS.add("Grappling");
      CONDITIONS.add("Hasted");
      CONDITIONS.add("Helpless");
      CONDITIONS.add("Hidden");
      CONDITIONS.add("Incapacitated");
      CONDITIONS.add("Incorporeal");
      CONDITIONS.add("Invisible");
      CONDITIONS.add("Knocked Down");
      CONDITIONS.add("Nauseated");
      CONDITIONS.add("Panicked");
      CONDITIONS.add("Paralyzed");
      CONDITIONS.add("Petrified");
      CONDITIONS.add("Pinned");
      CONDITIONS.add("Poisoned");
      CONDITIONS.add("Prone");
      CONDITIONS.add("Restrained");
      CONDITIONS.add("Shaken");
      CONDITIONS.add("Sickened");
      CONDITIONS.add("Sinking");
      CONDITIONS.add("Sleeping");
      CONDITIONS.add("Stable");
      CONDITIONS.add("Staggered");
      CONDITIONS.add("Stunned");
      CONDITIONS.add("Turned");
      CONDITIONS.add("Unconscious");
      Collections.sort(CONDITIONS);
    }
    
    public static boolean isBurst(String attack){
      return BURST_QUALITIES.contains(capitalize(attack)) || BURST_QUALITIES.contains(attack.toLowerCase());
    }

    public static boolean isPhysical(String attack){
      return PHYSICAL.contains(capitalize(attack)) || PHYSICAL.contains(attack.toLowerCase());
    }
  }
  
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
      MODIFY_TYPES.put("Circumstance", false);
      MODIFY_TYPES.put("Dodge", true);
      MODIFY_TYPES.put("Competence", false);
      MODIFY_TYPES.put("Deflection", false);
      MODIFY_TYPES.put("Natural", false);
      MODIFY_TYPES.put("Armor", false);
      MODIFY_TYPES.put("Shield", false);
      MODIFY_TYPES.put("Enhancement", false);
      
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
  
//  public static class FeatureEffectStacking
//  {
//    public static final boolean IS_STACK_EFFECTS_INSIDE_OF_FEATURE = false;
//
//    public static final List<String> DONT_STACK;
////    public static final  List<String> STACK;
//
//    static {
//      DONT_STACK = new ArrayList<>();
//      
//      for(Entry<String, Boolean> s : ModifyType.MODIFY_TYPES.entrySet())
//      {
//        if (!s.getValue())
//        {
//          DONT_STACK.add(s.getKey());
//        }
//      }
//    }
//
//  }
  
  public static class UI
  {
    // uses default D20PRO UI
//    public JToggleButton[] accessCustomTopRightControls()
//    {
//      
//    }
  }
  
  public static class Prefs
  {
    public static final Map<String, Object> ADDON = new HashMap<>();
    static {
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_CUSTOM_DICE_HANDLER, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_REQUIRES_RESTART, true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_AD,  false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_HAS_ADVANTAGE, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_HAS_DISADVANTAGE, false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_OVERCAST,  true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_CRIT_TYPE,  false);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_FLAT_AC, true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_TOUCH_AC, true);
      ADDON.put(D20PreferencesModel_AddOn.KEY_USE_UI_ALT, false);
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
    
    public static final String KEY_USE_FLAT_AC = "useFlatAC";
    public static final String KEY_USE_TOUCH_AC = "useTouchAC";
    
    public static final String KEY_CRIT_TYPE = "customCritType";
    
    public static final String KEY_USE_UI_ALT = "useUIAlt";
    
    public D20PreferencesModel_AddOn()
    {
      // requires a restart due to UI changes which will not load unless the app is restarted
      // will look into scripting the restart
      assignBoolean( KEY_REQUIRES_RESTART, true);
      
      // we'll be checking for A/D status on all d20 rolls
      assignBoolean( KEY_USE_CUSTOM_DICE_HANDLER, false);
      
      // note that we want AD usage available
      assignBoolean( KEY_USE_AD, false );
      
      // usage flags which will be controlled from the AD toggle UI button
      assignBoolean( KEY_HAS_ADVANTAGE, false );
      assignBoolean( KEY_HAS_DISADVANTAGE, false );  
      
      // provide UI for overcasting
      assignBoolean(KEY_USE_OVERCAST, true);
      
      // enable alternate UI in Character Editor (currently only for 5e)
      assignBoolean(KEY_USE_UI_ALT, false);
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
