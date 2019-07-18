package objects.units;

import main.Main;
import main.SelectionID;
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

	public Cavalry(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
		if (weight == UnitID.LIGHT) {
			speed = .3f;
			setDefense(20);
			attack = 0.5f;
		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			setDefense(20);
			attack = 1f;
		} else {
			speed = .05f;
			setDefense(20f);
			attack = 2f;
		}
		id = UnitID.CAVALRY;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (!isBoarded() && health > 0) {
			shootBullet(attack);
			if (!nation.isAIControlled()) {
				clickToMove();
				clickToDropDown();
			}
			if (spotted > 0) {
				spotted--;
			} else {
				disengage();
			}
			targetMove(0.5f);
		}
	}

	@Override
	public void render(Render r) {
		if ((spotted > 0 || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) && !isBoarded()) {
			float direction = position.subVec(getFacing()).getRadian();
			if (velocity.getY() > 0)
				direction += 3.14f;
			if (!nation.isAIControlled()) {
				if (isSelected()) {
					if (Main.world.gotoMethod == SelectionID.CENTER_OF_MASS) {
						r.drawLine(getPosition(),
								getPosition().addVector(
										Main.world.midPoint.subVec(new Point(Main.mouse.getX(), Main.mouse.getY()))),
								nation.color, 0, 0.5f);
					} else {
						r.drawLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0,
								0.5f);

					}
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())
						|| Main.world.getShowPaths() && position.getDist(target) > 16) {
					r.drawLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color,
							220 << 16 | 220 << 8 | 220, 0.5f);
					if (patrolling) {
						r.drawLine(getPosition(), new Point(this.patrol1), nation.color, 220 << 16 | 220 << 8 | 220,
								0.5f);
						r.drawLine(getPosition(), new Point(this.patrol2), nation.color, 220 << 16 | 220 << 8 | 220,
								0.5f);
					}
				}
			}

			r.drawImage((int) position.getX(), (int) position.getY(), 32,
					Render.getScreenBlend(Render.getColor(weight, nation.color), Render.cavalry), 1, direction);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36, r.hitSprite, 1, direction);
			}

			if (patrolling) {
				r.drawImage((int) position.getX(), (int) position.getY() - 10, 16,
						Render.getScreenBlend(255 << 24 | 255 << 16 | 255 << 8 | 255, Render.patrol), 1, 0);
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
