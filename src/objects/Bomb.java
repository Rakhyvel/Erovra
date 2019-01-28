package objects;

import main.Image;
import main.UnitID;
import output.Render;
import utility.Point;

public class Bomb extends Projectile {

	int fall = 600;

	public Bomb(Point position, Nation nation) {
		super(position, nation);
		speed = 0f;
		attack = 0;
		this.velocity = velocity.normalize().scalar(speed);
		id = UnitID.BOMB;
	}

	public void tick(double t) {
		fall--;
		if (fall < 1) {
			attack = 30;
		}
		if (fall < 0) {
			hit();
		}
	}

	public void render(Render r) {
		double scale = (-1/360000.0)*(600.0-fall)*(600.0-fall)+1.0;
		if (fall > 1) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), (float)(16*scale), Image.resize(r.bomb, 16, (float)scale), 255);
		}
	}
}
