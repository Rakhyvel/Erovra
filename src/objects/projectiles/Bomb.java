package objects.projectiles;

import main.Image;
import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

public class Bomb extends Projectile {

	int fall = 600;

	public Bomb(Point position, Nation nation) {
		super(position, nation);
		speed = 0;
		attack = 0;
		this.velocity = velocity.normalize().scalar(getSpeed());
		id = UnitID.BOMB;
	}

	@Override
	public void tick(double t) {
		fall--;
		if (fall < 1) {
			attack = 30;
		}
		if (fall < 0) {
			hit();
		}
	}

	@Override
	public void render(Render r) {
		double scale = (-1/360000.0)*(600.0-fall)*(600.0-fall)+1.0;
		if (fall > 1) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), (float)(16*scale), Image.resize(r.bomb, 16, (float)scale), 255);
		}
	}
}
