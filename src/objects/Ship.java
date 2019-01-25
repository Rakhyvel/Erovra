package objects;

import main.UnitID;
import output.Render;
import terrain.Map;
import utility.Point;

public class Ship extends Unit {
	float cal;

	public Ship(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			defense = 1f;
			cal = 0.5f;
			target = nation.enemyNation.capital.getPosition()
					.addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96));

		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			defense = 2;
			cal = 1f;
		} else {
			speed = .05f;
			defense = 3;
			cal = 2f;
		}
		id = UnitID.SHIP;
	}

	public void tick(double t) {
		if (weight != UnitID.LIGHT) {
			wander();
			torpedoAim();
		} else {
			autoAim(cal);
			if (isLanded()) {
				nation.addUnit(new Infantry(position, nation));
				nation.unitArray.remove(this);
			}
		}
		detectHit();
		targetMove();

	}

	boolean isLanded() {
		return Map.getArray(position) > 0.505f;
	}

	public void render(Render r) {
		if ((nation.name.contains("Russia") && engaged) || nation.name.contains("Sweden")) {
			if (velocity.getY() < 0) {
				if (weight == UnitID.LIGHT) {
					r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.landing, nation.color,
							facing.getTargetVector(position).normalize().getRadian());
				} else if (weight == UnitID.MEDIUM) {
					r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.destroyer, nation.color,
							facing.getTargetVector(position).normalize().getRadian());
				} else {
					r.drawImageScreen((int) position.getX(), (int) position.getY(), 16, r.cruiser, nation.color,
							facing.getTargetVector(position).normalize().getRadian());
				}
			} else {
				if (weight == UnitID.LIGHT) {
					r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.landing, nation.color,
							(float)Math.PI+facing.getTargetVector(position).normalize().getRadian());
				} else if (weight == UnitID.MEDIUM) {
					r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.destroyer, nation.color,
							(float)Math.PI+facing.getTargetVector(position).normalize().getRadian());
				} else {
					r.drawImageScreen((int) position.getX(), (int) position.getY(), 16, r.cruiser, nation.color,
							(float)Math.PI+facing.getTargetVector(position).normalize().getRadian());
				}
			}
		}
	}
}
