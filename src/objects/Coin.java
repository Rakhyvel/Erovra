package objects;

import output.Render;
import utility.Point;

public class Coin {
	private Point position;
	private Nation nation;
	private double speed = 1;

	public Coin(Point position, Nation nation) {
		this.position = position;
		this.nation = nation;
	}

	/**
	 * Handles the logic for the coins 60 times a second
	 * @param t  Time since last tick, in millis
	 */
	public void tick(double t) {
		if(speed < 6)
			speed*=1.1;
		position = position.addVector(position.getTargetVector(nation.capital.getPosition()).normalize().scalar(speed));
		if(position.getDist(nation.capital.getPosition())<13 && !(nation.defeated || nation.enemyNation.defeated)) {
			nation.coins++;
			nation.coinArray.remove(this);
		}
	}

	/**
	 * Draws the coin
	 * @param r  Instance of the canvas
	 */
	public void render(Render r) {
		if (nation.name.contains("Sweden")) {
			r.drawImage((int) position.getX(), (int) position.getY(), 16,Render.getScreenBlend(250<<16|250<<8, r.coin),1,0);
		}
	}

}
