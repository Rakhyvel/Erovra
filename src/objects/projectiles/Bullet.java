package objects.projectiles;

import main.UnitID;
import objects.Nation;
import objects.gui.Image;
import output.Render;
import utility.Point;
import utility.Vector;

/**
 * Handles the logic and rendering for bullets
 * 
 * @author Rakhyvel
 * @see Projectile
 *
 */
public class Bullet extends Projectile {
	Image bullet = new Image("/res/projectiles/bullet.png", 2, 2);

	public Bullet(Point position, Nation nation, Vector velocity, float cal, UnitID id) {
		super(position, nation);
		speed = 4f;
		setAttack(cal);
		this.velocity = velocity.normalize().scalar(getSpeed());
		this.id = id;
	}

	@Override
	public void tick(double t) {
		if (position.getX() < -velocity.getX() || position.getX() > 1024 - velocity.getX()
				|| position.getY() < -velocity.getY() || position.getY() > 512 - velocity.getY()) {
			hit();
		}
		bulletMove();
	}

	@Override
	public void render(Render r) {
		r.drawImage((int) position.getX(), (int) position.getY(), bullet.getScreenBlend(nation.color),0);
	}
}
