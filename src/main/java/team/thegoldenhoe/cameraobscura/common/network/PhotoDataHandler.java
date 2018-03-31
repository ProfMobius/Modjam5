package team.thegoldenhoe.cameraobscura.common.network;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhotoDataHandler {
	
	private static int photoID = 0;

	private static Map<Integer, TreeSet<MessagePhotoData>> messageBuffer = new HashMap<>();

	private static Queue<MessagePhotoData> messageQueue = new ConcurrentLinkedQueue<>();

	public static void addMessage(int uuid, MessagePhotoData message) {
		TreeSet<MessagePhotoData> messageSet = messageBuffer.get(Integer.valueOf(uuid));
		if (messageSet == null) {
			messageSet = new TreeSet<MessagePhotoData>(MessagePhotoData.COMPARATOR);
			messageBuffer.put(Integer.valueOf(uuid), messageSet);
		} else {
			messageSet.add(message);
		}
	}

	public static void bufferMessage(MessagePhotoData message) {
		messageQueue.add(message);
	}

	public static void processMessageQueue() {
		for (MessagePhotoData message : messageQueue) {
			addMessage(message.uuid, message);
		}
	}

	/**
	 * Iterates through all buffered messages to check if we have received complete data yet
	 */
	public static void processMessageBuffer() {
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
					System.out.println("Full image recv!!!!");
					completedUuids.add(uuid);					
				}
			}
		}

		// Clear out completed entries from the map
		for (Integer uuid : completedUuids) {
			messageBuffer.remove(uuid);
		}
	}
	
	public static int getUniqueID() {
		return photoID++;
	}
}
