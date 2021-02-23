package com.flansmod.warforge.common.network;

import java.util.ArrayList;
import java.util.UUID;

import com.flansmod.warforge.server.Faction;

// What gets sent over network to display faction information on client
public class FactionDisplayInfo 
{	
	public UUID mFactionID = Faction.NULL;
	public String mFactionName = "";
	public UUID mLeaderID = Faction.NULL;
	public ArrayList<PlayerDisplayInfo> mMembers = new ArrayList<PlayerDisplayInfo>();
	public int mNotoriety = 0;
}
