package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Airfield extends Unit {

	int start = 0;
	float maxStart = 1;
	UnitID productWeight;

	public Airfield(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
	}

	public void tick(double t) {
		detectHit();
		start--;

		if (start < 0) {
			addProduct();
			decideNewProduct();
		}
	}

	public void addProduct() {
		if (productWeight != UnitID.NONE) nation.addUnit(new Plane(position, nation, productWeight));
	}

	public void decideNewProduct() {
		if (nation.coins >= nation.shipCost * 2) {
			nation.coins -= nation.shipCost * 2;
			productWeight = UnitID.MEDIUM;
			start = 72000;
			maxStart = start;
		} else if (nation.coins >= nation.shipCost) {
			nation.coins -= nation.shipCost;
			productWeight = UnitID.MEDIUM;
			start = 36000;
			maxStart = start;
		} else if (nation.coins >= nation.shipCost / 2) {
			nation.coins -= nation.shipCost / 2;
			productWeight = UnitID.LIGHT;
			start = 18000;
			maxStart = start;
		} else {
			productWeight = UnitID.NONE;
			start = 1;
			maxStart = start;
		}
	}

	public void render(Render r) {
		r.drawRect((int) position.getX() - 16, (int) position.getY() - 21, (int) (32.0 * ((maxStart - start) / maxStart)), 4, nation.color);
		if ((nation.name.contains("Russia") && engaged) || nation.name.contains("Sweden")) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.airfield, r.darken(nation.color));
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
