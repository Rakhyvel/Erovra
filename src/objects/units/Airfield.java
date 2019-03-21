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
	Image airfield;

	public Airfield(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		defense = 2;
		id = UnitID.AIRFIELD;
		airfield = new Image("/res/buildings/airfield.png", 32, 32).getScreenBlend(nation.color);
	}

	@Override
	public void tick(double t) {
		detectHit();
		if(health > 0) {
			if (engaged || hit > 0)
				spotted = true;
			engaged = false;
			setStart(getStart() - 1);
			if (!nation.isAIControlled()) {
				clickToDropDown();
			}
	
			if (getStart() < 0) {
				addProduct();
				if (nation.isAIControlled()) decideNewProduct();
			}
		}
	}

	/**
	 * Adds whatever product is decided to the game
	 */
	public void addProduct() {
		if (getProductWeight() != UnitID.NONE && getProductWeight() != null) {
			nation.addUnit(new Plane(position, nation, getProductWeight()));
			if (getProductWeight() == UnitID.LIGHT) nation.airSupremacy++;
			setProductWeight(UnitID.NONE);
			if (!nation.isAIControlled()) setProduct(UnitID.NONE);
		}
	}

	/**
	 * Decides what product to manufacture. Only used for AI
	 */
	public void decideNewProduct() {
		if (nation.enemyNation.airSupremacy >= nation.airSupremacy) {
			buyUnit(UnitID.PLANE, UnitID.LIGHT, nation.getPlaneCost() / 2, 5400);
		} else {
			if (nation.enemyNation.landSupremacy > nation.landSupremacy && nation.enemyNation.seaSupremacy > nation.seaSupremacy) {
				buyUnit(UnitID.PLANE, UnitID.MEDIUM, nation.getPlaneCost(), 10800);
			} else {
				buyUnit(UnitID.PLANE, UnitID.HEAVY, nation.getPlaneCost() / 2, 7200);
			}
		}
	}

	@Override
	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			if (getProductWeight() != UnitID.NONE && getStart() > 1) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 255 << 24);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18, (int) (28.0 * ((maxStart - getStart()) / maxStart)), 2, nation.color);
			}
			r.drawImage((int) position.getX(), (int) position.getY(), airfield,0);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), r.cityHit,0);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
		if (getProduct() == UnitID.NONE) {
			if (d.buttonsHovered == 1) {
				buyUnit(UnitID.PLANE, UnitID.LIGHT, nation.getCavalryCost() / 2, 5400);
			} else if (d.buttonsHovered == 2) {
				buyUnit(UnitID.PLANE, UnitID.MEDIUM, nation.getPlaneCost(), 10800);
			} else if (d.buttonsHovered == 3) {
				buyUnit(UnitID.PLANE, UnitID.HEAVY, nation.getPlaneCost() / 2, 7200);
			} else if (d.buttonsHovered == 4) {
				nation.unitArray.remove(this);
				d.shouldClose();
				nation.coins += 10;
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
		d.drawIndustry(r, "Fighter", "Attacker", "Bomber", nation.getPlaneCost() / 2, nation.getPlaneCost(), nation.getPlaneCost() / 2, this);
	}

}
