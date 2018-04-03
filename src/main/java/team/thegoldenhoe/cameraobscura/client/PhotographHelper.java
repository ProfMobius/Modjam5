package team.thegoldenhoe.cameraobscura.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ScreenShotHelper;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.common.CameraCapabilities;
import team.thegoldenhoe.cameraobscura.common.ICameraNBT;
import team.thegoldenhoe.cameraobscura.common.ItemProps;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.common.network.CameraTypes;
import team.thegoldenhoe.cameraobscura.common.network.MessagePhotoDataToServer;
import team.thegoldenhoe.cameraobscura.common.network.PhotoDataHandler;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

public class PhotographHelper {

	/**
	 * Captures a screenshot (with GUI hidden) and saves it to the server's screenshots folder
	 */
	public static void capturePhotograph() {
		try {
			Minecraft mc = Minecraft.getMinecraft();
			ItemStack stack = mc.player.getHeldItemMainhand();
			if (stack.isEmpty() || !(stack.getItem() instanceof ItemProps)) {
				stack = mc.player.getHeldItemOffhand();
				if (stack.isEmpty() || !(stack.getItem() instanceof ItemProps)) {
					System.err.println("CAMERA FAIL!");
					return;
				}
			}

			List<PhotoFilter> filters = new ArrayList<PhotoFilter>(2);
			CSModelMetadata data = ModelHandler.getModelFromStack(stack);
			CameraTypes type = data.getCameraType();
			ICameraNBT cameraCap = stack.getCapability(CameraCapabilities.getCameraCapability(), null);
			if (cameraCap != null) {
				// Pretty ghetto but for modjam just check camera types and choose filters
				// accordingly. Eventually we should move this all over to capabilities.
				if (type == CameraTypes.DIGITAL) {
					Pair<PhotoFilter, PhotoFilter> filterPair = cameraCap.getFilters();
					if (filterPair.getLeft() != null) {
						filters.add(filterPair.getLeft());
					}
					if (filterPair.getRight() != null) {
						filters.add(filterPair.getRight());
					}	
				} else if (type == CameraTypes.VINTAGE) {
					filters.add(PhotoFilters.BLACK_AND_WHITE);
				} else if (type == CameraTypes.POLAROID) {
					filters.add(PhotoFilters.VINTAGE);
				}
			}

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
				stack = mc.player.getHeldItemMainhand();
				if (stack.isEmpty() || !(stack.getItem() instanceof ItemProps)) {
					stack = mc.player.getHeldItemOffhand();
					if (stack.isEmpty() || !(stack.getItem() instanceof ItemProps)) {
						System.err.println("CAMERA FAIL!");
						return;
					}
				}

				MessagePhotoDataToServer msg = new MessagePhotoDataToServer(uuid, "test", subImageBytes, order, imageBytes.length, mc.player.getUniqueID());
				CONetworkHandler.NETWORK.sendToServer(msg);
				order++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
