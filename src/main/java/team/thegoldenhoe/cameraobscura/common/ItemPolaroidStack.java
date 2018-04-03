package team.thegoldenhoe.cameraobscura.common;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
		if (stack.getTagCompound() == null) {
			//return;
		} else {
			NBTTagList paths = stack.getTagCompound().getTagList("Paths", 10);
			System.out.println("Found some nbt on the item:" + paths.tagCount());
		}

		ICameraStorageNBT.PolaroidStackStorage storage = stack.getCapability(CameraCapabilities.getPolaroidStackCapability(), null);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return CameraCapabilities.getProvider(CameraCapabilities.getPolaroidStackCapability(), () -> {
			PolaroidStackStorage ret = new PolaroidStackStorage() {
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
