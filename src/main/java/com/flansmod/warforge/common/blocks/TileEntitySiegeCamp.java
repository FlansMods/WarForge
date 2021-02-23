package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySiegeCamp extends TileEntity implements IClaim
{
	private UUID mPlacer = Faction.NULL;
	private UUID mFaction = Faction.NULL;
	private int mColour = 0xffffff;
	
	public TileEntitySiegeCamp()
	{
		
	}
	
	public void OnPlacedBy(EntityLivingBase placer) 
	{
		mPlacer = placer.getUniqueID();
		
	}
	
	@Override
	public TileEntity GetAsTileEntity() { return this; }
	@Override
	public DimBlockPos GetPos() { return new DimBlockPos(world.provider.getDimension(), getPos()); }
	@Override
	public int GetStrengthRequiredToSiege() { return 0; }
	@Override
	public UUID GetFaction() { return mFaction; }
	@Override 
	public boolean CanBeSieged() { return true; }
	@Override
	public int GetColour() { return mColour; }

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

	@Override
	public void OnServerSetFaction(Faction faction) 
	{
		if(faction != null)
		{
			mFaction = faction.mUUID;
			mColour = faction.mColour;
		}
		else
		{
			WarForgeMod.logger.error("Siege camp placed by player with no faction");
		}
	}
}
