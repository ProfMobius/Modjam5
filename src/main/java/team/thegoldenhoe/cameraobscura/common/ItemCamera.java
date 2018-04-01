package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import team.thegoldenhoe.cameraobscura.client.ClientProxy;

public class ItemCamera extends Item {

	public ItemCamera() {

	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (world.isRemote) {
			ClientProxy.photographPending = true;
			// Store the setting previous to taking the picture
			ClientProxy.hideGUIDefault = Minecraft.getMinecraft().gameSettings.hideGUI;
			Minecraft.getMinecraft().gameSettings.hideGUI = true;
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

}
