package main;

import java.util.ArrayList;

import objects.Nation;
import objects.gui.DropDown;
import objects.gui.ErrorMessage;
import objects.gui.Menu;
import objects.units.Unit;
import output.Render;
import utility.Point;

/**
 * The world class is used to contain all objects within the game. This includes
 * units, menus, etc. The world calls all objects' tick and render methods
 * 
 * @author Rakhyvel
 *
 */
public class World {

	public Nation friendly;
	public Nation hostile;
	public ArrayList<Nation> nationArray = new ArrayList<Nation>();
	public ArrayList<Menu> menuArray = new ArrayList<Menu>();
	boolean pauseClicked = false;
	boolean speedClicked = false;
	boolean slowClicked = false;
	boolean pathClicked = false;
	public ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
	public Unit highlightedUnit = null;
	private DropDown dropDown = new DropDown();
	public ErrorMessage errorMessage = new ErrorMessage();
	private boolean nullifySelected = false;
	Point mouseStartPoint = new Point(-1, -1);
	public String defeatedName = "";
	boolean showPaths = false;
	public SelectionID selectionMethod = SelectionID.SINGLE;
	Point startMouseDown = null;
	public Point midPoint = new Point(0,0);

	public World() {
		menuArray.add(errorMessage);
	}

	/**
	 * Calls the tick method for each object in the game
	 * 
	 * @param t The time in millis since last tick
	 */
	public void tick(double t) {
		highlightedUnit = null;
		if (Main.gameState == StateID.ONGOING) {
			if (Main.mouse.getX() > 930 && Main.mouse.getX() < 960 && Main.mouse.getY() < 30) {
				if (Main.mouse.getMouseLeftDown()) {
					pathClicked = true;
				} else if (pathClicked) {
					setShowPaths(!getShowPaths());
					pathClicked = false;
				}
			}
			if (Main.mouse.getX() > 902 && Main.mouse.getX() < 928 && Main.mouse.getY() < 30) {
				if (Main.mouse.getMouseLeftDown()) {
					pathClicked = true;
				} else if (pathClicked) {
					selectionMethod = SelectionID.getID((selectionMethod.ordinal() + 1) % 4);
					pathClicked = false;
				}
			}

			if (Main.keyboard.shift.isPressed() && Main.keyboard.ctrl.isPressed()) {
				if (selectionMethod != SelectionID.TASK) {
					selectionMethod = SelectionID.TASK;
					selectedUnits.clear();
				}
			} else if (Main.keyboard.ctrl.isPressed()) {
				if (selectionMethod != SelectionID.MULTI) {
					selectionMethod = SelectionID.MULTI;
					selectedUnits.clear();
				}
			} else if (Main.keyboard.shift.isPressed()) {
				if(selectionMethod != SelectionID.BOX) {
					selectionMethod = SelectionID.BOX;
					selectedUnits.clear();
				}
			} else {
				selectionMethod = SelectionID.SINGLE;
			}
			
			if (selectionMethod == SelectionID.BOX) {
				if (Main.mouse.getMouseLeftDown()) {
					if (mouseStartPoint == null) {
						mouseStartPoint = new Point(Main.mouse.getX(), Main.mouse.getY());
					} else {
					}
				} else if (mouseStartPoint != null) {
					for (int i = 0; i < friendly.unitSize(); i++) {
						if(!friendly.getUnit(i).getID().isBuilding()) {
							if (friendly.getUnit(i).getPosition().getX() > Math.min(Main.mouse.getX(),
									mouseStartPoint.getX())) {
								if (friendly.getUnit(i).getPosition().getX() < Math.max(Main.mouse.getX(),
										mouseStartPoint.getX())) {
									if (friendly.getUnit(i).getPosition().getY() > Math.min(Main.mouse.getY(),
											mouseStartPoint.getY())) {
										if (friendly.getUnit(i).getPosition().getY() < Math.max(Main.mouse.getY(),
												mouseStartPoint.getY())) {
											addSelection(friendly.getUnit(i));
											friendly.getUnit(i).selected = true;
										}
									}
								}
							}
						}
					}
					mouseStartPoint = null;
				}
			}
			
			if(Main.keyboard.p.isPressed()) {
				for(int i = 0; i < selectedUnits.size();i++) {
					selectedUnits.get(i).patrolling = true;
				}
			}

			for (int i2 = 0; i2 < friendly.projectileSize(); i2++) {
				friendly.getProjectile(i2).tick(t);
			}
			for (int i2 = 0; i2 < friendly.unitSize(); i2++) {
				friendly.getUnit(i2).tick(t);
			}
			for (int i = 0; i < selectedUnits.size(); i++) {
				selectedUnits.get(i).setHit(3);
			}
			for (int i2 = 0; i2 < friendly.coinSize(); i2++) {
				friendly.getCoin(i2).tick(t);
			}

			for (int i2 = 0; i2 < hostile.projectileSize(); i2++) {
				hostile.getProjectile(i2).tick(t);
			}
			for (int i2 = 0; i2 < hostile.unitSize(); i2++) {
				hostile.getUnit(i2).tick(t);
			}
			for (int i2 = 0; i2 < hostile.coinSize(); i2++) {
				hostile.getCoin(i2).tick(t);
			}

			midPoint.setX(0);
			midPoint.setY(0);
			for(int i = 0; i < selectedUnits.size(); i++) {
				midPoint.setX(midPoint.getX()+(selectedUnits.get(i).getPosition().getX())/selectedUnits.size());
				midPoint.setY(midPoint.getY()+(selectedUnits.get(i).getPosition().getY())/selectedUnits.size());
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
		if (nullifySelected) {
			selectedUnits.clear();
			nullifySelected = false;
		}
	}

	/**
	 * Calls for each object in the game to draw
	 * 
	 * @param r Render object used to draw to the canvas
	 */
	public void render(Render r) {
		if (Main.gameState == StateID.ONGOING) {
			if (!defeatedName.equals("")) {
				if (defeatedName.contains("Sweden")) {
					Main.setState(StateID.DEFEAT);
				} else {
					Main.setState(StateID.VICTORY);
				}
			}
			for (int i2 = 0; i2 < friendly.projectileSize(); i2++) {
				if (friendly.getProjectile(i2).getID() == UnitID.TORPEDO) {
					friendly.getProjectile(i2).render(r);
				}
			}
			for (int i2 = 0; i2 < hostile.projectileSize(); i2++) {
				if (hostile.getProjectile(i2).getID() == UnitID.TORPEDO) {
					hostile.getProjectile(i2).render(r);
				}
			}
			
			for (int i2 = hostile.unitSize() - 1; i2 >= 0; i2--) {
				if (hostile.getUnit(i2).getID().isBuilding())
					hostile.getUnit(i2).render(r);
			}
			if(selectionMethod == SelectionID.BOX && Main.mouse.getMouseLeftDown() && mouseStartPoint != null) {
				r.drawRect((int) Math.min(Main.mouse.getX(), mouseStartPoint.getX()),
						(int) Math.min(Main.mouse.getY(), mouseStartPoint.getY()),
						(int) Math.abs(mouseStartPoint.getX() - Main.mouse.getX()),
						(int) Math.abs(mouseStartPoint.getY() - Main.mouse.getY()), 180 << 24 ^ friendly.color);
			}
			for (int i2 = friendly.unitSize() - 1; i2 >= 0; i2--) {
				if (friendly.getUnit(i2).getID().isBuilding())
					friendly.getUnit(i2).render(r);
			}

			for (int i2 = friendly.unitSize() - 1; i2 >= 0; i2--) {
				if (!friendly.getUnit(i2).getID().isBuilding() && friendly.getUnit(i2).getID() != UnitID.PLANE)
					friendly.getUnit(i2).render(r);
			}
			for (int i2 = hostile.unitSize() - 1; i2 >= 0; i2--) {
				if (!hostile.getUnit(i2).getID().isBuilding() && hostile.getUnit(i2).getID() != UnitID.PLANE)
					hostile.getUnit(i2).render(r);
			}

			for (int i2 = 0; i2 < friendly.projectileSize(); i2++) {
				if (friendly.getProjectile(i2).getID() != UnitID.TORPEDO) {
					friendly.getProjectile(i2).render(r);
				}
			}
			for (int i2 = 0; i2 < hostile.projectileSize(); i2++) {
				if (hostile.getProjectile(i2).getID() != UnitID.TORPEDO) {
					hostile.getProjectile(i2).render(r);
				}
			}

			for (int i2 = friendly.unitSize() - 1; i2 >= 0; i2--) {
				if (friendly.getUnit(i2).getID() == UnitID.PLANE)
					friendly.getUnit(i2).render(r);
			}
			for (int i2 = hostile.unitSize() - 1; i2 >= 0; i2--) {
				if (hostile.getUnit(i2).getID() == UnitID.PLANE)
					hostile.getUnit(i2).render(r);
			}

			r.drawRect(1024 - 64, 0, 64, 20, 128 << 24);
			r.drawRectBorders(1024 - 94, 0, 30, 30, 128 << 24, 15);
			if (getShowPaths()) {
				r.drawImage(1024 - 79, 15, 26, Render.getScreenBlend(friendly.color, r.showPath), 1, 0);
			} else {
				r.drawImage(1024 - 79, 15, 26, r.showPath, 1, 0);
			}
			r.drawRectBorders(901, 0, 30, 30, 128 << 24, 15);
			if (selectionMethod == SelectionID.SINGLE) {
				r.drawImage(1024 - 79 - 28, 15, 26, r.selectSingle, 1, 0);
			} else if (selectionMethod == SelectionID.MULTI) {
				r.drawImage(1024 - 79 - 28, 15, 26, r.selectMulti, 1, 0);
			} else if (selectionMethod == SelectionID.BOX) {
				r.drawImage(1024 - 79 - 28, 15, 26, r.selectBox, 1, 0);
			} else if (selectionMethod == SelectionID.TASK) {
				r.drawImage(1024 - 79 - 28, 15, 26, r.selectTask, 1, 0);
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
	 * @param g Graphics object
	 */
	public void drawCoins(Render r) {
		r.drawString((char) 7 + "" + String.valueOf(friendly.getCoinAmount()), 993, 10, r.font16,
				255 << 24 | 250 << 16 | 250 << 8 | 250);
	}

	/**
	 * Sets the friendly nation
	 * 
	 * @param nation The nation to be set to friendly
	 */
	public void setFriendly(Nation nation) {
		friendly = nation;
		defeatedName = "";
	}

	/**
	 * The hostile nation
	 * 
	 * @param nation The nation to be set to hostile
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

	public void nullifySelected() {
		nullifySelected = true;
	}

	public void setShowPaths(boolean showPaths) {
		this.showPaths = showPaths;
	}

	public boolean getShowPaths() {
		return showPaths;
	}
	
	public void addSelection(Unit unit) {
		selectedUnits.add(unit);
	}

}
