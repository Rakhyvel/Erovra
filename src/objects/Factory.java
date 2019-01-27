package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Factory extends Unit {

	int start = 0;
	float maxStart = 1;
	UnitID product;
	UnitID productWeight;

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
			maxStart = start;
		} else if (nation.coins >= nation.artilleryCost) {
			nation.coins -= nation.artilleryCost;
			product = UnitID.ARTILLERY;
			productWeight = UnitID.MEDIUM;
			start = 36000;
			maxStart = start;
		} else if (nation.coins >= nation.artilleryCost / 2) {
			nation.coins -= nation.artilleryCost / 2;
			product = UnitID.ARTILLERY;
			productWeight = UnitID.LIGHT;
			start = 18000;
			maxStart = start;
		} else if (nation.coins >= nation.cavalryCost * 2) {
			nation.coins -= nation.cavalryCost * 2;
			product = UnitID.CAVALRY;
			productWeight = UnitID.HEAVY;
			start = 72000;
			maxStart = start;
		} else if (nation.coins >= nation.cavalryCost) {
			nation.coins -= nation.cavalryCost;
			product = UnitID.CAVALRY;
			productWeight = UnitID.MEDIUM;
			start = 36000;
			maxStart = start;
		} else if (nation.coins >= nation.cavalryCost / 2) {
			nation.coins -= nation.cavalryCost / 2;
			product = UnitID.CAVALRY;
			productWeight = UnitID.LIGHT;
			start = 18000;
			maxStart = start;
		} else {
			product = UnitID.NONE;
			start = 1;
			maxStart = start;
		}
	}

	public void render(Render r) {
		r.drawRect((int)position.getX()-16, (int)position.getY()-21, (int)(32.0*((maxStart-start)/maxStart)), 4, nation.color);
		if ((nation.name.contains("Russia") && engaged) || nation.name.contains("Sweden")) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.factory, nation.color);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
