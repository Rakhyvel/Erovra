package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

public class Infantry extends Unit {

	public Infantry(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = .1f;
		defense = 1;
		id = UnitID.INFANTRY;
	}

	public void tick(double t) {
		if (!boarded) {
			if (nation.isAIControlled()) {
				wander();
			} else {
				clickToMove();
				clickToDropDown();
			}
			engaged = autoAim(1);
			detectHit();
			targetMove();
		}
	}

	public void render(Render r) {
		if ((engaged || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) && !boarded) {
			float direction = position.subVec(facing).getRadian();
			if (velocity.getY() > 0) direction += 3.14f;

			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.infantry, nation.color, direction);
			if (hit > 1 || selected) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color, direction);
			}
		}
	}
}
