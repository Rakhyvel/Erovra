package objects.projectiles;

import main.UnitID;
import objects.Nation;
import objects.units.Unit;
import output.Render;
import utility.Point;
import utility.Vector;

public abstract class Projectile extends Unit {

	public float attack = 0;

	public Projectile(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
	}

	@Override
	public abstract void tick(double t);

	@Override
	public abstract void render(Render r);

	public void hit() {
		nation.projectileArray.remove(this);
		position = new Point(0, 0);
	}

	public void bulletMove() {
		position = position.addVector(velocity);
	}

	// shellMove(): moves the shell, when its reached its destination, it detonates.
	public void shellMove() {
		velocity = new Vector(0, 0);
		if (position.getDist(getTarget()) > 1) {
			velocity = position.getTargetVector(getTarget()).normalize().scalar(getSpeed());
		} else {
			hit();
		}
		position = position.addVector(velocity);
	}
}
