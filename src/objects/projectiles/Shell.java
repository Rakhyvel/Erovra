package objects.projectiles;

import main.UnitID;
import objects.Nation;
import objects.gui.Image;
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
	Image shell = new Image("/res/projectiles/shell.png", 4, 4);
	Image shadow;

	public Shell(Point position, Nation nation, Point target) {
		super(position, nation);
		speed = 1.5f;
		setAttack(0);
		this.velocity = velocity.normalize().scalar(getSpeed());
		this.setTarget(target);
		id = UnitID.SHELL;
		distance = (float)position.getDistSquared(target);
		shadow = new Image(shell);
		shadow.setOpacity(0.1f);
		shadow.shadowify();
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
		r.drawImage((int) position.getX(), (int) position.getY(), shadow.resize(0.5f), 0);
		r.drawImage((int) position.getX(), (int) (position.getY()-(16*scale)), shell.resize((float)(scale/2.0+0.5f)),0);
	}
}
