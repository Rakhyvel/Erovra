package objects.gui;

import main.SpriteSheet;

public class Font {
	SpriteSheet charset;
	int size;
	private static int[] kern16 = {
			0,0,4,4,4,1,7,5,7,0,0,6,5,0,6,5,
			7,6,5,4,5,7,7,4,5,4,6,6,0,0,0,0,
			8,3,5,9,7,12,9,2,4,4,5,8,2,4,2,6,
			7,5,7,7,8,7,7,7,7,7,2,2,8,8,8,7,
			13,11,9,9,9,9,8,10,9,3,7,9,7,11,9,10,
			9,10,9,9,9,9,10,13,9,10,7,4,5,4,5,9,
			3,7,7,7,7,7,6,7,7,3,4,7,3,11,7,7,
			8,7,5,7,5,7,7,11,7,7,8,5,3,5,8,4};

	public Font(SpriteSheet charset, int size) {
		this.charset = charset;
		this.size = size;
	}

	public int[] getLetter(int letter) {
		return charset.getSubset(letter % 16, letter / 16, size);
	}

	public int getKern(int index) {
		if (size == 32)
			return 32;
		return kern16[index];
	}
	
	public int getStringWidth(String label) {
		int length = 0;
		for(int i = 0; i < label.length(); i++) {
			length+=getKern(label.charAt(i))+3;
		}
		return length+16;
	}
}
