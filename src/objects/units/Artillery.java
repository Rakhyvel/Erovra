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
			setDefense(0.1f);
		} else if (weight == UnitID.MEDIUM) {
			speed = 0.1f;
			setDefense(2);
		} else {
			speed = 0.05f;
			setDefense(2);
		}
		id = UnitID.ARTILLERY;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (!isBoarded() && health > 0) {
			if (weight == UnitID.MEDIUM){
				shootShell(128);
			} else if (weight == UnitID.HEAVY){
				shootShell(256);
			}
			if (!nation.isAIControlled()) {
				clickToMove();
				clickToDropDown();
			}
			if (engaged && spotted == 0 || hit > 0) {
				spotted = (int) (60/speed);
			}
			if (spotted > 0){
				spotted--;
			} else {
				disengage();
			}
			targetMove(0.5f);
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
					if(weight == UnitID.MEDIUM) r.drawImage((int) position.getX(), (int) position.getY(), 128, r.medArtRange,0.5f,0);
					if(weight == UnitID.HEAVY) r.drawImage((int) position.getX(), (int) position.getY(),256, r.heavyArtRange,0.5f,0);
				}
			}

			r.drawImage((int) position.getX(), (int) position.getY(),32, Render.getScreenBlend(Render.getColor(weight,nation.color),Render.artillery),1, direction);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36,r.hitSprite,1, direction);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {}

	@Override
	public void dropDownRender(Render r, DropDown d) {}
}