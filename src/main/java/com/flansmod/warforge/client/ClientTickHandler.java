package com.flansmod.warforge.client;

import com.flansmod.warforge.common.WarForgeMod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class ClientTickHandler 
{
	@SubscribeEvent
	public void OnTick(ClientTickEvent tick)
	{
		WarForgeMod.INSTANCE.Update();
		WarForgeMod.INSTANCE.packetHandler.handleClientPackets();
	}
}
