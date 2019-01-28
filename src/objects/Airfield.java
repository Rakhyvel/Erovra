package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Airfield extends Unit {

	int start = 0;
	float maxStart = 1;
	UnitID productWeight;
	boolean spotted = false;

	public Airfield(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		defense = 2;
		id = UnitID.AIRFIELD;
	}

	public void tick(double t) {
		if(engaged) spotted = true;
		engaged = false;
		detectHit();
		start--;

		if (start < 0) {
			addProduct();
			decideNewProduct();
		}
	}

	public void addProduct() {
		if (productWeight != UnitID.NONE && productWeight != null) nation.addUnit(new Plane(position, nation, productWeight));
	}

	public void decideNewProduct() {
		if (nation.coins >= nation.planeCost * 2) {
			nation.coins -= nation.planeCost * 2;
			productWeight = UnitID.MEDIUM;
			start = 14000;
			maxStart = start;
		} else if (nation.coins >= nation.planeCost) {
			nation.coins -= nation.planeCost;
			productWeight = UnitID.HEAVY;
			start = 6000;
			maxStart = start;
		} else if (nation.coins >= nation.planeCost * 0.75) {
			nation.coins -= nation.planeCost * 0.75;
			productWeight = UnitID.LIGHT;
			start = 5000;
			maxStart = start;
		} else {
			productWeight = UnitID.NONE;
			start = 1;
			maxStart = start;
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden")) {
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
