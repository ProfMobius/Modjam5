package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import team.thegoldenhoe.cameraobscura.client.gui.GuiCamera;
import team.thegoldenhoe.cameraobscura.common.network.CameraTypes;

public class COGuiHandler implements IGuiHandler {

	public COGuiHandler() {

	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == CameraTypes.DIGITAL.getGuiID()) {
			EnumHand hand = EnumHand.values()[x];
			ItemStack held = player.getHeldItem(hand);

			if (!held.isEmpty() && held.getItem() instanceof ItemProps) {
				return new ContainerDigitalCamera(player.inventory, held.getCapability(CameraCapabilities.getCameraCapability(), null), hand);
			}
		} else if (id == CameraTypes.POLAROID.getGuiID()) {
			EnumHand hand = EnumHand.values()[x];
			ItemStack held = player.getHeldItem(hand);

			if (!held.isEmpty() && held.getItem() instanceof ItemProps) {
				return new ContainerPolaroidCamera(player.inventory, held.getCapability(CameraCapabilities.getCameraCapability(), null), hand);
			}
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == CameraTypes.DIGITAL.getGuiID()) {
			EnumHand hand = EnumHand.values()[x];
			ItemStack held = player.getHeldItem(hand);

			if (!held.isEmpty() && held.getItem() instanceof ItemProps) {
				return new GuiCamera(new ContainerDigitalCamera(player.inventory, held.getCapability(CameraCapabilities.getCameraCapability(), null), hand));
			}
		} else if (id == CameraTypes.POLAROID.getGuiID()) {
			EnumHand hand = EnumHand.values()[x];
			ItemStack held = player.getHeldItem(hand);

			if (!held.isEmpty() && held.getItem() instanceof ItemProps) {
				return new GuiCamera(new ContainerPolaroidCamera(player.inventory, held.getCapability(CameraCapabilities.getCameraCapability(), null), hand));
			}
		}
		return null;
	}

}
