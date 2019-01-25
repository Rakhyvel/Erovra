package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Factory extends Unit {

	int start = 300;
	UnitID product = UnitID.CAVALRY;
	UnitID productWeight = UnitID.MEDIUM;

	public Factory(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.FACTORY;
		defense = 3;
		product = UnitID.CAVALRY;
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
		if (product == UnitID.CAVALRY) {
			nation.addUnit(new Cavalry(position, nation, productWeight));
		} else if (product == UnitID.ARTILLERY) {
			nation.addUnit(new Artillery(position, nation, productWeight));
		}
	}

	public void decideNewProduct() {
		if (nation.coins >= nation.artilleryCost * 2) {
			nation.coins -= nation.artilleryCost * 2;
			product = UnitID.ARTILLERY;
			productWeight = UnitID.HEAVY;
			start = 72000;
		} else if (nation.coins >= nation.artilleryCost) {
			nation.coins -= nation.artilleryCost;
			product = UnitID.ARTILLERY;
			productWeight = UnitID.MEDIUM;
			start = 36000;
		} else if (nation.coins >= nation.artilleryCost / 2) {
			nation.coins -= nation.artilleryCost / 2;
			product = UnitID.ARTILLERY;
			productWeight = UnitID.LIGHT;
			start = 18000;
		} else if (nation.coins >= nation.cavalryCost * 2) {
			nation.coins -= nation.cavalryCost * 2;
			product = UnitID.CAVALRY;
			productWeight = UnitID.HEAVY;
			start = 72000;
		} else if (nation.coins >= nation.cavalryCost) {
			nation.coins -= nation.cavalryCost;
			product = UnitID.CAVALRY;
			productWeight = UnitID.MEDIUM;
			start = 36000;
		} else if (nation.coins >= nation.cavalryCost / 2) {
			nation.coins -= nation.cavalryCost / 2;
			product = UnitID.CAVALRY;
			productWeight = UnitID.LIGHT;
			start = 18000;
		} else {
			product = UnitID.NONE;
		}
	}

	public void render(Render r) {
		if ((nation.name.contains("Russia") && engaged) || nation.name.contains("Sweden")) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.factory, nation.color);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
