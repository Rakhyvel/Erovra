package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

public class Cavalry extends Unit {

	float cal;

	public Cavalry(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			defense = 2;
			cal = 0.5f;
		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			defense = 2;
			cal = 2f;
		} else {
			speed = .05f;
			defense = 2.5f;
			cal = 2.5f;
		}
		id = UnitID.CAVALRY;
	}

	public void tick(double t) {
		if(!isBoarded()){
			if(nation.isAIControlled()){
				wander();
			} else {
				clickToMove();
			}
			engaged = autoAim(cal);
			detectHit();
			targetMove();
		}
	}

	public void render(Render r) {
		if ((engaged || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) && !isBoarded()) {
			float direction = position.subVec(facing).getRadian();
			if (velocity.getY() > 0) direction += 3.14f;
			
			if (weight == UnitID.LIGHT) r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, r.lighten(nation.color), direction);
			if (weight == UnitID.MEDIUM) r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, nation.color, direction);
			if (weight == UnitID.HEAVY) r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, r.darken(nation.color), direction);
			if (hit > 1 || isSelected()) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color, direction);
			}
		}
	}
}
