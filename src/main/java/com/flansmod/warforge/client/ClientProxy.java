package com.flansmod.warforge.client;

import java.util.HashMap;
import java.util.UUID;

import com.flansmod.warforge.common.CommonProxy;
import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.TileEntityBasicClaim;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.common.blocks.TileEntitySiegeCamp;
import com.flansmod.warforge.common.network.PacketRequestFactionInfo;
import com.flansmod.warforge.common.network.SiegeCampProgressInfo;

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
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{
	public static HashMap<DimBlockPos, SiegeCampProgressInfo> sSiegeInfo = new HashMap<DimBlockPos, SiegeCampProgressInfo>();
	
	@Override
	public void PreInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
	}
	
	@Override
	public void Init(FMLInitializationEvent event)
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCitadel.class, new TileEntityBeamRender());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBasicClaim.class, new TileEntityBeamRender());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySiegeCamp.class, new TileEntityBeamRender());
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
			//case GUI_TYPE_SIEGE_CAMP: return new GuiSiegeCamp();
			case GUI_TYPE_LEADERBOARD: return new GuiLeaderboard();
		}
		return null;
	}
	
	@Override 	
	public TileEntity GetTile(DimBlockPos pos)
	{
		if(Minecraft.getMinecraft().world.provider.getDimension() == pos.mDim)	
			return Minecraft.getMinecraft().world.getTileEntity(pos.ToRegularPos());
		
		WarForgeMod.LOGGER.error("Can't get info about a tile entity in a different dimension on client");
		return null;
	}
	
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event)
	{		
		RegisterModel(WarForgeMod.CONTENT.citadelBlockItem);
		RegisterModel(WarForgeMod.CONTENT.basicClaimBlockItem);
		RegisterModel(WarForgeMod.CONTENT.reinforcedClaimBlockItem);
		RegisterModel(WarForgeMod.CONTENT.siegeCampBlockItem);
		RegisterModel(WarForgeMod.CONTENT.adminClaimBlockItem);
		
		RegisterModel(WarForgeMod.CONTENT.denseIronOreItem);
		RegisterModel(WarForgeMod.CONTENT.denseGoldOreItem);
		RegisterModel(WarForgeMod.CONTENT.denseDiamondOreItem);
		RegisterModel(WarForgeMod.CONTENT.magmaVentItem);
	}
	
	private void RegisterModel(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
	
	public void UpdateSiegeInfo(SiegeCampProgressInfo info) 
	{
		if(sSiegeInfo.containsKey(info.mAttackingPos))
		{
			sSiegeInfo.remove(info.mAttackingPos);
		}
		
		sSiegeInfo.put(info.mAttackingPos, info);
	}
	
	public static void RequestFactionInfo(UUID factionID)
	{
		PacketRequestFactionInfo request = new PacketRequestFactionInfo();
		request.mFactionIDRequest = factionID;
		WarForgeMod.INSTANCE.NETWORK.sendToServer(request);
	}
}
