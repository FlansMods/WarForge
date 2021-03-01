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
	
	public TileEntityBasicClaim()
	{
		
	}
	
	@Override
	public int GetDefenceStrength() { return WarForgeMod.CLAIM_STRENGTH_BASIC; }
	@Override
	public int GetSupportStrength() { return WarForgeMod.SUPPORT_STRENGTH_BASIC; }
	@Override
	public int GetAttackStrength() { return 0; }
}
