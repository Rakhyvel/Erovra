package objects;

import main.UnitID;
import output.Render;
import utility.Point;

public class Cavalry extends Unit {
	float cal;

	public Cavalry(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			defense = 2;
			cal = 0.5f;
		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			defense = 2;
			cal = 1f;
		} else {
			speed = .05f;
			defense = 2;
			cal = 2f;
		}
		id = UnitID.CAVALRY;
	}

	public void tick(double t) {
		wander();
		autoAim(cal);
		detectHit();
		targetMove();
	}

	public void render(Render r) {
		if ((nation.name.contains("Russia") && engaged) || nation.name.contains("Sweden")) {
			if (weight == UnitID.LIGHT)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, r.lighten(nation.color),
						position.getTargetVector(facing).normalize().getRadian());
			if (weight == UnitID.MEDIUM)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, nation.color,
						position.getTargetVector(facing).normalize().getRadian());
			if (weight == UnitID.HEAVY)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, r.darken(nation.color),
						position.getTargetVector(facing).normalize().getRadian());
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color,
						position.getTargetVector(facing).normalize().getRadian());
			}
		}
	}
}
