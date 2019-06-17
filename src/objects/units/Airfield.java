package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;

/**
 * Handles the airfield logic and rendering, decides what planes should be made
 * when unit's nation is AI controlled.
 * 
 * @author Rakhyvel
 * @see Unit
 *
 */
public class Airfield extends Industry {

	private boolean spotted = false;
	int buyInCost = 0;

	public Airfield(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		setDefense(1);
		id = UnitID.AIRFIELD;
		weight = UnitID.LIGHT;
		buyInCost = nation.getAirfieldCost() / 2;
		nation.unupgradedAirfields+=1;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (engaged || hit > 0){
				spotted = true;
				if(!nation.engagedUnits.contains(this))
					nation.engagedUnits.add(this);
			}
			engaged = false;
			setStart(getStart() - 1);
			if (!nation.isAIControlled()) {
				clickToDropDown();
			}

			if (getStart() < 0) {
				addProduct();
				if (nation.isAIControlled()) {
					if (!upgrading) {
						if (weight == UnitID.LIGHT && buyInCost < nation.coins) {
							upgrade(nation.getAirfieldCost() / 2);
						} else if (weight == UnitID.MEDIUM && buyInCost * 2 < nation.coins) {
							upgrade(nation.getAirfieldCost() / 2);
						} else {
							decideNewProduct();
						}
					}
				}
			}
		}
	}

	/**
	 * Adds whatever product is decided to the game
	 */
	public void addProduct() {
		if (getProductWeight() != null && getProductWeight() != UnitID.NONE) {
			if (getProduct() != UnitID.AIRFIELD) {
				nation.addUnit(new Plane(position, nation, getProductWeight()));
				if (getProductWeight() == UnitID.LIGHT) nation.airSupremacy++;
			} else {
				if (weight == UnitID.MEDIUM) {
					weight = UnitID.HEAVY;
					nation.unupgradedAirfields-=1;
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
	 * Decides what product to manufacture. Only used for AI
	 */
	public void decideNewProduct() {
		if((nation.getCityCost()) >= nation.unitSize()) {
			if (nation.enemyNation.airSupremacy >= nation.airSupremacy) {
				buyUnit(UnitID.PLANE, UnitID.LIGHT, nation.getPlaneCost() / 2 * getDefense() * 0.5, getTime(weight, UnitID.LIGHT));
			} else {
				if (nation.enemyNation.landEngagedSupremacy > nation.landSupremacy && nation.enemyNation.seaEngagedSupremacy > nation.seaSupremacy) {
					buyUnit(UnitID.PLANE, UnitID.MEDIUM, nation.getPlaneCost() * getDefense() * 0.5, getTime(weight, UnitID.MEDIUM));
				} else {
					int smallestDistance = 1310720;
					for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
						Unit tempUnit = nation.enemyNation.getUnit(i);
						if ((tempUnit.id == UnitID.CITY || tempUnit.id == UnitID.FACTORY || tempUnit.id == UnitID.PORT || tempUnit.id == UnitID.AIRFIELD) && !tempUnit.capital) {
							Point tempPoint = tempUnit.getPosition();
							int tempDist = (int) position.getDist(tempPoint);
							if (tempDist < smallestDistance) {
								smallestDistance = tempDist;
							}
						}
					}
					if (smallestDistance < 1310720) buyUnit(UnitID.PLANE, UnitID.HEAVY, nation.getPlaneCost() * 2.5 * getDefense() * 0.5, getTime(weight, UnitID.HEAVY));
				}
			}
		}
	}

	@Override
	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			if (getProductWeight() != UnitID.NONE && getStart() > 1 && nation.name.contains("Sweden")) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 255 << 24);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18, (int) (28.0 * ((maxStart - getStart()) / maxStart)), 2, nation.color);
			}
			r.drawImage((int) position.getX(), (int) position.getY(), 32,Render.getScreenBlend(Render.getColor(weight,nation.color), r.airfield),1, 0);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36,r.cityHit,1, 0);
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
					buyUnit(UnitID.PLANE, UnitID.LIGHT, nation.getCavalryCost() / 2 * getDefense() * 0.5, getTime(weight, UnitID.LIGHT));
				} else if (d.buttonsHovered == 3) {
					buyUnit(UnitID.PLANE, UnitID.MEDIUM, nation.getPlaneCost() * getDefense() * 0.5, getTime(weight, UnitID.MEDIUM));
				} else if (d.buttonsHovered == 4) {
					buyUnit(UnitID.PLANE, UnitID.HEAVY, nation.getPlaneCost() * 2.5 * getDefense() * 0.5, getTime(weight, UnitID.HEAVY));
				}
			} else if (d.getTab() == 1) {
				if (d.buttonsHovered == 2) {
					upgrade(nation.getAirfieldCost() / 2);
				} else if (d.buttonsHovered == 3) {
					nation.unitArray.remove(this);
					d.shouldClose();
					nation.coins += 10;
					nation.setAirfieldCost(nation.getAirfieldCost() / 2);
				}
			}
		} else {
			if (d.buttonsHovered == 2) {
				// This part cancels both upgrades and production. The or operator differenciates between the two
				if (Main.mouse.getX() > d.getPosition().getX() + 90 || getProduct() == UnitID.AIRFIELD) {
					cancelOrder(d);
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
			if (getProduct() == UnitID.NONE) d.drawTab(2,r.resize(r.fighter1,0.75,36,35),r.settings,null,27,25,0, r);
			if (d.getTab() == 0) {
				d.drawIndustry(r, "Fighter", "Attacker", "Bomber", nation.getPlaneCost() / 2 * (getDefense() / 2), nation.getPlaneCost() * (getDefense() / 2), nation.getPlaneCost() * 2.5 * (getDefense() / 2), this);
			} else if (d.getTab() == 1) {
				if (nation.getCoinAmount() >= nation.getAirfieldCost() / 2) {
					d.drawOption("Upgrade (" + nation.getAirfieldCost() / 2 + ")", 2, 32, 5, r);
				} else {
					d.drawOption("Upgrade (" + nation.getAirfieldCost() / 2 + ")", 2, 0, 5, r);
				}
				d.drawOption("Decommision", 3, 32, 5, r);
				r.drawRectBorders((int) d.getPosition().getX(), (int) d.getPosition().getY() + 30 * 4, 180, 30, 180 << 24 | 32 << 16 | 32 << 8 | 32, 13);
			}
		} else {
			d.drawUpgrading(this, r);
		}
	}
}