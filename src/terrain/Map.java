package terrain;

import java.awt.image.BufferedImage;
import java.util.Random;

import main.Image;
import main.Main;
import main.MapID;
import utility.Point;

public class Map {

	Random rand = new Random();
	public static float[][] mountain = new float[Main.width + 1][Main.height + 1];
	public static int[] mapData = new int[513 * 1025];
	public static float[] islandMask = new float[513 * 1025];
	public static Point[] points = new Point[7];
	Image image = new Image();

	/**
	 * Uses simplex noise and linear interpolation to render the terrain for the
	 * game
	 * 
	 * @param seed Random seed to be used for the generation
	 * @param id   Which type of generation to be done
	 */
	public void generateMap(int seed, MapID id) {
		rand.setSeed(seed);
		if (id == MapID.CUSTOM) {
			int[] customImage = image.loadImage("/res/denmark.png", 1024, 512);
			for (int i = 0; i < 1024 * 512; i++) {
				int x = (i % 1024);
				int y = (i / 1024);
				mountain[x][y] = ((customImage[i] & 255) / 255.0f) + .49f;
			}
		} else {
			if (id == MapID.ISLANDS) {
				for (int i = 0; i < 7; i++) {
					points[i] = new Point(i % 7 * 170, rand.nextInt(512));
				}
				points[0].setY(64 + rand.nextInt(100));
				points[0].setX(64);
				points[6].setY(400 + rand.nextInt(100));
				points[6].setX(960);
				for (int i = 0; i < 1025 * 513; i++) {
					int x = i % 1025;
					int y = i / 1025;
					int smallestDistance = 35000;
					Point point = new Point(x, y);
					for (int i2 = 0; i2 < 7; i2++) {
						int tempDist = (int) point.getDist(points[i2]);
						if (tempDist < smallestDistance) {
							smallestDistance = tempDist;
						}
					}
					islandMask[i] = (1 - ((smallestDistance) / 25000.0f)) / 2 + 0.2f;
				}
			}
			for (int i = 0; i < 45; i++) {
				int x = (i % 9);
				int y = (i / 9);
				if (id == MapID.PLAINS) {
					mountain[x * 128][y * 128] = rand.nextFloat() / 3 + 0.6f;
				} else if (id == MapID.RIVER) {
					mountain[x * 128][y * 128] = Math.max(((1 - x) + (1 - y) + 10), x + y) * 0.07f;
				} else if (id == MapID.ISLANDS) {
					int pos = (y * 128) * 1025 + (x * 128);
					mountain[x * 128][y * 128] = islandMask[pos];
				} else if (id == MapID.SEA) {
					if (x == 0 || x == 8) {
						mountain[x * 128][y * 128] = .9f;
					} else if (x == 1 || x == 7) {
						mountain[x * 128][y * 128] = rand.nextFloat() * .5f + .25f;
					} else {
						if (x != 4) {
							mountain[x * 128][y * 128] = (x - 4) * (x - 4) * 0.05f;
						} else {
							mountain[x * 128][y * 128] = 0;
						}
					}
				} else if (id == MapID.MOUNTAIN) {
					float r = rand.nextFloat();
					mountain[x * 128][y * 128] = (r * r) + .5f;
				}
			}

			for (int i = 2; i <= 8; i++) {
				// int p: two to the power of i
				// int r: how many squares there are
				// int s: how many squares on the edge there are
				int p = 1 << (8 - i);
				int r = 1 << (2 * i);
				int s = 2 << i;

				// Interpolation for the squares on the top and left edges
				for (int i2 = 0; i2 < 2 * r; i2++) {
					int x = i2 % s * 2 * p;
					int y = i2 / s * 2 * p;

					float m = (rand.nextFloat() - .5f) / (4 << i);
					if (x + 2 * p < Main.width + 1) {
						mountain[x + p][y] = ((mountain[x][y] + mountain[x + 2 * p][y]) / 2) + 2 * m;
					}
					m = (rand.nextFloat() - .5f) / (4 << i);
					if (y + 2 * p < Main.height + 1) {
						mountain[x][y + p] = ((mountain[x][y] + mountain[x][y + 2 * p]) / 2) + 2 * m;
					}
				}
				// Interpolation for the rest
				for (int i2 = 0; i2 < 2 * r; i2++) {
					int x = i2 % s * 2 * p;
					int y = i2 / s * 2 * p;
					// m: a random
					float m = 0;

					if (x + 2 * p < Main.width + 1 && y + 2 * p < Main.height + 1) {
						m = (rand.nextFloat() - .5f) / (4 << i);
						mountain[x + p][y + 2 * p] = ((mountain[x][y + 2 * p] + mountain[x + 2 * p][y + 2 * p]) * .5f)
								+ 2 * m;

						m = (rand.nextFloat() - .5f) / (4 << i);
						mountain[x + 2 * p][y + p] = ((mountain[x + 2 * p][y] + mountain[x + 2 * p][y + 2 * p]) * .5f)
								+ 2 * m;

						m = (rand.nextFloat() - .5f) / (4 << i);
						mountain[x + p][y + p] = ((mountain[x + p][y + 2 * p] + mountain[x + 2 * p][y + p]
								+ mountain[x][y + p] + mountain[x + p][y]) * .25f) + 4 * m;
					}
				}

			}
		}
		// colors the MapArray
		for (int i = 0; i < 1025 * 513; i++) {
			int x = i % 1025;
			int y = i / 1025;
			if (id == MapID.ISLANDS) {

				mapData[i] = getColor(getArray(x, y));
			} else {
				mapData[i] = getColor(getArray(x, y));
			}
		}
	}

	// getArray(...): returns the float value at a given coordinate
	/**
	 * @param x coordiate on the map
	 * @param y coordiate on the map
	 * @return The float value of the given position on the terrain
	 */
	public static float getArray(int x, int y) {
		return mountain[x][y];
	}

	/**
	 * @param p Point on the map
	 * @return The float value of the given position on the terrain
	 */
	public static float getArray(Point p) {
		if (p.getX() > 0 && p.getX() < 1024 && p.getY() > 0 && p.getY() < 512)
			return mountain[(int) p.getX()][(int) p.getY()];
		return -1;
	}

	/**
	 * @param value The float value from 0f-1f
	 * @return A color based on the depth, 0 being a deep blue, 0.5f being coast,
	 *         and 1 being green plains.
	 */
	int getColor(float value) {
		int blue = 0;
		int green = 0;
		int red = 0;

		if (value < .5f) {
			blue = (int) (400 * (value - .5f) + 250);
			green = (int) (500 * (value - .5f) + 200);
			red = (int) (600 * (value - .5f) + 150);
		} else if (value < 1) {
			blue = (int) (-400 * (value - .5f) + 130);
			green = (int) (-200 * (value - .5f) + 200);
			red = (int) (-600 * (value - .5f) + 200);
		} else {
			blue = (int) (value * value * value * 85);
			green = (int) (value * value * value * 85);
			red = (int) (value * value * value * 85);
		}
		if (blue < 0)
			blue = 0;
		if (green < 0)
			green = 0;
		if (red < 0)
			red = 0;
		if (blue > 255)
			blue = 255;
		if (green > 255)
			green = 255;
		if (red > 255)
			red = 255;
		return 255 << 24 | red << 16 | green << 8 | blue;
	}
}