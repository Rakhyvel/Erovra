package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for City units
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class City extends Unit {

	private boolean spotted = false;

	public City(Point position, Nation nation, int founded) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.CITY;
		defense = 4;
		nation.landSupremacy++;
	}

	@Override
	public void tick(double t) {
		if (engaged) spotted = true;
		engaged = false;
		detectHit();
		if ((Main.ticks - born) % 320 == 0) {
			nation.addCoin(position);
		}
		if(Main.ticks % 6000 == 0 && capital){
			nation.addUnit(new Infantry(position,nation));
		}
	}

	@Override
	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) {
			if (capital) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 0);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18, (int) (28.0 * (Main.ticks%6000) / 6000), 2, nation.color);
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.capital, nation.color);
			} else {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.city, nation.color);
			}
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.cityHit, nation.color);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
	}

}
