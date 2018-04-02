package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.client.ClientProxy;

public class ItemCamera extends Item {

	public ItemCamera() {
		setMaxStackSize(1);
	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (hand == EnumHand.OFF_HAND) {
			player.openGui(CameraObscura.instance, 0, world, hand.ordinal(), 0, 0);	
		} else {
			if (world.isRemote) {
				ClientProxy.photographPending = true;
				// Store the setting previous to taking the picture
				ClientProxy.hideGUIDefault = Minecraft.getMinecraft().gameSettings.hideGUI;
				Minecraft.getMinecraft().gameSettings.hideGUI = true;
			}			
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return CameraCapabilities.getProvider(CameraCapabilities.getCameraCapability(), () -> {
            ICameraNBT ret = new ICameraNBT.CameraHandler() {
                @Override
                public void markDirty() {
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
