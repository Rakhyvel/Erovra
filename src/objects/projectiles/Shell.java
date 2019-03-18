package objects.projectiles;

import main.Image;
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
	
	float distance;

	public Shell(Point position, Nation nation, Point target) {
		super(position, nation);
		speed = 3f;
		setAttack(0);
		this.velocity = velocity.normalize().scalar(getSpeed());
		this.setTarget(target);
		id = UnitID.SHELL;
		distance = (float)position.getDistSquared(target);
	}

	@Override
	public void tick(double t) {
		shellMove();
		if (position.getDist(getTarget()) < 6) {
			setAttack(1.5f);
		}
	}

	@Override
	public void render(Render r) {
		double scale = position.getDistSquared(target)/distance;
		scale = (-3 * (scale-.5) * (scale-.5)) + 1.25f;
		r.drawImage((int) position.getX(), (int) position.getY(), (int)(4*scale), Image.resize(r.shell, 4, (float)scale),0);
	}
}
