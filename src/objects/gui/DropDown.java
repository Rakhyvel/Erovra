package objects.gui;

import main.Main;
import main.StateID;
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
	int passengers = 0;
	boolean shouldClose = false;
	int closeTick;
	int dropDownHeight;
	boolean chanceToSelect = false;
	boolean cavalry = true;
	boolean leftClicked;

	@Override
	public void tick() {
		if (shouldClose) {
			if (Main.ticks - closeTick > 1) {
				shouldClose = false;
				hide();
			}
		}
		if (shown) {
			unit.setHit(3);
			if (passengers == 0) {
				setDropDownHeight();
				if (isMouseInsideDropDown()) {
					buttonsHovered = getButtonsHovered();
					if (Main.mouse.getMouseLeftDown()) {
						leftClicked = true;
					} else if (leftClicked) {
						leftClicked = false;
						if (unit.getID() == UnitID.INFANTRY) {
							detectInfantry();
							shouldClose();
						} else if (unit.getID() == UnitID.FACTORY) {
							detectFactory();
						} else if (unit.getID() == UnitID.PORT) {
							detectPort();
						} else if (unit.getID() == UnitID.AIRFIELD) {
							detectAirfield();
						} else if (unit.getID() == UnitID.SHIP) {
							passengers = buttonsHovered;
						}
					}
				} else if (unit.getID() != UnitID.SHIP) {
					buttonsHovered = 0;
					if (Main.mouse.getMouseLeftDown())
						shouldClose();
				}
			}
			detectShip();
		}

	}

	@Override
	public void render(Render r) {
		if (shown && Main.gameState == StateID.ONGOING) {
			r.drawRect((int) position.getX(), (int) position.getY(), 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
			r.drawString(name + " (" + (int) unit.getHealth() + "/10)", (int) position.getX() + 85,
					(int) position.getY() + 10, 16, r.font16, 255 << 16 | 255 << 8 | 255);
			if (unit.getID() == UnitID.INFANTRY) {
				if (unit.nation.getCoinAmount() >= unit.nation.getCityCost()) {
					drawOption("City (" + unit.nation.getCityCost() + ")", 1, 0.5f, r);
				} else {
					drawOption("City (" + unit.nation.getCityCost() + ")", 1, 0.7f, r);
				}
				if (unit.nation.getCoinAmount() >= unit.nation.getFactoryCost()) {
					drawOption("Factory (" + unit.nation.getFactoryCost() + ")", 2, 0.5f, r);
				} else {
					drawOption("Factory (" + unit.nation.getFactoryCost() + ")", 2, 0.7f, r);
				}
				if (unit.nation.getCoinAmount() >= unit.nation.getPortCost()) {
					drawOption("Port (" + unit.nation.getPortCost() + ")", 3, 0.5f, r);
				} else {
					drawOption("Port (" + unit.nation.getPortCost() + ")", 3, 0.7f, r);
				}
				if (unit.nation.getCoinAmount() >= unit.nation.getAirfieldCost()) {
					drawOption("Airfield (" + unit.nation.getAirfieldCost() + ")", 4, 0.5f, r);
				} else {
					drawOption("Airfield (" + unit.nation.getAirfieldCost() + ")", 4, 0.7f, r);
				}
			} else if (unit.getID() == UnitID.FACTORY) {
				if (cavalry) {
					drawIndustry(r, "Light tank", "Medium tank", "Heavy tank", unit.nation.getCavalryCost() / 2,
							unit.nation.getCavalryCost(), unit.nation.getCavalryCost() * 2);

					if (unit.getProduct() == UnitID.NONE) {
						drawOption("[ Cavalry >", 4, 0.5f, r);
					}
				} else {
					drawIndustry(r, "Anti air", "Mortar", "Howitzer", unit.nation.getArtilleryCost() / 4,
							unit.nation.getArtilleryCost(), unit.nation.getArtilleryCost() * 2);

					if (unit.getProduct() == UnitID.NONE) {
						drawOption("< Artillery ]", 4, 0.5f, r);
					}
				}
			} else if (unit.getID() == UnitID.PORT) {
				drawIndustry(r, "Landing craft", "Destroyer", "Cruiser", unit.nation.getShipCost() / 4, unit.nation.getShipCost(),
						unit.nation.getShipCost() * 2);
			} else if (unit.getID() == UnitID.AIRFIELD) {
				drawIndustry(r, "Fighter", "Attacker", "Bomber", unit.nation.getPlaneCost() / 2, unit.nation.getPlaneCost(),
						unit.nation.getPlaneCost() / 2);
			} else if (unit.getID() == UnitID.SHIP) {
				drawOption("", 1, 0.5f, r);
				drawOption("", 2, 0.5f, r);
				drawSlot(1, r);
				drawSlot(2, r);
			}

			r.drawRect((int) position.getX() + 5, (int) position.getY() + 23, 156, 6, 0);
			r.drawRect((int) position.getX() + 7, (int) position.getY() + 25, (int) (15.2 * unit.getHealth()), 2,
					unit.nation.color);
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
		r.drawString(name, x + 85, y + slotID * 30 + 13, 16, r.font16, 255 << 16 | 255 << 8 | 255);

	}

	void drawOption(String label, int buttonID, float shade, Render r) {
		int x = (int) position.getX();
		int y = (int) position.getY();
		int textColor = 255 << 16 | 255 << 8 | 255;
		if (shade == 0.7f) {
			textColor = 255 << 16;
		}
		if (buttonsHovered == buttonID && shade != 0.7f) {
			r.drawRect(x, y + buttonID * 30, 170, 30, 255 << 16 | 255 << 8 | 255, 0.5f);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, 16, r.font16, 255 << 16 | 255 << 8 | 255);
		} else {
			r.drawRect(x, y + buttonID * 30, 170, 30, 0, shade);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, 16, r.font16, textColor);
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
			if (p.getY() < 512 - dropDownHeight + 30) {
				position.setY(p.getY());
			} else {
				position.setY(512 - dropDownHeight + 30);
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
		setDropDownHeight();
	}

	private void setDropDownHeight() {
		if (unit.getID() == UnitID.INFANTRY) {
			dropDownHeight = 30 * 6;
		} else if (unit.getID() == UnitID.FACTORY || unit.getID() == UnitID.PORT || unit.getID() == UnitID.CITY
				|| unit.getID() == UnitID.AIRFIELD) {
			if (unit.getProduct() == UnitID.NONE) {
				dropDownHeight = 30 * 6;
			} else {
				dropDownHeight = 30;
			}
		} else if (unit.getID() == UnitID.SHIP) {
			dropDownHeight = 30 * 4;
		} else {
			dropDownHeight = 60;
		}
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

	private void shouldClose() {
		shouldClose = true;
		closeTick = Main.ticks;
	}

	public boolean getShown() {
		return shown && (unit.getID() != UnitID.SHIP);
	}

	public boolean isMouseInsideDropDown() {
		return Main.mouse.getX() >= position.getX() && Main.mouse.getX() < position.getX() + 170
				&& Main.mouse.getY() > position.getY() && Main.mouse.getY() < position.getY() + dropDownHeight;
	}

	public int getButtonsHovered() {
		if (Main.mouse.getY() < position.getY() + 30)
			return 0;
		if (Main.mouse.getY() > position.getY() + dropDownHeight) {
			return 0;
		}
		return (int) (Main.mouse.getY() - position.getY() + 5) / 30 - 1;
	}

	public void detectInfantry() {
		if (buttonsHovered == 1) {
			unit.nation.buyCity(unit.getPosition());
		} else if (buttonsHovered == 2) {
			unit.nation.buyFactory(unit.getPosition());
		} else if (buttonsHovered == 3) {
			unit.nation.buyPort(unit.getPosition());
		} else if (buttonsHovered == 4) {
			unit.nation.buyAirfield(unit.getPosition());
		}
	}

	public void detectFactory() {
		if (unit.getProduct() == UnitID.NONE) {
			if (cavalry) {
				if (buttonsHovered == 1) {
					unit.buyUnit(UnitID.CAVALRY, UnitID.LIGHT, unit.nation.getCavalryCost() / 2, 7200);
				} else if (buttonsHovered == 2) {
					unit.buyUnit(UnitID.CAVALRY, UnitID.MEDIUM, unit.nation.getCavalryCost(), 10800);
				} else if (buttonsHovered == 3) {
					unit.buyUnit(UnitID.CAVALRY, UnitID.HEAVY, unit.nation.getCavalryCost() * 2, 21600);
				} else if (buttonsHovered == 4) {
					cavalry = !cavalry;
				}
			} else {
				if (buttonsHovered == 1) {
					unit.buyUnit(UnitID.ARTILLERY, UnitID.LIGHT, unit.nation.getArtilleryCost() / 4, 3600);
				} else if (buttonsHovered == 2) {
					unit.buyUnit(UnitID.ARTILLERY, UnitID.MEDIUM, unit.nation.getArtilleryCost(), 10800);
				} else if (buttonsHovered == 3) {
					unit.buyUnit(UnitID.ARTILLERY, UnitID.HEAVY, unit.nation.getArtilleryCost() * 2, 21600);
				} else if (buttonsHovered == 4) {
					cavalry = !cavalry;
				}
			}
		}
	}

	public void detectPort() {
		if (unit.getProduct() == UnitID.NONE) {
			if (buttonsHovered == 1) {
				unit.buyUnit(UnitID.SHIP, UnitID.LIGHT, unit.nation.getShipCost() / 4, 3600);
			} else if (buttonsHovered == 2) {
				unit.buyUnit(UnitID.SHIP, UnitID.MEDIUM, unit.nation.getShipCost(), 10800);
			} else if (buttonsHovered == 3) {
				unit.buyUnit(UnitID.SHIP, UnitID.HEAVY, unit.nation.getShipCost() * 2, 10800);
			}
		}
	}

	public void detectAirfield() {
		if (unit.getProduct() == UnitID.NONE) {
			if (buttonsHovered == 1) {
				unit.buyUnit(UnitID.PLANE, UnitID.LIGHT, unit.nation.getCavalryCost() / 2, 5400);
			} else if (buttonsHovered == 2) {
				unit.buyUnit(UnitID.PLANE, UnitID.MEDIUM, unit.nation.getPlaneCost(), 10800);
			} else if (buttonsHovered == 3) {
				unit.buyUnit(UnitID.PLANE, UnitID.HEAVY, unit.nation.getPlaneCost() / 2, 7200);
			}
		}
	}

	public void detectShip() {
		if (passengers != 0) {
			if (chanceToSelect) {
				for (int i = 0; i < unit.nation.unitSize(); i++) {
					if (unit.nation.getUnit(i).isSelected() && !unit.nation.getUnit(i).equals(unit)) {
						unit.nation.getUnit(i).setBoarded(true);
						unit.nation.getUnit(i).setSelected(false);
						Main.world.selectedUnit = null;
						if (passengers == 1) {
							unit.setPassenger1(unit.nation.getUnit(i));
							passengers = 0;
							break;
						} else if (passengers == 2) {
							unit.setPassenger2(unit.nation.getUnit(i));
							passengers = 0;
							break;
						}
					}
				}
				shouldClose();
				chanceToSelect = false;
			}

			if (Main.mouse.getMouseLeftDown() && !isMouseInsideDropDown())
				chanceToSelect = true;
		}
	}

	public void drawIndustry(Render r, String light, String medium, String heavy, int lightCost, int medCost,
			int heavyCost) {
		if (unit.getProduct() != UnitID.NONE) {
			int minutes = 0, seconds = 0;
			String product = "";
			if (unit.getProductWeight() == UnitID.LIGHT) {
				product += light;
			} else if (unit.getProductWeight() == UnitID.MEDIUM) {
				product += medium;
			} else if (unit.getProductWeight() == UnitID.HEAVY) {
				product += heavy;
			}
			if (unit.getStart() > 0) {
				minutes = unit.getStart() / 3600;
				seconds = (unit.getStart() / 60) - minutes * 60;
				product += " " + minutes + "m," + seconds + "s";
			}
			r.drawRect((int) position.getX(), (int) position.getY() + 30, 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
			r.drawString(product, (int) position.getX() + 91, (int) position.getY() + 40, 16, r.font16,
					255 << 16 | 255 << 8 | 255);
		} else {
			if (unit.nation.getCoinAmount() >= lightCost) {
				drawOption(light + " (" + lightCost + ")", 1, 0.5f, r);
			} else {
				drawOption(light + " (" + lightCost + ")", 1, 0.7f, r);
			}
			if (unit.nation.getCoinAmount() >= medCost) {
				drawOption(medium + " (" + medCost + ")", 2, 0.5f, r);
			} else {
				drawOption(medium + " (" + medCost + ")", 2, 0.7f, r);
			}
			if (unit.nation.getCoinAmount() >= heavyCost) {
				drawOption(heavy + " (" + heavyCost + ")", 3, 0.5f, r);
			} else {
				drawOption(heavy + " (" + heavyCost + ")", 3, 0.7f, r);
			}
		}
	}
}
