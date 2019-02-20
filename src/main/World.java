package main;

import java.util.ArrayList;

import objects.Nation;
import objects.gui.DropDown;
import objects.gui.ErrorMessage;
import objects.gui.Menu;
import objects.units.Unit;
import output.Render;

/**
 * The world class is used to contain all objects within the game. This includes
 * units, menus, etc. The world calls all objects' tick and render methods
 * 
 * @author Rakhyvel
 *
 */
public class World {

	private Nation friendly;
	private Nation hostile;
	public ArrayList<Nation> nationArray = new ArrayList<Nation>();
	public ArrayList<Menu> menuArray = new ArrayList<Menu>();
	boolean pauseClicked = false;
	boolean speedClicked = false;
	boolean slowClicked = false;
	public Unit selectedUnit = null;
	public Unit highlightedUnit = null;
	private DropDown dropDown = new DropDown();
	public ErrorMessage errorMessage = new ErrorMessage();
	private boolean nullifySelected = false;
	
	public World(){
		menuArray.add(errorMessage);
	}

	/**
	 * Calls the tick method for each object in the game
	 * 
	 * @param t
	 *            The time in millis since last tick
	 */
	public void tick(double t) {
		highlightedUnit = null;
		if (Main.gameState == StateID.ONGOING) {
			for (int i2 = 0; i2 < friendly.unitSize(); i2++) {
				friendly.getUnit(i2).tick(t);
			}
			if (selectedUnit != null) {
				selectedUnit.setHit(3);
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
		if (Main.keyboard.period.isPressed()) {
			if (!speedClicked) {
				speedClicked = true;
				Main.speedUp();
			}
		} else {
			speedClicked = false;
		}
		if (Main.keyboard.comma.isPressed()) {
			if (!slowClicked) {
				slowClicked = true;
				Main.slowDown();
			}
		} else {
			slowClicked = false;
		}
		for (int i = 0; i < menuArray.size(); i++) {
			menuArray.get(i).tick();
		}
		if(nullifySelected){
			selectedUnit = null;
			nullifySelected = false;
		}
	}

	/**
	 * Calls for each object in the game to draw
	 * 
	 * @param r
	 *            Render object used to draw to the canvas
	 */
	public void render(Render r) {
		if (Main.gameState == StateID.ONGOING) {
			for (int i2 = friendly.unitSize() - 1; i2 >= 0; i2--) {
				if (friendly.getUnit(i2).getID() != UnitID.PLANE) friendly.getUnit(i2).render(r);
			}
			for (int i2 = hostile.unitSize() - 1; i2 >= 0; i2--) {
				if (hostile.getUnit(i2).getID() != UnitID.PLANE) hostile.getUnit(i2).render(r);
			}

			for (int i2 = 0; i2 < friendly.projectileSize(); i2++) {
				friendly.getProjectile(i2).render(r);
			}
			for (int i2 = 0; i2 < hostile.projectileSize(); i2++) {
				hostile.getProjectile(i2).render(r);
			}

			for (int i2 = friendly.unitSize() - 1; i2 >= 0; i2--) {
				if (friendly.getUnit(i2).getID() == UnitID.PLANE) friendly.getUnit(i2).render(r);
			}
			for (int i2 = hostile.unitSize() - 1; i2 >= 0; i2--) {
				if (hostile.getUnit(i2).getID() == UnitID.PLANE) hostile.getUnit(i2).render(r);
			}

			for (int i2 = 0; i2 < friendly.coinSize(); i2++) {
				friendly.getCoin(i2).render(r);
			}
			for (int i2 = 0; i2 < hostile.coinSize(); i2++) {
				hostile.getCoin(i2).render(r);
			}
		}
		for (int i = 0; i < menuArray.size(); i++) {
			menuArray.get(i).render(r);
		}
	}

	/**
	 * Draws the amount of coins
	 * 
	 * @param g
	 *            Graphics object
	 */
	public void drawCoins(Render r) {
		r.drawString((char) 7 + "" + String.valueOf(friendly.getCoinAmount()), 973, 10, r.font16, 250 << 16 | 250 << 8 | 250);
	}

	/**
	 * Sets the friendly nation
	 * 
	 * @param nation
	 *            The nation to be set to friendly
	 */
	public void setFriendly(Nation nation) {
		friendly = nation;
	}

	/**
	 * The hostile nation
	 * 
	 * @param nation
	 *            The nation to be set to hostile
	 */
	public void setHostile(Nation nation) {
		hostile = nation;
	}

	/**
	 * @return The DropDown object
	 */
	public DropDown getDropDown() {
		return dropDown;
	}
	
	public void nullifySelected(){
		nullifySelected = true;
	}
}
