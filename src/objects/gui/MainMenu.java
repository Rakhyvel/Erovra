package objects.gui;

import main.Main;
import main.StateID;
import output.Render;

public class MainMenu extends Menu {

	boolean clicked = false;

	public void tick() {
		if(Main.gameState == StateID.MENU){
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
					if (buttonsHovered == 1) {
						Main.startNewMatch();
					} else if (buttonsHovered == 2) {
						Main.setState(StateID.MENU);
					} else if (buttonsHovered == 3) {
						Main.running = false;
					}
				}
			} else {
				clicked = false;
			}
		}
	}

	public void render(Render r) {
		if (Main.gameState == StateID.MENU) {
			r.drawString("Erovra", 512, 130, 32, r.font32, 0);
			drawButton("Start New Game", 512, 208, 1, r);
			drawButton("Settings", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		}
	}

}
