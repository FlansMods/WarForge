package com.flansmod.warforge.common;

import com.flansmod.warforge.common.blocks.TileEntityCitadel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
	public static final int GUI_TYPE_CITADEL = 0;
	public static final int GUI_TYPE_CREATE_FACTION = 1;
	
	public void PreInit(FMLPreInitializationEvent event)
	{
		
	}
	
	public void Init(FMLInitializationEvent event)
	{
		
	}
	
	public void PostInit(FMLPostInitializationEvent event)
	{
		
	}
	
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
	
	/**
	 * Gets the container for the specified GUI
	 */
	public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		BlockPos pos = new BlockPos(x, y, z);
		switch(ID)
		{
			case GUI_TYPE_CITADEL: return new ContainerCitadel(player.inventory, (TileEntityCitadel)world.getTileEntity(pos));
			case GUI_TYPE_CREATE_FACTION: return null;
		}
		return null;
	}
}
