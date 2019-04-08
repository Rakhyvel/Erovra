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
	
	float distance;

	public Shell(Point position, Nation nation, Point target) {	
		super(position, nation);
		speed = 1.5f;
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
		//r.drawImage((int) position.getX(), (int) position.getY(), shadow.resize(0.5f), 0);
		r.drawImage((int) position.getX(), (int) position.getY(), 4, r.shadowify(r.shell),1,0);
		r.drawImage((int) position.getX(), (int) (position.getY()-(16*scale)), (int)((scale/2.0+0.5f)*4), r.resize(r.shell,(scale/2.0+0.5f),4,4),1,0);
		//.resize((float)(scale/2.0+0.5f))
	}
}
