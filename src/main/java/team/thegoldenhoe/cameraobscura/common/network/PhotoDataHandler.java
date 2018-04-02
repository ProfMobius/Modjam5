package team.thegoldenhoe.cameraobscura.common.network;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.Utils;
import team.thegoldenhoe.cameraobscura.common.CameraCapabilities;
import team.thegoldenhoe.cameraobscura.common.ICameraNBT;
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT;
import team.thegoldenhoe.cameraobscura.common.ItemProps;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

public class PhotoDataHandler {

	private static int photoID = 0;

	private static Map<Integer, TreeSet<MessagePhotoDataToServer>> messageBuffer = new HashMap<>();

	private static Queue<MessagePhotoDataToServer> messageQueue = new ConcurrentLinkedQueue<>();

	public static void addMessage(int uuid, MessagePhotoDataToServer message) {
		TreeSet<MessagePhotoDataToServer> messageSet = messageBuffer.get(Integer.valueOf(uuid));
		if (messageSet == null) {
			messageSet = new TreeSet<MessagePhotoDataToServer>(MessagePhotoDataToServer.COMPARATOR);
			messageSet.add(message);
			messageBuffer.put(Integer.valueOf(uuid), messageSet);
		} else {
			messageSet.add(message);
		}
	}

	/**
	 * Add a message to the message queue for processing
	 */
	public static void bufferMessage(MessagePhotoDataToServer message) {
		messageQueue.add(message);
	}

	/**
	 * Iterate through all queued messages and buffer them for processing
	 */
	public static synchronized void processMessageQueue() {
		while (!messageQueue.isEmpty()) {
			MessagePhotoDataToServer msg = messageQueue.poll();
			addMessage(msg.uuid, msg);
		}
	}

	/**
	 * Iterate through all buffered messages to check if we have received complete data yet
	 */
	public static void processMessageBuffer(World world) {
		Set<Integer> uuids = messageBuffer.keySet();
		List<Integer> completedUuids = new LinkedList<Integer>();

		for (Integer uuid : uuids) {
			TreeSet<MessagePhotoDataToServer> messages = messageBuffer.get(uuid);
			int bytesReceived = 0;

			// Iterate through all messages received for this uuid
			for (MessagePhotoDataToServer message : messages) {
				bytesReceived += message.data.length;

				// If we have received all necessary data
				if (bytesReceived == message.length) {
					completedUuids.add(uuid);
				}
			}
		}

		// Clear out completed entries from the map
		for (Integer uuid : completedUuids) {
			TreeSet<MessagePhotoDataToServer> messages = messageBuffer.get(uuid);
			byte[] bytes = null;
			ByteBuffer buffer = null;
			UUID photographerUUID = null;
			ItemStack camera = ItemStack.EMPTY;

			// Iterate through all messages received for this uuid
			for (MessagePhotoDataToServer message : messages) {
				if (bytes == null) {
					bytes = new byte[message.length];
					buffer = ByteBuffer.wrap(bytes);
				}
				if (photographerUUID == null) {
					photographerUUID = UUID.fromString(message.playerUUID);
				}
				
				if (camera == null) {
					camera = message.camera;
				}
				
				buffer.put(message.data);
			}

			String savePath = saveImage(createImageFromBytes(bytes));
			
			// TODO: Check type of camera here, decrement film / storage space
			// if digital, save to sd card if present
			// if manual, save to photograph, decrement film level
			if (savePath != null) {
				postImageSaved(world, photographerUUID, camera, savePath);
			} else {
				System.err.println("Save path for image was null. This should never happen, but it did. Look at you, you special person!");
			}
			
			messageBuffer.remove(uuid);
		}
	}
	
	private static void postImageSaved(World world, UUID photographerUUID, ItemStack stack, String savePath) {
		if (photographerUUID == null) {
			throw new NullPointerException("Photographer UUID is null, which means we can't produce an item. Sorry :(");
		}
		
		if (stack.isEmpty()) {
			throw new NullPointerException("Camera is null, which means we don't know how to produce the item properly. Sorry :(");
		}
		
		if (stack.getItem() != null && stack.getItem() instanceof ItemProps) {
			ItemProps camera = (ItemProps)stack.getItem();

			CSModelMetadata data = ModelHandler.getModelFromStack(stack);
			CameraTypes type = data.getCameraType();
			switch (type) {
			case VINTAGE:
				break;
			case POLAROID:
				break;
			case DIGITAL:
				ICameraNBT cap = stack.getCapability(CameraCapabilities.getCameraCapability(), null);
				ItemStack sdCard = cap.getStackInSlot(0);
				ICameraStorageNBT storage = cap.getStorageDevice();
				if (storage.canSave()) {
					storage.saveImage(savePath);
					cap.markDirty();
				} else {
					System.err.println("Somehow between when the picture was taken and saved, the storage device became full. Whoops!");
				}
				break;
			case NOT_A_CAMERA:
				System.err.println("Not sure how we got here, but a non camera was trying to save an image. Whoops!");
				break;
			}
		}
	}

	/**
	 * Takes a BufferedImage and saves it to the photographs folder on the server
	 */
	private static String saveImage(BufferedImage image) {
		try {
			String dirName = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath();
			File directory = new File(dirName, "photographs");
			directory.mkdir();
			File imageFile = Utils.getTimestampedPNGFileForDirectory(directory);
			imageFile = imageFile.getCanonicalFile();
			ImageIO.write(image, "png", imageFile);
			return imageFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static BufferedImage createImageFromBytes(byte[] bytes) {
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		try {
			return ImageIO.read(stream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getUniqueID() {
		return photoID++;
	}
}
