package objects.units;

import main.Main;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.gui.Image;
import objects.projectiles.Bomb;
import objects.projectiles.Bullet;
import output.Render;
import utility.Point;
import utility.Trig;
import utility.Vector;

/**
 * Handles the logic and rendering for planes
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class Plane extends Unit {

	private Point patrolPoint;
	private boolean bombsAway = false;
	private Point secondaryTarget = new Point(nation.capital.position);
	int weightColor = 255 << 24;
	Image plane1;
	Image plane2;
	Image planeHit;

	public Plane(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .9f;
			setDefense(1f);
			patrolPoint = new Point(nation.enemyNation.capital.getPosition())
					.addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96));
			weightColor = Render.lighten(nation.color);
			plane1 = new Image("/res/air/fighter.png", 36, 35).getScreenBlend(weightColor);
			plane2 = new Image("/res/air/fighter1.png", 36, 35).getScreenBlend(weightColor);
			planeHit = new Image("/res/air/fighterHit.png", 40, 39);
		} else if (weight == UnitID.MEDIUM) {
			speed = .6f;
			setDefense(2f);
			weightColor = nation.color;
			plane1 = new Image("/res/air/attack.png", 44, 33).getScreenBlend(weightColor);
			plane2 = new Image("/res/air/attack1.png", 44, 33).getScreenBlend(weightColor);
			planeHit = new Image("/res/air/attackHit.png", 48, 37);
		} else {
			speed = 0.3f;
			setDefense(2f);
			acquireTarget();
			weightColor = Render.darken(nation.color);
			plane1 = new Image("/res/air/bomber1.png", 68, 40).getScreenBlend(weightColor);
			plane2 = new Image("/res/air/bomber2.png", 68, 40).getScreenBlend(weightColor);
			planeHit = new Image("/res/air/bomberHit.png", 72, 44);
		}
		id = UnitID.PLANE;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (health > 0) {
			if (getWeight() != UnitID.HEAVY) {
				planeAim();
				patrol();
				if (!nation.isAIControlled())
					clickToPatrol();
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
					if (nation.isAIControlled()) {
						acquireTarget();
					} else {
						clickToMove();
					}
					if (position.getDist(getTarget()) < 1) {
						setTarget(new Point(nation.capital.getPosition()));
						nation.addProjectile(new Bomb(position, nation));
						bombsAway = true;
						if (Main.world.selectedUnit != null) {
							if (selected || Main.world.selectedUnit.equals(this))
								Main.world.selectedUnit = null;
						}
					}
				}
			}
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
	}

	/**
	 * Moves the plane based on its position's and its patrol point
	 */
	void patrol() {
		// If you're not dead on target, turn, or if you're too close, turn
//		if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) < 0.99)
//			a += 0.035 * getSpeed();
//		// If you're directly behind, turn slower
//		if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) < -0.5)
//			a -= 0.025 * getSpeed();
		Vector targetVector = patrolPoint.subVec(position).normalize();
		Vector determinateVector = new Vector(0,0);
		// x = x * cos(pi/2)-y*sin(pi/2)
		// y = x * sin(pi/2)+y*cos(pi/2)
		determinateVector
				.setX((float) (velocity.getX() * Math.cos(Math.PI / 2) - velocity.getY() * Math.sin(Math.PI / 2)));
		determinateVector
				.setY((float) (velocity.getX() * Math.sin(Math.PI / 2) + velocity.getY() * Math.cos(Math.PI / 2)));

		if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) > -0.5) {
			if(velocity.normalize().dot(position.subVec(patrolPoint).normalize()) < 0.99) {
				if (determinateVector.dot(targetVector) > 0) {
					a += 0.03f * getSpeed();
				} else{
					a -= 0.03f * getSpeed();
				}
			}
		} else {
			a += 0.015f * getSpeed();
		}
		setTarget(position.addPoint(new Point(Trig.sin(a), Trig.cos(a))));

	}

	/**
	 * Decides what the plane should aim at
	 */
	void planeAim() {
		int smallestDistance = 1310720;
		if (weight == UnitID.MEDIUM)
			smallestDistance = 36864;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		// Walk through entire enemy unit array
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			// If am fighter, and enemy is plane, target
			// If am not fighter, and enemy is not plane, target
			// Do not target if anything else
			if (((tempUnit.id == UnitID.PLANE) == (getWeight() == UnitID.LIGHT))
					&& (tempUnit.id != UnitID.CITY && tempUnit.id != UnitID.FACTORY && tempUnit.id != UnitID.PORT
							&& tempUnit.id != UnitID.AIRFIELD)
					&& !tempUnit.isBoarded() && tempUnit.getID() != UnitID.NONE) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = 0;
				if (weight == UnitID.MEDIUM) {
					tempDist = (int) secondaryTarget.getDist(tempPoint);
				} else {
					tempDist = (int) position.getDist(tempPoint);
				}
				if (tempUnit.id == UnitID.SHIP)
					tempDist /= 2;
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
					smallestUnit = tempUnit;
				}
			}
		}
		if (smallestUnit != null) {
			smallestUnit.engage();
			patrolPoint = new Point(smallestPoint
					.addVector(smallestUnit.velocity.subVec(velocity).scalar(Math.sqrt(smallestDistance) / 4)));
			if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) > 0.95 && smallestDistance < 73728) {
				if ((Main.ticks - born) % 15 == 0 && getWeight() == UnitID.LIGHT) {
					nation.addProjectile(
							new Bullet(position, nation, position.getTargetVector(patrolPoint), .5f, UnitID.AIRBULLET));
				} else if ((Main.ticks - born) % 60 == 0 && getWeight() == UnitID.MEDIUM) {
					Point pointA = new Point(22 * Trig.sin(a + 0.1f), 22 * Trig.cos(a + 0.1f));
					Point pointB = new Point(22 * Trig.sin(a - 0.1f), 22 * Trig.cos(a - 0.1f));
					nation.addProjectile(new Bullet(position.addPoint(pointA), nation,
							position.getTargetVector(smallestPoint), 1f, UnitID.ANTIPERSONEL));
					nation.addProjectile(new Bullet(position.addPoint(pointB), nation,
							position.getTargetVector(smallestPoint), 1f, UnitID.ANTIPERSONEL));
				}
			}
		} else {
			if (nation.isAIControlled()) {
				if (nation.airSupremacy > nation.enemyNation.airSupremacy) {
					secondaryTarget = new Point(nation.enemyNation.capital.getPosition());
				} else {
					secondaryTarget = new Point(nation.capital.getPosition());
				}
			}
			patrolPoint = new Point(secondaryTarget);
		}
	}

	/**
	 * Decides what building bombers should target
	 */
	void acquireTarget() {
		int smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if ((tempUnit.id == UnitID.CITY || tempUnit.id == UnitID.FACTORY || tempUnit.id == UnitID.PORT
					|| tempUnit.id == UnitID.AIRFIELD)) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
				}
			}
		}
		setTarget(new Point(smallestPoint));
	}

	@Override
	public void render(Render r) {
		float direction = position.subVec(getTarget()).getRadian();
		if (velocity.getY() > 0)
			direction += 3.14f;
		if (!nation.isAIControlled()) {
			if (weight == UnitID.HEAVY && !bombsAway) {
				if (isSelected()) {
					r.drawImage((int) target.getX(), (int) target.getY(), r.target.getScreenBlend(nation.color), 0);
					r.drawLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
					r.drawLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color,
							220 << 16 | 220 << 8 | 220);
				}
			} else if (weight == UnitID.MEDIUM || weight == UnitID.LIGHT) {
				if (isSelected()) {
					r.drawImage((int) secondaryTarget.getX(), (int) secondaryTarget.getY(),
							r.target.getScreenBlend(nation.color), 0);
					r.drawLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
					r.drawImage((int) secondaryTarget.getX(), (int) secondaryTarget.getY(),
							r.target.getScreenBlend(nation.color), 0);
				}
			}
		}

		if (Main.ticks % 4 < 2) {
			r.drawImage((int) position.getX(), (int) position.getY(), plane1, direction);
		} else {
			r.drawImage((int) position.getX(), (int) position.getY(), plane2, direction);
		}
		if (hit > 1)
			r.drawImage((int) position.getX(), (int) position.getY(), planeHit, direction);
	}

	@Override
	public void dropDownDecide(DropDown d) {
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
	}

	void clickToPatrol() {
		if (Main.world.selectedUnit == null) {
			if (boundingBox(Main.mouse.getX(), Main.mouse.getY()) && Main.world.highlightedUnit == null) {
				Main.world.highlightedUnit = this;
				hit = 3;
			}
			// If there is no a unit currently selected
			if (Main.mouse.getMouseLeftDown() && !Main.world.getDropDown().getShown()) {
				// If mouse is down AND the dropdown isn't displayed
				if (isSelected() || boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
					// If unit is already selected OR the unit is not selected,
					// and the mouse is
					// near
					// Set leftClicked to true
					leftClicked = true;
				}
			} else if (leftClicked && boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
				// if the mouse isn't down, but leftClicked is true
				setSelected(!isSelected());
				if (selected) {
					Main.world.selectedUnit = this;
				}
				leftClicked = false;
			} else {
				selected = false;
				leftClicked = false;
			}
		} else if (Main.world.selectedUnit.equals(this)) {
			// If there is a unit being selected AND the selected unit is this
			// unit AND the
			// mouse is down
			if (Main.mouse.getMouseLeftDown()) {
				leftClicked = true;
			} else if (leftClicked) {
				secondaryTarget.setX(Main.mouse.getX());
				secondaryTarget.setY(Main.mouse.getY());
				Main.world.nullifySelected();
			}
			if (Main.mouse.getMouseRightDown()) {
				selected = false;
				Main.world.nullifySelected();
			}
		} else {
			leftClicked = false;
		}
	}
}
