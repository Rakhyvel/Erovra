package objects.units;

import main.Main;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.projectiles.Bomb;
import objects.projectiles.Bullet;
import output.Render;
import utility.Point;
import utility.Trig;
/**
 * Handles the logic and rendering for planes
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class Plane extends Unit {

	private Point patrolPoint;
	private boolean bombsAway = false;

	public Plane(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .9f;
			defense = 1f;
			patrolPoint = nation.enemyNation.capital.getPosition().addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96));
		} else if (weight == UnitID.MEDIUM) {
			speed = .6f;
			defense = 2f;
		} else {
			speed = 0.3f;
			defense = 1f;
			acquireTarget();
		}
		id = UnitID.PLANE;
	}

	@Override
	public void tick(double t) {
		if (getWeight() != UnitID.HEAVY) {
			planeAim();
			patrol();
		} else {
			if (getTarget().getX() == -1) {
				nation.unitArray.remove(this);
				nation.coins += 10;
			}
			aaAim();
			if (bombsAway) {
				if (position.getDist(getTarget()) < 1) {
					nation.coins += health;
					nation.unitArray.remove(this);
				}
			} else {
				if(nation.isAIControlled()) {
					acquireTarget();
				} else {
					clickToMove();
				}
				if (position.getDist(getTarget()) < 1) {
					setTarget(new Point(nation.capital.getPosition()));
					nation.addProjectile(new Bomb(position, nation));
					bombsAway = true;
				}
			}
		}
		detectHit();
		velocity = position.getTargetVector(getTarget()).normalize().scalar(getSpeed());
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

	/**
	 * Moves the plane based on its position's and its patrol point
	 */
	void patrol() {
		// If you're not dead on target, turn, or if you're too close, turn
		if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) < 0.99) a += 0.035 * getSpeed();
		// If you're directly behind, turn slower
		if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) < -0.5) a -= 0.025 * getSpeed();
		setTarget(position.addPoint(new Point(Trig.sin(a), Trig.cos(a))));

	}

	/**
	 * Decides what the plane should aim at
	 */
	void planeAim() {
		int smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		// Walk through entire enemy unit array
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			// If am fighter, and enemy is plane, target
			// If am not fighter, and enemy is not plane, target
			// Do not target if anything else
			if (((tempUnit.id == UnitID.PLANE) == (getWeight() == UnitID.LIGHT)) && (tempUnit.id != UnitID.CITY && tempUnit.id != UnitID.FACTORY && tempUnit.id != UnitID.PORT && tempUnit.id != UnitID.AIRFIELD) && !tempUnit.isBoarded() && tempUnit.getID() != UnitID.NONE) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempUnit.id == UnitID.SHIP) tempDist /= 2;
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
					smallestUnit = tempUnit;
				}
			}
		}
		if (smallestUnit != null) {
			smallestUnit.engage();
			patrolPoint = smallestPoint.addVector(smallestUnit.velocity.subVec(velocity).scalar(Math.sqrt(smallestDistance) / 4));
			if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) > 0.95 && smallestDistance < 73728) {
				if ((Main.ticks - born) % 15 == 0 && getWeight() == UnitID.LIGHT) {
					nation.addProjectile(new Bullet(position, nation, position.getTargetVector(patrolPoint), .5f, UnitID.AIRBULLET));
				} else if ((Main.ticks - born) % 60 == 0 && getWeight() == UnitID.MEDIUM) {
					Point pointA = new Point(22 * Trig.sin(a + 0.1f), 22 * Trig.cos(a + 0.1f));
					Point pointB = new Point(22 * Trig.sin(a - 0.1f), 22 * Trig.cos(a - 0.1f));
					nation.addProjectile(new Bullet(position.addPoint(pointA), nation, position.getTargetVector(smallestPoint), 1f, UnitID.ANTIPERSONEL));
					nation.addProjectile(new Bullet(position.addPoint(pointB), nation, position.getTargetVector(smallestPoint), 1f, UnitID.ANTIPERSONEL));
				}
			}
		} else {
			patrolPoint = nation.capital.position;
		}
	}

	/**
	 * Decides what building bombers should target
	 */
	void acquireTarget() {
		int smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if ((tempUnit.id == UnitID.CITY || tempUnit.id == UnitID.FACTORY || tempUnit.id == UnitID.PORT || tempUnit.id == UnitID.AIRFIELD)) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
					smallestUnit = tempUnit;
				}
			}
		}
		if (smallestUnit != null) {
			smallestUnit.engage();
			setTarget(new Point(smallestPoint));
		} else {
			setTarget(smallestPoint);
		}
	}

	@Override
	public void render(Render r) {
		float direction = position.subVec(getTarget()).getRadian();
		if (velocity.getY() > 0) direction += 3.14f;
		
		if(weight == UnitID.HEAVY && !nation.isAIControlled() && !bombsAway) {
			if (isSelected()) {
				r.drawImageScreen((int) getTarget().getX(), (int) getTarget().getY(), 16, r.flag, nation.color);
				r.drawLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
			} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
				r.drawLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color, 220 << 16 | 220 << 8 | 220);
			}
		}

		if (Main.ticks % 4 < 2) {
			if (getWeight() == UnitID.LIGHT) r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.fighter1, r.lighten(nation.color), direction);
			if (getWeight() == UnitID.MEDIUM) r.drawImageScreen((int) position.getX(), (int) position.getY(), 44, r.attacker1, nation.color, direction);
			if (getWeight() == UnitID.HEAVY) r.drawImageScreen((int) position.getX(), (int) position.getY(), 68, r.bomber1, r.darken(nation.color), direction);
		} else {
			if (getWeight() == UnitID.LIGHT) r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.fighter2, r.lighten(nation.color), direction);
			if (getWeight() == UnitID.MEDIUM) r.drawImageScreen((int) position.getX(), (int) position.getY(), 44, r.attacker2, nation.color, direction);
			if (getWeight() == UnitID.HEAVY) r.drawImageScreen((int) position.getX(), (int) position.getY(), 68, r.bomber2, r.darken(nation.color), direction);
		}
		if (hit > 1 && getWeight() == UnitID.LIGHT) r.drawImageScreen((int) position.getX(), (int) position.getY(), 40, r.fighterHit, r.lighten(nation.color), direction);
		if (hit > 1 && getWeight() == UnitID.MEDIUM) r.drawImageScreen((int) position.getX(), (int) position.getY(), 48, r.attackerHit, nation.color, direction);
		if (hit > 1 && getWeight() == UnitID.HEAVY) r.drawImageScreen((int) position.getX(), (int) position.getY(), 72, r.bomberHit, r.darken(nation.color), direction);
	}

	@Override
	public void dropDownDecide(DropDown d) {
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {		
	}

}
