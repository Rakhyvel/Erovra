package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Infantry extends Unit {

	public Infantry(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = .1f;
		defense = 1;
		id = UnitID.INFANTRY;
	}

	public void tick(double t) {
		wander();
		autoAim(1);
		detectHit();
		targetMove();
	}

	public void render(Render r) {
		if ((nation.name.contains("Russia") && engaged) || nation.name.contains("Sweden")) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.infantry, nation.color, a);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color, a);
			}
		}
	}
}
