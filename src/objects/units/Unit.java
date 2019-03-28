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

	public Point target;
	private Point facing;
	public Nation nation;
	protected boolean engaged = false;

	protected int hit = 0;
	protected int spotted = 0;
	private float defense;
	protected float speed;
	protected float health;

	protected UnitID id;
	protected UnitID weight;

	public boolean capital = false;
	private boolean boarded = false;
	protected int born;

	public boolean selected = false;
	boolean rightClicked = false;
	boolean leftClicked = false;

	public int dropDownHeight = 30;

	// USELESS, DELETE!

	public Unit(Point position, Nation nation, UnitID weight) {
		this.position = position;
		this.nation = nation;
		this.velocity = new Vector(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
		setWeight(weight);
		health = 10;
		if (nation.name.contains("Russia")) {
			health += Main.difficulty;
		} else {
			health -= Main.difficulty;
		}
		setTarget(position.addPoint(new Point(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f)));
		setFacing(position);
		born = Main.ticks;
	}

	public Unit(Unit unit) {
		this.position = unit.position;
		this.nation = unit.nation;
		this.velocity = new Vector(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
		health = 10;
		if (nation.name.contains("Russia")) health += 10;
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
		spotted = (int) (60 / speed);
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
	public void setWeight(UnitID weight) {
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
		if (target != null && this.target != null) {
			this.target = new Point(target);
		} else {
			this.target = target;
		}
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
		if(facing != null) {
		this.facing = new Point(facing);
		} else {
			this.facing = facing;
		}
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
	
	Point getLandingPoint(Point point1, Point point2, int depth) {
		boolean ocean = true;
		Point testPoint = new Point(-1, -1);
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;
			testPoint.setX((point1.getX() - point2.getX()) * invDepth + point2.getX());
			testPoint.setY((point1.getY() - point2.getY()) * invDepth + point2.getY());
			ocean &= (Map.getArray((int) testPoint.getX(), (int) testPoint.getY()) < .5);
			if (!ocean) {
				return testPoint;
			}
		}
		return new Point(position);
	}

	/**
	 * Finds the closest spotted enemy, if there are any, moves the unit to
	 * engage. If there are no spotted enemy, moves around randomly
	 */
	public void wander() {
		int smallestDistance = 327680;
		if (id == UnitID.SHIP) smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize() && !(id == UnitID.ARTILLERY && weight == UnitID.LIGHT); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if (tempUnit.engaged) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance && ((tempUnit.id == UnitID.SHIP) == (id == UnitID.SHIP)) && (tempUnit.id != UnitID.PLANE) && !((id != UnitID.INFANTRY) && (tempUnit.capital)) && tempUnit.getID() != UnitID.NONE) {
					if (id != UnitID.SHIP) {
						if (clearPath(tempPoint, 32)) {
							smallestDistance = tempDist;
							smallestPoint = tempPoint;
							smallestUnit = tempUnit;
						}
					} else {
						if (wetPath(tempPoint, 32)) {
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
			setFacing(smallestPoint);
		} else {
			if (position.getDist(getTarget()) < 1) retarget();
			if (id == UnitID.INFANTRY) settle();

			if (id == UnitID.SHIP && weight != UnitID.LIGHT) {
				smallestDistance = 327680;
				smallestPoint = new Point(-1, -1);
				for (int i = 0; i < nation.unitSize(); i++) {
					Unit tempUnit = nation.getUnit(i);
					Point tempPoint = tempUnit.target.addPoint(tempUnit.getPosition()).multScalar(0.5);
					int tempDist = (int) position.getDist(tempPoint);
					if (tempDist < smallestDistance && tempUnit.id == UnitID.SHIP && tempUnit.weight == UnitID.LIGHT && tempUnit.velocity.magnitude() > 0) {
						if (wetPath(tempPoint, 32)) {
							smallestDistance = tempDist;
							smallestPoint = tempUnit.target.addPoint(getLandingPoint(tempUnit.getPosition(),tempUnit.getTarget(),32)).multScalar(0.5);
							smallestUnit = tempUnit;
						}
					}
				}
				if (smallestUnit != null && target.getDist(smallestPoint) > 9400) {
					setTarget(smallestPoint);
					setFacing(getTarget());
				} else {
					if (position.getDist(getTarget()) < 1) retarget();
				}
			}
		}
	}

	/**
	 * If the unit is far enough away other ports and cities, builds either a
	 * city, port, or factory. Only builds max 2 airfields and 3 factories
	 */
	public void settle() {
		nation.buyCity(position);
		if(nation.getCityCost() > 30) {
			Point portPoint = new Point(((int) (position.getX() / 64)) * 64 + 32, ((int) (position.getY() / 64)) * 64 + 32);
			Point smallestPoint = new Point(-1, -1);
			int smallestDistance = 1310720;
			for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
				Unit tempUnit = nation.enemyNation.getUnit(i);
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance && (tempUnit.getID() != UnitID.SHIP && tempUnit.getID() != UnitID.PLANE)) {
					if (wetLandingPath(tempPoint, portPoint, 64)) {
						smallestPoint = tempPoint;
					}
				}
			}
			if (smallestPoint.getX() != -1 && Map.getArray(portPoint) < .5f && nation.checkProximity(portPoint) && !engaged) {
				nation.buyPort(position);
				target = portPoint;
				facing = target;
			}
			if (nation.getAirfieldCost() < nation.coins /2) nation.buyAirfield(position);
			if (nation.getFactoryCost() < nation.coins /2) nation.buyFactory(position);
		}
	}

	/**
	 * Moves the unit to its target
	 */
	public void targetMove() {
		if (id != UnitID.SHIP) {
			if ((Map.getArray(getNextStep(target)) > 0.5f && Map.getArray(position) > 0.5f) && (Map.getArray(getNextStep(target)) < 1f && Map.getArray(position) < 1f)) {
				velocityMove();
			} else {
				if (nation.isAIControlled()) {
					escapeEdge();
				} else {
					target = new Point(position);
				}
			}
		} else {
			if (getWeight() != UnitID.LIGHT) {
				if ((Map.getArray(getNextStep(target)) < 0.5f && Map.getArray(position) < 0.5f) && Map.getArray(getNextStep(target)) != -1) {
					velocityMove();
				} else {
					if (nation.isAIControlled()) {
						escapeEdge();
					} else {
						target = new Point(position);
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
			if (tempProjectile.id == UnitID.BOMB) distance /= 4;
			if (distance < 256 && !tempProjectile.equals(null) && tempProjectile.getAttack() > 0 && !((id != UnitID.PLANE) && (tempProjectile.getID() == UnitID.AIRBULLET)) && !((id != UnitID.SHIP) && tempProjectile.id == UnitID.TORPEDO) && !(id == UnitID.PLANE && tempProjectile.id == UnitID.SHELL) && !((id == UnitID.AIRFIELD || id == UnitID.FACTORY || id == UnitID.CITY || id == UnitID.PORT) && tempProjectile.getID() == UnitID.ANTIPERSONEL) && !(tempProjectile.getID() == UnitID.BOMB && capital)) {
				if (tempProjectile.id != UnitID.BOMB && tempProjectile.id != UnitID.SHELL) tempProjectile.hit();
				hit = 9;
				health -= tempProjectile.getAttack() / getDefense();

				if (health <= 0) {
					if (capital) {
						System.out.println(nation.name + " has lost! This took:");
						System.out.println(Main.ticks / 3600 + " minutes!");
						nation.defeat();
					}
					if (Main.world.selectedUnit != null) {
						if (selected || Main.world.selectedUnit.equals(this)) Main.world.selectedUnit = null;
					}
					health = 100;
					if (id == UnitID.PLANE && getWeight() == UnitID.LIGHT) nation.airSupremacy--;
					if (id == UnitID.SHIP && getWeight() != UnitID.LIGHT) nation.seaSupremacy--;
					if (id == UnitID.CITY) {
						if (nation.getCityCost() >= 1) nation.setCityCost(nation.getCityCost() / 2);
						if (tempProjectile.getID() != UnitID.BOMB) nation.enemyNation.addUnit(new City(position, nation.enemyNation, Main.ticks));
						nation.enemyNation.setCityCost(nation.enemyNation.getCityCost() * 2);
					} else if (id == UnitID.FACTORY) {
						if (nation.getFactoryCost() >= 30) nation.setFactoryCost(nation.getFactoryCost() / 2);
						if (tempProjectile.getID() != UnitID.BOMB) nation.enemyNation.addUnit(new Factory(position, nation.enemyNation));
						nation.enemyNation.setFactoryCost(nation.enemyNation.getFactoryCost() * 2);
					} else if (id == UnitID.PORT) {
						if (nation.getPortCost() > 20) nation.setPortCost(nation.getPortCost() / 2);
						if (tempProjectile.getID() != UnitID.BOMB) nation.enemyNation.addUnit(new Port(position, nation.enemyNation));
						nation.enemyNation.setPortCost(nation.enemyNation.getPortCost() * 2);
					} else if (id == UnitID.AIRFIELD) {
						if (nation.getAirfieldCost() > 20) nation.setAirfieldCost(nation.getAirfieldCost() / 2);
						if (tempProjectile.getID() != UnitID.BOMB) nation.enemyNation.addUnit(new Airfield(position, nation.enemyNation));
						nation.enemyNation.setAirfieldCost(nation.enemyNation.getAirfieldCost() * 2);
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
			setFacing(new Point(smallestPoint));
			if ((Main.ticks - born) % 60 == 0) {
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(smallestPoint), cal * (health / 10), UnitID.BULLET));
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
		int smallestDistance = 73728;
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
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(smallestPoint.addVector(smallestUnit.velocity.scalar(Math.sqrt(smallestDistance) / 4))), .1f, UnitID.AIRBULLET));
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
		if (smallestUnit != null && wetPath(smallestPoint, 32)) {
			smallestUnit.engage();
			if (nation.isAIControlled()) {
				if (smallestDistance < 9000) setTarget(position);
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
		int smallestDistance = range * range;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.position;
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && tempUnit.id != UnitID.PLANE && !tempUnit.isBoarded() && !tempUnit.capital) {
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
				smallestUnit = tempUnit;
			}
		}
		if (smallestPoint.getX() != -1) {
			smallestUnit.engage();
			if (nation.isAIControlled()) setTarget(position);
			setFacing(smallestPoint);
			engaged = true;
			if (Main.ticks % 90 == 0) {
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
				getTarget().setX(Main.mouse.getX());
				getTarget().setY(Main.mouse.getY());
				setFacing(getTarget());
				Main.world.nullifySelected();
			}
			if (Main.mouse.getMouseRightDown()) {
				if (position.getDist(target) < 1) setFacing(new Point(Main.mouse.getX(), Main.mouse.getY()));
				selected = false;
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
			if (boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
				rightClicked = true;
			}
		} else if (rightClicked) {
			Main.world.getDropDown().show(this);
			rightClicked = false;
		}
	}

	boolean boundingBox(double x, double y) {
		double angle = position.subVec(getFacing()).getRadian();
		if (getID() == UnitID.INFANTRY || getID() == UnitID.ARTILLERY || getID() == UnitID.CAVALRY) {
			return Math.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 8 && Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x) + Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 16;
		} else if (getID() == UnitID.SHIP) {
			if (getWeight() == UnitID.LIGHT) {
				return Math.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 16 && Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x) + Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 13.5;
			} else if (getWeight() == UnitID.MEDIUM) {
				return Math.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 22.5 && Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x) + Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 13.5;
			} else {
				return Math.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 30.5 && Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x) + Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 8;
			}
		}
		return Math.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 16 && Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x) + Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 16;
	}

	/**
	 * Changes the target of the unit to a random one
	 */
	void retarget() {
		if (id == UnitID.SHIP) {
			// int r = (int) (speed * 75.0f);
			a += rand.nextFloat() * speed - speed / 2;
			setTarget(position.addPoint(new Point(rand.nextInt(128) - 64, rand.nextInt(128) - 64)));
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
		float r = 1;
		float cutoffPoint = a + 6.28f;
		if (id != UnitID.SHIP) {
			while ((Map.getArray(getNextStep(target)) <= .5 || Map.getArray(getNextStep(target)) >= 1) && a < cutoffPoint) {
				r += 0.2;
				p1 = new Point(r * Trig.sin(a), r * Trig.cos(a));
				p2 = new Point(r * Trig.sin(a + 3.14f), r * Trig.cos(a + 3.14f));
				double d1 = Math.abs(Map.getArray(position.addPoint(p1)) - 0.75);
				double d2 = Math.abs(Map.getArray(position.addPoint(p2)) - 0.75);
				if (d1 < d2) {
					setTarget(position.addPoint(p1));
				} else {
					setTarget(position.addPoint(p2));
					a += 3.14;
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
				double d1 = Math.abs(Map.getArray(position.addPoint(p1)) - 0.25);
				double d2 = Math.abs(Map.getArray(position.addPoint(p2)) - 0.25);
				if (d1 < d2) {
					setTarget(position.addPoint(p1));
				} else {
					setTarget(position.addPoint(p2));
					a += 3.14;
				}
				a += 0.015f;
			}
			if (Map.getArray(getTarget()) < .5 && Map.getArray(getTarget()) > -1) {
				velocityMove();
			}
		}
	}

	public boolean clearPath(Point point, double depth) {
		boolean land = true;
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;

			float height = Map.getArray((int) ((point.getX() - position.getX()) * invDepth + position.getX()), (int) ((point.getY() - position.getY()) * invDepth + position.getY()));
			land &= (height > .5 && height < 1);
			if (!land) return land;
		}
		return land;
	}

	public boolean clearPath(Point point, Point point2, double depth) {
		boolean land = true;
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;
			float height = Map.getArray((int) ((point.getX() - point2.getX()) * invDepth + point2.getX()), (int) ((point.getY() - point2.getY()) * invDepth + point2.getY()));
			land &= (height > .5 && height < 1);
			if (!land) return land;
		}
		return land;
	}

	public boolean wetPath(Point point, double depth) {
		boolean ocean = true;
		Point testPoint = new Point(-1, -1);
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;
			testPoint.setX((point.getX() - position.getX()) * invDepth + position.getX());
			testPoint.setY((point.getY() - position.getY()) * invDepth + position.getY());
			ocean &= (Map.getArray((int) testPoint.getX(), (int) testPoint.getY()) < .5);
			if (!ocean) {
				return ocean;
			}
		}
		return ocean;
	}

	public boolean wetPath(Point point, Point point2, double depth) {
		boolean ocean = true;
		Point testPoint = new Point(-1, -1);
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;
			testPoint.setX((point.getX() - point2.getX()) * invDepth + point2.getX());
			testPoint.setY((point.getY() - point2.getY()) * invDepth + point2.getY());
			ocean &= (Map.getArray((int) testPoint.getX(), (int) testPoint.getY()) < .5);
			if (!ocean) {
				return ocean;
			}
		}
		return ocean;
	}

	public boolean wetLandingPath(Point point, double depth) {
		boolean ocean = true;
		Point testPoint = new Point(-1, -1);
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;
			testPoint.setX((point.getX() - position.getX()) * invDepth + position.getX());
			testPoint.setY((point.getY() - position.getY()) * invDepth + position.getY());
			ocean &= (Map.getArray((int) testPoint.getX(), (int) testPoint.getY()) < .5);
			if (!ocean) {
				return clearPath(testPoint, position, 16);
			}
		}
		return ocean;
	}

	public boolean wetLandingPath(Point point, Point point2, double depth) {
		boolean ocean = true;
		Point testPoint = new Point(-1, -1);
		for (int i = 1; i < depth; i++) {
			double invDepth = i / depth;
			testPoint.setX((point.getX() - point2.getX()) * invDepth + point2.getX());
			testPoint.setY((point.getY() - point2.getY()) * invDepth + point2.getY());
			ocean &= (Map.getArray((int) testPoint.getX(), (int) testPoint.getY()) < .5);
			if (!ocean) {
				return clearPath(point, testPoint, 16);
			}
		}
		return ocean;
	}

	public void setDefense(float defense) {
		this.defense = defense;
	}
}