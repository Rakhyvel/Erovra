package objects.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Main;

/**
 * Handles loading images and resizing images, in int array form.
 * 
 * @author Joey
 *
 */
public class Image {

	private float opacity = 1;
	private int[] pixels;
	private String path;
	private int width;
	int height;
	float rotation = 0;

	public Image(String path, int width, int height) {
		this.setPath(path);
		this.setWidth(width);
		this.height = height;
		setPixels(loadImage(path, width, height));
	}

	public Image(String path, int width, int height, int[] pixels, float opacity, float rotation) {
		this.setPath(path);
		this.setWidth(width);
		this.height = height;
		setPixels(pixels);
		this.opacity = opacity;
		this.rotation = rotation;
	}

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
	public Image resize(float factor) {
		float invFactor = 1 / factor;
		int newWidth = (int)(factor * getWidth());
		int[] img2 = new int[(int) (getWidth() * factor * height * factor)];
		for (int i = 0; i < img2.length; i++) {
			int x = (int) ((i % newWidth) * invFactor);
			int y = (int) ((i / newWidth) * invFactor);
			int id = y * getWidth() + x;
			if (id >= 0 && id < getPixels().length) img2[i] = getPixels()[y * getWidth() + x];
		}
		return new Image(getPath(), (int) (getWidth() * factor), (int)(img2.length/newWidth),img2, opacity, rotation);
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
	public Image rescale(float factor) {
		// Set up the new variables
		int[] img2 = new int[getPixels().length];
		int xCenter = (int) (512 * Main.zoom) - 512;
		int yCenter = (int) (256 * Main.zoom) - 256;
		int x, y, id;
		// Walk through the new image
		for (int i = 0; i < 1025 * 513; i++) {

			// Find the new coordinates
			x = (int) (i % getWidth() * Main.zoom - xCenter);
			y = (int) (i / getWidth() * Main.zoom - yCenter);

			// If the new coordinates are on screen, print them
			id = (y * 2048 + x);
			if (id >= 0 && id < getPixels().length && x >= 0 && x < 2048) img2[i] = getPixels()[id];
		}
		return new Image(getPath(), getWidth(), height, img2, opacity, rotation);
	}

	public Image getScreenBlend(int color) {
		int r, g, b;
		float screen, alpha;
		int[] img2 = new int[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			alpha = ((pixels[i] >> 24 & 255) / 255.0f);
			if (alpha > 0.9f) {
				// Finding and splitting starting colors
				screen = (pixels[i] & 255) / 255.0f;
				r = ((color >> 16) & 255);
				g = ((color >> 8) & 255);
				b = (color & 255);

				// Setting new colors
				if (screen <= 0.5f) {
					screen *= 2;
					r = (int) (r * screen);
					g = (int) (g * screen);
					b = (int) (b * screen);
				} else {
					r = (int) (255 - 2 * (255 - r) * (1 - screen));
					g = (int) (255 - 2 * (255 - g) * (1 - screen));
					b = (int) (255 - 2 * (255 - b) * (1 - screen));
				}
				// Recombining colors
				img2[i] = (255 << 24) | (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);
			}
		}
		return new Image(getPath(), getWidth(), height, img2, opacity, rotation);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int[] getPixels() {
		return pixels;
	}

	public void setPixels(int[] pixels) {
		this.pixels = pixels;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setRotation(float pi) {
		rotation = pi;
	}

	public float getRotate() {
		return rotation;
	}
}
