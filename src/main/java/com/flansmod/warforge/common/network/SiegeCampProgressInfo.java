package com.flansmod.warforge.common.network;

import com.flansmod.warforge.common.DimBlockPos;

public class SiegeCampProgressInfo
{
	public DimBlockPos mDefendingPos;
	public DimBlockPos mAttackingPos;
	public int mAttackingColour;
	public int mDefendingColour;
	public String mAttackingName;
	public String mDefendingName;
	
	public int mCompletionPoint = 5;
	public int mPreviousProgress = 0;
	public int mProgress = 0;
}
