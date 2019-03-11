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

	public Factory(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.FACTORY;
		defense = 3;
		if (nation.getFactoryCost() == 60 && nation.isAIControlled()) {
			cavalry = false;
		}
		dropDownHeight = 150;
	}

	@Override
	public void tick(double t) {
		if (engaged || hit > 0)
			spotted = true;
		engaged = false;
		detectHit();
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

	/**
	 * Adds manufactured unit to the game
	 */
	@Override
	public void addProduct() {
		if (getProductWeight() != null && getProductWeight() != UnitID.NONE) {
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
		}
	}

	/**
	 * Decides which product to produce
	 */
	@Override
	public void decideNewProduct() {
		if (cavalry) {
			if (buyUnit(UnitID.CAVALRY, UnitID.HEAVY, nation.getCavalryCost() * 2, 21600)) {
				// Heavy cavalry
			} else if (buyUnit(UnitID.CAVALRY, UnitID.MEDIUM, nation.getCavalryCost(), 10800)) {
				// Medium cavalry
			} else if (buyUnit(UnitID.CAVALRY, UnitID.LIGHT, nation.getCavalryCost() / 2, 7200)) {
				// Light Cavalry
			}

		} else {
			if (buyUnit(UnitID.ARTILLERY, UnitID.HEAVY, nation.getArtilleryCost() * 2, 21600)) {
				// Heavy artillery
			} else if (buyUnit(UnitID.ARTILLERY, UnitID.MEDIUM, nation.getArtilleryCost(), 10800)) {
				// Medium artillery
			} else if (buyUnit(UnitID.ARTILLERY, UnitID.LIGHT, nation.getArtilleryCost() / 2, 7200)) {
				// Anti Air artillery
			}
		}
	}

	@Override
	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) {
			if (getProductWeight() != UnitID.NONE && getStart() > 1) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 0);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18,
						(int) (28.0 * ((maxStart - getStart()) / maxStart)), 2, nation.color);
			}
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.factory, nation.color);
			if (hit > 1 || isSelected()) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
		if (getProduct() == UnitID.NONE) {
			if (cavalry) {
				if (d.buttonsHovered == 1) {
					buyUnit(UnitID.CAVALRY, UnitID.LIGHT, nation.getCavalryCost() / 2, 7200);
				} else if (d.buttonsHovered == 2) {
					buyUnit(UnitID.CAVALRY, UnitID.MEDIUM, nation.getCavalryCost(), 10800);
				} else if (d.buttonsHovered == 3) {
					buyUnit(UnitID.CAVALRY, UnitID.HEAVY, nation.getCavalryCost() * 2, 21600);
				} else if (d.buttonsHovered == 4) {
					cavalry = !cavalry;
				}
			} else {
				if (d.buttonsHovered == 1) {
					buyUnit(UnitID.ARTILLERY, UnitID.LIGHT, nation.getArtilleryCost() / 2, 3600);
				} else if (d.buttonsHovered == 2) {
					buyUnit(UnitID.ARTILLERY, UnitID.MEDIUM, nation.getArtilleryCost(), 10800);
				} else if (d.buttonsHovered == 3) {
					buyUnit(UnitID.ARTILLERY, UnitID.HEAVY, nation.getArtilleryCost() * 2, 21600);
				} else if (d.buttonsHovered == 4) {
					cavalry = !cavalry;
				}
			}
		}
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		dropDownHeight = getDropDownHeight();
		d.setPosition(position);
		if (cavalry) {
			d.drawIndustry(r, "Light tank", "Medium tank", "Heavy tank", nation.getCavalryCost() / 2,
					nation.getCavalryCost(), nation.getCavalryCost() * 2, this);

			if (getProduct() == UnitID.NONE) {
				d.drawOption("[ Cavalry >", 4, 0.5f, r);
			}
		} else {
			d.drawIndustry(r, "Anti air", "Mortar", "Howitzer", nation.getArtilleryCost() / 2,
					nation.getArtilleryCost(), nation.getArtilleryCost() * 2, this);

			if (getProduct() == UnitID.NONE) {
				d.drawOption("< Artillery ]", 4, 0.5f, r);
			}
		}
	}
}
