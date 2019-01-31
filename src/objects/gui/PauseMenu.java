package objects.gui;

import main.Main;
import main.StateID;
import output.Render;

public class PauseMenu extends Menu{
	public void tick() {
		
	}
	public void render(Render r) {
		if(Main.gameState == StateID.VICTORY){
			r.darkenScreen();
			r.drawString("Victory!", 441, 160, 32, r.font,128 << 8 | 220);
		} else if (Main.gameState == StateID.DEFEAT){
			r.darkenScreen();
			r.drawString("Defeat!", 450, 160, 32, r.font,220 << 16 | 50 << 8);
		}
	}
}