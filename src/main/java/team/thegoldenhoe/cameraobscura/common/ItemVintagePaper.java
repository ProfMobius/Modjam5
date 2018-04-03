package team.thegoldenhoe.cameraobscura.common;

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
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT.VintageStorage;

public class ItemVintagePaper extends Item {

	public ItemVintagePaper() {
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
		if (stack.getTagCompound() == null) {
			tooltip.add("Empty");
			tooltip.add(TextFormatting.DARK_PURPLE.toString() + TextFormatting.ITALIC + "Usable in vintage camera");
			return;
		}

		if (stack.getTagCompound().hasKey("Photo")) {
			tooltip.add(TextFormatting.ITALIC + "Contains photo");
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return CameraCapabilities.getProvider(CameraCapabilities.getVintageStorageCapability(), () -> {
			VintageStorage ret = new VintageStorage() {

				@Override
				public void saveImage(String path, EntityPlayer player) {
					super.saveImage(path, player);
					stack.setTagCompound(serializeNBT());

					if (!player.world.isRemote) {
						stack.getTagCompound().setString("Photo", path);
					}
				}
			};

			if (stack.hasTagCompound()) {
				ret.deserializeNBT(stack.getTagCompound());
				// If a photo is already saved, update nbt to reflect that.
				// Prevents dupe saving.
				if (stack.getTagCompound().hasKey("Photo")) {
					String path = stack.getTagCompound().getString("Photo");
					ret.getSavedImagePaths().add(path);
					ret.serializeNBT();
				}
			}
			return ret;
		});
	}

}
