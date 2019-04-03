package objects.gui;

import output.Render;

/**
 * Used as a platform to build other menus
 * 
 * @author Rakhyvel
 * 
 * @see GameMenu
 * @see MainMenu
 * @see DropDown
 *
 */
public abstract class Menu {

	public int buttonsHovered;

	/**
	 * Called 60 times a second (on a good day!)
	 */
	public abstract void tick();

	/**
	 * Called when the game renders
	 * 
	 * @param r
	 *            Custom render object
	 */
	public abstract void render(Render r);

	/**
	 * Draws a GUI button
	 * 
	 * @param label
	 *            Text to be displayed inside
	 * @param x
	 *            The centerpoint x coordinate
	 * @param y
	 *            The centerpoint y coordinate
	 * @param buttonID
	 *            Which button this one is
	 * @param r
	 *            Instance of the canvas
	 */
	void drawButton(String label, int x, int y, int buttonID, Render r) {
		if (buttonsHovered == buttonID) {
			r.drawRectBorders(x - 110, y - 20, 220, 40, 180 << 24| 128 << 16 | 128 << 8 | 128,15);
			r.drawString(label, x, y, r.font16, 255<<24 | 250 << 16 | 250 << 8 | 250);
		} else {
			r.drawRect(x - 110, y - 20, 220, 40, 180 << 24| 20 << 16 | 20 << 8 | 20);
			r.drawString(label, x, y, r.font16, 255<<24 | 250 << 16 | 250 << 8 | 250);
		}
	}
}
