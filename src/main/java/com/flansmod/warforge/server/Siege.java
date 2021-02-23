package com.flansmod.warforge.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction.PlayerData;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

public class Siege 
{
	public UUID mAttackingFaction;
	public UUID mDefendingFaction;
	public ArrayList<DimBlockPos> mAttackingSiegeCamps;
	public DimBlockPos mDefendingClaim;
	public ArrayList<DimBlockPos> mSupportingClaims;
	
	// This is defined by the chunk we are attacking and what type it is
	public int mAttackSuccessThreshold = 5;
	
	// Attack progress starts at 0 and can be moved to -5 or mAttackSuccessThreshold
	private int mAttackProgress = 0;
	
	public int GetAttackProgress() { return mAttackProgress; }
	public int GetDefenceProgress() { return -mAttackProgress; }
	
	
	public Siege()
	{
		mAttackingSiegeCamps = new ArrayList<DimBlockPos>(4);
		mSupportingClaims = new ArrayList<DimBlockPos>(5);
	}
	
	public Siege(UUID attacker, UUID defender, DimBlockPos defending)
	{
		mAttackingFaction = attacker;
		mDefendingFaction = defender;
		mDefendingClaim = defending;
	}
	
	public void ReadFromNBT(NBTTagCompound tags)
	{
		mAttackingSiegeCamps.clear();
		mSupportingClaims.clear();
		
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
		
		claimList = tags.getTagList("supportLocations", 11); // IntArray (see NBTBase.class)
		if(claimList != null)
		{
			for(NBTBase base : claimList)
			{
				NBTTagIntArray claimInfo = (NBTTagIntArray)base;
				DimBlockPos pos = DimBlockPos.ReadFromNBT(claimInfo);
				mSupportingClaims.add(pos);
			}
		}
		
		mDefendingClaim = DimBlockPos.ReadFromNBT(tags, "defendLocation");
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
		
		claimsList = new NBTTagList();
		for(DimBlockPos pos : mSupportingClaims)
		{
			claimsList.appendTag(pos.WriteToNBT());
		}
		tags.setTag("supportLocations", claimsList);
		
		tags.setTag("defendLocation", mDefendingClaim.WriteToNBT());
	}
}
