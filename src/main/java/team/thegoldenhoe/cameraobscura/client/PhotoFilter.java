package team.thegoldenhoe.cameraobscura.client;

import java.awt.image.BufferedImage;

public interface PhotoFilter {
	public BufferedImage getFilteredImage(BufferedImage src);
}
