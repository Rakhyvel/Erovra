package objects.gui;

import main.Main;
import main.StateID;
import output.Render;

public class GameMenu extends Menu {
	int buttonsHovered = 0;

	public void tick() {
		if (Main.mouse.getX() >= 402 && Main.mouse.getX() < 622 && Main.mouse.getY() > 208 && Main.mouse.getY() < 344) {
			if(Main.mouse.getY() < 248) {
				buttonsHovered = 1;
			} else if(Main.mouse.getY() >= 256 && Main.mouse.getY() < 296) {
				buttonsHovered = 2;
			} else if(Main.mouse.getY() > 304) {
				buttonsHovered = 3;
			} else {
				buttonsHovered = 0;
			}
		} else {
			buttonsHovered = 0;
		}
	}

	public void render(Render r) {
		if (Main.gameState == StateID.VICTORY) {
			r.drawString("Victory!", 512, 160, 32, r.font32, 128 << 8 | 220);
			drawButton("New Game", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		} else if (Main.gameState == StateID.DEFEAT) {
			r.drawString("Defeat!", 512, 160, 32, r.font32, 220 << 16 | 50 << 8);
			drawButton("New Game", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		} else if (Main.gameState == StateID.PAUSED) {
			drawButton("Continue", 512, 208, 1, r);
			drawButton("New Game", 512, 256, 2, r);
			drawButton("Surrender", 512, 304, 3, r);
		}
	}

	void drawButton(String label, int x, int y, int buttonID, Render r) {
		if (buttonsHovered == buttonID) {
			r.drawRect(x - 110, y - 20, 220, 40, 255 << 16 | 255 << 8 | 255, 0.5f);
			r.drawString(label, x, y - 2, 16, r.font16, 0);
		} else {
			r.drawRect(x - 110, y - 20, 220, 40, 0, 0.5f);
			r.drawString(label, x, y - 2, 16, r.font16, 0);
		}
	}
}