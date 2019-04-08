package objects.projectiles;

import main.Main;
import main.UnitID;
import objects.Nation;
import objects.gui.Image;
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
	private Point startingPoint;

	public Torpedo(Point position, Nation nation, Vector velocity) {
		super(position, nation);
		startingPoint = new Point(position);
		speed = 2f;
		setAttack(2);
		this.velocity = (velocity.normalize().scalar(getSpeed()));
		id = UnitID.TORPEDO;
	}

	@Override
	public void tick(double t) {
		if (position.getX() < -velocity.getX() || position.getX() > 1024 - velocity.getX()
				|| position.getY() < -velocity.getY() || position.getY() > 512 - velocity.getY()) {
			hit();
		}
		bulletMove();
		if (Map.getArray(position) > 0.5f || position.getDist(startingPoint) > 70000)
			hit();

	}

	@Override
	public void render(Render r) {
		float direction = velocity.getRadian();
		if (velocity.getY() > 0)
			direction += 3.14f;
		
		float opacity = (float) (0.001*Math.sqrt(-(position.getDist(startingPoint)-70000)));
		if (Main.ticks % 8 < 4) {
			r.drawImage((int) position.getX(), (int) position.getY(), 1, r.torpedo1, opacity,direction);
		} else {
			r.drawImage((int) position.getX(), (int) position.getY(), 3, r.torpedo, opacity, direction);
		}
	}

}
