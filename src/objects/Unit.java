package objects;

import java.util.Random;

import main.Image;
import main.Main;
import main.UnitID;
import output.Render;
import terrain.Map;
import utility.Point;
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

	public void wander() {
		int smallestDistance = 131072;
		Point smallestPoint = new Point(-1, -1);
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			if (tempUnit.engaged) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance && !tempUnit.equals(this)) {
					smallestDistance = tempDist;
					smallestPoint = tempUnit.facing;
				}
			}
		}
		if (smallestPoint.getX() != -1) {
			if (id != UnitID.SHIP) {
				if (Map.getArray(position) > 0.52f) {
					target = smallestPoint;
					facing = target;
				} else {
					retarget();
				}
			} else {
				if (Map.getArray(position) < 0.48f) {
					target = smallestPoint;
					facing = target;
				} else {
					retarget();
				}
			}
		} else {
			if (position.getDist(target) < 1)
				retarget();
			if (id == UnitID.INFANTRY && !engaged)
				settle();
		}
	}

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
			if (!nation.buyCity(position))
				nation.buyFactory(position);
			nation.buyPort(position);
		}
	}

	public void targetMove() {
		velocity = new Vector(0, 0);
		if (position.getDist(target) > 0.9) {
			velocity = position.getTargetVector(target).normalize().scalar(speed);
		}
		Point newPoint = position.addVector(velocity);
		if (id != UnitID.SHIP) {
			if (Map.getArray(newPoint) > 0.5f) {
				position = position.addVector(velocity);
			} else {
				retarget();
				a-=Math.PI/2;
			}
		} else {
			if (weight != UnitID.LIGHT) {
				if (Map.getArray(newPoint) < 0.5f) {
					position = position.addVector(velocity);
				} else {
					retarget();
					a-=Math.PI/2;
				}
			} else {
				position = position.addVector(velocity);
			}
		}
	}

	public void detectHit() {
		if (hit > 0)
			hit--;
		for (int i = 0; i < nation.enemyNation.projectileSize(); i++) {
			double distance = position.getDist(nation.enemyNation.getProjectile(i).getPosition());
			Projectile tempProjectile = nation.enemyNation.getProjectile(i);
			if (distance < 256 && !tempProjectile.equals(null) && tempProjectile.attack > 0) {
				tempProjectile.hit();
				hit = 8;
				health -= tempProjectile.attack / defense;
				if (health <= 0) {
					nation.unitArray.remove(this);
					if (id == UnitID.CITY) {
						nation.enemyNation.coins += 9;
						if (nation.cityCost > 10)
							nation.cityCost -= 10;
					} else if (id == UnitID.PORT) {
						nation.enemyNation.coins += 14;
						if (nation.portCost > 15)
							nation.portCost -= 15;
					} else if (id == UnitID.FACTORY) {
						nation.enemyNation.coins += 7;
						if (nation.factoryCost > 8)
							nation.factoryCost -= 8;
					} else if (id == UnitID.ARTILLERY) {
						nation.enemyNation.coins += 49;
					} else if (id == UnitID.CAVALRY) {
						nation.enemyNation.coins += 4;
					}
				}
			}
		}
	}

	public void autoAim(float cal) {
		// COULD BE OPTIMIZED!
		int smallestDistance = 2048;
		Point smallestPoint = new Point(-1, -1);
		Unit tempUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance) {
				if (tempUnit.id == UnitID.SHIP && id == UnitID.SHIP)
					tempDist /= 32;
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
			}
		}
		if (smallestPoint.getX() != -1 && (id == UnitID.SHIP ^ tempUnit.id != UnitID.SHIP)) {
			engaged = true;
			if (id != UnitID.SHIP) {
				target = position;
				facing = smallestPoint;
			}
			if (Main.ticks % 60 == 0) {
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(smallestPoint), cal));
			}
		} else {
			engaged = false;
		}
	}
	public void torpedoAim() {
		// COULD BE OPTIMIZED!
		int smallestDistance = 131072;
		Point smallestPoint = new Point(-1, -1);
		Unit tempUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && tempUnit.id == UnitID.SHIP) {
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
			}
		}
		if (smallestPoint.getX() != -1) {
			engaged = true;
			if (Main.ticks % 90 == 0) {
				nation.addProjectile(new Torpedo(position, nation, position.getTargetVector(smallestPoint)));
			}
		} else {
			engaged = false;
		}
	}

	public void autoArtilleryAim() {
		// COULD BE OPTIMIZED!
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

	void retarget() {
		int r = (int) (speed * 75.0f);
		a+= rand.nextFloat() * speed - speed/2;
		target = position.addPoint(new Point(r * Math.sin(a), r * Math.cos(a)));

		if (target.getX() < 0) {
			a -= Math.PI;
			target.setX(0);
		}
		if (target.getY() < 0) {
			a -= Math.PI;
			target.setY(0);
		}
		if (target.getY() > 512) {
			a -= Math.PI;
			target.setY(512);
		}
		if (target.getX() > 1024) {
			a -= Math.PI;
			target.setX(1024);
		}
		facing = target;
	}
}