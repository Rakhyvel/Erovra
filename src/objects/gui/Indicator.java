package objects.gui;

import output.Render;

public class Indicator extends Menu {
	String message = "";
	int tick = 0;

	@Override
	public void tick() {
		if (tick > 0)
			tick--;
	}

	@Override
	public void render(Render r) {
		if (tick > 30) {
			r.drawString(message, 512, 380, r.font16, 255 << 24 | 250 << 16 | 250 << 8 | 250);
		} else if (tick > 0) {
			r.drawString(message, 512, 380, r.font16, (int) ((tick / 30.0f) * 255) << 24 | 250 << 16 | 250 << 8 | 250);
		}
	}

	public void showMessage(String errorMessage) {
		tick = 240;
		this.message = errorMessage;
	}
	public void hideMessage() {
		tick = 0;
	}
}
