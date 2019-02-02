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

	// resize(): A complicated resizing algorithm that returns an int array that is both scaled by a given factor and the same size as the original array
	public static int[] rescale(int[] img, int width, float factor) {
		//Set up the new variables
		int[] img2 = new int[img.length];
		int xCenter = (int)(512*Main.zoom)-512;
		int yCenter = (int)(256*Main.zoom)-256;
		float inverseNewWidth = Main.zoom/width;
		int x, y, id, modulus = 0;
		
		//Walk through the new image
		for (int i = 0; i < img2.length; i++, modulus++) {
			// If the modulus is greater than the width, reset the modulus
			// This acts just like the % symbol, but is faster as it doesn't require division
			if(modulus >= width)
				modulus = 0;
			
			//Find the new coordinates
			x = (int) (modulus * factor - xCenter);
			y = (int) (i * inverseNewWidth  - yCenter);
			
			//If the new coordinates are on screen, print them
			id = (y * width + x);
			if(id >= 0 && id < img.length && x >=0 && x < 1025)
				img2[i] = img[id];
		}
		return img2;
	}
}
