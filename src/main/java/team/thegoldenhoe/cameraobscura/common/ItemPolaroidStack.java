package team.thegoldenhoe.cameraobscura.common;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPolaroidStack extends Item {

	public static final int MAX_ENTRIES = 6;

	public ItemPolaroidStack() {
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
		if (stack.getTagCompound() == null) {
			return;
		}

		//NBTTagList ingredients = stack.getTagCompound().getTagList("Ingredients", 10);

	}

}
