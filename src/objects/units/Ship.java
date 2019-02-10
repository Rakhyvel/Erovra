package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import terrain.Map;
import utility.Point;

/**
 * Handles the logic and rendering for ships
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class Ship extends Unit {

	private Unit passenger1;
	private Unit passenger2;
	private int passengers = 0;
	boolean clicked = false;
	public boolean chanceToSelect = false;

	public Ship(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			defense = .3f;
			if (nation.isAIControlled()) {
				setTarget(nation.enemyNation.capital.position.addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96)));
				velocity = position.subVec(getTarget()).normalize().scalar(speed);
			}
		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			defense = 2;
		} else {
			speed = .05f;
			defense = 3;
		}
		id = UnitID.SHIP;
	}

	@Override
	public void tick(double t) {
		if (!(nation.defeated || nation.enemyNation.defeated)) {
			detectHit();
			if (getWeight() != UnitID.LIGHT) {
				engaged = torpedoAim() || aaAim();
				if (nation.isAIControlled()) {
					wander();
				} else {
					clickToMove();
				}
				targetMove();
			} else {
				engaged = aaAim();
				if (isLanded()) {
					if (getPassenger1() != null) {
						getPassenger1().position = position;
						getPassenger1().setBoarded(false);
						if (getPassenger2() != null) {
							getPassenger2().position = position;
							getPassenger2().setBoarded(false);
						}
					}
					nation.unitArray.remove(this);
				}
				if (nation.isAIControlled()) {
					if (getPassenger2() != null) {
						position = position.addVector(velocity);
					} else {
						loadPassengers();
					}
				} else {
					clickToMove();
					targetMove();
					clickToDropDown();
				}
				if (health < 0) {
					nation.unitArray.remove(getPassenger1());
					nation.unitArray.remove(getPassenger2());
				}
				if (passengers != 0) {
					if (chanceToSelect) {
						for (int i = 0; i < nation.unitSize(); i++) {
							if (nation.getUnit(i).isSelected() && !nation.getUnit(i).equals(this)) {
								nation.getUnit(i).setBoarded(true);
								nation.getUnit(i).setSelected(false);
								Main.world.selectedUnit = null;
								if (passengers == 1 && getPassenger1()==null) {
									setPassenger1(nation.getUnit(i));
									passengers = 0;
									break;
								} else if (passengers == 2 && getPassenger1()==null) {
									setPassenger2(nation.getUnit(i));
									passengers = 0;
									break;
								}
							}
						}
						chanceToSelect = false;
						Main.world.getDropDown().shouldClose();
					}
					if (!Main.world.getDropDown().isMouseInsideDropDown() && Main.mouse.getMouseLeftDown()) {
						clicked = true;
					} else if (clicked) {
						clicked = false;
						chanceToSelect = true;
					}
				}
			}
		}
	}

	// isLander(): checks to see if the boat has reached land, used for landind
	// craft
	boolean isLanded() {
		return Map.getArray(position) > 0.505f;
	}

	void loadPassengers() {
		int smallestDistance = 524288;
		Unit firstUnit = null;
		Unit secondUnit = null;
		setPassenger1(null);
		setPassenger2(null);
		int unitCount = 0;
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && ((tempUnit.id == UnitID.CAVALRY) || (tempUnit.id == UnitID.INFANTRY) || (tempUnit.id == UnitID.ARTILLERY)) && !tempUnit.engaged && !tempUnit.isBoarded()) {
				smallestDistance = tempDist;
				secondUnit = firstUnit;
				firstUnit = tempUnit;
				unitCount++;
			}
		}
		if (firstUnit != null && unitCount > 2) {
			firstUnit.setBoarded(true);
			setPassenger1(firstUnit);
			if (secondUnit != null) {
				secondUnit.setBoarded(true);
				setPassenger2(secondUnit);
			}
		}
	}

	@Override
	public void render(Render r) {
		if (engaged || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			float direction = position.subVec(getTarget()).getRadian();
			if (velocity.getY() > 0) direction += 3.14f;

			if (getWeight() == UnitID.LIGHT) r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.landing, r.lighten(nation.color), direction);
			if (getWeight() == UnitID.MEDIUM) r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.destroyer, nation.color, direction);
			if (getWeight() == UnitID.HEAVY) r.drawImageScreen((int) position.getX(), (int) position.getY(), 16, r.cruiser, r.darken(nation.color), direction);

			if ((hit > 1) && getWeight() == UnitID.LIGHT) r.drawImageScreen((int) position.getX(), (int) position.getY(), 17, r.landingHit, r.lighten(nation.color), direction);
			if ((hit > 1) && getWeight() == UnitID.MEDIUM) r.drawImageScreen((int) position.getX(), (int) position.getY(), 17, r.destroyerHit, nation.color, direction);
			if ((hit > 1) && getWeight() == UnitID.HEAVY) r.drawImageScreen((int) position.getX(), (int) position.getY(), 20, r.cruiserHit, r.darken(nation.color), direction);
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
		passengers = d.buttonsHovered;
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		drawOption("", 1, 0.5f, r, d);
		drawOption("", 2, 0.5f, r, d);
		drawSlot(1, r, d);
		drawSlot(2, r, d);
	}

	/**
	 * @return
	 */
	public Unit getPassenger1() {
		return passenger1;
	}

	/**
	 * @param passenger1
	 */
	public void setPassenger1(Unit passenger1) {
		this.passenger1 = passenger1;
	}

	/**
	 * @return
	 */
	public Unit getPassenger2() {
		return passenger2;
	}

	/**
	 * @param passenger2
	 */
	public void setPassenger2(Unit passenger2) {
		this.passenger2 = passenger2;
	}

	void drawSlot(int slotID, Render r, DropDown d) {
		int x = (int) d.getPosition().getX();
		int y = (int) d.getPosition().getY();
		String name = "-Empty-";
		if (slotID == 1) {
			if (getPassenger1() != null) {
				name = String.valueOf(getPassenger1().getID());
			}
		} else {
			if (getPassenger2() != null) {
				name = String.valueOf(getPassenger2().getID());
			}
		}
		r.drawString(name, x + 85, y + slotID * 30 + 13, r.font16, 250 << 16 | 250 << 8 | 250);

	}

	void drawOption(String label, int buttonID, float shade, Render r, DropDown d) {
		int x = (int) d.getPosition().getX();
		int y = (int) d.getPosition().getY();
		int textColor = 250 << 16 | 250 << 8 | 250;
		if (shade == 0.7f) {
			textColor = 255 << 16;
		}
		if (passengers == buttonID && shade != 0.7f) {
			r.drawRect(x, y + buttonID * 30, 170, 30, 200 << 16 | 200 << 8 | 200, 0.5f);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, r.font16, 250 << 16 | 250 << 8 | 250);
		} else {
			r.drawRect(x, y + buttonID * 30, 170, 30, 0, shade);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, r.font16, textColor);
		}
	}
}