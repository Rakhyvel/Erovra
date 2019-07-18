package objects.gui;

import main.Main;
import output.Render;

public class ErrorMessage extends Menu {
	String errorMessage = "";
	int errorTick = 0;

	@Override
	public void tick() {
		if (errorTick > 0)
			errorTick--;
	}

	@Override
	public void render(Render r) {
		if (errorTick > 30) {
			r.drawString(errorMessage, 512, 400, r.font16, 255<<24 | 250<<16);
		} else if (errorTick > 0) {
			r.drawString(errorMessage, 512, 400, r.font16, (int)((errorTick/30.0f)*255) << 24 | 250<<16);
		}
	}

	public void showErrorMessage(String errorMessage) {
		errorTick = 120;
		this.errorMessage = errorMessage;
	}
}
