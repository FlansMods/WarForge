package com.flansmod.warforge.client;

import com.flansmod.warforge.common.CommonProxy;
import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.TileEntityBasicClaim;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

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
			case GUI_TYPE_BASIC_CLAIM: return new GuiBasicClaim(getServerGuiElement(ID, player, world, x, y, z));
			case GUI_TYPE_FACTION_INFO: return new GuiFactionInfo();
			case GUI_TYPE_SIEGE_CAMP: return new GuiSiegeCamp();
		}
		return null;
	}
	
	@Override 	
	public TileEntity GetTile(DimBlockPos pos)
	{
		if(Minecraft.getMinecraft().world.provider.getDimension() == pos.mDim)	
			return Minecraft.getMinecraft().world.getTileEntity(pos.ToRegularPos());
		
		WarForgeMod.logger.error("Can't get info about a tile entity in a different dimension on client");
		return null;
	}

	
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event)
	{		
		RegisterModel(WarForgeMod.citadelBlockItem);
		RegisterModel(WarForgeMod.basicClaimBlockItem);
		RegisterModel(WarForgeMod.reinforcedClaimBlockItem);
		RegisterModel(WarForgeMod.siegeCampBlockItem);
		
		RegisterModel(WarForgeMod.denseIronOreItem);
		RegisterModel(WarForgeMod.denseGoldOreItem);
		RegisterModel(WarForgeMod.denseDiamondOreItem);
		RegisterModel(WarForgeMod.magmaVentItem);
	}
	
	private void RegisterModel(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}
