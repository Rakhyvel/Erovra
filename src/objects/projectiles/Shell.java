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
	double angle = 0;
	double secondDeriv = 0;
	double airTime = 0;
	double initialVelocity = 0;

	public Shell(Point position, double power, Nation nation, Point target) {	
		super(position, nation);
		speed = 1.5f;
		setAttack(0);
		this.velocity = velocity.normalize().scalar(getSpeed());
		this.setTarget(target);
		id = UnitID.SHELL;
		distance = (float)position.getDistSquared(target);
		angle = getAngle(distance, power);
		secondDeriv = getSecondDeriv(power, angle);
		airTime = distance/getAirTime(power,angle);
		initialVelocity = Math.tan(angle);
	}

	@Override
	public void tick(double t) {
		shellMove(airTime);
		distance-=airTime;
		if (distance < 1) {
			setAttack(1.5f);
		}
	}

	@Override
	public void render(Render r) {
		double scale = (secondDeriv*distance*distance+initialVelocity*distance)/64;
		r.drawImage((int) position.getX(), (int) position.getY(), 4, r.shadowify(r.shell),1,0);
		r.drawImage((int) position.getX(), (int) (position.getY()-(16*scale)), (int)((scale/2.0+0.5f)*4), r.resize(r.shell,(scale/2.0+0.5f),4,4),1,0);
	}
	
	double getAngle(float range, double power){
		return (-Math.asin(range/(power*power))+Math.PI)/2;
	}
	
	double getSecondDeriv(double power, double angle){
		return -1/(2*power*power*Math.cos(angle)*Math.cos(angle));
	}
	
	double getAirTime(double power, double angle){
		return 10*power*Math.sin(angle);
	}
}
