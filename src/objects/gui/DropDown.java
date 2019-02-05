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
	Unit passenger1, passenger2;
	int passengers;

	public void tick() {
		if (shown) {
			unit.setHit(3);
			if (passengers == 0) {
				if (Main.mouse.getX() >= position.getX() && Main.mouse.getX() < position.getX() + 170
						&& Main.mouse.getY() > position.getY() && Main.mouse.getY() < position.getY() + 175) {
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
							} else if (unit.getID() == UnitID.SHIP) {

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
					} else if (Main.mouse.getY() >= position.getY() + 115
							&& Main.mouse.getY() < position.getY() + 145) {
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
					} else if (Main.mouse.getY() >= position.getY() + 145
							&& Main.mouse.getY() < position.getY() + 175) {
						buttonsHovered = 4;
						if (Main.mouse.getMouseLeftDown())
							unit.nation.buyAirfield(unit.getPosition());
					} else {
						if (unit.getID() != UnitID.SHIP)
							buttonsHovered = 0;
					}
				} else {
					if (unit.getID() != UnitID.SHIP)
						buttonsHovered = 0;
				}
			}
			if (Main.mouse.getMouseLeftDown()) {
				if (unit.getID() != UnitID.SHIP) {
					hide();
				} else {
					if (Main.mouse.getX() >= position.getX() && Main.mouse.getX() < position.getX() + 170
							&& Main.mouse.getY() > position.getY() && Main.mouse.getY() < position.getY() + 120) {
						passengers = buttonsHovered;
					} else {
						if(passengers == 0) {
							hide();
						}
					}
				}
			}
			if (passengers != 0 && unit != null) {
				for (int i = 0; i < unit.nation.unitSize(); i++) {
					if (unit.nation.getUnit(i).isSelected() && !unit.nation.getUnit(i).equals(unit)) {
						unit.nation.getUnit(i).setBoarded(true);
						unit.nation.getUnit(i).setSelected(false);
						if (passengers == 1) {
							unit.setPassenger1(unit.nation.getUnit(i));
							passengers = 0;
							hide();
							break;
						} else if (passengers == 2) {
							unit.setPassenger2(unit.nation.getUnit(i));
							passengers = 0;
							hide();
							break;
						}
					}
				}
			}
		}
	}

	public void render(Render r) {
		if (shown) {
			r.drawRect((int) position.getX(), (int) position.getY(), 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
			r.drawString(name, (int) position.getX() + 91, (int) position.getY() + 10, 16, r.font16, 0);
			if (unit.getID() == UnitID.INFANTRY) {
				drawOption("City (" + unit.nation.cityCost + ")", 1, r);
				drawOption("Factory (" + unit.nation.factoryCost + ")", 2, r);
				drawOption("Port (" + unit.nation.portCost + ")", 3, r);
				drawOption("Airfield (" + unit.nation.airfieldCost + ")", 4, r);
			} else if (unit.getID() == UnitID.FACTORY) {
				drawOption("Light tank (" + unit.nation.cavalryCost / 2 + ")", 1, r);
				drawOption("Medium tank (" + unit.nation.cavalryCost + ")", 2, r);
				drawOption("Heavy tank (" + unit.nation.cavalryCost * 2 + ")", 3, r);
			} else if (unit.getID() == UnitID.PORT) {
				drawOption("Landing craft (" + unit.nation.shipCost / 4 + ")", 1, r);
				drawOption("Destroyer (" + unit.nation.shipCost + ")", 2, r);
				drawOption("Cruiser (" + unit.nation.shipCost * 2 + ")", 3, r);
			} else if (unit.getID() == UnitID.AIRFIELD) {
				drawOption("Fighter (" + unit.nation.planeCost / 2 + ")", 1, r);
				drawOption("Attacker (" + unit.nation.planeCost + ")", 2, r);
				drawOption("Bomber (" + unit.nation.planeCost / 2 + ")", 3, r);
			} else if (unit.getID() == UnitID.SHIP) {
				drawOption("", 1, r);
				drawOption("", 2, r);
				drawSlot(1, r);
				drawSlot(2, r);
			}

			r.drawRect((int) position.getX()+5, (int) position.getY()+23, 156, 6, 0);
			r.drawRect((int) position.getX()+7, (int) position.getY()+25, (int) (15.2 * unit.getHealth()), 2, unit.nation.color);
		}
	}

	void drawSlot(int slotID, Render r) {
		int x = (int) position.getX();
		int y = (int) position.getY();
		String name = "-Empty-";
		if (slotID == 1) {
			if (unit.getPassenger1() != null) {
				name = String.valueOf(unit.getPassenger1().getID());
			}
		} else {
			if (unit.getPassenger2() != null) {
				name = String.valueOf(unit.getPassenger2().getID());
			}
		}
		r.drawString(name, x + 85, y + slotID * 30 + 13, 16, r.font16, 0);

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
			if (p.getX() < 855) {
				position.setX(p.getX());
			} else {
				position.setX(855);
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

	public void setPassengers(Unit unit1, Unit unit2) {
		this.passenger1 = unit1;
		this.passenger2 = unit2;
	}
}
