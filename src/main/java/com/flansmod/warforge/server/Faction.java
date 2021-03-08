package com.flansmod.warforge.server;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.DimChunkPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.IClaim;
import com.flansmod.warforge.common.blocks.TileEntitySiegeCamp;
import com.flansmod.warforge.common.blocks.TileEntityYieldCollector;
import com.flansmod.warforge.common.network.FactionDisplayInfo;
import com.flansmod.warforge.common.network.PlayerDisplayInfo;
import com.flansmod.warforge.server.Leaderboard.FactionStat;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
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
	public HashMap<DimBlockPos, Integer> mClaims;
	public HashMap<UUID, PlayerData> mMembers;
	public HashMap<UUID, Float> mPendingInvites;
	public boolean mHasHadAnyLoginsToday;
	public int mColour = 0xffffff;
	public int mNotoriety = 0;
	public int mWealth = 0;
	public int mLegacy = 0;
	
	public Faction()
	{
		mMembers = new HashMap<UUID, PlayerData>();
		mPendingInvites = new HashMap<UUID, Float>();
		mClaims = new HashMap<DimBlockPos, Integer>();
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
		if(!uuidToRemove.equals(NULL))
			mPendingInvites.remove(uuidToRemove);
		
		if(!mHasHadAnyLoginsToday)
		{
			for(HashMap.Entry<UUID, PlayerData> kvp : mMembers.entrySet())
			{
				if(WarForgeMod.MC_SERVER.getPlayerList().getPlayerByUUID(kvp.getKey()) != null)
					mHasHadAnyLoginsToday = true;
			}
		}
	}
	
	public void AdvanceDay()
	{
		if(mHasHadAnyLoginsToday)
		{
			mLegacy++;
		}
		
		mHasHadAnyLoginsToday = false;
	}
	
	public FactionDisplayInfo CreateInfo()
	{
		FactionDisplayInfo info = new FactionDisplayInfo();
		info.mFactionID = mUUID;
		info.mFactionName = mName;
		info.mNotoriety = mNotoriety;
		info.mWealth = mWealth;
		info.mLegacy = mLegacy;
		
		info.mLegacyRank = WarForgeMod.sLeaderboard.GetOneIndexedRankOf(this, FactionStat.LEGACY);
		info.mNotorietyRank = WarForgeMod.sLeaderboard.GetOneIndexedRankOf(this, FactionStat.NOTORIETY);
		info.mWealthRank = WarForgeMod.sLeaderboard.GetOneIndexedRankOf(this, FactionStat.WEALTH);
		info.mTotalRank = WarForgeMod.sLeaderboard.GetOneIndexedRankOf(this, FactionStat.TOTAL);
		
		info.mNumClaims = mClaims.size();
		info.mCitadelPos = mCitadelPos;
		
		for(HashMap.Entry<UUID, PlayerData> entry : mMembers.entrySet())
		{
			if(entry.getValue().mRole == Role.LEADER)
				info.mLeaderID = entry.getKey();
			
			PlayerDisplayInfo playerInfo = new PlayerDisplayInfo();
			GameProfile	profile = WarForgeMod.MC_SERVER.getPlayerProfileCache().getProfileByUUID(entry.getKey());
			playerInfo.mPlayerName = profile == null ? "Unknown Player" : profile.getName();
			playerInfo.mPlayerUUID = entry.getKey();
			playerInfo.mRole = entry.getValue().mRole;
			info.mMembers.add(playerInfo);
		}
		return info;
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
		MessageAll(new TextComponentString(GetPlayerName(playerID) + " joined " + mName));
		
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
		

		MessageAll(new TextComponentString(GetPlayerName(playerID) + " was made leader of " + mName));
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
	
	public boolean Disband()
	{
		MessageAll(new TextComponentString(mName + " was disbanded."));
		mMembers.clear();
		mClaims.clear();
		mPendingInvites.clear();
		
		return true;
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
	
	public void OnClaimPlaced(IClaim claim) 
	{
		mClaims.put(claim.GetPos(), 0);
	}
	
	public void OnSiegeCreated(TileEntitySiegeCamp campTE, DimChunkPos attackedChunk) 
	{
		// TODO
	}
	
	public void OnSiegeReceived(TileEntitySiegeCamp campTE, DimChunkPos attackedChunk) 
	{
		// TODO
	}
	
	public void OnClaimLost(DimBlockPos claimBlockPos) 
	{
		// Destroy our claim block
		WarForgeMod.MC_SERVER.getWorld(claimBlockPos.mDim).setBlockToAir(claimBlockPos);
		
		MessageAll(new TextComponentString("Our faction lost a claim at " + claimBlockPos.ToFancyString()));
		
		mClaims.remove(claimBlockPos);
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
	
	public DimBlockPos GetSpecificPosForClaim(DimChunkPos pos) 
	{
		for(DimBlockPos claimPos : mClaims.keySet())
		{
			if(claimPos.ToChunkPos().equals(pos))
				return claimPos;
		}
		return null;
	}
	
	public void EvaluateVault() 
	{
		World world = WarForgeMod.MC_SERVER.getWorld(mCitadelPos.mDim);
		DimChunkPos chunkPos = mCitadelPos.ToChunkPos();
		
		int count = 0;
		if(world != null)
		{
			for(int i = 0; i < 16; i++)
			{
				for(int k = 0; k < 16; k++)
				{
					for(int j = 0; j < 256; j++)
					{
						BlockPos blockPos = chunkPos.getBlock(i, j, k);
						IBlockState state = world.getBlockState(blockPos);
						if(WarForgeMod.VAULT_BLOCKS.contains(state.getBlock()))
							count++;
					}
				}
			}
		}
		
		mWealth = count;
	}

	public void AwardYields() 
	{
		// 
		for(HashMap.Entry<DimBlockPos, Integer> kvp : mClaims.entrySet())
		{
			DimBlockPos pos = kvp.getKey();
			World world = WarForgeMod.MC_SERVER.getWorld(pos.mDim);
			
			// If its loaded, process immediately
			if(world.isBlockLoaded(pos))
			{
				TileEntity te = world.getTileEntity(pos.ToRegularPos());
				if(te instanceof TileEntityYieldCollector)
				{
					((TileEntityYieldCollector)te).ProcessYield(1);
				}
			}
			// Otherwise, cache the number of times it needs to process when it next loads
			else
			{
				kvp.setValue(kvp.getValue() + 1);
			}
		}
	}
	
	public void ReadFromNBT(NBTTagCompound tags)
	{
		mClaims.clear();
		mMembers.clear();
		
		// Get citadel pos and defining params
		mUUID = tags.getUniqueId("uuid");
		mName = tags.getString("name");
		
		// Get our claims and citadel
		mCitadelPos = DimBlockPos.ReadFromNBT(tags, "citadelPos");
		
		
		NBTTagList claimList = tags.getTagList("claims", 10); // CompoundTag (see NBTBase.class)
		if(claimList != null)
		{
			for(NBTBase base : claimList)
			{
				NBTTagCompound claimInfo = (NBTTagCompound)base;
				DimBlockPos pos = DimBlockPos.ReadFromNBT((NBTTagIntArray)claimInfo.getTag("pos"));
				int pendingYields = claimInfo.getInteger("pendingYields");
				mClaims.put(pos, pendingYields);
			}
		}
		if(!mClaims.containsKey(mCitadelPos))
		{
			WarForgeMod.sLogger.error("Citadel was not claimed by the faction. Forcing claim");
			mClaims.put(mCitadelPos, 0);
		}

		
		// Get gameplay params
		mNotoriety = tags.getInteger("notoriety");
		mWealth = tags.getInteger("wealth");
		mLegacy = tags.getInteger("legacy");

		// Get member data
		NBTTagList memberList = tags.getTagList("members", 10); // NBTTagCompound (see NBTBase.class)
		if(memberList != null)
		{
			for(NBTBase base : memberList)
			{
				NBTTagCompound memberTags = (NBTTagCompound)base;
				UUID uuid = memberTags.getUniqueId("uuid");
				PlayerData data = new PlayerData();
				data.ReadFromNBT(memberTags);
				mMembers.put(uuid, data);
			}
		}
	}
	
	public void WriteToNBT(NBTTagCompound tags)
	{
		// Set citadel pos and core params
		tags.setUniqueId("uuid", mUUID);
		tags.setString("name", mName);
		
		// Set claims
		NBTTagList claimsList = new NBTTagList();
		for(HashMap.Entry<DimBlockPos, Integer> kvp : mClaims.entrySet())
		{
			NBTTagCompound claimTags = new NBTTagCompound();
			claimTags.setTag("pos", kvp.getKey().WriteToNBT());
			claimTags.setInteger("pendingYields", kvp.getValue());
			
			claimsList.appendTag(claimTags);
		}
		tags.setTag("claims", claimsList);
		mCitadelPos.WriteToNBT(tags, "citadelPos");
		
		// Set gameplay params
		tags.setInteger("notoriety", mNotoriety);
		tags.setInteger("wealth", mWealth);
		tags.setInteger("legacy", mLegacy);
		
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
		return WarForgeMod.MC_SERVER.getPlayerList().getPlayerByUUID(playerID);
	}
}
