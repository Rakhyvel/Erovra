package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Port extends Unit {
	int start = 300;

	public Port(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.PORT;
		defense = 5;
	}

	public void tick(double t) {
		detectHit();
		start--;
		if(start <= 0) {
			if (nation.coins >= nation.shipCost*2) {
				nation.coins -= nation.shipCost*2;
				nation.addUnit(new Ship(position,nation, UnitID.HEAVY));
				start = 5000;
			} else if (nation.coins >= nation.shipCost) {
				nation.coins -= nation.shipCost;
				nation.addUnit(new Ship(position,nation, UnitID.MEDIUM));
				start = 5000;
			} else if (nation.coins >= nation.shipCost/2) {
				nation.coins -= nation.shipCost/2;
				nation.addUnit(new Ship(position,nation, UnitID.LIGHT));
				start = 5000;
			}
		}
	}

	public void render(Render r) {
		if ((nation.name.contains("Russia") && engaged) || nation.name.contains("Sweden")) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.port, r.darken(nation.color));
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
