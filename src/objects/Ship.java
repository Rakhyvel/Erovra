package objects;

import main.UnitID;
import output.Render;
import terrain.Map;
import utility.Point;

public class Ship extends Unit {

	float cal;
	UnitID passenger1, passenger2, weight1, weight2;

	public Ship(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			defense = 1f;
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
			torpedoAim();
			aaAim();
			wander();
			targetMove();
		} else {
			aaAim();
			autoAim(.5f);
			if (isLanded()) {
				if (passenger1 == UnitID.INFANTRY) nation.addUnit(new Infantry(position, nation));
				if (passenger1 == UnitID.CAVALRY) nation.addUnit(new Cavalry(position, nation, weight1));
				if (passenger1 == UnitID.ARTILLERY) nation.addUnit(new Cavalry(position, nation, weight1));
				if (passenger2 == UnitID.INFANTRY) nation.addUnit(new Infantry(position, nation));
				if (passenger2 == UnitID.CAVALRY) nation.addUnit(new Cavalry(position, nation, weight2));
				if (passenger2 == UnitID.ARTILLERY) nation.addUnit(new Cavalry(position, nation, weight2));
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
		int smallestDistance = 600000;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		Unit secondUnit = null;
		int id1 = -1, id2 = -1;
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && ((tempUnit.id == UnitID.CAVALRY) || (tempUnit.id == UnitID.ARTILLERY) || (tempUnit.id == UnitID.INFANTRY))) {
				secondUnit = smallestUnit;
				id2 = id1;
				id1 = i;
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
				smallestUnit = tempUnit;
			}
		}
		System.out.println(smallestUnit);
		if (smallestUnit != null && secondUnit != null) {
			passenger1 = smallestUnit.id;
			weight1 = smallestUnit.weight;
			passenger2 = secondUnit.id;
			weight2 = secondUnit.weight;
			nation.unitArray.remove(smallestUnit);
			nation.unitArray.remove(secondUnit);
		} else {
			nation.coins+=10;
			nation.unitArray.remove(this);
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
