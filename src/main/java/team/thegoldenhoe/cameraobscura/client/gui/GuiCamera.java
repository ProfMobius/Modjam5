package team.thegoldenhoe.cameraobscura.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import team.thegoldenhoe.cameraobscura.client.ClientUtils;
import team.thegoldenhoe.cameraobscura.common.container.ICameraContainer;

public class GuiCamera extends GuiContainer {

	private ICameraContainer container;

	public GuiCamera(ICameraContainer inventorySlots) {
		super((Container) inventorySlots);
		this.container = inventorySlots;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		ClientUtils.bindTextureGui(this.container.getContainerBackground());
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

	}

}
