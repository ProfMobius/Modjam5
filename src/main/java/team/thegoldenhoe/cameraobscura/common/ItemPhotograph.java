package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT.VintageStorage;

public class ItemPhotograph extends Item {

	public ItemPhotograph() {
		setMaxStackSize(1);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return CameraCapabilities.getProvider(CameraCapabilities.getVintageStorageCapability(), () -> {
			VintageStorage ret = new VintageStorage() {
				@Override
				public void saveImage(String path) {
					super.saveImage(path);
					stack.setTagCompound(serializeNBT());
				}
			};
			if (stack.hasTagCompound()) {
				ret.deserializeNBT(stack.getTagCompound());
			}
			return ret;
		});
	}

}
