package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Factory extends Unit {

	int start = 0;
	float maxStart = 1;
	UnitID product;
	UnitID productWeight;
	boolean spotted = false;

	public Factory(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.FACTORY;
		defense = 3;
		product = UnitID.CAVALRY;
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
		if(productWeight != null && productWeight != UnitID.NONE){
			if (product == UnitID.CAVALRY) {
				nation.addUnit(new Cavalry(position, nation, productWeight));
			} else if (product == UnitID.ARTILLERY) {
				nation.addUnit(new Artillery(position, nation, productWeight));
			}
		}
	}

	public void decideNewProduct() {
		if (nation.coins >= nation.artilleryCost * 2) {
			nation.coins -= nation.artilleryCost * 2;
			product = UnitID.ARTILLERY;
			productWeight = UnitID.HEAVY;
			start = 28000;
			maxStart = start;
		} else if (nation.coins >= nation.artilleryCost) {
			nation.coins -= nation.artilleryCost;
			product = UnitID.ARTILLERY;
			productWeight = UnitID.MEDIUM;
			start = 12000;
			maxStart = start;
		} else if (nation.coins >= nation.artilleryCost / 2) {
			nation.coins -= nation.artilleryCost / 2;
			product = UnitID.ARTILLERY;
			productWeight = UnitID.LIGHT;
			start = 6000;
			maxStart = start;
		} else if (nation.coins >= nation.cavalryCost * 2) {
			//17 mimnutes
			nation.coins -= nation.cavalryCost * 2;
			product = UnitID.CAVALRY;
			productWeight = UnitID.HEAVY;
			start = 14000;
			maxStart = start;
		} else if (nation.coins >= nation.cavalryCost) {
			//9 minutes
			nation.coins -= nation.cavalryCost;
			product = UnitID.CAVALRY;
			productWeight = UnitID.MEDIUM;
			start = 6000;
			maxStart = start;
		} else if (nation.coins >= nation.cavalryCost / 2) {
			//5 minutes
			nation.coins -= nation.cavalryCost / 2;
			product = UnitID.CAVALRY;
			productWeight = UnitID.LIGHT;
			start = 5000;
			maxStart = start;
		} else {
			product = UnitID.NONE;
			start = 1;
			maxStart = start;
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden")) {
			if(product != UnitID.NONE){
				r.drawRect((int)position.getX()-16, (int)position.getY()-20, 32, 6, 0);
				r.drawRect((int)position.getX()-14, (int)position.getY()-18, (int)(28.0*((maxStart-start)/maxStart)), 2, nation.color);
			}
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.factory, nation.color);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
