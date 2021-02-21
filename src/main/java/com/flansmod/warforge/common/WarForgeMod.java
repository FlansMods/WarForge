package com.flansmod.warforge.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.flansmod.warforge.common.blocks.BlockCitadel;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.common.network.PacketHandler;
import com.flansmod.warforge.server.Faction;
import com.flansmod.warforge.server.ServerTickHandler;

@Mod(modid = WarForgeMod.MODID, name = WarForgeMod.NAME, version = WarForgeMod.VERSION)
public class WarForgeMod
{
    public static final String MODID = "warforge";
    public static final String NAME = "WarForge Factions";
    public static final String VERSION = "1.0";

    public static Logger logger;
    
    private HashMap<UUID, Faction> mFactions = new HashMap<UUID, Faction>();
    private HashMap<DimChunkPos, UUID> mClaims = new HashMap<DimChunkPos, UUID>();
    
	@Instance(MODID)
	public static WarForgeMod INSTANCE;
	@SidedProxy(clientSide = "com.flansmod.warforge.client.ClientProxy", serverSide = "com.flansmod.warforge.common.CommonProxy")
	public static CommonProxy proxy;
	
	public static final PacketHandler packetHandler = new PacketHandler();

	public static Block citadelBlock;
	public static Item citadelBlockItem;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        
        citadelBlock = new BlockCitadel(Material.ROCK).setRegistryName("citadelBlock").setUnlocalizedName("citadelBlock");
        citadelBlockItem = new ItemBlock(citadelBlock).setRegistryName("citadelBlock").setUnlocalizedName("citadelBlock");
        
		GameRegistry.registerTileEntity(TileEntityCitadel.class, new ResourceLocation(MODID, "citadel"));
        
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
		logger.info("Registered items");
	}
	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().register(citadelBlock);
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
    	faction.mNotoriety = 0;
    	
    	mFactions.put(proposedID, faction);
    	citadel.SetFaction(proposedID);
    	
    	return true;
    }
    
    public boolean RequestRemovePlayerFromFaction(EntityPlayer remover, UUID factionID, UUID toRemove)
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
    	
    	boolean canRemove = remover.getUniqueID() == toRemove // remove self
    					|| faction.IsPlayerOutrankingOfficerOf(remover.getUniqueID(), toRemove) // remover is an officer and of higher rank
    					|| IsOp(remover);
    	
    	if(!canRemove)
    	{
    		remover.sendMessage(new TextComponentString("You don't have permission to remove that player"));
    		return false;
    	}
    	
    	faction.RemovePlayer(toRemove);
    	
    	return true;
    }
        
    public boolean RequestInvitePlayerToMyFaction(EntityPlayer factionOfficer, UUID invitee)
    {
    	Faction myFaction = GetFaction(factionOfficer.getUniqueID());
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
    	FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(invitee).sendMessage(new TextComponentString("You have received an invite to " + faction.mName + ". Type /f accept to join"));
    	
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
    	if(!IsPlayerRoleInFaction(factionLeader.getUniqueID(), factionID, Faction.Role.OFFICER))
    	{
    		factionLeader.sendMessage(new TextComponentString("You are not the leader of this faction"));
    		return false;
    	}
    	
    	return true;
    }
    
    // Non request 
    public void ForceAddPlayerToFaction(EntityPlayer player, UUID factionID)
    {
    	
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
    		return FMLServerHandler.instance().getServer().getPlayerList().canSendCommands(((EntityPlayer)sender).getGameProfile());
    	return sender instanceof MinecraftServer;
    }
}
