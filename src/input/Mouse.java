package input;

import java.awt.MouseInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import main.Main;

public class Mouse extends MouseAdapter {
	// The mouse class gets mouse input from the user. It handles mouse presses and the position of the mouse
	private boolean mouseLeftDown = false;
	private boolean mouseRightDown = false;

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseLeftDown = true;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			mouseRightDown = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseLeftDown = false;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			mouseRightDown = false;
		}
	}

	public int getX() {
		return (int) MouseInfo.getPointerInfo().getLocation().getX() - Main.getFrameX();
	}

	public int getY() {
		return (int) MouseInfo.getPointerInfo().getLocation().getY() - Main.getFrameY();
	}

	public boolean getMouseLeftDown() {
		return mouseLeftDown;
	}

	public boolean getMouseRightDown() {
		return mouseRightDown;
	}
}
