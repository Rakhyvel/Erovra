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
	private boolean leftClicked;
	private static int tab = 0;
	private boolean shipExempt = false;

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
				if (Main.mouse.getMouseLeftDown() || Main.mouse.getMouseRightDown())
					shouldClose();
			}
		}

	}

	@Override
	public void render(Render r) {
		if (shown && Main.gameState == StateID.ONGOING) {
			r.drawRectBorders((int) getPosition().getX(), (int) getPosition().getY(), 180, 30,
					180 << 24 | 86 << 16 | 86 << 8 | 86, 15); // 350
			r.drawString(String.valueOf(unit.getID()), (int) getPosition().getX() + 7, (int) getPosition().getY() + 12,
					r.font16, 255 << 24 | 250 << 16 | 250 << 8 | 250, false); // 380
			unit.dropDownRender(r, this); // 510

			r.drawRectBorders((int) getPosition().getX(), (int) getPosition().getY() + 22,
					(int) (18 * unit.getHealth()), 8, unit.nation.color, 15); // 300
		}
	}

	/**
	 * Draws buttons on the dropdown menu
	 * 
	 * @param label    The text to go inside the button
	 * @param buttonID The ID of the button
	 * @param shade    The opacity of the button (0.5f == normal, 0.7f ==
	 *                 unavailable)
	 * @param r        The render object
	 */
	public void drawOption(String label, int buttonID, int rectColor, int borders, Render r) {
		int x = (int) getPosition().getX();
		int y = (int) getPosition().getY();
		int textColor = 255 << 24 | 250 << 16 | 250 << 8 | 250;
		if (rectColor == 0) {
			textColor = 255 << 24 | 250 << 16;
		}
		if (buttonsHovered == buttonID && rectColor > 0) {
			rectColor *= 2.7;
		}
		r.drawRectBorders(x, y + buttonID * 30, 180, 30, 180 << 24 | rectColor << 16 | rectColor << 8 | rectColor,
				borders);
		r.drawString(label, x + 7, y + 14 + buttonID * 30, r.font16, textColor, false);
	}

	public void drawOption(String label, int x, int buttonID, int rectColor, int borders, Render r) {
		int y = (int) getPosition().getY();
		int textColor = 255 << 24 | 250 << 16 | 250 << 8 | 250;
		if (buttonsHovered == buttonID && rectColor > 0 && Main.mouse.getX() > x && Main.mouse.getX() < x + 90) {
			rectColor *= 2.7;
		}
		if (x == getPosition().getX() + 90) {
			borders = 12;
			r.drawRectBorders(x, y + buttonID * 30, 90, 30, 180 << 24 | rectColor << 16 | rectColor << 8 | rectColor,
					borders);
		} else {
			if (rectColor == 0) {
				r.drawRectBorders(x, y + buttonID * 30, 90, 30, 180 << 24 | 25 << 16 | 128 << 8 | 230, borders);
			} else {
				r.drawRectBorders(x, y + buttonID * 30, 90, 30,
						180 << 24 | rectColor << 16 | rectColor << 8 | rectColor, borders);
			}
		}
		r.drawString(label, x + 45, y + 14 + buttonID * 30, r.font16, textColor);
	}

	public void drawTab(int tabs, int[] tab1, int[] tab2, int[] tab3, int w1, int w2, int w3, Render r) {
		for (int i = 0; i < tabs; i++) {
			int x = (int) getPosition().getX() + 180 / tabs * i;
			int y = (int) getPosition().getY() + 30;
			int color = 180 << 24;
			int border = 12;
			if (getTab() == i) {
				color = 180 << 24 | 86 << 16 | 86 << 8 | 86;
			}
			if (i == 0)
				border |= 1;
			r.drawRectBorders(x, y, 180 / tabs, 30, color, border);
			if (i == 0)
				r.drawImage(x + 90 / tabs, y + 15, w1, tab1, 1, 0);
			if (i == 1)
				r.drawImage(x + 90 / tabs, y + 15, w2, tab2, 1, 0);
			if (i == 2) {
				r.drawImage(x + 90 / tabs, y + 15, w3, tab3, 1, 0);
			}
		}
	}

	public void selectTabs(int tabs) {
		setTab((int) ((Main.mouse.getX() - position.getX()) / (180 / tabs)));
	}

	/**
	 * Sets the position of the drop down. If the position will cause the drop down
	 * to draw off-screen, the position is capped
	 * 
	 * @param p The position of the unit
	 */
	public void setPosition(Point p) {
		if (p.getX() > 0) {
			if (p.getX() < 845) {
				position.setX(p.getX());
			} else {
				position.setX(845);
			}
		} else {
			position.setX(0);
		}
		if (p.getY() > 0) {
			if (p.getY() < 512 - getDropDownHeight()) {
				position.setY(p.getY());
			} else {
				position.setY(512 - getDropDownHeight());
			}
		} else {
			position.setY(0);
		}
	}

	/**
	 * Shows the drop down
	 * 
	 * @param unit The unit the drop down belongs to
	 */
	public void show(Unit unit) {
		this.unit = unit;
		unit.setHit(3);
		shown = true;
		setPosition(unit.getPosition());
		setTab(0);
	}

	/**
	 * Hides the drop down
	 */
	private void hide() {
		unit = null;
		shown = false;
		shipExempt = false;
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
		return shown && !(shipExempt);
	}

	public boolean getRealShown() {
		return shown;
	}

	/**
	 * @return Whether or not the mouse is within the drop down's bounds
	 */
	public boolean isMouseInsideDropDown() {
		return Main.mouse.getX() >= getPosition().getX() && Main.mouse.getX() < getPosition().getX() + 180
				&& Main.mouse.getY() > getPosition().getY()
				&& Main.mouse.getY() < getPosition().getY() + getDropDownHeight();
	}

	/**
	 * @return The button the mouse is hovering over
	 */
	public int getButtonsHovered() {
		if (Main.mouse.getY() < getPosition().getY() - 30)
			return 0;
		if (Main.mouse.getY() > getPosition().getY() + getDropDownHeight()) {
			return 0;
		}
		if (Main.mouse.getX() < getPosition().getX()) {
			return 0;
		}
		if (Main.mouse.getX() > getPosition().getX() + 180) {
			return 0;
		}
		return (int) (Main.mouse.getY() - getPosition().getY() + 30) / 30 - 1;
	}

	/**
	 * Draw's buttons for factories, ports, and airfields
	 * 
	 * @param r         Render object
	 * @param light     The text to display for the light unit option
	 * @param medium    The text to display for the medium unit option
	 * @param heavy     The text to display for the heavy unit option
	 * @param lightCost The cost of a light unit
	 * @param medCost   The cost of a medium unit
	 * @param heavyCost The cost of a heavy unit
	 */
	public void drawIndustry(Render r, String light, String medium, String heavy, double lightCost, double medCost,
			double heavyCost, Industry industry) {
		if (industry.getProduct() != UnitID.NONE) {
			int minutes = 0, seconds = 0;
			String product = " ";
			if (industry.getProductWeight() == UnitID.LIGHT) {
				product += light;
			} else if (industry.getProductWeight() == UnitID.MEDIUM) {
				product += medium;
			} else if (industry.getProductWeight() == UnitID.HEAVY) {
				product += heavy;
			}
			if (industry.getStart() > 0) {
				minutes = industry.getStart() / 3600;
				seconds = (industry.getStart() / 60) - minutes * 60;
				if (minutes >= 1) {
					product += " " + minutes + "m,";
				}
				product += " " + seconds + "s";
			}
			r.drawRectBorders((int) getPosition().getX(), (int) getPosition().getY() + 30, 180, 30,
					180 << 24 | 128 << 16 | 128 << 8 | 128, 13);
			r.drawString(product, (int) getPosition().getX(), (int) getPosition().getY() + 44, r.font16,
					255 << 24 | 250 << 16 | 250 << 8 | 250, false);
			if (industry.getAutomatic()) {
				drawOption("Auto", (int) getPosition().getX(), 2, 0, 13, r);
			} else {
				drawOption("Auto", (int) getPosition().getX(), 2, 32, 13, r);
			}
			drawOption("Cancel", (int) getPosition().getX() + 90, 2, 32, 13, r);
		} else {
			if (unit.nation.getCoinAmount() >= lightCost) {
				drawOption(light + " (" + (int) lightCost + ")", 2, 32, 5, r);
			} else {
				drawOption(light + " (" + (int) lightCost + ")", 2, 0, 5, r);
			}
			if (unit.nation.getCoinAmount() >= medCost) {
				drawOption(medium + " (" + (int) medCost + ")", 3, 32, 5, r);
			} else {
				drawOption(medium + " (" + (int) medCost + ")", 3, 0, 5, r);
			}
			if (unit.nation.getCoinAmount() >= heavyCost) {
				drawOption(heavy + " (" + (int) heavyCost + ")", 4, 32, 13, r);
			} else {
				drawOption(heavy + " (" + (int) heavyCost + ")", 4, 0, 13, r);
			}
		}
	}

	public Point getPosition() {
		return position;
	}

	public int getDropDownHeight() {
		if (unit != null)
			return unit.dropDownHeight;
		return 0;
	}

	public int getTab() {
		return tab;
	}

	public void setTab(int t) {
		tab = t;
	}

	public void drawUpgrading(Industry industry, Render r) {
		int minutes = 0, seconds = 0;
		String product = "Upgrading";
		if (industry.getStart() > 0) {
			minutes = industry.getStart() / 3600;
			seconds = (industry.getStart() / 60) - minutes * 60;
			if (minutes >= 1) {
				product += " " + minutes + "m,";
			}
			product += " " + seconds + "s";
		}
		r.drawRectBorders((int) getPosition().getX(), (int) getPosition().getY() + 30, 180, 30,
				180 << 24 | 128 << 16 | 128 << 8 | 128, 13);
		r.drawString(product, (int) getPosition().getX() + 7, (int) getPosition().getY() + 44, r.font16,
				255 << 24 | 250 << 16 | 250 << 8 | 250, false);
		drawOption("Cancel upgrade", 2, 32, 13, r);
	}

	public boolean isUnit(Unit unit) {
		if (this.unit == null)
			return false;
		return this.unit.equals(unit);
	}
	
	// This exemption allows units to be selected even when the landing craft drop down is open
	public void exemptShip() {
		shipExempt = true;
	}
}
