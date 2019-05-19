package objects.projectiles;

import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.units.Unit;
import output.Render;
import utility.Point;
import utility.Vector;

/**
 * Is a base for all projectiles in game. Handles movement and collision detection.
 * 
 * @author Rakhyvel
 *
 */
public abstract class Projectile extends Unit {

	private float attack = 0;

	public Projectile(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
	}

	@Override
	public abstract void tick(double t);

	@Override
	public abstract void render(Render r);
	
	public void dropDownDecide(DropDown d){
		// Do nothing
	}
	public void dropDownRender(Render r, DropDown d){
		// Do nothing
	}

	/**
	 * Removes the projectile from the game
	 */
	public void hit() {
		nation.projectileArray.remove(this);
		position = new Point(0, 0);
	}

	/**
	 * Moves the projectile according to its velocity, as with bullets
	 */
	public void bulletMove() {
		position = position.addVector(velocity);
	}

	/**
	 * Moves the shell according to its velocity. Shell is not damaging until it reaches its target, where its attack is then activated
	 */
	public void shellMove(double airTime) {
		velocity = new Vector(0, 0);
		if (position.getDist(getTarget()) > 0.5) {
			velocity = position.getTargetVector(getTarget()).normalize().scalar(airTime);
		} else {
			hit();
		}
		position = position.addVector(velocity);
	}

	/**
	 * @return  The damage the projectile does
	 */
	public float getAttack() {
		return attack;
	}

	/**
	 * May only be accessed by Projectile subclasses
	 * 
	 * @param attack  How much damage a projectile does
	 */
	protected void setAttack(float attack) {
		this.attack = attack;
	}
}
