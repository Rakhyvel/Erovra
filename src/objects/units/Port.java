package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.gui.Image;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for ports
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class Port extends Industry {

	private boolean spotted = false;
	Image port = new Image("/res/buildings/port.png", 32, 32);
	private static Image[] icons = { new Image("/res/water/destroyer.png", 13, 45),
			new Image("/res/target.png", 32, 32).resize(0.75f) };
	int buyInCost = 0;

	public Port(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.PORT;
		setDefense(1);
		setProductWeight(UnitID.NONE);
		icons[0].setRotation(-(float) Math.PI / 2);
		weight = UnitID.LIGHT;
		buyInCost = nation.getPortCost() / 2;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (engaged || hit > 0)
				spotted = true;
			disengage();
			if (!(nation.defeated || nation.enemyNation.defeated)) {
				if (!nation.isAIControlled()) {
					clickToDropDown();
				}
				if (getStart() < 0) {
					addProduct();
					if (nation.isAIControlled()) {
						if (!upgrading) {
							if (weight == UnitID.LIGHT && buyInCost < nation.getPortCost()) {
								upgrade(nation.getPortCost() / 2);
							} else if (weight == UnitID.MEDIUM && buyInCost * 2 < nation.getPortCost()) {
								upgrade(nation.getPortCost() / 2);
							} else {
								decideNewProduct();
							}
						}
					}
				} else {
					setStart(getStart() - 1);
				}
			}
		}
	}

	/**
	 * Adds the manufactured product into the game
	 */
	public void addProduct() {
		if (getProductWeight() != UnitID.NONE && getProductWeight() != null) {
			if (getProduct() != UnitID.PORT) {
				nation.addUnit(new Ship(position, nation, getProductWeight()));
				if (getProductWeight() == UnitID.MEDIUM)
					nation.seaSupremacy++;
				if (getProductWeight() == UnitID.HEAVY)
					nation.seaSupremacy += 2;
			} else {
				if (weight == UnitID.MEDIUM) {
					weight = UnitID.HEAVY;
					setDefense(4);
				} else if (weight == UnitID.LIGHT) {
					weight = UnitID.MEDIUM;
					setDefense(2);
				}
				upgrading = false;
			}
		}
		setProduct(UnitID.NONE);
		setProductWeight(UnitID.NONE);
	}

	/**
	 * If the nation is AI controlled, decides what ship to build
	 */
	public void decideNewProduct() {
		int smallestDistance = 1310720;
		int unitCount = 0;
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance
					&& ((tempUnit.id == UnitID.CAVALRY) || (tempUnit.id == UnitID.INFANTRY)
							|| (tempUnit.id == UnitID.ARTILLERY && tempUnit.weight != UnitID.LIGHT))
					&& !tempUnit.isBoarded() && !tempUnit.engaged) {
				unitCount++;
			}
		}
		smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if ((tempUnit.getID() != UnitID.SHIP && tempUnit.getID() != UnitID.PLANE
					&& tempUnit.getID() != UnitID.PORT)) {
				if (wetLandingPath(tempPoint, 16) || tempDist < 16384 || tempUnit.capital) {
					smallestPoint = tempPoint;
				}
			}
		}
		if (unitCount > 3 && smallestPoint.getX() != -1 && nation.enemyNation.seaSupremacy < nation.seaSupremacy) {
			buyUnit(UnitID.SHIP, UnitID.LIGHT, nation.getShipCost() / 4 * getDefense() * 0.5, getTime(weight,UnitID.LIGHT));
		} else {
			if (nation.enemyNation.landSupremacy >= nation.landSupremacy
					|| nation.enemyNation.airSupremacy >= nation.airSupremacy) {
				if (buyUnit(UnitID.SHIP, UnitID.HEAVY, nation.getShipCost() * 2 * getDefense() * 0.5,
						getTime(weight,UnitID.HEAVY))) {
				} else {
					buyUnit(UnitID.SHIP, UnitID.MEDIUM, nation.getShipCost() * getDefense() * 0.5,
							getTime(weight,UnitID.MEDIUM));
				}
			}
		}
	}

	@Override
	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) {
			if (getProductWeight() != UnitID.NONE && getStart() > 1) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 255 << 24);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18,
						(int) (28.0 * ((maxStart - getStart()) / maxStart)), 2, nation.color);
			}
			r.drawImage((int) position.getX(), (int) position.getY(), Render.getWeighted(port, weight, nation.color),
					0);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), r.cityHit, 0);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
		if (getProduct() == UnitID.NONE) {
			if (d.buttonsHovered == 1) {
				d.selectTabs(2);
			}
			if (d.getTab() == 0) {
				if (d.buttonsHovered == 2) {
					buyUnit(UnitID.SHIP, UnitID.LIGHT, nation.getShipCost() / 4 * getDefense() * 0.5,
							getTime(weight,UnitID.LIGHT));
				} else if (d.buttonsHovered == 3) {
					buyUnit(UnitID.SHIP, UnitID.MEDIUM, nation.getShipCost() * getDefense() * 0.5,
							getTime(weight,UnitID.MEDIUM));
				} else if (d.buttonsHovered == 4) {
					buyUnit(UnitID.SHIP, UnitID.HEAVY, nation.getShipCost() * 2 * getDefense() * 0.5,
							getTime(weight,UnitID.HEAVY));
				}
			} else if (d.getTab() == 1) {
				if (d.buttonsHovered == 2) {
					if (nation.getCoinAmount() >= nation.getPortCost() / 2) {
						upgrade(nation.getPortCost() / 2);
					} else {
						Main.world.errorMessage.showErrorMessage("Insufficient funds!");
					}
				} else if (d.buttonsHovered == 3) {
					nation.unitArray.remove(this);
					d.shouldClose();
					nation.coins += 10;
					nation.setPortCost(nation.getPortCost() / 2);
				}
			}
		} else {
			if (d.buttonsHovered == 2) {
				setProductWeight(UnitID.NONE);
				setProduct(UnitID.NONE);
				nation.coins += 10;
				upgrading = false;
			}
		}
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		dropDownHeight = getDropDownHeight();
		d.setPosition(position);
		if (!upgrading) {
			if (getProduct() == UnitID.NONE)
				d.drawTab(2, icons, r);
			if (d.getTab() == 0) {
				d.drawIndustry(r, "Landing craft", "Destroyer", "Cruiser",
						nation.getShipCost() / 4 * (getDefense() / 2), nation.getShipCost() * (getDefense() / 2),
						nation.getShipCost() * 2 * (getDefense() / 2), this);
			} else if (d.getTab() == 1) {
				if (nation.getCoinAmount() >= nation.getPortCost() / 2) {
					d.drawOption("Upgrade (" + nation.getPortCost() / 2 + ")", 2, 32, 5, r);
				} else {
					d.drawOption("Upgrade (" + nation.getPortCost() / 2 + ")", 2, 0, 5, r);
				}
				d.drawOption("Decommision", 3, 32, 5, r);
				r.drawRectBorders((int) d.getPosition().getX(), (int) d.getPosition().getY() + 30 * 4, 180, 30,
						180 << 24 | 32 << 16 | 32 << 8 | 32, 13);
			}
		} else {
			d.drawUpgrading(this, r);
		}
	}

}
