package output;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import main.Main;
import main.SpriteSheet;
import main.StateID;
import main.UnitID;
import main.World;
import objects.gui.Font;
import objects.gui.Image;
import terrain.Map;
import utility.Point;
import utility.Vector;

public class Render extends Canvas {

	private static final long serialVersionUID = 1L;
	int width;
	int height;
	BufferedImage img;
	int[] pixels;
	int[] map;
	int[] background = new int[1025 * 513];
	int[] menu = eggShellScreen();
	float zoom = 1f;
	World world;
	int[] newMap;
	boolean captured = false;

	static Image image = new Image();
	// Ground Units
	public static int[] artillery = image.loadImage("/res/ground/artillery.png", 32, 16);
	public static int[] infantry = image.loadImage("/res/ground/infantry.png", 32, 16);
	public static int[] cavalry = image.loadImage("/res/ground/cavalry.png", 32, 16);
	public int[] hitSprite = image.loadImage("/res/ground/hit.png", 36, 20);
	public int[] medArtRange = image.loadImage("/res/ground/medArtRange.png", 128, 128);
	public int[] heavyArtRange = image.loadImage("/res/ground/heavyArtRange.png", 256, 256);

	// Water Units
	public int[] landing = image.loadImage("/res/water/landing.png", 13, 32);
	public int[] destroyer = image.loadImage("/res/water/destroyer.png", 13, 45);
	public int[] cruiser = image.loadImage("/res/water/cruiser.png", 14, 61);
	public int[] landingHit = image.loadImage("/res/water/landingHit.png", 17, 36);
	public int[] destroyerHit = image.loadImage("/res/water/destroyerHit.png", 17, 49);
	public int[] cruiserHit = image.loadImage("/res/water/cruiserHit.png", 18, 65);

	// Air Units
	public int[] fighter1 = image.loadImage("/res/air/fighter.png", 36, 35);
	public int[] fighter2 = image.loadImage("/res/air/fighter1.png", 36, 35);
	public int[] attacker1 = image.loadImage("/res/air/attack.png", 44, 33);
	public int[] attacker2 = image.loadImage("/res/air/attack1.png", 44, 33);
	public int[] bomber1 = image.loadImage("/res/air/bomber1.png", 68, 40);
	public int[] bomber2 = image.loadImage("/res/air/bomber2.png", 68, 40);
	public int[] fighterHit = image.loadImage("/res/air/fighterHit.png", 40, 39);
	public int[] attackerHit = image.loadImage("/res/air/attackHit.png", 48, 37);
	public int[] bomberHit = image.loadImage("/res/air/bomberHit.png", 72, 44);
	public int[] fighterShadow = shadowify(fighter2);
	public int[] attackerShadow = shadowify(attacker2);
	public int[] bomberShadow = shadowify(bomber2);

	// Buildings
	public int[] city = image.loadImage("/res/buildings/city.png", 32, 32);
	public int[] port = image.loadImage("/res/buildings/port.png", 32, 32);
	public int[] factory = image.loadImage("/res/buildings/factory.png", 32, 32);
	public int[] capital = image.loadImage("/res/buildings/capital.png", 32, 32);
	public int[] airfield = image.loadImage("/res/buildings/airfield.png", 32, 32);
	public int[] cityHit = image.loadImage("/res/buildings/buildingHit.png", 36, 36);

	// Projectiles
	public int[] shell = image.loadImage("/res/projectiles/shell.png", 4, 4);
	public int[] torpedo = image.loadImage("/res/projectiles/torpedo.png", 3, 14);
	public int[] torpedo1 = image.loadImage("/res/projectiles/torpedo1.png", 1, 14);
	public int[] bullet = image.loadImage("/res/projectiles/bullet.png", 2, 2);
	public int[] bomb = image.loadImage("/res/projectiles/bomb.png", 16, 8);

	// Misc
	public int[] coin = image.loadImage("/res/coin.png", 16, 16);
	public int[] flag = image.loadImage("/res/flag.png", 16, 16);
	public int[] arrow = image.loadImage("/res/arrow.png", 20, 10);
	public int[] largeShaft = image.loadImage("/res/largeShaft.png", 7, 7);
	public int[] smallShaft = image.loadImage("/res/smallShaft.png", 3, 3);
	public int[] target = image.loadImage("/res/target.png", 32, 32);
	public int[] settings = image.loadImage("/res/settings.png", 25, 25);
	public int[] showPath = image.loadImage("/res/showPath.png", 26, 26);

	// Fonts
	public Font font32 = new Font(new SpriteSheet("/res/fonts/font32.png", 512), 32);
	public Font font16 = new Font(new SpriteSheet("/res/fonts/font16.png", 256), 16);

	public Render(int x, int y, World world) {
		width = x;
		height = y;
		img = new BufferedImage(width + 1, height + 1, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		map = new int[1025 * 513];
		this.world = world;
		this.addMouseListener(Main.mouse);
		this.addKeyListener(Main.keyboard);
		zoom = 1;
	}

	/**
	 * Calls all objects to render to pixels int array, draws pixel array to
	 * screen.
	 */
	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			return;
		}

		Graphics g = bs.getDrawGraphics();
		// //////////////////////////////////////
		if (Main.gameState == StateID.ONGOING) {
			System.arraycopy(Map.mapData, 0, pixels, 0, 1025 * 513);
			captured = false;
		} else if (Main.gameState == StateID.DEFEAT || Main.gameState == StateID.PAUSED || Main.gameState == StateID.VICTORY) {
			if (!captured) {
				System.arraycopy(darkenScreen(pixels), 0, background, 0, 1025 * 513);
				captured = true;
			}
			System.arraycopy(background, 0, pixels, 0, 1025 * 513);
		} else if (Main.gameState == StateID.MENU) {
			System.arraycopy(menu, 0, pixels, 0, 1025 * 513);
		}
		world.render(this);
		drawString("FPS:", 22, 10, font16, 255 << 24 | 250 << 16 | 250 << 8 | 250);
		drawString(String.valueOf(Main.fps), 55, 10, font16, 244 << 24 | 250 << 16 | 250 << 8 | 250);
		if (Main.gameState == StateID.ONGOING) world.drawCoins(this);
		g.drawImage(img, 0, 0, null);
		g.dispose();
		bs.show();
	}
	
	void drawVoronoi() {
		for (int i = 0; i < 1025 * 513; i++) {
			double closestFriendlyDist = 300000000;
			for (int i2 = 0; i2 < Main.world.friendly.unitSize(); i2++) {
				if (Main.world.friendly.getUnit(i2).getID() != UnitID.PLANE && Main.world.friendly.getUnit(i2).getPosition().getDist(new Point(i%1025,i/1025)) < closestFriendlyDist) {
					closestFriendlyDist = Main.world.friendly.getUnit(i2).getPosition().getDist(new Point(i%1025,i/1025));
				}
			}
			double closestHostileDist = 300000000;
			for (int i2 = 0; i2 < Main.world.hostile.unitSize(); i2++) {
				if (Main.world.hostile.getUnit(i2).getID() != UnitID.PLANE && Main.world.hostile.getUnit(i2).getPosition().getDist(new Point(i%1025,i/1025)) < closestHostileDist) {
					closestHostileDist = Main.world.hostile.getUnit(i2).getPosition().getDist(new Point(i%1025,i/1025));
				}
			}
			
			if(closestFriendlyDist < closestHostileDist) {
				pixels[i] = (64)+pixels[i];
			} else if(closestFriendlyDist > closestHostileDist){
				pixels[i] = (64<<16)+pixels[i];
			} else {
				pixels[i] = 0;
			}
		}
	}

	/**
	 * Draws a rectangle, with opacity
	 * 
	 * @param x
	 *            The x coordinate of the top left corner of the rectangle
	 * @param y
	 *            The y coordinate of the top left corner of the rectangle
	 * @param w
	 *            The width of the rectangle
	 * @param h
	 *            The height of the rectangle
	 * @param color
	 *            The color of the rectangle
	 * @param alpha
	 *            The opacity of the rectangle
	 */
	public void drawRect(int x, int y, int w, int h, int color) {
		float alpha = ((color >> 24) & 255) / 255.0f;
		for (int i = 0; i < w * h; i++) {
			int x1 = i % w + x;
			int y1 = i / w;
			int id = (y1 + y) * (width + 1) + x1;
			if (x >= 0 && x < 1025 && id > 0 && id < 1025 * 513) {
				int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
				int newColor = (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);
				r = (pixels[id] >> 16) & 255;
				g = (pixels[id] >> 8) & 255;
				b = pixels[id] & 255;
				int newColor2 = (int) (r * (1 - alpha)) << 16 | (int) (g * (1 - alpha)) << 8 | (int) (b * (1 - alpha));
				pixels[id] = newColor + newColor2;
			}
		}
	}

	public void drawRectBorders(int x, int y, int w, int h, int color, int borders) {
		float alpha = ((color >> 24) & 255) / 255.0f;
		for (int i = 0; i < w * h; i++) {
			int x1 = i % w;
			int y1 = i / w;
			int id = (y1 + y) * (width + 1) + x1 + x;
			if (x >= 0 && x < 1025 && id > 0 && id < 1025 * 513) {
				int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
				int newColor = (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);
				r = (pixels[id] >> 16) & 255;
				g = (pixels[id] >> 8) & 255;
				b = pixels[id] & 255;
				int newColor2 = (int) (r * (1 - alpha)) << 16 | (int) (g * (1 - alpha)) << 8 | (int) (b * (1 - alpha));
				// bottom | right | top | left
				if ((((borders & 1) == 1) && x1 < 2) || (((borders & 2) == 2) && y1 < 2) || (((borders & 4) == 4) && x1 > w - 3) || (((borders & 8) == 8) && y1 > h - 3)) {
					pixels[id] = 230 << 16 | 230 << 8 | 230;
				} else {
					pixels[id] = newColor + newColor2;
				}
			}
		}
	}

	/**
	 * Draws an image
	 * 
	 * @param x
	 *            The x coordinate of the top left corner of the image
	 * @param y
	 *            The y coordinate of the top right corner of the image
	 * @param w
	 *            The width of the image
	 * @param image
	 *            The image
	 */
	public void drawImage(int x, int y, int w, int[] image, float opacity, float rotate) {
		int h = 1, r, g, b;
		double cos = 0, sin = 0;
		if (w > 0) {
			h = image.length / w;
		}
		float halfW = w / 2;
		float halfH = h / 2;
		float deltaI = 1;
		if (rotate != 0) {
			deltaI = 0.49f;
			cos = Math.cos(-rotate);
			sin = Math.sin(-rotate);
		}

		for (float i = 0; i < image.length; i += deltaI) {
			float alpha = ((image[(int) i] >> 24 & 255) / 255.0f) * opacity;
			if (alpha > 0) {
				double x1 = (i % w) - halfW;
				double y1 = i / w - halfH;
				double x2 = (int) (x1 + x);
				double y2 = (int) (y1 + y);
				if (rotate != 0) {
					x2 = (int) (x1 * cos - y1 * sin + x);
					y2 = (int) (x1 * sin + y1 * cos + y);
				}
				int id = (int) (x2 + y2 * (width + 1) + 0.5);
				if (id > 0 && id < width * height) {
					// Finding alpha
					r = ((image[(int) i] >> 16) & 255);
					g = ((image[(int) i] >> 8) & 255);
					b = (image[(int) i] & 255);
					int newColor = (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);

					// Finding alpha
					r = ((pixels[id] >> 16) & 255);
					g = ((pixels[id] >> 8) & 255);
					b = (pixels[id] & 255);
					int newColor2 = (int) (r * (1 - alpha)) << 16 | (int) (g * (1 - alpha)) << 8 | (int) (b * (1 - alpha));

					// Finally adding colors to pixel array
					pixels[id] = newColor + newColor2;
				}
			}
		}
	}

	/**
	 * Draws a string
	 * 
	 * @param label
	 *            String to be drawn
	 * @param x
	 *            coordinate value of the middle of the string
	 * @param y
	 *            coordinate value of the string
	 * @param font
	 *            Font to be used
	 * @param color
	 *            Color of the string
	 */

	public void drawString(String label, int x, int y, Font font, int color) {
		int carriage = 0;
		int letter;
		int length = font.getStringWidth(label);
		int[] letterImage;
		for (int i = 0; i < label.length(); i++) {
			letter = label.charAt(i);
			if (letter == 7) {
				letterImage = getScreenBlend(250 << 16 | 250 << 8, font.getLetter(letter));
			} else {
				letterImage = getScreenBlend(color, font.getLetter(letter));
			}
			drawImage(x + carriage - length / 2 + font.getSize() / 2, y, font.getSize(), letterImage, 1, 0);
			carriage += font.getKern(letter);
		}
	}

	public void drawString(String label, int x, int y, Font font, int color, boolean centered) {
		int carriage = 0;
		int letter;
		int length = font.getStringWidth(label);
		int[] letterImage;
		for (int i = 0; i < label.length(); i++) {
			letter = label.charAt(i);
			if (letter == 7) {
				letterImage = getScreenBlend(250 << 16 | 250 << 8, font.getLetter(letter));
			} else {
				letterImage = getScreenBlend(color, font.getLetter(letter));
			}
			if (centered) {
				drawImage(x + carriage - length / 2 + font.getSize() / 2, y, font.getSize(), letterImage, 1, 0);
			} else {
				drawImage(x + carriage + font.getSize() / 2, y, font.getSize(), letterImage, 1, 0);
			}
			carriage += font.getKern(letter);
		}
	}

	public static int[] getScreenBlend(int color, int[] img) {
		int r, g, b;
		float screen, alpha;
		int[] img2 = new int[img.length];
		for (int i = 0; i < img.length; i++) {
			alpha = ((img[i] >> 24 & 255) / 255.0f);
			if (alpha > 0.9f) {
				// Finding and splitting starting colors
				screen = (img[i] & 255) / 255.0f;
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
		return img2;
	}

	public int[] shadowify(int[] img) {
		int[] img2 = new int[img.length];
		for (int i = 0; i < img.length; i++) {
			if ((img[i] >> 24 & 255) > 0) img2[i] = 26 << 24;
		}
		return img2;
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
	public int[] resize(int[] img, double factor, int width, int height) {
		double invFactor = 1 / factor;
		int newWidth = (int) (factor * width);
		int[] img2 = new int[(int) (width * factor * height * factor)];
		for (int i = 0; i < img2.length; i++) {
			int x = (int) ((i % newWidth) * invFactor);
			int y = (int) ((i / newWidth) * invFactor);
			int id = y * width + x;
			if (id >= 0 && id < img.length) img2[i] = img[y * width + x];
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
	 * @param d
	 *            The factor to be scaled up
	 * @return An integer array of the scaled up image, with the same length as
	 *         the original
	 */
	public int[] rescale(int[] img, double d, int width, int height) {
		// Set up the new variables
		int[] img2 = new int[img.length];
		int xCenter = (int) (512 * Main.zoom) - 512;
		int yCenter = (int) (256 * Main.zoom) - 256;
		int x, y, id;
		// Walk through the new image
		for (int i = 0; i < 1025 * 513; i++) {

			// Find the new coordinates
			x = (int) (i % width * Main.zoom - xCenter);
			y = (int) (i / width * Main.zoom - yCenter);

			// If the new coordinates are on screen, print them
			id = (y * 2048 + x);
			if (id >= 0 && id < img.length && x >= 0 && x < 2048) img2[i] = img[id];
		}
		return img2;
	}

	public static int getColor(UnitID weight, int color) {
		if (weight == UnitID.MEDIUM) {
			return color;
		} else if (weight == UnitID.LIGHT) {
			return lighten(color);
		}
		return darken(color);
	}

	/**
	 * Returns a lighter color
	 * 
	 * @param color
	 *            The original color
	 * @return A color 50% lighter
	 */
	public static int lighten(int color) {
		int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
		r = (int) (r * 0.5) + 128;
		g = (int) (g * 0.5) + 128;
		b = (int) (b * 0.5) + 128;
		if (b > r) {
			g += 40;
			r -= 25;
		} else {
			b -= 30;
			g += 30;
		}
		return 255 << 24 | r << 16 | g << 8 | b;
	}

	/**
	 * Returns a darker color
	 * 
	 * @param color
	 *            The original color
	 * @return A new color, 50% darker
	 */
	public static int darken(int color) {
		int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
		r = (r >> 1);
		g = (g >> 1);
		b = (b >> 1);
		if (b > r) {
			g *= 0.75;
		} else {
			b *= 1.8;
		}
		return 255 << 24 | r << 16 | g << 8 | b;
	}

	/**
	 * Desaturates a given color
	 * 
	 * @param color
	 *            Original color
	 * @return A less saturated color
	 */
	public int desaturate(int color) {
		int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
		// int a = (r + g + b) / 3;
		// r = (a + r) >> 1;
		// g = (a + g) >> 1;
		// b = (a + b) >> 1;
		// int cMax = Math.max(r,Math.max(g,b));
		// 76 127 178
		int hue = getRGB(getHue(r, g, b), getSaturation(r, g, b) * 0.4, getValue(r, g, b) * 0.4);
		return hue;
	}

	public double getValue(int r, int g, int b) {
		double r1 = r / 255.0;
		double g1 = g / 255.0;
		double b1 = b / 255.0;
		return Math.max(r1, Math.max(g1, b1));
	}

	public double getSaturation(int r, int g, int b) {
		double r1 = r / 255.0;
		double g1 = g / 255.0;
		double b1 = b / 255.0;
		double cMax = Math.max(r1, Math.max(g1, b1));
		double cMin = Math.min(r1, Math.min(g1, b1));
		if (cMax == 0) return 0;
		return ((cMax - cMin) / cMax);
	}

	public double getHue(int r, int g, int b) {
		double r1 = r / 255.0;
		double g1 = g / 255.0;
		double b1 = b / 255.0;
		double cMax = Math.max(r1, Math.max(g1, b1));
		double cMin = Math.min(r1, Math.min(g1, b1));
		double hue = 0;
		if (cMax - cMin == 0) {
			return 0;
		}
		if (cMax == r1) {
			hue = 60 * (((g1 - b1) / (cMax - cMin)) % 6);
		} else if (cMax == g1) {
			hue = 60 * (((b1 - r1) / (cMax - cMin)) + 2);
		} else {
			hue = 60 * (((r1 - g1) / (cMax - cMin)) + 4);
		}
		if (hue < 0) {
			hue += 360;
		}
		return hue;
	}

	public int getRGB(double hue, double saturation, double value) {
		if (hue < 0) {
			hue += 360;
		}
		double c = value * saturation;
		double x = c * (1 - Math.abs(((hue / 60) % 2) - 1));
		double m = value - c;
		double r = 0;
		double g = 0;
		double b = 0;

		if (hue >= 0 && hue < 60) {
			r = c;
			g = x;
			b = 0;
		} else if (hue >= 60 && hue < 120) {
			r = x;
			g = c;
			b = 0;
		} else if (hue >= 120 && hue < 180) {
			r = 0;
			g = c;
			b = x;
		} else if (hue >= 180 && hue < 240) {
			r = 0;
			g = x;
			b = c;
		} else if (hue >= 240 && hue < 300) {
			r = x;
			g = 0;
			b = c;
		} else if (hue >= 300 && hue < 360) {
			r = c;
			g = 0;
			b = x;
		}
		return (int) ((r + m) * 255) << 16 | (int) ((g + m) * 255) << 8 | (int) ((b + m) * 255);
	}

	public int dither(int x, int y, int value) {
		int r = (value >> 16) & 255;
		int g = (value >> 8) & 255;
		int b = value & 255;
		int cMax = Math.max(r, Math.max(g, b));
		int white = 230 << 16 | 230 << 8 | 230;
		if (x % 2 == 0) {
			if (y % 2 == 0) {
				if (cMax > 160) {
					return white;
				}
			} else {
				if (cMax > 224) {
					return white;
				}
			}
		} else {
			if (y % 2 == 0) {
				if (cMax > 192) {
					return white;
				}
			} else {
				if (cMax > 128) {
					return white;
				}
			}
		}
		return 23 << 16 | 23 << 8 | 23;
	}

	/**
	 * Takes an image and makes it darker and more desaturated
	 * 
	 * @param image
	 *            Original image
	 * @return Darker and desaturated image
	 */
	public int[] darkenScreen(int[] image) {
		for (int i = 0; i < 1025 * 513 - 4; i++) {
			image[i] = desaturate(image[i]);
		}
		return image;
	}

	/**
	 * @return An gray image
	 */
	public int[] eggShellScreen() {
		int[] image = new int[1025 * 513];
		for (int i = 0; i < 1025 * 513; i++) {
			image[i] = 50 << 16 | 50 << 8 | 50;
		}
		return image;
	}

	public void drawLine(Point start, Point end, int color, int background, float baseline) {
		// Setting up and drawing the bigger background shaft
		Vector march = start.subVec(end).normalize().scalar(3);
		Point step = new Point(start);
		step = step.addVector(march.scalar(4));
		for (int i = 0; i < start.getDistSquared(end)-12 && ((Map.getArray(step) < baseline + 0.5 && Map.getArray(step) >= baseline) || (baseline == 1 && Map.getArray(step) != -1)); i += 3) {
			step = step.addVector(march);
			drawImage((int) step.getX(), (int) step.getY(), 7, largeShaft, 1, 0);
		}
		
		// Setting up and drawing the smaller shaft
		step = new Point(start);
		march = start.subVec(end).normalize();
		step = step.addVector(march.scalar(12));
		for (int i = 0; i < start.getDistSquared(end)-12 && ((Map.getArray(step) < baseline + 0.5 && Map.getArray(step) >= baseline) || (baseline == 1 && Map.getArray(step) != -1)); i++) {
			step = step.addVector(march);
			drawImage((int) step.getX(), (int) step.getY(), 3, Render.getScreenBlend(color, smallShaft), Math.min(16, i)/16.0f, 0);
		}

		// Drawing the arrow head
		if (end.getY() < start.getY()) {
			drawImage((int) step.getX(), (int) step.getY(), 20, getScreenBlend(color, arrow), 1, end.subVec(start).getRadian());
		} else {
			drawImage((int) step.getX(), (int) step.getY(), 20, getScreenBlend(color, arrow), 1, end.subVec(start).getRadian() + 3.14f);
		}
	}

	}