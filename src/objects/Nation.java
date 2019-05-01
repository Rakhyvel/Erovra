package objects;

import java.util.ArrayList;

import main.Main;
import main.UnitID;
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
	public int coins = 40;
	boolean ai = true;

	// Units
	private final int cavalryCost = 20;
	private final int artilleryCost = 20;
	private final int shipCost = 20;
	private int planeCost = 20;

	// Structures
	private int cityCost = 15;
	private int portCost = 20;
	private int factoryCost = 15;
	private int airfieldCost = 20;

	// Supremacy
	// (Number of destroyers/fighters each nation has, if they have less, make
	// more)
	public int airSupremacy = 0;
	public int seaSupremacy = 0;
	public int landSupremacy = 0;

	public String name;
	public ArrayList<Unit> unitArray = new ArrayList<Unit>(20);
	public ArrayList<Unit> engagedUnits = new ArrayList<Unit>();
	public ArrayList<Projectile> projectileArray = new ArrayList<Projectile>();
	public ArrayList<Coin> coinArray = new ArrayList<Coin>();
	public Nation enemyNation;
	public Unit capital;
	public boolean defeated;

	public Nation(int color, String name) {
		this.color = color;
		this.name = name;
	}

	/**
	 * Adds a Unit object to the nations arrayList of Units
	 * 
	 * @param unit The Unit subclass to be added.
	 * @see Unit
	 */
	public void addUnit(Unit unit) {
		unitArray.add(unit);
	}
	
	public void removeUnit(Unit unit) {
		unitArray.remove(unit);
		engagedUnits.remove(unit);
	}

	/**
	 * Adds a Projectile object to the nations arrayList of projectiles
	 * 
	 * @param projectile The Procjectile subclass to be added.
	 * @see Projectile
	 * @see Unit
	 */
	public void addProjectile(Projectile projectile) {
		projectileArray.add(projectile);
	}

	/**
	 * Adds a Coin object to the nations arrayList of coins
	 * 
	 * @param projectile The Coin to be added
	 * @see Coin
	 */
	public void addCoin(Point position) {
		coinArray.add(new Coin(position, this));
	}

	/**
	 * Removes all Unit objects from the nation's Unit ArrayList. Used in city
	 * location finding
	 */
	public void purgeAll() {
		for (int i = 0; i < unitArray.size(); i++) {
			unitArray.remove(0);
		}
	}

	/**
	 * Sets the capital of the nation to the one specified
	 * 
	 * @param id The index of the Unit Array to be made capital of the nation.
	 */
	public void setCaptial(int id) {
		getUnit(id).capital = true;
		capital = getUnit(id);
		capital.setWeight(UnitID.HEAVY);
		capital.setDefense(4);
	}

	/**
	 * @return The size of the Unit ArrayList
	 */
	public int unitSize() {
		return unitArray.size();
	}

	/**
	 * @return The size of the Projectile ArrayList
	 */
	public int projectileSize() {
		return projectileArray.size();
	}

	/**
	 * @return The size of the Coin ArrayList
	 */
	public int coinSize() {
		return coinArray.size();
	}

	/**
	 * @param id The index of the Unit in the Unit ArrayList
	 * @return The Unit at the index specified
	 */
	public Unit getUnit(int id) {
		if (id > unitSize()) {
			return null;
		}
		return unitArray.get(id);
	}

	/**
	 * @param id The index of the Projectile in the Projectile ArrayList
	 * @return The Projectile at the index specified
	 */
	public Projectile getProjectile(int id) {
		if (id > projectileSize()) {
			return null;
		}
		return projectileArray.get(id);
	}

	/**
	 * @param id The index of the Coin in the Coin ArrayList
	 * @return The Coin at the index specified
	 */
	public Coin getCoin(int id) {
		if (id > coinSize()) {
			return null;
		}
		return coinArray.get(id);
	}

	/**
	 * Sets the enemy nation
	 * 
	 * @param nation The nation that will be made an enemy
	 */
	public void setEnemyNation(Nation nation) {
		if (nation != this) {
			enemyNation = nation;
		}
	}

	/**
	 * @return The quantity of coins in the nations balance
	 */
	public int getCoinAmount() {
		return coins;
	}

	/**
	 * First checks to see if there is enough coins, then checks to see if the land
	 * is able to have a city
	 * 
	 * @param position The position of the infantry unit buying
	 * @return Whether or not the settlement was successful
	 */
	public boolean buyCity(Point position) {
		if (coins >= getCityCost()) {
			Point cityPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
					((int) (position.getY() / 64)) * 64 + 32);
			if (Map.getArray(cityPoint) > 0.5f && Map.getArray(cityPoint) < 1) {
				if(checkProximity(position)) {
					coins -= getCityCost();
					setCityCost(getCityCost() * 2);
					addUnit(new City(cityPoint, this, Main.ticks));
					return true;
				}
			} else if(!isAIControlled()){
				Main.world.errorMessage.showErrorMessage("Cannot build a city here!");
			}
		} else if(!isAIControlled()){
			Main.world.errorMessage.showErrorMessage("Insufficient funds!");
		}
		return false;
	}

	/**
	 * First checks to see if there is enough coins, then checks to see if the land
	 * is able to have a factory
	 * 
	 * @param position  The position of the infantry unit buying
	 */
	public void buyFactory(Point position) {
		if(coins >= factoryCost) {
			Point factoryPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
					((int) (position.getY() / 64)) * 64 + 32);
			if (Map.getArray(factoryPoint) > 0.5f && Map.getArray(factoryPoint) < 1) {
				if(checkProximity(position)) {
					coins -= getFactoryCost();
					setFactoryCost(getFactoryCost() * 2);
					addUnit(new Factory(factoryPoint, this));
				}
			} else if(!isAIControlled()){
				Main.world.errorMessage.showErrorMessage("Cannot build a factory here!");
			}
		} else if(!isAIControlled()){
			Main.world.errorMessage.showErrorMessage("Insufficient funds!");
		}
	}

	/**
	 * First checks to see if there is enough coins,
	 * then checks to see if the land is able to have a port
	 * 
	 * @param position  The position of the infantry unit buying
	 */
	public boolean buyPort(Point position) {
		if(coins >= portCost) {
			Point portPoint = new Point(((int) (position.getX() / 64)) * 64 + 32, ((int) (position.getY() / 64)) * 64 + 32);
			float land = Map.getArray(portPoint);
			if (land < 0.5f && land > 0) {
				if(checkProximity(position)) {
					coins -= getPortCost();
					setPortCost(getPortCost() * 2);
					addUnit(new Port(portPoint, this));
					return true;
				}
			} else if(!isAIControlled()){
				Main.world.errorMessage.showErrorMessage("Cannot build a port on a land tile!");
				return false;
			}
		} else if(!isAIControlled()){
			Main.world.errorMessage.showErrorMessage("Insufficient funds!");
			return false;
		}
		return false;
	}

	public void buyAirfield(Point position) {
		if(coins >= airfieldCost) {
			Point airfieldPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
					((int) (position.getY() / 64)) * 64 + 32);
			float land = Map.getArray(airfieldPoint);
			if (land > 0.5f && land < 1) {
				if(checkProximity(position)) {
					coins -= getAirfieldCost();
					setAirfieldCost(getAirfieldCost() * 2);
					addUnit(new Airfield(airfieldPoint, this));
				}
			} else if(!isAIControlled()){
				Main.world.errorMessage.showErrorMessage("Cannot build an airfield here!");
			}
		} else if(!isAIControlled()){
			Main.world.errorMessage.showErrorMessage("Insufficient funds!");
		}
	}

	public void defeat() {
		Main.world.defeatedName = name;
	}

	public void victory() {
		Main.world.defeatedName = enemyNation.name;
	}

	public void setAIControlled(boolean ai) {
		this.ai = ai;
	}

	public boolean isAIControlled() {
		return ai;
	}

	public boolean checkProximity(Point position) {
		int smallestDistance = 32;
		for (int i = 0; i < unitSize(); i++) {
			Unit tempUnit = getUnit(i);
			if (tempUnit.getID() == UnitID.CITY || tempUnit.getID() == UnitID.PORT || tempUnit.getID() == UnitID.FACTORY
					|| tempUnit.getID() == UnitID.AIRFIELD) {
				Point tempPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
						((int) (position.getY() / 64)) * 64 + 32);
				int tempDist = (int) tempUnit.getPosition().getCabDist(tempPoint);
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
				}
			}
		}
		for (int i = 0; i < enemyNation.unitSize(); i++) {
			Unit tempUnit = enemyNation.getUnit(i);
			if (tempUnit.getID() == UnitID.CITY || tempUnit.getID() == UnitID.PORT || tempUnit.getID() == UnitID.FACTORY
					|| tempUnit.getID() == UnitID.AIRFIELD) {
				Point tempPoint = new Point(((int) (position.getX() / 64)) * 64 + 32,
						((int) (position.getY() / 64)) * 64 + 32);
				int tempDist = (int) tempUnit.getPosition().getCabDist(tempPoint);
				if (tempDist < smallestDistance) {
					smallestDistance = tempDist;
				}
			}
		}
		if(!isAIControlled() && smallestDistance < 32) {
			Main.world.errorMessage.showErrorMessage("Cannot build a building here!");
		}
		return smallestDistance >= 32;
	}

	public int getCityCost() {
		return cityCost;
	}

	public void setCityCost(int cityCost) {
		this.cityCost = cityCost;
	}

	public int getFactoryCost() {
		return factoryCost;
	}

	public void setFactoryCost(int factoryCost) {
		this.factoryCost = factoryCost;
	}

	public int getPortCost() {
		return portCost;
	}

	public void setPortCost(int portCost) {
		this.portCost = portCost;
	}

	public int getAirfieldCost() {
		return airfieldCost;
	}

	public void setAirfieldCost(int airfieldCost) {
		this.airfieldCost = airfieldCost;
	}

	public int getCavalryCost() {
		return cavalryCost;
	}

	public int getArtilleryCost() {
		return artilleryCost;
	}

	public int getShipCost() {
		return shipCost;
	}

	public int getPlaneCost() {
		return planeCost;
	}
	public void setPlaneCost(int planeCost) {
		this.planeCost = planeCost;
	}
	
	public void setLandSupremacy(int i){
		if(landSupremacy+i >= 0)
			landSupremacy+=i;
	}
}
