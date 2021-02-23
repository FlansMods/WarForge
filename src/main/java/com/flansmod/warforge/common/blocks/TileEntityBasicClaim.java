package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBasicClaim extends TileEntityYieldCollector implements ISiegeable
{
	public static final int NUM_SLOTS = NUM_BASE_SLOTS; // No additional slots here
	public UUID mFactionUUID = Faction.NULL;
	public int mStrength = 5;
	
	public TileEntityBasicClaim(int strength)
	{
		mStrength = strength;
	}

	public void OnPlacedBy(EntityLivingBase placer) 
	{
		Faction faction = WarForgeMod.INSTANCE.GetFactionOfPlayer(placer.getUniqueID());
		if(faction != null)
			mFactionUUID = faction.mUUID;
	}

	@Override
	public TileEntity GetAsTileEntity() 
	{
		return null;
	}

	@Override
	public int GetStrengthRequiredToSiege() 
	{
		return 0;
	}
}
