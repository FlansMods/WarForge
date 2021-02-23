package com.flansmod.warforge.common.blocks;

import net.minecraft.tileentity.TileEntity;

public interface ISiegeable 
{
	public TileEntity GetAsTileEntity();
	
	public int GetStrengthRequiredToSiege();
}
