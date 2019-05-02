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
	
	int weightColor = 255<<24;

	public Infantry(Point position, Nation nation) {
		super(position, nation, UnitID.MEDIUM);
		speed = .1f;
		setDefense(1);
		id = UnitID.INFANTRY;
		dropDownHeight = 150;
	}

	@Override
	public void tick(double t) {
		detectHit();
		if (!isBoarded() && health > 0) {
			shootBullet(1);
			if (nation.isAIControlled()) {
				settle();
			} else {
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

			r.drawImage((int) position.getX(), (int) position.getY(), 32, Render.getScreenBlend(Render.getColor(weight,nation.color),Render.infantry),1, direction);
			if (hit > 1) {
				r.drawImage((int) position.getX(), (int) position.getY(), 36,r.hitSprite,1, direction);
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
			d.drawOption("City (" + nation.getCityCost() + ")", 1, 32, 5, r);
		} else {
			d.drawOption("City (" + nation.getCityCost() + ")", 1, 0, 5, r);
		}
		if (nation.getCoinAmount() >= nation.getFactoryCost()) {
			d.drawOption("Factory (" + nation.getFactoryCost() + ")", 2, 32, 5, r);
		} else {
			d.drawOption("Factory (" + nation.getFactoryCost() + ")", 2, 0, 5, r);
		}
		if (nation.getCoinAmount() >= nation.getPortCost()) {
			d.drawOption("Port (" + nation.getPortCost() + ")", 3, 32, 5, r);
		} else {
			d.drawOption("Port (" + nation.getPortCost() + ")", 3, 0, 5, r);
		}
		if (nation.getCoinAmount() >= nation.getAirfieldCost()) {
			d.drawOption("Airfield (" + nation.getAirfieldCost() + ")", 4, 32, 13, r);
		} else {
			d.drawOption("Airfield (" + nation.getAirfieldCost() + ")", 4, 0, 13, r);
		}
	}
	
	/**
	 * If the unit is far enough away other ports and cities, builds either a city,
	 * port, or factory. Only builds max 2 airfields and 3 factories
	 */
	public void settle() {
		nation.buyCity(position);
		if (nation.getCityCost() > 30) {
			if (nation.airSupremacy <= nation.enemyNation.airSupremacy) {
				if (nation.getAirfieldCost() < nation.coins / 2)
					nation.buyAirfield(position);
			} else {
				if (nation.seaSupremacy <= nation.enemyNation.seaSupremacy && nation.buyPort(position)) {
				} else {
					if (nation.getFactoryCost() < nation.coins / 2)
						nation.buyFactory(position);
				}
			}
		}
	}
}
