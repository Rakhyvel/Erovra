package main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import objects.Nation;
import output.Render;

public class World {

	public ArrayList<Nation> nationArray = new ArrayList<Nation>();
	Random rand = new Random();

	// tick(double t): Calls the tick method for each object in the game, takes
	// t as the time in millis since last tick
	public void tick(double t) {
		for (int i = 0; i < nationArray.size(); i++) {
			for (int i2 = nationArray.get(i).unitSize() - 1; i2 >= 0; i2--) {
				nationArray.get(i).getUnit(i2).tick(t);
			}
			for (int i2 = 0; i2 < nationArray.get(i).projectileSize(); i2++) {
				nationArray.get(i).getProjectile(i2).tick(t);
			}
			for (int i2 = 0; i2 < nationArray.get(i).coinSize(); i2++) {
				nationArray.get(i).getCoin(i2).tick(t);
			}
		}
	}

	// render(Render r): Calls for each object in the game to draw, takes in the
	// custom Render object, r.
	public void render(Render r) {
		for (int i = 0; i < nationArray.size(); i++) {
			for (int i2 = nationArray.get(i).unitSize() - 1; i2 >= 0; i2--) {
				if (nationArray.get(i).getUnit(i2).getID() != UnitID.PLANE) nationArray.get(i).getUnit(i2).render(r);
			}
			for (int i2 = 0; i2 < nationArray.get(i).projectileSize(); i2++) {
				nationArray.get(i).getProjectile(i2).render(r);
			}
			for (int i2 = 0; i2 < nationArray.get(i).coinSize(); i2++) {
				nationArray.get(i).getCoin(i2).render(r);
			}
			for (int i2 = nationArray.get(i).unitSize() - 1; i2 >= 0; i2--) {
				if (nationArray.get(i).getUnit(i2).getID() == UnitID.PLANE) nationArray.get(i).getUnit(i2).render(r);
			}
		}
		r.drawImageScreen(960, 12, 16, r.coin, 255 << 16 | 255 << 8);

	}

	// drawCoins(Graphics g): Draws the ammount of coins
	public void drawCoins(Graphics g) {
		g.setColor(new Color(0, 0, 0));
		g.drawString(String.valueOf(nationArray.get(1).getCoinAmount()), 973, 17);
		g.setColor(new Color(255, 255, 255));
		g.drawString(String.valueOf(nationArray.get(1).getCoinAmount()), 972, 16);
	}
}
