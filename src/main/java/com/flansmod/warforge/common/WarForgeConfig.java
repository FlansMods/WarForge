package com.flansmod.warforge.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;

public class WarForgeConfig 
{
	// Config
	public static Configuration configFile;
	
	// World gen
	public static final String CATEGORY_WORLD_GEN = "WorldGen";
	public static int DENSE_IRON_CELL_SIZE = 64;
	public static int DENSE_IRON_DEPOSIT_RADIUS = 4;
	public static int DENSE_IRON_MIN_INSTANCES_PER_CELL = 1;
	public static int DENSE_IRON_MAX_INSTANCES_PER_CELL = 3;
	public static int DENSE_IRON_MIN_HEIGHT = 28;
	public static int DENSE_IRON_MAX_HEIGHT = 56;
	public static int DENSE_IRON_OUTER_SHELL_RADIUS = 8;
	public static float DENSE_IRON_OUTER_SHELL_CHANCE = 0.1f;
	
	public static int DENSE_GOLD_CELL_SIZE = 128;
	public static int DENSE_GOLD_DEPOSIT_RADIUS = 3;
	public static int DENSE_GOLD_MIN_INSTANCES_PER_CELL = 1;
	public static int DENSE_GOLD_MAX_INSTANCES_PER_CELL = 2;
	public static int DENSE_GOLD_MIN_HEIGHT = 6;
	public static int DENSE_GOLD_MAX_HEIGHT = 26;
	public static int DENSE_GOLD_OUTER_SHELL_RADIUS = 6;
	public static float DENSE_GOLD_OUTER_SHELL_CHANCE = 0.05f;
	
	public static int DENSE_DIAMOND_CELL_SIZE = 128;
	public static int DENSE_DIAMOND_DEPOSIT_RADIUS = 2;
	public static int DENSE_DIAMOND_MIN_INSTANCES_PER_CELL = 1;
	public static int DENSE_DIAMOND_MAX_INSTANCES_PER_CELL = 1;
	public static int DENSE_DIAMOND_MIN_HEIGHT = 1;
	public static int DENSE_DIAMOND_MAX_HEIGHT = 4;
	public static int DENSE_DIAMOND_OUTER_SHELL_RADIUS = 5;
	public static float DENSE_DIAMOND_OUTER_SHELL_CHANCE = 0.025f;
	
	public static int MAGMA_VENT_CELL_SIZE = 64;
	public static int MAGMA_VENT_DEPOSIT_RADIUS = 2;
	public static int MAGMA_VENT_MIN_INSTANCES_PER_CELL = 1;
	public static int MAGMA_VENT_MAX_INSTANCES_PER_CELL = 1;
	public static int MAGMA_VENT_MIN_HEIGHT = 1;
	public static int MAGMA_VENT_MAX_HEIGHT = 4;
	public static int MAGMA_VENT_OUTER_SHELL_RADIUS = 0;
	public static float MAGMA_VENT_OUTER_SHELL_CHANCE = 1.0f;
	
	public static int ANCIENT_OAK_CELL_SIZE = 256;
	public static float ANCIENT_OAK_CHANCE = 0.1f;
	public static float ANCIENT_OAK_HOLE_RADIUS = 24f;
	public static float ANCIENT_OAK_MAX_TRUNK_RADIUS = 8f;
	public static float ANCIENT_OAK_CORE_RADIUS = 2f;
	public static float ANCIENT_OAK_MAX_HEIGHT = 128f;
	
	public static int CLAY_POOL_CHANCE = 32;
	
	public static int QUARTZ_PILLAR_CHANCE = 128;
	
	public static final int HIGHEST_YIELD_ASSUMPTION = 64;
	
	// Claims
	public static final String CATEGORY_CLAIMS = "Claims";
	public static int CLAIM_STRENGTH_CITADEL = 15;
	public static int CLAIM_STRENGTH_REINFORCED = 10;
	public static int CLAIM_STRENGTH_BASIC = 5;
	public static int SUPPORT_STRENGTH_CITADEL = 3;
	public static int SUPPORT_STRENGTH_REINFORCED = 2;
	public static int SUPPORT_STRENGTH_BASIC = 1;
	
	public static int ATTACK_STRENGTH_SIEGE_CAMP = 1;
	public static float LEECH_PROPORTION_SIEGE_CAMP = 0.25f;
	
	// Yields
	public static final String CATEGORY_YIELDS = "Yields";
	public static float YIELD_DAY_LENGTH = 1.0f; // In real-world hours
	public static float NUM_IRON_PER_DAY_PER_ORE = 0.05f;
	public static boolean IRON_YIELD_AS_ORE = true; // Otherwise, give ingots
	public static float NUM_GOLD_PER_DAY_PER_ORE = 0.05f;
	public static boolean GOLD_YIELD_AS_ORE = true; // Otherwise, give ingots
	public static float NUM_DIAMOND_PER_DAY_PER_ORE = 0.05f;
	public static boolean DIAMOND_YIELD_AS_ORE = false; // Otherwise, give diamonds
	public static float NUM_CLAY_PER_DAY_PER_ORE = 0.05f;
	public static boolean CLAY_YIELD_AS_BLOCKS = false; // Otherwise, clay balls
	public static float NUM_QUARTZ_PER_DAY_PER_ORE = 0.05f;
	public static boolean QUARTZ_YIELD_AS_BLOCKS = false; // Otherwise, quartz pieces
	public static float NUM_OAK_PER_DAY_PER_LOG = 0.05f;
	public static boolean ANCIENT_OAK_YIELD_AS_LOGS = false; // Otherwise, planks 
	
	// Sieges
	public static final String CATEGORY_SIEGES = "Sieges";
	public static int SIEGE_SWING_PER_DEFENDER_DEATH = 1;
	public static int SIEGE_SWING_PER_ATTACKER_DEATH = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_BASE = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS = 1;
	public static float SIEGE_DAY_LENGTH = 24.0f; // In real-world hours
	public static float SIEGE_INFO_RADIUS = 200f;
	
	// Notoriety
	public static final String CATEGORY_NOTORIETY = "Notoriety";
	public static int NOTORIETY_PER_PLAYER_KILL = 3;
	//public static int NOTORIETY_PER_DRAGON_KILL = 9;
	public static int NOTORIETY_PER_SIEGE_ATTACK_SUCCESS = 10;
	public static int NOTORIETY_PER_SIEGE_DEFEND_SUCCESS = 10;
	
	// Vault blocks
	public static String[] VAULT_BLOCK_IDS = new String[] { "minecraft:gold_block" };
	public static ArrayList<Block> VAULT_BLOCKS = new ArrayList<Block>();
	
	public static float SHOW_NEW_AREA_TIMER = 200.0f;
    public static int FACTION_NAME_LENGTH_MAX = 32;
	public static boolean BLOCK_ENDER_CHEST = false;
	

    public static long FACTIONS_BOT_CHANNEL_ID = 799595436154683422L;
    
	public static class ProtectionConfig
	{		
		public boolean BREAK_BLOCKS = true;
		public boolean PLACE_BLOCKS = true;
		public boolean INTERACT = true;
		public boolean USE_ITEM = true;
		public boolean BLOCK_REMOVAL = true;
		public boolean EXPLOSION_DAMAGE = false;
		public boolean PLAYER_TAKE_DAMAGE_FROM_MOB = true;
		public boolean PLAYER_TAKE_DAMAGE_FROM_PLAYER = true;
		public boolean PLAYER_TAKE_DAMAGE_FROM_OTHER = true;
		public boolean PLAYER_DEAL_DAMAGE = true;
		
		private String[] BLOCK_PLACE_EXCEPTION_IDS = new String[] { "minecraft:torch" };
		private String[] BLOCK_BREAK_EXCEPTION_IDS = new String[] { "minecraft:torch" };
		private String[] BLOCK_INTERACT_EXCEPTION_IDS = new String[] { "minecraft:ender_chest" };
		private String[] ITEM_USE_EXCEPTION_IDS = new String[] { "minecraft:snowball" };
		
		public List<Block> BLOCK_PLACE_EXCEPTIONS;
		public List<Block> BLOCK_BREAK_EXCEPTIONS;
		public List<Block> BLOCK_INTERACT_EXCEPTIONS;
		public List<Item> ITEM_USE_EXCEPTIONS;
		
		private List<Block> FindBlocks(String[] input)
		{
			List<Block> output = new ArrayList<Block>(input.length);
			for(String blockID : input)
			{
				Block block = Block.getBlockFromName(blockID);
				if(block != null)
					output.add(block);
			}
			return output;
		}
		
		private List<Item> FindItems(String[] input)
		{
			List<Item> output = new ArrayList<Item>(input.length);
			for(String itemID : input)
			{
				Item item = Item.getByNameOrId(itemID);
				if(item != null)
					output.add(item);
			}
			return output;
		}
		
		public void FindBlocks()
		{
			BLOCK_PLACE_EXCEPTIONS = FindBlocks(BLOCK_PLACE_EXCEPTION_IDS);
			BLOCK_BREAK_EXCEPTIONS = FindBlocks(BLOCK_BREAK_EXCEPTION_IDS);
			BLOCK_INTERACT_EXCEPTIONS = FindBlocks(BLOCK_INTERACT_EXCEPTION_IDS);
			ITEM_USE_EXCEPTIONS = FindItems(ITEM_USE_EXCEPTION_IDS);
		}
		
		public void SyncConfig(String name, String desc)
		{
			String category = name;
			BREAK_BLOCKS = configFile.getBoolean(name + " - Break Blocks", category, BREAK_BLOCKS, "Can players break blocks in " + desc);
			PLACE_BLOCKS = configFile.getBoolean(name + " - Place Blocks", category, PLACE_BLOCKS, "Can players place blocks in " + desc);
			BLOCK_REMOVAL = configFile.getBoolean(name + " - Block Removal", category, BLOCK_REMOVAL, "Can blocks be removed at all in (including from explosions, mobs etc) " + desc);
			EXPLOSION_DAMAGE = configFile.getBoolean(name + " - Explosion Damage", category, EXPLOSION_DAMAGE, "Can explosions damage blocks in " + desc);
			INTERACT = configFile.getBoolean(name + " - Interact", category, INTERACT, "Can players interact with blocks and entities in " + desc);
			USE_ITEM = configFile.getBoolean(name + " - Use Items", category, USE_ITEM, "Can players use items in " + desc);
			PLAYER_TAKE_DAMAGE_FROM_MOB = configFile.getBoolean(name + " - Take Dmg From Mob", category, PLAYER_TAKE_DAMAGE_FROM_MOB, "Can players take mob damage in " + desc);
			PLAYER_TAKE_DAMAGE_FROM_PLAYER = configFile.getBoolean(name + " - Take Dmg From Player", category, PLAYER_TAKE_DAMAGE_FROM_PLAYER, "Can players take damage from other players in " + desc);
			PLAYER_TAKE_DAMAGE_FROM_OTHER = configFile.getBoolean(name + " - Take Any Other Dmg", category, PLAYER_TAKE_DAMAGE_FROM_OTHER, "Can players take damage from any other source in " + desc);
			PLAYER_DEAL_DAMAGE = configFile.getBoolean(name + " - Deal Damage", category, PLAYER_DEAL_DAMAGE, "Can players deal damage in " + desc);
			
			BLOCK_PLACE_EXCEPTION_IDS = configFile.getStringList(name + " - Place Exceptions", category, BLOCK_PLACE_EXCEPTION_IDS, "The block IDs that can still be placed. Has no effect if block placement is allowed anyway");
			BLOCK_BREAK_EXCEPTION_IDS = configFile.getStringList(name + " - Break Exceptions", category, BLOCK_BREAK_EXCEPTION_IDS, "The block IDs that can still be broken. Has no effect if block breaking is allowed anyway");
			BLOCK_INTERACT_EXCEPTION_IDS = configFile.getStringList(name + " - Interact Exceptions", category, BLOCK_INTERACT_EXCEPTION_IDS, "The block IDs that can still be interacted with. Has no effect if interacting is allowed anyway");
			ITEM_USE_EXCEPTION_IDS = configFile.getStringList(name + " - Use Exceptions", category, ITEM_USE_EXCEPTION_IDS, "The item IDs that can still be used. Has no effect if interacting is allowed anyway");
				
			
		}
	}
	
	// Permissions
	public static ProtectionConfig UNCLAIMED = new ProtectionConfig();
	public static ProtectionConfig SAFE_ZONE = new ProtectionConfig();
	public static ProtectionConfig WAR_ZONE = new ProtectionConfig();
	public static ProtectionConfig CITADEL_FRIEND = new ProtectionConfig();
	public static ProtectionConfig CITADEL_FOE = new ProtectionConfig();
	public static ProtectionConfig CLAIM_FRIEND = new ProtectionConfig();
	public static ProtectionConfig CLAIM_FOE = new ProtectionConfig();
	public static ProtectionConfig SIEGECAMP_SIEGER = new ProtectionConfig();
	public static ProtectionConfig SIEGECAMP_OTHER = new ProtectionConfig();	
	
	// Init default perms
	static
	{ 
		SAFE_ZONE.BREAK_BLOCKS = false;					SAFE_ZONE.PLACE_BLOCKS = false;						SAFE_ZONE.INTERACT = false;						SAFE_ZONE.USE_ITEM = false;
		SAFE_ZONE.PLAYER_TAKE_DAMAGE_FROM_MOB = false;	SAFE_ZONE.PLAYER_TAKE_DAMAGE_FROM_PLAYER = false;	SAFE_ZONE.PLAYER_TAKE_DAMAGE_FROM_OTHER = true;	SAFE_ZONE.PLAYER_DEAL_DAMAGE = false;
		SAFE_ZONE.BLOCK_REMOVAL = false;				SAFE_ZONE.EXPLOSION_DAMAGE = false;
		SAFE_ZONE.BLOCK_BREAK_EXCEPTION_IDS = new String[] {};	
		SAFE_ZONE.BLOCK_PLACE_EXCEPTION_IDS = new String[] {};	
		SAFE_ZONE.BLOCK_INTERACT_EXCEPTION_IDS = new String[] { "minecraft:ender_chest", "minecraft:lever", "minecraft:button", "warforge:leaderboard" };
		
		WAR_ZONE.BREAK_BLOCKS = false;					WAR_ZONE.PLACE_BLOCKS = false;						WAR_ZONE.INTERACT = true;						WAR_ZONE.USE_ITEM = true;
		WAR_ZONE.PLAYER_TAKE_DAMAGE_FROM_MOB = true;	WAR_ZONE.PLAYER_TAKE_DAMAGE_FROM_PLAYER = true;		WAR_ZONE.PLAYER_TAKE_DAMAGE_FROM_OTHER = true;	WAR_ZONE.PLAYER_DEAL_DAMAGE = true;
		WAR_ZONE.BLOCK_REMOVAL = false;					WAR_ZONE.EXPLOSION_DAMAGE = false;
		WAR_ZONE.BLOCK_BREAK_EXCEPTION_IDS = new String[] { "minecraft:web", "minecraft:tnt", "minecraft:end_crystal" };	
		WAR_ZONE.BLOCK_PLACE_EXCEPTION_IDS = new String[] { "minecraft:web", "minecraft:tnt", "minecraft:end_crystal" };	
		WAR_ZONE.BLOCK_INTERACT_EXCEPTION_IDS = new String[] { "minecraft:ender_chest", "minecraft:lever", "minecraft:button", "warforge:leaderboard" };
	
		CITADEL_FOE.BREAK_BLOCKS = false;				CITADEL_FOE.PLACE_BLOCKS = false;					CITADEL_FOE.INTERACT = false;					CITADEL_FOE.USE_ITEM = false;
		CLAIM_FOE.BREAK_BLOCKS = false;					CLAIM_FOE.PLACE_BLOCKS = false;						CLAIM_FOE.INTERACT = false;						CLAIM_FOE.USE_ITEM = false;
		
		UNCLAIMED.EXPLOSION_DAMAGE = true;
	}

	public static void SyncConfig(File suggestedFile)
	{
		configFile = new Configuration(suggestedFile);
		
		// Protections
		UNCLAIMED.SyncConfig("Unclaimed", "Unclaimed Chunks");
		SAFE_ZONE.SyncConfig("SafeZone", "Safe Zone");
		WAR_ZONE.SyncConfig("WarZone", "War Zone");
		CITADEL_FRIEND.SyncConfig("CitadelFriend", "Citadels of their Faction");
		CITADEL_FOE.SyncConfig("CitadelFoe", "Citadels of other Factions");
		CLAIM_FRIEND.SyncConfig("ClaimFriend", "Claims of their Faction");
		CLAIM_FOE.SyncConfig("ClaimFoe", "Claims of other Factions");
		//SIEGECAMP_SIEGER.syncConfig("Sieger", "Sieges they started");
		//SIEGECAMP_OTHER.syncConfig("SiegeOther", "Other sieges, defending or neutral");
		
		// World Generation Settings
		DENSE_IRON_CELL_SIZE = configFile.getInt("Dense Iron - Cell Size", CATEGORY_WORLD_GEN, DENSE_IRON_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		DENSE_IRON_DEPOSIT_RADIUS = configFile.getInt("Dense Iron - Deposit Radius", CATEGORY_WORLD_GEN, DENSE_IRON_DEPOSIT_RADIUS, 1, 16, "Radius of a deposit");
		DENSE_IRON_MIN_INSTANCES_PER_CELL = configFile.getInt("Dense Iron - Min Deposits Per Cell", CATEGORY_WORLD_GEN, DENSE_IRON_MIN_INSTANCES_PER_CELL, 0, 256, "Minimum number of deposits per cell");
		DENSE_IRON_MAX_INSTANCES_PER_CELL = configFile.getInt("Dense Iron - Max Deposits Per Cell", CATEGORY_WORLD_GEN, DENSE_IRON_MAX_INSTANCES_PER_CELL, 0, 256, "Maximum number of deposits per cell");
		DENSE_IRON_MIN_HEIGHT = configFile.getInt("Dense Iron - Min Height", CATEGORY_WORLD_GEN, DENSE_IRON_MIN_HEIGHT, 0, 256, "Minimum height of deposits");
		DENSE_IRON_MAX_HEIGHT = configFile.getInt("Dense Iron - Max Height", CATEGORY_WORLD_GEN, DENSE_IRON_MAX_HEIGHT, 0, 256, "Maximum height of deposits");
		DENSE_IRON_OUTER_SHELL_RADIUS = configFile.getInt("Dense Iron - Outer Shell Radius", CATEGORY_WORLD_GEN, DENSE_IRON_OUTER_SHELL_RADIUS, 0, 32, "Radius in which to place vanilla ores");
		DENSE_IRON_OUTER_SHELL_CHANCE = configFile.getFloat("Dense Iron - Outer Shell Chance", CATEGORY_WORLD_GEN, DENSE_IRON_OUTER_SHELL_CHANCE, 0f, 1f, "Percent of blocks in outer radius that are vanilla ores");
		
		DENSE_GOLD_CELL_SIZE = configFile.getInt("Dense Gold - Cell Size", CATEGORY_WORLD_GEN, DENSE_GOLD_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		DENSE_GOLD_DEPOSIT_RADIUS = configFile.getInt("Dense Gold - Deposit Radius", CATEGORY_WORLD_GEN, DENSE_GOLD_DEPOSIT_RADIUS, 1, 16, "Radius of a deposit");
		DENSE_GOLD_MIN_INSTANCES_PER_CELL = configFile.getInt("Dense Gold - Min Deposits Per Cell", CATEGORY_WORLD_GEN, DENSE_GOLD_MIN_INSTANCES_PER_CELL, 0, 256, "Minimum number of deposits per cell");
		DENSE_GOLD_MAX_INSTANCES_PER_CELL = configFile.getInt("Dense Gold - Max Deposits Per Cell", CATEGORY_WORLD_GEN, DENSE_GOLD_MAX_INSTANCES_PER_CELL, 0, 256, "Maximum number of deposits per cell");
		DENSE_GOLD_MIN_HEIGHT = configFile.getInt("Dense Gold - Min Height", CATEGORY_WORLD_GEN, DENSE_GOLD_MIN_HEIGHT, 0, 256, "Minimum height of deposits");
		DENSE_GOLD_MAX_HEIGHT = configFile.getInt("Dense Gold - Max Height", CATEGORY_WORLD_GEN, DENSE_GOLD_MAX_HEIGHT, 0, 256, "Maximum height of deposits");
		DENSE_GOLD_OUTER_SHELL_RADIUS = configFile.getInt("Dense Gold - Outer Shell Radius", CATEGORY_WORLD_GEN, DENSE_GOLD_OUTER_SHELL_RADIUS, 0, 32, "Radius in which to place vanilla ores");
		DENSE_GOLD_OUTER_SHELL_CHANCE = configFile.getFloat("Dense Gold - Outer Shell Chance", CATEGORY_WORLD_GEN, DENSE_GOLD_OUTER_SHELL_CHANCE, 0f, 1f, "Percent of blocks in outer radius that are vanilla ores");

		DENSE_DIAMOND_CELL_SIZE = configFile.getInt("Dense Diamond - Cell Size", CATEGORY_WORLD_GEN, DENSE_DIAMOND_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		DENSE_DIAMOND_DEPOSIT_RADIUS = configFile.getInt("Dense Diamond - Deposit Radius", CATEGORY_WORLD_GEN, DENSE_DIAMOND_DEPOSIT_RADIUS, 1, 16, "Radius of a deposit");
		DENSE_DIAMOND_MIN_INSTANCES_PER_CELL = configFile.getInt("Dense Diamond - Min Deposits Per Cell", CATEGORY_WORLD_GEN, DENSE_DIAMOND_MIN_INSTANCES_PER_CELL, 0, 256, "Minimum number of deposits per cell");
		DENSE_DIAMOND_MAX_INSTANCES_PER_CELL = configFile.getInt("Dense Diamond - Max Deposits Per Cell", CATEGORY_WORLD_GEN, DENSE_DIAMOND_MAX_INSTANCES_PER_CELL, 0, 256, "Maximum number of deposits per cell");
		DENSE_DIAMOND_MIN_HEIGHT = configFile.getInt("Dense Diamond - Min Height", CATEGORY_WORLD_GEN, DENSE_DIAMOND_MIN_HEIGHT, 0, 256, "Minimum height of deposits");
		DENSE_DIAMOND_MAX_HEIGHT = configFile.getInt("Dense Diamond - Max Height", CATEGORY_WORLD_GEN, DENSE_DIAMOND_MAX_HEIGHT, 0, 256, "Maximum height of deposits");
		DENSE_DIAMOND_OUTER_SHELL_RADIUS = configFile.getInt("Dense Diamond - Outer Shell Radius", CATEGORY_WORLD_GEN, DENSE_DIAMOND_OUTER_SHELL_RADIUS, 0, 32, "Radius in which to place vanilla ores");
		DENSE_DIAMOND_OUTER_SHELL_CHANCE = configFile.getFloat("Dense Diamond - Outer Shell Chance", CATEGORY_WORLD_GEN, DENSE_DIAMOND_OUTER_SHELL_CHANCE, 0f, 1f, "Percent of blocks in outer radius that are vanilla ores");
		
		MAGMA_VENT_CELL_SIZE = configFile.getInt("Magma Vent - Cell Size", CATEGORY_WORLD_GEN, MAGMA_VENT_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		MAGMA_VENT_DEPOSIT_RADIUS = configFile.getInt("Magma Vent - Deposit Radius", CATEGORY_WORLD_GEN, MAGMA_VENT_DEPOSIT_RADIUS, 1, 16, "Radius of a deposit");
		MAGMA_VENT_MIN_INSTANCES_PER_CELL = configFile.getInt("Magma Vent - Min Deposits Per Cell", CATEGORY_WORLD_GEN, MAGMA_VENT_MIN_INSTANCES_PER_CELL, 0, 256, "Minimum number of deposits per cell");
		MAGMA_VENT_MAX_INSTANCES_PER_CELL = configFile.getInt("Magma Vent - Max Deposits Per Cell", CATEGORY_WORLD_GEN, MAGMA_VENT_MAX_INSTANCES_PER_CELL, 0, 256, "Maximum number of deposits per cell");
		MAGMA_VENT_MIN_HEIGHT = configFile.getInt("Magma Vent - Min Height", CATEGORY_WORLD_GEN, MAGMA_VENT_MIN_HEIGHT, 0, 256, "Minimum height of deposits");
		MAGMA_VENT_MAX_HEIGHT = configFile.getInt("Magma Vent - Max Height", CATEGORY_WORLD_GEN, MAGMA_VENT_MAX_HEIGHT, 0, 256, "Maximum height of deposits");
		MAGMA_VENT_OUTER_SHELL_RADIUS = configFile.getInt("Magma Vent - Outer Shell Radius", CATEGORY_WORLD_GEN, MAGMA_VENT_OUTER_SHELL_RADIUS, 0, 32, "Radius in which to place vanilla ores");
		MAGMA_VENT_OUTER_SHELL_CHANCE = configFile.getFloat("Magma Vent - Outer Shell Chance", CATEGORY_WORLD_GEN, MAGMA_VENT_OUTER_SHELL_CHANCE, 0f, 1f, "Percent of blocks in outer radius that are vanilla ores");

		ANCIENT_OAK_CELL_SIZE = configFile.getInt("Ancient Oak - Cell Size", CATEGORY_WORLD_GEN, ANCIENT_OAK_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		ANCIENT_OAK_HOLE_RADIUS = configFile.getFloat("Ancient Oak - Hole Radius", CATEGORY_WORLD_GEN, ANCIENT_OAK_HOLE_RADIUS, 0f, 100f, "Radius of the hole dug into the ground for the tree");
		ANCIENT_OAK_MAX_TRUNK_RADIUS = configFile.getFloat("Ancient Oak - Trunk Radius", CATEGORY_WORLD_GEN, ANCIENT_OAK_MAX_TRUNK_RADIUS, 0f, 100f, "Radius of the trunk of regular logs");
		ANCIENT_OAK_CORE_RADIUS = configFile.getFloat("Ancient Oak - Core Radius", CATEGORY_WORLD_GEN, ANCIENT_OAK_CORE_RADIUS, 0f, 100f, "Radius of the core of ancient oak blocks");
		ANCIENT_OAK_MAX_HEIGHT = configFile.getFloat("Ancient Oak - Max Height", CATEGORY_WORLD_GEN, ANCIENT_OAK_MAX_HEIGHT, 0f, 256f, "Max height of the tree");
		ANCIENT_OAK_CHANCE = configFile.getFloat("Ancient Oak - Chance", CATEGORY_WORLD_GEN, ANCIENT_OAK_CHANCE, 0f, 1f, "Chance of the tree spawning in a cell");		
		
		CLAY_POOL_CHANCE = configFile.getInt("Clay Pool Rarity", CATEGORY_WORLD_GEN, CLAY_POOL_CHANCE, 1, 1024, "Chance of a clay pool appearing per chunk");
		
		QUARTZ_PILLAR_CHANCE = configFile.getInt("Quartz Pillar Rarity", CATEGORY_WORLD_GEN, QUARTZ_PILLAR_CHANCE, 1, 1024, "Chance of a quartz pillar appearing per chunk");
			
		
		
		// Claim Settings
		CLAIM_STRENGTH_CITADEL = configFile.getInt("Citadel Claim Strength", CATEGORY_CLAIMS, CLAIM_STRENGTH_CITADEL, 1, 1024, "The strength of citadel claims");
		CLAIM_STRENGTH_REINFORCED = configFile.getInt("Reinforced Claim Strength", CATEGORY_CLAIMS, CLAIM_STRENGTH_REINFORCED, 1, 1024, "The strength of reinforced claims");
		CLAIM_STRENGTH_BASIC = configFile.getInt("Basic Claim Strength", CATEGORY_CLAIMS, CLAIM_STRENGTH_BASIC, 1, 1024, "The strength of basic claims");
		SUPPORT_STRENGTH_CITADEL = configFile.getInt("Citadel Support Strength", CATEGORY_CLAIMS, SUPPORT_STRENGTH_CITADEL, 1, 1024, "The support strength a citadel gives to adjacent claims");
		SUPPORT_STRENGTH_REINFORCED = configFile.getInt("Reinforced Support Strength", CATEGORY_CLAIMS, SUPPORT_STRENGTH_REINFORCED, 1, 1024, "The support strength a reinforced claim gives to adjacent claims");
		SUPPORT_STRENGTH_BASIC = configFile.getInt("Basic Support Strength", CATEGORY_CLAIMS, SUPPORT_STRENGTH_BASIC, 1, 1024, "The support strength a basic claim gives to adjacent claims");
		
		// Siege Camp Settings
		ATTACK_STRENGTH_SIEGE_CAMP = configFile.getInt("Siege Camp Attack Strength", CATEGORY_SIEGES, ATTACK_STRENGTH_SIEGE_CAMP, 1, 1024, "How much attack pressure a siege camp exerts on adjacent enemy claims");
		LEECH_PROPORTION_SIEGE_CAMP = configFile.getFloat("Siege Camp Leech Proportion", CATEGORY_SIEGES, LEECH_PROPORTION_SIEGE_CAMP, 0f, 1f, "What proportion of a claim's yields are leeched when a siege camp is set to leech mode");

		// Siege swing parameters
		SIEGE_SWING_PER_DEFENDER_DEATH = configFile.getInt("Siege Swing Per Defender Death", CATEGORY_SIEGES, SIEGE_SWING_PER_DEFENDER_DEATH, 1, 1024, "How much a siege progress swings when a defender dies in the siege");
		SIEGE_SWING_PER_ATTACKER_DEATH = configFile.getInt("Siege Swing Per Attacker Death", CATEGORY_SIEGES, SIEGE_SWING_PER_ATTACKER_DEATH, 1, 1024, "How much a siege progress swings when an attacker dies in the siege");
		SIEGE_SWING_PER_DAY_ELAPSED_BASE = configFile.getInt("Siege Swing Per Day", CATEGORY_SIEGES, SIEGE_SWING_PER_DAY_ELAPSED_BASE, 1, 1024, "How much a siege progress swings each day (see below). This happens regardless of logins");
		SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS = configFile.getInt("Siege Swing Per Day Without Attacker Logins", CATEGORY_SIEGES, SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS, 1, 1024, "How much a siege progress swings when no attackers have logged on for a day (see below)");
		SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS = configFile.getInt("Siege Swing Per Day Without Defender Logins", CATEGORY_SIEGES, SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS, 1, 1024, "How much a siege progress swings when no defenders have logged on for a day (see below)");
		SIEGE_DAY_LENGTH = configFile.getFloat("Siege Day Length", CATEGORY_SIEGES, SIEGE_DAY_LENGTH, 0.0001f, 100000f, "The length of a day for siege login purposes, in real-world hours.");
		SIEGE_INFO_RADIUS = configFile.getFloat("Siege Info Radius", CATEGORY_SIEGES, SIEGE_INFO_RADIUS, 1f, 1000f, "The range at which you see siege information. (Capped by the server setting)");
		
		// Vault parameters
		VAULT_BLOCK_IDS = configFile.getStringList("Valuable Blocks", Configuration.CATEGORY_GENERAL, VAULT_BLOCK_IDS, "The block IDs that count towards the value of your citadel's vault");
		
		// Yield paramters
		NUM_IRON_PER_DAY_PER_ORE = configFile.getFloat("#Iron Per Day Per Ore", CATEGORY_YIELDS, NUM_IRON_PER_DAY_PER_ORE, 0.001f, 1000f, "For each dense iron ore block in a claim, how many resources do players get per yield timer");
		NUM_GOLD_PER_DAY_PER_ORE = configFile.getFloat("#Gold Per Day Per Ore", CATEGORY_YIELDS, NUM_GOLD_PER_DAY_PER_ORE, 0.001f, 1000f, "For each dense gold ore block in a claim, how many resources do players get per yield timer");
		NUM_DIAMOND_PER_DAY_PER_ORE = configFile.getFloat("#Diamond Per Day Per Ore", CATEGORY_YIELDS, NUM_DIAMOND_PER_DAY_PER_ORE, 0.001f, 1000f, "For each dense diamond ore block in a claim, how many resources do players get per yield timer");
		NUM_QUARTZ_PER_DAY_PER_ORE = configFile.getFloat("#Quartz Per Day Per Ore", CATEGORY_YIELDS, NUM_QUARTZ_PER_DAY_PER_ORE, 0.001f, 1000f, "For each dense quartz ore block in a claim, how many resources do players get per yield timer");
		NUM_CLAY_PER_DAY_PER_ORE = configFile.getFloat("#Clay Per Day Per Dense Clay", CATEGORY_YIELDS, NUM_CLAY_PER_DAY_PER_ORE, 0.001f, 1000f, "For each dense clay block in a claim, how many resources do players get per yield timer");
		NUM_OAK_PER_DAY_PER_LOG = configFile.getFloat("#Log Per Day Per Ancient Oak", CATEGORY_YIELDS, NUM_OAK_PER_DAY_PER_LOG, 0.001f, 1000f, "For each ancient oak block in a claim, how many resources do players get per yield timer");
		IRON_YIELD_AS_ORE = configFile.getBoolean("Iron Yield As Ore", CATEGORY_YIELDS, IRON_YIELD_AS_ORE, "If true, dense iron ore gives ore blocks. If false, it gives ingots");
		GOLD_YIELD_AS_ORE = configFile.getBoolean("Gold Yield As Ore", CATEGORY_YIELDS, GOLD_YIELD_AS_ORE, "If true, dense gold ore gives ore blocks. If false, it gives ingots");
		DIAMOND_YIELD_AS_ORE = configFile.getBoolean("Diamond Yield As Ore", CATEGORY_YIELDS, DIAMOND_YIELD_AS_ORE, "If true, dense diamond ore gives ore blocks. If false, it gives diamonds");
		CLAY_YIELD_AS_BLOCKS = configFile.getBoolean("Clay Yield As Blocks", CATEGORY_YIELDS, CLAY_YIELD_AS_BLOCKS, "If true, dense clay gives clay blocks. If false, it gives clay balls");
		QUARTZ_YIELD_AS_BLOCKS = configFile.getBoolean("Quartz Yield As Blocks", CATEGORY_YIELDS, QUARTZ_YIELD_AS_BLOCKS, "If true, dense quartz ore gives quartz blocks. If false, it gives quartz items");
		ANCIENT_OAK_YIELD_AS_LOGS = configFile.getBoolean("Ancient Oak Yield As Logs", CATEGORY_YIELDS, DIAMOND_YIELD_AS_ORE, "If true, ancient oak gives logs. If false, it gives planks");
		YIELD_DAY_LENGTH = configFile.getFloat("Yield Day Length", CATEGORY_YIELDS, YIELD_DAY_LENGTH, 0.0001f, 100000f, "The length of time between yields, in real-world hours.");
				
	
		
		// Notoriety
		NOTORIETY_PER_PLAYER_KILL = configFile.getInt("Notoriety gain per PVP kill", CATEGORY_NOTORIETY, NOTORIETY_PER_PLAYER_KILL, 0, 1024, "How much notoriety a player earns for their faction when killing another player");
		NOTORIETY_PER_SIEGE_ATTACK_SUCCESS = configFile.getInt("Notoriety gain per siege attack win", CATEGORY_NOTORIETY, NOTORIETY_PER_SIEGE_ATTACK_SUCCESS, 0, 1024, "How much notoriety a faction earns when successfully winning an siege as attacker");
		NOTORIETY_PER_SIEGE_DEFEND_SUCCESS = configFile.getInt("Notoriety gain per siege defend win", CATEGORY_NOTORIETY, NOTORIETY_PER_SIEGE_DEFEND_SUCCESS, 0, 1024, "How much notoriety a faction earns when successfully defending a siege");
		
				
		// Visual
		SHOW_NEW_AREA_TIMER = configFile.getFloat("New Area Timer", Configuration.CATEGORY_GENERAL, SHOW_NEW_AREA_TIMER, 0.0f, 1000f, "How many in-game ticks to show the 'You have entered {faction}' message for.");
		FACTION_NAME_LENGTH_MAX = configFile.getInt("Max Faction Name Length", Configuration.CATEGORY_GENERAL, FACTION_NAME_LENGTH_MAX, 3, 128, "How many characters long can a faction name be.");
		
		
		// Other permissions
		BLOCK_ENDER_CHEST = configFile.getBoolean("Disable Ender Chest", Configuration.CATEGORY_GENERAL, BLOCK_ENDER_CHEST, "Prevent players from opening ender chests");
			
		String botChannelString = configFile.getString("Discord Bot Channel ID", Configuration.CATEGORY_GENERAL, "" + FACTIONS_BOT_CHANNEL_ID, "https://github.com/Chikachi/DiscordIntegration/wiki/IMC-Feature");
		FACTIONS_BOT_CHANNEL_ID = Long.parseLong(botChannelString);
		
		if(configFile.hasChanged())
			configFile.save();
	}
}