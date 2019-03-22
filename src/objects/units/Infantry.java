package objects.units;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import objects.gui.Image;
import output.Render;
import utility.Point;

/**
 * Handles the logic and rendering for infantry
 * 
 * @author Rakhyvel
 * @see Unit
 */
public class Infantry extends Unit {
	
	int weightColor = 255<<24;
	Image infantry;

	public Infantry(Point position, Nation nation) {
		super(position, nation, UnitID.NONE);
		speed = .1f;
		defense = 1;
		id = UnitID.INFANTRY;
		dropDownHeight = 150;
		weightColor = nation.color;
		infantry = new Image("/res/ground/infantry.png", 32, 16).getScreenBlend(weightColor);
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (!isBoarded() && health > 0) {
			if (nation.isAIControlled()) {
				wander();
			} else {
				clickToMove();
				clickToDropDown();
			}
			engaged = autoAim(1);
			if (engaged && spotted == 0 || hit > 0) {
				spotted = (int) (60/speed);
			}
			if (spotted > 0)
				spotted--;
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

			r.drawImage((int) position.getX(), (int) position.getY(), infantry, direction);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), r.hitSprite, direction);
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
		d.setPosition(position);
		if (nation.getCoinAmount() >= nation.getCityCost()) {
			d.drawOption("City (" + nation.getCityCost() + ")", 2, 0.5f, r);
		} else {
			d.drawOption("City (" + nation.getCityCost() + ")", 2, 0.7f, r);
		}
		if (nation.getCoinAmount() >= nation.getFactoryCost()) {
			d.drawOption("Factory (" + nation.getFactoryCost() + ")", 3, 0.5f, r);
		} else {
			d.drawOption("Factory (" + nation.getFactoryCost() + ")", 3, 0.7f, r);
		}
		if (nation.getCoinAmount() >= nation.getPortCost()) {
			d.drawOption("Port (" + nation.getPortCost() + ")", 4, 0.5f, r);
		} else {
			d.drawOption("Port (" + nation.getPortCost() + ")", 4, 0.7f, r);
		}
		if (nation.getCoinAmount() >= nation.getAirfieldCost()) {
			d.drawOption("Airfield (" + nation.getAirfieldCost() + ")", 5, 0.5f, r);
		} else {
			d.drawOption("Airfield (" + nation.getAirfieldCost() + ")", 5, 0.7f, r);
		}
	}
}
