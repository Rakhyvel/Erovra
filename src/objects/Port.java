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
		defense = 9;
		productWeight = UnitID.NONE;
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
		if (productWeight != UnitID.NONE && productWeight != null) nation.addUnit(new Ship(position, nation, productWeight));
	}

	public void decideNewProduct() {
		if (nation.coins >= nation.shipCost * 2) {
			nation.coins -= nation.shipCost * 2;
			productWeight = UnitID.HEAVY;
			start = 6000;
			maxStart = start;
		} else if (nation.coins >= nation.shipCost) {
			nation.coins -= nation.shipCost;
			productWeight = UnitID.MEDIUM;
			start = 5000;
			maxStart = start;
		} else if (nation.coins >= 5) {
			nation.coins -= 5;
			productWeight = UnitID.LIGHT;
			start = 3000;
			maxStart = start;
		} else {
			productWeight = UnitID.NONE;
			start = 0;
			maxStart = start;
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden")) {
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
