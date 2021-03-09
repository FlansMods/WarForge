package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class TileEntityAdminClaim extends TileEntity implements IClaim
{
	protected UUID mFactionUUID = Faction.NULL;
	public int mColour = 0xffffff;
	public String mFactionName = "";
	
	// IClaim
	@Override
	public UUID GetFaction() { return mFactionUUID; }
	@Override
	public int GetColour() { return mColour; }
	@Override
	public TileEntity GetAsTileEntity() { return this; }
	@Override
	public DimBlockPos GetPos() { return new DimBlockPos(world.provider.getDimension(), getPos()); }
	@Override 
	public boolean CanBeSieged() { return false; }
	@Override
	public String GetDisplayName() { return mFactionName; }
	// ------------
	@Override
	public int GetAttackStrength() { return 0; }
	@Override
	public int GetDefenceStrength() { return 0; }
	@Override
	public int GetSupportStrength() { return 0; }
	@Override
	public void OnServerSetFaction(Faction faction)
	{
		if(faction == null)
		{
			mFactionUUID = Faction.NULL;
		}
		else
		{
			mFactionUUID = faction.mUUID;
			mColour = faction.mColour;
			mFactionName = faction.mName;
		}
		
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos, this.getBlockType(), 0, 0);
		markDirty();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setUniqueId("faction", mFactionUUID);		
		return nbt;
	}

	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
	
		mFactionUUID = nbt.getUniqueId("faction");
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			Faction faction = WarForgeMod.FACTIONS.GetFaction(mFactionUUID);
			if(!mFactionUUID.equals(Faction.NULL) && faction == null)
			{
				WarForgeMod.LOGGER.error("Faction " + mFactionUUID + " could not be found for citadel at " + pos);
				//world.setBlockState(getPos(), Blocks.AIR.getDefaultState());
			}
			if(faction != null)
			{
				mColour = faction.mColour;
				mFactionName = faction.mName;
			}
		}
		else
		{
			WarForgeMod.LOGGER.error("Loaded TileEntity from NBT on client?");
		}
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
		mFactionName = tags.getString("name");
	}
	
	@Override
	public NBTTagCompound getUpdateTag()
	{
		// You have to get parent tags so that x, y, z are added.
		NBTTagCompound tags = super.getUpdateTag();

		// Custom partial nbt write method
		tags.setUniqueId("faction", mFactionUUID);
		tags.setInteger("colour", mColour);
		tags.setString("name", mFactionName);
		
		return tags;
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tags)
	{
		mFactionUUID = tags.getUniqueId("faction");
		mColour = tags.getInteger("colour");
		mFactionName = tags.getString("name");
	}
	
}
