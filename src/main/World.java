package main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import objects.Nation;
import objects.gui.DropDown;
import objects.gui.Menu;
import output.Render;

public class World {

	/*
	 * The world class is used to contain all objects within the game. This includes
	 * units, menus, etc. The world calls all objects' tick and render methods
	 */

	Nation friendly;
	Nation hostile;
	public ArrayList<Nation> nationArray = new ArrayList<Nation>();
	public ArrayList<Menu> menuArray = new ArrayList<Menu>();
	boolean pauseClicked = false;
	DropDown dropDown = new DropDown();

	// tick(double t): Calls the tick method for each object in the game, takes
	// t as the time in millis since last tick
	public void tick(double t) {
		if (Main.gameState == StateID.ONGOING) {
			for (int i2 = 0; i2 < friendly.unitSize(); i2++) {
				friendly.getUnit(i2).tick(t);
			}
			for (int i2 = 0; i2 < friendly.projectileSize(); i2++) {
				friendly.getProjectile(i2).tick(t);
			}
			for (int i2 = 0; i2 < friendly.coinSize(); i2++) {
				friendly.getCoin(i2).tick(t);
			}

			for (int i2 = 0; i2 < hostile.unitSize(); i2++) {
				hostile.getUnit(i2).tick(t);
			}
			for (int i2 = 0; i2 < hostile.projectileSize(); i2++) {
				hostile.getProjectile(i2).tick(t);
			}
			for (int i2 = 0; i2 < hostile.coinSize(); i2++) {
				hostile.getCoin(i2).tick(t);
			}
		}
		for (int i = 0; i < menuArray.size(); i++) {
			menuArray.get(i).tick();
		}
		if (Main.keyboard.esc.isPressed()) {
			if (!pauseClicked) {
				pauseClicked = true;
				if (Main.gameState == StateID.PAUSED) {
					Main.setState(StateID.ONGOING);
				} else if (Main.gameState == StateID.ONGOING) {
					Main.setState(StateID.PAUSED);
				}
			}
		} else {
			pauseClicked = false;
		}
		if (Main.keyboard.minus.isPressed()) {
			Main.zoomOut();
		}
		if (Main.keyboard.plus.isPressed()) {
			Main.zoomIn();
		}
	}

	// render(Render r): Calls for each object in the game to draw, takes in the
	// custom Render object, r.
	public void render(Render r) {
		if (Main.gameState == StateID.ONGOING) {
			for (int i2 = friendly.unitSize() - 1; i2 >= 0; i2--) {
				if (friendly.getUnit(i2).getID() != UnitID.PLANE)
					friendly.getUnit(i2).render(r);
			}
			for (int i2 = hostile.unitSize() - 1; i2 >= 0; i2--) {
				if (hostile.getUnit(i2).getID() != UnitID.PLANE)
					hostile.getUnit(i2).render(r);
			}

			for (int i2 = 0; i2 < friendly.projectileSize(); i2++) {
				friendly.getProjectile(i2).render(r);
			}
			for (int i2 = 0; i2 < hostile.projectileSize(); i2++) {
				hostile.getProjectile(i2).render(r);
			}

			for (int i2 = friendly.unitSize() - 1; i2 >= 0; i2--) {
				if (friendly.getUnit(i2).getID() == UnitID.PLANE)
					friendly.getUnit(i2).render(r);
			}
			for (int i2 = hostile.unitSize() - 1; i2 >= 0; i2--) {
				if (hostile.getUnit(i2).getID() == UnitID.PLANE)
					hostile.getUnit(i2).render(r);
			}

			for (int i2 = 0; i2 < friendly.coinSize(); i2++) {
				friendly.getCoin(i2).render(r);
			}
			for (int i2 = 0; i2 < hostile.coinSize(); i2++) {
				hostile.getCoin(i2).render(r);
			}
			r.drawImageScreen(960, 12, 16, r.coin, 255 << 16 | 255 << 8);
		}
		for (int i = 0; i < menuArray.size(); i++) {
			menuArray.get(i).render(r);
		}
	}

	// drawCoins(Graphics g): Draws the amount of coins
	public void drawCoins(Graphics g) {
		g.setColor(new Color(0, 0, 0));
		g.drawString(String.valueOf(friendly.getCoinAmount()), 973, 17);
		g.setColor(new Color(255, 255, 255));
		g.drawString(String.valueOf(friendly.getCoinAmount()), 972, 16);
	}

	public void setFriendly(Nation nation) {
		friendly = nation;
	}

	public void setHostile(Nation nation) {
		hostile = nation;
	}

	public DropDown getDropDown() {
		return dropDown;
	}
}
