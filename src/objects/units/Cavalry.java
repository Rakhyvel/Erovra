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
			setDefense(2);
			cal = 0.5f;
		} else if (weight == UnitID.MEDIUM) {
			speed = .1f;
			setDefense(2);
			cal = 1f;
		} else {
			speed = .05f;
			setDefense(2f);
			cal = 2f;
		}
		id = UnitID.CAVALRY;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (!isBoarded() && health > 0) {
			if (nation.isAIControlled()) {
				autoAim(2048,cal);
			} else {
				clickToMove();
			}
			if (engaged && spotted == 0 || hit > 0) {
				spotted = (int) (60/speed);
				if(!nation.engagedUnits.contains(this))
					nation.engagedUnits.add(this);
			}
			if (spotted != 0){
				spotted--;
			} else {
				nation.engagedUnits.remove(this);
			}
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
			if(!nation.isAIControlled()) {
				if (isSelected()) {
					r.drawLandLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
				} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
					r.drawLandLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color,
							220 << 16 | 220 << 8 | 220);
				}
			}
				

			r.drawImage((int) position.getX(), (int) position.getY(), 32,Render.getScreenBlend(Render.getColor(weight,nation.color),Render.cavalry),1, direction);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36,r.hitSprite,1, direction);
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
