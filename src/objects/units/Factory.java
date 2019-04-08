package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for factory units
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class Factory extends Industry {

	private boolean spotted = false;
	private boolean cavalry = true;
	int buyInCost = 0;

	public Factory(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.FACTORY;
		weight = UnitID.LIGHT;
		setDefense(1);
		if (nation.getFactoryCost() == 60 && nation.isAIControlled()) {
			cavalry = false;
		}
		dropDownHeight = 150;
		buyInCost = nation.getFactoryCost() / 2;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (engaged || hit > 0)
				spotted = true;
			engaged = false;
			if (!nation.isAIControlled()) {
				clickToDropDown();
			}

			if (getStart() < 0) {
				addProduct();
				if (nation.isAIControlled())
					if (!upgrading) {
						if (weight == UnitID.LIGHT && buyInCost < nation.getFactoryCost()) {
							upgrade(nation.getFactoryCost() / 2);
						} else if (weight == UnitID.MEDIUM && buyInCost * 2 < nation.getFactoryCost()) {
							upgrade(nation.getFactoryCost() / 2);
						} else {
							decideNewProduct();
						}
					}
			} else {
				setStart(getStart() - 1);
			}
		}
	}

	/**
	 * Adds manufactured unit to the game
	 */
	@Override
	public void addProduct() {
		if (getProductWeight() != null && getProductWeight() != UnitID.NONE) {
			if (getProduct() != UnitID.FACTORY) {
				if (getProduct() == UnitID.CAVALRY) {
					nation.addUnit(new Cavalry(position, nation, getProductWeight()));
					nation.landSupremacy++;
				} else if (getProduct() == UnitID.ARTILLERY) {
					nation.addUnit(new Artillery(position, nation, getProductWeight()));
					nation.landSupremacy++;
				}
				setProductWeight(UnitID.NONE);
				if (!nation.isAIControlled()) {
					setProduct(UnitID.NONE);
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
				if (!nation.isAIControlled()) {
					setProduct(UnitID.NONE);
				}
			}
		}
	}

	/**
	 * Decides which product to produce
	 */
	@Override
	public void decideNewProduct() {
		if(nation.airSupremacy > nation.enemyNation.airSupremacy && nation.seaSupremacy >= nation.enemyNation.seaSupremacy){
			if (cavalry) {
				if (buyUnit(UnitID.CAVALRY, UnitID.HEAVY, nation.getCavalryCost() * 2 * getDefense() * 0.5,
						getTime(weight,UnitID.LIGHT))) {
					// Heavy cavalry
				} else if (buyUnit(UnitID.CAVALRY, UnitID.MEDIUM, nation.getCavalryCost() * getDefense() * 0.5,
						getTime(weight,UnitID.MEDIUM))) {
					// Medium cavalry
				} else if (buyUnit(UnitID.CAVALRY, UnitID.LIGHT, nation.getCavalryCost() / 2 * getDefense() * 0.5,
						getTime(weight,UnitID.HEAVY))) {
					// Light Cavalry
				}
	
			} else {
				if (buyUnit(UnitID.ARTILLERY, UnitID.HEAVY, nation.getArtilleryCost() * 2 * getDefense() * 0.5,
						getTime(weight,UnitID.LIGHT))) {
					// Heavy artillery
				} else if (buyUnit(UnitID.ARTILLERY, UnitID.MEDIUM, nation.getArtilleryCost() * getDefense() * 0.5,
						getTime(weight,UnitID.MEDIUM))) {
					// Medium artillery
				} else if (buyUnit(UnitID.ARTILLERY, UnitID.LIGHT, nation.getArtilleryCost() / 2 * getDefense() * 0.5,
						getTime(weight,UnitID.HEAVY))) {
					// Anti Air artillery
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
			r.drawImage((int) position.getX(), (int) position.getY(), 32, Render.getScreenBlend(Render.getColor(weight,nation.color),r.factory),1,
					0);
			if (hit > 1 || isSelected()) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36,r.cityHit,1, 0);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
		if (getProduct() == UnitID.NONE) {
			if (d.buttonsHovered == 1) {
				d.selectTabs(3);
				if (d.getTab() == 0) {
					cavalry = true;
				} else if (d.getTab() == 1) {
					cavalry = false;
				}
			}
			if (d.getTab() == 0) {
				if (d.buttonsHovered == 2) {
					buyUnit(UnitID.CAVALRY, UnitID.LIGHT, (nation.getCavalryCost() / 2) * (getDefense() / 2),
							getTime(weight,UnitID.LIGHT));
				} else if (d.buttonsHovered == 3) {
					buyUnit(UnitID.CAVALRY, UnitID.MEDIUM, nation.getCavalryCost() * getDefense() * 0.5,
							getTime(weight,UnitID.MEDIUM));
				} else if (d.buttonsHovered == 4) {
					buyUnit(UnitID.CAVALRY, UnitID.HEAVY, nation.getCavalryCost() * 2 * getDefense() * 0.5,
							getTime(weight,UnitID.HEAVY));
				}
			} else if (d.getTab() == 1) {
				if (d.buttonsHovered == 2) {
					buyUnit(UnitID.ARTILLERY, UnitID.LIGHT, nation.getArtilleryCost() / 2 * getDefense() * 0.5,
							getTime(weight,UnitID.LIGHT));
				} else if (d.buttonsHovered == 3) {
					buyUnit(UnitID.ARTILLERY, UnitID.MEDIUM, nation.getArtilleryCost() * getDefense() * 0.5,
							getTime(weight,UnitID.MEDIUM));
				} else if (d.buttonsHovered == 4) {
					buyUnit(UnitID.ARTILLERY, UnitID.HEAVY, nation.getArtilleryCost() * 2 * getDefense() * 0.5,
							getTime(weight,UnitID.HEAVY));
				}
			} else if (d.getTab() == 2) {
				if (d.buttonsHovered == 2) {
					// Upgrading
					if (nation.getCoinAmount() >= nation.getFactoryCost() / 2) {
						upgrade(nation.getFactoryCost() / 2);
					} else {
						Main.world.errorMessage.showErrorMessage("Insufficient funds!");
					}
				} else if (d.buttonsHovered == 3) {
					// Decomission
					nation.unitArray.remove(this);
					d.shouldClose();
					nation.coins += 10;
					nation.setFactoryCost(nation.getFactoryCost() / 2);
				}
			}
		} else {
			if (d.buttonsHovered == 2) {
				setProductWeight(UnitID.NONE);
				setProduct(UnitID.NONE);
				nation.coins += refund;
				upgrading = false;
				d.setTab(0);
				if (d.getTab() == 0) {
					cavalry = true;
				} else if (d.getTab() == 1) {
					cavalry = false;
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
				d.drawTab(3, Render.cavalry, Render.artillery, r.settings,32,32,25, r);
			if (d.getTab() == 2) {
				if (nation.getCoinAmount() >= nation.getFactoryCost() / 2) {
					d.drawOption("Upgrade (" + nation.getFactoryCost() / 2 + ")", 2, 32, 5, r);
				} else {
					d.drawOption("Upgrade (" + nation.getFactoryCost() / 2 + ")", 2, 0, 5, r);
				}
				d.drawOption("Decommision", 3, 32, 5, r);
				r.drawRectBorders((int) d.getPosition().getX(), (int) d.getPosition().getY() + 30 * 4, 180, 30,
						180 << 24 | 32 << 16 | 32 << 8 | 32, 13);
			} else {
				if (d.getTab() == 0) {
					d.drawIndustry(r, "Light tank", "Medium tank", "Heavy tank",
							(nation.getCavalryCost() / 2) * (getDefense() / 2),
							nation.getCavalryCost() * (getDefense() / 2),
							(nation.getCavalryCost() * 2) * (getDefense() / 2), this);
				} else if(d.getTab() == 1){
					d.drawIndustry(r, "Anti air", "Mortar", "Howitzer",
							nation.getArtilleryCost() / 2 * (getDefense() / 2),
							nation.getArtilleryCost()     * (getDefense() / 2),
							nation.getArtilleryCost() * 2 * (getDefense() / 2), this);
				}
			}
		} else {
			d.drawUpgrading(this, r);
		}
	}
}
