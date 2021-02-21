package com.flansmod.warforge.server;

import java.util.HashMap;
import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.server.FMLServerHandler;

public class Faction 
{
	public static final UUID NULL = new UUID(0, 0);
	public static final UUID CreateUUID(String factionName)
	{
		return new UUID(0xfedcba0987654321L, ((long)factionName.hashCode()) * 0xa0a0b1b1c2c2d3d3L);
	}
	private static final float INVITE_DECAY_TIME = 20 * 60 * 5; // 5 minutes, TODO: Config
	
	public class PlayerData
	{
		public Faction.Role mRole = Faction.Role.MEMBER;
		
		public void ReadFromNBT(NBTTagCompound tags)
		{
			// Read and write role by string so enum order can change
			mRole = Faction.Role.valueOf(tags.getString("role"));
		}
		
		public void WriteToNBT(NBTTagCompound tags)
		{
			tags.setString("role", mRole.name());
		}
	}
	
	public enum Role
	{
		GUEST, // ?
		MEMBER,
		OFFICER,
		LEADER,
	}
	
	public UUID mUUID;
	public String mName;
	public DimBlockPos mCitadelPos;
	public HashMap<UUID, PlayerData> mMembers;
	public HashMap<UUID, Float> mPendingInvites;
	public int mNotoriety;
	
	public Faction()
	{
		mMembers = new HashMap<UUID, PlayerData>();
		mPendingInvites = new HashMap<UUID, Float>();
	}
	
	public void Update()
	{
		UUID uuidToRemove = NULL;
		for(HashMap.Entry<UUID, Float> entry : mPendingInvites.entrySet())
		{
			entry.setValue(entry.getValue() - 1);
			if(entry.getValue() <= 0)
				uuidToRemove = entry.getKey();
		}
		
		// So this could break if players were sending > 1 unique invite per tick, but why would they do that?
		if(uuidToRemove != NULL)
			mPendingInvites.remove(uuidToRemove);
	}
	
	public boolean InvitePlayer(UUID playerID)
	{
		// Don't invite offline players
		if(GetPlayer(playerID) == null)
			return false;
		
		if(mPendingInvites.containsKey(playerID))
			mPendingInvites.replace(playerID, INVITE_DECAY_TIME);
		else
			mPendingInvites.put(playerID, INVITE_DECAY_TIME);
		return true;
	}
	
	public boolean IsInvitingPlayer(UUID playerID)
	{
		return mPendingInvites.containsKey(playerID);
	}
	
	public boolean AddPlayer(UUID playerID)
	{
		mMembers.put(playerID, new PlayerData());
		if(mPendingInvites.containsKey(playerID))
			mPendingInvites.remove(playerID);
		
		// Let everyone know
		MessageAll(new TextComponentString(GetPlayerName(playerID) + " joined the faction."));
		
		return true;
	}
	
	// TODO: 
	// Rank up
	// Rank down
	
	public boolean SetLeader(UUID playerID)
	{
		if(!mMembers.containsKey(playerID))
			return false;
		
		for(HashMap.Entry<UUID, PlayerData> entry : mMembers.entrySet())
		{
			// Set the target player as leader
			if(entry.getKey() == playerID)
				entry.getValue().mRole = Role.LEADER;
			// And set any existing leaders to officers
			else if(entry.getValue().mRole == Role.LEADER)
				entry.getValue().mRole = Role.OFFICER;
		}
		

		MessageAll(new TextComponentString(GetPlayerName(playerID) + " was made leader of the faction."));
		return true;
	}
	
	public boolean RemovePlayer(UUID playerID)
	{
		if(mMembers.containsKey(playerID))
		{
			mMembers.remove(playerID);
			return true;
		}
		return false;
	}
	
	public boolean IsPlayerInFaction(UUID playerID)
	{
		return mMembers.containsKey(playerID);
	}
	
	public boolean IsPlayerRoleInFaction(UUID playerID, Role role)
	{
		if(mMembers.containsKey(playerID))
			return mMembers.get(playerID).mRole.ordinal() >= role.ordinal();
		return false;
	}
	
	public boolean IsPlayerOutrankingOfficerOf(UUID playerID, UUID targetID)
	{
		if(mMembers.containsKey(playerID) && mMembers.containsKey(targetID))
		{
			Role playerRole = mMembers.get(playerID).mRole;
			Role targetRole = mMembers.get(targetID).mRole;
			return playerRole.ordinal() >= Role.OFFICER.ordinal()
				&& playerRole.ordinal() > targetRole.ordinal();
		}
		return false;
	}
	
	// Messaging
	public void MessageAll(ITextComponent chat)
	{
		for(UUID playerID : mMembers.keySet())
		{
			EntityPlayer player = GetPlayer(playerID);
			if(player != null)
				player.sendMessage(chat);
		}
	}
	
	
	public void ReadFromNBT(NBTTagCompound tags)
	{
		// Get citadel pos and defining params
		mUUID = tags.getUniqueId("uuid");
		mName = tags.getString("name");
		int dim = tags.getInteger("citadelDim");
		int x = tags.getInteger("citadelX");
		int y = tags.getInteger("citadelY");
		int z = tags.getInteger("citadelZ");
		mCitadelPos = new DimBlockPos(dim, x, y, z);
		// TODO: Verify block still exists
		
		// Get gameplay params
		mNotoriety = tags.getInteger("notoriety");
		
		// Get member data
		NBTTagList memberList = tags.getTagList("members", 10); // NBTTagCompound (see NBTBase.class)
		for(NBTBase base : memberList)
		{
			NBTTagCompound memberTags = (NBTTagCompound)base;
			UUID uuid = memberTags.getUniqueId("uuid");
			PlayerData data = new PlayerData();
			data.ReadFromNBT(memberTags);
			mMembers.put(uuid, data);
		}
	}
	
	public void WriteToNBT(NBTTagCompound tags)
	{
		// Set citadel pos and core params
		tags.setUniqueId("uuid", mUUID);
		tags.setString("name", mName);
		tags.setInteger("citadelDim", mCitadelPos.mDim);
		tags.setInteger("citadelX", mCitadelPos.getX());
		tags.setInteger("citadelY", mCitadelPos.getY());
		tags.setInteger("citadelZ", mCitadelPos.getZ());
		
		// Set gameplay params
		tags.setInteger("notoriety", mNotoriety);
		
		// Add member data
		NBTTagList memberList = new NBTTagList();
		for(HashMap.Entry<UUID, PlayerData> kvp : mMembers.entrySet())
		{
			NBTTagCompound memberTags = new NBTTagCompound();
			memberTags.setUniqueId("uuid", kvp.getKey());
			kvp.getValue().WriteToNBT(memberTags);
			memberList.appendTag(memberTags);
		}
		tags.setTag("members", memberList);
	}
	
	private static String GetPlayerName(UUID playerID)
	{
		EntityPlayer player = GetPlayer(playerID);
		return player == null ? ("[" + playerID.toString() + "]") : player.getName();
	}
	
	private static EntityPlayer GetPlayer(UUID playerID)
	{
		return FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playerID);
	}
}
