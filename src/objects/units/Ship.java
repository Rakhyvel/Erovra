package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import output.Render;
import terrain.Map;
import utility.Point;

public class Ship extends Unit {

	float cal;

	public Ship(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			defense = 1f;
			cal = 0.5f;
			if (nation.isAIControlled()) {
				target = nation.enemyNation.capital.position
						.addPoint(new Point(rand.nextInt(192) - 96, rand.nextInt(192) - 96));
				velocity = position.subVec(target).normalize().scalar(speed);
			}
		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			defense = 2;
			cal = 1f;
		} else {
			speed = .05f;
			defense = 3;
			cal = 2f;
		}
		id = UnitID.SHIP;
	}

	public void tick(double t) {
		if (!(nation.defeated || nation.enemyNation.defeated)) {
			detectHit();
			if (weight != UnitID.LIGHT) {
				engaged = torpedoAim() || aaAim();
				if (nation.isAIControlled()) {
					wander();
				} else {
					clickToMove();
				}
				targetMove();
			} else {
				engaged = aaAim();
				if (isLanded()) {
					if (getPassenger1() != null) {
						getPassenger1().position = position;
						getPassenger1().setBoarded(false);
						if (getPassenger2() != null) {
							getPassenger2().position = position;
							getPassenger2().setBoarded(false);
						}
					}
					nation.unitArray.remove(this);
				}
				if (nation.isAIControlled()) {
					if (getPassenger2() != null) {
						position = position.addVector(velocity);
					} else {
						loadPassengers();
					}
				} else {
					clickToMove();
					targetMove();
					clickToDropDown();
				}
				if (health < 0) {
					nation.unitArray.remove(getPassenger1());
					nation.unitArray.remove(getPassenger2());
				}
			}
		}
	}

	// isLander(): checks to see if the boat has reached land, used for landind
	// craft
	boolean isLanded() {
		return Map.getArray(position) > 0.505f;
	}

	void loadPassengers() {
		int smallestDistance = 524288;
		Unit firstUnit = null;
		Unit secondUnit = null;
		setPassenger1(null);
		setPassenger2(null);
		int unitCount = 0;
		for (int i = 0; i < nation.unitSize(); i++) {
			Unit tempUnit = nation.getUnit(i);
			Point tempPoint = tempUnit.getPosition();
			int tempDist = (int) position.getDist(tempPoint);
			if (tempDist < smallestDistance && ((tempUnit.id == UnitID.CAVALRY) || (tempUnit.id == UnitID.INFANTRY)
					|| (tempUnit.id == UnitID.ARTILLERY)) && !tempUnit.engaged && !tempUnit.isBoarded()) {
				smallestDistance = tempDist;
				secondUnit = firstUnit;
				firstUnit = tempUnit;
				unitCount++;
			}
		}
		if (firstUnit != null && unitCount > 2) {
			firstUnit.setBoarded(true);
			setPassenger1(firstUnit);
			if (secondUnit != null) {
				secondUnit.setBoarded(true);
				setPassenger2(secondUnit);
			}
		}
	}

	public void render(Render r) {
		if (engaged || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) {
			float direction = position.subVec(target).getRadian();
			if (velocity.getY() > 0)
				direction += 3.14f;

			if (weight == UnitID.LIGHT)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.landing, r.lighten(nation.color),
						direction);
			if (weight == UnitID.MEDIUM)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 13, r.destroyer, nation.color,
						direction);
			if (weight == UnitID.HEAVY)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 16, r.cruiser, r.darken(nation.color),
						direction);

			if ((hit > 1 || isSelected()) && weight == UnitID.LIGHT)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 17, r.landingHit,
						r.lighten(nation.color), direction);
			if ((hit > 1 || isSelected()) && weight == UnitID.MEDIUM)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 17, r.destroyerHit, nation.color,
						direction);
			if ((hit > 1 || isSelected()) && weight == UnitID.HEAVY)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 20, r.cruiserHit,
						r.darken(nation.color), direction);
		}
	}
}