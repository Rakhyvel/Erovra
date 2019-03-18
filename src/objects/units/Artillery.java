package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for artillery units
 * 
 * @author Rakhyvel
 * @see Unit
 *
 */
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
		detectHit();
		if (!isBoarded() && health > 0) {
			if (!nation.isAIControlled()) {
				clickToMove();
			} else {
				wander();
			}
			if (getWeight() == UnitID.LIGHT) {
				engaged = aaAim();
			} else if (getWeight() == UnitID.MEDIUM) {
				engaged = autoArtilleryAim(64);
			} else {
				engaged = autoArtilleryAim(128);
			}
			if (engaged && spotted == 0 || hit > 0) {
				spotted = (int) (60 / speed);
			}
			if (spotted > 0) spotted--;
			targetMove();
		}
	}

	@Override
	public void render(Render r) {
		if ((spotted > 0 || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT || Main.gameState == StateID.VICTORY) && !isBoarded()) {
			float direction = position.subVec(getFacing()).getRadian();
			if (velocity.getY() > 0) direction += 3.14f;

			if(!nation.isAIControlled()) {
				if (isSelected()) {
					r.drawLandLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
					r.drawLandLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color, 220 << 16 | 220 << 8 | 220);
					if(weight == UnitID.MEDIUM) r.drawImage((int) position.getX() - 64, (int) position.getY() - 64, 128, r.medArtRange);
					if(weight == UnitID.HEAVY) r.drawImage((int) position.getX() - 128, (int) position.getY() - 128, 256, r.heavyArtRange);
				}
			}

			if (getWeight() == UnitID.LIGHT) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, r.lighten(nation.color), direction);
			} else if (getWeight() == UnitID.MEDIUM) {
				if (isSelected()) r.drawImage((int) position.getX() - 64, (int) position.getY() - 64, 128, r.medArtRange);
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, nation.color, direction);
			} else if (getWeight() == UnitID.HEAVY) {
				if (isSelected()) r.drawImage((int) position.getX() - 128, (int) position.getY() - 128, 256, r.heavyArtRange);
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.artillery, r.darken(nation.color), direction);
			}
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color, direction);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {}

	@Override
	public void dropDownRender(Render r, DropDown d) {}
}