package com.flansmod.warforge.common.network;

import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.server.Faction;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketCreateFaction extends PacketBase
{
	public DimBlockPos mCitadelPos;
	public String mFactionName = "";
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		data.writeInt(mCitadelPos.mDim);
		data.writeInt(mCitadelPos.getX());
		data.writeInt(mCitadelPos.getY());
		data.writeInt(mCitadelPos.getZ());
		writeUTF(data, mFactionName);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		int dim = data.readInt();
		int x = data.readInt();
		int y = data.readInt();
		int z = data.readInt();
		mCitadelPos = new DimBlockPos(dim, x, y, z);
		mFactionName = readUTF(data);
	}

	@Override
	public void handleServerSide(EntityPlayerMP playerEntity) 
	{
		if(playerEntity.dimension != mCitadelPos.mDim)
		{
			WarForgeMod.logger.error("Player requested creating a faction in the wrong dim");
		}
		else
		{
			TileEntity te = playerEntity.world.getTileEntity(mCitadelPos);
			if(te != null && te instanceof TileEntityCitadel)
			{
				WarForgeMod.INSTANCE.RequestCreateFaction((TileEntityCitadel)te, playerEntity, mFactionName);
			}
		}
	}

	@Override
	public void handleClientSide(EntityPlayer clientPlayer) 
	{
		WarForgeMod.logger.error("Recieved create faction message on client");
	}
	
}
