package main;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image {
	// loadImage(): Takes a path to an image, its width and height, and returns an integer array using 32 bit color
	public int[] loadImage(String path, int width, int height) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(getClass().getResourceAsStream(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image.getRGB(0, 0, width, height, null, 0, width);
	}
}
