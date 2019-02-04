package objects.gui;

import main.Main;
import main.StateID;
import output.Render;

public class GameMenu extends Menu {

	boolean clicked = false;

	public void tick() {
		if (Main.gameState != StateID.ONGOING) {
			if (Main.mouse.getX() >= 402 && Main.mouse.getX() < 622 && Main.mouse.getY() > 208 && Main.mouse.getY() < 344) {
				if (Main.mouse.getY() < 248) {
					buttonsHovered = 1;
				} else if (Main.mouse.getY() >= 256 && Main.mouse.getY() < 296) {
					buttonsHovered = 2;
				} else if (Main.mouse.getY() > 304) {
					buttonsHovered = 3;
				} else {
					buttonsHovered = 0;
				}
			} else {
				buttonsHovered = 0;
			}
			if (Main.mouse.getMouseLeftDown()) {
				if (!clicked) {
					clicked = true;
					if (Main.gameState == StateID.VICTORY || Main.gameState == StateID.DEFEAT) {
						if (buttonsHovered == 2) {
							Main.setState(StateID.MENU);
						} else if (buttonsHovered == 3) {
							Main.running = false;
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
			} else {
				clicked = false;
			}
		}
	}

	public void render(Render r) {
		if (Main.gameState == StateID.VICTORY) {
			r.drawString("Victory!", 512, 160, 32, r.font32, 128 << 8 | 220);
			r.drawString("Time: " + Main.ticks / 3600 + " minutes", 512, 192, 16, r.font16, 255 << 16 | 255 << 8 | 255);
			drawButton("Main Menu", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		} else if (Main.gameState == StateID.DEFEAT) {
			r.drawString("Defeat!", 512, 160, 32, r.font32, 220 << 16 | 50 << 8);
			r.drawString("Time: " + Main.ticks / 3600 + " minutes", 512, 192, 16, r.font16, 255 << 16 | 255 << 8 | 255);
			drawButton("Main Menu", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		} else if (Main.gameState == StateID.PAUSED) {
			drawButton("Continue", 512, 208, 1, r);
			drawButton("Settings", 512, 256, 2, r);
			drawButton("Surrender", 512, 304, 3, r);
		}
	}
}