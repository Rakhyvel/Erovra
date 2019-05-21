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
	int[] icon1;
	int[] icon2;
	boolean passed = false;
	Point pathfind;

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
		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			setDefense(1.5f);
		} else {
			speed = .075f;
			setDefense(3f);
		}
		id = UnitID.SHIP;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (!(nation.defeated || nation.enemyNation.defeated) && health > 0) {
			if (getWeight() != UnitID.LIGHT) {
				shootTorpedo();
				aaAim();
				if (!nation.isAIControlled()) {
					clickToMove();
					clickToDropDown();
				}
				if (spotted > 0){
					spotted--;
				} else {
					disengage();
				}
				targetMove(0);
			} else {
//				if (getPassenger1() != null) engaged = aaAim();
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
					nation.removeUnit(this);
				}

				if (nation.isAIControlled()) {
					if (getPassenger1() != null) {
						if(!passed){
							if (pathfind != null) {
								velocity = position.getTargetVector(pathfind).normalize().scalar(speed);
								setFacing(pathfind);
								if (position.getDist(pathfind) < 2)
									pathfind = pathfind(target, 0f);
							} else {
								velocity = position.getTargetVector(target).normalize().scalar(speed);
								setFacing(target);
							}
						}
						position = position.addVector(velocity);
						if(!passed)
							if(position.getDist(target) < 1)
								passed = true;
						if (position.getX() < 0 || position.getX() > 1024 || position.getY() < 0 || position.getY() > 512) {
							nation.removeUnit(getPassenger1());
							nation.removeUnit(getPassenger2());
							nation.removeUnit(this);
							System.out.println(target.toString());
						}
					} else {
						velocity.setX(0);
						velocity.setY(0);
						loadPassengers();
					}
				} else {
					clickToMove();
					targetMove(0);
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
										setIcon(1, nation.getUnit(i).id, nation.getUnit(i).weight);
										break;
									} else if (passengers == 2 && getPassenger2() == null) {
										setPassenger2(nation.getUnit(i));
										setIcon(2, nation.getUnit(i).id, nation.getUnit(i).weight);
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
			}
			if (engaged && spotted == 0 || hit > 0) {
				spotted = (int) (60 / speed);
				if (!nation.engagedUnits.contains(this)) nation.engagedUnits.add(this);
			}
			if (spotted > 0) {
				spotted--;
			} else {
				disengage();
			}
		} else {
			nation.removeUnit(getPassenger1());
			nation.removeUnit(getPassenger2());
		}
	}

	private void setIcon(int i, UnitID id, UnitID weight) {
		if (i == 1) {
			if (id == UnitID.CAVALRY) {
				icon1 = Render.getScreenBlend(Render.getColor(weight, nation.color), Render.cavalry);
			} else if (id == UnitID.INFANTRY) {
				icon1 = Render.getScreenBlend(Render.getColor(weight, nation.color), Render.infantry);
			} else if (id == UnitID.ARTILLERY) {
				icon1 = Render.getScreenBlend(Render.getColor(weight, nation.color), Render.artillery);
			}
		} else {
			if (id == UnitID.CAVALRY) {
				icon2 = Render.getScreenBlend(Render.getColor(weight, nation.color), Render.cavalry);
			} else if (id == UnitID.INFANTRY) {
				icon2 = Render.getScreenBlend(Render.getColor(weight, nation.color), Render.infantry);
			} else if (id == UnitID.ARTILLERY) {
				icon2 = Render.getScreenBlend(Render.getColor(weight, nation.color), Render.artillery);
			}
		}
	}

	// isLander(): checks to see if the boat has reached land, used for landind
	// craft
	boolean isLanded() {
		return Map.getArray(position) > 0.51f;
	}

	void loadPassengers() {
		int smallestDistance = 1310720;
		int firstUnit = -1;
		int secondUnit = -1;
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
				firstUnit = i;
				unitCount++;
			}
		}
		if (firstUnit != -1 && unitCount > 2) {
			setPassenger1(nation.getUnit(firstUnit));
			getPassenger1().setBoarded(true);
			getPassenger1().disengage();
			if (secondUnit != -1) {
				setPassenger2(nation.getUnit(secondUnit));
				getPassenger2().setBoarded(true);
				getPassenger2().disengage();
			}
		}
	}

	@Override
	public void render(Render r) {
		if (spotted > 0 || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			float direction = 0;
			if(weight == UnitID.LIGHT){
				direction = velocity.getRadian();
				if (velocity.getY() > 0) direction += 3.14f;
			} else {
				direction = position.subVec(getFacing()).getRadian();
				if (position.subVec(getFacing()).getY() > 0) direction += 3.14f;
			}
			

			if (!nation.isAIControlled()) {
				if (isSelected()) {
					r.drawSeaLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY()) || Main.world.getShowPaths()) {
					if (weight == UnitID.HEAVY) r.drawImage((int) position.getX(), (int) position.getY(), 128, r.medArtRange, 0.5f, 0);
					r.drawSeaLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color, 255 << 24 | 220 << 16 | 220 << 8 | 220);
				}
			}
			if (getWeight() == UnitID.LIGHT) r.drawImage((int) position.getX(), (int) position.getY(), 13, Render.getScreenBlend(Render.getColor(weight, nation.color), r.landing), 1, direction);
			if (getWeight() == UnitID.MEDIUM) r.drawImage((int) position.getX(), (int) position.getY(), 13, Render.getScreenBlend(Render.getColor(weight, nation.color), r.destroyer), 1, direction);
			if (getWeight() == UnitID.HEAVY) r.drawImage((int) position.getX(), (int) position.getY(), 14, Render.getScreenBlend(Render.getColor(weight, nation.color), r.cruiser), 1, direction);
			
			if ((hit > 1) && getWeight() == UnitID.LIGHT) r.drawImage((int) position.getX(), (int) position.getY(), 17, Render.getScreenBlend(Render.getColor(weight, nation.color), r.landingHit), 1, direction);
			if ((hit > 1) && getWeight() == UnitID.MEDIUM) r.drawImage((int) position.getX(), (int) position.getY(), 17, Render.getScreenBlend(Render.getColor(weight, nation.color), r.destroyerHit), 1, direction);
			if ((hit > 1) && getWeight() == UnitID.HEAVY) r.drawImage((int) position.getX(), (int) position.getY(), 18, Render.getScreenBlend(Render.getColor(weight, nation.color), r.cruiserHit), 1, direction);
		}
	}

	void decideTarget() {
		Point smallestPoint = new Point(-1, -1);
		double smallestDist = 1310720;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if ((tempUnit.id == UnitID.FACTORY || tempUnit.id == UnitID.CITY || tempUnit.id == UnitID.AIRFIELD) && ((nation.airSupremacy < nation.enemyNation.airSupremacy && tempUnit.id == UnitID.AIRFIELD) || (nation.landSupremacy < nation.enemyNation.landEngagedSupremacy && tempUnit.id == UnitID.FACTORY) || (nation.airSupremacy >= nation.enemyNation.airSupremacy && nation.seaSupremacy >= nation.enemyNation.seaEngagedSupremacy && nation.landSupremacy >= nation.enemyNation.landEngagedSupremacy))) {
				Point tempPoint = tempUnit.getPosition();
				double tempDist = tempPoint.getDist(position);
				if (smallestDist > tempDist) {
					smallestPoint = tempPoint;
					smallestDist = tempDist;
				}
				System.out.println(tempUnit.id);
			}
		}
		if (smallestPoint.getX() != -1) {
			setTarget(smallestPoint);
			setFacing(smallestPoint);
			pathfind = pathfind(target, 0f);
		}
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
		if (name.contains("-Empty") || name.contains("-Select-")) {
			r.drawString(name, x + 85, y + slotID * 30 + 15, r.font16, 255 << 24 | 250 << 16 | 250 << 8 | 250);
		} else {
			r.drawString(name, x + 7, y + slotID * 30 + 15, r.font16, 255 << 24 | 250 << 16 | 250 << 8 | 250, false);
			if (slotID == 1 && icon1 != null) {
				r.drawImage(x + 157, y + slotID * 30 + 15, 32, icon1, 1, 0);
				r.drawRectBorders(x + 133, y + slotID * 30, 3, 30, 0, 4);
			}
			if (slotID == 2 && icon2 != null) {
				r.drawImage(x + 157, y + slotID * 30 + 15, 32, icon2, 1, 0);
				r.drawRectBorders(x + 133, y + slotID * 30, 3, 30, 0, 4);
			}

		}
	}

	void drawOption(String label, int buttonID, float shade, Render r, DropDown d) {
		int x = (int) d.getPosition().getX();
		int y = (int) d.getPosition().getY();
		int textColor = 255 << 24 | 250 << 16 | 250 << 8 | 250;
		if (shade == 0.7f) {
			textColor = 255 << 24 | 255 << 16;
		}
		boolean hovered = buttonID == d.getButtonsHovered();
		int rectColor = 32;
		int borders = 32;
		if (buttonID == 1) {
			if (getPassenger1() != null) {
				hovered = false;
				rectColor = 0;
			}
		} else if (buttonID == 2) {
			if (getPassenger2() != null) {
				hovered = false;
				rectColor = 0;
			}
		}
		if (buttonID == 1) {
			borders = 5;
		} else if (buttonID == 2) {
			borders = 13;
		}
		if (hovered | passengers == buttonID) {
			rectColor *= 2.7;
		}
		r.drawRectBorders(x, y + buttonID * 30, 180, 30, 180 << 24 | rectColor << 16 | rectColor << 8 | rectColor, borders);
		r.drawString(label, x + 85, y + 13 + buttonID * 30, r.font16, textColor, false);
	}
}