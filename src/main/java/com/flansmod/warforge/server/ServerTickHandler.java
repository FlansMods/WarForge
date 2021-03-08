package com.flansmod.warforge.server;

import com.flansmod.warforge.common.WarForgeMod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ServerTickHandler 
{
	@SubscribeEvent
	public void OnTick(ServerTickEvent tick)
	{
		WarForgeMod.INSTANCE.UpdateServer();
		WarForgeMod.INSTANCE.sPacketHandler.handleServerPackets();
	}
}
