package com.flansmod.warforge.common;

import com.flansmod.warforge.common.blocks.BlockAdminClaim;
import com.flansmod.warforge.common.blocks.BlockBasicClaim;
import com.flansmod.warforge.common.blocks.BlockCitadel;
import com.flansmod.warforge.common.blocks.BlockLeaderboard;
import com.flansmod.warforge.common.blocks.BlockSiegeCamp;
import com.flansmod.warforge.common.blocks.BlockYieldProvider;
import com.flansmod.warforge.common.blocks.TileEntityAdminClaim;
import com.flansmod.warforge.common.blocks.TileEntityBasicClaim;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.common.blocks.TileEntityLeaderboard;
import com.flansmod.warforge.common.blocks.TileEntityReinforcedClaim;
import com.flansmod.warforge.common.blocks.TileEntitySiegeCamp;
import com.flansmod.warforge.server.Leaderboard.FactionStat;

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
	
	public Block denseIronOreBlock, denseGoldOreBlock, denseDiamondOreBlock, magmaVentBlock,
		denseQuartzOreBlock, denseClayBlock, ancientOakBlock;
	public Item denseIronOreItem, denseGoldOreItem, denseDiamondOreItem, magmaVentItem,
		denseQuartzOreItem, denseClayItem, ancientOakItem;
	
	public Block adminClaimBlock;
	public Item adminClaimBlockItem;
	
	public Block topLeaderboardBlock, notorietyLeaderboardBlock, wealthLeaderboardBlock, legacyLeaderboardBlock;
	public Item topLeaderboardItem, notorietyLeaderboardItem, wealthLeaderboardItem, legacyLeaderboardItem;
	
	
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
 
        // Admin claim block
        adminClaimBlock = new BlockAdminClaim().setRegistryName("adminclaimblock").setUnlocalizedName("adminclaimblock");
        adminClaimBlockItem = new ItemBlock(adminClaimBlock).setRegistryName("adminclaimblock").setUnlocalizedName("adminclaimblock");
        GameRegistry.registerTileEntity(TileEntityAdminClaim.class, new ResourceLocation(WarForgeMod.MODID, "adminclaim"));
 
        
        denseIronOreBlock = new BlockYieldProvider(Material.ROCK, WarForgeConfig.IRON_YIELD_AS_ORE ? new ItemStack(Blocks.IRON_ORE) : new ItemStack(Items.IRON_INGOT), WarForgeConfig.NUM_IRON_PER_DAY_PER_ORE).setRegistryName("denseironore").setUnlocalizedName("denseironore");
        denseGoldOreBlock = new BlockYieldProvider(Material.ROCK, WarForgeConfig.GOLD_YIELD_AS_ORE ? new ItemStack(Blocks.GOLD_ORE) : new ItemStack(Items.GOLD_INGOT), WarForgeConfig.NUM_GOLD_PER_DAY_PER_ORE).setRegistryName("densegoldore").setUnlocalizedName("densegoldore");
        denseDiamondOreBlock = new BlockYieldProvider(Material.ROCK, WarForgeConfig.DIAMOND_YIELD_AS_ORE ? new ItemStack(Blocks.DIAMOND_ORE) : new ItemStack(Items.DIAMOND), WarForgeConfig.NUM_DIAMOND_PER_DAY_PER_ORE).setRegistryName("densediamondore").setUnlocalizedName("densediamondore");
        magmaVentBlock = new BlockYieldProvider(Material.ROCK, new ItemStack(Items.LAVA_BUCKET), 0.0f).setRegistryName("magmavent").setUnlocalizedName("magmavent");
        denseQuartzOreBlock = new BlockYieldProvider(Material.ROCK, WarForgeConfig.QUARTZ_YIELD_AS_BLOCKS ? new ItemStack(Blocks.QUARTZ_BLOCK) : new ItemStack(Items.QUARTZ), WarForgeConfig.NUM_QUARTZ_PER_DAY_PER_ORE).setRegistryName("densequartzore").setUnlocalizedName("densequartzore");
        denseClayBlock = new BlockYieldProvider(Material.CLAY, WarForgeConfig.CLAY_YIELD_AS_BLOCKS ? new ItemStack(Blocks.CLAY) : new ItemStack(Items.CLAY_BALL), WarForgeConfig.NUM_CLAY_PER_DAY_PER_ORE).setRegistryName("denseclay").setUnlocalizedName("denseclay");
        ancientOakBlock = new BlockYieldProvider(Material.WOOD, WarForgeConfig.ANCIENT_OAK_YIELD_AS_LOGS ? new ItemStack(Blocks.LOG2, 1, 1) : new ItemStack(Blocks.PLANKS, 1, 5), WarForgeConfig.NUM_OAK_PER_DAY_PER_LOG).setRegistryName("ancientoak").setUnlocalizedName("ancientoak");
        
        denseIronOreItem = new ItemBlock(denseIronOreBlock).setRegistryName("denseironore").setUnlocalizedName("denseironore");
        denseGoldOreItem = new ItemBlock(denseGoldOreBlock).setRegistryName("densegoldore").setUnlocalizedName("densegoldore");
        denseDiamondOreItem = new ItemBlock(denseDiamondOreBlock).setRegistryName("densediamondore").setUnlocalizedName("densediamondore");
        magmaVentItem = new ItemBlock(magmaVentBlock).setRegistryName("magmavent").setUnlocalizedName("magmavent");
        denseClayItem = new ItemBlock(denseClayBlock).setRegistryName("denseclay").setUnlocalizedName("denseclay");
        denseQuartzOreItem = new ItemBlock(denseQuartzOreBlock).setRegistryName("densequartzore").setUnlocalizedName("densequartzore");
        ancientOakItem = new ItemBlock(ancientOakBlock).setRegistryName("ancientoak").setUnlocalizedName("ancientoak");
    
        topLeaderboardBlock = new BlockLeaderboard(Material.ROCK, FactionStat.TOTAL).setRegistryName("topleaderboard").setUnlocalizedName("topleaderboard");
        wealthLeaderboardBlock = new BlockLeaderboard(Material.ROCK, FactionStat.WEALTH).setRegistryName("wealthleaderboard").setUnlocalizedName("wealthleaderboard");
        notorietyLeaderboardBlock = new BlockLeaderboard(Material.ROCK, FactionStat.NOTORIETY).setRegistryName("notorietyleaderboard").setUnlocalizedName("notorietyleaderboard");
        legacyLeaderboardBlock = new BlockLeaderboard(Material.ROCK, FactionStat.LEGACY).setRegistryName("legacyleaderboard").setUnlocalizedName("legacyleaderboard");
        
        topLeaderboardItem = new ItemBlock(topLeaderboardBlock).setRegistryName("topleaderboard").setUnlocalizedName("topleaderboard");
        wealthLeaderboardItem = new ItemBlock(wealthLeaderboardBlock).setRegistryName("wealthleaderboard").setUnlocalizedName("wealthleaderboard");
        notorietyLeaderboardItem = new ItemBlock(notorietyLeaderboardBlock).setRegistryName("notorietyleaderboard").setUnlocalizedName("notorietyleaderboard");
        legacyLeaderboardItem = new ItemBlock(legacyLeaderboardBlock).setRegistryName("legacyleaderboard").setUnlocalizedName("legacyleaderboard");
        GameRegistry.registerTileEntity(TileEntityLeaderboard.class, new ResourceLocation(WarForgeMod.MODID, "leaderboard"));
        
        MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event)
	{		
		event.getRegistry().register(citadelBlockItem);
		event.getRegistry().register(basicClaimBlockItem);
		event.getRegistry().register(reinforcedClaimBlockItem);
		event.getRegistry().register(siegeCampBlockItem);
		event.getRegistry().register(adminClaimBlockItem);
		event.getRegistry().register(denseIronOreItem);
		event.getRegistry().register(denseGoldOreItem);
		event.getRegistry().register(denseDiamondOreItem);
		event.getRegistry().register(denseQuartzOreItem);
		event.getRegistry().register(denseClayItem);
		event.getRegistry().register(ancientOakItem);
		event.getRegistry().register(magmaVentItem);
		event.getRegistry().register(topLeaderboardItem);
		event.getRegistry().register(wealthLeaderboardItem);
		event.getRegistry().register(notorietyLeaderboardItem);
		event.getRegistry().register(legacyLeaderboardItem);
		WarForgeMod.LOGGER.info("Registered items");
	}
	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().register(citadelBlock);
		event.getRegistry().register(basicClaimBlock);
		event.getRegistry().register(reinforcedClaimBlock);
		event.getRegistry().register(siegeCampBlock);
		event.getRegistry().register(adminClaimBlock);
		event.getRegistry().register(denseIronOreBlock);
		event.getRegistry().register(denseGoldOreBlock);
		event.getRegistry().register(denseDiamondOreBlock);
		event.getRegistry().register(denseQuartzOreBlock);
		event.getRegistry().register(denseClayBlock);
		event.getRegistry().register(ancientOakBlock);
		event.getRegistry().register(magmaVentBlock);
		event.getRegistry().register(topLeaderboardBlock);
		event.getRegistry().register(wealthLeaderboardBlock);
		event.getRegistry().register(notorietyLeaderboardBlock);
		event.getRegistry().register(legacyLeaderboardBlock);
		WarForgeMod.LOGGER.info("Registered blocks");
	}
}
