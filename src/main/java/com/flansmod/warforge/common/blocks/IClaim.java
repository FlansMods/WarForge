package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.server.Faction;

import net.minecraft.tileentity.TileEntity;

public interface IClaim 
{
	public DimBlockPos GetPos();
	
	public TileEntity GetAsTileEntity();
	
	public boolean CanBeSieged();
	
	public int GetStrengthRequiredToSiege();
	
	public void OnServerSetFaction(Faction faction);
	
	public UUID GetFaction();
	
	public int GetColour();
}
