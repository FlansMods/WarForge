package com.flansmod.warforge.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.flansmod.warforge.common.WarForgeMod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
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
			faction = WarForgeMod.INSTANCE.GetFactionOfPlayer(((EntityPlayer)sender).getUniqueID());
		}
		
		// Argument 0 is subcommand
		switch(args[0].toLowerCase())
		{
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
					faction = WarForgeMod.INSTANCE.GetFaction(args[2]);
					if(faction != null)
						WarForgeMod.INSTANCE.RequestInvitePlayerToFaction(sender, faction.mUUID, invitee.getUniqueID());
					else 
						sender.sendMessage(new TextComponentString("Could not find faction " + args[2]));
					
					break;
				}
				
				// Any other case, we assume players can only invite to their own faction
				if(sender instanceof EntityPlayer)
				{
					WarForgeMod.INSTANCE.RequestInvitePlayerToMyFaction((EntityPlayer)sender, invitee.getUniqueID());
				}	

				break;
			}
			case "accept":
			{
				if(sender instanceof EntityPlayer)
				{
					WarForgeMod.INSTANCE.RequestAcceptInvite((EntityPlayer)sender);
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
					WarForgeMod.INSTANCE.RequestDisbandFaction((EntityPlayer)sender, faction.mUUID);
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
							faction = WarForgeMod.INSTANCE.GetFactionOfPlayer(toRemoveID);
						}
						
						WarForgeMod.INSTANCE.RequestRemovePlayerFromFaction(sender, faction.mUUID, toRemoveID);
					}
				}
				break;
			}
			case "leave":
			case "exit":
			{
				if(sender instanceof EntityPlayer)
				{
					WarForgeMod.INSTANCE.RequestRemovePlayerFromFaction(sender, faction.mUUID, ((EntityPlayer) sender).getUniqueID());
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
			
			default:
			{
				break;
			}
		}
	
	}

}
