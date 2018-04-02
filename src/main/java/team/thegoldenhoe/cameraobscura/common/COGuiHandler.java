package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import team.thegoldenhoe.cameraobscura.client.gui.GuiCamera;

public class COGuiHandler implements IGuiHandler {

	public COGuiHandler() {

	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 0) {
			EnumHand hand = EnumHand.values()[x];
			ItemStack held = player.getHeldItem(hand);

			if (held != null && held.getItem() instanceof ItemCamera) {
				return new ContainerCamera(player.inventory, held.getCapability(CameraCapabilities.getCameraCapability(), null), hand);
			}
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 0) {
			EnumHand hand = EnumHand.values()[x];
			ItemStack held = player.getHeldItem(hand);

			if (held != null && held.getItem() instanceof ItemCamera) {
				return new GuiCamera(new ContainerCamera(player.inventory, held.getCapability(CameraCapabilities.getCameraCapability(), null), hand));
			}
		}
		return null;
	}

}
