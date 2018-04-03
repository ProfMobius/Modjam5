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
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT.PolaroidStackStorage;

public class ItemPolaroidStack extends Item {

	public ItemPolaroidStack() {
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
		// lil hacky here to print 6 but it's modjam so w/e
		int numPrintsRemaining = 6;
		if (stack.getTagCompound() == null) {
			tooltip.add("Empty");
		}

		ICameraStorageNBT.PolaroidStackStorage storage = stack.getCapability(CameraCapabilities.getPolaroidStackCapability(), null);
		if (storage != null) {
			ArrayList<String> paths = storage.getSavedImagePaths();
			numPrintsRemaining = storage.getMaxSaves() - paths.size();
			tooltip.add(TextFormatting.AQUA.toString() + TextFormatting.BOLD + "Prints Remaining: " + numPrintsRemaining);
			tooltip.add(TextFormatting.DARK_PURPLE.toString() + TextFormatting.ITALIC + "Usable in polaroid camera");

			for (String path : storage.getSavedImagePaths()) {
				tooltip.add(TextFormatting.ITALIC + path.substring(path.lastIndexOf('\\') + 1).trim());
			}	
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return CameraCapabilities.getProvider(CameraCapabilities.getPolaroidStackCapability(), () -> {
			PolaroidStackStorage ret = new PolaroidStackStorage() {
				@Override
				public void saveImage(String path, EntityPlayer player) {
					super.saveImage(path, player);
					stack.setTagCompound(serializeNBT());

					if (!player.world.isRemote) {
						ItemStack polaroidPhoto = new ItemStack(ItemRegistry.polaroidSingle);
						polaroidPhoto.setTagCompound(new NBTTagCompound());
						polaroidPhoto.getTagCompound().setString("Photo", path);

						player.addItemStackToInventory(polaroidPhoto);
					}
				}
			};
			if (stack.hasTagCompound()) {
				ret.deserializeNBT(stack.getTagCompound());
			}
			return ret;
		});
	}

}
