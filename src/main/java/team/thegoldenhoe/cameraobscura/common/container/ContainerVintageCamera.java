package team.thegoldenhoe.cameraobscura.common.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandler;
import team.thegoldenhoe.cameraobscura.common.network.CameraTypes;

public class ContainerVintageCamera extends ContainerSingleSlotCamera implements ICameraContainer {

	public ContainerVintageCamera(InventoryPlayer inventory, IItemHandler itemHandler, EnumHand hand, String bgName) {
		super(inventory, itemHandler, hand, bgName, CameraTypes.VINTAGE);
	}
}
