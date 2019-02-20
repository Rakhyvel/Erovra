package objects.units;

import main.Main;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;


public class DeathMarker extends Unit{

	public DeathMarker(Point position, Nation nation) {
		super(position, nation, UnitID.LIGHT);
		id = UnitID.NONE;
	}

	@Override
	public void tick(double t) {	
	}

	@Override
	public void render(Render r) {
		r.drawImageScreen((int)position.getX(), (int)position.getY(), 16, r.flag, nation.enemyNation.color);
	}

	@Override
	public void dropDownDecide(DropDown d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		// TODO Auto-generated method stub
		
	}

}
