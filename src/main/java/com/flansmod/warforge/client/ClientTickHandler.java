package com.flansmod.warforge.client;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.DimChunkPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.IClaim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class ClientTickHandler 
{
	private Tessellator tess;
	private static final ResourceLocation texture = new ResourceLocation(WarForgeMod.MODID, "world/borders.png");

	public ClientTickHandler()
	{
		tess = Tessellator.getInstance();
	}
	
	@SubscribeEvent
	public void OnTick(ClientTickEvent tick)
	{
		WarForgeMod.INSTANCE.Update();
		WarForgeMod.INSTANCE.packetHandler.handleClientPackets();
	}
	
	@SubscribeEvent
	public void OnRenderLast(RenderWorldLastEvent event)
	{
		//Get the camera frustrum for clipping
		Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
		double x = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.getPartialTicks();
		double y = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.getPartialTicks();
		double z = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.getPartialTicks();
		
		//Push
		GlStateManager.pushMatrix();
		//Setup lighting
		Minecraft.getMinecraft().entityRenderer.enableLightmap();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		double skyRenderDistance = 160d;
		double groundRenderDistance = 128d;
		int resolution = 1;
		
		if(player != null)
		{
			for(TileEntity te : Minecraft.getMinecraft().world.loadedTileEntityList)
			{
				if(te instanceof IClaim)
				{
					GlStateManager.pushMatrix();
					DimBlockPos blockPos = ((IClaim) te).GetPos();
					DimChunkPos chunkPos = blockPos.ToChunkPos();
					
					double distance = Math.sqrt((blockPos.getX() - x)*(blockPos.getX() - x)+(blockPos.getY() - y)*(blockPos.getY() - y)+(blockPos.getZ() - z)*(blockPos.getZ() - z));					
					double groundLevelBlend = (skyRenderDistance - distance) / (skyRenderDistance - groundRenderDistance);
					
					if(groundLevelBlend < 0.0d)
						groundLevelBlend = 0.0d;
					
					if(groundLevelBlend > 1.0d)
						groundLevelBlend = 1.0d;
					
					groundLevelBlend = groundLevelBlend * groundLevelBlend * (3 - 2 * groundLevelBlend);
					
					int i = ((IClaim) te).GetColour();
                    float f = (float)(i >> 16 & 255) / 255.0F;
                    float f1 = (float)(i >> 8 & 255) / 255.0F;
                    float f2 = (float)(i & 255) / 255.0F;
					GlStateManager.color(f, f1, f2, 1.0F);
					GlStateManager.translate(chunkPos.x * 16 - x, 0 - y, chunkPos.z * 16 - z);
					
					tess.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
					
					VertexAt(chunkPos, te.getWorld(), 0, 0, groundLevelBlend);
					VertexAt(chunkPos, te.getWorld(), 16, 0, groundLevelBlend);
					VertexAt(chunkPos, te.getWorld(), 16, 16, groundLevelBlend);
					VertexAt(chunkPos, te.getWorld(), 0, 16, groundLevelBlend);

					tess.draw();
					
					GlStateManager.popMatrix();
				}
			}
		}
		
		//Reset Lighting
	
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		//Pop
		GlStateManager.popMatrix();
	}
	
	private void VertexAt(DimChunkPos chunkPos, World world, int x, int z, double groundLevelBlend)
	{
		int maxHeight = world.getHeight(chunkPos.x * 16 + x, chunkPos.z * 16 + z) + 16;

		double height = 256 + (maxHeight - 256) * groundLevelBlend;
		
		tess.getBuffer().pos(x, height, z).tex(z / 16f, x / 16f).endVertex();
	}
}
