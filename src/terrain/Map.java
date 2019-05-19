package terrain;

import java.util.Random;

import objects.gui.Image;
import main.MapID;
import utility.Point;

public class Map {

	Random rand = new Random();
	public static float[][] mountain = new float[1025][513];
	public static float[][] mountainFinal = new float[1025][513];
	public static int[] mapData = new int[1025 * 513];
	public static float[] islandMask = new float[513 * 1025];
	public static Point[] points = new Point[1025];
	MapID id;
	Image mapImg = new Image();

	/**
	 * Uses simplex noise and linear interpolation to render the terrain for the
	 * game
	 * 
	 * @param seed
	 *            Random seed to be used for the generation
	 * @param id
	 *            Which type of generation to be done
	 */
	public void generateMap(int seed, MapID map) {
		// rand.setSeed(seed);
		id = map;

		if (id == MapID.CUSTOM) {
			int[] customImage = mapImg.loadImage("/res/island.jpg", 1024, 512);
			for (int i = 0; i < 1024 * 512 && i < customImage.length; i++) {
				int x = (i % 1024);
				int y = (i / 1024);
				mountain[x][y] = (customImage[i] & 255) / 555.0f + .49f;
			}
		} else {
			if (id == MapID.SEA) {
				mountain = perlinNoise(1, 0.5f);
			} else if (id == MapID.ISLANDS || id == MapID.MOUNTAIN) {
				for (int i = 0; i < 7; i++) {
					points[i] = new Point(i % 7 * 170, rand.nextInt(256) + 128);
				}
				points[0].setY(rand.nextInt(512));
				points[0].setX(64);
				points[6].setY(rand.nextInt(512));
				points[6].setX(960);
				points[3].setY((points[0].getY() + points[6].getY()) / 2);
				for (int i = 0; i < 1025 * 513; i++) {
					int x = i % 1025;
					int y = i / 1025;
					int smallestDistance = 200;
					Point point = new Point(x, y);
					for (int i2 = 0; i2 < 7; i2++) {
						int tempDist = (int) point.getDistSquared(points[i2]);
						if (id == MapID.ISLANDS) tempDist *= (-1 / 10.0f) * (i2) * (i2 - 6) + 1f;
						if (tempDist < smallestDistance) {
							smallestDistance = tempDist;
						}
					}
					islandMask[i] = 1.6f * ((1 - ((smallestDistance) / 255.0f) + 0.1f) / 2.2f);
				}
				for (int i = 0; i < 1024 * 512; i++) {
					int x = i % 1025;
					int y = i / 1025;
					mountain[x][y] = islandMask[i];
				}
			} else if (id == MapID.RIVER) {
				// n/sqrt(m^2+1)
				double a = rand.nextFloat() * Math.PI;
				Point b = new Point(512, Math.tan(a) * 512).addPoint(new Point(512, 256));
				Point c = new Point(-512, Math.tan(a) * -512).addPoint(new Point(512, 256));
				Point e = new Point(256 / Math.tan(a), 256).addPoint(new Point(512, 256));
				Point f = new Point(-256 / Math.tan(a), -256).addPoint(new Point(512, 256));
				if (a > Math.PI / 2) {
					if (b.getDist(new Point(512, 256)) < f.getDist(new Point(512, 256))) {
						points[0] = b;
					} else {
						points[0] = f;
					}
					if (c.getDist(new Point(512, 256)) < e.getDist(new Point(512, 256))) {
						points[1024] = c;
					} else {
						points[1024] = e;
					}
				} else {
					if (b.getDist(new Point(512, 256)) < e.getDist(new Point(512, 256))) {
						points[0] = b;
					} else {
						points[0] = e;
					}
					if (f.getDist(new Point(512, 256)) < c.getDist(new Point(512, 256))) {
						points[1024] = f;
					} else {
						points[1024] = c;
					}
				}
				for (int n = 9; n >= 0; n--) {
					int p = 1 << n;
					for (int i = p; i < 1024; i += p * 2) {
						points[i] = new Point(points[i - p].addPoint(points[i + p])).multScalar(0.5f);
						double slope = -1 * (points[i - p].getX() - points[i + p].getX()) / (points[i - p].getY() - points[i + p].getY());
						double dist = points[i - p].getDistSquared(points[i + p]);
						double displacement = ((rand.nextDouble() * 1) - 1 / 2.0f) * dist / 2.0f;
						points[i].setX(points[i].getX() + (displacement / Math.sqrt(slope * slope + 1)));
						points[i].setY(points[i].getY() + (slope * displacement / Math.sqrt(slope * slope + 1)));
					}
				}
				for (int i = 0; i < 1025 * 513; i++) {
					int x = i % 1025;
					int y = i / 1025;
					int smallestDistance = 70;
					Point point = new Point(x, y);
					for (int i2 = 0; i2 < 1024; i2++) {
						int tempDist = (int) point.getDistSquared(points[i2]);
						if (tempDist < smallestDistance) {
							smallestDistance = tempDist;
						}
					}
					islandMask[i] = (smallestDistance - 96) / 250.0f + 0.75f;
				}
				for (int i = 0; i < 1024 * 512; i++) {
					int x = i % 1025;
					int y = i / 1025;
					mountain[x][y] = islandMask[i];
				}
			} else {
				mountain = perlinNoise(1, 0.5f);
			}
			for (int i2 = 2; i2 < 9; i2++) {
				int denominator = 1 << (i2 + 1);
				float[][] tempMountain = perlinNoise(i2, 1f / denominator);
				for (int i = 0; i < 1025 * 513; i++) {
					int x = i % 1025;
					int y = i / 1025;
					mountain[x][y] = mountain[x][y] + tempMountain[x][y];
				}
			}
		}
		for (int i = 0; i < 1025 * 513; i++) {
			int x = i % 1025;
			int y = i / 1025;
			if (id == MapID.MOUNTAIN) {
				mountain[x][y] = generateMountain(mountain[x][y]);
			}
			if (id == MapID.PLAINS) {
				mountain[x][y] = generatePlains(mountain[x][y]);
			}
		}
		// colors the MapArray
		for (int i = 0; i < 1025 * 513; i++) {
			int x = i % 1025;
			int y = i / 1025;
			mapData[i] = getColor(getArray(x, y));
		}
	}

	float[][] perlinNoise(int frequency, float amplitude) {
		if (id == MapID.PLAINS) {
			amplitude *= 2;
		}
		float[][] noise = new float[1025][513];
		if (frequency > 0 && frequency < 9) {
			int wavelength = 1 << (-frequency + 9);
			int width = (1025 / wavelength) + 1;
			int height = (513 / wavelength) + 1;
			for (int i = 0; i < width * height; i++) {
				int x = (i % width) * wavelength;
				int y = (i / width) * wavelength;
				if (frequency == 1) {
					if (id == MapID.SEA) {
						noise[x][y] = generateSea(amplitude, x);
					} else {
						noise[x][y] = rand.nextFloat();
					}
				} else {
					noise[x][y] = ((2 * rand.nextFloat() - 1f) * amplitude);
					if (id == MapID.RIVER && islandMask[x + y * 1025] < 0.5) {
						noise[x][y] = ((2 * rand.nextFloat() - 1f) * amplitude / 2.0f);
					}
				}
			}
			for (int i = 0; i < 1025 * 513; i++) {
				int x = i % 1025;
				int y = i / 1025;
				int x1 = (int) (x / wavelength) * wavelength;
				int y1 = (int) (y / wavelength) * wavelength;
				if (x != x1 || y != y1) {
					noise[x][y] = bicosineInterpolation(x1, y1, wavelength, noise, x, y);
				}
			}
		}
		return noise;
	}

	float bicosineInterpolation(int x1, int y1, int p, float[][] noise, int x2, int y2) {
		// (x1,y1)- coords of top left corner of box
		// p- length/heigth pf box
		// (x3,y3)- coords of point
		if (x1 + p > 1025 || y1 + p > 513) {
			return 1f;
		}
		float topInterpolation = cosineInterpolation(x1, noise[(int) x1][(int) y1], x1 + p, noise[(int) (x1 + p)][(int) y1], x2);
		float bottomInterpolation = cosineInterpolation(x1, noise[(int) x1][(int) (y1 + p)], x1 + p, noise[(int) (x1 + p)][(int) (y1 + p)], x2);
		return cosineInterpolation(y1, topInterpolation, y1 + p, bottomInterpolation, y2);
	}

	float cosineInterpolation(int x1, float y1, int x2, float y2, int m) {
		double xDiff = (x2 - x1);
		double mu2 = (1 - Math.cos((m / xDiff - x1 / xDiff) * Math.PI)) / 2;
		double y3 = (y1 * (1 - mu2) + y2 * mu2);
		return (float) y3;
	}

	float generateMountain(float land) {
		float newLand = (land) * (land + 0.5f) + 0.5f;
		return newLand;
	}

	float generatePlains(float land) {
		return land / 3.0f + 0.55f;
	}

	float generateIslands(float land) {
		return land;
	}

	float generateRiver(int x, int y, float amplitude) {
		float land = amplitude * ((rand.nextFloat() - 0.5f) + amplitude * 50 * Math.abs(x + 50 * (rand.nextFloat() - 0.5f) - (y + 256 + 100 * (rand.nextFloat() - 0.5f))) / (51.0f / amplitude) + amplitude);
		if (land > 0.3) {
			land = 0.3f;
		}
		return land;
	}

	float generateSea(float amplitude, int x) {
		return (Math.abs(x - 512) / (650.0f + rand.nextFloat() * 700.0f)) + 0.1f;
	}

	float transform(float r) {
		double verticalStretch = 0.536713074275;
		double newHeight;
		if (r > 1) {
			newHeight = 2 * (r - 1) * (r - 1) + 1;
		} else {
			newHeight = verticalStretch * Math.tan(1.5 * r - .75) + 0.5;
		}
		return (float) newHeight;
	}

	float bilinearInterpolation(int x1, int y1, int p, float[][] noise, int x2, int y2) {
		// (x1,y1)- coords of top left corner of box
		// p- length/heigth pf box
		// (x3,y3)- coords of point
		if (x1 + p > 1025 || y1 + p > 513) {
			return 1.5f;
		}
		float topInterpolation = linearInterpolation(x1, noise[(int) x1][(int) y1], x1 + p, noise[(int) (x1 + p)][(int) y1], x2);
		float bottomInterpolation = linearInterpolation(x1, noise[(int) x1][(int) (y1 + p)], x1 + p, noise[(int) (x1 + p)][(int) (y1 + p)], x2);
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
		if (x >= 0 && x < 1025 && y >= 0 && y < 513) return mountain[x][y];
		return -1;
	}

	/**
	 * @param p
	 *            Point on the map
	 * @return The float value of the given position on the terrain
	 */
	public static float getArray(Point p) {
		if (p.getX() >= 0 && p.getX() < 1024 && p.getY() >= 0 && p.getY() < 512) return mountain[(int) p.getX()][(int) p.getY()];
		return -1;
	}

	public static boolean withinBaseline(Point p, float baseline) {
		if (getArray(p) == -1) return false;
		return getArray(p) > baseline && getArray(p) < baseline + 0.5f;
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
			blue = (int) (460 * value + 16);
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