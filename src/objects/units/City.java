package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for City units
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class City extends Industry {

	private boolean spotted = false;
	int buyInCost = 0;
	boolean recruiting = true;

	public City(Point position, Nation nation, int founded) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.CITY;
		setDefense(1);
		weight = UnitID.LIGHT;
		buyInCost = nation.getCityCost() / 2;
		nation.unupgradedCities += 1;
		setStart(5999);
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (engaged || hit > 0) {
				spotted = true;
				if (!nation.engagedUnits.contains(this)) nation.engagedUnits.add(this);
			}
			engaged = false;
			if ((Main.ticks - born) % (640 / getDefense()) == 0 && !upgrading) {
				nation.addCoin(position);
			}
			if (start < 0 && capital) {
				nation.addUnit(new Infantry(position, nation));
				setStart(6000);
				nation.landSupremacy++;
			}
			if (!nation.isAIControlled()) {
				clickToDropDown();
			}
			if (getStart() < 0) {
				addProduct();
				if (nation.isAIControlled()) if (!upgrading) {
					if (weight == UnitID.LIGHT && buyInCost < nation.coins) {
						upgrade(nation.getCityCost() / 2);
					} else if (weight == UnitID.MEDIUM && buyInCost * 2 < nation.coins) {
						upgrade(nation.getCityCost() / 2);
					}
				}
			} else if (recruiting) {
				setStart(getStart() - 1);
			}
			if (upgrading && getStart() < 0) {
				if (weight == UnitID.MEDIUM) {
					weight = UnitID.HEAVY;
					setDefense(4);
					nation.unupgradedCities -= 1;
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

	@Override
	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			if (capital) {
				if(recruiting){
					r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 255 << 24);
					r.drawRect((int) position.getX() - 14, (int) position.getY() - 18, (int) (28.0 * (6000 - getStart()) / 6000), 2, nation.color);
				}
				r.drawImage((int) position.getX(), (int) position.getY(), 32, Render.getScreenBlend(nation.color, r.capital), 1, 0);
			} else {
				if (getProduct() != UnitID.NONE && getStart() > 1) {
					r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 255 << 24);
					r.drawRect((int) position.getX() - 14, (int) position.getY() - 18, (int) (28.0 * ((maxStart - getStart()) / maxStart)), 2, nation.color);
				}
				r.drawImage((int) position.getX(), (int) position.getY(), 32, Render.getScreenBlend(Render.getColor(weight, nation.color), r.city), 1, 0);
			}
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36, r.cityHit, 1, 0);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
		if (getProduct() == UnitID.NONE) {
			if (d.buttonsHovered == 1 && !capital) {
				upgrade(nation.getCityCost() / 2);
			} else if (d.buttonsHovered == 2) {
				if (!capital) {
					nation.unitArray.remove(this);
					d.shouldClose();
					nation.coins += 10;
					nation.setCityCost(nation.getCityCost() / 2);
				} else {
					recruiting = !recruiting;
				}
			}
		} else {
			if (d.buttonsHovered == 2 && !capital) {
				setProductWeight(UnitID.NONE);
				setProduct(UnitID.NONE);
				nation.coins += refund;
				upgrading = false;
			}
		}
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		dropDownHeight = getDropDownHeight();
		d.setPosition(position);
		if (!capital) {
			if (!upgrading) {
				if (nation.getCoinAmount() >= nation.getCityCost() / 2) {
					d.drawOption("Upgrade (" + nation.getCityCost() / 2 + ")", 1, 32, 5, r);
				} else {
					d.drawOption("Upgrade (" + nation.getCityCost() / 2 + ")", 1, 0, 5, r);
				}
				d.drawOption("Decommision", 2, 32, 13, r);
			} else {
				d.drawUpgrading(this, r);
			}
		} else {
			int minutes = 0, seconds = 0;
			String product = "Recruits";
			if ((6000 - getStart() % 6000) > 0) {
				minutes = (getStart() % 6000) / 3600;
				seconds = ((getStart() % 6000) / 60) - minutes * 60;
				if (minutes >= 1) {
					product += " " + minutes + "m,";
				}
				product += " " + seconds + "s";
			}
			r.drawRectBorders((int) d.getPosition().getX(), (int) d.getPosition().getY() + 30, 180, 30, 180 << 24 | 128 << 16 | 128 << 8 | 128, 5);
			if (recruiting) {
				d.drawOption("Pause recruitment", 2, 32, 13, r);
			} else {
				d.drawOption("Resume recruitment", 2, 32, 13, r);
			}
			r.drawString(product, (int) d.getPosition().getX() + 7, (int) d.getPosition().getY() + 44, r.font16, 255 << 24 | 250 << 16 | 250 << 8 | 250, false);
			if (!recruiting) {
				r.drawRect((int) d.getPosition().getX() + 7, (int) d.getPosition().getY() + 44, 166, 2, 255 << 24 | 230 << 16 | 25 << 8 | 25);
			}
		}
	}

	@Override
	public void addProduct() {}

	@Override
	public void decideNewProduct() {}

}
