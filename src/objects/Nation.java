package objects;

import java.util.ArrayList;

import main.Main;
import main.StateID;
import objects.projectiles.Projectile;
import objects.units.Airfield;
import objects.units.City;
import objects.units.Factory;
import objects.units.Port;
import objects.units.Unit;
import terrain.Map;
import utility.Point;

public class Nation {

	public int color;
	public int coins = 9;
	
	//Units
	public int cavalryCost = 20;
	public int artilleryCost = 20;
	public int shipCost = 20;
	public int planeCost = 20;
	
	//Structures
	public int cityCost = 10;
	public int portCost = 20;
	public int factoryCost = 15;
	public int airfieldCost = 20;
	
	//Supremacy
	//(Number of destroyers/fighters each nation has, if they have less, make more)
	public int airSupremacy = 0;
	public int seaSupremacy = 0;
	public int landSupremacy = 0;
	
	public String name;
	public ArrayList<Unit> unitArray = new ArrayList<Unit>(20);
	public ArrayList<Projectile> projectileArray = new ArrayList<Projectile>();
	public ArrayList<Coin> coinArray = new ArrayList<Coin>();
	public Nation enemyNation;
	public Unit capital;
	public boolean defeated;

	public Nation(int color, String name) {
		this.color = color;
		this.name = name;
	}

	public void addUnit(Unit unit) {
		unitArray.add(unit);
	}

	public void addProjectile(Projectile projectile) {
		projectileArray.add(projectile);
	}

	public void addCoin(Point position) {
		coinArray.add(new Coin(position, this));
	}

	// purgeAll(): removes every object from unitArray
	public void purgeAll() {
		for (int i = 0; i < unitArray.size(); i++) {
			unitArray.remove(0);
		}
	}

	// setCapital(int id): sets the object at a given id to be the nation's
	// capital
	public void setCaptial(int id) {
		getUnit(id).capital = true;
		capital = getUnit(id);
	}

	public int unitSize() {
		return unitArray.size();
	}

	public int projectileSize() {
		return projectileArray.size();
	}

	public int coinSize() {
		return coinArray.size();
	}

	public Unit getUnit(int id) {
		if (unitArray.isEmpty()) {
			return null;
		}
		return unitArray.get(id);
	}

	public Projectile getProjectile(int id) {
		if (id < projectileArray.size()) {
			return projectileArray.get(id);
		}
		return null;
	}

	public Coin getCoin(int id) {
		return coinArray.get(id);
	}

	public void setEnemyNation(Nation nation) {
		if (nation != this) {
			enemyNation = nation;
		}
	}

	public int getCoinAmount() {
		return coins;
	}

	// buyCity(Point position): First checks to see if there is enough coins,
	// then checks to see if the land is able to have a city
	public boolean buyCity(Point position) {
		if (coins >= cityCost) {
			Point cityPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
					((int) (position.getY() / 64)) * 64 + 32);
			if (Map.getArray(cityPoint) > 0.5f) {
				coins -= cityCost;
				cityCost += 7;
				addUnit(new City(cityPoint, this, Main.ticks));
				return true;
			}
		}
		return false;
	}

	// buyFactory(Point position): First checks to see if there is enough coins,
	// then checks to see if the land is able to have a factory
	public void buyFactory(Point position) {
		Point factoryPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
				((int) (position.getY() / 64)) * 64 + 32);
		if (Map.getArray(factoryPoint) > 0.5f && coins >= factoryCost) {
			coins -= factoryCost;
			factoryCost += 10;
			addUnit(new Factory(factoryPoint, this));
		}
	}

	// buyPort(Point position): First checks to see if there is enough coins,
	// then checks to see if the land is able to have a port
	public void buyPort(Point position) {
		Point portPoint = new Point(((int) (position.getX() / 64)) * 64 + 32, ((int) (position.getY() / 64)) * 64 + 32);
		float land = Map.getArray(portPoint);
		if (land < 0.5f && land > 0 && coins >= portCost) {
			coins -= portCost;
			portCost += 10;
			addUnit(new Port(portPoint, this));
		}
	}

	public void buyAirfield(Point position) {
		Point airfieldPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
				((int) (position.getY() / 64)) * 64 + 32);
		float land = Map.getArray(airfieldPoint);
		if (land > 0.5f && coins >= airfieldCost) {
			coins -= airfieldCost;
			airfieldCost += 10;
			addUnit(new Airfield(airfieldPoint, this));
		}
	}
	public void defeat() {
		if(name.contains("Sweden")) {
			Main.setState(StateID.DEFEAT);
		} else {
			Main.setState(StateID.VICTORY);
		}
	}
	public void victory() {
		Main.setState(StateID.VICTORY);
	}
}
