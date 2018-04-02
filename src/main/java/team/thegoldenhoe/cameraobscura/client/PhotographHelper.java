package team.thegoldenhoe.cameraobscura.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ScreenShotHelper;
import team.thegoldenhoe.cameraobscura.common.ItemCamera;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.common.network.MessagePhotoDataToServer;
import team.thegoldenhoe.cameraobscura.common.network.PhotoDataHandler;

public class PhotographHelper {

	/**
	 * Captures a screenshot (with GUI hidden) and saves it to the server's screenshots folder
	 */
	public static void capturePhotograph(PhotoFilter... filters) {
		try {
			Minecraft mc = Minecraft.getMinecraft();
			BufferedImage screenshot = ScreenShotHelper.createScreenshot(mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
			for (PhotoFilter filter : filters) {
				screenshot = filter.getFilteredImage(screenshot);
			}
			
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
				ItemStack stack = mc.player.getHeldItemMainhand();
				if (stack.isEmpty() || !(stack.getItem() instanceof ItemCamera)) {
					stack = mc.player.getHeldItemOffhand();
					if (stack.isEmpty() || !(stack.getItem() instanceof ItemCamera)) {
						System.err.println("CAMERA FAIL!");
						return;
					}
				}
				MessagePhotoDataToServer msg = new MessagePhotoDataToServer(uuid, "test", subImageBytes, order, imageBytes.length, mc.player.getUniqueID(), stack);
				CONetworkHandler.NETWORK.sendToServer(msg);
				order++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
