package main;

import java.util.Random;

import javax.swing.JFrame;

import objects.City;
import objects.Infantry;
import objects.Nation;
import objects.Port;
import objects.Ship;
import output.Render;
import terrain.Map;
import utility.Point;

public class Main {
	/*
	 * ToDo: ATS class, AI class, nations, grayscale object (takes in image,
	 * generates 8bit grayscale to be used)
	 */
	public static boolean running = true;
	public static final int width = 1024;
	public static final int height = 512;
	JFrame frame = new JFrame();
	Random rand = new Random();
	Map map = new Map();
	public static World world = new World();
	Render render = new Render(1024, 512, world);
	public static short fps;
	public static int ticks = 0;
	public static float version = 0.2f;

	// Main: contains the game loop for the game. Calls Render class
	public static void main(String arg[]) {
		Main m = new Main();
		m.window();
		m.init();

		double dt = 16.66;
		double currentTime = System.currentTimeMillis();
		double accumulator = 0.0;
		double t = 0;
		short frames = 0;

		double currentFrameTime = System.currentTimeMillis();

		while (running) {
			double newTime = System.currentTimeMillis();
			double frameTime = newTime - currentTime;
			currentTime = newTime;

			accumulator += frameTime;

			while (accumulator >= dt) {
				Main.world.tick(t);
				accumulator -= dt;
				t += dt;
				ticks++;
			}
			m.render.render();
			frames++;
			if (System.currentTimeMillis() - currentFrameTime > 1000) {
				currentFrameTime = System.currentTimeMillis();
				fps = frames;
				frames = 0;
			}
		}
	}

	void init() {
		Nation sweden = new Nation(47 << 16 | 100 << 8 | 250, "Sweden");
		Nation russia = new Nation(250 << 16 | 100 << 8 | 49, "Russia");
		world.nationArray.add(russia);
		world.nationArray.add(sweden);
		sweden.setEnemyNation(russia);
		russia.setEnemyNation(sweden);
		map.generateMap((int) System.currentTimeMillis() & 255);

		do {
			sweden.purgeAll();
			russia.purgeAll();
			for (int i = 0; i < 84; i++) {
				int x = (int) (i / 6) * 64 + 96;
				int y = (int) (i % 6) * 64 + 96;
				if (Map.getArray(x, y) > 0.5f) {
					russia.addUnit(new City(new Point(x, y), russia, Main.ticks));
					russia.setCaptial(1);
					break;
				}
			}
			for (int i = 0; i < 84; i++) {
				int x = ((int) 6 - (i / 6)) * 64 + 544;
				int y = (int) (6 - (i % 6)) * 64 + 32;
				if (Map.getArray(x, y) > 0.5f) {
					sweden.addUnit(new City(new Point(x, y), sweden, Main.ticks));
					sweden.setCaptial(1);
					break;
				}
			}
		} while (world.nationArray.get(0).unitSize() + world.nationArray.get(1).unitSize() < 2);
		/*
		 * for (int i = 0; i < 84; i++) { int x = ((int) 6 - (i / 6)) * 64 + 544; int y
		 * = (int) (6 - (i % 6)) * 64 + 32; if (Map.getArray(x, y) < 0.5f &&
		 * (Map.getArray(x + 16, y) > 0.5f || Map.getArray(x, y + 16) > 0.5f ||
		 * Map.getArray(x - 16, y) > 0.5f || Map.getArray(x, y - 16) > 0.5f ||
		 * Map.getArray(x + 16, y + 16) > 0.5f || Map.getArray(x + 16, y - 16) > 0.5f ||
		 * Map.getArray(x - 16, y + 16) > 0.5f || Map.getArray(x - 16, y - 16) > 0.5f))
		 * { sweden.addUnit(new Port(new Point(x, y), sweden)); } }
		 */
	}

	void window() {
		frame.setSize(width, height + 30);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setTitle("Erovra v" + Main.version);
		frame.setLocationRelativeTo(null);
		frame.add(render);
	}

}