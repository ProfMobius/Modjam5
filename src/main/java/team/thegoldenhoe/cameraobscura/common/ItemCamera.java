package team.thegoldenhoe.cameraobscura.common;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.World;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.common.network.MessagePhotoData;
import team.thegoldenhoe.cameraobscura.common.network.PhotoDataHandler;

public class ItemCamera extends Item {

	public ItemCamera() {

	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (world.isRemote) {
			try {
				Minecraft mc = Minecraft.getMinecraft();
				BufferedImage screenshot = ScreenShotHelper.createScreenshot(mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				ImageIO.write(screenshot, "png", stream);
				byte[] imageBytes = stream.toByteArray();
				ByteBuffer buff = ByteBuffer.wrap(imageBytes);
				short order = 0;
				int bytePacketLen = 30000;
				int uuid = PhotoDataHandler.getUniqueID();

				while (buff.hasRemaining()) {
					byte[] subImageBytes = new byte[buff.remaining() > bytePacketLen ? bytePacketLen : buff.remaining()];
					buff.get(subImageBytes, 0, buff.remaining() > bytePacketLen ? bytePacketLen : buff.remaining());
					MessagePhotoData msg = new MessagePhotoData(uuid, "test", subImageBytes, order, imageBytes.length);
					CONetworkHandler.NETWORK.sendToServer(msg);
					order++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

}
