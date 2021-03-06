package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
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
	int buyInCost = 0;

	public Port(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.PORT;
		setDefense(1);
		setProductWeight(UnitID.NONE);
		weight = UnitID.LIGHT;
		buyInCost = nation.getPortCost() / 2;
		nation.unupgradedPorts += 1;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (engaged || hit > 0) {
				spotted = true;
				if (!nation.engagedUnits.contains(this))
					nation.engagedUnits.add(this);
			}
			disengage();
			if (!(nation.defeated || nation.enemyNation.defeated)) {
				if (!nation.isAIControlled()) {
					clickToDropDown();
				}
				if (getStart() < 0) {
					addProduct();
					if (nation.isAIControlled()) {
						if (!upgrading) {
							if (weight == UnitID.LIGHT && buyInCost < nation.coins
									&& nation.airSupremacy >= nation.enemyNation.airEngagedSupremacy) {
								upgrade(nation.getPortCost() / 2);
							} else if (weight == UnitID.MEDIUM && buyInCost * 2 < nation.coins
									&& nation.airSupremacy >= nation.enemyNation.airEngagedSupremacy) {
								upgrade(nation.getPortCost() / 2);
								nation.unupgradedPorts -= 1;
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
				if (getProductWeight() != UnitID.LIGHT) {
				}
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
		reviewAutomatic();
	}

	/**
	 * If the nation is AI controlled, decides what ship to build
	 */
	public void decideNewProduct() {

		if ((nation.getCityCost()) >= nation.unitSize()) {
			if (1 <= nation.airSupremacy) {
				int smallestDistance = 1310720;
				int unitCount = 0;
				for (int i = 0; i < nation.unitSize(); i++) {
					Unit tempUnit = nation.getUnit(i);
					int tempDist = (int) position.getDist(tempUnit.getPosition());
					if (tempDist < smallestDistance
							&& ((tempUnit.id == UnitID.CAVALRY) || (tempUnit.id == UnitID.INFANTRY)
									|| (tempUnit.id == UnitID.ARTILLERY && tempUnit.weight != UnitID.LIGHT))
							&& !tempUnit.isBoarded()) {
						smallestDistance = tempDist;
						unitCount++;
					}
				}
				if (unitCount > 3) {
					buyUnit(UnitID.SHIP, UnitID.LIGHT, (int) (nation.getShipCost() / 4 * getDefense() * 0.5),
							getTime(weight, UnitID.LIGHT));
				}
			} else if(nation.seaSupremacy-nation.enemyNation.seaEngagedSupremacy < 5){
				// If nation does not have sea supremacy
				if (buyUnit(UnitID.SHIP, UnitID.HEAVY, nation.getShipCost() * 2 * getDefense() * 0.5,
						getTime(weight, UnitID.HEAVY))) {
					nation.seaSupremacy++;
					// If nation can afford cruiser
				} else {
					buyUnit(UnitID.SHIP, UnitID.MEDIUM, nation.getShipCost() * getDefense() * 0.5,
							getTime(weight, UnitID.MEDIUM));
					nation.seaSupremacy++;
					// If nation cannot afford cruiser, but can afford destroyer
				}
			}
		}
	}

	@Override
	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) {
			if (getProductWeight() != UnitID.NONE && getStart() > 1 && nation.name.contains("Sweden")) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 255 << 24);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18,
						(int) (28.0 * ((maxStart - getStart()) / maxStart)), 2, nation.color);
			}
			r.drawImage((int) position.getX(), (int) position.getY(), 32,
					Render.getScreenBlend(Render.getColor(weight, nation.color), r.port), 1, 0);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36, r.cityHit, 1, 0);
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
							getTime(weight, UnitID.LIGHT));
				} else if (d.buttonsHovered == 3) {
					buyUnit(UnitID.SHIP, UnitID.MEDIUM, nation.getShipCost() * getDefense() * 0.5,
							getTime(weight, UnitID.MEDIUM));
				} else if (d.buttonsHovered == 4) {
					buyUnit(UnitID.SHIP, UnitID.HEAVY, nation.getShipCost() * 2 * getDefense() * 0.5,
							getTime(weight, UnitID.HEAVY));
				}
			} else if (d.getTab() == 1) {
				if (d.buttonsHovered == 2 && weight != UnitID.HEAVY) {
					upgrade(nation.getPortCost() / 2);
				} else if (d.buttonsHovered == 3) {
					nation.unitArray.remove(this);
					d.shouldClose();
					nation.coins += 10;
					nation.setPortCost(nation.getPortCost() / 2);
				}
			}
		} else {
			if (d.buttonsHovered == 2) {
				// This part cancels both upgrades and production. The or operator
				// differenciates between the two
				if (Main.mouse.getX() > d.getPosition().getX() + 90 || getProduct() == UnitID.PORT) {
					cancelOrder();
					d.setTab(0);
				} else {
					automatic = !automatic;
				}
			}
		}
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		dropDownHeight = getDropDownHeight();
		d.setPosition(position);
		if (!upgrading) {
			if (getProduct() == UnitID.NONE)
				d.drawTab(2, r.destroyer2, r.settings, null, 45, 25, 16, r);
			if (d.getTab() == 0) {
				d.drawIndustry(r, "Landing craft", "Destroyer", "Cruiser",
						(int) (nation.getShipCost() / 4 * (getDefense() / 2)),
						nation.getShipCost() * (getDefense() / 2), nation.getShipCost() * 2 * (getDefense() / 2), this);
			} else if (d.getTab() == 1) {
				if (weight != UnitID.HEAVY) {
					if (nation.getCoinAmount() >= nation.getPortCost() / 2) {
						d.drawOption("Upgrade (" + nation.getPortCost() / 2 + ")", 2, 32, 5, r);
					} else {
						d.drawOption("Upgrade (" + nation.getPortCost() / 2 + ")", 2, 0, 5, r);
					}
				} else {
					d.drawOption("Fully upgraded", 2, 32, 5, r);
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
