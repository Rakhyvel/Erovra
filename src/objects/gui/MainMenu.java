package objects.gui;

import main.Main;
import main.StateID;
import output.Render;

/**
 * Handles the main menu
 * 
 * @author Rakhyvel
 *
 */
public class MainMenu extends Menu {

	private boolean clicked = false;

	@Override
	public void tick() {
		if (Main.gameState == StateID.MENU) {
			getButtonsHovered();
			if (Main.mouse.getMouseLeftDown()) {
				if (!clicked) {
					clicked = true;
					if (buttonsHovered == 1) {
						Main.startNewMatch();
					} else if (buttonsHovered == 2) {
						Main.setState(StateID.MENU);
					} else if (buttonsHovered == 3) {
						Main.endGame();
					}
				}
			} else {
				clicked = false;
			}
		}
	}

	@Override
	public void render(Render r) {
		if (Main.gameState == StateID.MENU) {
			r.drawString("Erovra", 512, 130, r.font32, 0);
			drawButton("Start New Game", 512, 208, 1, r);
			drawButton("Settings", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		}
	}

	/**
	 * Gets the button that the user is hovering over with the mouse
	 */
	private void getButtonsHovered() {
		if (Main.mouse.getX() >= 402 && Main.mouse.getX() < 622 && Main.mouse.getY() > 188 && Main.mouse.getY() < 324) {
			if (Main.mouse.getY() < 228) {
				buttonsHovered = 1;
			} else if (Main.mouse.getY() >= 236 && Main.mouse.getY() < 276) {
				buttonsHovered = 2;
			} else if (Main.mouse.getY() > 284) {
				buttonsHovered = 3;
			} else {
				buttonsHovered = 0;
			}
		} else {
			buttonsHovered = 0;
		}
	}
}
