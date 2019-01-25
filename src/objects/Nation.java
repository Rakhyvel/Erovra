package objects;

import java.util.ArrayList;

import main.Main;
import main.UnitID;
import terrain.Map;
import utility.Point;

public class Nation {
	int color;
	int coins = 9;
	int cityCost = 10;
	int portCost = 15;
	int factoryCost = 25;
	int cavalryCost = 20;
	int artilleryCost = 100;
	int shipCost = 20;
	String name;
	ArrayList<Unit> unitArray = new ArrayList<Unit>();
	public ArrayList<Projectile> projectileArray = new ArrayList<Projectile>();
	ArrayList<Coin> coinArray = new ArrayList<Coin>();
	Nation enemyNation;
	Unit capital;

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

	public void purgeAll() {
		for (int i = 0; i < unitArray.size(); i++) {
			unitArray.remove(0);
		}
	}

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

	public boolean buyCity(Point position) {
		if (coins >= cityCost) {
			Point cityPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
					((int) (position.getY() / 64)) * 64 + 32);
			if (Map.getArray(cityPoint) > 0.5f) {
				coins -= cityCost;
				cityCost += 10;
				addUnit(new City(cityPoint, this, Main.ticks));
				return true;
			}
		}
		return false;
	}

	public void buyFactory(Point position) {
		Point factoryPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
				((int) (position.getY() / 64)) * 64 + 32);
		if (Map.getArray(factoryPoint) > 0.5f && coins >= factoryCost) {
			coins -= factoryCost;
			factoryCost += 25;
			addUnit(new Factory(factoryPoint, this));
		}
	}

	public void buyPort(Point position) {
		Point portPoint = new Point(((int) (position.getX() / 64)) * 64 + 32, ((int) (position.getY() / 64)) * 64 + 32);
		float land = Map.getArray(portPoint);
		if (land < 0.5f && coins >= portCost) {
			coins -= portCost;
			portCost += 15;
			addUnit(new Port(portPoint, this));
		}
	}
}
