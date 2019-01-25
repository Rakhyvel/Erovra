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

	public void tick(double t) {
		//RUN BACKWARDS!!!!!!!!!!!!
		for (int i = 0; i < nationArray.size(); i++) {
			for (int i2 = nationArray.get(i).unitSize()-1; i2 >= 0; i2--) {
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

	public void render(Render r) {
		for (int i = 0; i < nationArray.size(); i++) {
			for (int i2 = 0; i2 < nationArray.get(i).unitSize(); i2++) {
				nationArray.get(i).getUnit(i2).render(r);
			}
			for (int i2 = 0; i2 < nationArray.get(i).projectileSize(); i2++) {
				nationArray.get(i).getProjectile(i2).render(r);
			}
			for (int i2 = 0; i2 < nationArray.get(i).coinSize(); i2++) {
				nationArray.get(i).getCoin(i2).render(r);
			}
		}
		r.drawImageScreen(960, 12, 16, r.coin, 255 << 16 | 255 << 8);

	}

	public void drawCoins(Graphics g) {
		g.setColor(new Color(0, 0, 0));
		g.drawString(String.valueOf(nationArray.get(1).getCoinAmount()), 973, 17);
		g.setColor(new Color(255, 255, 255));
		g.drawString(String.valueOf(nationArray.get(1).getCoinAmount()), 972, 16);
	}
}
