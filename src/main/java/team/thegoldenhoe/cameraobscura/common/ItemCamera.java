package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.common.network.MessagePhotoData;

public class ItemCamera extends Item {

	public ItemCamera() {

	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (world.isRemote) {
			CONetworkHandler.NETWORK.sendToServer(new MessagePhotoData("photo", new byte[] {1}));	
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

}
