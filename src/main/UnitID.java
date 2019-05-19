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
	
	public static boolean isLandUnit(UnitID id){
		return id == INFANTRY || id == CAVALRY || id == ARTILLERY;
	}
	public static boolean isBuilding(UnitID id){
		return id == AIRFIELD || id == CITY || id == FACTORY || id == PORT;
	}
}
