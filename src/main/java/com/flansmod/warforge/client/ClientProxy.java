package com.flansmod.warforge.client;

import com.flansmod.warforge.common.CommonProxy;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void PreInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		switch(ID)
		{
			case GUI_TYPE_CITADEL: return new GuiCitadel(getServerGuiElement(ID, player, world, x, y, z));
			case GUI_TYPE_CREATE_FACTION: return new GuiCreateFaction((TileEntityCitadel)world.getTileEntity(new BlockPos(x, y, z)));
		}
		return null;
	}
}
