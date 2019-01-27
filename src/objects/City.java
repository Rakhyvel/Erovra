package objects;

import main.Main;
import main.UnitID;
import output.Render;
import utility.Point;

public class City extends Unit {

	int founded = 1;

	public City(Point position, Nation nation, int founded) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.CITY;
		defense = 4;
		this.founded = founded + 1;
		nation.addUnit(new Infantry(position, nation));
	}

	public void tick(double t) {
		engaged = false;
		detectHit();
		if ((Main.ticks - founded) % 600 == 0) {
			nation.addCoin(position);
		}
	}

	public void render(Render r) {
		if (engaged || nation.name.contains("Sweden")) {
			if (capital) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.capital, nation.color);
			} else {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.city, nation.color);
			}
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

}
