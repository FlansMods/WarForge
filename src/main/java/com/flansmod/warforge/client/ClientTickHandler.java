package com.flansmod.warforge.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.DimChunkPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.IClaim;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.common.network.SiegeCampProgressInfo;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityBannerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientTickHandler 
{
	private Tessellator tess;
	private static final ResourceLocation texture = new ResourceLocation(WarForgeMod.MODID, "world/borders.png");
	private static final ResourceLocation siegeprogress = new ResourceLocation(WarForgeMod.MODID, "gui/siegeprogress.png");
	private final ModelBanner bannerModel = new ModelBanner();
	
	public ClientTickHandler()
	{
		tess = Tessellator.getInstance();
	}
	
	@SubscribeEvent
	public void OnTick(ClientTickEvent tick)
	{
		WarForgeMod.INSTANCE.packetHandler.handleClientPackets();
		ArrayList<DimBlockPos> expired = new ArrayList<DimBlockPos>();
		for(HashMap.Entry<DimBlockPos, SiegeCampProgressInfo> kvp : ClientProxy.sSiegeInfo.entrySet())
		{
			kvp.getValue().ClientTick();
			if(kvp.getValue().Completed())
			{
				expired.add(kvp.getKey());
			}
		}
		
		for(DimBlockPos pos : expired)
		{
			ClientProxy.sSiegeInfo.remove(pos);
		}
			
	}
	
	@SubscribeEvent
	public void OnRenderHUD(RenderGameOverlayEvent event)
	{
		if(event.getType() == ElementType.BOSSHEALTH)
		{
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if(player != null)
			{
				
				SiegeCampProgressInfo infoToRender = null;
				double bestDistanceSq = Double.MAX_VALUE;
				
				for(SiegeCampProgressInfo info : ClientProxy.sSiegeInfo.values())
				{
					double distSq = info.mDefendingPos.distanceSq(player.posX, player.posY, player.posZ);
					if(info.mDefendingPos.mDim == player.dimension 
					&& distSq < WarForgeMod.SIEGE_INFO_RADIUS * WarForgeMod.SIEGE_INFO_RADIUS)
					{
						if(distSq < bestDistanceSq)
						{
							bestDistanceSq = distSq;
							infoToRender = info;
						}
					}
				}
				
				// Render siege overlay
				if(infoToRender != null)
				{
	                float attackR = (float)(infoToRender.mAttackingColour >> 16 & 255) / 255.0F;
	                float attackG = (float)(infoToRender.mAttackingColour >> 8 & 255) / 255.0F;
	                float attackB = (float)(infoToRender.mAttackingColour & 255) / 255.0F;
	                float defendR = (float)(infoToRender.mDefendingColour >> 16 & 255) / 255.0F;
	                float defendG = (float)(infoToRender.mDefendingColour >> 8 & 255) / 255.0F;
	                float defendB = (float)(infoToRender.mDefendingColour & 255) / 255.0F;
					
					// Render background, bars etc
					int xSize = 256;
					int ySize = 49;
					// Anchor point = top middle of screen
					int j = event.getResolution().getScaledWidth() / 2 - xSize / 2;
					int k = 0;
					
					float scroll = mc.getFrameTimer().getIndex() +  + event.getPartialTicks();
					scroll *= 0.25f;
					scroll = scroll % 10;

					mc.renderEngine.bindTexture(siegeprogress);
					
					drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
					
					float siegeLength = infoToRender.mCompletionPoint + 5;
					float barLengthPx = 228;
					float notchDistance = barLengthPx / siegeLength;
					
					// Draw filled bar
					int firstPx = 0;
					int lastPx = 0;
					boolean isIncreasing = infoToRender.mProgress > infoToRender.mPreviousProgress;
					
					if(infoToRender.mProgress > 0)
					{
						firstPx = (int)(notchDistance * 5);
						lastPx = (int)(notchDistance * (infoToRender.mProgress + 5));
					}
					else
					{
						firstPx = (int)(notchDistance * (5 + infoToRender.mProgress));
						lastPx = (int)(notchDistance * 5);
					}
						
					if(isIncreasing)
					{
						GlStateManager.color(attackR, attackG, attackB, 1.0F);
						drawTexturedModalRect(j + 17 + firstPx, k + 24, 17 + (10 - scroll), 51, lastPx - firstPx, 18);
					}
					else 
					{
						GlStateManager.color(defendR, defendG, defendB, 1.0F);
						drawTexturedModalRect(j + 17 + firstPx, k + 24, 17 + scroll, 72, lastPx - firstPx, 18);
					}
					
					GlStateManager.color(1f, 1f, 1f, 1f);
					
					// Draw shield at -5 (successful defence)
					drawTexturedModalRect(j + 6, k + 23, 4, 102, 15, 20);
					
					// Draw notches at each integer interval
					for(int i = -4; i < infoToRender.mCompletionPoint; i++)
					{
						int x = (int)((i + 5) * notchDistance + 17);
						if(i == 0)
							drawTexturedModalRect(j + x - 2, k + 24, 6, 50, 5, 18);
						else 
							drawTexturedModalRect(j + x - 2, k + 24, 1, 50, 4, 18);
					}
					
					// Draw sword at +CompletionPoint (successful attack)
					drawTexturedModalRect(j + 239, k + 19, 238, 98, 13, 25);
					
					// Draw text
					mc.fontRenderer.drawStringWithShadow(infoToRender.mDefendingName, j + 6, k + 6, infoToRender.mAttackingColour);
					mc.fontRenderer.drawStringWithShadow("VS", j + xSize / 2 - mc.fontRenderer.getStringWidth("VS") / 2, k + 6, 0xffffff);
					mc.fontRenderer.drawStringWithShadow(infoToRender.mAttackingName, j + xSize - 6 - mc.fontRenderer.getStringWidth(infoToRender.mAttackingName), k + 6, infoToRender.mDefendingColour);
				}
			}
			
		}
	}
	
	private void drawTexturedModalRect(int x, int y, float u, float v, int w, int h)
	{
		float texScale = 1f / 256f;
		
		tess.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
		
		tess.getBuffer().pos(x, y + h, -90d)		.tex(u * texScale, (v + h) * texScale).endVertex();
		tess.getBuffer().pos(x + w, y + h, -90d)	.tex((u + w) * texScale, (v + h) * texScale).endVertex();
		tess.getBuffer().pos(x + w, y, -90d)		.tex((u + w) * texScale, (v) * texScale).endVertex();
		tess.getBuffer().pos(x, y, -90d)			.tex(u * texScale, (v) * texScale).endVertex();

		tess.draw();
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
			DimChunkPos playerPos = new DimChunkPos(player.dimension, player.getPosition());
			List<DimChunkPos> siegeablePositions = new ArrayList<DimChunkPos>();
			RayTraceResult result = player.rayTrace(10.0f, event.getPartialTicks());
			if(result.typeOfHit == RayTraceResult.Type.BLOCK)
			{
				playerPos = new DimChunkPos(player.dimension, result.getBlockPos());
			}
			
			boolean playerIsInExistingClaim = false;
			
			// Rendering existing claims
			for(TileEntity te : Minecraft.getMinecraft().world.loadedTileEntityList)
			{
				if(te instanceof IClaim)
				{
					DimBlockPos blockPos = ((IClaim) te).GetPos();
					DimChunkPos chunkPos = blockPos.ToChunkPos();
					
					if(playerPos.x == chunkPos.x && playerPos.z == chunkPos.z)
					{
						playerIsInExistingClaim = true;
					}
					if(((IClaim)te).CanBeSieged())	
						siegeablePositions.add(chunkPos);
					
					GlStateManager.pushMatrix();

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
			
			// Player CanPlace? Overlay
			if(player.getHeldItemMainhand().getItem() instanceof ItemBlock)
			{
				boolean shouldRender = false;
				boolean canPlace = false;
				Block holding = ((ItemBlock)player.getHeldItemMainhand().getItem()).getBlock();
				if(holding == WarForgeMod.basicClaimBlock
				|| holding == WarForgeMod.citadelBlock
				|| holding == WarForgeMod.reinforcedClaimBlock)
				{
					shouldRender = true;
					canPlace = !playerIsInExistingClaim;
				}
				if(holding == WarForgeMod.siegeCampBlock)
				{
					shouldRender = true;
					canPlace = false;
					if(!playerIsInExistingClaim)
					{
						for(EnumFacing facing : EnumFacing.HORIZONTALS)
						{
							if(siegeablePositions.contains(playerPos.Offset(facing, 1)))
							{
								canPlace = true;
							}
						}
					}
				}
				
				if(shouldRender)
				{
				
					// Render overlay
					if(canPlace)
						GlStateManager.color(0f, 1f, 0f, 1.0F);
					else
						GlStateManager.color(1f, 0f, 0f, 1.0F);
						
					GlStateManager.translate(playerPos.x * 16 - x, 0 - y, playerPos.z * 16 - z);
					for(int i = 0; i < 16; i++)
					{
						for(int k = 0; k < 16; k++)
						{
							BlockPos pos = new BlockPos(playerPos.x * 16 + i, player.posY, playerPos.z * 16 + k);
							
							//pos = player.world.getHeight(pos);
							for(; pos.getY() > 0 && player.world.isAirBlock(pos); pos = pos.down())
							{ 
							}
							
							tess.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
							
							tess.getBuffer().pos(i + 0, pos.getY() + 1.5d, k + 0).tex(0f, 0f).endVertex();
							tess.getBuffer().pos(i + 1, pos.getY() + 1.5d, k + 0).tex(1f, 0f).endVertex();
							tess.getBuffer().pos(i + 1, pos.getY() + 1.5d, k + 1).tex(1f, 1f).endVertex();
							tess.getBuffer().pos(i + 0, pos.getY() + 1.5d, k + 1).tex(0f, 1f).endVertex();
	
							tess.draw();
						}
					}
				}
			}
			
			// Flag rendering
			
			GlStateManager.disableLighting();
			GlStateManager.disableAlpha();
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.enableCull();
			GlStateManager.color(1f, 1f, 1f);
			
			for(TileEntity te : Minecraft.getMinecraft().world.loadedTileEntityList)
			{
				if(te instanceof TileEntityCitadel)
				{
					TileEntityCitadel citadel = (TileEntityCitadel)te;
					DimBlockPos blockPos = ((IClaim) te).GetPos();
					
					double distance = Math.sqrt((blockPos.getX() - x)*(blockPos.getX() - x)+(blockPos.getY() - y)*(blockPos.getY() - y)+(blockPos.getZ() - z)*(blockPos.getZ() - z));					
					double groundLevelBlend = (skyRenderDistance - distance) / (skyRenderDistance - groundRenderDistance);
					
					if(groundLevelBlend < 0.0d)
						groundLevelBlend = 0.0d;
					
					if(groundLevelBlend > 1.0d)
						groundLevelBlend = 1.0d;
					
					groundLevelBlend = groundLevelBlend * groundLevelBlend * (3 - 2 * groundLevelBlend);
				
					
					ItemStack bannerStack = citadel.getStackInSlot(TileEntityCitadel.BANNER_SLOT_INDEX);
					if(bannerStack.isEmpty())
					{
						
					}
					else if(mBannerTextures.containsKey(bannerStack))
					{
						Minecraft.getMinecraft().renderEngine.bindTexture(mBannerTextures.get(bannerStack));
					}
					else if(bannerStack.getItem() instanceof ItemBanner)
					{
						ItemBanner banner = (ItemBanner)bannerStack.getItem();
					    
			        	// Start with base colour
						EnumDyeColor baseColour = ItemBanner.getBaseColor(bannerStack);
			        	String patternResourceLocation = "b" + baseColour.getDyeDamage();
			        	List<BannerPattern> patternList = Lists.<BannerPattern>newArrayList();
					    List<EnumDyeColor> colorList = Lists.<EnumDyeColor>newArrayList();
					    
		                patternList.add(BannerPattern.BASE);
		                colorList.add(baseColour);
					    
			        	// Then append patterns
				        if (bannerStack.hasTagCompound() && bannerStack.getTagCompound().hasKey("Patterns", 9))
				        {
				        	NBTTagList patterns = bannerStack.getTagCompound().getTagList("Patterns", 10).copy();
			                if (patterns != null)
			                {
			                    for (int p = 0; p < patterns.tagCount(); p++)
			                    {
			                        NBTTagCompound nbttagcompound = patterns.getCompoundTagAt(p);
			                        BannerPattern bannerpattern = BannerPattern.byHash(nbttagcompound.getString("Pattern"));

			                        if (bannerpattern != null)
			                        {
			                            patternList.add(bannerpattern);
			                            int j = nbttagcompound.getInteger("Color");
			                            colorList.add(EnumDyeColor.byDyeDamage(j));
			                            patternResourceLocation = patternResourceLocation + bannerpattern.getHashname() + j;
			                        }
			                    }
			                }
				        }
				        
				        
						ResourceLocation resLoc = BannerTextures.BANNER_DESIGNS.getResourceLocation(patternResourceLocation, patternList, colorList);
						mBannerTextures.put(bannerStack, resLoc);
						Minecraft.getMinecraft().renderEngine.bindTexture(resLoc);
						
						 GlStateManager.pushMatrix();
				            
			            double deltaX = te.getPos().getX() - x;
			            double deltaZ = te.getPos().getZ() - z;
			            
			            float angle = (float)Math.atan2(deltaZ, deltaX) * 180f / (float)Math.PI + 90f;
			            
			            double yPos = te.getPos().getY() + 2d;
			            yPos = 256 + (yPos - 256) * groundLevelBlend;
			            float scale = (float)(1d * groundLevelBlend + 10d * (1d - groundLevelBlend));
			            
			            GlStateManager.translate(0.5d + te.getPos().getX() - x, yPos - y, 0.5d + te.getPos().getZ() - z);
			            GlStateManager.scale(scale, -scale, -scale);
			            GlStateManager.rotate(angle, 0f, 1f, 0f);
			            this.bannerModel.renderBanner();
			            GlStateManager.popMatrix();
					}
				}
			}
		}
		
		//Reset Lighting
	
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		//Pop
		GlStateManager.popMatrix();
	}
	
	private HashMap<ItemStack, ResourceLocation> mBannerTextures = new HashMap<ItemStack, ResourceLocation>();
	
	private void VertexAt(DimChunkPos chunkPos, World world, int x, int z, double groundLevelBlend)
	{
		int maxHeight = world.getHeight(chunkPos.x * 16 + x, chunkPos.z * 16 + z) + 16;

		double height = 256 + (maxHeight - 256) * groundLevelBlend;
		
		tess.getBuffer().pos(x, height, z).tex(z / 16f, x / 16f).endVertex();
	}
}
