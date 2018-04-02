package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.client.ClientProxy;
import team.thegoldenhoe.cameraobscura.common.network.CameraTypes;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

public class ItemCamera extends ItemProps {

	public ItemCamera() {
		setMaxStackSize(1);
	}
	
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack stack = player.getHeldItem(hand);

        if (stack.getCount() == 0) {
            return EnumActionResult.FAIL;
        }

        CSModelMetadata data = ModelHandler.getModelFromStack(stack);
        if (data.placeable) {
            return place(player, world, pos, hand, side, hitX, hitY, hitZ);
        } else {
        	takePicOrOpenGui(world, player, hand, data.getCameraType());
        }

        return EnumActionResult.FAIL;
    }

	/**
	 * Called when the equipped item is right clicked.
	 */
	protected void takePicOrOpenGui(World world, EntityPlayer player, EnumHand hand, CameraTypes type) {
		if (hand == EnumHand.OFF_HAND) {
			player.openGui(CameraObscura.instance, 0, world, hand.ordinal(), 0, 0);	
		} else {
			if (world.isRemote) {
				ItemStack stack = player.getHeldItemMainhand();
				switch (type) {
				case VINTAGE:
					// TODO: if camera doesn't have paper, print error message to screen
					break;
				case POLAROID:
					// TODO: if camera doesn't have stack, print error message to screen
					break;
				case DIGITAL:
					ICameraNBT cap = stack.getCapability(CameraCapabilities.getCameraCapability(), null);
					ItemStack sdCard = cap.getStackInSlot(0);
					if (sdCard.isEmpty()) {
						player.sendStatusMessage(new TextComponentString(I18n.format("cameraobscura.chat.missing_sd")), false);
					} else {
						ICameraStorageNBT storage = cap.getSDCard();
						if (storage.canSave()) {
							takePicture();
						} else {
							player.sendStatusMessage(new TextComponentString(I18n.format("cameraobscura.chat.full_sd")), false);
						}
					}
					break;
				case NOT_A_CAMERA:
					System.err.println("Not sure how we got here, but a non camera was trying to save an image. Whoops!");
					return;
				}
			}			
		}
	}
	
	private void takePicture() {
		ClientProxy.photographPending = true;
		// Store the setting previous to taking the picture
		ClientProxy.hideGUIDefault = Minecraft.getMinecraft().gameSettings.hideGUI;
		Minecraft.getMinecraft().gameSettings.hideGUI = true;
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
