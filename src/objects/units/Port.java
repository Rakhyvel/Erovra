package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

public class Port extends Unit {

	boolean spotted = false;

	public Port(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.PORT;
		defense = 5;
		productWeight = UnitID.NONE;
	}

	public void tick(double t) {
		if (engaged) spotted = true;
		disengage();
		if (!(nation.defeated || nation.enemyNation.defeated)) {
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
	}

	public void addProduct() {
		if (productWeight != UnitID.NONE && productWeight != null) {
			nation.addUnit(new Ship(position, nation, productWeight));
			if (productWeight != UnitID.LIGHT) nation.seaSupremacy++;
			productWeight = UnitID.NONE;
		}
	}

	public void decideNewProduct() {
		if (nation.enemyNation.seaSupremacy >= nation.seaSupremacy) {
			if (nation.coins >= (nation.shipCost / 4)) {
				int smallestDistance = 524288;
				int unitCount = 0;
				for (int i = 0; i < nation.unitSize(); i++) {
					Unit tempUnit = nation.getUnit(i);
					Point tempPoint = tempUnit.getPosition();
					int tempDist = (int) position.getDist(tempPoint);
					if (tempDist < smallestDistance && ((tempUnit.id == UnitID.CAVALRY) || (tempUnit.id == UnitID.INFANTRY) || (tempUnit.id == UnitID.ARTILLERY)) && !tempUnit.engaged && !tempUnit.isBoarded()) {
						unitCount++;
					}
				}
				if (unitCount > 3) {
					buyUnit(UnitID.SHIP, UnitID.LIGHT, nation.shipCost / 4, 3000);
				}
			}
		} else {
			if (nation.enemyNation.landSupremacy >= nation.landSupremacy || nation.enemyNation.airSupremacy >= nation.airSupremacy) {
				buyUnit(UnitID.SHIP, UnitID.MEDIUM, nation.shipCost, 10800);
			} else {
				buyUnit(UnitID.SHIP, UnitID.HEAVY, nation.shipCost * 2, 10800);
			}
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			if (productWeight != UnitID.NONE) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 0);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18, (int) (28.0 * ((maxStart - start) / maxStart)), 2, nation.color);
			}
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.port, r.darken(nation.color));
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
