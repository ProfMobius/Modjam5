package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT.SDCardStorage;

public class ItemSDCard extends Item {

	public ItemSDCard() {
		super();
		setMaxStackSize(1);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return CameraCapabilities.getProvider(CameraCapabilities.getSDCardStorageCapability(), () -> {
			SDCardStorage ret = new SDCardStorage() {
				@Override
				public void saveImage(String path, EntityPlayer player) {
					super.saveImage(path, player);
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
