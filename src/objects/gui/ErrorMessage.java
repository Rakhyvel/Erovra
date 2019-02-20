package objects.gui;

import output.Render;


public class ErrorMessage extends Menu{
	String errorMessage = "";
	int errorTick = 0;
	@Override
	public void tick() {
		if(errorTick > 0)
			errorTick--;
	}

	@Override
	public void render(Render r) {
		if(errorTick > 0){
			r.drawString(errorMessage, 512, 400, r.font16, 250<<16);
		}
	}
	public void showErrorMessage(String errorMessage){
		errorTick = 120;
		this.errorMessage = errorMessage;
	}
}
