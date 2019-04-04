package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.gui.Image;
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
	int weightColor = 255 << 24;
	Image ship;
	Image shipHit;

	public Ship(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			setDefense(1f);
			if (nation.isAIControlled()) {
				decideTarget();
			}
			velocity.setX(0);
			velocity.setY(0);
			dropDownHeight = 90;
			weightColor = Render.lighten(nation.color);
			ship = new Image("/res/water/landing.png", 13, 32).getScreenBlend(weightColor);
			shipHit = new Image("/res/water/landingHit.png", 17, 36).getScreenBlend(weightColor);
		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			setDefense(2);
			weightColor = nation.color;
			ship = new Image("/res/water/destroyer.png", 13, 45).getScreenBlend(weightColor);
			shipHit = new Image("/res/water/destroyerHit.png", 17, 49).getScreenBlend(weightColor);
		} else {
			speed = .075f;
			setDefense(6);
			weightColor = Render.darken(nation.color);
			ship = new Image("/res/water/cruiser.png", 14, 61).getScreenBlend(weightColor);
			shipHit = new Image("/res/water/cruiserHit.png", 18, 65).getScreenBlend(weightColor);
		}
		id = UnitID.SHIP;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (!(nation.defeated || nation.enemyNation.defeated) && health > 0) {
			if (getWeight() != UnitID.LIGHT) {
				if (nation.isAIControlled()) {
					wander();
				} else {
					clickToMove();
				}
				if (getWeight() == UnitID.HEAVY) {
					engaged = torpedoAim() || aaAim() || autoArtilleryAim(64);
				} else {
					engaged = torpedoAim() || aaAim();
				}
				targetMove();
			} else {
				if (getPassenger1() != null) engaged = aaAim();
				if (isLanded()) {
					if (getPassenger1() != null) {
						getPassenger1().position = position;
						getPassenger1().setBoarded(false);
						getPassenger1().setTarget(position.addPoint(new Point(.1, 0)));
						if (getPassenger2() != null) {
							getPassenger2().position = position;
							getPassenger2().setBoarded(false);
							getPassenger2().setTarget(position.addPoint(new Point(.1, 0)));
						}
					}
					nation.unitArray.remove(this);
				}

				if (nation.isAIControlled()) {
					if (getPassenger1() != null) {
						position = position.addVector(velocity);
						if (position.getX() < -velocity.getX() || position.getX() > 1024 - velocity.getX() || position.getY() < -velocity.getY() || position.getY() > 512 - velocity.getY()) {
							nation.unitArray.remove(getPassenger1());
							nation.unitArray.remove(getPassenger2());
							nation.unitArray.remove(this);
						}
					} else {
						velocity.setX(0);
						velocity.setY(0);
						loadPassengers();
					}
				} else {
					clickToMove();
					targetMove();
					clickToDropDown();
					if (passengers != 0) {
						if (chanceToSelect) {
							for (int i = 0; i < nation.unitSize(); i++) {
								if (nation.getUnit(i).isSelected() && nation.getUnit(i).getID() != UnitID.PLANE && nation.getUnit(i).getID() != UnitID.SHIP) {
									nation.getUnit(i).setBoarded(true);
									nation.getUnit(i).setSelected(false);
									Main.world.selectedUnit = null;
									if (passengers == 1 && getPassenger1() == null) {
										setPassenger1(nation.getUnit(i));
										break;
									} else if (passengers == 2 && getPassenger2() == null) {
										setPassenger2(nation.getUnit(i));
										break;
									}
								}
							}
							passengers = 0;
							chanceToSelect = false;
							Main.world.getDropDown().shouldClose();
						}
						if (Main.mouse.getMouseLeftDown()) {
							clicked = true;
							dropDownHeight = 0;
						} else if (clicked) {
							clicked = false;
							chanceToSelect = true;
						}
					} else {
						dropDownHeight = 90;
					}
				}
				if (health < 0) {
					nation.unitArray.remove(getPassenger1());
					nation.unitArray.remove(getPassenger2());
				}
			}
			if (engaged && spotted == 0 || hit > 0) {
				spotted = (int) (60 / speed);
			}
			if (spotted > 0) spotted--;
		}
	}

	// isLander(): checks to see if the boat has reached land, used for landind
	// craft
	boolean isLanded() {
		return Map.getArray(position) > 0.5f;
	}

	void loadPassengers() {
		decideTarget();
		int smallestDistance = 1310720;
		Unit firstUnit = null;
		Unit secondUnit = null;
		int unitCount = 0;
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempUnit.weight == UnitID.HEAVY) {
				tempDist /= 4.0;
			} else if (tempUnit.weight == UnitID.MEDIUM) {
				tempDist /= 2.0;
			}
			if (tempDist < smallestDistance && ((tempUnit.id == UnitID.CAVALRY) || (tempUnit.id == UnitID.INFANTRY) || (tempUnit.id == UnitID.ARTILLERY && tempUnit.weight != UnitID.LIGHT)) && !tempUnit.isBoarded()) {
				smallestDistance = tempDist;
				secondUnit = firstUnit;
				firstUnit = tempUnit;
				unitCount++;
			}
		}
		if (firstUnit != null && unitCount > 2) {
			setPassenger1(firstUnit);
			getPassenger1().setBoarded(true);
			getPassenger1().disengage();
			if (secondUnit != null) {
				setPassenger2(secondUnit);
				getPassenger2().setBoarded(true);
				getPassenger2().disengage();
			}
		}
	}

	@Override
	public void render(Render r) {
		if (spotted > 0 || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			float direction = position.subVec(getFacing()).getRadian();
			if (position.subVec(getFacing()).getY() > 0) direction += 3.14f;

			if (!nation.isAIControlled()) {
				if (weight == UnitID.HEAVY) r.drawImage((int) position.getX(), (int) position.getY(), r.medArtRange, 0);
				if (isSelected()) {
					r.drawSeaLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
					r.drawSeaLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color, 220 << 16 | 220 << 8 | 220);
				}
			}

			r.drawImage((int) position.getX(), (int) position.getY(), ship, direction);

			if ((hit > 1)) r.drawImage((int) position.getX(), (int) position.getY(), shipHit, direction);
		}
	}

	void decideTarget() {
		Point smallestPoint = new Point(-1, -1);
		double smallestDist = 1310720;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			double tempDist = tempPoint.getDist(position);
			if ((tempUnit.getID() != UnitID.SHIP && tempUnit.getID() != UnitID.PLANE && tempUnit.getID() != UnitID.PORT)) {
				if (smallestDist > tempDist) {
					smallestPoint = tempPoint;
					smallestDist = tempDist;
				}
			}
		}
		setTarget(smallestPoint);
		setFacing(getTarget());
		velocity = position.subVec(getTarget()).normalize().scalar(speed);
	}

	@Override
	public void dropDownDecide(DropDown d) {
		passengers = 0;
		if (d.buttonsHovered == 1) {
			if (getPassenger1() == null) {
				passengers = d.buttonsHovered;
			}
		} else if (d.buttonsHovered == 2) {
			if (getPassenger2() == null) {
				passengers = d.buttonsHovered;
			}
		}
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		d.setPosition(position);
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
		if (passengers == slotID) {
			name = "-Select-";
		}
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
		boolean hovered = buttonID == d.getButtonsHovered();
		if (buttonID == 1) {
			if (getPassenger1() != null) hovered = false;
		} else if (buttonID == 2) {
			if (getPassenger2() != null) hovered = false;
		}
		if ((hovered | passengers == buttonID) && shade != 0.7f) {
			r.drawRect(x, y + buttonID * 30, 170, 30, 128 << 24 | 200 << 16 | 200 << 8 | 200);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, r.font16, 250 << 16 | 250 << 8 | 250);
		} else {
			r.drawRect(x, y + buttonID * 30, 170, 30, (int) (shade * 255) << 24);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, r.font16, textColor);
		}
	}
}