package objects.gui;

import main.Main;
import main.StateID;
import main.UnitID;
import objects.units.Industry;
import objects.units.Unit;
import output.Render;
import utility.Point;

/**
 * Handles drop down logic
 * 
 * @author Rakhyvel
 *
 */
public class DropDown extends Menu {

	// !* When dropdown render/tick is called it should call the unit's dropdown
	// tick/render method, that way its not as stupid

	private Point position = new Point(512, 50);
	private Unit unit = null;
	private boolean shown = false;
	private boolean shouldClose = false;
	private int closeTick;
	private int dropDownHeight;
	private boolean leftClicked;

	@Override
	public void tick() {
		if (shouldClose) {
			if (Main.ticks - closeTick > 1) {
				shouldClose = false;
				hide();
			}
		}
		if (shown) {
			unit.setHit(3);
			setDropDownHeight();
			if (isMouseInsideDropDown()) {
				buttonsHovered = getButtonsHovered();
				if (Main.mouse.getMouseLeftDown()) {
					leftClicked = true;
				} else if (leftClicked) {
					leftClicked = false;
					unit.dropDownDecide(this);
				}
			} else {
				buttonsHovered = 0;
				if (Main.mouse.getMouseLeftDown())
					shouldClose();
			}
		}

	}

	@Override
	public void render(Render r) {
		if (shown && Main.gameState == StateID.ONGOING) {
			r.drawRect((int) getPosition().getX(), (int) getPosition().getY(), 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
			r.drawString(String.valueOf(unit.getID()) + " (" + (int) unit.getHealth() + "/10)", (int) getPosition().getX() + 85, (int) getPosition().getY() + 10, r.font16, 250 << 16 | 250 << 8 | 250);

			unit.dropDownRender(r, this);

			r.drawRect((int) getPosition().getX() + 5, (int) getPosition().getY() + 23, 156, 6, 0);
			r.drawRect((int) getPosition().getX() + 7, (int) getPosition().getY() + 25, (int) (15.2 * unit.getHealth()), 2, unit.nation.color);
		}
	}

	/**
	 * Draws buttons on the dropdown menu
	 * 
	 * @param label
	 *            The text to go inside the button
	 * @param buttonID
	 *            The ID of the button
	 * @param shade
	 *            The opacity of the button (0.5f == normal, 0.7f ==
	 *            unavailable)
	 * @param r
	 *            The render object
	 */
	public void drawOption(String label, int buttonID, float shade, Render r) {
		int x = (int) getPosition().getX();
		int y = (int) getPosition().getY();
		int textColor = 250 << 16 | 250 << 8 | 250;
		if (shade == 0.7f) {
			textColor = 255 << 16;
		}
		if (buttonsHovered == buttonID && shade != 0.7f) {
			r.drawRect(x, y + buttonID * 30, 170, 30, 200 << 16 | 200 << 8 | 200, 0.5f);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, r.font16, 250 << 16 | 250 << 8 | 250);
		} else {
			r.drawRect(x, y + buttonID * 30, 170, 30, 0, shade);
			r.drawString(label, x + 85, y + 13 + buttonID * 30, r.font16, textColor);
		}
	}

	/**
	 * Sets the position of the drop down. If the position will cause the drop
	 * down to draw off-screen, the position is capped
	 * 
	 * @param p
	 *            The position of the unit
	 */
	public void setPosition(Point p) {
		if (p.getX() > 0) {
			if (p.getX() < 855) {
				position.setX(p.getX());
			} else {
				position.setX(855);
			}
		} else {
			position.setX(0);
		}
		if (p.getY() > 0) {
			if (p.getY() < 512 - dropDownHeight + 30) {
				position.setY(p.getY());
			} else {
				position.setY(512 - dropDownHeight + 30);
			}
		} else {
			position.setY(0);
		}
	}

	/**
	 * Shows the drop down
	 * 
	 * @param unit
	 *            The unit the drop down belongs to
	 */
	public void show(Unit unit) {
		this.unit = unit;
		unit.setHit(3);
		shown = true;
		setPosition(unit.getPosition());
		setDropDownHeight();
	}

	/**
	 * Calculates the height of the drop down depending on the unit
	 */
	private void setDropDownHeight() {
		if (unit.getID() == UnitID.INFANTRY) {
			dropDownHeight = 30 * 6;
		} else if (unit.getID() == UnitID.FACTORY || unit.getID() == UnitID.PORT || unit.getID() == UnitID.CITY || unit.getID() == UnitID.AIRFIELD) {
			dropDownHeight = 30 * 6;
		} else if (unit.getID() == UnitID.SHIP) {
			dropDownHeight = 30 * 4;
		} else {
			dropDownHeight = 60;
		}
	}

	/**
	 * Hides the drop down
	 */
	private void hide() {
		unit = null;
		shown = false;
	}

	/**
	 * Sets the drop down to close in the future
	 */
	public void shouldClose() {
		shouldClose = true;
		closeTick = Main.ticks;
	}

	/**
	 * @return Whether or not the drop down is visible. If the unit is a ship,
	 *         always returns false.
	 */
	public boolean getShown() {
		return shown && (unit.getID() != UnitID.SHIP);
	}

	/**
	 * @return Whether or not the mouse is within the drop down's bounds
	 */
	public boolean isMouseInsideDropDown() {
		return Main.mouse.getX() >= getPosition().getX() && Main.mouse.getX() < getPosition().getX() + 170 && Main.mouse.getY() > getPosition().getY() && Main.mouse.getY() < getPosition().getY() + dropDownHeight;
	}

	/**
	 * @return The button the mouse is hovering over
	 */
	public int getButtonsHovered() {
		if (Main.mouse.getY() < getPosition().getY() + 30) return 0;
		if (Main.mouse.getY() > getPosition().getY() + dropDownHeight) {
			return 0;
		}
		return (int) (Main.mouse.getY() - getPosition().getY() + 5) / 30 - 1;
	}

	/**
	 * Draw's buttons for factories, ports, and airfields
	 * 
	 * @param r
	 *            Render object
	 * @param light
	 *            The text to display for the light unit option
	 * @param medium
	 *            The text to display for the medium unit option
	 * @param heavy
	 *            The text to display for the heavy unit option
	 * @param lightCost
	 *            The cost of a light unit
	 * @param medCost
	 *            The cost of a medium unit
	 * @param heavyCost
	 *            The cost of a heavy unit
	 */
	public void drawIndustry(Render r, String light, String medium, String heavy, int lightCost, int medCost, int heavyCost, Industry indsutry) {
		if (indsutry.getProduct() != UnitID.NONE) {
			int minutes = 0, seconds = 0;
			String product = " ";
			if (indsutry.getProductWeight() == UnitID.LIGHT) {
				product += light;
			} else if (indsutry.getProductWeight() == UnitID.MEDIUM) {
				product += medium;
			} else if (indsutry.getProductWeight() == UnitID.HEAVY) {
				product += heavy;
			}
			if (indsutry.getStart() > 0) {
				minutes = indsutry.getStart() / 3600;
				seconds = (indsutry.getStart() / 60) - minutes * 60;
				if (minutes >= 1) {
					product += minutes + "m,";
				}
				product += seconds + "s";
			}
			r.drawRect((int) getPosition().getX(), (int) getPosition().getY() + 30, 170, 30, 64 << 16 | 64 << 8 | 64, 0.5f);
			r.drawString(product, (int) getPosition().getX() + 85, (int) getPosition().getY() + 40, r.font16, 250 << 16 | 250 << 8 | 250);
		} else {
			if (unit.nation.getCoinAmount() >= lightCost) {
				drawOption(light + " (" + lightCost + ")", 1, 0.5f, r);
			} else {
				drawOption(light + " (" + lightCost + ")", 1, 0.7f, r);
			}
			if (unit.nation.getCoinAmount() >= medCost) {
				drawOption(medium + " (" + medCost + ")", 2, 0.5f, r);
			} else {
				drawOption(medium + " (" + medCost + ")", 2, 0.7f, r);
			}
			if (unit.nation.getCoinAmount() >= heavyCost) {
				drawOption(heavy + " (" + heavyCost + ")", 3, 0.5f, r);
			} else {
				drawOption(heavy + " (" + heavyCost + ")", 3, 0.7f, r);
			}
		}
	}

	public Point getPosition() {
		return position;
	}
}
