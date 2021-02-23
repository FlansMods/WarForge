package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBasicClaim extends TileEntityYieldCollector implements IClaim
{
	public static final int NUM_SLOTS = NUM_BASE_SLOTS; // No additional slots here
	public int mStrength = 5;
	
	public TileEntityBasicClaim()
	{
		
	}
	
	public TileEntityBasicClaim(int strength)
	{
		mStrength = strength;
	}
	
	// TODO: Think we might need to store the strength or have two seperate TileEntity registrations

	@Override
	public int GetStrengthRequiredToSiege() { return mStrength; }
}
