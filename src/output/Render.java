package output;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import main.Image;
import main.Main;
import main.World;
import terrain.Map;

public class Render extends Canvas {

	// tf does serialVersion even do lmao
	private static final long serialVersionUID = 1L;
	int width;
	int height;
	BufferedImage img;
	int[] pixels;
	float zoom = 1f;
	World world;
	int[] newMap;

	Image image = new Image();
	// Ground Units
	public int[] artillery = image.loadImage("/res/artillery.png", 32, 16);
	public int[] infantry = image.loadImage("/res/infantry.png", 32, 16);
	public int[] cavalry = image.loadImage("/res/cavalry.png", 32, 16);

	// Water Units
	public int[] landing = image.loadImage("/res/landing.png", 13, 32);
	public int[] destroyer = image.loadImage("/res/destroyer.png", 13, 45);
	public int[] cruiser = image.loadImage("/res/cruiser.png", 16, 61);

	// Air Units
	public int[] fighter1 = image.loadImage("/res/fighter.png", 36, 35);
	public int[] fighter2 = image.loadImage("/res/fighter1.png", 36, 35);
	public int[] attacker1 = image.loadImage("/res/attack.png", 44, 33);
	public int[] attacker2 = image.loadImage("/res/attack1.png", 44, 33);
	public int[] bomber1 = image.loadImage("/res/bomber1.png", 67, 40);
	public int[] bomber2 = image.loadImage("/res/bomber2.png", 67, 40);

	// Buildings
	public int[] city = image.loadImage("/res/city.png", 32, 32);
	public int[] port = image.loadImage("/res/port.png", 32, 32);
	public int[] factory = image.loadImage("/res/factory.png", 32, 32);
	public int[] capital = image.loadImage("/res/capital.png", 32, 32);
	public int[] airfield = image.loadImage("/res/airfield.png", 32, 32);

	// Projectiles
	public int[] shell = image.loadImage("/res/shell.png", 4, 4);
	public int[] torpedo = image.loadImage("/res/torpedo.png", 2, 7);
	public int[] bullet = image.loadImage("/res/bullet.png", 2, 2);
	public int[] bomb = image.loadImage("/res/bomb.png", 16, 8);

	// Misc
	public int[] hitSprite = image.loadImage("/res/hit.png", 36, 20);
	public int[] cityHit = image.loadImage("/res/buildingHit.png", 36, 36);
	public int[] coin = image.loadImage("/res/coin.png", 16, 16);

	public Render(int x, int y, World world) {
		width = x;
		height = y;
		img = new BufferedImage(width + 1, height + 1, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		this.world = world;
	}

	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			return;

		}

		Graphics g = bs.getDrawGraphics();
		// //////////////////////////////////////
		System.arraycopy(Map.mapData, 0, pixels, 0, 1025 * 513);
		drawLongLat();
		world.render(this);

		g.drawImage(img, 0, 0, null);
		world.drawCoins(g);
		g.setColor(new Color(0, 0, 0));
		g.drawString(String.valueOf(Main.version), 5, 17);
		g.setColor(new Color(255, 255, 255));
		g.drawString(String.valueOf(Main.version), 4, 16);
		g.setColor(new Color(0, 0, 0));
		g.drawString(String.valueOf("FPS: " + Main.fps), 5, 33);
		g.setColor(new Color(255, 255, 255));
		g.drawString(String.valueOf("FPS: " + Main.fps), 4, 32);
		// //////////////////////////////////////
		g.dispose();
		bs.show();
	}

	// drawLongLat(): Draws the longitude and latitude lines on the map
	void drawLongLat() {
		for (int i = 0; i < 512 * 17; i++) {
			int x = (int) ((i % 17) * 64);
			int y = (int) (i / 17);
			int id = y * (width + 1) + x;
			int r = (int) (((pixels[id] >> 16) & 255) * .75);
			int g = (int) (((pixels[id] >> 8) & 255) * .75);
			int b = (int) ((pixels[id] & 255) * .75);
			pixels[id] = r << 16 | g << 8 | b;
		}
		for (int i = 0; i < 512 * 18; i++) {
			int x = (int) (i % 1024);
			int y = (int) (i / 1024) * 64;
			int id = y * (width + 1) + x;
			int r = (int) (((pixels[id] >> 16) & 255) * .75);
			int g = (int) (((pixels[id] >> 8) & 255) * .75);
			int b = (int) ((pixels[id] & 255) * .75);
			pixels[id] = r << 16 | g << 8 | b;
		}
	}

	// drawRect(...): draws a rectangle
	public void drawRect(int x, int y, int w, int h, int color) {
		for (int i = 0; i < w * h; i++) {
			int x1 = (int) (i % w) + x;
			int y1 = (int) (i / w);
			pixels[(y1 + y) * (width + 1) + x1] = color;
		}
	}

	// drawImage(...): draws an image
	public void drawImage(int x, int y, int w, int[] image) {
		for (int i = 0; i < image.length; i++) {
			int x1 = (int) (i % w) + x;
			int y1 = (int) (i / w);
			int id = (y1 + y) * (width + 1) + x1;
			int color = image[i];
			float alpha = (color >> 24 & 255) / 255.0f;
			pixels[id] = (int) ((alpha * color) + ((1 - alpha) * pixels[id]));
		}
	}

	// drawImageScreen(...): draws an image, applies overlay blending
	public void drawImageScreen(int x, int y, float w, int[] image, int color) {
		int h = (int) (image.length / w);
		for (int i = 0; i < image.length; i++) {
			int x1 = (int) ((i % w) + x - w / 2);
			int y1 = (int) (i / w) - h / 2;
			float screen = (image[i] & 255) / 255.0f;
			int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
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
			int newColor = r << 16 | g << 8 | b;
			float alpha = (image[i] >> 24 & 255) / 255.0f;
			int id = (int) ((y1 + y) * (width + 1) + x1);
			pixels[id] = (int) ((alpha * newColor) + ((1 - alpha) * pixels[id]));

		}
	}

	// drawImageScreen(...): draws an image, applies overlay blending and
	// rotates
	// image
	public void drawImageScreen(int x, int y, int w, int[] image, int color, float rotate) {
		int h = image.length / w;
		for (int i = 0; i < image.length; i++) {
			if ((image[i] >> 24 & 255) > 0) {
				double x1 = (i % w) - w / 2;
				double y1 = (int) (i / w) - h / 2;
				double x2 = (int) (x1 * Math.cos(-rotate) - y1 * Math.sin(-rotate) + x);
				double y2 = (int) ((x1 * Math.sin(-rotate) + y1 * Math.cos(-rotate)) + y) * (width + 1) + 0.5;
				int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
				float screen = (image[i] & 255) / 255.0f;
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
				int newColor = r << 16 | g << 8 | b;
				float alpha = (image[i] >> 24 & 255) / 255.0f;
				if (x2 + y2 > 0 && x2 + y2 < width * height) {
					pixels[(int) ((int) (x2) + (int) (y2))] = (int) ((alpha * newColor) + ((1 - alpha) * pixels[(int) (x2 + y2)]));
					x2 += 0.5;
					y2 += 0.5;
					pixels[(int) ((int) (x2) + (int) (y2))] = (int) ((alpha * newColor) + ((1 - alpha) * pixels[(int) (x2 + y2)]));
				}
			}
		}
	}

	// lighten(int color): returns a lighter color given a 24 bit int color
	public int lighten(int color) {
		int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
		r *= 2;
		g *= 2;
		b *= 2;
		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;
		return r << 16 | g << 8 | b;
	}

	// lighten(int color): returns a darker color given a 24 bit int color
	public int darken(int color) {
		int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
		if (r > b) {
			r *= 0.5;
			g = 0;
			b = 0;
		} else {
			r = 0;
			g = 0;
			b *= 0.5;
		}
		return r << 16 | g << 8 | b;
	}
}