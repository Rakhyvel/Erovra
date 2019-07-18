package main;

public enum SelectionID {
	SINGLE(), MULTI(), BOX(), TASK(),
	
	CENTER_OF_MASS, POINT;
	
	public static SelectionID getID(int ordinal) {
		if (ordinal == 0)
			return SINGLE;
		if (ordinal == 1)
			return MULTI;
		if (ordinal == 2)
			return BOX;
		if (ordinal == 3)
			return TASK;
		return null;
	}
}
