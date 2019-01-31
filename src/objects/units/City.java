package objects.units;

import main.Main;
import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

public class City extends Unit {
	boolean spotted = false;

	public City(Point position, Nation nation, int founded) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.CITY;
		defense = 3;
		nation.addUnit(new Infantry(position, nation));
	}

	public void tick(double t) {
		if (engaged)
			spotted = true;
		engaged = false;
		if (!(nation.defeated || nation.enemyNation.defeated)) {
			detectHit();
			if ((Main.ticks - born) % 600 == 0) {
				nation.addCoin(position);
			}
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || nation.enemyNation.defeated || nation.defeated) {
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
