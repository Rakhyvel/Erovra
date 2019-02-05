package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

public class Factory extends Unit {

	boolean spotted = false;

	public Factory(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.FACTORY;
		defense = 3;
		if (((nation.factoryCost - 15) / 10 & 3) == 0) {
			setProduct(UnitID.ARTILLERY);
		} else {
			setProduct(UnitID.CAVALRY);
		}
	}

	public void tick(double t) {
		if (engaged) spotted = true;
		engaged = false;
		detectHit();
		start--;
		if (!nation.isAIControlled()) {
			clickToDropDown();
		}

		if (start < 0) {
			addProduct();
			if (nation.isAIControlled()) decideNewProduct();
		}
	}

	public void addProduct() {
		if (productWeight != null && productWeight != UnitID.NONE) {
			if (getProduct() == UnitID.CAVALRY) {
				nation.addUnit(new Cavalry(position, nation, productWeight));
				nation.landSupremacy++;
			} else if (getProduct() == UnitID.ARTILLERY) {
				nation.addUnit(new Artillery(position, nation, productWeight));
				nation.landSupremacy++;
			}
			productWeight = UnitID.NONE;
		}
	}

	public void decideNewProduct() {
		if (buyUnit(UnitID.ARTILLERY, UnitID.HEAVY, nation.artilleryCost * 2, 21600)) {
			// Heavy artillery
		} else if (buyUnit(UnitID.CAVALRY, UnitID.HEAVY, nation.cavalryCost * 2, 21600)) {
			// Heavy cavalry
		} else if (buyUnit(UnitID.ARTILLERY, UnitID.MEDIUM, nation.artilleryCost, 10800)) {
			// Medium artillery
		} else if (buyUnit(UnitID.CAVALRY, UnitID.MEDIUM, nation.cavalryCost, 10800)) {
			// Medium cavalry
		} else if (buyUnit(UnitID.ARTILLERY, UnitID.LIGHT, nation.artilleryCost/2, 10800)) {
			// Anti Air artillery
		} else if (buyUnit(UnitID.CAVALRY, UnitID.LIGHT, nation.cavalryCost/2, 10800)) {
			// Light Cavalry
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			if (start > 1) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 0);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18, (int) (28.0 * ((maxStart - start) / maxStart)), 2, nation.color);
			}
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.factory, nation.color);
			if (hit > 1 || isSelected()) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
