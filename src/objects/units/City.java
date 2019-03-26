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
 * Handles the logic and rendering for City units
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class City extends Industry {

	private boolean spotted = false;
	private static Image city = new Image("/res/buildings/city.png", 32, 32);
	private static Image capitalImg = new Image("/res/buildings/capital.png", 32, 32);

	public City(Point position, Nation nation, int founded) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.CITY;
		defense = 1;
		weight = UnitID.LIGHT;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (engaged || hit > 0)
				spotted = true;
			engaged = false;
			if ((Main.ticks - born) % 320 == 0) {
				nation.addCoin(position);
			}
			if (Main.ticks % 6000 == 0 && capital) {
				nation.addUnit(new Infantry(position, nation));
				nation.setLandSupremacy(1);
			}
			if (!nation.isAIControlled()) {
				clickToDropDown();
			}
			setStart(getStart() - 1);
			if(upgrading && getStart() < 0) {
				if (weight == UnitID.MEDIUM) {
					weight = UnitID.HEAVY;
					defense = 4;
				} else if (weight == UnitID.LIGHT) {
					weight = UnitID.MEDIUM;
					defense = 2;
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
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) {
			if (capital) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 255 << 24);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18,
						(int) (28.0 * (Main.ticks % 6000) / 6000), 2, nation.color);
				r.drawImage((int) position.getX(), (int) position.getY(), Render.getWeighted(capitalImg, UnitID.MEDIUM, nation.color), 0);
			} else {
				r.drawImage((int) position.getX(), (int) position.getY(), Render.getWeighted(city, weight, nation.color), 0);
			}
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), r.cityHit, 0);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
		if (getProduct() == UnitID.NONE) {
			if (d.buttonsHovered == 1) {
				upgrade();
			} else if (d.buttonsHovered == 2) {
				nation.unitArray.remove(this);
				d.shouldClose();
				nation.coins += 10;
				nation.setCityCost(nation.getCityCost() / 2);
			}
		} else {
			if (d.buttonsHovered == 2) {
				setProductWeight(UnitID.NONE);
				setProduct(UnitID.NONE);
				nation.coins += 10;
			}
		}
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		dropDownHeight = getDropDownHeight();
		d.setPosition(position);
		if(!capital) {
			if(!upgrading) {
				d.drawOption("Upgrade", 1, 32, 5, r);
				d.drawOption("Decommision", 2, 32, 5, r);
				r.drawRectBorders((int) d.getPosition().getX(), (int) d.getPosition().getY() + 30 * 3, 180, 30, 180 << 24 | 32 << 16 | 32 << 8 | 32, 13);
			} else {
				d.drawUpgrading(this,r);
			}
		}
	}

	@Override
	public void addProduct() {
	}

	@Override
	public void decideNewProduct() {
	}

}
