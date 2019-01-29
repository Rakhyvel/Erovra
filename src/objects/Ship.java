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
			defense = 0.2f;
			cal = 0.5f;
			target = nation.enemyNation.capital.position.addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96));
			velocity = position.subVec(target).normalize().scalar(speed);
			loadPassengers();
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
			if (passenger1 == null) {
				nation.coins += 5;
				nation.unitArray.remove(this);
			}
			engaged = aaAim() || autoAim(0.5f);
			if (isLanded()) {
				if (passenger1 != null) {
					passenger1.position = position;
					passenger1.boarded = false;
					if (passenger2 != null) {
						passenger2.position = position;
						passenger2.boarded = false;
					}
				}
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

	void loadPassengers() {
		int smallestDistance = 73728;
		Unit firstUnit = null;
		Unit secondUnit = null;
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && ((tempUnit.id == UnitID.CAVALRY) || (tempUnit.id == UnitID.INFANTRY)) && !tempUnit.engaged && !tempUnit.boarded) {
				smallestDistance = tempDist;
				secondUnit = firstUnit;
				firstUnit = tempUnit;
			}
		}
		if (firstUnit != null) {
			firstUnit.boarded = true;
			passenger1 = firstUnit;
			if (secondUnit != null) {
				secondUnit.boarded = true;
				passenger2 = secondUnit;
			}
		}
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