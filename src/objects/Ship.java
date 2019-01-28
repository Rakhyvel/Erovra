package objects;

import main.UnitID;
import output.Render;
import terrain.Map;
import utility.Point;

public class Ship extends Unit {

	float cal;
	Unit passenger1, passenger2;

	public Ship(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			defense = 0.1f;
			cal = 0.5f;
			target = nation.enemyNation.capital.position.addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96));
			velocity = position.subVec(target).normalize().scalar(speed);
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
			engaged = torpedoAim() || aaAim();
			wander();
			targetMove();
		} else {
			engaged = aaAim();
			if (isLanded()) {
				nation.addUnit(new Infantry(position,nation));
				nation.unitArray.remove(this);
			}
			position = position.addVector(velocity);
		}
		detectHit();

	}

	// isLander(): checks to see if the boat has reached land, used for landind
	// craft
	boolean isLanded() {
		return Map.getArray(position) > 0.505f;
	}

	public void render(Render r) {
		if (engaged || nation.name.contains("Sweden")) {
			float direction = position.subVec(target).getRadian();
			if (velocity.getY() > 0) direction += 3.14f;

			if (weight == UnitID.LIGHT) r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.landing, r.lighten(nation.color), direction);
			if (weight == UnitID.MEDIUM) r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.destroyer, nation.color, direction);
			if (weight == UnitID.HEAVY) r.drawImageScreen((int) position.getX(), (int) position.getY(), 16, r.cruiser, r.darken(nation.color), direction);
		}
	}
}
