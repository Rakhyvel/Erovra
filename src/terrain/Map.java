package terrain;

import java.util.Random;

import main.Image;
import main.Main;
import utility.Point;

public class Map {

	Random rand = new Random();
	public static float[][] mountain = new float[Main.width + 1][Main.height + 1];
	public static int[] mapData = new int[513 * 1025];
	Image image = new Image();

	// generateMap(int seed): Uses simplex noise and linear interpolation to
	// render the terrain for the game.
	public void generateMap(int seed) {
		rand.setSeed(seed);
		for (int i = 0; i < 45; i++) {
			int x = (i % 9);
			int y = (i / 9);
			// River
			// mountain[x * 128][y * 128] = Math.max(((1 - x) + (1 - y) + 10), x
			// + y) * 0.07f;

			// Plains
			// mountain[x * 128][y * 128] = rand.nextFloat()/3 + 0.6f;

			// Island

			// Open sea
			if(x == 0 || x ==8){
				mountain[x * 128][y * 128] = .9f;
			} else if (x == 1 || x == 7){
				mountain[x * 128][y*128] = rand.nextFloat() * .5f + .25f;
			} else {
				if (x != 4) {
					mountain[x * 128][y * 128] = (x - 4) * (x - 4) * 0.05f;
				} else {
					mountain[x * 128][y * 128] = 0;
				}
			}

			// Mountain pass
			// float r = rand.nextFloat();
			// mountain[x * 128][y * 128] = (r);

			// Upload
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
				int x = (int) (i2 % s * 2) * p;
				int y = (int) (i2 / s * 2) * p;

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
				int x = (int) (i2 % s * 2) * p;
				int y = (int) (i2 / s * 2) * p;
				// m: a random
				float m = 0;

				if (x + 2 * p < Main.width + 1 && y + 2 * p < Main.height + 1) {
					m = (rand.nextFloat() - .5f) / (4 << i);
					mountain[x + p][y + 2 * p] = ((mountain[x][y + 2 * p] + mountain[x + 2 * p][y + 2 * p]) * .5f) + 2 * m;

					m = (rand.nextFloat() - .5f) / (4 << i);
					mountain[x + 2 * p][y + p] = ((mountain[x + 2 * p][y] + mountain[x + 2 * p][y + 2 * p]) * .5f) + 2 * m;

					m = (rand.nextFloat() - .5f) / (4 << i);
					mountain[x + p][y + p] = ((mountain[x + p][y + 2 * p] + mountain[x + 2 * p][y + p] + mountain[x][y + p] + mountain[x + p][y]) * .25f) + 4 * m;
				}
			}
		}
		// colors the MapArray
		for (int i = 0; i < 1025 * 513; i++) {
			int x = (int) (i % 1025);
			int y = (int) (i / 1025);
			mapData[i] = getColor(getArray(x, y));
		}
	}

	// getArray(...): returns the float value at a given coordinate
	public static float getArray(int x, int y) {
		return mountain[x][y];
	}

	// getArray(...): returns the float value at a given coordinate
	public static float getArray(Point p) {
		if (p.getX() > 0 && p.getX() < 1024 && p.getY() > 0 && p.getY() < 512) return mountain[(int) p.getX()][(int) p.getY()];
		return -1;
	}

	// getColor(flaot value): returns the color of the terrain at a certain
	// value
	int getColor(float value) {
		int blue = 0;
		int green = 0;
		int red = 0;

		if (value < .5f) {
			blue = (int) (400 * (value - .5f) + 250);
			green = (int) (800 * (value - .5f) + 200);
			red = (int) (1600 * (value - .5f) + 150);
		} else if (value < 1) {
			blue = (int) (-400 * (value - .5f) + 130);
			green = (int) (-200 * (value - .5f) + 200);
			red = (int) (-500 * (value - .5f) + 200);
		} else {
			blue = (int) (value * value * value * 85);
			green = (int) (value * value * value * 85);
			red = (int) (value * value * value * 85);
		}
		if (blue < 0) blue = 0;
		if (green < 0) green = 0;
		if (red < 0) red = 0;
		if (blue > 255) blue = 255;
		if (green > 255) green = 255;
		if (red > 255) red = 255;
		return 255 << 24 | red << 16 | green << 8 | blue;
	}
}