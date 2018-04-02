package team.thegoldenhoe.cameraobscura.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
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

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.drawString(this.fontRenderer, I18n.format("cameraobscura.gui.sdcard"), 28, 40, 0xffffff);
		this.drawString(this.fontRenderer, I18n.format("cameraobscura.gui.filters"), 97, 40, 0xffffff);
	}

}
