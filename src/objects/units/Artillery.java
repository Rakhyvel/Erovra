package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import output.Render;
import utility.Point;

public class Artillery extends Unit {

	public Artillery(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .1f;
			defense = 1;
		} else if (weight == UnitID.MEDIUM) {
			speed = 0.1f;
			defense = 2;
		} else {
			speed = 0.05f;
			defense = 1;
		}
		id = UnitID.ARTILLERY;
	}

	@Override
	public void tick(double t) {
		if (!isBoarded()) {
			if (!nation.isAIControlled()) {
				clickToMove();
			} else {
				wander();
			}
			if (getWeight() == UnitID.LIGHT) {
				engaged = aaAim() | engaged;
			} else if (getWeight() == UnitID.MEDIUM) {
				engaged = autoArtilleryAim(64) | engaged;
			} else {
				engaged = autoArtilleryAim(32) | engaged;
			}
			detectHit();
			targetMove();
		}
	}

	@Override
	public void render(Render r) {
		if ((engaged || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) && !isBoarded()) {
			float direction = position.subVec(getTarget()).getRadian();
			if (velocity.getY() > 0)
				direction += 3.14f;

			if (getWeight() == UnitID.LIGHT)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery,
						r.lighten(nation.color), direction);
			if (getWeight() == UnitID.MEDIUM)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, nation.color,
						direction);
			if (getWeight() == UnitID.HEAVY)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, r.darken(nation.color),
						direction);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color,
						direction);
			}
		}
	}
}