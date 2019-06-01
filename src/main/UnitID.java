package main;

public enum UnitID {
	//Structures
	CITY, FACTORY, AIRFIELD, PORT,
	//Forces
	INFANTRY, CAVALRY, ARTILLERY, SHIP, PLANE, 
	//Projectiles
	BULLET, AIRBULLET, ANTIPERSONEL, SHELL, TORPEDO, BOMB,
	//Weights
	LIGHT, MEDIUM, HEAVY, NONE;
	
	public boolean isLandUnit(){
		return this == INFANTRY || this == CAVALRY || this == ARTILLERY;
	}
	public boolean isBuilding(){
		return this == AIRFIELD || this == CITY || this == FACTORY || this == PORT;
	}
}
