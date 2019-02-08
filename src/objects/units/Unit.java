package objects.units;

import java.util.Random;

import main.Main;
import main.UnitID;
import main.World;
import objects.Nation;
import objects.projectiles.Bullet;
import objects.projectiles.Projectile;
import objects.projectiles.Shell;
import objects.projectiles.Torpedo;
import output.Render;
import terrain.Map;
import utility.Point;
import utility.Trig;
import utility.Vector;

public abstract class Unit {

	protected Point position;
	private Point target;
	private Point facing;
	protected Vector velocity;
	public Nation nation;
	protected int hit = 0;
	protected float defense;
	protected float speed;
	float health;
	protected UnitID id;
	UnitID weight;
	Random rand = new Random();
	protected float a = rand.nextFloat() * (float) Math.PI * 2;
	boolean engaged = false;
	public boolean capital = false;
	private boolean boarded = false;
	int born;

	private boolean selected = false;
	boolean rightClicked = false;
	boolean leftClicked = false;

	private int start = 0;
	float maxStart = 1;
	private UnitID product;
	private UnitID productWeight;

	private Unit passenger1;
	private Unit passenger2;

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

	public void setHit(int hit) {
		this.hit = hit;
	}

	public void setWeight(UnitID weight) {
		this.weight = weight;
	}

	// wander(): Finds the closest spotted enemy, if there are any, moves the
	// unit to engage. If there are no spotted enemy, moves around randomly
	public void wander() {
		int smallestDistance = 131072;
		if (id == UnitID.SHIP)
			smallestDistance = 1310720;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize() && id != UnitID.ARTILLERY; i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			if (tempUnit.engaged) {
				Point tempPoint = tempUnit.getPosition();
				int tempDist = (int) position.getDist(tempPoint);
				if (tempDist < smallestDistance && ((tempUnit.id == UnitID.SHIP) == (id == UnitID.SHIP))
						&& (tempUnit.id != UnitID.PLANE) && !((id != UnitID.INFANTRY) && (tempUnit.capital))) {
					smallestDistance = tempDist;
					smallestPoint = tempPoint;
					smallestUnit = tempUnit;
				}
			}
		}
		if (smallestUnit != null) {
			setTarget(smallestPoint);
			setFacing(getTarget());
		} else {
			if (position.getDist(getTarget()) < 1)
				retarget();
			if (id == UnitID.INFANTRY && !engaged)
				settle();
		}
	}

	// settle(): if the unit is far enough away other ports and cities, it
	// builds either a city, port, or factory
	public void settle() {
		nation.buyCity(position);
		nation.buyAirfield(position);
		nation.buyPort(position);
		nation.buyFactory(position);
	}

	// targetMove(): Moves the unit to its target
	public void targetMove() {
		Point newPoint = position.addVector(velocity);
		if (id != UnitID.SHIP) {
			if (Map.getArray(newPoint) > 0.5f) {
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
				if (Map.getArray(newPoint) < 0.5f && Map.getArray(newPoint) != -1) {
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

	void velocityMove() {
		velocity = position.getTargetVector(getTarget()).normalize().scalar(speed);
		if (position.getDist(getTarget()) > 1)
			position = position.addVector(velocity);
	}

	// detectHit(): checks enemy projectile array. If there are any close enough
	// to the unit, it subtracts the unit's health by (projectile's
	// attack)/(unit's defense)
	public void detectHit() {
		if (hit > 0)
			hit--;
		for (int i = 0; i < nation.enemyNation.projectileSize(); i++) {
			double distance = position.getDist(nation.enemyNation.getProjectile(i).getPosition());
			Projectile tempProjectile = nation.enemyNation.getProjectile(i);
			if (distance < 256 && !tempProjectile.equals(null) && tempProjectile.attack > 0
					&& !((id != UnitID.PLANE) && (tempProjectile.attack < 0.5f))
					&& !(id != UnitID.SHIP && tempProjectile.id == UnitID.TORPEDO)
					&& !(id == UnitID.PLANE && tempProjectile.id == UnitID.SHELL)
					&& !((id == UnitID.AIRFIELD || id == UnitID.FACTORY || id == UnitID.CITY || id == UnitID.PORT)
							&& tempProjectile.attack == 0.75f)) {
				if (tempProjectile.id != UnitID.BOMB)
					tempProjectile.hit();
				hit = 8;
				health -= tempProjectile.attack / defense;
				if (health <= 0) {
					if (capital) {
						System.out.println(nation.name + " has lost! This took:");
						System.out.println(Main.ticks / 3600 + " minutes!");
						nation.defeat();
					}
					health = 100;
					if (id == UnitID.PLANE && getWeight() == UnitID.LIGHT)
						nation.airSupremacy--;
					if (id == UnitID.SHIP && getWeight() != UnitID.LIGHT)
						nation.seaSupremacy--;
					if (id == UnitID.CITY) {
						if (nation.cityCost > 10)
							nation.cityCost -= 7;
					} else if (id == UnitID.FACTORY) {
						if (nation.factoryCost > 15)
							nation.factoryCost -= 10;
					} else if (id == UnitID.PORT) {
						if (nation.portCost > 20)
							nation.portCost -= 10;
					} else if (id == UnitID.AIRFIELD) {
						if (nation.airfieldCost > 20)
							nation.airfieldCost -= 10;
					} else if (id == UnitID.INFANTRY) {
						nation.landSupremacy--;
					} else if (id == UnitID.CAVALRY) {
						nation.landSupremacy--;
					} else if (id == UnitID.ARTILLERY) {
						nation.landSupremacy--;
					}
					nation.unitArray.remove(this);
				}
			}
		}
	}

	// autoAim(float cal): Checks for the closest enemy, if there are any close
	// by, it stops and shoots them using the caliber of bullet specified, and
	// returns true. If there are no units to shoot, this method returns false.
	boolean autoAim(float cal) {
		int smallestDistance = 2048;
		if (id == UnitID.SHIP)
			smallestDistance = 35000;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempUnit.id == UnitID.PLANE)
				tempDist /= 36;
			if (tempUnit.id == UnitID.CITY || tempUnit.id == UnitID.AIRFIELD || tempUnit.id == UnitID.PORT
					|| tempUnit.id == UnitID.FACTORY)
				tempDist *= 2;
			if (tempDist < smallestDistance && ((tempUnit.id == UnitID.SHIP) == (id == UnitID.SHIP))
					&& !(id != UnitID.INFANTRY && tempUnit.capital) && tempUnit.id != UnitID.PLANE
					&& !tempUnit.isBoarded()) {
				smallestDistance = tempDist;
				smallestPoint = tempPoint;
				smallestUnit = tempUnit;
			}
		}
		if (smallestUnit != null) {
			smallestUnit.engaged = true;
			if (id != UnitID.SHIP && smallestUnit.id != UnitID.PLANE)
				if (nation.isAIControlled())
					setTarget(position);
			setFacing(smallestUnit.getPosition());
			if ((Main.ticks - born) % 60 == 0) {
				nation.addProjectile(new Bullet(position, nation, position.getTargetVector(smallestPoint), cal));
			}
			return true;
		}
		return false;
	}

	// aaAim(): find the closest airplane and shoots it. If there are no
	// airplanes,
	// returns false
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
				nation.addProjectile(new Bullet(position, nation,
						position.getTargetVector(
								smallestPoint.addVector(smallestUnit.velocity.scalar(Math.sqrt(smallestDistance) / 4))),
						.1f));
			}
			return true;
		}
		return false;
	}

	// torpedoAim(): checks for enemy boats nearby, if there are any, shoots a
	// torpedo at the closest one, and returns true. If there are no boats to
	// shoot, torpedoAim returns false.
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
				if (smallestDistance < 2048)
					setTarget(position);
			}
			if ((Main.ticks - born) % 90 == 0) {
				nation.addProjectile(new Torpedo(position, nation, position.getTargetVector(smallestPoint)));
			}
			return true;
		}
		return false;
	}

	// autoArtilleryAim(): Checks for enemies nearby, if there are any, shoots
	// an
	// artillery shell at them
	public boolean autoArtilleryAim(int range) {
		int smallestDistance = range * range * 2;
		Point smallestPoint = new Point(-1, -1);
		Unit smallestUnit = null;
		for (int i = 0; i < nation.enemyNation.unitSize(); i++) {
			Unit tempUnit = nation.enemyNation.getUnit(i);
			Point tempPoint = tempUnit.position;
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && tempDist > smallestDistance / 8 && tempUnit.id != UnitID.PLANE) {
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

	void clickToMove() {
		if (Main.world.selectedUnit == null) {
			if (Main.mouse.getMouseLeftDown()) {
				if (!Main.world.getDropDown().getShown()) {
					if (isSelected()) {
						leftClicked = true;
					} else if (position.getDist(new Point(Main.mouse.getX(), Main.mouse.getY() - 24)) < 512) {
						leftClicked = true;
					}
				}
			} else if (leftClicked) {
				setSelected(!isSelected());
				if (selected)
					Main.world.selectedUnit = this;
				leftClicked = false;
			}
		} else {
			if (Main.world.selectedUnit.equals(this)) {
				if (Main.mouse.getMouseLeftDown()) {
					getTarget().setX(Main.mouse.getX());
					getTarget().setY(Main.mouse.getY() - 24);
					setFacing(getTarget());
					Main.world.selectedUnit = null;
				}
			}
		}
	}

	void clickToDropDown() {
		if (Main.mouse.getMouseRightDown()) {
			if (position.getDist(new Point(Main.mouse.getX(), Main.mouse.getY() - 24)) < 512) {
				rightClicked = true;
			}
		} else if (rightClicked) {
			Main.world.getDropDown().show(this);
			rightClicked = false;
		}
	}

	// retarget(): changes the target of the unit to one on a circle at radius r
	// away
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

	// escapeEdge(): Tries to pathfind around obstacles such as the coast or the
	// world edge. Very buggy and can cause the game to slow
	void escapeEdge() {
		Point p1;
		Point p2;
		float r = 10;
		a += 1.57;
		float cutoffPoint = a + 6.28f;
		if (id != UnitID.SHIP) {
			while (Map.getArray(getTarget()) < .5 && a < cutoffPoint) {
				r += 0.1;
				p1 = new Point(r * Trig.sin(a), r * Trig.cos(a));
				p2 = new Point(r * Trig.sin(a + 3.14f), r * Trig.cos(a + 3.14f));
				if (Map.getArray(p1) < Map.getArray(p2)) {
					setTarget(position.addPoint(p1));
				} else {
					setTarget(position.addPoint(p2));
					a += 3.14;
				}
				a += 0.015f;
			}
		} else {
			while (Map.getArray(getTarget()) > .5 || Map.getArray(getTarget()) == -1 && a < cutoffPoint) {
				r += 0.1;
				p1 = new Point(r * Trig.sin(a), r * Trig.cos(a));
				p2 = new Point(r * Trig.sin(a + 3.14f), r * Trig.cos(a + 3.14f));
				if (Map.getArray(p1) > Map.getArray(p2) && Map.getArray(p1) != -1 && Map.getArray(p2) != -1) {
					setTarget(position.addPoint(p1));
				} else {
					setTarget(position.addPoint(p2));
					a += 3.14;
				}
				a += 0.015f;
			}
		}
		velocityMove();
	}

	public boolean buyUnit(UnitID product, UnitID productWeight, int cost, int time) {
		if (nation.coins >= cost) {
			nation.coins -= cost;
			this.setProduct(product);
			this.setProductWeight(productWeight);
			setStart(time);
			maxStart = getStart();
			return true;
		} else {
			this.setProduct(UnitID.NONE);
			this.setProductWeight(UnitID.NONE);
			setStart(1);
			maxStart = getStart();
		}
		return false;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isBoarded() {
		return boarded;
	}

	public void setBoarded(boolean boarded) {
		this.boarded = boarded;
	}

	public Unit getPassenger1() {
		return passenger1;
	}

	public void setPassenger1(Unit passenger1) {
		this.passenger1 = passenger1;
	}

	public Unit getPassenger2() {
		return passenger2;
	}

	public void setPassenger2(Unit passenger2) {
		this.passenger2 = passenger2;
	}

	public UnitID getProduct() {
		if (product == null)
			return UnitID.NONE;
		return product;
	}

	public void setProduct(UnitID product) {
		this.product = product;
	}

	public Point getTarget() {
		return target;
	}

	public void setTarget(Point target) {
		this.target = target;
	}

	public Point getFacing() {
		return facing;
	}

	public void setFacing(Point facing) {
		this.facing = facing;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public UnitID getWeight() {
		return weight;
	}

	public UnitID getProductWeight() {
		return productWeight;
	}

	public void setProductWeight(UnitID productWeight) {
		this.productWeight = productWeight;
	}
}