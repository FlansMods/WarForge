package com.flansmod.warforge.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.flansmod.warforge.common.blocks.BlockBasicClaim;
import com.flansmod.warforge.common.blocks.BlockCitadel;
import com.flansmod.warforge.common.blocks.BlockSiegeCamp;
import com.flansmod.warforge.common.blocks.BlockYieldProvider;
import com.flansmod.warforge.common.blocks.IClaim;
import com.flansmod.warforge.common.blocks.TileEntityBasicClaim;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.common.blocks.TileEntityReinforcedClaim;
import com.flansmod.warforge.common.blocks.TileEntitySiegeCamp;
import com.flansmod.warforge.common.network.PacketHandler;
import com.flansmod.warforge.common.network.PacketSiegeCampProgressUpdate;
import com.flansmod.warforge.common.network.SiegeCampProgressInfo;
import com.flansmod.warforge.common.world.WorldGenBedrockOre;
import com.flansmod.warforge.common.world.WorldGenDenseOre;
import com.flansmod.warforge.server.CommandFactions;
import com.flansmod.warforge.server.Faction;
import com.flansmod.warforge.server.ServerTickHandler;
import com.flansmod.warforge.server.Siege;
import com.google.common.io.Files;
import com.mojang.authlib.GameProfile;
import com.flansmod.warforge.server.Faction.Role;
import com.flansmod.warforge.server.FactionStorage;
import com.flansmod.warforge.server.Leaderboard;

@Mod(modid = WarForgeMod.MODID, name = WarForgeMod.NAME, version = WarForgeMod.VERSION)
public class WarForgeMod
{
    public static final String MODID = "warforge";
    public static final String NAME = "WarForge Factions";
    public static final String VERSION = "1.0";
    
	@Instance(MODID)
	public static WarForgeMod INSTANCE;
	@SidedProxy(clientSide = "com.flansmod.warforge.client.ClientProxy", serverSide = "com.flansmod.warforge.common.CommonProxy")
	public static CommonProxy proxy;
	
	// Instances of component parts of the mod
	public static Logger LOGGER;
	public static final PacketHandler NETWORK = new PacketHandler();
	public static final Leaderboard LEADERBOARD = new Leaderboard();
	public static final FactionStorage FACTIONS = new FactionStorage();
	public static final Content CONTENT = new Content();
	public static final Protections PROTECTIONS = new Protections();
	
	public static MinecraftServer MC_SERVER = null;
	public static Random rand = new Random();
	
	public static long numberOfSiegeDaysTicked = 0L;
	public static long numberOfYieldDaysTicked = 0L;
	public static long timestampOfFirstDay = 0L;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
		//Load config
		configFile = new Configuration(event.getSuggestedConfigurationFile());
		SyncConfig();
		
		timestampOfFirstDay = System.currentTimeMillis();
		numberOfSiegeDaysTicked = 0L;
		numberOfYieldDaysTicked = 0L;
        
		CONTENT.preInit();        
        
        MinecraftForge.EVENT_BUS.register(new ServerTickHandler());
        MinecraftForge.EVENT_BUS.register(this);
        proxy.PreInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		NETWORK.initialise();
		proxy.Init(event);
    }
    
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		NETWORK.postInitialise();
		proxy.PostInit(event);
		
		VAULT_BLOCKS.clear();
		for(String blockID : VAULT_BLOCK_IDS)
		{
			Block block = Block.getBlockFromName(blockID);
			if(block != null)
			{
				VAULT_BLOCKS.add(block);
				LOGGER.info("Found block with ID " + blockID + " as a valuable block for the vault");
			}
			else
				LOGGER.error("Could not find block with ID " + blockID + " as a valuable block for the vault");
				
		}
		
		UNCLAIMED.FindBlocks();
		SAFE_ZONE.FindBlocks();
		WAR_ZONE.FindBlocks();
		CITADEL_FRIEND.FindBlocks();
		CITADEL_FOE.FindBlocks();
		CLAIM_FRIEND.FindBlocks();
		CLAIM_FOE.FindBlocks();
		SIEGECAMP_SIEGER.FindBlocks();
		SIEGECAMP_OTHER.FindBlocks();		
	}
    
    public long GetSiegeDayLengthMS()
    {
    	 return (long)(
     			SIEGE_DAY_LENGTH // In hours
     			* 60f // In minutes
     			* 60f // In seconds
     			* 1000f); // In milliseconds
    }
    
    public long GetYieldDayLengthMS()
    {
    	 return (long)(
     			YIELD_DAY_LENGTH // In hours
     			* 60f // In minutes
     			* 60f // In seconds
     			* 1000f); // In milliseconds
    }
    
	public long GetMSToNextSiegeAdvance() 
	{
		long elapsedMS = System.currentTimeMillis() - timestampOfFirstDay;
		long todayElapsedMS = elapsedMS % GetSiegeDayLengthMS();
		
		return GetSiegeDayLengthMS() - todayElapsedMS;
	}
    
	public long GetMSToNextYield() 
	{
		long elapsedMS = System.currentTimeMillis() - timestampOfFirstDay;
		long todayElapsedMS = elapsedMS % GetYieldDayLengthMS();
		
		return GetYieldDayLengthMS() - todayElapsedMS;
	}
    
    public void UpdateServer()
    {
    	long msTime = System.currentTimeMillis();
    	long dayLength = GetSiegeDayLengthMS();
    	
    	long dayNumber = (msTime - timestampOfFirstDay) / dayLength;
    	
    	if(dayNumber > numberOfSiegeDaysTicked)
    	{
    		// Time to tick a new day
    		numberOfSiegeDaysTicked = dayNumber;
    		
    		MessageAll(new TextComponentString("Battle takes its toll, all sieges have advanced."), true);
    		
    		FACTIONS.AdvanceSiegeDay();
    	}
    	
    	dayLength = GetYieldDayLengthMS();
    	dayNumber = (msTime - timestampOfFirstDay) / dayLength;
    	
    	if(dayNumber > numberOfYieldDaysTicked)
    	{
    		// Time to tick a new day
    		numberOfYieldDaysTicked = dayNumber;
    		
    		MessageAll(new TextComponentString("All passive yields have been awarded."), true);
    		
    		FACTIONS.AdvanceYieldDay();
    	}
    }
    
    
    
    @SubscribeEvent
    public void PlayerDied(LivingDeathEvent event)
    {
    	if(event.getEntity().world.isRemote)
    		return;
    		
    	if(event.getEntityLiving() instanceof EntityPlayerMP)
    	{
    		FACTIONS.PlayerDied((EntityPlayerMP)event.getEntityLiving());
    	}
    }
    
    private void BlockPlacedOrRemoved(BlockEvent event, IBlockState state)
    {
    	// Check for vault value
		if(VAULT_BLOCKS.contains(state.getBlock()))
		{
			DimChunkPos chunkPos = new DimBlockPos(event.getWorld().provider.getDimension(), event.getPos()).ToChunkPos();
			UUID factionID = FACTIONS.GetClaim(chunkPos);
			if(!factionID.equals(Faction.NULL)) 
			{
				Faction faction = FACTIONS.GetFaction(factionID);
				if(faction != null)
				{
					if(faction.mCitadelPos.ToChunkPos().equals(chunkPos))
					{
						faction.EvaluateVault();
					}
				}
			}
		}
    }
    
	@SubscribeEvent
	public void BlockPlaced(BlockEvent.EntityPlaceEvent event)
	{
		if(!event.getWorld().isRemote) 
		{
			BlockPlacedOrRemoved(event, event.getPlacedBlock());
		}
	}
	
	@SubscribeEvent
	public void BlockRemoved(BlockEvent.BreakEvent event)
	{
		if(!event.getWorld().isRemote) 
		{
			BlockPlacedOrRemoved(event, event.getState());
		}
	}
    
    @SubscribeEvent
    public void PreBlockPlaced(RightClickBlock event)
    {
    	if(event.getWorld().isRemote)
    	{
    		// This is a server op
    		return;
    	}
    	
    	Item item = event.getItemStack().getItem();
    	if(item != CONTENT.citadelBlockItem
    	&& item != CONTENT.basicClaimBlockItem
    	&& item != CONTENT.reinforcedClaimBlockItem
    	&& item != CONTENT.siegeCampBlockItem)
    	{
    		// We don't care if its not one of ours
    		return;
    	}
    	
    	Block block = ((ItemBlock)item).getBlock();
    	BlockPos placementPos = event.getPos().offset(event.getFace());
    	
    	// Only players can place these blocks
    	if(!(event.getEntity() instanceof EntityPlayer))
    	{
    		event.setCanceled(true);
    		return;
    	}
    	
    	EntityPlayer player = (EntityPlayer)event.getEntity();
    	Faction playerFaction = FACTIONS.GetFactionOfPlayer(player.getUniqueID());
    	// TODO : Op override

    	// All block placements are cancelled if there is already a block from this mod in that chunk
    	DimChunkPos pos = new DimBlockPos(event.getWorld().provider.getDimension(), placementPos).ToChunkPos();
    	if(!FACTIONS.GetClaim(pos).equals(Faction.NULL))
    	{
    		player.sendMessage(new TextComponentString("This chunk already has a claim"));
			event.setCanceled(true);
			return;
    	}
    	
    	// Cancel block placement for a couple of reasons
    	if(block == CONTENT.citadelBlock)
    	{
    		if(playerFaction != null) // Can't place a second citadel
    		{
    			player.sendMessage(new TextComponentString("You are already in a faction"));
    			event.setCanceled(true);
    			return;
    		}
    	}
    	else if(block == CONTENT.basicClaimBlock
    		|| block == CONTENT.reinforcedClaimBlock)
    	{
    		if(playerFaction == null) // Can't expand your claims if you aren't in a faction
    		{
    			player.sendMessage(new TextComponentString("You aren't in a faction. Craft a citadel or join a faction"));
    			event.setCanceled(true);
    			return;
    		}
    		if(!playerFaction.IsPlayerRoleInFaction(player.getUniqueID(), Role.OFFICER))
    		{
    			player.sendMessage(new TextComponentString("You are not an officer of your faction"));
    			event.setCanceled(true);
    			return;
    		}
    	}
    	else // Must be siege block
    	{
    		if(playerFaction == null) // Can't start sieges if you aren't in a faction
    		{
    			player.sendMessage(new TextComponentString("You aren't in a faction. Craft a citadel or join a faction"));
    			event.setCanceled(true);
    			return;
    		}
    		if(!playerFaction.IsPlayerRoleInFaction(player.getUniqueID(), Role.OFFICER))
    		{
    			player.sendMessage(new TextComponentString("You are not an officer of your faction"));
    			event.setCanceled(true);
    			return;
    		}

    		ArrayList<DimChunkPos> validTargets = new ArrayList<DimChunkPos>(4);
    		int numTargets = FACTIONS.GetAdjacentClaims(playerFaction.mUUID, pos, validTargets);
    		if(numTargets == 0)
    		{
    			player.sendMessage(new TextComponentString("There are no adjacent claims to siege"));
    			event.setCanceled(true);
    			return;
    		}
    		
    		// TODO: Check for alliances with those claims
    	}
    	
    }
    

    public void MessageAll(ITextComponent msg, boolean sendToDiscord) // TODO: optional list of pings
    {
    	// TODO: Discord integration
    	if(MC_SERVER != null)
    	{
	    	for(EntityPlayerMP player : MC_SERVER.getPlayerList().getPlayers())
	    	{
	    		player.sendMessage(msg);
	    	}
    	}
    }
        
    // World Generation
	private WorldGenDenseOre ironGenerator, goldGenerator;
	private WorldGenBedrockOre diamondGenerator, magmaGenerator;
	
	@SubscribeEvent
	public void populateOverworldChunk(PopulateChunkEvent event)
	{
		// Overworld generators
		if(event.getWorld().provider.getDimension() == 0)
		{
			if(ironGenerator == null)
				ironGenerator = new WorldGenDenseOre(CONTENT.denseIronOreBlock.getDefaultState(), Blocks.IRON_ORE.getDefaultState(), 
						DENSE_IRON_CELL_SIZE, DENSE_IRON_DEPOSIT_RADIUS, DENSE_IRON_OUTER_SHELL_RADIUS, DENSE_IRON_OUTER_SHELL_CHANCE,
						DENSE_IRON_MIN_INSTANCES_PER_CELL, DENSE_IRON_MAX_INSTANCES_PER_CELL, DENSE_IRON_MIN_HEIGHT, DENSE_IRON_MAX_HEIGHT);
			if(goldGenerator == null)
				goldGenerator = new WorldGenDenseOre(CONTENT.denseGoldOreBlock.getDefaultState(), Blocks.GOLD_ORE.getDefaultState(), 
						DENSE_GOLD_CELL_SIZE, DENSE_GOLD_DEPOSIT_RADIUS, DENSE_GOLD_OUTER_SHELL_RADIUS, DENSE_GOLD_OUTER_SHELL_CHANCE,
						DENSE_GOLD_MIN_INSTANCES_PER_CELL, DENSE_GOLD_MAX_INSTANCES_PER_CELL, DENSE_GOLD_MIN_HEIGHT, DENSE_GOLD_MAX_HEIGHT);
			if(diamondGenerator == null)
				diamondGenerator = new WorldGenBedrockOre(CONTENT.denseDiamondOreBlock.getDefaultState(), Blocks.DIAMOND_ORE.getDefaultState(), 
						DENSE_DIAMOND_CELL_SIZE, DENSE_DIAMOND_DEPOSIT_RADIUS, DENSE_DIAMOND_OUTER_SHELL_RADIUS, DENSE_DIAMOND_OUTER_SHELL_CHANCE,
						DENSE_DIAMOND_MIN_INSTANCES_PER_CELL, DENSE_DIAMOND_MAX_INSTANCES_PER_CELL, DENSE_DIAMOND_MIN_HEIGHT, DENSE_DIAMOND_MAX_HEIGHT);
			if(magmaGenerator == null)
				magmaGenerator = new WorldGenBedrockOre(CONTENT.magmaVentBlock.getDefaultState(), Blocks.LAVA.getDefaultState(), 
						MAGMA_VENT_CELL_SIZE, MAGMA_VENT_DEPOSIT_RADIUS, MAGMA_VENT_OUTER_SHELL_RADIUS, MAGMA_VENT_OUTER_SHELL_CHANCE,
						MAGMA_VENT_MIN_INSTANCES_PER_CELL, MAGMA_VENT_MAX_INSTANCES_PER_CELL, MAGMA_VENT_MIN_HEIGHT, MAGMA_VENT_MAX_HEIGHT);
		
			ironGenerator.generate(event.getWorld(), event.getRand(), new BlockPos(event.getChunkX() * 16, 128, event.getChunkZ() * 16));
			goldGenerator.generate(event.getWorld(), event.getRand(), new BlockPos(event.getChunkX() * 16, 128, event.getChunkZ() * 16));
			diamondGenerator.generate(event.getWorld(), event.getRand(), new BlockPos(event.getChunkX() * 16, 128, event.getChunkZ() * 16));
			magmaGenerator.generate(event.getWorld(), event.getRand(), new BlockPos(event.getChunkX() * 16, 128, event.getChunkZ() * 16));
		}
	}
	
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
	
	// Sieges
	public static final String CATEGORY_SIEGES = "Sieges";
	public static int SIEGE_SWING_PER_DEFENDER_DEATH = 1;
	public static int SIEGE_SWING_PER_ATTACKER_DEATH = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_BASE = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS = 1;
	public static float SIEGE_DAY_LENGTH = 24.0f; // In real-world hours
	public static float SIEGE_INFO_RADIUS = 200f;
	
	// Vault blocks
	public static String[] VAULT_BLOCK_IDS = new String[] { "minecraft:gold_block" };
	public static ArrayList<Block> VAULT_BLOCKS = new ArrayList<Block>();
	
	public static float SHOW_NEW_AREA_TIMER = 200.0f;
    public static int FACTION_NAME_LENGTH_MAX = 32;
	
	public static class ProtectionConfig
	{		
		public boolean BREAK_BLOCKS = true;
		public boolean PLACE_BLOCKS = true;
		public boolean INTERACT = true;
		public boolean USE_ITEM = true;
		public boolean BLOCK_REMOVAL = true;
		public boolean EXPLOSION_DAMAGE = true;
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
	}

	public static void SyncConfig()
	{
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
		IRON_YIELD_AS_ORE = configFile.getBoolean("Iron Yield As Ore", CATEGORY_YIELDS, IRON_YIELD_AS_ORE, "If true, dense iron ore gives ore blocks. If false, it gives ingots");
		GOLD_YIELD_AS_ORE = configFile.getBoolean("Gold Yield As Ore", CATEGORY_YIELDS, GOLD_YIELD_AS_ORE, "If true, dense gold ore gives ore blocks. If false, it gives ingots");
		DIAMOND_YIELD_AS_ORE = configFile.getBoolean("Diamond Yield As Ore", CATEGORY_YIELDS, DIAMOND_YIELD_AS_ORE, "If true, dense diamond ore gives ore blocks. If false, it gives diamonds");
		YIELD_DAY_LENGTH = configFile.getFloat("Yield Day Length", CATEGORY_YIELDS, YIELD_DAY_LENGTH, 0.0001f, 100000f, "The length of time between yields, in real-world hours.");
				
		// Visual
		SHOW_NEW_AREA_TIMER = configFile.getFloat("New Area Timer", Configuration.CATEGORY_GENERAL, SHOW_NEW_AREA_TIMER, 0.0f, 1000f, "How many in-game ticks to show the 'You have entered {faction}' message for.");
		FACTION_NAME_LENGTH_MAX = configFile.getInt("Max Faction Name Length", Configuration.CATEGORY_GENERAL, FACTION_NAME_LENGTH_MAX, 3, 128, "How many characters long can a faction name be.");
		
			
		if(configFile.hasChanged())
			configFile.save();
	}
	
	private void ReadFromNBT(NBTTagCompound tags)
	{
		FACTIONS.ReadFromNBT(tags);
		
		timestampOfFirstDay = tags.getLong("zero-timestamp");
		numberOfSiegeDaysTicked = tags.getLong("num-days-elapsed");
		numberOfYieldDaysTicked = tags.getLong("num-yields-awarded");
	}
	
	private void WriteToNBT(NBTTagCompound tags)
	{
		FACTIONS.WriteToNBT(tags);
		
		tags.setLong("zero-timestamp", timestampOfFirstDay);
		tags.setLong("num-days-elapsed", numberOfSiegeDaysTicked);
		tags.setLong("num-yields-awarded", numberOfYieldDaysTicked);
	}
	
	private static File getFactionsFile()
	{
		if(MC_SERVER.isDedicatedServer())
		{
			return new File(MC_SERVER.getFolderName() + "/warforgefactions.dat");
		}
		return new File("saves/" + MC_SERVER.getFolderName() + "/warforgefactions.dat");
	}
	
	private static File getFactionsFileBackup()
	{
		if(MC_SERVER.isDedicatedServer())
		{
			return new File(MC_SERVER.getFolderName() + "/warforgefactions.dat.bak");
		}
		return new File("saves/" + MC_SERVER.getFolderName() + "/warforgefactions.dat.bak");
		
		//return new File(MC_SERVER.getWorld(0).getSaveHandler().getWorldDirectory() + "/warforgefactions.dat.bak");
	}
		
	@EventHandler
	public void ServerAboutToStart(FMLServerAboutToStartEvent event)
	{
		MC_SERVER = event.getServer();
		CommandHandler handler = ((CommandHandler)MC_SERVER.getCommandManager());
		handler.registerCommand(new CommandFactions());
		
		try
		{
			NBTTagCompound tags = CompressedStreamTools.readCompressed(new FileInputStream(getFactionsFile()));
			ReadFromNBT(tags);
			LOGGER.info("Successfully loaded warforgefactions.dat");
		}
		catch(Exception e)
		{
			LOGGER.error("Failed to load warforgefactions.dat");
			e.printStackTrace();
		}
	}
	
	private void Save()
	{
		try
		{
			if(MC_SERVER != null)
			{
				NBTTagCompound tags = new NBTTagCompound();
				WriteToNBT(tags);
				
				File factionsFile = getFactionsFile();
				if(factionsFile.exists())
					Files.copy(factionsFile, getFactionsFileBackup());
				else
				{
					factionsFile.createNewFile();
				}
				
				CompressedStreamTools.writeCompressed(tags, new FileOutputStream(factionsFile));
				LOGGER.info("Successfully saved warforgefactions.dat");
			}
		}
		catch(Exception e)
		{
			LOGGER.error("Failed to save warforgefactions.dat");
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void SaveEvent(WorldEvent.Save event)
	{
		if(!event.getWorld().isRemote)
		{
			Save();
		}
	}
	
	@EventHandler
	public void ServerStopped(FMLServerStoppingEvent event)
	{
		Save();
		MC_SERVER = null;
	}
	
    // Helpers

    public static UUID GetUUID(ICommandSender sender)
    {
    	if(sender instanceof EntityPlayer)
    		return ((EntityPlayer)sender).getUniqueID();
    	return UUID.fromString("Unknown");
    }
    
    public static boolean IsOp(ICommandSender sender)
    {
    	if(sender instanceof EntityPlayer)
    		return MC_SERVER.getPlayerList().canSendCommands(((EntityPlayer)sender).getGameProfile());
    	return sender instanceof MinecraftServer;
    }

}
