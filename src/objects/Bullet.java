package objects;

import main.UnitID;
import output.Render;
import utility.Point;
import utility.Vector;

public class Bullet extends Projectile {

	public Bullet(Point position, Nation nation, Vector velocity, float cal) {
		super(position, nation);
		speed = 4f;
		attack = cal;
		this.velocity = velocity.normalize().scalar(speed);
		id = UnitID.BULLET;
	}

	public void tick(double t) {
		if (position.getX() < -velocity.getX() || position.getX() > 1024 - velocity.getX() || position.getY() < -velocity.getY() || position.getY() > 512 - velocity.getY()) {
			hit();
		}
		bulletMove();
	}

	public void render(Render r) {
		r.drawImageScreen((int) position.getX(), (int) position.getY(), (int) (2 * attack), r.bullet, nation.color, 0);
	}
}
