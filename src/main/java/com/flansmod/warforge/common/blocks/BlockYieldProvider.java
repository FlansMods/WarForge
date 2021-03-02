package com.flansmod.warforge.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

// An unharvestable resource block that contributes to the yield of any faction that claims it
public class BlockYieldProvider extends Block
{
	public ItemStack mYieldToProvide = ItemStack.EMPTY;
	public float mMultiplier = 1.0f;
	 
	public BlockYieldProvider(Material material, ItemStack yieldStack, float multiplier) 
	{
		super(material);
		
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		
		this.setBlockUnbreakable();
		this.setHardness(300000000F);
		
		mYieldToProvide = yieldStack;
		mMultiplier = multiplier;
	}
	
	
}
