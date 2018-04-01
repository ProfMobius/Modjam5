package team.thegoldenhoe.cameraobscura.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import team.thegoldenhoe.cameraobscura.client.ClientUtils;

public class GuiCamera extends GuiContainer {

	public GuiCamera(Container inventorySlots) {
		super(inventorySlots);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        ClientUtils.bindTextureGui("camera");
        drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

}
