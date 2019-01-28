package main;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image {

	// loadImage(): Takes a path to an image, its width and height, and returns
	// an integer array using 32 bit color
	public int[] loadImage(String path, int width, int height) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(getClass().getResourceAsStream(path));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return image.getRGB(0, 0, width, height, null, 0, width);
	}
	
	// resize(): A not-so complicated resizing algorithm that returns an int array scaled by a given factor
	public static int[] resize(int[] img, int width, float factor) {
		float invFactor = 1 / factor;
		float newWidth = factor * width;
		int[] img2 = new int[(int) (img.length * factor * factor)];
		for (int i = 0; i < img2.length; i++) {
			int x = (int) ((i % newWidth) * invFactor);
			int y = (int) ((i / newWidth) * invFactor);
			img2[i] = img[(int) (y * width + x)];
		}
		return img2;
	}

	// resize(): A not-so complicated resizing algorithm that returns an int array that is both scaled by a given factor and the same size as the original array
	public static int[] rescale(int[] img, int width, float factor) {
		float invFactor = 1 / factor;
		int[] img2 = new int[img.length];
		for (int i = 0; i < img2.length; i++) {
			int x = (int) (((i % width) * invFactor)+((-2/factor) + 2)*256);
			int y = (int) (((i / width) * invFactor)+((-2/factor) + 2)*128);
			int id = (y * width + x);
			if(id >= 0 && id < img.length)
				img2[i] = img[id];
		}
		return img2;
	}
}
