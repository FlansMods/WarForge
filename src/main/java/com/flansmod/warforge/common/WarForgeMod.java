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
import com.flansmod.warforge.server.Leaderboard;

@Mod(modid = WarForgeMod.MODID, name = WarForgeMod.NAME, version = WarForgeMod.VERSION)
public class WarForgeMod
{
    public static final String MODID = "warforge";
    public static final String NAME = "WarForge Factions";
    public static final String VERSION = "1.0";
    
    private HashMap<UUID, Faction> mFactions = new HashMap<UUID, Faction>();
    // This map contains every single claim, including siege camps.
    // So if you take one of these and try to look it up in the faction, check their active sieges too
    private HashMap<DimChunkPos, UUID> mClaims = new HashMap<DimChunkPos, UUID>();
    
    // This is all the currently active sieges, keyed by the defending position
    private HashMap<DimChunkPos, Siege> mSieges = new HashMap<DimChunkPos, Siege>();
    
	@Instance(MODID)
	public static WarForgeMod INSTANCE;
	@SidedProxy(clientSide = "com.flansmod.warforge.client.ClientProxy", serverSide = "com.flansmod.warforge.common.CommonProxy")
	public static CommonProxy proxy;
	
	public static Logger sLogger;
	public static final PacketHandler sPacketHandler = new PacketHandler();
	public static final Leaderboard sLeaderboard = new Leaderboard();

	public static Block citadelBlock, basicClaimBlock, reinforcedClaimBlock, siegeCampBlock;
	public static Item citadelBlockItem, basicClaimBlockItem, reinforcedClaimBlockItem, siegeCampBlockItem;
	
	public static Block denseIronOreBlock, denseGoldOreBlock, denseDiamondOreBlock, magmaVentBlock;
	public static Item denseIronOreItem, denseGoldOreItem, denseDiamondOreItem, magmaVentItem;
	
	public static MinecraftServer MC_SERVER = null;
	public static Random rand = new Random();
	
	public static long numberOfSiegeDaysTicked = 0L;
	public static long numberOfYieldDaysTicked = 0L;
	public static long timestampOfFirstDay = 0L;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        sLogger = event.getModLog();
		//Load config
		configFile = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();
		
		timestampOfFirstDay = System.currentTimeMillis();
		numberOfSiegeDaysTicked = 0L;
		numberOfYieldDaysTicked = 0L;
        
        citadelBlock = new BlockCitadel(Material.ROCK).setRegistryName("citadelblock").setUnlocalizedName("citadelblock");
        citadelBlockItem = new ItemBlock(citadelBlock).setRegistryName("citadelblock").setUnlocalizedName("citadelblock");
        GameRegistry.registerTileEntity(TileEntityCitadel.class, new ResourceLocation(MODID, "citadel"));
        
        // Basic and reinforced claims, they share a tile entity
        basicClaimBlock = new BlockBasicClaim(Material.ROCK).setRegistryName("basicclaimblock").setUnlocalizedName("basicclaimblock");
        basicClaimBlockItem = new ItemBlock(basicClaimBlock).setRegistryName("basicclaimblock").setUnlocalizedName("basicclaimblock");
        GameRegistry.registerTileEntity(TileEntityBasicClaim.class, new ResourceLocation(MODID, "basicclaim"));
        reinforcedClaimBlock = new BlockBasicClaim(Material.ROCK).setRegistryName("reinforcedclaimblock").setUnlocalizedName("reinforcedclaimblock");
        reinforcedClaimBlockItem = new ItemBlock(reinforcedClaimBlock).setRegistryName("reinforcedclaimblock").setUnlocalizedName("reinforcedclaimblock");
        GameRegistry.registerTileEntity(TileEntityReinforcedClaim.class, new ResourceLocation(MODID, "reinforcedclaim"));
        
        // Siege camp
        siegeCampBlock = new BlockSiegeCamp(Material.ROCK).setRegistryName("siegecampblock").setUnlocalizedName("siegecampblock");
        siegeCampBlockItem = new ItemBlock(siegeCampBlock).setRegistryName("siegecampblock").setUnlocalizedName("siegecampblock");
        GameRegistry.registerTileEntity(TileEntitySiegeCamp.class, new ResourceLocation(MODID, "siegecamp"));
        
        denseIronOreBlock = new BlockYieldProvider(Material.ROCK, IRON_YIELD_AS_ORE ? new ItemStack(Blocks.IRON_ORE) : new ItemStack(Items.IRON_INGOT), NUM_IRON_PER_DAY_PER_ORE).setRegistryName("denseironore").setUnlocalizedName("denseironore");
        denseGoldOreBlock = new BlockYieldProvider(Material.ROCK, GOLD_YIELD_AS_ORE ? new ItemStack(Blocks.GOLD_ORE) : new ItemStack(Items.GOLD_INGOT), NUM_GOLD_PER_DAY_PER_ORE).setRegistryName("densegoldore").setUnlocalizedName("densegoldore");
        denseDiamondOreBlock = new BlockYieldProvider(Material.ROCK, DIAMOND_YIELD_AS_ORE ? new ItemStack(Blocks.DIAMOND_ORE) : new ItemStack(Items.DIAMOND), NUM_DIAMOND_PER_DAY_PER_ORE).setRegistryName("densediamondore").setUnlocalizedName("densediamondore");
        magmaVentBlock = new BlockYieldProvider(Material.ROCK, new ItemStack(Items.LAVA_BUCKET), 0.01f).setRegistryName("magmavent").setUnlocalizedName("magmavent");
        
        denseIronOreItem = new ItemBlock(denseIronOreBlock).setRegistryName("denseironore").setUnlocalizedName("denseironore");
        denseGoldOreItem = new ItemBlock(denseGoldOreBlock).setRegistryName("densegoldore").setUnlocalizedName("densegoldore");
        denseDiamondOreItem = new ItemBlock(denseDiamondOreBlock).setRegistryName("densediamondore").setUnlocalizedName("densediamondore");
        magmaVentItem = new ItemBlock(magmaVentBlock).setRegistryName("magmavent").setUnlocalizedName("magmavent");
        
        
        
        MinecraftForge.EVENT_BUS.register(new ServerTickHandler());
        MinecraftForge.EVENT_BUS.register(this);
        proxy.PreInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		sPacketHandler.initialise();
		proxy.Init(event);
    }
    
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		sPacketHandler.postInitialise();
		proxy.PostInit(event);
		
		VAULT_BLOCKS.clear();
		for(String blockID : VAULT_BLOCK_IDS)
		{
			Block block = Block.getBlockFromName(blockID);
			if(block != null)
			{
				VAULT_BLOCKS.add(block);
				sLogger.info("Found block with ID " + blockID + " as a valuable block for the vault");
			}
			else
				sLogger.error("Could not find block with ID " + blockID + " as a valuable block for the vault");
				
		}
	}
    
    @SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event)
	{		
		event.getRegistry().register(citadelBlockItem);
		event.getRegistry().register(basicClaimBlockItem);
		event.getRegistry().register(reinforcedClaimBlockItem);
		event.getRegistry().register(siegeCampBlockItem);
		event.getRegistry().register(denseIronOreItem);
		event.getRegistry().register(denseGoldOreItem);
		event.getRegistry().register(denseDiamondOreItem);
		event.getRegistry().register(magmaVentItem);
		sLogger.info("Registered items");
	}
	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().register(citadelBlock);
		event.getRegistry().register(basicClaimBlock);
		event.getRegistry().register(reinforcedClaimBlock);
		event.getRegistry().register(siegeCampBlock);
		event.getRegistry().register(denseIronOreBlock);
		event.getRegistry().register(denseGoldOreBlock);
		event.getRegistry().register(denseDiamondOreBlock);
		event.getRegistry().register(magmaVentBlock);
		sLogger.info("Registered blocks");
	}
    
    public boolean IsPlayerInFaction(UUID playerID, UUID factionID)
    {
    	if(mFactions.containsKey(factionID))
    		return mFactions.get(factionID).IsPlayerInFaction(playerID);
    	return false;
    }
    
    public boolean IsPlayerRoleInFaction(UUID playerID, UUID factionID, Faction.Role role)
    {
    	if(mFactions.containsKey(factionID))
    		return mFactions.get(factionID).IsPlayerRoleInFaction(playerID, role);
    	return false;
    }
    
    public Faction GetFaction(UUID factionID)
    {
    	if(factionID.equals(Faction.NULL))
    		return null;
    	
    	if(mFactions.containsKey(factionID))
    		return mFactions.get(factionID);
    	
    	sLogger.error("Could not find a faction with UUID " + factionID);
    	return null;
    }
    
    public Faction GetFaction(String name)
    {
    	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
    	{
    		if(entry.getValue().mName.equals(name))
    			return entry.getValue();
    	}
    	return null;
    }
    
    public Faction GetFactionWithOpenInviteTo(UUID playerID)
    {
    	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
    	{
    		if(entry.getValue().IsInvitingPlayer(playerID))
    			return entry.getValue();
    	}
    	return null;
    }
    
	public String[] GetFactionNames() 
	{
		String[] names = new String[mFactions.size()];
		int i = 0;
    	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
    	{
    		names[i] = entry.getValue().mName;
    		i++;
    	}
    	return names;
	}
    
    // This is called for any non-citadel claim. Citadels can be factionless, so this makes no sense
	public void OnNonCitadelClaimPlaced(IClaim claim, EntityLivingBase placer) 
	{
		if(!placer.world.isRemote)
		{
			Faction faction = GetFactionOfPlayer(placer.getUniqueID());
			
			if(faction != null)
			{
				TileEntity tileEntity = claim.GetAsTileEntity();
				mClaims.put(claim.GetPos().ToChunkPos(), faction.mUUID);
				
				claim.OnServerSetFaction(faction);
				faction.OnClaimPlaced(claim);
			}
			else
				sLogger.error("Invalid placer placed a claim at " + claim.GetPos());
		}
	}
	    
    public UUID GetClaim(DimBlockPos pos)
    {
    	return GetClaim(pos.ToChunkPos());
    }
    
    public UUID GetClaim(DimChunkPos pos)
    {
    	if(mClaims.containsKey(pos))
    		return mClaims.get(pos);
    	return Faction.NULL;
    }
    
    public Faction GetFactionOfPlayer(UUID playerID)
    {
    	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
    	{
    		if(entry.getValue().IsPlayerInFaction(playerID))
    			return entry.getValue();
    	}
    	return null;
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
    	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
    	{
    		entry.getValue().Update();
    	}
    	
    	long msTime = System.currentTimeMillis();
    	long dayLength = GetSiegeDayLengthMS();
    	
    	long dayNumber = (msTime - timestampOfFirstDay) / dayLength;
    	
    	if(dayNumber > numberOfSiegeDaysTicked)
    	{
    		// Time to tick a new day
    		numberOfSiegeDaysTicked = dayNumber;
    		
    		MessageAll(new TextComponentString("Battle takes its toll, all sieges have advanced."), true);
    		
    		for(HashMap.Entry<DimChunkPos, Siege> kvp : mSieges.entrySet())
    		{
    			kvp.getValue().AdvanceDay();
    		}
    		
    		CheckForCompleteSieges();
    		
        	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
        	{
        		entry.getValue().AdvanceDay();
        	}
    	}
    	
    	dayLength = GetYieldDayLengthMS();
    	dayNumber = (msTime - timestampOfFirstDay) / dayLength;
    	
    	if(dayNumber > numberOfYieldDaysTicked)
    	{
    		// Time to tick a new day
    		numberOfYieldDaysTicked = dayNumber;
    		
    		MessageAll(new TextComponentString("All passive yields have been awarded."), true);
    		
        	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
        	{
        		entry.getValue().AwardYields();
        	}
    	}
    }
    
    private void CheckForCompleteSieges()
    {
    	// Cache in a list so we can remove from the siege HashMap
    	ArrayList<DimChunkPos> completedSieges = new ArrayList<DimChunkPos>();
		for(HashMap.Entry<DimChunkPos, Siege> kvp : mSieges.entrySet())
		{
			if(kvp.getValue().IsCompleted())
				completedSieges.add(kvp.getKey());
		}
		
		// Now process the results
		for(DimChunkPos chunkPos : completedSieges)
		{
			Siege siege = mSieges.get(chunkPos);
			
			Faction attackers = WarForgeMod.INSTANCE.GetFaction(siege.mAttackingFaction);
			Faction defenders = WarForgeMod.INSTANCE.GetFaction(siege.mDefendingFaction);
			
			if(attackers == null || defenders == null)
			{
				WarForgeMod.sLogger.error("Invalid factions in completed siege. Nothing will happen.");
				continue;
			}
			
			DimBlockPos blockPos = defenders.GetSpecificPosForClaim(chunkPos);
			boolean successful = siege.WasSuccessful();
			if(successful)
			{
				defenders.OnClaimLost(blockPos);
				mClaims.remove(blockPos.ToChunkPos());
				attackers.MessageAll(new TextComponentString("Our faction won the siege on " + defenders.mName + " at " + blockPos.ToFancyString()));
			}
			else
			{
				attackers.MessageAll(new TextComponentString("Our siege on " + defenders.mName + " at " + blockPos.ToFancyString() + " was unsuccessful"));
				defenders.MessageAll(new TextComponentString(attackers.mName + "'s siege on " + blockPos.ToFancyString() + " was unsuccessful"));
			}
			
			siege.OnCompleted();
			
			// Then remove the siege
			mSieges.remove(chunkPos);
		}
    	
    }
    
    @SubscribeEvent
    public void PlayerDied(LivingDeathEvent event)
    {
    	if(event.getEntity().world.isRemote)
    		return;
    		
    	if(event.getEntityLiving() instanceof EntityPlayerMP)
    	{
    		Faction faction = GetFactionOfPlayer(event.getEntityLiving().getUniqueID());
    		
    		for(HashMap.Entry<DimChunkPos, Siege> kvp : mSieges.entrySet())
    		{
    			// If the player is on the attackers side, send the event
    			if(kvp.getValue().mAttackingFaction.equals(faction.mUUID))
    			{
    				kvp.getValue().OnAttackerDied((EntityPlayerMP)event.getEntityLiving());
    			}
    			// If the player is on the defenders side, send the event
    			if(kvp.getValue().mDefendingFaction.equals(faction.mUUID))
    			{
    				kvp.getValue().OnDefenderDied((EntityPlayerMP)event.getEntityLiving());
    			}
    		}
    		
    		CheckForCompleteSieges();
    	}
    }
    
    private void BlockPlacedOrRemoved(BlockEvent event, IBlockState state)
    {
    	// Check for vault value
		if(VAULT_BLOCKS.contains(state.getBlock()))
		{
			DimChunkPos chunkPos = new DimBlockPos(event.getWorld().provider.getDimension(), event.getPos()).ToChunkPos();
			UUID factionID = GetClaim(chunkPos);
			if(!factionID.equals(Faction.NULL)) 
			{
				Faction faction = GetFaction(factionID);
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
    	if(item != citadelBlockItem
    	&& item != basicClaimBlockItem
    	&& item != reinforcedClaimBlockItem
    	&& item != siegeCampBlockItem)
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
    	Faction playerFaction = GetFactionOfPlayer(player.getUniqueID());
    	// TODO : Op override

    	// All block placements are cancelled if there is already a block from this mod in that chunk
    	DimChunkPos pos = new DimBlockPos(event.getWorld().provider.getDimension(), placementPos).ToChunkPos();
    	if(mClaims.containsKey(pos))
    	{
    		player.sendMessage(new TextComponentString("This chunk already has a claim"));
			event.setCanceled(true);
			return;
    	}
    	
    	// Cancel block placement for a couple of reasons
    	if(block == citadelBlock)
    	{
    		if(playerFaction != null) // Can't place a second citadel
    		{
    			player.sendMessage(new TextComponentString("You are already in a faction"));
    			event.setCanceled(true);
    			return;
    		}
    	}
    	else if(block == basicClaimBlock
    		|| block == reinforcedClaimBlock)
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
    		int numTargets = GetAdjacentClaims(playerFaction.mUUID, pos, validTargets);
    		if(numTargets == 0)
    		{
    			player.sendMessage(new TextComponentString("There are no adjacent claims to siege"));
    			event.setCanceled(true);
    			return;
    		}
    		
    		// TODO: Check for alliances with those claims
    	}
    	
    }
    
	public int GetAdjacentClaims(UUID excludingFaction, DimChunkPos pos, ArrayList<DimChunkPos> positions)
	{
		positions.clear();
		DimChunkPos north = pos.North();
		DimChunkPos east = pos.East();
		DimChunkPos south = pos.South();
		DimChunkPos west = pos.West();
		if(IsClaimed(excludingFaction, north))
			positions.add(north);
		if(IsClaimed(excludingFaction, east))
			positions.add(east);
		if(IsClaimed(excludingFaction, south))
			positions.add(south);
		if(IsClaimed(excludingFaction, west))
			positions.add(west);
		return positions.size();
	}
	
	public boolean IsClaimed(UUID excludingFaction, DimChunkPos pos)
	{
		UUID factionID = mClaims.get(pos);
		return factionID != null && !factionID.equals(excludingFaction) && !factionID.equals(Faction.NULL);
	}
	
    // ----------------------------------------
    //   Server responses to player requests
    public boolean RequestCreateFaction(TileEntityCitadel citadel, EntityPlayer player, String factionName)
    {
    	if(citadel == null)
    	{
    		player.sendMessage(new TextComponentString("You can't create a faction without a citadel"));
    		return false;
    	}
    	
    	if(factionName == null || factionName.isEmpty())
    	{
    		player.sendMessage(new TextComponentString("You can't create a faction with no name"));
    		return false;
    	}
    	
    	Faction existingFaction = GetFactionOfPlayer(player.getUniqueID());
    	if(existingFaction != null)
    	{
    		player.sendMessage(new TextComponentString("You are already in a faction"));
    		return false;
    	}
    	
    	UUID proposedID = Faction.CreateUUID(factionName);
    	if(mFactions.containsKey(proposedID))
    	{
    		player.sendMessage(new TextComponentString("A faction with the name " + factionName + " already exists"));
    		return false;
    	}
    	
    	// All checks passed, create a faction
    	Faction faction = new Faction();
    	faction.mUUID = proposedID;
    	faction.mName = factionName;
    	faction.mCitadelPos = new DimBlockPos(citadel);
		faction.mColour = Color.HSBtoRGB(rand.nextFloat(), rand.nextFloat() * 0.5f + 0.5f, 1.0f);
    	faction.mNotoriety = 0;
    	faction.mLegacy = 0;
    	faction.mWealth = 0;
    	
    	mFactions.put(proposedID, faction);
    	citadel.OnServerSetFaction(faction);
    	mClaims.put(citadel.GetPos().ToChunkPos(), proposedID);
    	sLeaderboard.RegisterFaction(faction);
    	
    	faction.AddPlayer(player.getUniqueID());
    	faction.SetLeader(player.getUniqueID());
    	
    	return true;
    }
    
    public boolean RequestRemovePlayerFromFaction(ICommandSender remover, UUID factionID, UUID toRemove)
    {
    	Faction faction = GetFaction(factionID);
    	if(faction == null)
    	{
    		remover.sendMessage(new TextComponentString("That faction doesn't exist"));
    		return false;
    	}
    	
    	if(!faction.IsPlayerInFaction(toRemove))
    	{
    		remover.sendMessage(new TextComponentString("That player is not in that faction"));
    		return false;
    	}
    	
    	boolean canRemove = IsOp(remover);
    	boolean removingSelf = false;
    	if(remover instanceof EntityPlayer)
    	{
    		UUID removerID = ((EntityPlayer)remover).getUniqueID();
    		if(removerID == toRemove) // remove self
    		{
    			canRemove = true;
    			removingSelf = true;
    		}
    		
    		if(faction.IsPlayerOutrankingOfficerOf(removerID, toRemove))
    			canRemove = true;
    	}
    	    	
    	if(!canRemove)
    	{
    		remover.sendMessage(new TextComponentString("You don't have permission to remove that player"));
    		return false;
    	}
    	
    	GameProfile userProfile = MC_SERVER.getPlayerProfileCache().getProfileByUUID(toRemove);
    	if(userProfile != null)
    	{
    		if(removingSelf)
    			faction.MessageAll(new TextComponentString(userProfile.getName() + " left " + faction.mName));
    		else
       			faction.MessageAll(new TextComponentString(userProfile.getName() + " was kicked from " + faction.mName));
    	}
    	
    	faction.RemovePlayer(toRemove);
    	
    	return true;
    }
        
    public boolean RequestInvitePlayerToMyFaction(EntityPlayer factionOfficer, UUID invitee)
    {
    	Faction myFaction = GetFactionOfPlayer(factionOfficer.getUniqueID());
    	if(myFaction != null)
    		return RequestInvitePlayerToFaction(factionOfficer, myFaction.mUUID, invitee);
    	return false;
    }
    
    public boolean RequestInvitePlayerToFaction(ICommandSender factionOfficer, UUID factionID, UUID invitee)
    {
    	Faction faction = GetFaction(factionID);
    	if(faction == null)
    	{
    		factionOfficer.sendMessage(new TextComponentString("That faction doesn't exist"));
    		return false;
    	}
    	
    	if(!IsOp(factionOfficer) && !faction.IsPlayerRoleInFaction(GetUUID(factionOfficer), Faction.Role.OFFICER))
    	{
    		factionOfficer.sendMessage(new TextComponentString("You are not an officer of this faction"));
    		return false;
    	}
    	
    	Faction existingFaction = GetFactionOfPlayer(invitee);
    	if(existingFaction != null)
    	{
    		factionOfficer.sendMessage(new TextComponentString("That player is already in a faction"));
    		return false;
    	}
    	
    	// TODO: Faction player limit - grows with claims?
    	
    	faction.InvitePlayer(invitee);
    	MC_SERVER.getPlayerList().getPlayerByUUID(invitee).sendMessage(new TextComponentString("You have received an invite to " + faction.mName + ". Type /f accept to join"));
    	
    	return true;
    }
    
    public void RequestAcceptInvite(EntityPlayer player)
    {
    	Faction inviter = GetFactionWithOpenInviteTo(player.getUniqueID());
    	if(inviter != null)
    	{
    		inviter.AddPlayer(player.getUniqueID());
    	}
    	else
    		player.sendMessage(new TextComponentString("You have no open invite to accept"));
    }
    
    public boolean RequestTransferLeadership(EntityPlayer factionLeader, UUID factionID, UUID newLeaderID)
    {
    	Faction faction = GetFaction(factionID);
    	if(faction == null)
    	{
    		factionLeader.sendMessage(new TextComponentString("That faction does not exist"));
    		return false;
    	}
    	
    	if(!IsOp(factionLeader) && !faction.IsPlayerRoleInFaction(factionLeader.getUniqueID(), Faction.Role.LEADER))
    	{
    		factionLeader.sendMessage(new TextComponentString("You are not the leader of this faction"));
    		return false;
    	}
    	
    	if(!faction.IsPlayerInFaction(newLeaderID))
    	{
    		factionLeader.sendMessage(new TextComponentString("That player is not in your faction"));
    		return false;
    	}
    	
    	faction.SetLeader(newLeaderID);
    	return true;
    }
    
    public boolean RequestDisbandFaction(EntityPlayer factionLeader, UUID factionID)
    {
    	if(!IsPlayerRoleInFaction(factionLeader.getUniqueID(), factionID, Faction.Role.LEADER))
    	{
    		factionLeader.sendMessage(new TextComponentString("You are not the leader of this faction"));
    		return false;
    	}
    	
    	Faction faction = mFactions.get(factionID);
    	faction.Disband();
    	mFactions.remove(factionID);
    	sLeaderboard.UnregisterFaction(faction);
    	
    	return true;
    }
    
    public boolean RequestStartSiege(EntityPlayer factionOfficer, DimBlockPos siegeCampPos, EnumFacing direction)
    {
    	Faction attacking = GetFactionOfPlayer(factionOfficer.getUniqueID());
    	if(attacking == null)
    	{
    		factionOfficer.sendMessage(new TextComponentString("You are not in a faction"));
    		return false;
    	}
    	
    	if(!attacking.IsPlayerRoleInFaction(factionOfficer.getUniqueID(), Faction.Role.OFFICER))
    	{
    		factionOfficer.sendMessage(new TextComponentString("You are not an officer of this faction"));
    		return false;
    	}
    	
    	// TODO: Verify there aren't existing alliances
    	
    	TileEntity siegeTE = proxy.GetTile(siegeCampPos);
    	DimChunkPos defendingChunk = siegeCampPos.ToChunkPos().Offset(direction, 1);
    	UUID defendingFactionID = mClaims.get(defendingChunk);
    	Faction defending = GetFaction(defendingFactionID);
    	if(defending == null)
    	{
    		factionOfficer.sendMessage(new TextComponentString("Could not find a target faction at that poisition"));
    		return false;
    	}
    	
    	DimBlockPos defendingPos = defending.GetSpecificPosForClaim(defendingChunk);
    	Siege siege = new Siege();
    	siege.mAttackingFaction = attacking.mUUID;
    	siege.mDefendingFaction = defendingFactionID;
    	siege.mAttackingSiegeCamps.add(siegeCampPos);
    	siege.mDefendingClaim = defendingPos;
    	//siege.mAttackSuccessThreshold
    	//siege.mSupportingClaims
    	
    	siege.Start();
    	
    	mSieges.put(defendingChunk, siege);
    	
    	// TODO: 
    	

    	
    	return true;
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
    
    public void SendSiegeInfoToNearby(DimChunkPos siegePos)
    {
    	Siege siege = mSieges.get(siegePos);
    	if(siege != null)
    	{
    		SiegeCampProgressInfo info = siege.GetSiegeInfo();
    		if(info != null)
    		{
    			PacketSiegeCampProgressUpdate packet = new PacketSiegeCampProgressUpdate();
    			packet.mInfo = info;
    			sPacketHandler.sendToAllAround(packet, siegePos.x * 16, 128d, siegePos.z * 16, SIEGE_INFO_RADIUS + 128f, siegePos.mDim);
    		}
    	}
    }
    
    // Non request 
    public void ForceAddPlayerToFaction(EntityPlayer player, UUID factionID)
    {
    	
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
				ironGenerator = new WorldGenDenseOre(denseIronOreBlock.getDefaultState(), Blocks.IRON_ORE.getDefaultState(), 
						DENSE_IRON_CELL_SIZE, DENSE_IRON_DEPOSIT_RADIUS, DENSE_IRON_OUTER_SHELL_RADIUS, DENSE_IRON_OUTER_SHELL_CHANCE,
						DENSE_IRON_MIN_INSTANCES_PER_CELL, DENSE_IRON_MAX_INSTANCES_PER_CELL, DENSE_IRON_MIN_HEIGHT, DENSE_IRON_MAX_HEIGHT);
			if(goldGenerator == null)
				goldGenerator = new WorldGenDenseOre(denseGoldOreBlock.getDefaultState(), Blocks.GOLD_ORE.getDefaultState(), 
						DENSE_GOLD_CELL_SIZE, DENSE_GOLD_DEPOSIT_RADIUS, DENSE_GOLD_OUTER_SHELL_RADIUS, DENSE_GOLD_OUTER_SHELL_CHANCE,
						DENSE_GOLD_MIN_INSTANCES_PER_CELL, DENSE_GOLD_MAX_INSTANCES_PER_CELL, DENSE_GOLD_MIN_HEIGHT, DENSE_GOLD_MAX_HEIGHT);
			if(diamondGenerator == null)
				diamondGenerator = new WorldGenBedrockOre(denseDiamondOreBlock.getDefaultState(), Blocks.DIAMOND_ORE.getDefaultState(), 
						DENSE_DIAMOND_CELL_SIZE, DENSE_DIAMOND_DEPOSIT_RADIUS, DENSE_DIAMOND_OUTER_SHELL_RADIUS, DENSE_DIAMOND_OUTER_SHELL_CHANCE,
						DENSE_DIAMOND_MIN_INSTANCES_PER_CELL, DENSE_DIAMOND_MAX_INSTANCES_PER_CELL, DENSE_DIAMOND_MIN_HEIGHT, DENSE_DIAMOND_MAX_HEIGHT);
			if(magmaGenerator == null)
				magmaGenerator = new WorldGenBedrockOre(magmaVentBlock.getDefaultState(), Blocks.LAVA.getDefaultState(), 
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
	public static int CLAIM_STRENGTH_CITADEL = 15;
	public static int CLAIM_STRENGTH_REINFORCED = 10;
	public static int CLAIM_STRENGTH_BASIC = 5;
	public static int SUPPORT_STRENGTH_CITADEL = 3;
	public static int SUPPORT_STRENGTH_REINFORCED = 2;
	public static int SUPPORT_STRENGTH_BASIC = 1;
	
	public static int ATTACK_STRENGTH_SIEGE_CAMP = 1;
	public static float LEECH_PROPORTION_SIEGE_CAMP = 0.25f;
	
	// Yields
	public static float YIELD_DAY_LENGTH = 1.0f; // In real-world hours
	public static float NUM_IRON_PER_DAY_PER_ORE = 0.05f;
	public static boolean IRON_YIELD_AS_ORE = true; // Otherwise, give ingots
	public static float NUM_GOLD_PER_DAY_PER_ORE = 0.05f;
	public static boolean GOLD_YIELD_AS_ORE = true; // Otherwise, give ingots
	public static float NUM_DIAMOND_PER_DAY_PER_ORE = 0.05f;
	public static boolean DIAMOND_YIELD_AS_ORE = false; // Otherwise, give diamonds
	
	// Sieges
	public static int SIEGE_SWING_PER_DEFENDER_DEATH = 1;
	public static int SIEGE_SWING_PER_ATTACKER_DEATH = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_BASE = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS = 1;
	public static float SIEGE_DAY_LENGTH = 24.0f; // In real-world hours
	public static float SIEGE_INFO_RADIUS = 200f;
	
	public static String[] VAULT_BLOCK_IDS = new String[] { "minecraft:gold_block" };
	public static ArrayList<Block> VAULT_BLOCKS = new ArrayList<Block>();

	public static void syncConfig()
	{
		// World Generation Settings
		DENSE_IRON_CELL_SIZE = configFile.getInt("Dense Iron - Cell Size", Configuration.CATEGORY_GENERAL, DENSE_IRON_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		DENSE_IRON_DEPOSIT_RADIUS = configFile.getInt("Dense Iron - Deposit Radius", Configuration.CATEGORY_GENERAL, DENSE_IRON_DEPOSIT_RADIUS, 1, 16, "Radius of a deposit");
		DENSE_IRON_MIN_INSTANCES_PER_CELL = configFile.getInt("Dense Iron - Min Deposits Per Cell", Configuration.CATEGORY_GENERAL, DENSE_IRON_MIN_INSTANCES_PER_CELL, 0, 256, "Minimum number of deposits per cell");
		DENSE_IRON_MAX_INSTANCES_PER_CELL = configFile.getInt("Dense Iron - Max Deposits Per Cell", Configuration.CATEGORY_GENERAL, DENSE_IRON_MAX_INSTANCES_PER_CELL, 0, 256, "Maximum number of deposits per cell");
		DENSE_IRON_MIN_HEIGHT = configFile.getInt("Dense Iron - Min Height", Configuration.CATEGORY_GENERAL, DENSE_IRON_MIN_HEIGHT, 0, 256, "Minimum height of deposits");
		DENSE_IRON_MAX_HEIGHT = configFile.getInt("Dense Iron - Max Height", Configuration.CATEGORY_GENERAL, DENSE_IRON_MAX_HEIGHT, 0, 256, "Maximum height of deposits");
		DENSE_IRON_OUTER_SHELL_RADIUS = configFile.getInt("Dense Iron - Outer Shell Radius", Configuration.CATEGORY_GENERAL, DENSE_IRON_OUTER_SHELL_RADIUS, 0, 32, "Radius in which to place vanilla ores");
		DENSE_IRON_OUTER_SHELL_CHANCE = configFile.getFloat("Dense Iron - Outer Shell Chance", Configuration.CATEGORY_GENERAL, DENSE_IRON_OUTER_SHELL_CHANCE, 0f, 1f, "Percent of blocks in outer radius that are vanilla ores");
		
		DENSE_GOLD_CELL_SIZE = configFile.getInt("Dense Gold - Cell Size", Configuration.CATEGORY_GENERAL, DENSE_GOLD_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		DENSE_GOLD_DEPOSIT_RADIUS = configFile.getInt("Dense Gold - Deposit Radius", Configuration.CATEGORY_GENERAL, DENSE_GOLD_DEPOSIT_RADIUS, 1, 16, "Radius of a deposit");
		DENSE_GOLD_MIN_INSTANCES_PER_CELL = configFile.getInt("Dense Gold - Min Deposits Per Cell", Configuration.CATEGORY_GENERAL, DENSE_GOLD_MIN_INSTANCES_PER_CELL, 0, 256, "Minimum number of deposits per cell");
		DENSE_GOLD_MAX_INSTANCES_PER_CELL = configFile.getInt("Dense Gold - Max Deposits Per Cell", Configuration.CATEGORY_GENERAL, DENSE_GOLD_MAX_INSTANCES_PER_CELL, 0, 256, "Maximum number of deposits per cell");
		DENSE_GOLD_MIN_HEIGHT = configFile.getInt("Dense Gold - Min Height", Configuration.CATEGORY_GENERAL, DENSE_GOLD_MIN_HEIGHT, 0, 256, "Minimum height of deposits");
		DENSE_GOLD_MAX_HEIGHT = configFile.getInt("Dense Gold - Max Height", Configuration.CATEGORY_GENERAL, DENSE_GOLD_MAX_HEIGHT, 0, 256, "Maximum height of deposits");
		DENSE_GOLD_OUTER_SHELL_RADIUS = configFile.getInt("Dense Gold - Outer Shell Radius", Configuration.CATEGORY_GENERAL, DENSE_GOLD_OUTER_SHELL_RADIUS, 0, 32, "Radius in which to place vanilla ores");
		DENSE_GOLD_OUTER_SHELL_CHANCE = configFile.getFloat("Dense Gold - Outer Shell Chance", Configuration.CATEGORY_GENERAL, DENSE_GOLD_OUTER_SHELL_CHANCE, 0f, 1f, "Percent of blocks in outer radius that are vanilla ores");

		DENSE_DIAMOND_CELL_SIZE = configFile.getInt("Dense Diamond - Cell Size", Configuration.CATEGORY_GENERAL, DENSE_DIAMOND_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		DENSE_DIAMOND_DEPOSIT_RADIUS = configFile.getInt("Dense Diamond - Deposit Radius", Configuration.CATEGORY_GENERAL, DENSE_DIAMOND_DEPOSIT_RADIUS, 1, 16, "Radius of a deposit");
		DENSE_DIAMOND_MIN_INSTANCES_PER_CELL = configFile.getInt("Dense Diamond - Min Deposits Per Cell", Configuration.CATEGORY_GENERAL, DENSE_DIAMOND_MIN_INSTANCES_PER_CELL, 0, 256, "Minimum number of deposits per cell");
		DENSE_DIAMOND_MAX_INSTANCES_PER_CELL = configFile.getInt("Dense Diamond - Max Deposits Per Cell", Configuration.CATEGORY_GENERAL, DENSE_DIAMOND_MAX_INSTANCES_PER_CELL, 0, 256, "Maximum number of deposits per cell");
		DENSE_DIAMOND_MIN_HEIGHT = configFile.getInt("Dense Diamond - Min Height", Configuration.CATEGORY_GENERAL, DENSE_DIAMOND_MIN_HEIGHT, 0, 256, "Minimum height of deposits");
		DENSE_DIAMOND_MAX_HEIGHT = configFile.getInt("Dense Diamond - Max Height", Configuration.CATEGORY_GENERAL, DENSE_DIAMOND_MAX_HEIGHT, 0, 256, "Maximum height of deposits");
		DENSE_DIAMOND_OUTER_SHELL_RADIUS = configFile.getInt("Dense Diamond - Outer Shell Radius", Configuration.CATEGORY_GENERAL, DENSE_DIAMOND_OUTER_SHELL_RADIUS, 0, 32, "Radius in which to place vanilla ores");
		DENSE_DIAMOND_OUTER_SHELL_CHANCE = configFile.getFloat("Dense Diamond - Outer Shell Chance", Configuration.CATEGORY_GENERAL, DENSE_DIAMOND_OUTER_SHELL_CHANCE, 0f, 1f, "Percent of blocks in outer radius that are vanilla ores");
		
		MAGMA_VENT_CELL_SIZE = configFile.getInt("Magma Vent - Cell Size", Configuration.CATEGORY_GENERAL, MAGMA_VENT_CELL_SIZE, 8, 4096, "Divide the world into cells of this size and generate 1 or more deposits per cell");
		MAGMA_VENT_DEPOSIT_RADIUS = configFile.getInt("Magma Vent - Deposit Radius", Configuration.CATEGORY_GENERAL, MAGMA_VENT_DEPOSIT_RADIUS, 1, 16, "Radius of a deposit");
		MAGMA_VENT_MIN_INSTANCES_PER_CELL = configFile.getInt("Magma Vent - Min Deposits Per Cell", Configuration.CATEGORY_GENERAL, MAGMA_VENT_MIN_INSTANCES_PER_CELL, 0, 256, "Minimum number of deposits per cell");
		MAGMA_VENT_MAX_INSTANCES_PER_CELL = configFile.getInt("Magma Vent - Max Deposits Per Cell", Configuration.CATEGORY_GENERAL, MAGMA_VENT_MAX_INSTANCES_PER_CELL, 0, 256, "Maximum number of deposits per cell");
		MAGMA_VENT_MIN_HEIGHT = configFile.getInt("Magma Vent - Min Height", Configuration.CATEGORY_GENERAL, MAGMA_VENT_MIN_HEIGHT, 0, 256, "Minimum height of deposits");
		MAGMA_VENT_MAX_HEIGHT = configFile.getInt("Magma Vent - Max Height", Configuration.CATEGORY_GENERAL, MAGMA_VENT_MAX_HEIGHT, 0, 256, "Maximum height of deposits");
		MAGMA_VENT_OUTER_SHELL_RADIUS = configFile.getInt("Magma Vent - Outer Shell Radius", Configuration.CATEGORY_GENERAL, MAGMA_VENT_OUTER_SHELL_RADIUS, 0, 32, "Radius in which to place vanilla ores");
		MAGMA_VENT_OUTER_SHELL_CHANCE = configFile.getFloat("Magma Vent - Outer Shell Chance", Configuration.CATEGORY_GENERAL, MAGMA_VENT_OUTER_SHELL_CHANCE, 0f, 1f, "Percent of blocks in outer radius that are vanilla ores");

		// Claim Settings
		CLAIM_STRENGTH_CITADEL = configFile.getInt("Citadel Claim Strength", Configuration.CATEGORY_GENERAL, CLAIM_STRENGTH_CITADEL, 1, 1024, "The strength of citadel claims");
		CLAIM_STRENGTH_REINFORCED = configFile.getInt("Reinforced Claim Strength", Configuration.CATEGORY_GENERAL, CLAIM_STRENGTH_REINFORCED, 1, 1024, "The strength of reinforced claims");
		CLAIM_STRENGTH_BASIC = configFile.getInt("Basic Claim Strength", Configuration.CATEGORY_GENERAL, CLAIM_STRENGTH_BASIC, 1, 1024, "The strength of basic claims");
		SUPPORT_STRENGTH_CITADEL = configFile.getInt("Citadel Support Strength", Configuration.CATEGORY_GENERAL, SUPPORT_STRENGTH_CITADEL, 1, 1024, "The support strength a citadel gives to adjacent claims");
		SUPPORT_STRENGTH_REINFORCED = configFile.getInt("Reinforced Support Strength", Configuration.CATEGORY_GENERAL, SUPPORT_STRENGTH_REINFORCED, 1, 1024, "The support strength a reinforced claim gives to adjacent claims");
		SUPPORT_STRENGTH_BASIC = configFile.getInt("Basic Support Strength", Configuration.CATEGORY_GENERAL, SUPPORT_STRENGTH_BASIC, 1, 1024, "The support strength a basic claim gives to adjacent claims");
		
		// Siege Camp Settings
		ATTACK_STRENGTH_SIEGE_CAMP = configFile.getInt("Siege Camp Attack Strength", Configuration.CATEGORY_GENERAL, ATTACK_STRENGTH_SIEGE_CAMP, 1, 1024, "How much attack pressure a siege camp exerts on adjacent enemy claims");
		LEECH_PROPORTION_SIEGE_CAMP = configFile.getFloat("Siege Camp Leech Proportion", Configuration.CATEGORY_GENERAL, LEECH_PROPORTION_SIEGE_CAMP, 0f, 1f, "What proportion of a claim's yields are leeched when a siege camp is set to leech mode");

		// Siege swing parameters
		SIEGE_SWING_PER_DEFENDER_DEATH = configFile.getInt("Siege Swing Per Defender Death", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_DEFENDER_DEATH, 1, 1024, "How much a siege progress swings when a defender dies in the siege");
		SIEGE_SWING_PER_ATTACKER_DEATH = configFile.getInt("Siege Swing Per Attacker Death", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_ATTACKER_DEATH, 1, 1024, "How much a siege progress swings when an attacker dies in the siege");
		SIEGE_SWING_PER_DAY_ELAPSED_BASE = configFile.getInt("Siege Swing Per Day", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_DAY_ELAPSED_BASE, 1, 1024, "How much a siege progress swings each day (see below). This happens regardless of logins");
		SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS = configFile.getInt("Siege Swing Per Day Without Attacker Logins", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS, 1, 1024, "How much a siege progress swings when no attackers have logged on for a day (see below)");
		SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS = configFile.getInt("Siege Swing Per Day Without Defender Logins", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS, 1, 1024, "How much a siege progress swings when no defenders have logged on for a day (see below)");
		SIEGE_DAY_LENGTH = configFile.getFloat("Siege Day Length", Configuration.CATEGORY_GENERAL, SIEGE_DAY_LENGTH, 0.0001f, 100000f, "The length of a day for siege login purposes, in real-world hours.");
		SIEGE_INFO_RADIUS = configFile.getFloat("Siege Info Radius", Configuration.CATEGORY_GENERAL, SIEGE_INFO_RADIUS, 1f, 1000f, "The range at which you see siege information. (Capped by the server setting)");
		
		// Vault parameters
		VAULT_BLOCK_IDS = configFile.getStringList("Valuable Blocks", Configuration.CATEGORY_GENERAL, VAULT_BLOCK_IDS, "The block IDs that count towards the value of your citadel's vault");
		
		// Yield paramters
		NUM_IRON_PER_DAY_PER_ORE = configFile.getFloat("#Iron Per Day Per Ore", Configuration.CATEGORY_GENERAL, NUM_IRON_PER_DAY_PER_ORE, 0.001f, 1000f, "For each dense iron ore block in a claim, how many resources do players get per yield timer");
		NUM_GOLD_PER_DAY_PER_ORE = configFile.getFloat("#Gold Per Day Per Ore", Configuration.CATEGORY_GENERAL, NUM_GOLD_PER_DAY_PER_ORE, 0.001f, 1000f, "For each dense gold ore block in a claim, how many resources do players get per yield timer");
		NUM_DIAMOND_PER_DAY_PER_ORE = configFile.getFloat("#Diamond Per Day Per Ore", Configuration.CATEGORY_GENERAL, NUM_DIAMOND_PER_DAY_PER_ORE, 0.001f, 1000f, "For each dense diamond ore block in a claim, how many resources do players get per yield timer");
		IRON_YIELD_AS_ORE = configFile.getBoolean("Iron Yield As Ore", Configuration.CATEGORY_GENERAL, IRON_YIELD_AS_ORE, "If true, dense iron ore gives ore blocks. If false, it gives ingots");
		GOLD_YIELD_AS_ORE = configFile.getBoolean("Gold Yield As Ore", Configuration.CATEGORY_GENERAL, GOLD_YIELD_AS_ORE, "If true, dense gold ore gives ore blocks. If false, it gives ingots");
		DIAMOND_YIELD_AS_ORE = configFile.getBoolean("Diamond Yield As Ore", Configuration.CATEGORY_GENERAL, DIAMOND_YIELD_AS_ORE, "If true, dense diamond ore gives ore blocks. If false, it gives diamonds");
		YIELD_DAY_LENGTH = configFile.getFloat("Yield Day Length", Configuration.CATEGORY_GENERAL, YIELD_DAY_LENGTH, 0.0001f, 100000f, "The length of time between yields, in real-world hours.");
				
		
		if(configFile.hasChanged())
			configFile.save();
	}
	
	private void ReadFromNBT(NBTTagCompound tags)
	{
		NBTTagList list = tags.getTagList("factions", 10); // Compound Tag
		
		mFactions.clear();
		mClaims.clear();
		
		for(NBTBase baseTag : list)
		{
			NBTTagCompound factionTags = ((NBTTagCompound)baseTag);
			UUID uuid = factionTags.getUniqueId("id");
			Faction faction = new Faction();
			faction.mUUID = uuid;
			faction.ReadFromNBT(factionTags);
			mFactions.put(uuid, faction);
			sLeaderboard.RegisterFaction(faction);
			
			// Also populate the DimChunkPos lookup table
			for(DimBlockPos blockPos : faction.mClaims.keySet())
			{
				mClaims.put(blockPos.ToChunkPos(), uuid);
			}
		}
		
		timestampOfFirstDay = tags.getLong("zero-timestamp");
		numberOfSiegeDaysTicked = tags.getLong("num-days-elapsed");
		numberOfYieldDaysTicked = tags.getLong("num-yields-awarded");
	}
	
	private void WriteToNBT(NBTTagCompound tags)
	{
		NBTTagList factionList = new NBTTagList();
		
		for(HashMap.Entry<UUID, Faction> kvp : mFactions.entrySet())
		{
			NBTTagCompound factionTags = new NBTTagCompound();
			factionTags.setUniqueId("id", kvp.getKey());
			kvp.getValue().WriteToNBT(factionTags);
			factionList.appendTag(factionTags);
		}
		
		tags.setTag("factions", factionList);
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
			sLogger.info("Successfully loaded warforgefactions.dat");
		}
		catch(Exception e)
		{
			sLogger.error("Failed to load warforgefactions.dat");
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
				sLogger.info("Successfully saved warforgefactions.dat");
			}
		}
		catch(Exception e)
		{
			sLogger.error("Failed to save warforgefactions.dat");
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
