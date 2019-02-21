package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for infantry
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class Infantry extends Unit {

	public Infantry(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = .1f;
		defense = 1;
		id = UnitID.INFANTRY;
	}

	@Override
	public void tick(double t) {
		if (!isBoarded()) {
			if (nation.isAIControlled()) {
				wander();
			} else {
				clickToMove();
				clickToDropDown();
			}
			engaged = autoAim(1) | engaged;
			detectHit();
			targetMove();
		}
	}

	@Override
	public void render(Render r) {
		if ((engaged || nation.name.contains("Sweden") || Main.gameState == StateID.DEFEAT
				|| Main.gameState == StateID.VICTORY) && !isBoarded()) {
			float direction = position.subVec(getFacing()).getRadian();
			if (velocity.getY() > 0)
				direction += 3.14f;
			if (isSelected()) {
				r.drawImageScreen((int)getTarget().getX(), (int)getTarget().getY(), 16, r.flag, nation.color);
				r.drawLine(getPosition(), new Point(Main.mouse.getX(), Main.mouse.getY()), nation.color, 0);
			} else if (this.boundingBox(Main.mouse.getX(), Main.mouse.getY())) {
				r.drawLine(getPosition(), new Point(getTarget().getX(), getTarget().getY()), nation.color, 220<<16|220<<8|220);
			}
			r.drawImageScreen((int) position.getX(), (int) position.getY(), 32, r.infantry, nation.color, direction);
			if (hit > 1) {
				r.drawImageScreen((int) position.getX(), (int) position.getY(), 36, r.hitSprite, nation.color,
						direction);
			}
		}
	}

	@Override
	public void dropDownDecide(DropDown d) {
		if (d.buttonsHovered == 1) {
			nation.buyCity(getPosition());
		} else if (d.buttonsHovered == 2) {
			nation.buyFactory(getPosition());
		} else if (d.buttonsHovered == 3) {
			nation.buyPort(getPosition());
		} else if (d.buttonsHovered == 4) {
			nation.buyAirfield(getPosition());
		}
	}

	@Override
	public void dropDownRender(Render r, DropDown d) {
		if (nation.getCoinAmount() >= nation.getCityCost()) {
			d.drawOption("City (" + nation.getCityCost() + ")", 1, 0.5f, r);
		} else {
			d.drawOption("City (" + nation.getCityCost() + ")", 1, 0.7f, r);
		}
		if (nation.getCoinAmount() >= nation.getFactoryCost()) {
			d.drawOption("Factory (" + nation.getFactoryCost() + ")", 2, 0.5f, r);
		} else {
			d.drawOption("Factory (" + nation.getFactoryCost() + ")", 2, 0.7f, r);
		}
		if (nation.getCoinAmount() >= nation.getPortCost()) {
			d.drawOption("Port (" + nation.getPortCost() + ")", 3, 0.5f, r);
		} else {
			d.drawOption("Port (" + nation.getPortCost() + ")", 3, 0.7f, r);
		}
		if (nation.getCoinAmount() >= nation.getAirfieldCost()) {
			d.drawOption("Airfield (" + nation.getAirfieldCost() + ")", 4, 0.5f, r);
		} else {
			d.drawOption("Airfield (" + nation.getAirfieldCost() + ")", 4, 0.7f, r);
		}
	}
}
