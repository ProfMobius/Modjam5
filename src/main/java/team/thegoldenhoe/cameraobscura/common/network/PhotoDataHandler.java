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

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import team.thegoldenhoe.cameraobscura.Utils;

public class PhotoDataHandler {

	private static int photoID = 0;

	private static Map<Integer, TreeSet<MessagePhotoData>> messageBuffer = new HashMap<>();

	private static Queue<MessagePhotoData> messageQueue = new ConcurrentLinkedQueue<>();

	public static void addMessage(int uuid, MessagePhotoData message) {
		TreeSet<MessagePhotoData> messageSet = messageBuffer.get(Integer.valueOf(uuid));
		if (messageSet == null) {
			messageSet = new TreeSet<MessagePhotoData>(MessagePhotoData.COMPARATOR);
			messageSet.add(message);
			messageBuffer.put(Integer.valueOf(uuid), messageSet);
		} else {
			messageSet.add(message);
		}
	}

	/**
	 * Add a message to the message queue for processing
	 */
	public static void bufferMessage(MessagePhotoData message) {
		messageQueue.add(message);
	}

	/**
	 * Iterate through all queued messages and buffer them for processing
	 */
	public static synchronized void processMessageQueue() {
		while (!messageQueue.isEmpty()) {
			MessagePhotoData msg = messageQueue.poll();
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
			TreeSet<MessagePhotoData> messages = messageBuffer.get(uuid);
			int bytesReceived = 0;

			// Iterate through all messages received for this uuid
			for (MessagePhotoData message : messages) {
				bytesReceived += message.data.length;

				// If we have received all necessary data
				if (bytesReceived == message.length) {
					completedUuids.add(uuid);
				}
			}
		}

		// Clear out completed entries from the map
		for (Integer uuid : completedUuids) {
			TreeSet<MessagePhotoData> messages = messageBuffer.get(uuid);
			byte[] bytes = null;
			ByteBuffer buffer = null;
			UUID photographerUUID = null;
			ItemStack camera = null;

			// Iterate through all messages received for this uuid
			for (MessagePhotoData message : messages) {
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

			saveImage(createImageFromBytes(bytes));
			
			// TODO: Check type of camera here, decrement film / storage space
			// if digital, save to sd card if present
			// if manual, save to photograph, decrement film level
			postImageSaved(world, photographerUUID, camera);
			
			messageBuffer.remove(uuid);
		}
	}
	
	private static void postImageSaved(World world, UUID photographerUUID, ItemStack camera) {
		if (photographerUUID == null) {
			throw new NullPointerException("Photographer UUID is null, which means we can't produce an item. Sorry :(");
		}
		
		if (camera == null) {
			throw new NullPointerException("Camera is null, which means we don't know how to produce the item properly. Sorry :(");
		}
		
		//if ()
	}

	/**
	 * Takes a BufferedImage and saves it to the photographs folder on the server
	 */
	private static void saveImage(BufferedImage image) {
		try {
			String dirName = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath();
			File directory = new File(dirName, "photographs");
			directory.mkdir();
			File imageFile = Utils.getTimestampedPNGFileForDirectory(directory);
			imageFile = imageFile.getCanonicalFile();
			ImageIO.write(image, "png", imageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
