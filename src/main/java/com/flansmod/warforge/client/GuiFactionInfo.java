package com.flansmod.warforge.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.network.FactionDisplayInfo;
import com.flansmod.warforge.common.network.PacketFactionInfo;
import com.flansmod.warforge.common.network.PlayerDisplayInfo;
import com.flansmod.warforge.server.Faction;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;

public class GuiFactionInfo extends GuiScreen
{
	private static final ResourceLocation texture = new ResourceLocation(WarForgeMod.MODID, "gui/factioninfo.png");
	private static HashMap<String, ResourceLocation> sSkinCache = new HashMap<String, ResourceLocation>();
	
	private int xSize, ySize;
	private FactionDisplayInfo info;
	
	public GuiFactionInfo()
	{
		info = PacketFactionInfo.sLatestInfo;
    	xSize = 176;
    	ySize = 256;
	}
		
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		
		// Draw background
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		
		// Then draw overlay
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		fontRenderer.drawStringWithShadow(info.mFactionName, j + xSize / 2 - fontRenderer.getStringWidth(info.mFactionName) * 0.5f, k + 13, 0xffffff);
			
		// Render user info, and look out for leader info while we there
		int rowLength = 7;
		int columnHeight = 2;
		PlayerDisplayInfo leaderInfo = null; 
		
		for(int y = 0; y < columnHeight; y++)
		{
			for(int x = 0; x < rowLength; x++)
			{
				int index = x + y * rowLength;
				if(index < info.mMembers.size())
				{
					if(leaderInfo != null)
					{
						index--;
					}
					
					PlayerDisplayInfo playerInfo = info.mMembers.get(index);
					// If this is the leader, skip them and cache to put at top
					if(leaderInfo == null)
					{
						if(playerInfo.mRole == Faction.Role.LEADER || playerInfo.mPlayerUUID.equals(info.mLeaderID))
						{
							leaderInfo = playerInfo;
							continue;
						}
					}
					
					// Bind our texture, render a background
					mc.renderEngine.bindTexture(texture);
					drawTexturedModalRect(j + 5 + 24 * x, k + 71 + 24 * y, playerInfo.mRole == Faction.Role.OFFICER ? 176 : 198, 0, 22, 22);
					
					// Then bind their face and render that
					RenderPlayerFace(j + 8 + 24 * x, k + 74 + 24 * y, playerInfo.mPlayerName);
				}
			}
		}
		
		if(leaderInfo != null)
		{
			fontRenderer.drawStringWithShadow("Leader", j + 56, k + 31, 0xffffff);
			fontRenderer.drawStringWithShadow(leaderInfo.mPlayerName, j + 56, k + 42, 0xffffff);
			RenderPlayerFace(j + 34, k + 32, leaderInfo.mPlayerName);
		}
		
	}
	
	private void RenderPlayerFace(int x, int y, String username)
	{
		if(!sSkinCache.containsKey(username))
		{
			// Then bind their face and render that
			ResourceLocation skin = DefaultPlayerSkin.getDefaultSkinLegacy();
			GameProfile profile = TileEntitySkull.updateGameprofile(new GameProfile((UUID)null, username));
	        if (profile != null)
	        {
	            Minecraft minecraft = Minecraft.getMinecraft();
	            Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);
	
	            if (map.containsKey(Type.SKIN))
	            {
	            	skin = minecraft.getSkinManager().loadSkin(map.get(Type.SKIN), Type.SKIN);
	            }
	            else
	            {
	                UUID uuid = EntityPlayer.getUUID(profile);
	                skin = DefaultPlayerSkin.getDefaultSkin(uuid);
	            }
	        }
	        sSkinCache.put(username, skin);
		}
        
		if(sSkinCache.containsKey(username))
		{
	        mc.renderEngine.bindTexture(sSkinCache.get(username));
	        drawModalRectWithCustomSizedTexture(x, y, 16, 16, 16, 16, 128, 128);
		}
	}	
}
