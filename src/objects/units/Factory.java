package objects.units;

import main.UnitID;
import objects.Nation;
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
		if (((nation.factoryCost-15)/10 & 1) == 0) {
			product = UnitID.ARTILLERY;
		} else {
			product = UnitID.CAVALRY;
		}
	}

	public void tick(double t) {
		if (engaged)
			spotted = true;
		engaged = false;
		if (!(nation.defeated || nation.enemyNation.defeated)) {
			detectHit();
			start--;

			if (start < 0) {
				addProduct();
				decideNewProduct();
			}
		}
	}

	public void addProduct() {
		if (productWeight != null && productWeight != UnitID.NONE) {
			if (product == UnitID.CAVALRY) {
				nation.addUnit(new Cavalry(position, nation, productWeight));
			} else if (product == UnitID.ARTILLERY) {
				nation.addUnit(new Artillery(position, nation, productWeight));
			}
			productWeight = UnitID.NONE;
		}
	}

	public void decideNewProduct() {
		if (product == UnitID.ARTILLERY) {
			if (nation.coins >= nation.artilleryCost * 2) {
				nation.coins -= nation.artilleryCost * 2;
				productWeight = UnitID.HEAVY;
				start = 21600;
				maxStart = start;
			} else if (nation.coins >= nation.artilleryCost) {
				nation.coins -= nation.artilleryCost;
				productWeight = UnitID.MEDIUM;
				start = 10800;
				maxStart = start;
			} else if (nation.coins >= nation.artilleryCost / 2) {
				nation.coins -= nation.artilleryCost / 2;
				productWeight = UnitID.LIGHT;
				start = 7200;
				maxStart = start;
			} else {
				start = 1;
				maxStart = start;
			}
		} else if (product == UnitID.CAVALRY) {
			if (nation.coins >= nation.cavalryCost * 2) {
				// 6 mimnutes
				nation.coins -= nation.cavalryCost * 2;
				productWeight = UnitID.HEAVY;
				start = 21600;
				maxStart = start;
			} else if (nation.coins >= nation.cavalryCost) {
				// 3 minutes
				nation.coins -= nation.cavalryCost;
				productWeight = UnitID.MEDIUM;
				start = 10800;
				maxStart = start;
			} else if (nation.coins >= nation.cavalryCost / 2) {
				// 2 minutes
				nation.coins -= nation.cavalryCost / 2;
				productWeight = UnitID.LIGHT;
				start = 7200;
				maxStart = start;
			} else {
				start = 1;
				maxStart = start;
			}
		} else {
			start = 1;
			maxStart = start;
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || nation.enemyNation.defeated || nation.defeated) {
			if (start > 1) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 0);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18,
						(int) (28.0 * ((maxStart - start) / maxStart)), 2, nation.color);
			}
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.factory, nation.color);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
