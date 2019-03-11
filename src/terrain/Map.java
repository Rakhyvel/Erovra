package terrain;

import java.util.Random;

import main.Image;
import main.Main;
import main.MapID;
import utility.Point;

public class Map {

	Random rand = new Random();
	public static float[][] mountain = new float[1025][513];
	public static int[] mapData = new int[1025 * 513];
	public static float[] islandMask = new float[513 * 1025];
	public static Point[] points = new Point[7];
	Image image = new Image();

	/**
	 * Uses simplex noise and linear interpolation to render the terrain for the
	 * game
	 * 
	 * @param seed
	 *            Random seed to be used for the generation
	 * @param id
	 *            Which type of generation to be done
	 */
	public void generateMap(int seed, MapID id) {
		rand.setSeed(seed);
		if (id == MapID.CUSTOM) {
			int[] customImage = image.loadImage("/res/island.jpg", 1024, 512);
			for (int i = 0; i < 1024 * 512 && i < customImage.length; i++) {
				int x = (i % 1024);
				int y = (i / 1024);
				mountain[x][y] = (customImage[i] & 255) / 555.0f + .49f;
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
					islandMask[i] = ((1 - ((smallestDistance) / 25000.0f)) / 2 + 0.2f);
				}
			}

			if (id == MapID.PLAINS) {
				generatePlains();
			} else if (id == MapID.RIVER) {
				generateRiver();
			} else if (id == MapID.ISLANDS) {
				generateIslands();
			} else if (id == MapID.SEA) {
				generateSea();
			} else if (id == MapID.MOUNTAIN) {
				generateMountain();
			}
			int a = 128;
			for (int i = 0; i < 1025 * 513; i++) {
				int x = i % 1025;
				int y = i / 1025;
				int x1 = (int) (x / a) * a;
				int y1 = (int) (y / a) * a;
				if (x != x1 || y != y1) {
					mountain[x][y] = bicosineInterpolation(x1, y1, a, x, y);
				}
			}
		}
		for (int i = 0; i < 1025 * 513; i++) {
			int x = i % 1025;
			int y = i / 1025;
			mountain[x][y] = transform(mountain[x][y], 1.5f);
			if (mountain[x][y] > 1.5) mountain[x][y] = 1.5f;
			if (mountain[x][y] < 0) mountain[x][y] = 0f;
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
	
	int[][] perlinNoise(int frequency, int amplitude){
		int[][] noise = new int[1025][513];
		if(frequency > 0 && frequency < 9) {
			int wavelength = 1 << (-frequency + 9);
			
		}
		return noise;
	}

	void generateMountain() {
		for (int i = 0; i < 45; i++) {
			int x = (i % 9);
			int y = (i / 9);
			float r = rand.nextFloat();
			if (r < 1) {
				mountain[x * 128][y * 128] = (r * r * .8f) + .6f;
			} else {
				mountain[x * 128][y * 128] = r;
			}
		}
	}

	void generatePlains() {
		for (int i = 0; i < 45; i++) {
			int x = (i % 9);
			int y = (i / 9);
			mountain[x * 128][y * 128] = rand.nextFloat() / 3 + 0.6f;
		}
	}

	void generateIslands() {
		for (int i = 0; i < 45; i++) {
			int x = (i % 9);
			int y = (i / 9);
			int pos = (y * 128) * 1025 + (x * 128);
			mountain[x * 128][y * 128] = islandMask[pos];
		}
	}

	void generateRiver() {
		for (int i = 0; i < 45; i++) {
			int x = (i % 9);
			int y = (i / 9);
			mountain[x * 128][y * 128] = Math.max(((1 - x) + (1 - y) + 10), x + y) * 0.07f;
		}
	}
	
	void generateSea(){
		for (int i = 0; i < 45; i++) {
			int x = (i % 9);
			int y = (i / 9);if (x == 0 || x == 8) {
				mountain[x * 128][y * 128] = .9f;
			} else if (x == 1 || x == 7) {
				mountain[x * 128][y * 128] = rand.nextFloat() * .5f + .25f;
			} else {
				if (x != 4) {
					mountain[x * 128][y * 128] = (x - 4) * (x - 4) * (x - 4) * (x - 4) * 0.003124f;
				} else {
					mountain[x * 128][y * 128] = 0;
				}
			}
		}
	}

	float transform(float r, float nonlinearity) {
		double verticalStretch = 0.762799 / nonlinearity - .0787654;
		double newHeight = (verticalStretch * Math.tan(nonlinearity * r - (nonlinearity * 0.5)) + 0.5);
		if (newHeight > 1) newHeight = 2 * (r - 1) * (r - 1) + 1;
		return (float) newHeight;
	}

	float bicosineInterpolation(int x1, int y1, int p, int x2, int y2) {
		// (x1,y1)- coords of top left corner of box
		// p- length/heigth pf box
		// (x3,y3)- coords of point
		if (x1 + p > 1025 || y1 + p > 513) {
			return 1.5f;
		}
		float topInterpolation = cosineInterpolation(x1, mountain[(int) x1][(int) y1], x1 + p, mountain[(int) (x1 + p)][(int) y1], x2);
		float bottomInterpolation = cosineInterpolation(x1, mountain[(int) x1][(int) (y1 + p)], x1 + p, mountain[(int) (x1 + p)][(int) (y1 + p)], x2);
		return cosineInterpolation(y1, topInterpolation, y1 + p, bottomInterpolation, y2);
	}

	float cosineInterpolation(int x1, float y1, int x2, float y2, int m) {
		double xDiff = (x2 - x1);
		double mu2 = (1 - Math.cos((m / xDiff - x1 / xDiff) * Math.PI)) / 2;
		double y3 = (y1 * (1 - mu2) + y2 * mu2);
		return (float) y3;
	}

	float bilinearInterpolation(int x1, int y1, int p, int x2, int y2) {
		// (x1,y1)- coords of top left corner of box
		// p- length/heigth pf box
		// (x3,y3)- coords of point
		if (x1 + p > 1025 || y1 + p > 513) {
			return 1.5f;
		}
		float topInterpolation = linearInterpolation(x1, mountain[(int) x1][(int) y1], x1 + p, mountain[(int) (x1 + p)][(int) y1], x2);
		float bottomInterpolation = linearInterpolation(x1, mountain[(int) x1][(int) (y1 + p)], x1 + p, mountain[(int) (x1 + p)][(int) (y1 + p)], x2);
		return linearInterpolation(y1, topInterpolation, y1 + p, bottomInterpolation, y2);
	}

	float linearInterpolation(int x1, float y1, int x2, float y2, int m) {
		return (y2 - y1) / (x2 - x1) * (m - x1) + y1;
	}

	// getArray(...): returns the float value at a given coordinate
	/**
	 * @param x
	 *            coordiate on the map
	 * @param y
	 *            coordiate on the map
	 * @return The float value of the given position on the terrain
	 */
	public static float getArray(int x, int y) {
		return mountain[x][y];
	}

	/**
	 * @param p
	 *            Point on the map
	 * @return The float value of the given position on the terrain
	 */
	public static float getArray(Point p) {
		if (p.getX() > 0 && p.getX() < 1024 && p.getY() > 0 && p.getY() < 512) return mountain[(int) p.getX()][(int) p.getY()];
		return -1;
	}

	/**
	 * @param value
	 *            The float value from 0f-1f
	 * @return A color based on the depth, 0 being a deep blue, 0.5f being
	 *         coast, and 1 being green plains.
	 */
	int getColor(float value) {
		int blue = 0;
		int green = 0;
		int red = 0;

		if (value < .495f) {
			blue = (int) (460 * value + 38);
			red = (int) (820 * value * value - 6);
			green = (int) (1040 * value * value - 6);
		} else if (value < .5) {
			blue = (int) (255);
			green = (int) (255);
			red = (int) (255);
		} else if (value < 1) {
			blue = (int) (730 * (value - 1) * (value - 1) - 9);
			green = (int) (-290 * (value - 1) + 80);
			red = (int) (1000 * (value - 1) * (value - 1) - 12);
		} else {
			blue = (int) (value * value * value * 85);
			green = (int) (value * value * value * 85);
			red = (int) (value * value * value * 85);
		}
		//
		// blue = (int) (value * 255);
		// green = (int) (value * 255);
		// red = (int) (value * 255);

		if (blue < 0) blue = 0;
		if (green < 0) green = 0;
		if (red < 0) red = 0;
		if (blue > 255) blue = 255;
		if (green > 255) green = 255;
		if (red > 255) red = 255;
		return 255 << 24 | red << 16 | green << 8 | blue;
	}
}