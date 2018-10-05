package com.d20pro.plugin.rules.foure;

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
 * @author saethi
 * @author owlbear
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class FourERules extends AbstractRules
{
  public FourERules()
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
    public static final byte LUCK = 6;

    public static final String[] NAMES = { "Natural", "Armor", "Shield", "Deflect", "Enhancement", "Dodge"}; //, "Luck" };

    public static final String[] LABELS = { "Base", "plus", "Armor", "plus", "Shield", "plus", "Natural", "Dex", "plus", "Size", "plus", "Deflect", "plus", "Unnamed", "plus", "Dodge", "and", "Max Dex"};
    
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

    private final int _toHit;

    private final int _toDamage;

    private final boolean _isFlank;
    
    private final boolean _isAD;
    
    private final boolean _isAdvantage;
    
    private final boolean _isDisadvantage;

    private final boolean _isSneak;

    private final Dice _diceSneak;

    private final boolean _isPowerAttack;

    private final int _powerAttack;

    /** The last attacks this creature declared */
    private DeclaredCreatureAttack[] _attacks;

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
  
  public static class Skill
  {
    public static final byte MAX_RANKS = 99;
    public static final byte MAX_BONUS = 99;
    public static final String[] HEADERS = {"", "Mod", "Skill Name", "Abil", "Rank", "Misc"};
    
    public static String SHOW_ALL = "Show All";
    public static String RANKED = "Ranked";
    public static String UNRANKED = "Unranked";
    public static String TOTAL_RANKS = "Ranks";

    public int getSkillBonus(CreatureTemplate template, int ranks)
    {
      return ranks;
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
      // byte[] saves = t.getSaves();
      // byte[] newsaves = new byte[Save.MAP.length];
      //
      // // copy existing saves into newsaves
      // System.arraycopy(saves, 0, newsaves, 0, Save.MAP.length);
      //
      // CreatureTemplate_Classes classes = t.getClasses();
      // // byte bonus = classes.resolveBAB(app, t);
      // double mod = Math.ceil(classes.resolveLevel() * .25);
      // int pb = (int) (1 + mod);
      //
      // for (GenericCreatureClass c : classes.accessClasses())
      // {
      // String[] s = c.accessClassTemplate().accessSavingThrows();
      // for (int i = 0; i < s.length; i++)
      // {
      // int idx = Arrays.asList(Save.NAMES).indexOf(s[i]);
      //
      // byte ability = (byte) t.accessAbilityScoreMod(Save.MAP[idx]);
      // // byte bonus = (byte) (newsaves[idx] - ability);
      // byte newsave = (byte) (pb + ability);// + bonus);
      // if (newsave > saves[idx])
      // newsaves[idx] = newsave;
      // }
      // }
      //
      // t.setSaves(newsaves);
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

  public static class Type
  {
    public static final String[] NAMES = { "Unknown", "Aberration", "Animal", "Construct", "Dragon", "Elemental", "Fey",
            "Giant", "Humanoid", "Magical Beast", "M. Humanoid", "Ooze", "Outsider", "Plant", "Undead", "Vermin" };

    public static final byte[] AttackRate = { 1 };

    public static final String[] HD = { "d8" };

    public static final String[] GoodSaves = { "Fort, Ref, Will" };
  }

  public static class CreatureClass
  {
    public static final byte MAX_CLASS_LEVEL = 30;

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

    public byte[] calculateSaves(CreatureTemplate creature)
    {
//      //// System.out.println(Thread.currentThread().getStackTrace()[2].getMethodName());
      if (creature.isCalculating())
        return null;
      else
        creature.isCalculating(true);

      checkHistorical(creature);

      byte[] map = Save.MAP;
      String[] names = Save.NAMES;
      ArrayList<GenericCreatureClass> classes = creature.getClasses().accessClasses();

      byte[] saves = new byte[map.length];

      byte[] base = creature.getSavesBase();

      // Handle good saves
      byte[] classmod = new byte[names.length];
      Arrays.fill(classmod, (byte) 0);



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

      // creatureinplay seems hold the secret... but this isn't directly accessible via creaturetemplate...

      for (byte i = 0; i < map.length; i++)
      {
        ////// System.out.println("[D20Rules] Save calcuated from " + base[i] + " + " + creature.accessAbilityScoreMod(map[i]) + " + " + classmod[i] + " + " + mods[i]);
        saves[i] = (byte) (base[i] + creature.accessAbilityScoreMod(map[i]) + classmod[i] + creature.getSaveMod(i));
      }

      creature.setSaves(saves);
      creature.setSavesBase(base);
//      creature.setSavesMod(mods);

      return saves;
    }

    public void calculateAbilityScore(CreatureTemplate template, byte id)
    {
      // poke
      if (template.isCalculating())
        return;
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

//  public static class Spell
//  {
//    public static final byte MAX_SPELL_LEVEL = 9;
//
//    /**
//     * Calculates the number of bonus spells applicable for the given level for
//     * the given ability score.
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
//      return adjustedScore / 8 + 1;
//    }
//  }
  public static class Spell
  {
    public static final byte MAX_SPELL_LEVEL = 9;
    public static final byte KNOWN_SPELL_CATEGORIES = 2; // Cantrips &
                                                         // Non-Cantrips

    public static final byte MAX_DOMAIN_SPELLS = 18;
    public static final float DOMAIN_SPELL_RATE = 1f;
    public static final byte DOMAIN_SPELLS_PER_LEVEL = 1;

    // Note: Spell levels start at Zero so no reason to offset a check against
    // this array
    public static final int[] OVERRIDE_DEBIT_SPELLS = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    public static final boolean OVERRIDE_SPELL_REMOVE_FROM_MEMORY = false;
    public static final boolean USE_SECONDARY_DOMAIN = true;
    
    // on the fence about placing these in spell or creatureclass...

    public static final byte PREPARED = 0; // available spells to cast
    public static final byte KNOWN = 1; // standard spell book style
    public static final byte SPECIAL = 2; // domain/cicle/etc.
    public static final byte SLOTS = 3; // track available spell casting slots
    public static final byte POOL = 4; // all spells use a single spell pool to
                                       // track usage

    public static final String[] BARD_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final byte[] BARD_SPELL_UI = { PREPARED, KNOWN, SLOTS };

    public static final String[] CLERIC_SPELL_TABS = { "Spells Prepared", "Domain" };
    public static final byte[] CLERIC_SPELL_UI = { PREPARED, SPECIAL };
    public static final byte CLERIC_MAX_DOMAIN_SPELLS = 18;
    public static final float CLERIC_DOMAIN_SPELL_RATE = 1f;
    public static final byte CLERIC_DOMAIN_SPELLS_PER_LEVEL = 1;
    public static final byte CLERIC_DOMAIN_MIN_LEVEL = 1;
    public static final byte CLERIC_DOMAIN_MAX_LEVEL = 5;

    public static final String[] DRUID_SPELL_TABS = { "Spells Prepared", "Circle" };
    public static final byte[] DRUID_SPELL_UI = { PREPARED, SPECIAL };
    public static final byte DRUID_MAX_DOMAIN_SPELLS = 8;
    public static final float DRUID_DOMAIN_SPELL_RATE = 1f;
    public static final byte DRUID_DOMAIN_SPELLS_PER_LEVEL = 1;
    public static final byte DRUID_DOMAIN_MIN_LEVEL = 2;
    public static final byte DRUID_DOMAIN_MAX_LEVEL = 5;

    public static final String[] PALADIN_SPELL_TABS = { "Spells Prepared", "Oath" };
    public static final byte[] PALADIN_SPELL_UI = { PREPARED, SPECIAL };
    public static final byte PALADIN_MAX_DOMAIN_SPELLS = 18;
    public static final float PALADIN_DOMAIN_SPELL_RATE = 1f;
    public static final byte PALADIN_DOMAIN_SPELLS_PER_LEVEL = 1;
    public static final byte PALADIN_DOMAIN_MIN_LEVEL = 1;
    public static final byte PALADIN_DOMAIN_MAX_LEVEL = 5;

    public static final String[] RANGER_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final byte[] RANGER_SPELL_UI = { PREPARED, KNOWN };

    public static final String[] ROGUE_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final byte[] ROGUE_SPELL_UI = { PREPARED, KNOWN, SLOTS };

    public static final String[] SORCERER_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final byte[] SORCERER_SPELL_UI = { PREPARED, KNOWN, SLOTS };

    public static final String[] WARLOCK_SPELL_TABS = { "Spells Known" };
    public static final byte[] WARLOCK_SPELL_UI = { KNOWN, SLOTS, POOL };

    public static final String[] WIZARD_SPELL_TABS = { "Spells Prepared", "Spells Known" };
    public static final byte[] WIZARD_SPELL_UI = { PREPARED, KNOWN };

    public static final String[] GENERIC_SPELL_TABS = { "Spells Prepared", "Spells Known", "Special" };
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
      if (spellLevel < 1 || spellLevel > MAX_SPELL_LEVEL)
        return 0;

      int adjustedScore = abilityScore - (spellLevel - 1) * 2 - 12;

      if (adjustedScore < 0)
        return 0;

      return adjustedScore / 8 + 1;
    }

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

      //byte STR = Ability.STR;
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
      return GameSystem.ThreeFive;
    }
  }

  public enum PowerAttack
  {
    NA("N/A"), FiveE("5E"), FourE("4E"), ThreeFive("3.5"), Pathfinder("Pathfinder");

    public final String name;

    private PowerAttack(String name)
    {
      this.name = name;
    }

    @Override
    public String toString()
    {
      return name;
    }
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
    public static final byte SPELL_POOL = 5;

    public static final String[] NAMES = { "At Will", "Per Day", "Per Init", "Charge", "Recharge", "Spell Pool" };
  }

  public static class Actions
  {
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
      GROUP_TO_TARGET.put("Skill Rank", stubRank);//filling up on game start
      GROUP_TO_TARGET.put("Skill Misc", stubMisc);
      //-----------------------------------------------------
      GROUP_TO_TARGET.put("Speed",Arrays.asList(MovementType.FULLNAMES));
      //-----------------------------------------------------
      GROUP_TO_TARGET.put("Ability", Arrays.asList(Ability.NAMES));
      GROUP_TO_TARGET.put("Saving Throw", Arrays.asList(Save.FULL_NAMES));
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
//    public static List<String> LIST;
//
//    static {
//      LIST = new ArrayList<>();
//      LIST.add("Fire");
//      LIST.add("Cold");
//      LIST.add("Acid");
//      LIST.add("Electricity");
//      LIST.add("Sonic");
//      LIST.add("Arcane");
//      LIST.add("Necrotic");
//      LIST.add("Poison");
//      LIST.add("Psychic");
//      LIST.add("Radiant");
//      LIST.add("Thunder");
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
      BURST_QUALITIES.add("Electricity");
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
      return BURST_QUALITIES.contains(attack);
    }

    public static boolean isPhysical(String attack){
      return PHYSICAL.contains(attack);
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
  
  public static class FeatureEffectStacking
  {
    public static final boolean IS_STACK_EFFECTS_INSIDE_OF_FEATURE = false;

    public static final List<String> DONT_STACK;
//    public static final  List<String> STACK;

    static {
      DONT_STACK = new ArrayList<>();
      
      for(Entry<String, Boolean> s : ModifyType.MODIFY_TYPES.entrySet())
      {
        if (!s.getValue())
        {
          DONT_STACK.add(s.getKey());
        }
      }
    }

  }



  public static class UI
  {
    // uses default D20PRO UI
//  public JToggleButton[] accessCustomTopRightControls()
//  {
//    
//  }
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
}
