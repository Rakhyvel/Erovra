package input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Keyboard extends KeyAdapter {

	// The keyboard class gets keyboard input from the user. It stores these key
	// inputs in key objects that return whether or not they are being pressed
	String letter = "";

	@Override
	public void keyPressed(KeyEvent e) {
		setKey(e.getKeyCode(), true);
		letter = String.valueOf(e.getKeyChar());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setKey(e.getKeyCode(), false);
		letter = "";
	}

	// This is where keys are set to true or false
	public void setKey(int keyCode, boolean pressed) {
		if (keyCode == KeyEvent.VK_ESCAPE) {
			esc.setPressed(pressed);
		}
		if (keyCode == KeyEvent.VK_EQUALS) {
			plus.setPressed(pressed);
		}
		if (keyCode == KeyEvent.VK_MINUS) {
			minus.setPressed(pressed);
		}
	}

	// This is where keys are created and instantiated
	public Key esc = new Key();
	public Key plus = new Key();
	public Key minus = new Key();

	public class Key {

		private boolean pressed = false;

		public boolean isPressed() {
			return pressed;
		}

		public void setPressed(boolean pressed) {
			this.pressed = pressed;
		}
	}

	public String getLetter() {
		return letter;
	}
}
