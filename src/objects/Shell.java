package objects;

import main.UnitID;
import output.Render;
import utility.Point;
import utility.Vector;

public class Shell extends Projectile {

	public Shell(Point position, Nation nation, Point target) {
		super(position, nation);
		speed = 2f;
		attack = 0;
		this.velocity = velocity.normalize().scalar(speed);
		this.target = target;
		id = UnitID.SHELL;
	}

	public void tick(double t) {
		shellMove();
		if (position.getDist(target) < 1024) {
			attack = 2;
		}
	}

	public void render(Render r) {
		r.drawImageScreen((int) position.getX(), (int) position.getY(), 4, r.shell, nation.color, velocity.getRadian());
	}
}
