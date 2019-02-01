package main;

import input.Keyboard;
import input.Mouse;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.JFrame;

import objects.Nation;
import objects.gui.GameMenu;
import objects.gui.MainMenu;
import objects.units.City;
import output.Render;
import terrain.Map;
import utility.Point;
import utility.Trig;

public class Main {

	// Game Loop
	public static boolean running = true;
	public static int fps;
	public static int ticks = 0;
	public static StateID gameState;
	public static MapID mapID = MapID.SEA;

	// Window
	public static final int width = 1024;
	public static final int height = 512;
	static JFrame frame = new JFrame();

	// Objects
	Random rand = new Random();
	static Map map = new Map();
	static World world = new World();
	Render render = new Render(1024, 512, world);
	public static Mouse mouse = new Mouse();
	public static Keyboard keyboard = new Keyboard();

	// GitHub
	public static String version = "Erovra 0.5.4";

	// main(String args[]: Contains the game loop, is the first method called
	// when
	// running
	public static void main(String args[]) {
		Main m = new Main();
		m.window();
		m.init();

		double dt = 50 / 30.0;
		double currentTime = System.currentTimeMillis();
		double accumulator = 0.0;
		double t = 0;
		int frames = 0;
		double frameTime;
		double currentFrameTime = System.currentTimeMillis();

		while (running) {
			frameTime = System.currentTimeMillis() - currentTime;
			currentTime = System.currentTimeMillis();

			accumulator += frameTime;

			while (accumulator >= dt) {
				Main.world.tick(t);
				accumulator -= dt;
				t += dt;
				if (gameState == StateID.ONGOING) ticks++;
			}
			m.render.render();
			frames++;
			if (System.currentTimeMillis() - currentFrameTime > 1000) {
				currentFrameTime = System.currentTimeMillis();
				fps = frames;
				frames = 0;
			}
		}
		// Pulling the plug after the game loop
		WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);

		frame.setVisible(false);
		frame.dispose();
		System.exit(0);
	}

	// init(): Creates the two nations, generates the map and find apropriate
	// locations for the nation's cities
	void init() {
		new Trig();
		world.menuArray.add(new GameMenu());
		world.menuArray.add(new MainMenu());
		Main.setState(StateID.MENU);
	}

	// window(): Sets up the window for the game
	void window() {
		frame.setSize(width + 7, height + 30);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setTitle(Main.version);
		frame.setLocationRelativeTo(null);
		frame.add(render);
	}

	public static void startNewMatch() {
		Main.setState(StateID.ONGOING);
		Nation sweden = new Nation(0 << 16 | 128 << 8 | 220, "Sweden");
		Nation russia = new Nation(220 << 16 | 50 << 8 | 0, "Russia");
		world.setHostile(russia);
		world.setFriendly(sweden);
		sweden.setEnemyNation(russia);
		russia.setEnemyNation(sweden);

		do {
			map.generateMap((int) System.currentTimeMillis() & 255, mapID);
			sweden.purgeAll();
			russia.purgeAll();
			for (int i = 0; i < 84; i++) {
				int x = (int) (i / 6) * 64 + 96;
				int y = (int) (i % 6) * 64 + 96;
				if (Map.getArray(x, y) > 0.5f) {
					sweden.addUnit(new City(new Point(x, y), sweden, Main.ticks));
					sweden.setCaptial(1);
					break;
				}
			}
			for (int i = 0; i < 84; i++) {
				int x = ((int) 6 - (i / 6)) * 64 + 544;
				int y = (int) (6 - (i % 6)) * 64 + 32;
				if (Map.getArray(x, y) > 0.5f) {
					russia.addUnit(new City(new Point(x, y), russia, Main.ticks));
					russia.setCaptial(1);
					break;
				}
			}
		}
		while (sweden.unitSize() + russia.unitSize() < 2);
	}

	public static void setState(StateID id) {
		Main.gameState = id;
	}

	public static int getFrameX() {
		return frame.getX();
	}

	public static int getFrameY() {
		return frame.getY();
	}
}