package objects.projectiles;

import main.UnitID;
import objects.Nation;
import output.Render;
import terrain.Map;
import utility.Point;
import utility.Vector;

public class Torpedo extends Projectile {

	public Torpedo(Point position, Nation nation, Vector velocity) {
		super(position, nation);
		speed = 2f;
		attack = 2;
		this.velocity = (velocity.normalize().scalar(getSpeed()));
		id = UnitID.TORPEDO;
	}

	public void tick(double t) {
		bulletMove();
		if (Map.getArray(position) > 0.5f) hit();

	}

	public void render(Render r) {
		r.drawImageScreen((int) position.getX(), (int) position.getY(), 1, r.torpedo, nation.color, velocity.getRadian());

	}

}
