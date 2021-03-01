package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileEntitySiegeCamp extends TileEntity implements IClaim
{
	private UUID mPlacer = Faction.NULL;
	private UUID mFactionUUID = Faction.NULL;
	private int mColour = 0xffffff;
	private BlockPos mSiegeTarget = null;
	
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
	public int GetDefenceStrength() { return 0; }
	@Override
	public int GetSupportStrength() { return 0; }
	@Override
	public int GetAttackStrength() { return WarForgeMod.ATTACK_STRENGTH_SIEGE_CAMP; }
	@Override
	public UUID GetFaction() { return mFactionUUID; }
	@Override 
	public boolean CanBeSieged() { return false; }
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
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
	}
	
	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, SPacketUpdateTileEntity packet)
	{
		NBTTagCompound tags = packet.getNbtCompound();
		
		mFactionUUID = tags.getUniqueId("faction");
		mColour = tags.getInteger("colour");
	}
	
	@Override
	public NBTTagCompound getUpdateTag()
	{
		// You have to get parent tags so that x, y, z are added.
		NBTTagCompound tags = super.getUpdateTag();

		// Custom partial nbt write method
		tags.setUniqueId("faction", mFactionUUID);
		tags.setInteger("colour", mColour);
		
		return tags;
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tags)
	{
		mFactionUUID = tags.getUniqueId("faction");
		mColour = tags.getInteger("colour");
	}

	@Override
	public void OnServerSetFaction(Faction faction) 
	{
		if(faction != null)
		{
			mFactionUUID = faction.mUUID;
			mColour = faction.mColour;
		}
		else
		{
			WarForgeMod.logger.error("Siege camp placed by player with no faction");
		}
	}

	
	
}
