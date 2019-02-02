package input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class Keyboard extends KeyAdapter {

	String letter = "";
	
	public void keyPressed(KeyEvent e) {
		setKey(e.getKeyCode(),true);
		letter = String.valueOf(e.getKeyChar());
	}

	public void keyReleased(KeyEvent e) {
		setKey(e.getKeyCode(),false);
		letter = "";
	}
	
	public void setKey(int keyCode, boolean pressed){
		if(keyCode == KeyEvent.VK_ESCAPE){
			esc.setPressed(pressed);
		}
		if(keyCode == KeyEvent.VK_EQUALS){
			plus.setPressed(pressed);
		}
		if(keyCode == KeyEvent.VK_MINUS){
			minus.setPressed(pressed);
		}
	}
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
	
	public String getLetter(){
		return letter;
	}
}
