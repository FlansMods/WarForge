package com.flansmod.warforge.client;

import com.flansmod.warforge.common.CommonProxy;
import com.flansmod.warforge.common.ContainerCitadel;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiCitadel extends GuiContainer
{	
	private static final ResourceLocation texture = new ResourceLocation(WarForgeMod.MODID, "gui/citadelmenu.png");

	private static final int BUTTON_INFO = 0;
	private static final int BUTTON_DISBAND = 1;
	private static final int BUTTON_CREATE = 2;
	
	public ContainerCitadel citadelContainer;
	
	public GuiCitadel(Container container) 
	{
		super(container);
		citadelContainer = (ContainerCitadel)container;
		
		ySize = 182;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		Faction faction = WarForgeMod.INSTANCE.GetFaction(citadelContainer.citadel.GetFactionID());
		
		//Create button
		GuiButton createButton = new GuiButton(BUTTON_CREATE, width / 2 - 20, height / 2 - 70, 100, 20, "Create");
		createButton.enabled = faction == null;
		createButton.visible = faction == null;
		buttonList.add(createButton);
		
		//Info Button
		GuiButton infoButton = new GuiButton(BUTTON_INFO, width / 2 - 20, height / 2 - 48, 100, 20, "Info");
		infoButton.enabled = faction != null;
		buttonList.add(infoButton);
		
		//Disband button
		GuiButton disbandButton = new GuiButton(BUTTON_DISBAND, width / 2 - 20, height / 2 - 70, 100, 20, "Disband");
		disbandButton.enabled = faction != null;
		disbandButton.visible = faction != null;
		buttonList.add(disbandButton);
	}
	

	@Override
	protected void actionPerformed(GuiButton button)
	{
		switch(button.id)
		{
			case BUTTON_CREATE:
			{
				// Open creation GUI
				mc.player.openGui(
						WarForgeMod.INSTANCE, 
						CommonProxy.GUI_TYPE_CREATE_FACTION, 
						mc.world, 
						citadelContainer.citadel.getPos().getX(),
						citadelContainer.citadel.getPos().getY(),
						citadelContainer.citadel.getPos().getZ());
				
				break;
			}
			case BUTTON_INFO:
			{
				// Open info GUI
				break;
			}
			case BUTTON_DISBAND:
			{
				// Open disband GUI
				break;
			}
		}	
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		super.drawGuiContainerForegroundLayer(x, y);
		
		Faction faction = WarForgeMod.INSTANCE.GetFaction(citadelContainer.citadel.GetFactionID());
		
		if(faction == null)
		{
			fontRenderer.drawString("Unclaimed Citadel", 6, 6, 0x404040);
			
		}
		else
		{
			fontRenderer.drawString("Citadel of " + faction.mName, 6, 6, 0x404040);
		}
		
		fontRenderer.drawString("Yields", 6, 20, 0x404040);
		fontRenderer.drawString("Banner:", 148 - fontRenderer.getStringWidth("Banner:"), 72, 0x404040);
		
		fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);

	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}
