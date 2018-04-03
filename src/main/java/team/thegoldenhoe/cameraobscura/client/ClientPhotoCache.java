package team.thegoldenhoe.cameraobscura.client;

import com.google.common.collect.Maps;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public enum ClientPhotoCache {
    INSTANCE;

    private Map<String, BufferedImage> cache = Maps.newHashMap();
    private Map<String, byte[]> recvCache = Maps.newHashMap();
    private Map<String, Integer> recvCacheIndex = Maps.newHashMap();

    public BufferedImage getImage(final String name) {
        return cache.get(name);
    }

    public void addBytes(final String name, final byte[] data, final boolean isFinal, final int fullSize) {
        byte[] bytes = recvCache.computeIfAbsent(name, k -> new byte[fullSize]);
        int index = recvCacheIndex.computeIfAbsent(name, k -> 0);
        System.arraycopy(data, 0, bytes, index, data.length);
        recvCacheIndex.put(name, index + data.length);

        if (isFinal) {
            ByteArrayInputStream bais = new ByteArrayInputStream(recvCache.get(name));
            try {
                BufferedImage img = ImageIO.read(bais);
                cache.put(name, img);
                recvCache.remove(name);
                recvCacheIndex.remove(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
