package com.flansmod.warforge.client;

import java.io.IOException;

import com.flansmod.warforge.common.DimBlockPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.common.blocks.TileEntityCitadel;
import com.flansmod.warforge.common.network.PacketCreateFaction;
import com.flansmod.warforge.server.Faction;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class GuiCreateFaction extends GuiScreen
{
	private static final ResourceLocation texture = new ResourceLocation(WarForgeMod.MODID, "gui/createfaction.png");

	private static final int BUTTON_CREATE = 0;
	private static final int BUTTON_CANCEL = 1;
	private static final int TEXT_FIELD_NAME = 2;
	
    protected GuiTextField inputField;
    private GuiButton createButton, cancelButton;
	private TileEntityCitadel citadel;
    
    public GuiCreateFaction(TileEntityCitadel tile)
    {
    	citadel = tile;
    }
    
	@Override
	public void initGui()
	{
		super.initGui();
				
		// Create button
		createButton = new GuiButton(BUTTON_CREATE, width / 2 - 42, height / 2 + 2, 40, 20, "Create");
		buttonList.add(createButton);
		
		// Cancel Button
		cancelButton = new GuiButton(BUTTON_CANCEL, width / 2 + 2, height / 2 + 2, 40, 20, "Cancel");
		buttonList.add(cancelButton);
		
		inputField = new GuiTextField(TEXT_FIELD_NAME, fontRenderer, width / 2 - 42, height / 2 - 22, 84, 20);
		inputField.setMaxStringLength(64);
        inputField.setEnableBackgroundDrawing(false);
        inputField.setFocused(true);
        inputField.setText("Faction Name");
        inputField.setCanLoseFocus(false);
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		switch(button.id)
		{
			case BUTTON_CREATE:
			{
				// Send request to server
				PacketCreateFaction packet = new PacketCreateFaction();
				packet.mCitadelPos = new DimBlockPos(citadel.getWorld().provider.getDimension(), citadel.getPos());
				packet.mFactionName = inputField.getText();
				WarForgeMod.INSTANCE.packetHandler.sendToServer(packet);
				mc.displayGuiScreen(null);
				
				break;
			}
			case BUTTON_CANCEL:
			{
				// Just close this GUI, no further action
				mc.displayGuiScreen(null);
				break;
			}
		}	
	}
	
	@Override
    public void updateScreen()
    {
        inputField.updateCursorCounter();
    }
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        inputField.textboxKeyTyped(typedChar, keyCode);

        if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 1)
            {
                actionPerformed(cancelButton);
            }
        }
        else
        {
            actionPerformed(createButton);
        }
    }
}
