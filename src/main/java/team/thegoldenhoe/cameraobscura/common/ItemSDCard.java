package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemSDCard extends Item {

	public ItemSDCard() {
		super();
		setMaxStackSize(1);
	}
	
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return CameraCapabilities.getProvider(CameraCapabilities.getSDCardCapability(), () -> {
            ISDCardNBT ret = new ISDCardNBT.SDCardHandler() {

            };
            if (stack.hasTagCompound()) {
                ret.deserializeNBT(stack.getTagCompound());
            }
            return ret;
        });
    }

}
