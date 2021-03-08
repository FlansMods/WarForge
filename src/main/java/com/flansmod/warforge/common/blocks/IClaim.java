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
	public int GetAttackStrength();
	public int GetDefenceStrength();
	public int GetSupportStrength();
	
	public void OnServerSetFaction(Faction faction);
	
	// Server side uuid - means nothing to a client
	public UUID GetFaction();
	
	// Client side data - can't use UUID to identify anything on client
	public int GetColour();
	public String GetDisplayName();
}
