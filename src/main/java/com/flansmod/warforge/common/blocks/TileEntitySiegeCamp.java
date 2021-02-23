package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySiegeCamp extends TileEntity // implements ISiegeable
{
	private UUID mPlacer = Faction.NULL;
	private UUID mFaction = Faction.NULL;
	
	public void OnPlacedBy(EntityLivingBase placer) 
	{
		mPlacer = placer.getUniqueID();
		Faction faction = WarForgeMod.INSTANCE.GetFactionOfPlayer(mPlacer);
		if(faction != null)
		{
			mFaction = faction.mUUID;
		}
		else
		{
			WarForgeMod.logger.error("Siege camp placed by player with no faction");
		}
	}

	/*
	@Override
	public TileEntity GetAsTileEntity() 
	{
		return this;
	}

	@Override
	public int GetStrengthRequiredToSiege() 
	{
		return 0;
	}
	*/

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setUniqueId("placer", mPlacer);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		mPlacer = nbt.getUniqueId("placer");
	}
}
