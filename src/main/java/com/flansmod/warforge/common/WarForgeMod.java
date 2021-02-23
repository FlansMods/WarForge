package com.flansmod.warforge.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.BlockEvent;
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
import com.flansmod.warforge.common.blocks.TileEntitySiegeCamp;
import com.flansmod.warforge.common.network.PacketHandler;
import com.flansmod.warforge.common.world.WorldGenBedrockOre;
import com.flansmod.warforge.common.world.WorldGenDenseOre;
import com.flansmod.warforge.server.CommandFactions;
import com.flansmod.warforge.server.Faction;
import com.flansmod.warforge.server.ServerTickHandler;
import com.flansmod.warforge.server.Siege;
import com.mojang.authlib.GameProfile;
import com.flansmod.warforge.server.Faction.Role;

@Mod(modid = WarForgeMod.MODID, name = WarForgeMod.NAME, version = WarForgeMod.VERSION)
public class WarForgeMod
{
    public static final String MODID = "warforge";
    public static final String NAME = "WarForge Factions";
    public static final String VERSION = "1.0";

    public static Logger logger;
    
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
	
	public static final PacketHandler packetHandler = new PacketHandler();

	public static Block citadelBlock, basicClaimBlock, reinforcedClaimBlock, siegeCampBlock;
	public static Item citadelBlockItem, basicClaimBlockItem, reinforcedClaimBlockItem, siegeCampBlockItem;
	
	public static Block denseIronOreBlock, denseGoldOreBlock, denseDiamondOreBlock, magmaVentBlock;
	public static Item denseIronOreItem, denseGoldOreItem, denseDiamondOreItem, magmaVentItem;
	
	public static MinecraftServer MC_SERVER = null;
	public static Random rand = new Random();
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
		//Load config
		configFile = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();
		
        
        citadelBlock = new BlockCitadel(Material.ROCK).setRegistryName("citadelblock").setUnlocalizedName("citadelblock");
        citadelBlockItem = new ItemBlock(citadelBlock).setRegistryName("citadelblock").setUnlocalizedName("citadelblock");
        GameRegistry.registerTileEntity(TileEntityCitadel.class, new ResourceLocation(MODID, "citadel"));
        
        // Basic and reinforced claims, they share a tile entity
        basicClaimBlock = new BlockBasicClaim(Material.ROCK, CLAIM_STRENGTH_BASIC).setRegistryName("basicclaimblock").setUnlocalizedName("basicclaimblock");
        basicClaimBlockItem = new ItemBlock(basicClaimBlock).setRegistryName("basicclaimblock").setUnlocalizedName("basicclaimblock");
        reinforcedClaimBlock = new BlockBasicClaim(Material.ROCK, CLAIM_STRENGTH_REINFORCED).setRegistryName("reinforcedclaimblock").setUnlocalizedName("reinforcedclaimblock");
        reinforcedClaimBlockItem = new ItemBlock(reinforcedClaimBlock).setRegistryName("reinforcedclaimblock").setUnlocalizedName("reinforcedclaimblock");
        GameRegistry.registerTileEntity(TileEntityBasicClaim.class, new ResourceLocation(MODID, "basicclaim"));
        
        // Siege camp
        siegeCampBlock = new BlockSiegeCamp(Material.ROCK).setRegistryName("siegecampblock").setUnlocalizedName("siegecampblock");
        siegeCampBlockItem = new ItemBlock(siegeCampBlock).setRegistryName("siegecampblock").setUnlocalizedName("siegecampblock");
        GameRegistry.registerTileEntity(TileEntitySiegeCamp.class, new ResourceLocation(MODID, "siegecamp"));
        
        denseIronOreBlock = new BlockYieldProvider(Material.ROCK, new ItemStack(Items.IRON_INGOT), 1.0f).setRegistryName("denseironore").setUnlocalizedName("denseironore");
        denseGoldOreBlock = new BlockYieldProvider(Material.ROCK, new ItemStack(Items.GOLD_INGOT), 1.0f).setRegistryName("densegoldore").setUnlocalizedName("densegoldore");
        denseDiamondOreBlock = new BlockYieldProvider(Material.ROCK, new ItemStack(Items.DIAMOND), 2.0f).setRegistryName("densediamondore").setUnlocalizedName("densediamondore");
        magmaVentBlock = new BlockYieldProvider(Material.ROCK, new ItemStack(Items.IRON_INGOT), 1.0f).setRegistryName("magmavent").setUnlocalizedName("magmavent");
        
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
		packetHandler.initialise();
		proxy.Init(event);
    }
    
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		packetHandler.postInitialise();
		proxy.PostInit(event);
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
		logger.info("Registered items");
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
		logger.info("Registered blocks");
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
    	
    	logger.error("Could not find a faction with UUID " + factionID);
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
    
    // This is called for any non-citadel claim. Citadels can be factionless, so this makes no sense
	public void OnNonCitadelClaimPlaced(IClaim claim, EntityLivingBase placer) 
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
			logger.error("Invalid placer placed a claim at " + claim.GetPos());
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
    
    public void Update()
    {
    	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
    	{
    		entry.getValue().Update();
    	}
    }
    
    @SubscribeEvent
    public void OnBlockPlaced(RightClickBlock event)
    {
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
    	
    	mFactions.put(proposedID, faction);
    	citadel.OnServerSetFaction(faction);
    	
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
    	
    	return true;
    }
    
    public boolean RequestStartSiege(EntityPlayer factionOfficer, UUID factionID, DimBlockPos targetPos, DimBlockPos siegePos)
    {
    	Faction faction = GetFaction(factionID);
    	if(faction == null)
    	{
    		factionOfficer.sendMessage(new TextComponentString("The faction could not be found"));
    		return false;
    	}
    	
    	if(!faction.IsPlayerRoleInFaction(factionOfficer.getUniqueID(), Faction.Role.OFFICER))
    	{
    		factionOfficer.sendMessage(new TextComponentString("You are not an officer of this faction"));
    		return false;
    	}
    	
    	// TODO: Verify there aren't existing alliances
    	
    	TileEntity targetTE = proxy.GetTile(targetPos);
    	TileEntity siegeTE = proxy.GetTile(siegePos);
    	
    	// TODO: 
    	

    	
    	return true;
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
	
	public static int CLAIM_STRENGTH_CITADEL = 15;
	public static int CLAIM_STRENGTH_REINFORCED = 10;
	public static int CLAIM_STRENGTH_BASIC = 5;
	
	public static int ATTACK_STRENGTH_SIEGE_CAMP = 1;
	public static float LEECH_PROPORTION_SIEGE_CAMP = 0.25f;
	
	public static int SIEGE_SWING_PER_DEFENDER_DEATH = 1;
	public static int SIEGE_SWING_PER_ATTACKER_DEATH = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS = 1;
	public static int SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS = 1;
	public static float SIEGE_DAY_LENGTH = 24.0f; // In real-world hours

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
		
		// Siege Camp Settings
		ATTACK_STRENGTH_SIEGE_CAMP = configFile.getInt("Siege Camp Attack Strength", Configuration.CATEGORY_GENERAL, ATTACK_STRENGTH_SIEGE_CAMP, 1, 1024, "How much attack pressure a siege camp exerts on adjacent enemy claims");
		LEECH_PROPORTION_SIEGE_CAMP = configFile.getFloat("Siege Camp Leech Proportion", Configuration.CATEGORY_GENERAL, LEECH_PROPORTION_SIEGE_CAMP, 0f, 1f, "What proportion of a claim's yields are leeched when a siege camp is set to leech mode");

		// Siege swing parameters
		SIEGE_SWING_PER_DEFENDER_DEATH = configFile.getInt("Siege Swing Per Defender Death", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_DEFENDER_DEATH, 1, 1024, "How much a siege progress swings when a defender dies in the siege");
		SIEGE_SWING_PER_ATTACKER_DEATH = configFile.getInt("Siege Swing Per Attacker Death", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_ATTACKER_DEATH, 1, 1024, "How much a siege progress swings when an attacker dies in the siege");
		SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS = configFile.getInt("Siege Swing Per Day Without Attacker Logins", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS, 1, 1024, "How much a siege progress swings when no attackers have logged on for a day (see below)");
		SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS = configFile.getInt("Siege Swing Per Day Without Defender Logins", Configuration.CATEGORY_GENERAL, SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS, 1, 1024, "How much a siege progress swings when no defenders have logged on for a day (see below)");
		SIEGE_DAY_LENGTH = configFile.getFloat("Siege Day Length", Configuration.CATEGORY_GENERAL, SIEGE_DAY_LENGTH, 0.1f, 100000f, "The length of a day for siege login purposes, in real-world hours.");

		
		if(configFile.hasChanged())
			configFile.save();
	}
	
	@EventHandler
	public void ServerAboutToStart(FMLServerAboutToStartEvent event)
	{
		MC_SERVER = event.getServer();
		CommandHandler handler = ((CommandHandler)MC_SERVER.getCommandManager());
		handler.registerCommand(new CommandFactions());
	}
	
	@EventHandler
	public void ServerStopped(FMLServerStoppingEvent event)
	{
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
