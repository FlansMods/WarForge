package com.flansmod.warforge.common.network;

import com.flansmod.warforge.common.CommonProxy;
import com.flansmod.warforge.common.WarForgeMod;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketFactionInfo extends PacketBase 
{
	// Cheeky hack to make it available to the GUI
	public static FactionDisplayInfo sLatestInfo = null;
	
	public FactionDisplayInfo mInfo;
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		if(mInfo != null)
		{
			writeUUID(data, mInfo.mFactionID);
			writeUTF(data, mInfo.mFactionName);
			
			data.writeInt(mInfo.mNotoriety);
			
			// Member list
			data.writeInt(mInfo.mMembers.size());
			for(int i = 0; i < mInfo.mMembers.size(); i++) 
			{
				writeUUID(data, mInfo.mMembers.get(i).mPlayerUUID);
				writeUTF(data, mInfo.mMembers.get(i).mPlayerName);
			}
			writeUUID(data, mInfo.mLeaderID);
		}
		
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		mInfo = new FactionDisplayInfo();
		
		mInfo.mFactionID = readUUID(data);
		mInfo.mFactionName = readUTF(data);
		mInfo.mNotoriety = data.readInt();
		
		// Member list
		int count = data.readInt();
		for(int i = 0; i < count; i++)
		{
			PlayerDisplayInfo playerInfo = new PlayerDisplayInfo();
			playerInfo.mPlayerUUID = readUUID(data);
			playerInfo.mPlayerName = readUTF(data);
			mInfo.mMembers.add(playerInfo);
		}
		mInfo.mLeaderID = readUUID(data);
	}

	@Override
	public void handleServerSide(EntityPlayerMP playerEntity) 
	{
		WarForgeMod.INSTANCE.logger.error("Received FactionInfo on server");
	}

	@Override
	public void handleClientSide(EntityPlayer clientPlayer) 
	{
		sLatestInfo = mInfo;
		clientPlayer.openGui(
				WarForgeMod.INSTANCE, 
				CommonProxy.GUI_TYPE_FACTION_INFO, 
				clientPlayer.world, 
				clientPlayer.getPosition().getX(),
				clientPlayer.getPosition().getY(),
				clientPlayer.getPosition().getZ());
	}

}
