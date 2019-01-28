package objects;

import main.Main;
import main.UnitID;
import output.Render;
import utility.Point;
import utility.Trig;

public class Plane extends Unit {

	Point patrolPoint;
	boolean bombsAway = false;

	public Plane(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .9f;
			defense = 1f;
			patrolPoint = nation.enemyNation.capital.getPosition()
					.addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96));
		} else if (weight == UnitID.MEDIUM) {
			speed = .6f;
			defense = 2f;
		} else {
			speed = 0.3f;
			defense = 3f;
			acquireTarget();
		}
		id = UnitID.PLANE;
	}

	public void tick(double t) {
		if (weight != UnitID.HEAVY) {
			planeAim();
			patrol();
		} else {
			aaAim();
			if (bombsAway) {
				if (position.getDist(target) < 1) {
					nation.coins += 1.5 * health;
					nation.unitArray.remove(this);
				}
			} else {
				if (position.getDist(target) < 1) {
					target = nation.capital.position;
					nation.addProjectile(new Bomb(position, nation));
					bombsAway = true;
				}
			}
		}
		detectHit();
		velocity = position.getTargetVector(target).normalize().scalar(speed);
		position = position.addVector(velocity);
		if (position.getX() < 0) {
			position.setX(0);
		}
		if (position.getY() < 0) {
			position.setY(0);
		}
		if (position.getY() > 512) {
			position.setY(512);
		}
		if (position.getX() > 1024) {
			position.setX(1024);
		}
	}

	void patrol() {
		// If you're not dead on target, turn, or if you're too close, turn
		if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) < 0.99
				|| position.getDist(patrolPoint) < 8192)
			a += 0.035 * speed;
		// If you're directly behind, turn slower
		if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) < -0.5)
			a -= 0.025 * speed;
		target = position.addPoint(new Point(Trig.sin(a), Trig.cos(a)));

	}

	void planeAim() {
		int smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if (((tempUnit.id == UnitID.PLANE) == (weight == UnitID.LIGHT)) && (tempUnit.id != UnitID.CITY
					&& tempUnit.id != UnitID.FACTORY && tempUnit.id != UnitID.PORT && tempUnit.id != UnitID.AIRFIELD)) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempUnit.id == UnitID.SHIP)
					tempDist /= 2;
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
					smallestUnit = tempUnit;
				}
			}
		}
		if (smallestPoint.getX() != -1) {
			smallestUnit.engaged = true;
			patrolPoint = smallestPoint.addVector(smallestUnit.velocity.subVec(velocity).scalar(Math.sqrt(smallestDistance)/4));
			if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) > 0.95 && smallestDistance < 73728) {
				if ((Main.ticks - born) % 15 == 0 && weight == UnitID.LIGHT) {
					nation.addProjectile(new Bullet(position, nation,
							position.getTargetVector(patrolPoint), 0.49f));
				} else if ((Main.ticks - born) % 60 == 0 && weight == UnitID.MEDIUM) {
					Point pointA = new Point(22 * Trig.sin(a + 0.1f), 22 * Trig.cos(a + 0.1f));
					Point pointB = new Point(22 * Trig.sin(a - 0.1f), 22 * Trig.cos(a - 0.1f));
					nation.addProjectile(new Bullet(position.addPoint(pointA), nation,
							position.getTargetVector(patrolPoint), 1f));
					nation.addProjectile(new Bullet(position.addPoint(pointB), nation,
							position.getTargetVector(patrolPoint), 1f));
				}
			}
		} else {
			patrolPoint = nation.capital.position;
		}
	}

	void acquireTarget() {
		int smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if (tempUnit.id == UnitID.CITY || tempUnit.id == UnitID.FACTORY || tempUnit.id == UnitID.PORT
					|| tempUnit.id == UnitID.AIRFIELD) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempUnit.capital)
					tempDist *= 16;
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
				}
			}
		}
		target = smallestPoint.addPoint(new Point(rand.nextInt(10) - 5, rand.nextInt(10) - 5));
	}

	public void render(Render r) {
		float direction = position.subVec(target).getRadian();
		if (velocity.getY() > 0)
			direction += 3.14f;

		if (Main.ticks % 4 < 2) {
			if (weight == UnitID.LIGHT)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.fighter1, r.lighten(nation.color),
						direction);
			if (weight == UnitID.MEDIUM)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 44, r.attacker1, nation.color,
						direction);
			if (weight == UnitID.HEAVY)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 67, r.bomber1, r.darken(nation.color),
						direction);
		} else {
			if (weight == UnitID.LIGHT)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.fighter2, r.lighten(nation.color),
						direction);
			if (weight == UnitID.MEDIUM)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 44, r.attacker2, nation.color,
						direction);
			if (weight == UnitID.HEAVY)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 67, r.bomber2, r.darken(nation.color),
						direction);
		}
		if(hit > 1 && weight == UnitID.LIGHT)r.drawImageScreen((int) position.getX(), (int) position.getY(), 40, r.fighterHit, r.lighten(nation.color),
				direction);
		if(hit > 1 && weight == UnitID.MEDIUM)r.drawImageScreen((int) position.getX(), (int) position.getY(), 48, r.attackerHit, r.lighten(nation.color),
				direction);
		if(hit > 1 && weight == UnitID.HEAVY)r.drawImageScreen((int) position.getX(), (int) position.getY(), 71, r.bomberHit, nation.color,
				direction);
	}

}
