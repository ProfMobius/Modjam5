package team.thegoldenhoe.cameraobscura.client;

import com.google.common.collect.Maps;

import java.awt.image.BufferedImage;
import java.util.Map;

public enum ClientPhotoCache {
    INSTANCE;

    private Map<String, BufferedImage> cache = Maps.newHashMap();

    public BufferedImage getImage(final String name) {
        return cache.get(name);
    }
}
