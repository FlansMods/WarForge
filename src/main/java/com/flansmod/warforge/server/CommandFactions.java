package com.flansmod.warforge.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.DimChunkPos;
import com.flansmod.warforge.common.Protections;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.network.LeaderboardInfo;
import com.flansmod.warforge.common.network.PacketFactionInfo;
import com.flansmod.warforge.common.network.PacketLeaderboardInfo;
import com.flansmod.warforge.server.Leaderboard.FactionStat;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandFactions extends CommandBase
{
	private static List<String> ALIASES;
	static
	{
		ALIASES = new ArrayList<String>(4);
		ALIASES.add("f");
		ALIASES.add("factions");
		ALIASES.add("war");
		ALIASES.add("warforge");
	}
	
	@Override
	public String getName() { return "faction"; }
	@Override
    public List<String> getAliases() {  return ALIASES;  }

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return null;
	}
	
	@Override 
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }
	
	private static final String[] tabCompletions = new String[] { 
			"invite", "accept", "disband", "expel", "leave", "time", "info", "top", "notoriety", "wealth", "legacy",
	};
	
	private static final String[] tabCompletionsOp = new String[] { 
			"invite", "accept", "disband", "expel", "leave", "time", "info", "top", "notoriety", "wealth", "legacy",
			"safe", "war", "protection",
	};
	
	@Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
        {
        	return getListOfStringsMatchingLastWord(args, tabCompletions);
        }
        
        if(args.length == 2)
        {
        	switch(args[0])
        	{
        		case "info": 
        			return getListOfStringsMatchingLastWord(args, WarForgeMod.FACTIONS.GetFactionNames());
        		case "invite":
        		case "expel":
        			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        		default: 
        			return getListOfStringsMatchingLastWord(args, new String[0]);
        	}
        }
        
        return getListOfStringsMatchingLastWord(args, new String[0]);
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(args.length == 0)
		{
			sender.sendMessage(new TextComponentString("Try /f help"));
			return;
		}
		
		Faction faction = null;
		if(sender instanceof EntityPlayer)
		{
			faction = WarForgeMod.FACTIONS.GetFactionOfPlayer(((EntityPlayer)sender).getUniqueID());
		}
		
		// Argument 0 is subcommand
		switch(args[0].toLowerCase())
		{
			case "help":
			{
				sender.sendMessage(new TextComponentString("/f invite <playerName>"));
				sender.sendMessage(new TextComponentString("/f accept"));
				sender.sendMessage(new TextComponentString("/f disband"));
				sender.sendMessage(new TextComponentString("/f expel <playerName>"));
				sender.sendMessage(new TextComponentString("/f leave"));
				sender.sendMessage(new TextComponentString("/f time"));
				sender.sendMessage(new TextComponentString("/f info <factionName>"));
				sender.sendMessage(new TextComponentString("/f top"));
				sender.sendMessage(new TextComponentString("/f wealth"));
				sender.sendMessage(new TextComponentString("/f legacy"));
				sender.sendMessage(new TextComponentString("/f notoriety"));
				
				if(WarForgeMod.IsOp(sender))
				{
					sender.sendMessage(new TextComponentString("/f safezone"));
					sender.sendMessage(new TextComponentString("/f warzone"));
				}
				
				break;
			}
		
			case "create":
			{
				sender.sendMessage(new TextComponentString("Craft a Citadel to create a faction"));
				break;
			}
			case "invite":
			{
				// Argument 1 is the player to invite
				if(args.length < 2)
				{
					sender.sendMessage(new TextComponentString("Invalid arguments, please specify player name"));
					break;
				}
				
				EntityPlayer invitee = server.getPlayerList().getPlayerByUsername(args[1]);
				if(invitee == null)
				{
					sender.sendMessage(new TextComponentString("Could not find player " + args[1]));
					break;
				}
				
				// First, resolve the op version where we can specify the faction
				if(args.length >= 3 && WarForgeMod.IsOp(sender))
				{
					faction = WarForgeMod.FACTIONS.GetFaction(args[2]);
					if(faction != null)
						WarForgeMod.FACTIONS.RequestInvitePlayerToFaction(sender, faction.mUUID, invitee.getUniqueID());
					else 
						sender.sendMessage(new TextComponentString("Could not find faction " + args[2]));
					
					break;
				}
				
				// Any other case, we assume players can only invite to their own faction
				if(sender instanceof EntityPlayer)
				{
					WarForgeMod.FACTIONS.RequestInvitePlayerToMyFaction((EntityPlayer)sender, invitee.getUniqueID());
				}	

				break;
			}
			case "accept":
			{
				if(sender instanceof EntityPlayer)
				{
					WarForgeMod.FACTIONS.RequestAcceptInvite((EntityPlayer)sender);
				}
				else
				{
					sender.sendMessage(new TextComponentString("The server can't accept a faction invite"));
				}
				break;
			}
			case "disband":
			{
				if(sender instanceof EntityPlayer && faction != null)
				{
					WarForgeMod.FACTIONS.RequestDisbandFaction((EntityPlayer)sender, faction.mUUID);
				}
				// TODO: Op case
				break;
			}
			case "expel":
			case "remove":
			{
				if(args.length >= 2)
				{
					EntityPlayer toRemove = server.getPlayerList().getPlayerByUsername(args[1]);
					if(toRemove != null)
					{
						UUID toRemoveID =  toRemove.getUniqueID();
						
						if(faction == null)
						{
							faction = WarForgeMod.FACTIONS.GetFactionOfPlayer(toRemoveID);
						}
						
						WarForgeMod.FACTIONS.RequestRemovePlayerFromFaction(sender, faction.mUUID, toRemoveID);
					}
				}
				break;
			}
			case "leave":
			case "exit":
			{
				if(sender instanceof EntityPlayer)
				{
					WarForgeMod.FACTIONS.RequestRemovePlayerFromFaction(sender, faction.mUUID, ((EntityPlayer) sender).getUniqueID());
				}
				break;
			}
			case "time":
			{
				long day = WarForgeMod.INSTANCE.numberOfSiegeDaysTicked;
				long ms = WarForgeMod.INSTANCE.GetMSToNextYield();
				long s = ms / 1000;
				long m = s / 60;
				long h = m / 60;
				long d = h / 24;
				
				sender.sendMessage(new TextComponentString("Yields will next be awarded in " + (d) + " days, " + (h % 24) + ":" + (m % 60) + ":" + (s % 60)));
				
				ms = WarForgeMod.INSTANCE.GetMSToNextSiegeAdvance();
				s = ms / 1000;
				m = s / 60;
				h = m / 60;
				d = h / 24;
				
				sender.sendMessage(new TextComponentString("Sieges will progress in " + (d) + " days, " + (h % 24) + ":" + (m % 60) + ":" + (s % 60)));
				break;
			}
			
			// Faction info
			case "info":
			{
				if(sender instanceof EntityPlayerMP)
				{
					Faction factionToSend = null;
					if(args.length >= 2)
					{
						factionToSend = WarForgeMod.FACTIONS.GetFaction(args[1]);
					}
					if(factionToSend == null)
					{
						factionToSend = WarForgeMod.FACTIONS.GetFactionOfPlayer(((EntityPlayerMP)sender).getUniqueID());
					}
					if(factionToSend == null)
					{
						sender.sendMessage(new TextComponentString("Could not find that faction"));
					}
					else
					{
						PacketFactionInfo packet = new PacketFactionInfo();
						packet.mInfo = factionToSend.CreateInfo();
						WarForgeMod.NETWORK.sendTo(packet, (EntityPlayerMP)sender);
					}
				}
				break;
			}
			
			// Leaderboards
			case "top":
			{
				if(sender instanceof EntityPlayerMP)
				{
					UUID uuid = ((EntityPlayerMP)sender).getUniqueID();
					PacketLeaderboardInfo packet = new PacketLeaderboardInfo();
					packet.mInfo = WarForgeMod.LEADERBOARD.CreateInfo(0, FactionStat.TOTAL, uuid);
					WarForgeMod.NETWORK.sendTo(packet, (EntityPlayerMP)sender);
				}
				break;
			}
			case "wealth":
			case "wealthtop":
			case "bal":
			case "baltop":
			{
				if(sender instanceof EntityPlayerMP)
				{
					UUID uuid = ((EntityPlayerMP)sender).getUniqueID();
					PacketLeaderboardInfo packet = new PacketLeaderboardInfo();
					packet.mInfo = WarForgeMod.LEADERBOARD.CreateInfo(0, FactionStat.WEALTH, uuid);
					WarForgeMod.NETWORK.sendTo(packet, (EntityPlayerMP)sender);
				}
				break;
			}
			case "notoriety":
			case "notorietytop":
			case "pvp":
			case "pvptop":
			{
				if(sender instanceof EntityPlayerMP)
				{
					UUID uuid = ((EntityPlayerMP)sender).getUniqueID();
					PacketLeaderboardInfo packet = new PacketLeaderboardInfo();
					packet.mInfo = WarForgeMod.LEADERBOARD.CreateInfo(0, FactionStat.NOTORIETY, uuid);
					WarForgeMod.NETWORK.sendTo(packet, (EntityPlayerMP)sender);
				}
				break;
			}
			case "legacy":
			case "legacytop":
			case "playtime":
			case "playtimetop":
			{
				if(sender instanceof EntityPlayerMP)
				{
					UUID uuid = ((EntityPlayerMP)sender).getUniqueID();
					PacketLeaderboardInfo packet = new PacketLeaderboardInfo();
					packet.mInfo = WarForgeMod.LEADERBOARD.CreateInfo(0, FactionStat.LEGACY, uuid);
					WarForgeMod.NETWORK.sendTo(packet, (EntityPlayerMP)sender);
				}
				break;
			}
			case "safe":
			case "safezone":
			case "claimsafe":
			{
				if(WarForgeMod.IsOp(sender))
				{
					if(sender instanceof EntityPlayer)
					{
						EntityPlayer player = (EntityPlayer)sender;
						DimChunkPos pos = new DimBlockPos(player.dimension, player.getPosition()).ToChunkPos();
						WarForgeMod.FACTIONS.RequestOpClaim(player, pos, FactionStorage.SAFE_ZONE_ID);
					}
					else
					{
						sender.sendMessage(new TextComponentString("Use an in-game operator account."));
					}
				}
				else
				{
					sender.sendMessage(new TextComponentString("You are not op."));
				}
				break;
			}
			case "warzone":
			case "war":
			case "claimwarzone":
			{
				if(WarForgeMod.IsOp(sender))
				{
					if(sender instanceof EntityPlayer)
					{
						EntityPlayer player = (EntityPlayer)sender;
						DimChunkPos pos = new DimBlockPos(player.dimension, player.getPosition()).ToChunkPos();
						WarForgeMod.FACTIONS.RequestOpClaim(player, pos, FactionStorage.WAR_ZONE_ID);
					}
					else
					{
						sender.sendMessage(new TextComponentString("Use an in-game operator account."));
					}
				}
				else
				{
					sender.sendMessage(new TextComponentString("You are not op."));
				}
				break;
			}
			case "opProtection":
			case "protection":
			case "protectionOverride":
			{
				if(WarForgeMod.IsOp(sender))
				{
					Protections.OP_OVERRIDE = !Protections.OP_OVERRIDE;
					if(Protections.OP_OVERRIDE)
						sender.sendMessage(new TextComponentString("Admins can now build in protected areas."));
					else
						sender.sendMessage(new TextComponentString("Admins can no longer build in protected areas."));
				}
				else
				{
					sender.sendMessage(new TextComponentString("You are not op."));
				}
				break;
			}
			
			default:
			{
				break;
			}
		}
	
	}

}
