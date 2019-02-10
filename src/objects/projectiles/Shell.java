package objects.projectiles;

import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for shell objects in game
 * 
 * @author Rakhyvel
 *
 */
public class Shell extends Projectile {

	public Shell(Point position, Nation nation, Point target) {
		super(position, nation);
		speed = 2f;
		setAttack(0);
		this.velocity = velocity.normalize().scalar(getSpeed());
		this.setTarget(target);
		id = UnitID.SHELL;
	}

	@Override
	public void tick(double t) {
		shellMove();
		if (position.getDist(getTarget()) < 1024) {
			setAttack(2);
		}
	}

	@Override
	public void render(Render r) {
		r.drawImageScreen((int) position.getX(), (int) position.getY(), 4, r.shell, nation.color, a);
	}
}
