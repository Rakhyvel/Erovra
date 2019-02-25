package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for cavalry units
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class Cavalry extends Unit {

	private float cal;

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

	@Override
	public void tick(double t) {
		if (!isBoarded()) {
			if (nation.isAIControlled()) {
				wander();
			} else {
				clickToMove();
			}
			engaged = autoAim(cal);
			if (engaged && spotted == 0) {
				spotted = (int) (60/speed);
			}
			if (spotted > 0)
				spotted--;
			detectHit();
			targetMove();
		}
	}

	@Override
	public void render(Render r) {
		if ((spotted > 0 || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) && !isBoarded()) {
			float direction = position.subVec(getFacing()).getRadian();
			if (velocity.getY() > 0)
				direction += 3.14f;

			if (isSelected()) {
				r.drawImageScreen((int)getTarget().getX(), (int)getTarget().getY(), 16, r.flag, nation.color);
				r.drawLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
			} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
				r.drawLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color,
						220 << 16 | 220 << 8 | 220);
			}

			if (getWeight() == UnitID.LIGHT)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, r.lighten(nation.color),
						direction);
			if (getWeight() == UnitID.MEDIUM)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, nation.color, direction);
			if (getWeight() == UnitID.HEAVY)
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.cavalry, r.darken(nation.color),
						direction);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color,
						direction);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
	}
}
