package objects;

import java.util.Random;

import main.Image;
import main.Main;
import main.UnitID;
import output.Render;
import terrain.Map;
import utility.Point;
import utility.Trig;
import utility.Vector;

public abstract class Unit {

	Point position;
	Point target;
	Point facing;
	Vector velocity;
	Nation nation;
	int hit = 0;
	float defense;
	float speed;
	float health;
	static Image image = new Image();
	UnitID id;
	UnitID weight;
	Random rand = new Random();
	float a = rand.nextFloat() * (float) Math.PI * 2;
	boolean engaged = false;
	boolean capital = false;

	public Unit(Point position, Nation nation, UnitID weight) {
		this.position = position;
		this.nation = nation;
		this.velocity = new Vector(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
		this.weight = weight;
		health = 10;
		target = position.addPoint(new Point(rand.nextFloat() - 2f, rand.nextFloat() - 0.5f));
		facing = position;
	}

	public Unit(Unit unit) {
		this.position = unit.position;
		this.nation = unit.nation;
		this.velocity = new Vector(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
		health = 10;
		target = position.addPoint(new Point(rand.nextFloat() - 0.5f, rand.nextFloat() - 1f));
		facing = position;
	}

	public abstract void tick(double t);

	public abstract void render(Render r);

	public Point getPosition() {
		return position;
	}

	public float getDefense() {
		return defense;
	}

	public float getSpeed() {
		return speed;
	}

	public float getHealth() {
		return health;
	}

	public UnitID getID() {
		return id;
	}

	public void engage() {
		engaged = true;
	}

	public void disengage() {
		engaged = false;
	}

	// wander(): Finds the closest spotted enemy, if there are any, moves the
	// unit to engage. If there are no spotted enemy, moves around randomly
	public void wander() {
		int smallestDistance = 131072;
		Point smallestPoint = new Point(-1, -1);
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if (tempUnit.engaged) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance && !tempUnit.equals(this) && ((tempUnit.id == UnitID.SHIP) == (id == UnitID.SHIP))) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
				}
			}
		}
		if (smallestPoint.getX() != -1) {
			if (id != UnitID.SHIP) {
				target = smallestPoint;
				facing = target;
			} else {
				target = smallestPoint;
				facing = target;
			}
		} else {
			if (position.getDist(target) < .75) retarget();
			if (id == UnitID.INFANTRY && !engaged) settle();
		}
	}

	// settle(): if the unit is far enough away other ports and cities, it
	// builds either a city, port, or factory
	public void settle() {
		int smallestDistance = 64;
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			if (tempUnit.id == UnitID.CITY || tempUnit.id == UnitID.PORT || tempUnit.id == UnitID.FACTORY) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getCabDist(tempPoint);
				if (tempDist < smallestDistance && !tempUnit.equals(this)) {
					smallestDistance = tempDist;
				}
			}
		}
		if (smallestDistance >= 64) {
			nation.buyPort(position);
			if (!nation.buyCity(position)) {
				nation.buyFactory(position);
				nation.buyAirfield(position);
			}
		}
	}

	// targetMove(): Moves the unit to its target
	public void targetMove() {
		Point newPoint = position.addVector(velocity);
		if (id != UnitID.SHIP) {
			if (Map.getArray(newPoint) > 0.5f) {
				velocityMove();
			} else {
				escapeEdge();
			}
		} else {
			if (weight != UnitID.LIGHT) {
				if (Map.getArray(newPoint) < 0.5f) {
					velocityMove();
				} else {
					escapeEdge();
				}
			} else {
				velocityMove();
			}
		}
	}

	void velocityMove() {
		velocity = position.getTargetVector(target).normalize().scalar(speed);
		position = position.addVector(velocity);
	}

	// detectHit(): checks enemy projectile array. If there are any close enough
	// to the unit, it subtracts the unit's health by (projectile's
	// attack)/(unit's defense)
	public void detectHit() {
		if (hit > 0) hit--;
		for (int i = 0; i < nation.enemyNation.projectileSize(); i++) {
			double distance = position.getDist(nation.enemyNation.getProjectile(i).getPosition());
			Projectile tempProjectile = nation.enemyNation.getProjectile(i);
			if (distance < 256 && !tempProjectile.equals(null) && tempProjectile.attack > 0 && !(!(id == UnitID.PLANE) && (tempProjectile.attack < 0.4f))) {
				tempProjectile.hit();
				hit = 8;
				health -= tempProjectile.attack / defense;
				if (health <= 0) {
					health = 100;
					if (id == UnitID.CITY) {
						nation.enemyNation.coins += 9;
						if (nation.cityCost > 10) nation.cityCost -= 10;
					} else if (id == UnitID.PORT) {
						nation.enemyNation.coins += 14;
						if (nation.portCost > 15) nation.portCost /= 2;
					} else if (id == UnitID.FACTORY) {
						nation.enemyNation.coins += 7;
						if (nation.factoryCost > 8) nation.factoryCost /= 2;
					} else if (id == UnitID.AIRFIELD) {
						nation.enemyNation.coins += 29;
						if (nation.airfieldCost > 30) nation.airfieldCost /= 2;
					} else if (id == UnitID.ARTILLERY) {
						nation.enemyNation.coins += 49;
					} else if (id == UnitID.CAVALRY) {
						nation.enemyNation.coins += 4;
					} else if (id == UnitID.SHIP) {
						nation.enemyNation.coins += 9;
					} else if (id == UnitID.PLANE) {
						nation.enemyNation.coins += 9;
					}
					nation.unitArray.remove(this);
				}
			}
		}
	}

	// autoAim(float cal): Checks for the closest enemy, if there are any close
	// by, it stops and shoots them using the caliber of bullet specified
	public void autoAim(float cal) {
		int smallestDistance = 2048;
		if (id == UnitID.SHIP) smallestDistance = 35000;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempUnit.id == UnitID.PLANE) tempDist /= 36;
			if (tempDist < smallestDistance && ((tempUnit.id == UnitID.SHIP) == (id == UnitID.SHIP)) && !(id != UnitID.INFANTRY && tempUnit.capital)) {
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
				smallestUnit = tempUnit;
			}
		}
		if (smallestUnit != null) {
			smallestUnit.engaged = true;
			engaged = true;
			if (id != UnitID.SHIP) target = position;
			facing = smallestUnit.getPosition();
			if (Main.ticks % 60 == 0) {
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(smallestPoint), cal));
			}
		} else {
			engaged = false;
		}
	}

	void aaAim() {
		int smallestDistance = 35000;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && (tempUnit.id == UnitID.PLANE)) {
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
				smallestUnit = tempUnit;
			}
		}
		if (smallestUnit != null) {
			smallestUnit.engaged = true;
			engaged = true;
			if (Main.ticks % 30 == 0) {
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(smallestPoint), .4f));
			}
		} else {
			engaged = false;
		}
	}

	// torpedoAim(): checks for enemy boats nearby, if there are any, shoots a
	// torpedo at the closest one. For some reason, when tempUnit is set inside
	// the for loop, it changes outside the for loop to another unit. This is
	// why I used smallest Unit. I dont't know why that happened.
	public void torpedoAim() {
		int smallestDistance = 38000;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = null;
			tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && tempUnit.id == UnitID.SHIP) {
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
				smallestUnit = tempUnit;
			}
		}
		if (smallestUnit != null) {
			smallestUnit.engage();
			engaged = true;
			if (Main.ticks % 90 == 0) {
				nation.addProjectile(new Torpedo(position, nation, position.getTargetVector(smallestPoint)));
			}
		} else {
			engaged = false;
		}
	}

	// autoArtilleryAim(): Checks for enemies nearby, if there are any, shoots
	// an
	// artillery shell at them
	public void autoArtilleryAim() {
		int smallestDistance = 73728;
		Point smallestPoint = new Point(-1, -1);
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			if (tempUnit.engaged) {
				Point tempPoint = tempUnit.facing;
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance && tempDist > 8450) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
				}
			}
		}
		if (smallestPoint.getX() != -1) {
			facing = smallestPoint;
			target = position;
			engaged = true;
			if (Main.ticks % 300 == 0) {
				nation.addProjectile(new Shell(position, nation, smallestPoint));
			}
		} else {
			engaged = false;
		}
	}

	// retarget(): changes the target of the unit to one on a circle at radius r
	// away
	void retarget() {
		int r = (int) (speed * 75.0f);
		a += rand.nextFloat() * 0.3 - 0.15f;
		target = position.addPoint(new Point(r * Trig.sin(a), r * Trig.cos(a)));

		if (position.getX() < 0) {
			escapeEdge();
		}
		if (position.getX() > 1024) {
			escapeEdge();
		}
		if (position.getY() < 0) {
			escapeEdge();
		}
		if (position.getY() > 512) {
			escapeEdge();
		}
		facing = target;
	}

	void escapeEdge() {
		target = position;
		Point p1;
		Point p2;
		if (id != UnitID.SHIP) {
			while (Map.getArray(target) < .51) {
				a += 0.015f;
				p1 = new Point(10 * Trig.sin(a), 10 * Trig.cos(a));
				p2 = new Point(10 * Trig.sin(a + 3.14f), 10 * Trig.cos(a + 3.14f));
				if (Map.getArray(p1) < Map.getArray(p2)) {
					target = position.addPoint(p1);
				} else {
					target = position.addPoint(p2);
					a += 3.14;
				}
			}
		} else {
			while (Map.getArray(target) > .49) {
				a += 0.015f;
				p1 = new Point(10 * Trig.sin(a), 10 * Trig.cos(a));
				p2 = new Point(10 * Trig.sin(a + 3.14f), 10 * Trig.cos(a + 3.14f));
				if (Map.getArray(p1) > Map.getArray(p2) && Map.getArray(p1)!= -1 && Map.getArray(p2)!= -1) {
					target = position.addPoint(p1);
				} else {
					target = position.addPoint(p2);
					a += 3.14;
				}
			}
		}
		velocityMove();
	}
}