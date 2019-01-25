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
		if ((nation.name.contains("Russia") && engaged) || nation.name.contains("Sweden")) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, nation.color,
					position.getTargetVector(facing).normalize().getRadian());
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color,
						position.getTargetVector(facing).normalize().getRadian());
			}
		}
	}
}