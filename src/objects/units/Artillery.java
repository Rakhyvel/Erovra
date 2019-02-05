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
			speed = .05f;
			defense = 1;
		} else if (weight == UnitID.MEDIUM) {
			speed = 0.05f;
			defense = 2;
		} else {
			speed = 0f;
			defense = 1;
		}
		id = UnitID.ARTILLERY;
	}

	public void tick(double t) {
		if (!isBoarded()) {
			wander();
			if (weight == UnitID.LIGHT) {
				aaAim();
			} else if (weight == UnitID.MEDIUM) {
				autoArtilleryAim(64);
			} else {
				autoArtilleryAim(32);
			}
			detectHit();
			targetMove();
		}
	}

	public void render(Render r) {
		if ((engaged || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) && !isBoarded()) {
			float direction = position.subVec(target).getRadian();
			if (velocity.getY() > 0) direction += 3.14f;

			if (weight == UnitID.LIGHT) r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, r.lighten(nation.color), direction);
			if (weight == UnitID.MEDIUM) r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, nation.color, direction);
			if (weight == UnitID.HEAVY) r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, r.darken(nation.color), direction);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color, direction);
			}
		}
	}
}