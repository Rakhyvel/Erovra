package objects.units;

import main.Main;
import main.StateID;
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
		defense = 4;
		nation.addUnit(new Infantry(position, nation));
		nation.landSupremacy++;
	}

	public void tick(double t) {
		if (engaged) spotted = true;
		engaged = false;
		detectHit();
		if ((Main.ticks - born) % 600 == 0) {
			nation.addCoin(position);
		}
	}

	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
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
