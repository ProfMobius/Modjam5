package team.thegoldenhoe.cameraobscura.common;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT.SDCardStorage;

public class ItemSDCard extends Item {

	public ItemSDCard() {
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
		int numShotsRemaining = SDCardStorage.MAX_SAVES;
		if (stack.getTagCompound() == null) {
			tooltip.add("Empty");
		}

		ICameraStorageNBT.SDCardStorage storage = stack.getCapability(CameraCapabilities.getSDCardStorageCapability(), null);
		if (storage != null) {
			ArrayList<String> paths = storage.getSavedImagePaths();
			numShotsRemaining = storage.getMaxSaves() - paths.size();
			tooltip.add(TextFormatting.AQUA.toString() + TextFormatting.BOLD + "Shots Remaining: " + numShotsRemaining);
			tooltip.add(TextFormatting.WHITE.toString() + TextFormatting.ITALIC + "Usable in digital camera");
		}
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
