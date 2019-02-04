package objects.gui;

import main.Main;
import main.UnitID;
import objects.units.Unit;
import output.Render;
import utility.Point;

public class DropDown extends Menu {

	Point position = new Point(512, 50);
	Unit unit = null;
	boolean shown = false;
	String name = "";

	public void tick() {
		if (shown) {
			unit.setHit(3);
			if (Main.mouse.getX() >= position.getX() && Main.mouse.getX() < position.getX() + 170 && Main.mouse.getY() > position.getY() && Main.mouse.getY() < position.getY() + 175) {
				if (Main.mouse.getY() >= position.getY() + 55 && Main.mouse.getY() < position.getY() + 85) {
					buttonsHovered = 1;
					if (Main.mouse.getMouseLeftDown()) {
						if (unit.getID() == UnitID.INFANTRY) {
							unit.nation.buyCity(unit.getPosition());
						} else if (unit.getID() == UnitID.FACTORY) {
							unit.buyUnit(UnitID.CAVALRY, UnitID.LIGHT, unit.nation.cavalryCost / 2, 10800);
						} else if (unit.getID() == UnitID.PORT) {
							unit.buyUnit(UnitID.SHIP, UnitID.LIGHT, unit.nation.shipCost / 4, 3000);
						} else if (unit.getID() == UnitID.AIRFIELD) {
							unit.buyUnit(UnitID.PLANE, UnitID.LIGHT, unit.nation.cavalryCost / 2, 5400);
						}
					}
				} else if (Main.mouse.getY() >= position.getY() + 85 && Main.mouse.getY() < position.getY() + 115) {
					buttonsHovered = 2;
					if (Main.mouse.getMouseLeftDown()) {
						if (unit.getID() == UnitID.INFANTRY) {
							unit.nation.buyFactory(unit.getPosition());
						} else if (unit.getID() == UnitID.FACTORY) {
							unit.buyUnit(UnitID.CAVALRY, UnitID.MEDIUM, unit.nation.cavalryCost, 10800);
						} else if (unit.getID() == UnitID.PORT) {
							unit.buyUnit(UnitID.SHIP, UnitID.MEDIUM, unit.nation.shipCost, 10800);
						} else if (unit.getID() == UnitID.AIRFIELD) {
							unit.buyUnit(UnitID.PLANE, UnitID.MEDIUM, unit.nation.planeCost, 10800);
						}
					}
				} else if (Main.mouse.getY() >= position.getY() + 115 && Main.mouse.getY() < position.getY() + 145) {
					buttonsHovered = 3;
					if (Main.mouse.getMouseLeftDown()) {
						if (unit.getID() == UnitID.INFANTRY) {
							unit.nation.buyPort(unit.getPosition());
						} else if (unit.getID() == UnitID.FACTORY) {
							unit.buyUnit(UnitID.CAVALRY, UnitID.HEAVY, unit.nation.cavalryCost * 2, 21600);
						} else if (unit.getID() == UnitID.PORT) {
							unit.buyUnit(UnitID.SHIP, UnitID.HEAVY, unit.nation.shipCost * 2, 10800);
						} else if (unit.getID() == UnitID.AIRFIELD) {
							unit.buyUnit(UnitID.PLANE, UnitID.HEAVY, unit.nation.planeCost / 2, 7200);
						}
					}
				} else if (Main.mouse.getY() >= position.getY() + 145 && Main.mouse.getY() < position.getY() + 175) {
					buttonsHovered = 4;
					if (Main.mouse.getMouseLeftDown()) unit.nation.buyAirfield(unit.getPosition());

				} else {
					buttonsHovered = 0;
				}
			} else {
				buttonsHovered = 0;
			}
		}
		if (Main.mouse.getMouseLeftDown()) {
			hide();
		}
	}

	public void render(Render r) {
		if (shown) {
			if (unit.getID() == UnitID.INFANTRY) {
				r.drawRect((int) position.getX(), (int) position.getY(), 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
				r.drawString(name, (int) position.getX() + 91, (int) position.getY() + 10, 16, r.font16, 0);
				drawOption("City (" + unit.nation.cityCost + ")", 1, r);
				drawOption("Factory (" + unit.nation.factoryCost + ")", 2, r);
				drawOption("Port (" + unit.nation.portCost + ")", 3, r);
				drawOption("Airfield (" + unit.nation.airfieldCost + ")", 4, r);
			} else if (unit.getID() == UnitID.FACTORY) {
				r.drawRect((int) position.getX(), (int) position.getY(), 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
				r.drawString(name, (int) position.getX() + 91, (int) position.getY() + 10, 16, r.font16, 0);
				drawOption("Light tank (" + unit.nation.cavalryCost / 2 + ")", 1, r);
				drawOption("Medium tank (" + unit.nation.cavalryCost + ")", 2, r);
				drawOption("Heavy tank (" + unit.nation.cavalryCost * 2 + ")", 3, r);
			} else if (unit.getID() == UnitID.PORT) {
				r.drawRect((int) position.getX(), (int) position.getY(), 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
				r.drawString(name, (int) position.getX() + 91, (int) position.getY() + 10, 16, r.font16, 0);
				drawOption("Landing craft (" + unit.nation.shipCost / 4 + ")", 1, r);
				drawOption("Destroyer (" + unit.nation.shipCost + ")", 2, r);
				drawOption("Cruiser (" + unit.nation.shipCost * 2 + ")", 3, r);
			} else if (unit.getID() == UnitID.AIRFIELD) {
				r.drawRect((int) position.getX(), (int) position.getY(), 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
				r.drawString(name, (int) position.getX() + 91, (int) position.getY() + 10, 16, r.font16, 0);
				drawOption("Fighter (" + unit.nation.planeCost / 2 + ")", 1, r);
				drawOption("Attacker (" + unit.nation.planeCost + ")", 2, r);
				drawOption("Bomber (" + unit.nation.planeCost / 2 + ")", 3, r);
			}
		}
	}

	void drawOption(String label, int buttonID, Render r) {
		int x = (int) position.getX();
		int y = (int) position.getY();
		if (buttonsHovered == buttonID) {
			r.drawRect(x, y + buttonID * 30, 170, 30, 255 << 16 | 255 << 8 | 255, 0.5f);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, 16, r.font16, 0);
		} else {
			r.drawRect(x, y + buttonID * 30, 170, 30, 0, 0.5f);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, 16, r.font16, 0);
		}
	}

	public void setPosition(Point p) {
		if (p.getX() > 0) {
			if (p.getX() < 875) {
				position.setX(p.getX());
			} else {
				position.setX(875);
			}
		} else {
			position.setX(0);
		}
		if (p.getY() > 0) {
			if (p.getY() < 363) {
				position.setY(p.getY());
			} else {
				position.setY(363);
			}
		} else {
			position.setY(0);
		}
	}

	public void show(Unit unit) {
		this.unit = unit;
		unit.setHit(3);
		shown = true;
		setPosition(unit.getPosition());
		name = String.valueOf(unit.getID());
	}

	private void hide() {
		unit = null;
		shown = false;
		name = "You shouldn't be seeing this";
	}
}
