package team.thegoldenhoe.cameraobscura.common.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPolaroidSingle extends Item {

	public ItemPolaroidSingle() {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
		if (stack.getTagCompound() == null) {
			tooltip.add("Empty");
			tooltip.add(TextFormatting.AQUA.toString() + TextFormatting.ITALIC + "Usable to craft a polaroid stack");
			return;
		}

		if (stack.getTagCompound().hasKey("Photo")) {
			String path = stack.getTagCompound().getString("Photo");
			tooltip.add(TextFormatting.ITALIC + "Contains Photo");
		}
	}

}
