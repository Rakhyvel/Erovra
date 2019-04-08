package objects.projectiles;

import main.UnitID;
import objects.Nation;
import objects.gui.Image;
import output.Render;
import utility.Point;

/**
 * Handles logic and rendering for the bomb object
 * 
 * @author Rakhyvel
 * 
 * @see Projectile
 *
 */
public class Bomb extends Projectile {

	private int fall = 600;
	Image shadow;

	public Bomb(Point position, Nation nation) {
		super(position, nation);
		speed = 0;
		setAttack(0);
		this.velocity = velocity.normalize().scalar(getSpeed());
		id = UnitID.BOMB;
	}

	@Override
	public void tick(double t) {
		fall--;
		if (fall < 1) {
			setAttack(10);
		}
		if (fall < 0) {
			hit();
		}
	}

	@Override
	public void render(Render r) {
		double scale = (-1/360000.0)*(600.0-fall)*(600.0-fall)+1.1;
		if (fall > 1) {
			//r.drawImage((int) position.getX(), (int) position.getY(), 8, r.bomb, 1, 0);
			//bomb.shadow.resize(0.5)
			r.drawImage((int) position.getX(), (int) (position.getY()),8, r.resize(r.shadowify(r.bomb),0.5,16,8),1, 0);
			r.drawImage((int) position.getX(), (int) (position.getY()-(16*scale)),(int)((scale/2.0+0.5f)*16), r.resize(r.bomb,(scale/2.0+0.5f),16,8),1, 0);
			//.resize((float)(scale/2.0+0.5f))
		}
	}
}
