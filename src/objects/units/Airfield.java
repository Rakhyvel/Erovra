package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

public class Airfield extends Unit {

	boolean spotted = false;

	public Airfield(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		defense = 4;
		id = UnitID.AIRFIELD;
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
		if (productWeight != UnitID.NONE && productWeight != null) {
			nation.addUnit(new Plane(position, nation, productWeight));
			if (productWeight == UnitID.LIGHT) nation.airSupremacy++;
			productWeight = UnitID.NONE;
		}
	}

	public void decideNewProduct() {
		if (nation.enemyNation.airSupremacy >= nation.airSupremacy) {
			buyUnit(UnitID.PLANE, UnitID.LIGHT, nation.planeCost / 2, 5400);
		} else {
			if (nation.enemyNation.landSupremacy >= nation.landSupremacy || nation.enemyNation.seaSupremacy >= nation.seaSupremacy) {
				buyUnit(UnitID.PLANE, UnitID.MEDIUM, nation.planeCost, 10800);
			} else {
				buyUnit(UnitID.PLANE, UnitID.HEAVY, nation.planeCost / 2, 7200);
			}
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			if (productWeight != UnitID.NONE) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 0);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18, (int) (28.0 * ((maxStart - start) / maxStart)), 2, nation.color);
			}
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.airfield, nation.color);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
