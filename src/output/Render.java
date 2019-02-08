package output;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import main.Image;
import main.Main;
import main.SpriteSheet;
import main.StateID;
import main.World;
import objects.gui.Font;
import terrain.Map;

public class Render extends Canvas {

	// tf does serialVersion even do lmao
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

	Image image = new Image();
	// Ground Units
	public int[] artillery = image.loadImage("/res/ground/artillery.png", 32, 16);
	public int[] infantry = image.loadImage("/res/ground/infantry.png", 32, 16);
	public int[] cavalry = image.loadImage("/res/ground/cavalry.png", 32, 16);
	public int[] hitSprite = image.loadImage("/res/ground/hit.png", 36, 20);

	// Water Units
	public int[] landing = image.loadImage("/res/water/landing.png", 13, 32);
	public int[] destroyer = image.loadImage("/res/water/destroyer.png", 13, 45);
	public int[] cruiser = image.loadImage("/res/water/cruiser.png", 16, 61);
	public int[] landingHit = image.loadImage("/res/water/landingHit.png", 17, 36);
	public int[] destroyerHit = image.loadImage("/res/water/destroyerHit.png", 17, 49);
	public int[] cruiserHit = image.loadImage("/res/water/cruiserHit.png", 20, 65);

	// Air Units
	public int[] fighter1 = image.loadImage("/res/air/fighter.png", 36, 35);
	public int[] fighter2 = image.loadImage("/res/air/fighter1.png", 36, 35);
	public int[] attacker1 = image.loadImage("/res/air/attack.png", 44, 33);
	public int[] attacker2 = image.loadImage("/res/air/attack1.png", 44, 33);
	public int[] bomber1 = image.loadImage("/res/air/bomber1.png", 67, 40);
	public int[] bomber2 = image.loadImage("/res/air/bomber2.png", 67, 40);
	public int[] fighterHit = image.loadImage("/res/air/fighterHit.png", 40, 39);
	public int[] attackerHit = image.loadImage("/res/air/attackHit.png", 48, 37);
	public int[] bomberHit = image.loadImage("/res/air/bomberHit.png", 71, 44);

	// Buildings
	public int[] city = image.loadImage("/res/buildings/city.png", 32, 32);
	public int[] port = image.loadImage("/res/buildings/port.png", 32, 32);
	public int[] factory = image.loadImage("/res/buildings/factory.png", 32, 32);
	public int[] capital = image.loadImage("/res/buildings/capital.png", 32, 32);
	public int[] airfield = image.loadImage("/res/buildings/airfield.png", 32, 32);
	public int[] cityHit = image.loadImage("/res/buildings/buildingHit.png", 36, 36);

	// Projectiles
	public int[] shell = image.loadImage("/res/projectiles/shell.png", 4, 4);
	public int[] torpedo = image.loadImage("/res/projectiles/torpedo.png", 2, 7);
	public int[] bullet = image.loadImage("/res/projectiles/bullet.png", 2, 2);
	public int[] bomb = image.loadImage("/res/projectiles/bomb.png", 16, 8);

	// Misc
	public int[] coin = image.loadImage("/res/coin.png", 16, 16);

	// Fonts
	public Font font32 = new Font(new SpriteSheet("/res/fonts/font32.png", 512),32);
	public Font font16 = new Font(new SpriteSheet("/res/fonts/font16.png", 256),16);

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
			drawLongLat();
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

		g.drawImage(img, 0, 0, null);
		if (Main.gameState == StateID.ONGOING) world.drawCoins(g);
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
		// Latitude
		for (int i = 0; i < 512 * 17; i++) {
			int x = (int) (i % 17) * 64;
			int y = (int) (i / 17);
			int id = y * (width + 1) + x;
			if (x < 1025 && x > 0) {
				int r = (int) (((pixels[id] >> 16) & 255) * .75);
				int g = (int) (((pixels[id] >> 8) & 255) * .75);
				int b = (int) ((pixels[id] & 255) * .75);
				pixels[id] = r << 16 | g << 8 | b;
			}
		}
		// Longitude
		for (int i = 0; i < 512 * 18; i++) {
			int x = (int) (i % 1024);
			int y = (int) ((i / 1024) * 64);
			int id = y * (width + 1) + x;
			if (id < 1024 * 512 && id > 0) {
				int r = (int) (((pixels[id] >> 16) & 255) * .75);
				int g = (int) (((pixels[id] >> 8) & 255) * .75);
				int b = (int) ((pixels[id] & 255) * .75);
				pixels[id] = r << 16 | g << 8 | b;
			}
		}
	}

	// drawRect(...): draws a rectangle
	public void drawRect(int x, int y, int w, int h, int color) {
		for (int i = 0; i < w * h; i++) {
			int x1 = (int) (i % w) + x;
			int y1 = (int) (i / w);
			int id = (y1 + y) * (width + 1) + x1;
			if (id < 1025 * 513 && id >= 0) {
				pixels[id] = color;
			}
		}
	}

	public void drawRect(int x, int y, int w, int h, int color, float alpha) {
		for (int i = 0; i < w * h; i++) {
			int x1 = (int) (i % w) + x;
			int y1 = (int) (i / w);
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

	// drawImage(...): draws an image
	public void drawImage(int x, int y, int w, int[] image) {
		for (int i = 0; i < image.length; i++) {
			int color = image[i];
			float alpha = (color >> 24 & 255) / 255.0f;
			int x1 = (int) (i % w) + x;
			int y1 = (int) (i / w);
			int id = (y1 + y) * (width + 1) + x1;
			int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
			int newColor = (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);
			r = ((pixels[id] >> 16) & 255);
			g = ((pixels[id] >> 8) & 255);
			b = (pixels[id] & 255);
			int newColor2 = (int) (r * (1 - alpha)) << 16 | (int) (g * (1 - alpha)) << 8 | (int) (b * (1 - alpha));
			pixels[id] = (int) (newColor + (newColor2));
		}
	}

	// drawImageScreen(...): draws an image, applies overlay blending
	public void drawImageScreen(int x, int y, float w, int[] image, int color) {
		int h = (int) (image.length / w);
		int x1, y1, id, r, g, b, newColor, newColor2;
		float screen, alpha;
		for (int i = 0; i < image.length; i++) {
			alpha = (image[i] >> 24 & 255) / 255.0f;
			if (alpha > 0.9f) {
				// Finding position on game pixel array
				x1 = (int) ((i % w) + x - w / 2);
				y1 = (int) (i / w) - h / 2;
				id = (int) ((y1 + y) * (width + 1) + x1);
				if (x >= 0 && x < 1025 && id > 0 && id < 1025 * 513) {
					// Finding and splitting starting colors
					screen = (image[i] & 255) / 255.0f;
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
					newColor = (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);

					// Finding alpha
					r = ((pixels[id] >> 16) & 255);
					g = ((pixels[id] >> 8) & 255);
					b = (pixels[id] & 255);
					newColor2 = (int) (r * (1 - alpha)) << 16 | (int) (g * (1 - alpha)) << 8 | (int) (b * (1 - alpha));

					// Finally adding colors to pixel array
					pixels[id] = (int) (newColor + (newColor2));
				}
			}
		}
	}
	public void drawLetter(int x, int y, float w, int[] image, int color) {
		int h = (int) (image.length / w);
		int x1, y1, id, r, g, b, newColor, newColor2;
		float screen, alpha;
		for (int i = 0; i < image.length; i++) {
			alpha = (image[i] >> 24 & 255) / 255.0f;
			if (alpha > 0.9f) {
				// Finding position on game pixel array
				x1 = (int) ((i % w) + x);
				y1 = (int) (i / w);
				id = (int) ((y1 + y) * (width + 1) + x1);
				if (x >= 0 && x < 1025 && id > 0 && id < 1025 * 513) {
					// Finding and splitting starting colors
					screen = (image[i] & 255) / 255.0f;
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
					newColor = (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);

					// Finding alpha
					r = ((pixels[id] >> 16) & 255);
					g = ((pixels[id] >> 8) & 255);
					b = (pixels[id] & 255);
					newColor2 = (int) (r * (1 - alpha)) << 16 | (int) (g * (1 - alpha)) << 8 | (int) (b * (1 - alpha));

					// Finally adding colors to pixel array
					pixels[id] = (int) (newColor + (newColor2));
				}
			}
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
		r <<= 2;
		g <<= 2;
		b <<= 2;
		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;
		return r << 16 | g << 8 | b;
	}

	// lighten(int color): returns a darker color given a 24 bit int color
	public int darken(int color) {
		int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
		r >>= 1;
		g >>= 1;
		b >>= 1;
		if (r < 0) r = 0;
		if (g < 0) g = 0;
		if (b < 0) b = 0;
		return r << 16 | g << 8 | b;
	}

	public int desaturate(int color) {
		int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
		int a = (int) (r + g + b) >> 2;
		r = (a + r) >> 1;
		g = (a + g) >> 1;
		b = (a + b) >> 1;
		return r << 16 | g << 8 | b;
	}

	public int[] darkenScreen(int[] image) {
		for (int i = 0; i < 1025 * 513; i++) {
			image[i] = darken(desaturate(pixels[i]));
		}
		return image;
	}

	public int[] eggShellScreen() {
		int[] image = new int[1025 * 513];
		for (int i = 0; i < 1025 * 513; i++) {
			image[i] = 128 << 16 | 125 << 8 | 122;
		}
		return image;
	}

	public void drawString(String label, int x, int y, int size, Font font, int color) {
		int carriage = 0;
		int letter;
		int length = font.getStringWidth(label);
		int fix = 0;
		if (size == 16) {
			fix = 9;
		} else if (size == 32) {
			fix = -18;
		}
		for (int i = 0; i < label.length(); i++) {
			letter = (int) label.charAt(i);
			if (letter != 13) {
				drawLetter(x + carriage - length / 2 + fix, y-5, size, font.getLetter(letter), color);
				carriage += font.getKern(letter)+3;
			}
		}
	}
}