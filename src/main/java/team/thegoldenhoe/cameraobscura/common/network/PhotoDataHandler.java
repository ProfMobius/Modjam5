package team.thegoldenhoe.cameraobscura.common.network;

import net.minecraftforge.common.DimensionManager;
import team.thegoldenhoe.cameraobscura.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhotoDataHandler {

    private static int photoID = 0;

    private static Map<Integer, TreeSet<MessagePhotoDataToServer>> messageBuffer = new HashMap<>();

    private static Queue<MessagePhotoDataToServer> messageQueue = new ConcurrentLinkedQueue<>();

    public static void addMessage(final int uuid, final MessagePhotoDataToServer message) {
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
    public static void bufferMessage(final MessagePhotoDataToServer message) {
        messageQueue.add(message);
    }

    /**
     * Iterate through all queued messages and buffer them for processing
     */
    public static synchronized void processMessageQueue() {
        while (!messageQueue.isEmpty()) {
            final MessagePhotoDataToServer msg = messageQueue.poll();
            addMessage(msg.uuid, msg);
        }
    }

    /**
     * Iterate through all buffered messages to check if we have received complete data yet
     */
    public static void processMessageBuffer() {
        final Set<Integer> uuids = messageBuffer.keySet();
        final List<Integer> completedUuids = new LinkedList<Integer>();

        for (final Integer uuid : uuids) {
            final TreeSet<MessagePhotoDataToServer> messages = messageBuffer.get(uuid);
            int bytesReceived = 0;

            // Iterate through all messages received for this uuid
            for (final MessagePhotoDataToServer message : messages) {
                bytesReceived += message.data.length;

                // If we have received all necessary data
                if (bytesReceived == message.length) {
                    completedUuids.add(uuid);
                }
            }
        }

        // Clear out completed entries from the map
        for (final Integer uuid : completedUuids) {
            final TreeSet<MessagePhotoDataToServer> messages = messageBuffer.get(uuid);
            byte[] bytes = null;
            ByteBuffer buffer = null;

            // Iterate through all messages received for this uuid
            for (final MessagePhotoDataToServer message : messages) {
                if (bytes == null) {
                    bytes = new byte[message.length];
                    buffer = ByteBuffer.wrap(bytes);
                }
                buffer.put(message.data);
            }

            saveImage(createImageFromBytes(bytes));
            messageBuffer.remove(uuid);
        }
    }

    /**
     * Takes a BufferedImage and saves it to the photographs folder on the server
     */
    private static void saveImage(final BufferedImage image) {
        try {
            final String dirName = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath();
            final File directory = new File(dirName, "photographs");
            directory.mkdir();
            File imageFile = Utils.getTimestampedPNGFileForDirectory(directory);
            imageFile = imageFile.getCanonicalFile();
            ImageIO.write(image, "png", imageFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static File getFile(final String filename) {
        final String dirName = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath();
        final File directory = new File(dirName, "photographs");
        final File picture = new File(directory, filename);
        if (picture.exists() && picture.isFile()) {
            return picture;
        }
        return null;
    }

    private static BufferedImage createImageFromBytes(final byte[] bytes) {
        final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        try {
            return ImageIO.read(stream);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getUniqueID() {
        return photoID++;
    }
}
