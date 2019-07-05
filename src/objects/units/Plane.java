package objects.units;

import main.Main;
import main.SelectionID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.projectiles.Bomb;
import objects.projectiles.Bullet;
import output.Render;
import terrain.Map;
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

	public Plane(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .9f;
			setDefense(1f);
			patrolPoint = new Point(nation.enemyNation.capital.getPosition())
					.addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96));
		} else if (weight == UnitID.MEDIUM) {
			speed = .6f;
			setDefense(1f);
		} else {
			speed = 0.3f;
			setDefense(1f);
			acquireTarget();
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
				aaAim();
				if (getTarget().getX() == -1) {
					nation.unitArray.remove(this);
					nation.coins += 10;
				}
//				aaAim();
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
						nation.addProjectile(new Bomb(position.addPoint(new Point(0, 16)), nation));
						bombsAway = true;
						removeSelect();
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
		Vector determinateVector = new Vector(0, 0);
		// x = x * cos(pi/2)-y*sin(pi/2)
		// y = x * sin(pi/2)+y*cos(pi/2)
		determinateVector
				.setX((float) (velocity.getX() * Math.cos(Math.PI / 2) - velocity.getY() * Math.sin(Math.PI / 2)));
		determinateVector
				.setY((float) (velocity.getX() * Math.sin(Math.PI / 2) + velocity.getY() * Math.cos(Math.PI / 2)));

		if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) > -0.5) {
			// Determines if plane should turn or go straight
			if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) < 0.999) {
				// Determines whether its closer to turn left or right
				if (determinateVector.dot(targetVector) > 0) {
					a += 0.03f * getSpeed();
				} else {
					a -= 0.03f * getSpeed();
				}
			}
		} else {
			// (#48) This part of the code makes sure that attacker aircraft take longer
			// turns, so that they have longer to fire
			if (weight == UnitID.MEDIUM) {
				a += 0.01f * getSpeed();
			} else {
				a += 0.015f * getSpeed();
			}
		}
		setTarget(position.addPoint(new Point(Trig.sin(a), Trig.cos(a))));

	}

	void recon() {
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < 4084) {
				tempUnit.engage();
			}
		}
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
			if (velocity.normalize().dot(position.subVec(patrolPoint).normalize()) > 0.95
					&& position.getDist(smallestPoint) < 73728) {
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

			if (getWeight() == UnitID.LIGHT) {
				recon();
			}
		}
	}

	/**
	 * Decides what building bombers should target
	 */
	void acquireTarget() {
		int smallestDistance = 1310720;
		Point smallestPoint = nation.enemyNation.capital.getPosition();
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if (((nation.airSupremacy < nation.enemyNation.airSupremacy && tempUnit.id == UnitID.AIRFIELD)
					|| (nation.seaSupremacy < nation.enemyNation.seaSupremacy && tempUnit.id == UnitID.PORT)
					|| (nation.landSupremacy < nation.enemyNation.landSupremacy && tempUnit.id == UnitID.FACTORY)
					|| ((nation.airSupremacy >= nation.enemyNation.airSupremacy
							&& nation.seaSupremacy >= nation.enemyNation.seaSupremacy
							&& nation.landSupremacy >= nation.enemyNation.landSupremacy) && tempUnit.id == UnitID.CITY))
					&& !tempUnit.capital) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
				}
			}
		}
		setTarget(new Point(smallestPoint).addPoint(new Point(0, -16)));
	}

	@Override
	public void render(Render r) {
		float direction = position.subVec(getTarget()).getRadian();
		if (velocity.getY() > 0)
			direction += 3.14f;
		if (!nation.isAIControlled()) {
			if (weight == UnitID.HEAVY && !bombsAway) {
				if (isSelected()) {
					r.drawImage((int) target.getX(), (int) target.getY(), 32,
							Render.getScreenBlend(nation.color, r.target), 1, 0);
					r.drawLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0, 1);
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY()) || Main.world.getShowPaths()) {
					r.drawLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color,
							220 << 16 | 220 << 8 | 220, 1);
				}
			} else if (weight == UnitID.MEDIUM || weight == UnitID.LIGHT) {
				if (isSelected()) {
					r.drawImage((int) secondaryTarget.getX(), (int) secondaryTarget.getY(), 32,
							Render.getScreenBlend(nation.color, r.target), 1, 0);
					r.drawLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0, 1);
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY()) || Main.world.getShowPaths()) {
					r.drawImage((int) secondaryTarget.getX(), (int) secondaryTarget.getY(), 32,
							Render.getScreenBlend(nation.color, r.target), 1, 0);
				}
			}
		}
		double distance = 32 * Math.min(-Map.getArray((int) position.getX(), (int) position.getY() + 16) + 1.5, 1);
		double scale = Math.max(1 / 2 * (Map.getArray((int) position.getX(), (int) position.getY() + 16) - 0.5) + 0.75,
				0.75);
		if (weight == UnitID.LIGHT) {
			r.drawImage((int) position.getX(), (int) (position.getY() + distance), (int) (scale * 36),
					r.resize(r.fighterShadow, scale, 36, 35), 1, direction);
			if (Main.ticks % 4 < 2) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.fighter1), 1, direction);
			} else {
				r.drawImage((int) position.getX(), (int) position.getY(), 36,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.fighter2), 1, direction);
			}
			if (hit > 1)
				r.drawImage((int) position.getX(), (int) position.getY(), 40,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.fighterHit), 1, direction);
		} else if (weight == UnitID.MEDIUM) {
			r.drawImage((int) position.getX(), (int) (position.getY() + distance), (int) (scale * 44),
					r.resize(r.attackerShadow, scale, 44, 33), 1, direction);
			if (Main.ticks % 4 < 2) {
				r.drawImage((int) position.getX(), (int) position.getY(), 44,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.attacker1), 1, direction);
			} else {
				r.drawImage((int) position.getX(), (int) position.getY(), 44,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.attacker2), 1, direction);
			}
			if (hit > 1)
				r.drawImage((int) position.getX(), (int) position.getY(), 48,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.attackerHit), 1, direction);
		} else {
			r.drawImage((int) position.getX(), (int) (position.getY() + distance), (int) (scale * 68),
					r.resize(r.bomberShadow, scale, 68, 40), 1, direction);
			if (Main.ticks % 4 < 2) {
				r.drawImage((int) position.getX(), (int) position.getY(), 68,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.bomber1), 1, direction);
			} else {
				r.drawImage((int) position.getX(), (int) position.getY(), 68,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.bomber2), 1, direction);
			}
			if (hit > 1)
				r.drawImage((int) position.getX(), (int) position.getY(), 72,
						Render.getScreenBlend(Render.getColor(weight, nation.color), r.bomberHit), 1, direction);
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
	}

	void clickToPatrol() {
		if ((Main.world.selectedUnits.isEmpty() || Main.world.selectionMethod == SelectionID.MULTI) && Main.world.selectionMethod != SelectionID.BOX) {
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
					Main.world.selectedUnits.add(this);
				}
				leftClicked = false;
			} else {
				if (Main.world.selectionMethod != SelectionID.MULTI)
					selected = false;
				leftClicked = false;
			}
		} else if (Main.world.selectedUnits.contains(this)) {
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
