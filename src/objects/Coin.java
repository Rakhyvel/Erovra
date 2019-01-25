package objects;

import output.Render;
import utility.Point;

public class Coin {
	Point position;
	Nation nation;
	double speed = 1;

	public Coin(Point position, Nation nation) {
		this.position = position;
		this.nation = nation;
	}

	public void tick(double t) {
		if(speed < 6)
			speed*=1.1;
		position = position.addVector(position.getTargetVector(nation.capital.position).normalize().scalar(speed));
		if(position.getDist(nation.capital.position)<13) {
			nation.coins++;
			nation.coinArray.remove(this);
		}
	}

	public void render(Render r) {
		if (nation.name.contains("Sweden")) {
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 16, r.coin, 255<<16|255<<8);
		}
	}

}
