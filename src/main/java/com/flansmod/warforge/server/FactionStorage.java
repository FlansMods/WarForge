package com.flansmod.warforge.server;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.DimChunkPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.IClaim;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.common.network.PacketSiegeCampProgressUpdate;
import com.flansmod.warforge.common.network.SiegeCampProgressInfo;
import com.mojang.authlib.GameProfile;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;

public class FactionStorage 
{
    private HashMap<UUID, Faction> mFactions = new HashMap<UUID, Faction>();
    // This map contains every single claim, including siege camps.
    // So if you take one of these and try to look it up in the faction, check their active sieges too
    private HashMap<DimChunkPos, UUID> mClaims = new HashMap<DimChunkPos, UUID>();
    
    // This is all the currently active sieges, keyed by the defending position
    private HashMap<DimChunkPos, Siege> mSieges = new HashMap<DimChunkPos, Siege>();
    
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
    	
    	WarForgeMod.LOGGER.error("Could not find a faction with UUID " + factionID);
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
				WarForgeMod.LOGGER.error("Invalid placer placed a claim at " + claim.GetPos());
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
    
    public void Update()
    {
    	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
    	{
    		entry.getValue().Update();
    	}
    }
    
    public void AdvanceSiegeDay()
    {
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
    
    public void AdvanceYieldDay()
    {
    	for(HashMap.Entry<UUID, Faction> entry : mFactions.entrySet())
    	{
    		entry.getValue().AwardYields();
    	}
    }
    
    public void PlayerDied(EntityPlayerMP player)
    {
		Faction faction = GetFactionOfPlayer(player.getUniqueID());
    	
		if(faction != null)
		{
	    	for(HashMap.Entry<DimChunkPos, Siege> kvp : mSieges.entrySet())
			{
				// If the player is on the attackers side, send the event
				if(kvp.getValue().mAttackingFaction.equals(faction.mUUID))
				{
					kvp.getValue().OnAttackerDied(player);
				}
				// If the player is on the defenders side, send the event
				if(kvp.getValue().mDefendingFaction.equals(faction.mUUID))
				{
					kvp.getValue().OnDefenderDied(player);
				}
			}
	    	
	    	CheckForCompleteSieges();
		}
    }
    
    public void CheckForCompleteSieges()
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
			
			Faction attackers = GetFaction(siege.mAttackingFaction);
			Faction defenders = GetFaction(siege.mDefendingFaction);
			
			if(attackers == null || defenders == null)
			{
				WarForgeMod.LOGGER.error("Invalid factions in completed siege. Nothing will happen.");
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
		faction.mColour = Color.HSBtoRGB(WarForgeMod.rand.nextFloat(), WarForgeMod.rand.nextFloat() * 0.5f + 0.5f, 1.0f);
    	faction.mNotoriety = 0;
    	faction.mLegacy = 0;
    	faction.mWealth = 0;
    	
    	mFactions.put(proposedID, faction);
    	citadel.OnServerSetFaction(faction);
    	mClaims.put(citadel.GetPos().ToChunkPos(), proposedID);
    	WarForgeMod.LEADERBOARD.RegisterFaction(faction);
    	
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
    	
    	boolean canRemove = WarForgeMod.IsOp(remover);
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
    	
    	GameProfile userProfile = WarForgeMod.MC_SERVER.getPlayerProfileCache().getProfileByUUID(toRemove);
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
    	
    	if(!WarForgeMod.IsOp(factionOfficer) && !faction.IsPlayerRoleInFaction(WarForgeMod.GetUUID(factionOfficer), Faction.Role.OFFICER))
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
    	WarForgeMod.MC_SERVER.getPlayerList().getPlayerByUUID(invitee).sendMessage(new TextComponentString("You have received an invite to " + faction.mName + ". Type /f accept to join"));
    	
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
    	
    	if(!WarForgeMod.IsOp(factionLeader) && !faction.IsPlayerRoleInFaction(factionLeader.getUniqueID(), Faction.Role.LEADER))
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
    	WarForgeMod.LEADERBOARD.UnregisterFaction(faction);
    	
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
    	
    	TileEntity siegeTE = WarForgeMod.proxy.GetTile(siegeCampPos);
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
    			WarForgeMod.NETWORK.sendToAllAround(packet, siegePos.x * 16, 128d, siegePos.z * 16, WarForgeMod.SIEGE_INFO_RADIUS + 128f, siegePos.mDim);
    		}
    	}
    }
    
    public void ReadFromNBT(NBTTagCompound tags)
	{
    	mFactions.clear();
		mClaims.clear();
		
		NBTTagList list = tags.getTagList("factions", 10); // Compound Tag
		
		for(NBTBase baseTag : list)
		{
			NBTTagCompound factionTags = ((NBTTagCompound)baseTag);
			UUID uuid = factionTags.getUniqueId("id");
			Faction faction = new Faction();
			faction.mUUID = uuid;
			faction.ReadFromNBT(factionTags);
			mFactions.put(uuid, faction);
			WarForgeMod.LEADERBOARD.RegisterFaction(faction);
			
			// Also populate the DimChunkPos lookup table
			for(DimBlockPos blockPos : faction.mClaims.keySet())
			{
				mClaims.put(blockPos.ToChunkPos(), uuid);
			}
		}
	}
    
	public void WriteToNBT(NBTTagCompound tags)
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
	}
}