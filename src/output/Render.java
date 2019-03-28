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
	 * Calls all objects to render to pixels int array, draws pixel array to screen.
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
		} else if (Main.gameState == StateID.DEFEAT || Main.gameState == StateID.PAUSED
				|| Main.gameState == StateID.VICTORY) {
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
		if (Main.gameState == StateID.ONGOING)
			world.drawCoins(this);
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
	 * @param x     The x coordinate of the top left corner of the rectangle
	 * @param y     The y coordinate of the top left corner of the rectangle
	 * @param w     The width of the rectangle
	 * @param h     The height of the rectangle
	 * @param color The color of the rectangle
	 * @param alpha The opacity of the rectangle
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
				if((((borders & 1) == 1) && x1 < 2) || (((borders & 2) == 2) && y1 <2) || (((borders & 4) == 4) && x1 > w-3) || (((borders & 8) == 8) && y1 > h-3)) {
					pixels[id] = (int) 230 << 16 | (int) 230 << 8 | (int) 230;
				} else {
					pixels[id] = newColor + newColor2;
				}
			}
		}
	}

	/**
	 * Draws an image
	 * 
	 * @param x     The x coordinate of the top left corner of the image
	 * @param y     The y coordinate of the top right corner of the image
	 * @param w     The width of the image
	 * @param image The image
	 */
	public void drawImage(int x, int y, Image img, float rotate) {
		int w = img.getWidth();
		int[] image = img.getPixels();
		int h = 1, r, g, b;
		if (w > 0) {
			h = image.length / w;
		}

		for (float i = 0; i < image.length; i+=0.3333f) {
			float alpha = ((image[(int)i] >> 24 & 255) / 255.0f)*img.getOpacity();
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
				if (id > 0 && id < width * height){
					// Finding alpha
					r = ((image[(int)i] >> 16) & 255);
					g = ((image[(int)i] >> 8) & 255);
					b = (image[(int)i] & 255);
					int newColor = (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);

					// Finding alpha
					r = ((pixels[id] >> 16) & 255);
					g = ((pixels[id] >> 8) & 255);
					b = (pixels[id] & 255);
					int newColor2 = (int) (r * (1 - alpha)) << 16 | (int) (g * (1 - alpha)) << 8
							| (int) (b * (1 - alpha));

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

		for (float i = 0; i < image.length; i+=0.3333f) {
			float alpha = ((image[(int)i] >> 24 & 255) / 255.0f)*img.getOpacity();
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
				if (id > 0 && id < width * height){
					// Finding alpha
					r = ((image[(int)i] >> 16) & 255);
					g = ((image[(int)i] >> 8) & 255);
					b = (image[(int)i] & 255);
					int newColor = (int) (r * alpha) << 16 | (int) (g * alpha) << 8 | (int) (b * alpha);

					// Finding alpha
					r = ((pixels[id] >> 16) & 255);
					g = ((pixels[id] >> 8) & 255);
					b = (pixels[id] & 255);
					int newColor2 = (int) (r * (1 - alpha)) << 16 | (int) (g * (1 - alpha)) << 8
							| (int) (b * (1 - alpha));

					// Finally adding colors to pixel array
					pixels[id] = newColor + newColor2;
				}
			}
		}
	}
	
	/**
	 * Draws a string
	 * 
	 * @param label String to be drawn
	 * @param x     coordinate value of the middle of the string
	 * @param y     coordinate value of the string
	 * @param font  Font to be used
	 * @param color Color of the string
	 */

	public void drawString(String label, int x, int y, Font font, int color) {
		int carriage = 0;
		int letter;
		int length = font.getStringWidth(label);
		Image letterImage;
		for (int i = 0; i < label.length(); i++) {
			letter = label.charAt(i);
			if (letter == 7) {
				letterImage = new Image("",font.getSize(),font.getSize(),font.getLetter(letter),((color>>24)&255)/255.0f,0).getScreenBlend(250 << 16 | 250 << 8);
			} else {
				letterImage = new Image("",font.getSize(),font.getSize(),font.getLetter(letter),((color>>24)&255)/255.0f,0).getScreenBlend(color);
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
				letterImage = new Image("",font.getSize(),font.getSize(),font.getLetter(letter),((color>>24)&255)/255.0f,0).getScreenBlend(250 << 16 | 250 << 8);
			} else {
				letterImage = new Image("",font.getSize(),font.getSize(),font.getLetter(letter),((color>>24)&255)/255.0f,0).getScreenBlend(color);
			}
			if(centered) {
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
	 * @param color The original color
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
		return 255<<24 | r << 16 | g << 8 | b;
	}

	/**
	 * Returns a darker color
	 * 
	 * @param color The original color
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
		return 255<<24 | r << 16 | g << 8 | b;
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
	 * @param color Original color
	 * @return A less saturated color
	 */
	public int desaturate(int color) {
		int r = ((color >> 16) & 255), g = ((color >> 8) & 255), b = (color & 255);
		int a = (r + g + b)/3;
		r = (a + r) >> 1;
		g = (a + g) >> 1;
		b = (a + b) >> 1;
		return r << 16 | g << 8 | b;
	}

	/**
	 * Takes an image and makes it darker and more desaturated
	 * 
	 * @param image Original image
	 * @return Darker and desaturated image
	 */
	public int[] darkenScreen(int[] image) {
		for (int i = 0; i < 1025 * 513; i++) {
			image[i] = desaturate(desaturate(image[i]));
		}
		return image;
	}

	/**
	 * @return An gray image
	 */
	public int[] eggShellScreen() {
		int[] image = new int[1025 * 513];
		for (int i = 0; i < 1025 * 513; i++) {
			int y = i / 1025;
			int value = (int)-(0.4*y) + 230;
			image[i] = value << 16 | value << 8 | value;
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
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color),
						p2.subVec(p1).getRadian());
			} else {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color),
						p2.subVec(p1).getRadian()+3.14f);
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
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color),
						p2.subVec(p1).getRadian());
			} else {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color),
						p2.subVec(p1).getRadian()+3.14f);
			}
		}
	}

	public void drawLine(Point p1, Point p2, int color, int background) {
		Point endPoint = p2;
		double slope = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX());
		if (p1.getX() < 1021 && p1.getX() >= 4 && p1.getY() < 509 && p1.getY() >= 4 && p2.getX() < 1020
				&& p2.getX() >= 4 && p2.getY() < 508 && p2.getY() >= 4 && p1.getDist(p2) > 32) {
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
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color),
						p2.subVec(p1).getRadian());
			} else {
				drawImage((int) endPoint.getX(), (int) endPoint.getY(), arrow.getScreenBlend(color),
						p2.subVec(p1).getRadian()+3.14f);
			}
		}
	}
}