package objects.units;

import java.util.Random;

import main.Main;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.projectiles.Bullet;
import objects.projectiles.Projectile;
import objects.projectiles.Shell;
import objects.projectiles.Torpedo;
import output.Render;
import terrain.Map;
import utility.Point;
import utility.Trig;
import utility.Vector;

/**
 * Is the basis for most objects in the game. Handles AI as well as User control
 * 
 * @author Rakhyvel
 *
 */
public abstract class Unit {

	protected Random rand = new Random();

	protected Point position;
	protected Vector velocity;
	protected float a = rand.nextFloat() * (float) Math.PI * 2;

	private Point target;
	private Point facing;
	public Nation nation;
	protected boolean engaged = false;

	protected int hit = 0;
	protected float defense;
	protected float speed;
	protected float health;

	protected UnitID id;
	protected UnitID weight;

	public boolean capital = false;
	private boolean boarded = false;
	protected int born;

	private boolean selected = false;
	boolean rightClicked = false;
	boolean leftClicked = false;

	// USELESS, DELETE!

	public Unit(Point position, Nation nation, UnitID weight) {
		this.position = position;
		this.nation = nation;
		this.velocity = new Vector(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
		setWeight(weight);
		health = 10;
		setTarget(position.addPoint(new Point(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f)));
		setFacing(position);
		born = Main.ticks;
	}

	public Unit(Unit unit) {
		this.position = unit.position;
		this.nation = unit.nation;
		this.velocity = new Vector(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
		health = 10;
		setTarget(position.addPoint(new Point(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f)));
		setFacing(position);
		born = Main.ticks;
	}

	/**
	 * If things goes well, is called 60 times a second
	 * 
	 * @param t
	 *            Time since last call, in millis
	 */
	public abstract void tick(double t);

	/**
	 * Called when game draws
	 * 
	 * @param r
	 *            Instance of canvas
	 */
	public abstract void render(Render r);

	public abstract void dropDownDecide(DropDown d);

	public abstract void dropDownRender(Render r, DropDown d);

	/**
	 * @return Unit's position
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * @return Unit's defense
	 */
	public float getDefense() {
		return defense;
	}

	/**
	 * @return Unit's speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @return Unit's health
	 */
	public float getHealth() {
		return health;
	}

	/**
	 * @return Unit's ID
	 */
	public UnitID getID() {
		return id;
	}

	/**
	 * Engages unit
	 */
	public void engage() {
		engaged = true;
	}

	/**
	 * Disengages unit
	 */
	public void disengage() {
		engaged = false;
	}

	/**
	 * Sets the highlight ticker
	 * 
	 * @param hit
	 *            The amount the ticker should be set
	 */
	public void setHit(int hit) {
		this.hit = hit;
	}

	/**
	 * Sets the weight class of an object
	 * 
	 * @param weight
	 *            The weight class
	 */
	protected void setWeight(UnitID weight) {
		this.weight = weight;
	}

	/**
	 * @return Whether or not the unit is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets whether or not the unit is selected
	 * 
	 * @param selected
	 *            Whether or not the unit is selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return Whether or not the unit is aboard a landing craft
	 */
	public boolean isBoarded() {
		return boarded;
	}

	/**
	 * Sets whether or not the unit is aboard a landing craft
	 * 
	 * @param boarded
	 *            Whether or not the unit is aboard a landing craft
	 */
	public void setBoarded(boolean boarded) {
		this.boarded = boarded;
	}

	/**
	 * @return Unit's target
	 */
	public Point getTarget() {
		return target;
	}

	/**
	 * Sets the target of the unit
	 * 
	 * @param target
	 *            The target of the unit
	 */
	public void setTarget(Point target) {
		this.target = target;
	}

	/**
	 * @return The point the unit is facing
	 */
	public Point getFacing() {
		return facing;
	}

	/**
	 * Sets where the unit faces
	 * 
	 * @param facing
	 *            The point the unit should face
	 */
	public void setFacing(Point facing) {
		this.facing = facing;
	}

	/**
	 * @return Unit's weight class
	 */
	public UnitID getWeight() {
		return weight;
	}

	/**
	 * Returns the position the unit would be at if it were to advance at to the
	 * target given. Used in pathfinding to detect if the unit will cross into
	 * terrain it cannot move through
	 * 
	 * @param target
	 *            The position the unit should go to
	 * @return The position will be at next tick
	 */
	Point getNextStep(Point target) {
		return position.addVector(position.getTargetVector(target).normalize().scalar(speed));
	}

	/**
	 * Finds the closest spotted enemy, if there are any, moves the unit to
	 * engage. If there are no spotted enemy, moves around randomly
	 */
	public void wander() {
		int smallestDistance = 131072;
		if (id == UnitID.SHIP) smallestDistance = 131072000;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize() && id != UnitID.ARTILLERY; i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if (tempUnit.engaged) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance && ((tempUnit.id == UnitID.SHIP) == (id == UnitID.SHIP)) && (tempUnit.id != UnitID.PLANE) && !((id != UnitID.INFANTRY) && (tempUnit.capital)) && tempUnit.getID() != UnitID.NONE) {
					if (id != UnitID.SHIP) {
						if (clearPath(tempPoint, 8)) {
							smallestDistance = tempDist;
							smallestPoint = tempPoint;
							smallestUnit = tempUnit;
						}
					}
				}
			}
		}
		if (smallestUnit != null) {
			setTarget(smallestPoint);
			setFacing(getTarget());
		} else {
			if (position.getDist(getTarget()) < 1) retarget();
			if (id == UnitID.INFANTRY && !engaged) settle();
		}
	}

	/**
	 * If the unit is far enough away other ports and cities, builds either a
	 * city, port, or factory. Only builds max 2 airfields and 3 factories
	 */
	public void settle() {
		nation.buyCity(position);
		if (wetPath(nation.enemyNation.getUnit(0).getPosition(), 4)) nation.buyPort(position);
		nation.buyAirfield(position);
		if (nation.getFactoryCost() < 60) nation.buyFactory(position);
	}

	/**
	 * Moves the unit to its target
	 */
	public void targetMove() {
		if (id != UnitID.SHIP) {
			if ((Map.getArray(getNextStep(target)) > 0.5f && Map.getArray(position) > 0.5f) && (Map.getArray(getNextStep(target)) < 1f && Map.getArray(position) < 1)) {
				velocityMove();
			} else {
				if (nation.isAIControlled()) {
					escapeEdge();
				} else {
					position = position.addVector(velocity.scalar(-1));
				}
			}
		} else {
			if (getWeight() != UnitID.LIGHT) {
				if (Map.getArray(getNextStep(target)) < 0.5f && Map.getArray(getNextStep(target)) != -1) {
					velocityMove();
				} else {
					if (nation.isAIControlled()) {
						escapeEdge();
					} else {
						getTarget().setX(0);
					}
				}
			} else {
				velocityMove();
			}
		}
	}

	/**
	 * Moves the unit along its velocity vector
	 */
	void velocityMove() {
		if (position.getDist(getTarget()) > 0.1) {
			velocity = position.getTargetVector(getTarget()).normalize().scalar(speed);
			position = position.addVector(velocity);
		}
	}

	/**
	 * Checks enemy projectile array. If there are any close enough to the unit,
	 * it subtracts the unit's health by (projectile's attack)/(unit's defense)
	 */
	public void detectHit() {
		if (hit > 0) hit--;
		for (int i = 0; i < nation.enemyNation.projectileSize() && getID() != UnitID.NONE; i++) {
			double distance = position.getDist(nation.enemyNation.getProjectile(i).getPosition());
			Projectile tempProjectile = nation.enemyNation.getProjectile(i);
			if (distance < 256 && !tempProjectile.equals(null) && tempProjectile.getAttack() > 0 && !((id != UnitID.PLANE) && (tempProjectile.getAttack() < 0.5f)) && !(id != UnitID.SHIP && tempProjectile.id == UnitID.TORPEDO) && !(id == UnitID.PLANE && tempProjectile.id == UnitID.SHELL) && !((id == UnitID.AIRFIELD || id == UnitID.FACTORY || id == UnitID.CITY || id == UnitID.PORT) && tempProjectile.getAttack() == 0.75f)) {
				if (tempProjectile.id != UnitID.BOMB) tempProjectile.hit();
				hit = 8;
				health -= tempProjectile.getAttack() / defense;
				if (health <= 0) {
					if (capital) {
						System.out.println(nation.name + " has lost! This took:");
						System.out.println(Main.ticks / 3600 + " minutes!");
						nation.defeat();
					}
					System.out.println(Main.world.selectedUnit);
					if (Main.world.selectedUnit != null) {
						if (selected || Main.world.selectedUnit.equals(this)) Main.world.selectedUnit = null;
					}
					health = 100;
					if (id == UnitID.PLANE && getWeight() == UnitID.LIGHT) nation.airSupremacy--;
					if (id == UnitID.SHIP && getWeight() != UnitID.LIGHT) nation.seaSupremacy--;
					if (id == UnitID.CITY) {
						if (nation.getCityCost() > 1) nation.setCityCost(nation.getCityCost() - 21);
					} else if (id == UnitID.FACTORY) {
						if (nation.getFactoryCost() > 15) nation.setFactoryCost(nation.getFactoryCost() / 2);
					} else if (id == UnitID.PORT) {
						if (nation.getPortCost() > 20) nation.setPortCost(nation.getPortCost() / 2);
					} else if (id == UnitID.AIRFIELD) {
						if (nation.getAirfieldCost() > 20) nation.setAirfieldCost(nation.getAirfieldCost() / 2);
					} else if (id == UnitID.INFANTRY) {
						nation.setLandSupremacy(-1);
					} else if (id == UnitID.CAVALRY) {
						nation.setLandSupremacy(-1);
					} else if (id == UnitID.ARTILLERY) {
						nation.setLandSupremacy(-1);
					}
					nation.unitArray.remove(this);
				}
			}
		}
	}

	// autoAim(float cal):
	/**
	 * Checks for the closest enemy, if there are any close by, it stops and
	 * shoots them using the caliber of bullet specified, and returns true. If
	 * there are no units to shoot, this method returns false.
	 * 
	 * @param cal
	 *            The damage to be done by the bullet fired
	 * @return Whether or not there was an enemy
	 */
	boolean autoAim(float cal) {
		int smallestDistance = 2048;
		if (id == UnitID.SHIP) smallestDistance = 35000;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempUnit.id == UnitID.PLANE) tempDist /= 36;
			if (tempUnit.id == UnitID.CITY || tempUnit.id == UnitID.AIRFIELD || tempUnit.id == UnitID.PORT || tempUnit.id == UnitID.FACTORY) tempDist *= 2;
			if (tempDist < smallestDistance && ((tempUnit.id == UnitID.SHIP) == (id == UnitID.SHIP)) && !(id != UnitID.INFANTRY && tempUnit.capital) && tempUnit.id != UnitID.PLANE && !tempUnit.isBoarded() && tempUnit.getID() != UnitID.NONE) {
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
				smallestUnit = tempUnit;
			}
		}
		if (smallestUnit != null) {
			smallestUnit.engaged = true;
			if (id != UnitID.SHIP && smallestUnit.id != UnitID.PLANE) if (nation.isAIControlled()) setTarget(position);
			setFacing(smallestUnit.getPosition());
			if ((Main.ticks - born) % 60 == 0) {
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(smallestPoint), cal));
			}
			return true;
		}
		return false;
	}

	/**
	 * Finds the closest airplane and shoots it. If there are no airplanes,
	 * returns false.
	 * 
	 * @return True if enemy airplanes nearby, else false.
	 */
	boolean aaAim() {
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
			if ((Main.ticks - born) % 20 == 0) {
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(smallestPoint.addVector(smallestUnit.velocity.scalar(Math.sqrt(smallestDistance) / 4))), .1f));
			}
			return true;
		}
		return false;
	}

	// torpedoAim():
	/**
	 * Checks for enemy boats nearby, if there are any, shoots a torpedo at the
	 * closest one, and returns true. If there are no boats to shoot, torpedoAim
	 * returns false.
	 * 
	 * @return Whether or not there was a ship to be fired at.
	 */
	boolean torpedoAim() {
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
			if (nation.isAIControlled()) {
				if (smallestDistance < 2048) setTarget(position);
			}
			if ((Main.ticks - born) % 90 == 0) {
				nation.addProjectile(new Torpedo(position, nation, position.getTargetVector(smallestPoint)));
			}
			return true;
		}
		return false;
	}

	// autoArtilleryAim():
	/**
	 * Checks for enemies nearby, if there are any, shoots an artillery shell at
	 * them
	 * 
	 * @param range
	 *            How far (in pixels) the unit should see
	 * @return Whether or not there was a target
	 */
	public boolean autoArtilleryAim(int range) {
		int smallestDistance = range * range * 2;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.position;
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && tempDist > smallestDistance / 8 && tempUnit.id != UnitID.PLANE && !tempUnit.isBoarded()) {
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
				smallestUnit = tempUnit;
			}
		}
		if (smallestPoint.getX() != -1) {
			smallestUnit.engage();
			setFacing(smallestPoint);
			engaged = true;
			if (Main.ticks % 300 == 0) {
				nation.addProjectile(new Shell(position, nation, smallestPoint));
			}
			return true;
		}
		return false;
	}

	/**
	 * Determines if the user clicked on the unit, if so, targets the users next
	 * click.
	 */
	void clickToMove() {
		if (Main.world.selectedUnit == null) {
			// If there is no a unit currently selected
			if (Main.mouse.getMouseLeftDown() && !Main.world.getDropDown().getShown()) {
				// If mouse is down AND the dropdown isn't displayed
				if (isSelected() || position.getDist(new Point(Main.mouse.getX(), Main.mouse.getY() - 15)) < 512) {
					// If unit is already selected OR the unit is not selected,
					// and the mouse is
					// near
					// Set leftClicked to true
					leftClicked = true;
				}
			} else if (leftClicked) {
				// if the mouse isn't down, but leftClicked is true
				setSelected(!isSelected());
				if (selected) {
					System.out.println(Main.world.selectedUnit);
					Main.world.selectedUnit = this;
				}
				leftClicked = false;
			}
		} else if (Main.world.selectedUnit.equals(this)) {
			// If there is a unit being selected AND the selected unit is this
			// unit AND the
			// mouse is down
			if (Main.mouse.getMouseLeftDown()) {
				leftClicked = true;
			} else if (leftClicked) {
				getTarget().setX(Main.mouse.getX());
				getTarget().setY(Main.mouse.getY() - 15);
				setFacing(getTarget());
				Main.world.nullifySelected();
			}
		} else {
			leftClicked = false;
		}
	}

	/**
	 * Determines if the user has right clicked on the unit. If so, triggers a
	 * drop down menu
	 */
	void clickToDropDown() {
		if (Main.mouse.getMouseRightDown()) {
			if (position.getDist(new Point(Main.mouse.getX(), Main.mouse.getY() - 15)) < 512) {
				rightClicked = true;
			}
		} else if (rightClicked) {
			Main.world.getDropDown().show(this);
			rightClicked = false;
		}
	}

	/**
	 * Changes the target of the unit to a random one
	 */
	void retarget() {
		if (id == UnitID.SHIP) {
			int r = (int) (speed * 75.0f);
			a += rand.nextFloat() * speed - speed / 2;
			setTarget(position.addPoint(new Point(r * Math.sin(a), r * Math.cos(a))));
		} else {
			setTarget(position.addPoint(new Point(rand.nextInt(128) - 64, rand.nextInt(128) - 64)));
		}

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
		setFacing(getTarget());
	}

	/**
	 * Tries to pathfind around obstacles such as the coast or the world edge.
	 */
	void escapeEdge() {
		Point p1;
		Point p2;
		float r = 2;
		float cutoffPoint = a + 6.28f;
		if (id != UnitID.SHIP) {
			while (Map.getArray(getNextStep(target)) < .5 || Map.getArray(getNextStep(target)) > 1 && a < cutoffPoint) {
				r += 0.1;
				p1 = new Point(r * Trig.sin(a), r * Trig.cos(a));
				p2 = new Point(r * Trig.sin(a + 3.14f), r * Trig.cos(a + 3.14f));
				if (Map.getArray(position.addPoint(p1)) > Map.getArray(position.addPoint(p2)) && Map.getArray(position.addPoint(p1)) < 1) {
					setTarget(position.addPoint(p1));
				} else {
					if (Map.getArray(position.addPoint(p2)) < 1) {
						setTarget(position.addPoint(p2));
						a += 3.14;
					}
				}
				a += 0.015f;
			}
			if (Map.getArray(getTarget()) > .5 && Map.getArray(getTarget()) < 1) {
				velocityMove();
			}
		} else {
			while (Map.getArray(getNextStep(target)) > .5 || Map.getArray(getNextStep(target)) == -1 && a < cutoffPoint) {
				r += 0.1;
				p1 = new Point(r * Trig.sin(a), r * Trig.cos(a));
				p2 = new Point(r * Trig.sin(a + 3.14f), r * Trig.cos(a + 3.14f));
				if (Map.getArray(position.addPoint(p1)) < Map.getArray(position.addPoint(p2)) && Map.getArray(position.addPoint(p1)) != -1 && Map.getArray(position.addPoint(p2)) != -1) {
					setTarget(position.addPoint(p1));
				} else {
					setTarget(position.addPoint(p2));
					a += 3.14;
				}
				a += 0.015f;
			}
			if (Map.getArray(getTarget()) < .5) {
				velocityMove();
			}
		}
	}

	public boolean clearPath(Point point, double depth) {
		boolean land = true;
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;
			land &= (Map.getArray((int) ((point.getX() - position.getX()) * invDepth + position.getX()), (int) ((point.getY() - position.getY()) * invDepth + position.getY())) > .5);
			if (!land) return land;
		}
		return land;
	}

	public boolean wetPath(Point point, double depth) {
		boolean ocean = true;
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;
			ocean &= (Map.getArray((int) ((point.getX() - position.getX()) * invDepth + position.getX()), (int) ((point.getY() - position.getY()) * invDepth + position.getY())) < .5);
			if (!ocean) return ocean;
		}
		return ocean;
	}
}