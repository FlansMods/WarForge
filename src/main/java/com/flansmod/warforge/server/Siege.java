package com.flansmod.warforge.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.DimChunkPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.IClaim;
import com.flansmod.warforge.common.blocks.TileEntitySiegeCamp;
import com.flansmod.warforge.common.network.SiegeCampProgressInfo;
import com.flansmod.warforge.server.Faction.PlayerData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;

public class Siege 
{
	public UUID mAttackingFaction;
	public UUID mDefendingFaction;
	public ArrayList<DimBlockPos> mAttackingSiegeCamps;
	public DimBlockPos mDefendingClaim;
	
	/**
	 * The base progress comes from passive sources and must be recalculated whenever checking progress.
	 * Sources for the attackers are:
	 * 		- Additional siege camps
	 * Sources of the defenders are:
	 * 		- Adjacent claims with differing support strengths
	 *  */
	private int mBaseProgress = 0;
	/**
	 * The attack progress is accumulated over time based on active actions in the area of the siege
	 * Sources for the attackers are:
	 * 		- Defender deaths in or around the siege
	 * 		- Elapsed days with no defender logins
	 * 		- Elapsed days (there is a constant pressure from the attacker that will eventually wear down the defenders unless they push back)
	 * Sources for the defenders are:
	 * 		- Attacker deaths in or around the siege
	 * 		- Elapsed days with no attacker logins
	 */
	private int mAttackProgress = 0;
	
	
	
	// This is defined by the chunk we are attacking and what type it is
	public int mAttackSuccessThreshold = 5;
	
	// Attack progress starts at 0 and can be moved to -5 or mAttackSuccessThreshold
	public int GetAttackProgress() { return mBaseProgress + mAttackProgress; }
	public int GetDefenceProgress() { return -mBaseProgress -mAttackProgress; }
	
	public boolean IsCompleted()
	{
		return GetAttackProgress() >= mAttackSuccessThreshold || GetDefenceProgress() >= 5;
	}
	
	public boolean WasSuccessful()
	{
		return GetAttackProgress() >= mAttackSuccessThreshold;
	}
	
	public Siege()
	{
		mAttackingSiegeCamps = new ArrayList<DimBlockPos>(4);
	}
	
	public Siege(UUID attacker, UUID defender, DimBlockPos defending)
	{
		mAttackingFaction = attacker;
		mDefendingFaction = defender;
		mDefendingClaim = defending;
	}
	
	public SiegeCampProgressInfo GetSiegeInfo()
	{
		Faction attackers = WarForgeMod.INSTANCE.GetFaction(mAttackingFaction);
		Faction defenders = WarForgeMod.INSTANCE.GetFaction(mDefendingFaction);
		
		if(attackers == null || defenders == null)
		{
			WarForgeMod.logger.error("Invalid factions in siege. Can't display info");
			return null;
		}
		
		SiegeCampProgressInfo info = new SiegeCampProgressInfo();
		info.mAttackingPos = mAttackingSiegeCamps.get(0);
		info.mAttackingName = attackers.mName;
		info.mAttackingColour = attackers.mColour;
		info.mDefendingPos = mDefendingClaim;
		info.mDefendingName = defenders.mName;
		info.mDefendingColour = defenders.mColour;
		info.mProgress = GetAttackProgress();
		info.mCompletionPoint = mAttackSuccessThreshold;
		
		return info;
	}
	
	public boolean Start() 
	{
		Faction attackers = WarForgeMod.INSTANCE.GetFaction(mAttackingFaction);
		Faction defenders = WarForgeMod.INSTANCE.GetFaction(mDefendingFaction);
		
		if(attackers == null || defenders == null)
		{
			WarForgeMod.logger.error("Invalid factions in siege. Cannot start");
			return false;
		}
		
		CalculateBasePower();
		WarForgeMod.INSTANCE.MessageAll(new TextComponentString(attackers.mName + " started a siege against " + defenders.mName + " at " + mDefendingClaim.ToFancyString()), true);
		WarForgeMod.INSTANCE.SendSiegeInfoToNearby(mDefendingClaim.ToChunkPos());
		return true;
	}
	
	public void AdvanceDay()
	{
		Faction attackers = WarForgeMod.INSTANCE.GetFaction(mAttackingFaction);
		Faction defenders = WarForgeMod.INSTANCE.GetFaction(mDefendingFaction);
		
		if(attackers == null || defenders == null)
		{
			WarForgeMod.logger.error("Invalid factions in siege.");
			return;
		}
		
		CalculateBasePower();
		float totalSwing = 0.0f;
		totalSwing += WarForgeMod.SIEGE_SWING_PER_DAY_ELAPSED_BASE;
		if(!defenders.mHasHadAnyLoginsToday)
			totalSwing += WarForgeMod.SIEGE_SWING_PER_DAY_ELAPSED_NO_DEFENDER_LOGINS;
		if(!attackers.mHasHadAnyLoginsToday)
			totalSwing -= WarForgeMod.SIEGE_SWING_PER_DAY_ELAPSED_NO_ATTACKER_LOGINS;
		mAttackProgress += totalSwing;
		
		if(totalSwing > 0)
		{
			attackers.MessageAll(new TextComponentString("Your siege on " + defenders.mName + " at " + mDefendingClaim.ToFancyString() + " shifted " + totalSwing + " points in your favour. The progress is now at " + GetAttackProgress() + "/" + mAttackSuccessThreshold));
			defenders.MessageAll(new TextComponentString("The siege on " + mDefendingClaim.ToFancyString() + " by " + attackers.mName + " shifted " + totalSwing + " points in their favour. The progress is now at " + GetAttackProgress() + "/" + mAttackSuccessThreshold));
		}
		else if(totalSwing < 0)
		{
			defenders.MessageAll(new TextComponentString("The siege on " + mDefendingClaim.ToFancyString() + " by " + attackers.mName + " shifted " + -totalSwing + " points in your favour. The progress is now at " + GetAttackProgress() + "/" + mAttackSuccessThreshold));
			attackers.MessageAll(new TextComponentString("Your siege on " + defenders.mName + " at " + mDefendingClaim.ToFancyString() + " shifted " + -totalSwing + " points in their favour. The progress is now at " + GetAttackProgress() + "/" + mAttackSuccessThreshold));
		}
		else
		{
			defenders.MessageAll(new TextComponentString("The siege on " + mDefendingClaim.ToFancyString() + " by " + attackers.mName + " did not shift today. The progress is at " + GetAttackProgress() + "/" + mAttackSuccessThreshold));
			attackers.MessageAll(new TextComponentString("Your siege on " + defenders.mName + " at " + mDefendingClaim.ToFancyString() + " did not shift today. The progress is at " + GetAttackProgress() + "/" + mAttackSuccessThreshold));
		}
		
		WarForgeMod.INSTANCE.SendSiegeInfoToNearby(mDefendingClaim.ToChunkPos());
	}
	
	private void CalculateBasePower()
	{
		Faction attackers = WarForgeMod.INSTANCE.GetFaction(mAttackingFaction);
		Faction defenders = WarForgeMod.INSTANCE.GetFaction(mDefendingFaction);
		
		if(attackers == null || defenders == null || WarForgeMod.MC_SERVER == null)
		{
			WarForgeMod.logger.error("Invalid factions in siege.");
			return;
		}
		
		mBaseProgress = 0;
		DimChunkPos defendingChunk = mDefendingClaim.ToChunkPos();
		for(EnumFacing direction : EnumFacing.HORIZONTALS)
		{
			DimChunkPos checkChunk = defendingChunk.Offset(direction, 1);
			UUID factionInChunk = WarForgeMod.INSTANCE.GetClaim(checkChunk);
			// Sum up all additional attack claims
			if(factionInChunk.equals(mAttackingFaction))
			{
				DimBlockPos claimBlockPos = attackers.GetSpecificPosForClaim(checkChunk);
				if(claimBlockPos != null)
				{
					TileEntity te = WarForgeMod.MC_SERVER.getWorld(claimBlockPos.mDim).getTileEntity(claimBlockPos);
					if(te instanceof IClaim)
					{
						mBaseProgress += ((IClaim) te).GetAttackStrength();
					}
				}
			}
			// Sum up all defending support claims
			if(factionInChunk.equals(mDefendingFaction))
			{
				DimBlockPos claimBlockPos = defenders.GetSpecificPosForClaim(checkChunk);
				if(claimBlockPos != null)
				{
					TileEntity te = WarForgeMod.MC_SERVER.getWorld(claimBlockPos.mDim).getTileEntity(claimBlockPos);
					if(te instanceof IClaim)
					{
						mBaseProgress -= ((IClaim) te).GetSupportStrength();
					}
				}
			}
		}
	}
	
	public void OnCancelled()
	{
		
	}
	
	public void OnCompleted()
	{
		
	}
	
	// These events will fire from all over. Do the in-range checks in here
	public void OnAttackerDied(EntityPlayerMP attacker)
	{
		ChunkPos chunkPos = new ChunkPos(attacker.getPosition());
		int taxicabDistance = Math.abs(chunkPos.x - mDefendingClaim.ToChunkPos().x) 
							+ Math.abs(chunkPos.z - mDefendingClaim.ToChunkPos().z);
		
		if(taxicabDistance <= 2)
		{
			mAttackProgress -= WarForgeMod.SIEGE_SWING_PER_ATTACKER_DEATH;
			attacker.sendMessage(new TextComponentString("Your death has shifted the siege progress by " + WarForgeMod.SIEGE_SWING_PER_ATTACKER_DEATH));
			
			WarForgeMod.INSTANCE.SendSiegeInfoToNearby(mDefendingClaim.ToChunkPos());
		}
	}
	// These events will fire from all over. Do the in-range checks in here
	public void OnDefenderDied(EntityPlayerMP defender)
	{
		ChunkPos chunkPos = new ChunkPos(defender.getPosition());
		int taxicabDistance = Math.abs(chunkPos.x - mDefendingClaim.ToChunkPos().x) 
							+ Math.abs(chunkPos.z - mDefendingClaim.ToChunkPos().z);
		
		if(taxicabDistance <= 2)
		{
			mAttackProgress += WarForgeMod.SIEGE_SWING_PER_DEFENDER_DEATH;
			defender.sendMessage(new TextComponentString("Your death has shifted the siege progress by " + WarForgeMod.SIEGE_SWING_PER_DEFENDER_DEATH));
			
			WarForgeMod.INSTANCE.SendSiegeInfoToNearby(mDefendingClaim.ToChunkPos());
		}
	}
	
	public void ReadFromNBT(NBTTagCompound tags)
	{
		mAttackingSiegeCamps.clear();
		
		// Get the attacker and defender
		mAttackingFaction = tags.getUniqueId("attacker");
		mDefendingFaction = tags.getUniqueId("defender");
		
		// Get the important locations
		NBTTagList claimList = tags.getTagList("attackLocations", 11); // IntArray (see NBTBase.class)
		if(claimList != null)
		{
			for(NBTBase base : claimList)
			{
				NBTTagIntArray claimInfo = (NBTTagIntArray)base;
				DimBlockPos pos = DimBlockPos.ReadFromNBT(claimInfo);
				mAttackingSiegeCamps.add(pos);
			}
		}
				
		mDefendingClaim = DimBlockPos.ReadFromNBT(tags, "defendLocation");
		mAttackProgress = tags.getInteger("progress");
	}
	
	public void WriteToNBT(NBTTagCompound tags)
	{
		// Set attacker / defender
		tags.setUniqueId("attacker", mAttackingFaction);
		tags.setUniqueId("defender", mDefendingFaction);
		
		// Set important locations
		NBTTagList claimsList = new NBTTagList();
		for(DimBlockPos pos : mAttackingSiegeCamps)
		{
			claimsList.appendTag(pos.WriteToNBT());
		}
		tags.setTag("attackLocations", claimsList);
				
		tags.setTag("defendLocation", mDefendingClaim.WriteToNBT());
		tags.setInteger("progress", mAttackProgress);
	}
}
