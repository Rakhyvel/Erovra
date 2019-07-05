package objects.units;

import java.util.Random;

import main.Main;
import main.SelectionID;
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
	public Point pathfind;
	public boolean straightfire = false;
	private boolean accountedFor = false;

	protected int hit = 0;
	protected int spotted = 0;
	private float defense;
	protected float speed;
	protected float health;

	protected UnitID id;
	protected UnitID weight;
	private UnitID closestUnit;

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
		pathfind = new Point(position);
		born = Main.ticks;
	}

	public Unit(Unit unit) {
		this.position = unit.position;
		this.nation = unit.nation;
		this.velocity = new Vector(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
		health = 10;
		if (nation.name.contains("Russia"))
			health += 10;
		setTarget(position.addPoint(new Point(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f)));
		setFacing(position);
		born = Main.ticks;
	}

	/**
	 * If things goes well, is called 60 times a second
	 * 
	 * @param t Time since last call, in millis
	 */
	public abstract void tick(double t);

	/**
	 * Called when game draws
	 * 
	 * @param r Instance of canvas
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

	public void setPosition(Point pos) {
		if (pos != null && this.position != null) {
			this.position = new Point(pos);
		} else {
			this.position = pos;
		}
	}

	/**
	 * @return Unit's defense
	 */
	public float getDefense() {
		return defense;
	}

	public void setDefense(float defense) {
		this.defense = defense;
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
		if (!engaged) {
			engaged = true;
			nation.engagedUnits.add(this);
			spotted = (int) (60 / speed);
			if (!accountedFor) {
				if (id == UnitID.INFANTRY || id == UnitID.CAVALRY || id == UnitID.ARTILLERY) {
					nation.landEngagedSupremacy++;
				}
				if (id == UnitID.SHIP && weight != UnitID.LIGHT) {
					nation.seaEngagedSupremacy++;
				}
				accountedFor = true;
			}
		}
	}

	/**
	 * Disengages unit
	 */
	public void disengage() {
		if (engaged) {
			nation.engagedUnits.remove(this);
			engaged = false;
		}
	}

	/**
	 * Sets the highlight ticker
	 * 
	 * @param hit The amount the ticker should be set
	 */
	public void setHit(int hit) {
		this.hit = hit;
	}

	/**
	 * Sets the weight class of an object
	 * 
	 * @param weight The weight class
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
	 * @param selected Whether or not the unit is selected
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
	 * @param boarded Whether or not the unit is aboard a landing craft
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
	 * @param target The target of the unit
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
	 * @param facing The point the unit should face
	 */
	public void setFacing(Point facing) {
		if (facing != null && this.facing != null) {
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

	// This function removes the unit from selection. Used when units are
	// removed from game.
	void removeSelect() {
		if (!Main.world.selectedUnits.isEmpty()) {
			Main.world.selectedUnits.remove(this);
		}
	}

	/**
	 * Returns the position the unit would be at if it were to advance at to the
	 * target given. Used in pathfinding to detect if the unit will cross into
	 * terrain it cannot move through
	 * 
	 * @param target The position the unit should go to
	 * @return The position will be at next tick
	 */
	Point getNextStep(Point goal) {
		return position.addVector(position.getTargetVector(goal).normalize().scalar(speed));
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

	public Point pathfind(Point enemy, float lowerLimit) {
		// Returns enemy if direct path, a point of pathfind is required, and null if no
		// pathfind could be found
		if (clearPath(position, enemy, lowerLimit))
			return enemy;
		Point midpoint = getObstacle(position, enemy, lowerLimit);
		if (midpoint == null || midpoint.equals(position)) {
			return enemy;
		}
		return perpendicularize(midpoint, position, lowerLimit);
	}

	public static boolean clearPath(Point start, Point end, float lowerLimit) {
		Vector march = start.subVec(end).normalize();
		Point step = new Point(start);
		boolean clear = true;
		for (int i = 0; i < start.getDistSquared(end); i++) {
			clear &= Map.getArray(step) < lowerLimit + 0.5 && Map.getArray(step) > lowerLimit;
			step = step.addVector(march);
			if (!clear)
				return false;
		}
		return clear;
	}

	Point getObstacle(Point start, Point end, float lowerLimit) {
		Vector march = start.subVec(end).normalize().scalar(0.5f);
		Point step = new Point(start);
		Point startPoint = null;
		// Clear is whether or not the ray is clear
		boolean clear = true;
		while (step.getDistSquared(end) > 1) {
			if (clear && (Map.getArray(step) <= lowerLimit || Map.getArray(step) > lowerLimit + 0.5)) {
				// If previous was clear, and this step is not, this must be the
				// begining of the
				// first obstacle
				startPoint = new Point(step);
			}
			if (Map.getArray(step) > lowerLimit && Map.getArray(step) < lowerLimit + 0.5 && startPoint != null) {
				// If ray was inside an obstacle, but is now free, this must be
				// the end of the
				// obstacle. Return midpoint.
				return step.getMidPoint(startPoint);
			}
			// Check clarity, advance step
			clear &= Map.getArray(step) <= lowerLimit + 0.5 && Map.getArray(step) >= lowerLimit;
			step = step.addVector(march);
		}
		// If there was no obstacle, return null for midpoint
		return null;
	}

	Point perpendicularize(Point midPoint, Point start, float lowerLimit) {
		if (midPoint.getY() == start.getY())
			start.setY(start.getY() + 1);
		double slope = -1 * (midPoint.getX() - start.getX()) / (midPoint.getY() - start.getY());
		double displacement = 5;
		boolean aTooFar = false;
		boolean bTooFar = false;
		while (displacement > 0) {
			Point a = new Point(midPoint);
			Point b = new Point(midPoint);
			if (!aTooFar) {
				a.setX(a.getX() + (displacement / Math.sqrt(slope * slope + 1)));
				a.setY(a.getY() + (slope * displacement / Math.sqrt(slope * slope + 1)));
				if (a.getX() < 0 || a.getX() > 1025 || a.getY() < 0 || a.getY() > 513) {
					// If a is out of bounds, set this to true. It is impossible
					// for it to reset
					aTooFar = true;
				}
				if (clearPath(start, a, lowerLimit) && !aTooFar) {
					// If there is a clear path to probe a, return probe a
					return a;
				}
			}
			if (!bTooFar) {
				b.setX(b.getX() + (-displacement / Math.sqrt(slope * slope + 1)));
				b.setY(b.getY() + (slope * -displacement / Math.sqrt(slope * slope + 1)));
				if (b.getX() < 0 || b.getX() > 1025 || b.getY() < 0 || b.getY() > 513) {
					// If b is out of bounds, set this to true. It is impossible
					// for it to reset
					bTooFar = true;
				}
				if (clearPath(start, b, lowerLimit) && !bTooFar) {
					// If there is a clear path to probe b, return probe b
					return b;
				}
			}
			if (aTooFar & bTooFar) {
				// If both probes are out of bounds, return false
				return null;
			}
			displacement += 1;
		}
		// If in shadow, return null
		return null;
	}

	public boolean findTarget(float baseline, int range) {
		closestUnit = null;
		int smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		int smallestEngagedDistance = smallestDistance;
		Point smallestEngagedPoint = new Point(-1, -1);
		if (!(id == UnitID.ARTILLERY && weight == UnitID.LIGHT)) {
			for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
				Unit tempUnit = nation.enemyNation.getUnit(i);
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (((tempUnit.id == UnitID.SHIP) == (id == UnitID.SHIP))
						&& ((tempUnit.id == UnitID.PLANE) == (id == UnitID.ARTILLERY && weight == UnitID.LIGHT))) {
					if (tempDist < smallestDistance) {
						smallestDistance = tempDist;
						smallestPoint = tempPoint;
						smallestUnit = tempUnit;
					}
					if ((tempDist < smallestEngagedDistance
							|| (tempUnit.id.isLandUnit() && smallestUnit.id.isBuilding())) && tempUnit.engaged) {
						smallestEngagedDistance = tempDist;
						smallestEngagedPoint = tempPoint;
					}
				}
			}
		}
		if (nation.isAIControlled()) {
			if (smallestEngagedPoint.getX() != -1) {
				// If there was an enemy visible to the nation:
				if (position.getDist(target) < 1) {
					pathfind = pathfind(smallestEngagedPoint, baseline);
				} else {
					pathfind = target;
				}
				if (pathfind != null) {
					setTarget(pathfind);
					setFacing(pathfind);
				} else {
					retarget(baseline);
				}
			} else {
				// Enemy visible: no
				retarget(baseline);
			}
		}
		if (smallestDistance < range) {
			pathfind = pathfind(smallestPoint, baseline);
			if (pathfind != null) {
				if (nation.isAIControlled()) {
					setTarget(pathfind);
					setFacing(pathfind);
				} else {
					setFacing(smallestPoint);
				}
				smallestUnit.engage();
				engage();
			} else {
				if (nation.isAIControlled())
					retarget(baseline);
			}
			closestUnit = smallestUnit.id;
			return smallestPoint.equals(pathfind);
		} else {
			// (#55) This is to make sure units don't stay fixated on a target after its
			// died or gone out of range
			if (!getTarget().equals(position))
				setFacing(getTarget());
		}
		return false;
	}

	public void shootBullet(float cal) {
		if (findTarget(0.5f, 2048)) {
			if ((Main.ticks - born) % 60 == 0) {
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(facing), cal * (health / 10),
						UnitID.BULLET));
			}
			if (nation.isAIControlled()
					|| (!(id == UnitID.CAVALRY && weight == UnitID.LIGHT) && !closestUnit.isBuilding())) {
				setTarget(position);
			}
		}
	}

	public void shootShell(int range, double power) {
		if (findTarget(0.5f, range)) {
			if ((Main.ticks - born) % 90 == 0) {
				nation.addProjectile(new Shell(position, power, nation, facing));
			}
			if (id == UnitID.SHIP)
				setFacing(getTarget());
			if (nation.isAIControlled())
				setTarget(position);
		}
	}

	public void shootTorpedo() {
		if (findTarget(0, 16384)) {
			if ((Main.ticks - born) % 90 == 0) {
				nation.addProjectile(new Torpedo(position, nation, position.getTargetVector(facing)));
			}
			// (#50) This makes it so that if the ship is stopped, it does not set facing to
			// the target, which would cause the ship to point north and look odd
			if (!getTarget().equals(position))
				setFacing(getTarget());
			if (nation.isAIControlled())
				setTarget(position);
		}
	}

	boolean aaAim() {
		int smallestDistance = 7372;
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
			smallestUnit.engage();
			engage();
			if ((Main.ticks - born) % 20 == 0) {
				nation.addProjectile(new Bullet(position, nation,
						position.getTargetVector(
								smallestPoint.addVector(smallestUnit.velocity.scalar(Math.sqrt(smallestDistance) / 4))),
						.1f, UnitID.AIRBULLET));
			}
			return true;
		}
		return false;
	}

	void targetMove(float baseline) {
		if (Map.withinBaseline(getNextStep(target), baseline) || (id == UnitID.SHIP && weight == UnitID.LIGHT)) {
			velocity = position.getTargetVector(target).normalize().scalar(speed);
			position = position.addVector(velocity);
			if (!nation.isAIControlled() && position.getDist(target) < 1) {
				setTarget(position);
			}
		} else {
			if (nation.isAIControlled()) {
				double angle = rand.nextFloat() * 2 * Math.PI;
				double start = angle;
				do {
					setTarget(position.addPoint(new Point(Math.cos(angle) * 32, Math.sin(angle) * 32)));
					angle += 0.1;
				} while (!clearPath(target, position, baseline) && angle < start + 6.28f);
				setFacing(target);
				velocity = position.getTargetVector(target).normalize().scalar(speed);
				position = position.addVector(velocity);
			} else {
				setTarget(position);
			}
		}
		/*
		 * Needs to change pathfind once it gets there, not continously
		 */
	}

	/**
	 * Moves the unit along its velocity vector
	 */
	void velocityMove(Point goal) {
		if (position.getDist(goal) > 1) {
			velocity = position.getTargetVector(goal).normalize().scalar(speed);
			position = position.addVector(velocity);
		}
	}

	/**
	 * Checks enemy projectile array. If there are any close enough to the unit, it
	 * subtracts the unit's health by (projectile's attack)/(unit's defense)
	 */
	public void detectHit() {
		if (hit > 0)
			hit--;
		UnitID projectileID = UnitID.NONE;
		for (int i = 0; i < nation.enemyNation.projectileSize() && getID() != UnitID.NONE; i++) {
			double distance = position.getDist(nation.enemyNation.getProjectile(i).getPosition());
			Projectile tempProjectile = nation.enemyNation.getProjectile(i);
			projectileID = tempProjectile.getID();
			if (tempProjectile.id == UnitID.BOMB)
				distance /= 4;
			if (distance < 256 && !tempProjectile.equals(null) && tempProjectile.getAttack() > 0
					&& !((id != UnitID.PLANE) && (tempProjectile.getID() == UnitID.AIRBULLET))
					&& !((id != UnitID.SHIP) && tempProjectile.id == UnitID.TORPEDO)
					&& !(id == UnitID.PLANE && tempProjectile.id == UnitID.SHELL)
					&& !((id == UnitID.AIRFIELD || id == UnitID.FACTORY || id == UnitID.CITY || id == UnitID.PORT)
							&& tempProjectile.getID() == UnitID.ANTIPERSONEL)
					&& !(tempProjectile.getID() == UnitID.BOMB && capital)) {
				if (tempProjectile.id != UnitID.BOMB && tempProjectile.id != UnitID.SHELL)
					tempProjectile.hit();
				hit = 9;
				health -= tempProjectile.getAttack() / getDefense();
				if (health <= 0)
					break;
			}
		}
		if (health <= 0) {
			if (capital) {
				System.out.println(nation.name + " has lost! This took:");
				System.out.println(Main.ticks / 3600 + " minutes!");
				nation.defeat();
			}
			health = 0;
			if (id == UnitID.PLANE && getWeight() == UnitID.LIGHT) {
				nation.airSupremacy--;
			}
			if (id == UnitID.SHIP && getWeight() != UnitID.LIGHT) {
				nation.seaSupremacy--;
				nation.seaEngagedSupremacy--;
			}
			if (id == UnitID.CITY) {
				if (nation.getCityCost() >= 1)
					nation.setCityCost(nation.getCityCost() / 2);
				if (projectileID != UnitID.BOMB) {
					nation.enemyNation.addUnit(new City(position, nation.enemyNation, Main.ticks));
					nation.enemyNation.setCityCost(nation.enemyNation.getCityCost() * 2);
				}
			} else if (id == UnitID.FACTORY) {
				if (nation.getFactoryCost() >= 30)
					nation.setFactoryCost(nation.getFactoryCost() / 2);
				if (projectileID != UnitID.BOMB) {
					nation.enemyNation.addUnit(new Factory(position, nation.enemyNation));
					nation.enemyNation.setFactoryCost(nation.enemyNation.getFactoryCost() * 2);
				}
			} else if (id == UnitID.PORT) {
				if (nation.getPortCost() > 20)
					nation.setPortCost(nation.getPortCost() / 2);
				if (projectileID != UnitID.BOMB) {
					nation.enemyNation.addUnit(new Port(position, nation.enemyNation));
					nation.enemyNation.setPortCost(nation.enemyNation.getPortCost() * 2);
				}
			} else if (id == UnitID.AIRFIELD) {
				if (nation.getAirfieldCost() > 20)
					nation.setAirfieldCost(nation.getAirfieldCost() / 2);
				if (projectileID != UnitID.BOMB) {
					nation.enemyNation.addUnit(new Airfield(position, nation.enemyNation));
					nation.enemyNation.setAirfieldCost(nation.enemyNation.getAirfieldCost() * 2);
				}
			} else if (id == UnitID.INFANTRY || id == UnitID.CAVALRY || id == UnitID.ARTILLERY) {
				nation.setLandSupremacy(-1);
				nation.landEngagedSupremacy--;
			}
			removeSelect();
			nation.removeUnit(this);
			nation.engagedUnits.remove(this);
			if (!nation.isAIControlled())
				if (Main.world.getDropDown().isUnit(this))
					Main.world.getDropDown().shouldClose();
		}
	}

	/**
	 * Determines if the user clicked on the unit, if so, targets the users next
	 * click.
	 */
	void clickToMove() {
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
				if(Main.world.selectionMethod != SelectionID.MULTI)
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
				getTarget().setX(Main.mouse.getX());
				getTarget().setY(Main.mouse.getY());
				setFacing(getTarget());
				Main.world.nullifySelected();
			}
			if (Main.mouse.getMouseRightDown()) {
				if (position.getDist(target) < 1)
					setFacing(new Point(Main.mouse.getX(), Main.mouse.getY()));
				selected = false;
				Main.world.nullifySelected();
			}
		} else {
			leftClicked = false;
		}
	}

	/**
	 * Determines if the user has right clicked on the unit. If so, triggers a drop
	 * down menu
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
		if (Main.world.getDropDown().getShown())
			return false;
		double angle = position.subVec(getFacing()).getRadian();
		if (getID() == UnitID.INFANTRY || getID() == UnitID.ARTILLERY || getID() == UnitID.CAVALRY) {
			return Math.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 8
					&& Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x)
							+ Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 16;
		} else if (getID() == UnitID.SHIP) {
			if (getWeight() == UnitID.LIGHT) {
				return Math.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 16
						&& Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x)
								+ Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 13.5;
			} else if (getWeight() == UnitID.MEDIUM) {
				return Math
						.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 22.5
						&& Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x)
								+ Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 13.5;
			} else {
				return Math
						.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 30.5
						&& Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x)
								+ Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 8;
			}
		}
		return Math.abs(Math.sin(angle) * (position.getX() - x) + Math.cos(angle) * (position.getY() - y)) < 16
				&& Math.abs(Math.sin(angle + Math.PI / 2) * (position.getX() - x)
						+ Math.cos(angle + Math.PI / 2) * (position.getY() - y)) < 16;
	}

	/**
	 * Changes the target of the unit to a random one
	 */
	void retarget(float baseline) {
		if (position.getDist(target) < 1) {
			double angle = rand.nextFloat() * 2 * Math.PI;
			double start = angle;
			do {
				setTarget(position.addPoint(new Point(Math.cos(angle) * 50, Math.sin(angle) * 50)));
				angle += 0.1;
			} while (!clearPath(target, position, baseline) && angle < start + 6.28f);
			setFacing(target);
		}
	}
}