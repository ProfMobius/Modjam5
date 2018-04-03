package team.thegoldenhoe.cameraobscura.client;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;

public class PhotoFilters {

	public static final PhotoFilter BLACK_AND_WHITE = new PhotoFilter() {
		@Override
		public BufferedImage getFilteredImage(BufferedImage src) {
			BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			return op.filter(src, null);
		}
	};

	public static final PhotoFilter SEPIA = new PhotoFilter() {
		/**
		 * Sepia algorithm from https://stackoverflow.com/questions/21899824
		 */
		@Override
		public BufferedImage getFilteredImage(BufferedImage src) {
			int sepiaDepth = 20, sepiaIntensity = 35;
			int w = src.getWidth();
			int h = src.getHeight();

			WritableRaster raster = src.getRaster();

			// r, g, b per pixel
			int[] pixels = new int[w * h * 3];
			raster.getPixels(0, 0, w, h, pixels);

			for (int i = 0; i < pixels.length; i += 3) {
				int r = pixels[i];
				int g = pixels[i + 1];
				int b = pixels[i + 2];

				int gray = (r + g + b) / 3;
				r = g = b = gray;
				r = r + (sepiaDepth * 2);
				g = g + sepiaDepth;

				if (r > 255) r = 255;
				if (g > 255) g = 255;
				if (b > 255) b = 255;

				// Darken blue color to increase sepia effect
				b -= sepiaIntensity;

				// normalize if out of bounds
				if (b < 0) b = 0;
				if (b > 255) b = 255;

				pixels[i] = r;
				pixels[i + 1] = g;
				pixels[i + 2] = b;
			}

			raster.setPixels(0, 0, w, h, pixels);
			return src;
		}
	};

	public static final PhotoFilter BLUR = new PhotoFilter() {

		@Override
		public BufferedImage getFilteredImage(BufferedImage src) {
			int radius = 3;
			int size = radius * 2 + 1;
			float weight = 1.0f / (size * size);
			float[] data = new float[size * size];

			for (int i = 0; i < data.length; i++) {
				data[i] = weight;
			}
			BufferedImageOp op = new ConvolveOp(new Kernel(size, size, data), ConvolveOp.EDGE_NO_OP, null);
			return op.filter(src, null);
		}
	};

	public static final PhotoFilter BRIGHT_AND_HAPPY = new PhotoFilter() {
		@Override
		public BufferedImage getFilteredImage(BufferedImage src) {
			RescaleOp op = new RescaleOp(1.4f, 15, null);
			op.filter(src, src);
			return src;
		}
	};

	public static final PhotoFilter HIGH_CONTRAST = new PhotoFilter() {
		@Override
		public BufferedImage getFilteredImage(BufferedImage src) {
			RescaleOp op = new RescaleOp(2.4f, 55, null);
			op.filter(src, src);
			return src;
		}
	};

	public static final PhotoFilter LOW_SOBEL = new PhotoFilter() {
		@Override
		public BufferedImage getFilteredImage(BufferedImage src) {
			return getSobel(src, 30);
		}
	};

	public static final PhotoFilter HIGH_SOBEL = new PhotoFilter() {
		@Override
		public BufferedImage getFilteredImage(BufferedImage src) {
			return getSobel(src, 60);
		}
	};

	private static BufferedImage getSobel(BufferedImage src, int threshold) {
		int w = src.getWidth();
		int h = src.getHeight();

		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

		int blackRgb = Color.BLACK.getRGB();
		int whiteRgb = Color.WHITE.getRGB();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = src.getRGB(x, y);
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = (rgb) & 0xff;
				int gray = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
				if (gray >= threshold) {
					out.setRGB(x, y, whiteRgb);
				} else {
					out.setRGB(x, y, blackRgb);
				}
			}
		}

		return out;
	}

	public static final PhotoFilter VINTAGE = new PhotoFilter() {
		@Override
		public BufferedImage getFilteredImage(BufferedImage src) {
			int w = src.getWidth();
			int h = src.getHeight();

			WritableRaster raster = src.getRaster();

			// r, g, b per pixel
			int[] pixels = new int[w * h * 3];
			raster.getPixels(0, 0, w, h, pixels);
			adjustChannels(pixels);		
			raster.setPixels(0, 0, w, h, pixels);
			return src;
		}

		/**
		 * Adjust channels.
		 *
		 * @param pixels the pixels
		 */
		private void adjustChannels(int[] pixels) {
			int red, green, blue;
			for (int i = 0; i < pixels.length; i++) {
				red = (pixels[i] >> 16) & 0xff;
				red *= 0.9;
				if (red > 240)
					red = 240;
				if (red < 0)
					red = 0;

				green = (pixels[i] >> 8) & 0xff;
				if (green > 123)
					green *= 1.05;
				if (green > 255)
					green = 255;
				if (green < 0)
					green = 0;

				blue = (pixels[i]) & 0xff;
				if (blue < 125) {
					blue *= 1.15;
				} else if (blue >= 125) {
					blue *= 0.85;
				}
				if (blue > 255)
					blue = 255;
				if (blue < 0)
					blue = 0;

				//				float[] hsb = Color.RGBtoHSB(red, green, blue, null);
				//				float hue = hsb[0];
				//				float saturation = hsb[1];
				//				float brightness = hsb[2];
				//
				//				brightness = 1 - brightness + 0.2f;
				//				
				//				int rgb = Color.HSBtoRGB(hue, saturation, brightness);

				int rgb = red;
				rgb = (rgb << 8) + green;
				rgb = (rgb << 8) + blue;

				pixels[i] = rgb;
			}
		}
	};

}
