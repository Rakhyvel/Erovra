package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.gui.Image;
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
	Image city;
	Image capitalImg;

	public City(Point position, Nation nation, int founded) {
		super(position, nation, UnitID.NONE);
		speed = 0;
		id = UnitID.CITY;
		defense = 2;
		city = new Image("/res/buildings/city.png", 32, 32).getScreenBlend(nation.color);
		capitalImg = new Image("/res/buildings/capital.png", 32, 32).getScreenBlend(nation.color);
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (engaged || hit > 0)
				spotted = true;
			engaged = false;
			if ((Main.ticks - born) % 320 == 0) {
				nation.addCoin(position);
			}
			if (Main.ticks % 6000 == 0 && capital) {
				nation.addUnit(new Infantry(position, nation));
				nation.setLandSupremacy(1);
			}
		}
	}

	@Override
	public void render(Render r) {
		if (spotted || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) {
			if (capital) {
				r.drawRect((int) position.getX() - 16, (int) position.getY() - 20, 32, 6, 255 << 24);
				r.drawRect((int) position.getX() - 14, (int) position.getY() - 18,
						(int) (28.0 * (Main.ticks % 6000) / 6000), 2, nation.color);
				r.drawImage((int) position.getX(), (int) position.getY(), capitalImg,0);
			} else {
				r.drawImage((int) position.getX(), (int) position.getY(), city,0);
			}
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), r.cityHit, 0);
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
