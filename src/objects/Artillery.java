package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Artillery extends Unit {

	public Artillery(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		defense = 1;
		speed = 0.05f;
		if (weight == UnitID.LIGHT) {
			speed = .1f;
			defense = 1;
		} else if (weight == UnitID.MEDIUM) {
			speed = 0f;
			defense = 2;
		} else {
			speed = 0.05f;
			defense = 1;
		}
		id = UnitID.ARTILLERY;
	}

	public void tick(double t) {
		wander();
		autoArtilleryAim();
		detectHit();
		targetMove();
	}

	public void render(Render r) {
		if (engaged || nation.name.contains("Sweden") && !boarded) {
			float direction = position.subVec(target).getRadian();
			if (velocity.getY() > 0) direction += 3.14f;
			
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, nation.color, direction);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color, direction);
			}
		}
	}
}