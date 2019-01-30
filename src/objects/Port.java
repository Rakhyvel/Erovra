package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Port extends Unit {

	int start = 0;
	float maxStart = 1;
	UnitID productWeight;
	boolean spotted = false;

	public Port(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.PORT;
		defense = 3;
		productWeight = UnitID.NONE;
	}

	public void tick(double t) {
		if (engaged)
			spotted = true;
		disengage();
		detectHit();
		start--;

		if (start < 0) {
			addProduct();
			decideNewProduct();
		}
	}

	public void addProduct() {
		if (productWeight != UnitID.NONE && productWeight != null) {
			nation.addUnit(new Ship(position, nation, productWeight));
			if (productWeight != UnitID.LIGHT)
				nation.seaSupremacy++;
			productWeight = UnitID.NONE;
		}
	}

	public void decideNewProduct() {
		if (nation.enemyNation.seaSupremacy >= nation.seaSupremacy) {
			if (nation.coins >= nation.shipCost * 2) {
				nation.coins -= nation.shipCost * 2;
				productWeight = UnitID.HEAVY;
				start = 21600;
				maxStart = start;
			} else if (nation.coins >= nation.shipCost) {
				nation.coins -= nation.shipCost;
				productWeight = UnitID.MEDIUM;
				start = 10800;
				maxStart = start;
			} else {
				productWeight = UnitID.NONE;
				start = 0;
				maxStart = start;
			}
		} else {
			if (nation.coins >= (nation.shipCost / 4)) {
				int smallestDistance = 524288;
				int unitCount = 0;
				for (int i = 0; i < nation.unitSize(); i++) {
					Unit tempUnit = nation.getUnit(i);
					Point tempPoint = tempUnit.getPosition();
					int tempDist = (int) position.getDist(tempPoint);
					if (tempDist < smallestDistance && ((tempUnit.id == UnitID.CAVALRY)
							|| (tempUnit.id == UnitID.INFANTRY) || (tempUnit.id == UnitID.ARTILLERY))
							&& !tempUnit.engaged && !tempUnit.boarded) {
						unitCount++;
					}
				}
				if (unitCount > 3) {
					nation.coins -= nation.shipCost / 4;
					productWeight = UnitID.LIGHT;
					start = 3000;
					maxStart = start;
				}
			} else {
				productWeight = UnitID.NONE;
				start = 0;
				maxStart = start;
			}
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden")) {
			if (productWeight != UnitID.NONE) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 0);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18,
						(int) (28.0 * ((maxStart - start) / maxStart)), 2, nation.color);
			}
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.port, r.darken(nation.color));
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
