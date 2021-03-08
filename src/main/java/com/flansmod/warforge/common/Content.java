package com.flansmod.warforge.common;

import com.flansmod.warforge.common.blocks.BlockBasicClaim;
import com.flansmod.warforge.common.blocks.BlockCitadel;
import com.flansmod.warforge.common.blocks.BlockSiegeCamp;
import com.flansmod.warforge.common.blocks.BlockYieldProvider;
import com.flansmod.warforge.common.blocks.TileEntityBasicClaim;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.common.blocks.TileEntityReinforcedClaim;
import com.flansmod.warforge.common.blocks.TileEntitySiegeCamp;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Content 
{
	public Block citadelBlock, basicClaimBlock, reinforcedClaimBlock, siegeCampBlock;
	public Item citadelBlockItem, basicClaimBlockItem, reinforcedClaimBlockItem, siegeCampBlockItem;
	
	public Block denseIronOreBlock, denseGoldOreBlock, denseDiamondOreBlock, magmaVentBlock;
	public Item denseIronOreItem, denseGoldOreItem, denseDiamondOreItem, magmaVentItem;
	
	public void preInit()
	{
        citadelBlock = new BlockCitadel(Material.ROCK).setRegistryName("citadelblock").setUnlocalizedName("citadelblock");
        citadelBlockItem = new ItemBlock(citadelBlock).setRegistryName("citadelblock").setUnlocalizedName("citadelblock");
        GameRegistry.registerTileEntity(TileEntityCitadel.class, new ResourceLocation(WarForgeMod.MODID, "citadel"));
        
        // Basic and reinforced claims, they share a tile entity
        basicClaimBlock = new BlockBasicClaim(Material.ROCK).setRegistryName("basicclaimblock").setUnlocalizedName("basicclaimblock");
        basicClaimBlockItem = new ItemBlock(basicClaimBlock).setRegistryName("basicclaimblock").setUnlocalizedName("basicclaimblock");
        GameRegistry.registerTileEntity(TileEntityBasicClaim.class, new ResourceLocation(WarForgeMod.MODID, "basicclaim"));
        reinforcedClaimBlock = new BlockBasicClaim(Material.ROCK).setRegistryName("reinforcedclaimblock").setUnlocalizedName("reinforcedclaimblock");
        reinforcedClaimBlockItem = new ItemBlock(reinforcedClaimBlock).setRegistryName("reinforcedclaimblock").setUnlocalizedName("reinforcedclaimblock");
        GameRegistry.registerTileEntity(TileEntityReinforcedClaim.class, new ResourceLocation(WarForgeMod.MODID, "reinforcedclaim"));
        
        // Siege camp
        siegeCampBlock = new BlockSiegeCamp(Material.ROCK).setRegistryName("siegecampblock").setUnlocalizedName("siegecampblock");
        siegeCampBlockItem = new ItemBlock(siegeCampBlock).setRegistryName("siegecampblock").setUnlocalizedName("siegecampblock");
        GameRegistry.registerTileEntity(TileEntitySiegeCamp.class, new ResourceLocation(WarForgeMod.MODID, "siegecamp"));
        
        denseIronOreBlock = new BlockYieldProvider(Material.ROCK, WarForgeMod.IRON_YIELD_AS_ORE ? new ItemStack(Blocks.IRON_ORE) : new ItemStack(Items.IRON_INGOT), WarForgeMod.NUM_IRON_PER_DAY_PER_ORE).setRegistryName("denseironore").setUnlocalizedName("denseironore");
        denseGoldOreBlock = new BlockYieldProvider(Material.ROCK, WarForgeMod.GOLD_YIELD_AS_ORE ? new ItemStack(Blocks.GOLD_ORE) : new ItemStack(Items.GOLD_INGOT), WarForgeMod.NUM_GOLD_PER_DAY_PER_ORE).setRegistryName("densegoldore").setUnlocalizedName("densegoldore");
        denseDiamondOreBlock = new BlockYieldProvider(Material.ROCK, WarForgeMod.DIAMOND_YIELD_AS_ORE ? new ItemStack(Blocks.DIAMOND_ORE) : new ItemStack(Items.DIAMOND), WarForgeMod.NUM_DIAMOND_PER_DAY_PER_ORE).setRegistryName("densediamondore").setUnlocalizedName("densediamondore");
        magmaVentBlock = new BlockYieldProvider(Material.ROCK, new ItemStack(Items.LAVA_BUCKET), 0.01f).setRegistryName("magmavent").setUnlocalizedName("magmavent");
        
        denseIronOreItem = new ItemBlock(denseIronOreBlock).setRegistryName("denseironore").setUnlocalizedName("denseironore");
        denseGoldOreItem = new ItemBlock(denseGoldOreBlock).setRegistryName("densegoldore").setUnlocalizedName("densegoldore");
        denseDiamondOreItem = new ItemBlock(denseDiamondOreBlock).setRegistryName("densediamondore").setUnlocalizedName("densediamondore");
        magmaVentItem = new ItemBlock(magmaVentBlock).setRegistryName("magmavent").setUnlocalizedName("magmavent");
    
        MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event)
	{		
		event.getRegistry().register(citadelBlockItem);
		event.getRegistry().register(basicClaimBlockItem);
		event.getRegistry().register(reinforcedClaimBlockItem);
		event.getRegistry().register(siegeCampBlockItem);
		event.getRegistry().register(denseIronOreItem);
		event.getRegistry().register(denseGoldOreItem);
		event.getRegistry().register(denseDiamondOreItem);
		event.getRegistry().register(magmaVentItem);
		WarForgeMod.LOGGER.info("Registered items");
	}
	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().register(citadelBlock);
		event.getRegistry().register(basicClaimBlock);
		event.getRegistry().register(reinforcedClaimBlock);
		event.getRegistry().register(siegeCampBlock);
		event.getRegistry().register(denseIronOreBlock);
		event.getRegistry().register(denseGoldOreBlock);
		event.getRegistry().register(denseDiamondOreBlock);
		event.getRegistry().register(magmaVentBlock);
		WarForgeMod.LOGGER.info("Registered blocks");
	}
}
