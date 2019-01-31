package main;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SpriteSheet {

	int[] pixels;
	int WIDTH;

	public SpriteSheet(String path, int width) {
		pixels = loadImageArray(path);
		WIDTH = width;
	}

	public int[] loadImageArray(String path) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(this.getClass().getResourceAsStream(path));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		if (img == null) {
			return pixels;
		}
		return img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
	}

	public int[] getSubset(int x, int y, int width) {
		int[] temp = new int[width * width];

		int m = ((y*width) * WIDTH) + (x*width);
		int n = WIDTH - width;
		for (int i = 0; i < width * width; i++, m++) {
			if (i % width == 0) {
				m += n;
			}
				temp[i] = pixels[m-n];
		}
		return temp;
	}
}