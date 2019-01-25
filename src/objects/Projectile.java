package objects;

import main.UnitID;
import output.Render;
import utility.Point;
import utility.Vector;

public abstract class Projectile extends Unit {

	float attack = 0;

	public Projectile(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
	}

	public abstract void tick(double t);

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
		if (position.getDist(target) > 1) {
			velocity = position.getTargetVector(target).normalize().scalar(speed);
		} else {
			hit();
		}
		position = position.addVector(velocity);
	}
}
