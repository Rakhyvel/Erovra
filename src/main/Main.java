package main;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import input.Keyboard;
import input.Mouse;
import objects.Nation;
import objects.gui.GameMenu;
import objects.gui.MainMenu;
import objects.units.City;
import objects.units.Infantry;
import objects.units.Unit;
import output.Render;
import terrain.Map;
import utility.Point;
import utility.Trig;

/**
 * Starts the game, contains some constants and global objects
 * 
 * @author Rakhyvel
 */
public class Main {

	// Game Loop
	private static boolean running = true;
	public static int fps;
	public static int ticks = 0;
	public static StateID gameState;
	public static MapID mapID = MapID.RANDOM;
	private static double dt = 50 / 3.0;
	public static Random rand = new Random();

	// Window
	public static final int width = 1024;
	public static final int height = 512;
	private static JFrame frame = new JFrame();

	// Objects
	private static Map map = new Map();
	public static World world = new World();
	private Render render = new Render(1024, 512, world);
	public static Mouse mouse = new Mouse();
	public static Keyboard keyboard = new Keyboard();
	public static float zoom = 1f;
	public static int difficulty = 0;
	public static String os = getOperatingSystem();

	// GitHub
	public static String version = "Erovra 1.0.19";

	/**
	 * Sets up the window, initializes the world object, and runs the game loop
	 * 
	 * @param args No idea what these do
	 */
	public static void main(String args[]) {
		Main m = new Main();
		m.window();
		m.init();

		double currentTime = System.currentTimeMillis();
		double accumulator = 0.0;
		double t = 0;
		int frames = 0;
		double frameTime;
		double currentFrameTime = System.currentTimeMillis();

		while (isRunning()) {
			frameTime = System.currentTimeMillis() - currentTime;
			currentTime = System.currentTimeMillis();

			accumulator += frameTime;

			while (accumulator >= dt) {
				Main.world.tick(t);
				accumulator -= dt;
				t += dt;
				if (gameState == StateID.ONGOING)
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
		// Pulling the plug after the game loop
		WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);

		frame.setVisible(false);
		frame.dispose();
		System.exit(0);
	}

	/**
	 * Initializes objects before the game loop starts
	 */
	void init() {
		new Trig();
		world.menuArray.add(new GameMenu());
		world.menuArray.add(new MainMenu());
		world.menuArray.add(world.getDropDown());
		Main.setState(StateID.MENU);
	}

	/**
	 * Sets up the game's window 30 for windows 41 for mac
	 */
	void window() {
		frame.setSize(width + 7, height + 30);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setTitle(Main.version);
		frame.setLocationRelativeTo(null);
		frame.add(render);
		frame.setIconImage(new ImageIcon(getClass().getResource("/res/icon.png")).getImage());
	}

	/**
	 * Sets the state of the game to ongoing, creates two teams, and generates a new
	 * map
	 */
	public static void startNewMatch() {
		ticks = 0;
		world.selectedUnits.clear();
		Main.setState(StateID.ONGOING);
		int hue = 208;
		Nation sweden = new Nation(255 << 24 | Render.getRGB(hue, .8, .8), "Sweden");
		Nation russia = new Nation(255 << 24 | Render.getRGB(1, .8, .8), "Russia");
//		sweden.setAIControlled(false);
//		russia.setAIControlled(false);
		world.setHostile(russia);
		world.setFriendly(sweden);
		sweden.setEnemyNation(russia);
		russia.setEnemyNation(sweden);
		MapID id = mapID;
		if (mapID == MapID.RANDOM)
			id = MapID.values()[rand.nextInt(5)];
		System.out.println(id);
		world.getDropDown().shouldClose();
		boolean clearPath;

		do {
			clearPath = true;
			// Generate a new map
			map.generateMap((int) System.currentTimeMillis() & 255, id);

			// Clear all objects in each nation
			sweden.purgeAll();
			russia.purgeAll();

			// Start at the top left corner and try to find a suitable place for
			// a city
			for (int i = 0; i < 6; i++) {
				int x = i / 6 * 64 + 96;
				int y = i % 6 * 64 + 96;
				int clearAdjacentSides = 0;
				if (Unit.clearPath(new Point(x, y), new Point(x, y + 64), 0.5f)) {
					clearAdjacentSides++;
				}
				if (Unit.clearPath(new Point(x, y), new Point(x, y - 64), 0.5f)) {
					clearAdjacentSides++;
				}
				if (Unit.clearPath(new Point(x, y), new Point(x + 64, y), 0.5f)) {
					clearAdjacentSides++;
				}
				if (Unit.clearPath(new Point(x, y), new Point(x - 64, y + 64), 0.5f)) {
					clearAdjacentSides++;
				}
				if (Map.getArray(x, y) > 0.5f && Map.getArray(x, y) < 1 && clearAdjacentSides > 1
						|| id == MapID.CUSTOM) {
					russia.addUnit(new City(new Point(x, y), russia, Main.ticks));
					russia.setCaptial(0);
					break;
				}
			}

			// Start at the bottom right corner and try to find a suitable place
			// for a city
			for (int i = 0; i < 6; i++) {
				int x = (6 - (i / 6)) * 64 + 544;
				int y = (6 - (i % 6)) * 64 + 32;
				int clearAdjacentSides = 0;
				if (Unit.clearPath(new Point(x, y), new Point(x, y + 64), 0.5f)) {
					clearAdjacentSides++;
				}
				if (Unit.clearPath(new Point(x, y), new Point(x, y - 64), 0.5f)) {
					clearAdjacentSides++;
				}
				if (Unit.clearPath(new Point(x, y), new Point(x + 64, y), 0.5f)) {
					clearAdjacentSides++;
				}
				if (Unit.clearPath(new Point(x, y), new Point(x - 64, y + 64), 0.5f)) {
					clearAdjacentSides++;
				}
				if (Map.getArray(x, y) > 0.5f && Map.getArray(x, y) < 1 && clearAdjacentSides > 1
						|| id == MapID.CUSTOM) {
					sweden.addUnit(new City(new Point(x, y), sweden, Main.ticks));
					sweden.setCaptial(0);
					break;
				}
			}

			if (id == MapID.MOUNTAIN && sweden.unitSize() == 1 && russia.unitSize() == 1) {
				System.out.println("pathing");
				// (#46) Pathfinding moutain maps
				sweden.addUnit(new Infantry(new Point(sweden.getUnit(0).getPosition()), sweden));
				for (int i = 0; i < 6 && clearPath; i++) {
					Point pathfind = sweden.getUnit(1).pathfind(russia.capital.getPosition(), 0.5f);
					if (pathfind == null) {
						clearPath = false;
					} else {
						System.out.println(pathfind.toString());
						sweden.getUnit(1).setPosition(pathfind);
					}
					if (i == 6) {
						clearPath = false;
					}
				}
				sweden.removeUnit(sweden.getUnit(1));
			}
		} while (sweden.unitSize() + russia.unitSize() < 2 || !clearPath);
		sweden.addUnit(new Infantry(new Point(sweden.getUnit(0).getPosition()), sweden));
		russia.addUnit(new Infantry(new Point(russia.getUnit(0).getPosition()), russia));
		sweden.unupgradedCities = 0;
		russia.unupgradedCities = 0;
		Main.world.indicator.showMessage("Find the enemy capital and destroy it!");
	}

	/**
	 * @param id The game state to be changed.
	 */
	public static void setState(StateID id) {
		Main.gameState = id;
	}

	public static int getFrameX() {
		return frame.getX();
	}

	public static int getFrameY() {
		return frame.getY();
	}

	public static void zoomIn() {
		if (zoom > 0.25f) {
			zoom /= 1.01;
		} else {
			zoom = 0.25f;
		}
	}

	public static void zoomOut() {
		if (zoom < 2) {
			zoom *= 1.01;
		} else {
			zoom = 2;
		}
	}

	public static boolean isRunning() {
		return running;
	}

	public static void endGame() {
		running = false;
	}

	public static void speedUp() {
		dt /= 2;
		System.out.println((50 / 3.0) / dt);
	}

	public static void slowDown() {
		dt *= 2;
		System.out.println((50 / 3.0) / dt);
	}

	public static String getOperatingSystem() {
		String os = System.getProperty("os.name");
		// System.out.println("Using System Property: " + os);
		return os;
	}
}