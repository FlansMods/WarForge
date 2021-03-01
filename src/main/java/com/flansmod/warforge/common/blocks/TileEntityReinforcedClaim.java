package com.flansmod.warforge.common.blocks;

import com.flansmod.warforge.common.WarForgeMod;

public class TileEntityReinforcedClaim extends TileEntityBasicClaim
{
	@Override
	public int GetDefenceStrength() { return WarForgeMod.CLAIM_STRENGTH_REINFORCED; }
	@Override
	public int GetSupportStrength() { return WarForgeMod.SUPPORT_STRENGTH_REINFORCED; }
	@Override
	public int GetAttackStrength() { return 0; }
}
