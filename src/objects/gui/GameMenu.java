package objects.gui;

import main.Main;
import main.StateID;
import output.Render;

/**
 * Handles the pause, defeat and victory menu
 * 
 * @author Rakhyvel
 *
 */
public class GameMenu extends Menu {

	private boolean clicked = false;

	@Override
	public void tick() {
		if (Main.gameState != StateID.ONGOING) {
			buttonsHovered = getButtonsHovered();
			if (Main.mouse.getMouseLeftDown()) {
				if (!clicked) {
					clicked = true;
					decideAction();
				}
			} else {
				clicked = false;
			}
		}
	}

	@Override
	public void render(Render r) {
		if (Main.gameState == StateID.VICTORY) {
			r.drawString("Victory!", 512, 160, r.font32, 0 << 16 | 128 << 8 | 220);
			r.drawString("Time: " + Main.ticks / 3600 + " minutes", 512, 192, r.font16, 255 << 16 | 255 << 8 | 255);
			drawButton("Main Menu", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		} else if (Main.gameState == StateID.DEFEAT) {
			r.drawString("Defeat!", 512, 160, r.font32, 220 << 16 | 32 << 8 | 0);
			r.drawString("Time: " + Main.ticks / 3600 + " minutes", 512, 192, r.font16, 255 << 16 | 255 << 8 | 255);
			drawButton("Main Menu", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		} else if (Main.gameState == StateID.PAUSED) {
			drawButton("Continue", 512, 208, 1, r);
			drawButton("Settings", 512, 256, 2, r);
			drawButton("Surrender", 512, 304, 3, r);
		}
	}

	/**
	 * @return  The button the mouse is hovering over
	 */
	private int getButtonsHovered() {
		if (Main.mouse.getX() >= 402 && Main.mouse.getX() < 622 && Main.mouse.getY() > 188 && Main.mouse.getY() < 324) {
			if (Main.mouse.getY() < 228) {
				return 1;
			} else if (Main.mouse.getY() >= 236 && Main.mouse.getY() < 276) {
				return 2;
			} else if (Main.mouse.getY() > 284) {
				return 3;
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Decides what to do when the user presses a button
	 */
	private void decideAction(){
		if (Main.gameState == StateID.VICTORY || Main.gameState == StateID.DEFEAT) {
			if (buttonsHovered == 2) {
				Main.setState(StateID.MENU);
			} else if (buttonsHovered == 3) {
				Main.endGame();
			}
		} else if (Main.gameState == StateID.PAUSED) {
			if (buttonsHovered == 1) {
				Main.setState(StateID.ONGOING);
			} else if (buttonsHovered == 2) {
				Main.setState(StateID.ONGOING);
			} else if (buttonsHovered == 3) {
				Main.setState(StateID.DEFEAT);
			}
		}
	}
}