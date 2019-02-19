package objects.projectiles;

import main.UnitID;
import objects.Nation;
import output.Render;
import terrain.Map;
import utility.Point;
import utility.Vector;

/**
 * Handles the logic and rendering for torpedos in game
 * 
 * @author Rakhyvel
 *
 */
public class Torpedo extends Projectile {

	public Torpedo(Point position, Nation nation, Vector velocity) {
		super(position, nation);
		speed = 2f;
		setAttack(2);
		this.velocity = (velocity.normalize().scalar(getSpeed()));
		id = UnitID.TORPEDO;
	}

	@Override
	public void tick(double t) {
		if (position.getX() < -velocity.getX() || position.getX() > 1024 - velocity.getX() || position.getY() < -velocity.getY() || position.getY() > 512 - velocity.getY()) {
			hit();
		}
		bulletMove();
		if (Map.getArray(position) > 0.5f) hit();

	}

	@Override
	public void render(Render r) {
		r.drawImageScreen((int) position.getX(), (int) position.getY(), 1, r.torpedo, nation.color, velocity.getRadian());

	}

}
