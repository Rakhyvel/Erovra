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
		if(!boarded){
			wander();
			engaged = autoAim(1) || aaAim();
			detectHit();
			targetMove();
		}
	}

	public void render(Render r) {
		if (engaged || nation.name.contains("Sweden") && !boarded) {
			float direction = position.subVec(facing).getRadian();
			if (velocity.getY() > 0) direction += 3.14f;

			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.infantry, nation.color, direction);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color, direction);
			}
		}
	}
}