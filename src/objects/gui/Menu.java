package objects.gui;

import input.Mouse;
import output.Render;

public abstract class Menu {
	public abstract void tick();
	public abstract void render(Render r);
}
