package objects.gui;

import output.Render;

public abstract class Menu {
	int buttonsHovered;
	public abstract void tick();
	public abstract void render(Render r);

	void drawButton(String label, int x, int y, int buttonID, Render r) {
		if (buttonsHovered == buttonID) {
			r.drawRect(x - 110, y - 20, 220, 40, 255 << 16 | 255 << 8 | 255, 0.5f);
			r.drawString(label, x, y - 2, 16, r.font16, 255<<16|255<<8|255);
		} else {
			r.drawRect(x - 110, y - 20, 220, 40, 0, 0.5f);
			r.drawString(label, x, y - 2, 16, r.font16, 255<<16|255<<8|255);
		}
	}
}
