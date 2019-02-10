package main;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Handles loading images and resizing images, in int array form.
 * 
 * @author Joey
 *
 */
public class Image {

	/**
	 * Takes a path to an image, its width and height, and returns the image as
	 * an integer array using 32 bit color
	 * 
	 * @param path
	 *            The path to the image, relative to the src folder
	 * @param width
	 *            The width of the image
	 * @param height
	 *            The height of the image
	 * @return The loaded image
	 */
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

	/**
	 * Takes an integer array and returns a resized integer array by the factor
	 * given. The returned array will have a different size for factors != 1
	 * 
	 * @param img
	 *            The image to be resized
	 * @param width
	 *            The width of the original image
	 * @param factor
	 *            The factor to be scaled
	 * @return The resized image
	 */
	public static int[] resize(int[] img, int width, float factor) {
		float invFactor = 1 / factor;
		float newWidth = factor * width;
		int[] img2 = new int[(int) (img.length * factor * factor)];
		for (int i = 0; i < img2.length; i++) {
			int x = (int) ((i % newWidth) * invFactor);
			int y = (int) ((i / newWidth) * invFactor);
			img2[i] = img[y * width + x];
		}
		return img2;
	}

	/**
	 * Takes an integer array and scales it up by a given factor. The size of
	 * the returned integer array is the same as the original, and the null
	 * point is at the center of the image. Therefore, for factors greater than
	 * 1, there will be clipping, and less than 1 there will be empty space.
	 * 
	 * @param img
	 *            The image to be reszied
	 * @param width
	 *            The width of the original image
	 * @param factor
	 *            The factor to be scaled up
	 * @return An integer array of the scaled up image, with the same length as
	 *         the original
	 */
	public static int[] rescale(int[] img, int width, float factor) {
		// Set up the new variables
		int[] img2 = new int[img.length];
		int xCenter = (int) (512 * Main.zoom) - 512;
		int yCenter = (int) (256 * Main.zoom) - 256;
		float inverseNewWidth = Main.zoom / width;
		int x, y, id, modulus = 0;

		// Walk through the new image
		for (int i = 0; i < img2.length; i++, modulus++) {
			// If the modulus is greater than the width, reset the modulus
			// This acts just like the % symbol, but is faster as it doesn't
			// require division
			if (modulus >= width) modulus = 0;

			// Find the new coordinates
			x = (int) (modulus * factor - xCenter);
			y = (int) (i * inverseNewWidth - yCenter);

			// If the new coordinates are on screen, print them
			id = (y * width + x);
			if (id >= 0 && id < img.length && x >= 0 && x < 1025) img2[i] = img[id];
		}
		return img2;
	}
}
