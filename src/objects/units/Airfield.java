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
 * Handles the airfield logic and rendering, decides what planes should be made
 * when unit's nation is AI controlled.
 * 
 * @author Rakhyvel
 * @see Unit
 *
 */
public class Airfield extends Industry {

	private boolean spotted = false;
	private static Image airfield = new Image("/res/buildings/airfield.png", 32, 32);
	private static Image[] icons = { new Image("/res/air/fighter.png", 36, 35).resize(0.75f),
			new Image("/res/target.png", 32, 32).resize(0.75f) };

	public Airfield(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		defense = 2;
		id = UnitID.AIRFIELD;
		airfield = airfield.getScreenBlend(nation.color);
		weight = UnitID.LIGHT;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (engaged || hit > 0)
				spotted = true;
			engaged = false;
			setStart(getStart() - 1);
			if (!nation.isAIControlled()) {
				clickToDropDown();
			}

			if (getStart() < 0) {
				addProduct();
				if (nation.isAIControlled())
					decideNewProduct();
			}
		}
	}

	/**
	 * Adds whatever product is decided to the game
	 */
	public void addProduct() {
		if (getProductWeight() != UnitID.NONE && getProductWeight() != null) {
			if (getProduct() != UnitID.AIRFIELD) {
				nation.addUnit(new Plane(position, nation, getProductWeight()));
				if (getProductWeight() == UnitID.LIGHT)
					nation.airSupremacy++;
			} else {
				if (weight == UnitID.MEDIUM) {
					weight = UnitID.HEAVY;
					defense = 4;
				} else if (weight == UnitID.LIGHT) {
					weight = UnitID.MEDIUM;
					defense = 2;
				}
				upgrading = false;
			}
		}
		setProductWeight(UnitID.NONE);
		setProduct(UnitID.NONE);
	}

	/**
	 * Decides what product to manufacture. Only used for AI
	 */
	public void decideNewProduct() {
		if (nation.enemyNation.airSupremacy >= nation.airSupremacy) {
			buyUnit(UnitID.PLANE, UnitID.LIGHT, nation.getPlaneCost() / 2 * defense, 3200 / defense);
		} else {
			if (nation.enemyNation.landSupremacy > nation.landSupremacy
					&& nation.enemyNation.seaSupremacy > nation.seaSupremacy) {
				buyUnit(UnitID.PLANE, UnitID.MEDIUM, nation.getPlaneCost() * defense, 7200 / defense);
			} else {
				buyUnit(UnitID.PLANE, UnitID.HEAVY, nation.getPlaneCost() / 2 * defense, 3200 / defense);
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
			r.drawImage((int) position.getX(), (int) position.getY(), airfield, 0);
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
					buyUnit(UnitID.PLANE, UnitID.LIGHT, nation.getCavalryCost() / 2 * defense, 5400 / defense);
				} else if (d.buttonsHovered == 3) {
					buyUnit(UnitID.PLANE, UnitID.MEDIUM, nation.getPlaneCost() * defense, 10800 / defense);
				} else if (d.buttonsHovered == 4) {
					buyUnit(UnitID.PLANE, UnitID.HEAVY, nation.getPlaneCost() / 2 * defense, 7200 / defense);
				}
			} else if (d.getTab() == 1) {
				if (d.buttonsHovered == 2) {
					if (nation.getCoinAmount() >= nation.getAirfieldCost() / 2) {
						upgrade(nation.getAirfieldCost() / 2);
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
			if (d.buttonsHovered == 3) {
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
				d.drawIndustry(r, "Fighter", "Attacker", "Bomber", nation.getPlaneCost() / 2 * defense,
						nation.getPlaneCost() * defense, nation.getPlaneCost() / 2 * defense, this);
			} else if (d.getTab() == 1) {
				if (nation.getCoinAmount() >= nation.getPortCost() / 2) {
					d.drawOption("Upgrade (" + nation.getAirfieldCost() / 2 + ")", 2, 32, 5, r);
				} else {
					d.drawOption("Upgrade (" + nation.getAirfieldCost() / 2 + ")", 2, 0, 5, r);
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