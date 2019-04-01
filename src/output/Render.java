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

	// Ground Units
	public Image hitSprite = new Image("/res/ground/hit.png", 36, 20);
	public Image medArtRange = new Image("/res/ground/medArtRange.png", 128, 128);
	public Image heavyArtRange = new Image("/res/ground/heavyArtRange.png", 256, 256);

	// Buildings
	public Image cityHit = new Image("/res/buildings/buildingHit.png", 36, 36);

	// Misc
	public Image arrow = new Image("/res/arrow.png", 18, 9);
	public Image target = new Image("/res/target.png", 32, 32);

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
		// drawString("FPS:", 22, 10, font16, 250 << 16 | 250 << 8 | 250);
		// drawString(String.valueOf(Main.fps), 55, 10, font16, 250 << 16 | 250
		// << 8 | 250);
		if (Main.gameState == StateID.ONGOING) world.drawCoins(this);
		g.drawImage(img, 0, 0, null);
		g.dispose();
		bs.show();
	}

	/**
	 * Draws the longitude and latitude lines on the map
	 */
	void drawLongLat() {
		// int xCenter = (int) (512 * Main.zoom) - 512;
		// int yCenter = (int) (256 * Main.zoom) - 256;
		// Latitude
		for (int i = 0; i < 512 * 17; i++) {
			int x = (int) ((i % 17 * 64 / Main.zoom));
			int y = i / 17;
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
			int x = i % 1024;
			int y = (int) ((i / 1024) * 64 / Main.zoom);
			int id = y * (width + 1) + x;
			if (id < 1024 * 512 && id > 0) {
				int r = (int) (((pixels[id] >> 16) & 255) * .75);
				int g = (int) (((pixels[id] >> 8) & 255) * .75);
				int b = (int) ((pixels[id] & 255) * .75);
				pixels[id] = r << 16 | g << 8 | b;
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
	public void drawImage(int x, int y, Image img, float rotate) {
		int w = img.getWidth();
		int[] image = img.getPixels();
		int h = 1, r, g, b;
		if (w > 0) {
			h = image.length / w;
		}

		for (float i = 0; i < image.length; i += 0.3333f) {
			float alpha = ((image[(int) i] >> 24 & 255) / 255.0f) * img.getOpacity();
			double x1 = (i % w) - w / 2;
			double y1 = i / w - h / 2;
			double x2 = (int) (x1 + x);
			double y2 = (int) (y1 + y);
			if (alpha > 0) {
				x1 = (i % w) - w / 2;
				y1 = i / w - h / 2;
				if (rotate != 0) {
					x2 = (int) (x1 * Math.cos(-rotate) - y1 * Math.sin(-rotate) + x);
					y2 = (int) ((x1 * Math.sin(-rotate) + y1 * Math.cos(-rotate)) + y);
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

	public void drawImage(int x, int y, Image img) {
		float rotate = img.getRotate();
		int w = img.getWidth();
		int[] image = img.getPixels();
		int h = 1, r, g, b;
		if (w > 0) {
			h = image.length / w;
		}

		for (float i = 0; i < image.length; i += 0.3333f) {
			float alpha = ((image[(int) i] >> 24 & 255) / 255.0f) * img.getOpacity();
			double x1 = (i % w) - w / 2;
			double y1 = i / w - h / 2;
			double x2 = (int) (x1 + x);
			double y2 = (int) (y1 + y);
			if (alpha > 0) {
				x1 = (i % w) - w / 2;
				y1 = i / w - h / 2;
				if (rotate != 0) {
					x2 = (int) (x1 * Math.cos(-rotate) - y1 * Math.sin(-rotate) + x);
					y2 = (int) ((x1 * Math.sin(-rotate) + y1 * Math.cos(-rotate)) + y);
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
		Image letterImage;
		for (int i = 0; i < label.length(); i++) {
			letter = label.charAt(i);
			if (letter == 7) {
				letterImage = new Image("", font.getSize(), font.getSize(), font.getLetter(letter), ((color >> 24) & 255) / 255.0f, 0).getScreenBlend(250 << 16 | 250 << 8);
			} else {
				letterImage = new Image("", font.getSize(), font.getSize(), font.getLetter(letter), ((color >> 24) & 255) / 255.0f, 0).getScreenBlend(color);
			}
			drawImage(x + carriage - length / 2 + font.getSize() / 2, y, letterImage, 0);
			carriage += font.getKern(letter);
		}
	}

	public void drawString(String label, int x, int y, Font font, int color, boolean centered) {
		int carriage = 0;
		int letter;
		int length = font.getStringWidth(label);
		Image letterImage;
		for (int i = 0; i < label.length(); i++) {
			letter = label.charAt(i);
			if (letter == 7) {
				letterImage = new Image("", font.getSize(), font.getSize(), font.getLetter(letter), ((color >> 24) & 255) / 255.0f, 0).getScreenBlend(250 << 16 | 250 << 8);
			} else {
				letterImage = new Image("", font.getSize(), font.getSize(), font.getLetter(letter), ((color >> 24) & 255) / 255.0f, 0).getScreenBlend(color);
			}
			if (centered) {
				drawImage(x + carriage - length / 2 + font.getSize() / 2, y, letterImage, 0);
			} else {
				drawImage(x + carriage + font.getSize() / 2, y, letterImage, 0);
			}
			carriage += font.getKern(letter);
		}
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

	public static Image getWeighted(Image img, UnitID weight, int color) {
		if (weight == UnitID.LIGHT) {
			return img.getScreenBlend(lighten(color));
		}
		if (weight == UnitID.HEAVY) {
			return img.getScreenBlend(darken(color));
		}
		return img.getScreenBlend(color);
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
		int hue = getRGB(350,90,90);
		return hue << 16 | hue << 8 | hue;
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
		if(hue < 0){
			hue+=360;
		}
		return hue;
	}
	
	public int getRGB(double hue, double saturation, double value){
		if(hue < 0){
			hue+=360;
		}
		double c = value * saturation;
		double x = c*(1-Math.abs(((hue/60)%2)-1));
		double m = value - c;
		double r = 0;
		double g = 0;
		double b = 0;
		
		if(hue >= 0 && hue < 60){
			r = c;
			g = x;
			b = 0;
		} else if(hue >= 60 && hue < 120){
			r = x;
			g = c;
			b = 0;
		} else if(hue >= 120 && hue < 180){
			r = 0;
			g = c;
			b = x;
		} else if(hue >= 180 && hue < 240){
			r = 0;
			g = x;
			b = c;
		} else if(hue >= 240 && hue < 300){
			r = x;
			g = 0;
			b = c;
		} else if(hue >= 300 && hue < 360){
			r = c;
			g = 0;
			b = x;
		}
		return (int)((r+m)*255)<<16|(int)((g+m)*255)<<8|(int)((b+m)*255);
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
		for (int i = 0; i < 1025 * 513; i++) {
			int r = (int)(((image[i]>>16)&255)*(32/255.0));
			int g = (int)(((image[i]>>8)&255)*(32/255.0));
			int b = (int)(((image[i])&255)*(32/255.0));
			
			image[i] = r<<16|g<<8|b;
		}
		return image;
	}

	/**
	 * @return An gray image
	 */
	public int[] eggShellScreen() {
		int[] image = new int[1025 * 513];
		for (int i = 0; i < 1025 * 513; i++) {
			image[i] = 23<<16|23<<8|23;
		}
		return image;
	}

	public void drawLandLine(Point p1, Point p2, int color, int background) {
		Point endPoint = p2;
		double slope = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX());
		if (p2.getX() < 1021 && p2.getX() >= 5 && p2.getY() < 509 && p2.getY() >= 5 && p1.getDist(p2) > 32) {
			if (slope < 1 && slope > -1) {
				if (p2.getX() > p1.getX()) {
					for (double i = p1.getX(); i < p2.getX(); i++) {
						int x = (int) i;
						int y = (int) (slope * (i - p1.getX()) + p1.getY());
						int id = y * (width + 1) + x;
						if (Map.getArray((int) x, (int) y) < .5 || Map.getArray((int) x, (int) y) > 1) {
							endPoint.setX(x);
							endPoint.setY(y);
							break;
						}
						if (background != 0) {
							pixels[id - 4100] = background;
							pixels[id - 3075] = background;
						}
						pixels[id - 2050] = 0;
						pixels[id - 1025] = 0;
						pixels[id] = color;
						pixels[id + 1025] = color;
						pixels[id + 2050] = 0;
						pixels[id + 3075] = background;
						if (background != 0) {
							pixels[id + 5125] = background;
							pixels[id + 4100] = background;
						}
					}
				} else {
					for (double i = p1.getX(); i > p2.getX(); i--) {
						int x = (int) i;
						int y = (int) (slope * (i - p1.getX()) + p1.getY());
						int id = y * (width + 1) + x;
						if (Map.getArray((int) x, (int) y) < .5 || Map.getArray((int) x, (int) y) > 1) {
							endPoint.setX(x);
							endPoint.setY(y);
							break;
						}
						if (background != 0) {
							pixels[id - 5125] = background;
							pixels[id - 4100] = background;
						}
						pixels[id - 3075] = 0;
						pixels[id - 2050] = 0;
						pixels[id - 1025] = color;
						pixels[id] = color;
						pixels[id + 1025] = 0;
						pixels[id + 2050] = 0;
						if (background != 0) {
							pixels[id + 4100] = background;
							pixels[id + 3075] = background;
						}
					}
				}
			} else {
				if (p2.getY() > p1.getY()) {
					for (int y = (int) p1.getY(); y < p2.getY(); y++) {
						int x = (int) ((y - p1.getY()) / slope + p1.getX());
						int id = (int) (y * (width + 1)) + x;
						if (Map.getArray((int) x, (int) y) < .5 || Map.getArray((int) x, (int) y) > 1) {
							endPoint.setX(x);
							endPoint.setY(y);
							break;
						}
						if (background != 0) {
							pixels[id - 4] = background;
							pixels[id - 3] = background;
						}
						pixels[id - 2] = 0;
						pixels[id - 1] = 0;
						pixels[id] = color;
						pixels[id + 1] = color;
						pixels[id + 2] = 0;
						pixels[id + 3] = 0;
						if (background != 0) {
							pixels[id + 5] = background;
							pixels[id + 4] = background;
						}
					}
				} else {
					for (int y = (int) p1.getY(); y > p2.getY(); y--) {
						int x = (int) ((y - p1.getY()) / slope + p1.getX());
						int id = (int) (y * (width + 1)) + x;
						if (Map.getArray((int) x, (int) y) < .5 || Map.getArray((int) x, (int) y) > 1) {
							endPoint.setX(x);
							endPoint.setY(y);
							break;
						}
						if (background != 0) {
							pixels[id - 5] = background;
							pixels[id - 4] = background;
						}
						pixels[id - 3] = 0;
						pixels[id - 2] = 0;
						pixels[id - 1] = color;
						pixels[id] = color;
						pixels[id + 1] = 0;
						pixels[id + 2] = 0;
						if (background != 0) {
							pixels[id + 4] = background;
							pixels[id + 3] = background;
						}
					}
				}
			}
			if (p2.getY() < p1.getY()) {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color), p2.subVec(p1).getRadian());
			} else {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color), p2.subVec(p1).getRadian() + 3.14f);
			}
		}
	}

	public void drawSeaLine(Point p1, Point p2, int color, int background) {
		Point endPoint = p2;
		double slope = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX());
		if (p2.getX() < 1021 && p2.getX() >= 4 && p2.getY() < 509 && p2.getY() >= 4 && p1.getDist(p2) > 32) {
			if (slope < 1 && slope > -1) {
				if (p2.getX() > p1.getX()) {
					for (double i = p1.getX(); i < p2.getX(); i++) {
						int x = (int) i;
						int y = (int) (slope * (i - p1.getX()) + p1.getY());
						int id = y * (width + 1) + x;
						if (Map.getArray((int) x, (int) y) > .5) {
							endPoint.setX(x);
							endPoint.setY(y);
							break;
						}
						if (background != 0) {
							pixels[id - 4100] = background;
							pixels[id - 3075] = background;
						}
						pixels[id - 2050] = 0;
						pixels[id - 1025] = 0;
						pixels[id] = color;
						pixels[id + 1025] = color;
						pixels[id + 2050] = 0;
						pixels[id + 3075] = background;
						if (background != 0) {
							pixels[id + 5125] = background;
							pixels[id + 4100] = background;
						}
					}
				} else {
					for (double i = p1.getX(); i > p2.getX(); i--) {
						int x = (int) i;
						int y = (int) (slope * (i - p1.getX()) + p1.getY());
						int id = y * (width + 1) + x;
						if (Map.getArray((int) x, (int) y) > .5) {
							endPoint.setX(x);
							endPoint.setY(y);
							break;
						}
						if (background != 0) {
							pixels[id - 5125] = background;
							pixels[id - 4100] = background;
						}
						pixels[id - 3075] = 0;
						pixels[id - 2050] = 0;
						pixels[id - 1025] = color;
						pixels[id] = color;
						pixels[id + 1025] = 0;
						pixels[id + 2050] = 0;
						if (background != 0) {
							pixels[id + 4100] = background;
							pixels[id + 3075] = background;
						}
					}
				}
			} else {
				if (p2.getY() > p1.getY()) {
					for (int y = (int) p1.getY(); y < p2.getY(); y++) {
						int x = (int) ((y - p1.getY()) / slope + p1.getX());
						int id = (int) (y * (width + 1)) + x;
						if (Map.getArray((int) x, (int) y) > .5) {
							endPoint.setX(x);
							endPoint.setY(y);
							break;
						}
						if (background != 0) {
							pixels[id - 4] = background;
							pixels[id - 3] = background;
						}
						pixels[id - 2] = 0;
						pixels[id - 1] = 0;
						pixels[id] = color;
						pixels[id + 1] = color;
						pixels[id + 2] = 0;
						pixels[id + 3] = 0;
						if (background != 0) {
							pixels[id + 5] = background;
							pixels[id + 4] = background;
						}
					}
				} else {
					for (int y = (int) p1.getY(); y > p2.getY(); y--) {
						int x = (int) ((y - p1.getY()) / slope + p1.getX());
						int id = (int) (y * (width + 1)) + x;
						if (Map.getArray((int) x, (int) y) > .5) {
							endPoint.setX(x);
							endPoint.setY(y);
							break;
						}
						if (background != 0) {
							pixels[id - 5] = background;
							pixels[id - 4] = background;
						}
						pixels[id - 3] = 0;
						pixels[id - 2] = 0;
						pixels[id - 1] = color;
						pixels[id] = color;
						pixels[id + 1] = 0;
						pixels[id + 2] = 0;
						if (background != 0) {
							pixels[id + 4] = background;
							pixels[id + 3] = background;
						}
					}
				}
			}
			if (p2.getY() < p1.getY()) {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color), p2.subVec(p1).getRadian());
			} else {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color), p2.subVec(p1).getRadian() + 3.14f);
			}
		}
	}

	public void drawLine(Point p1, Point p2, int color, int background) {
		Point endPoint = p2;
		double slope = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX());
		if (p1.getX() < 1021 && p1.getX() >= 4 && p1.getY() < 509 && p1.getY() >= 4 && p2.getX() < 1020 && p2.getX() >= 4 && p2.getY() < 508 && p2.getY() >= 4 && p1.getDist(p2) > 32) {
			if (slope < 1 && slope > -1) {
				if (p2.getX() > p1.getX()) {
					for (double i = p1.getX(); i < p2.getX(); i++) {
						int x = (int) i;
						int y = (int) (slope * (i - p1.getX()) + p1.getY());
						int id = y * (width + 1) + x;
						if (background != 0) {
							pixels[id - 4100] = background;
							pixels[id - 3075] = background;
						}
						pixels[id - 2050] = 0;
						pixels[id - 1025] = 0;
						pixels[id] = color;
						pixels[id + 1025] = color;
						pixels[id + 2050] = 0;
						pixels[id + 3075] = background;
						if (background != 0) {
							pixels[id + 5125] = background;
							pixels[id + 4100] = background;
						}
					}
				} else {
					for (double i = p1.getX(); i > p2.getX(); i--) {
						int x = (int) i;
						int y = (int) (slope * (i - p1.getX()) + p1.getY());
						int id = y * (width + 1) + x;
						if (background != 0) {
							pixels[id - 5125] = background;
							pixels[id - 4100] = background;
						}
						pixels[id - 3075] = 0;
						pixels[id - 2050] = 0;
						pixels[id - 1025] = color;
						pixels[id] = color;
						pixels[id + 1025] = 0;
						pixels[id + 2050] = 0;
						if (background != 0) {
							pixels[id + 4100] = background;
							pixels[id + 3075] = background;
						}
					}
				}
			} else {
				if (p2.getY() > p1.getY()) {
					for (int y = (int) p1.getY(); y < p2.getY(); y++) {
						int x = (int) ((y - p1.getY()) / slope + p1.getX());
						int id = (int) (y * (width + 1)) + x;
						if (background != 0) {
							pixels[id - 4] = background;
							pixels[id - 3] = background;
						}
						pixels[id - 2] = 0;
						pixels[id - 1] = 0;
						pixels[id] = color;
						pixels[id + 1] = color;
						pixels[id + 2] = 0;
						pixels[id + 3] = 0;
						if (background != 0) {
							pixels[id + 5] = background;
							pixels[id + 4] = background;
						}
					}
				} else {
					for (int y = (int) p1.getY(); y > p2.getY(); y--) {
						int x = (int) ((y - p1.getY()) / slope + p1.getX());
						int id = (int) (y * (width + 1)) + x;
						if (background != 0) {
							pixels[id - 5] = background;
							pixels[id - 4] = background;
						}
						pixels[id - 3] = 0;
						pixels[id - 2] = 0;
						pixels[id - 1] = color;
						pixels[id] = color;
						pixels[id + 1] = 0;
						pixels[id + 2] = 0;
						if (background != 0) {
							pixels[id + 4] = background;
							pixels[id + 3] = background;
						}
					}
				}
			}
			if (p2.getY() < p1.getY()) {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color), p2.subVec(p1).getRadian());
			} else {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color), p2.subVec(p1).getRadian() + 3.14f);
			}
		}
	}
}