package objects.units;

import main.UnitID;
import objects.Nation;
import objects.gui.DropDown;
import output.Render;
import utility.Point;

public abstract class Industry extends Unit {

	protected int start = 0;
	protected float maxStart = 1;
	protected UnitID product;
	protected UnitID productWeight;

	public Industry(Point position, Nation nation, UnitID weight) {
		super(position, nation, weight);
	}

	public UnitID getProduct() {
		if (product == null) return UnitID.NONE;
		return product;
	}

	public void setProduct(UnitID product) {
		this.product = product;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public UnitID getProductWeight() {
		return productWeight;
	}

	public void setProductWeight(UnitID productWeight) {
		this.productWeight = productWeight;
	}

	public boolean buyUnit(UnitID product, UnitID productWeight, int cost, int time) {
		if (nation.coins >= cost) {
			nation.coins -= cost;
			this.setProduct(product);
			this.setProductWeight(productWeight);
			setStart(time);
			maxStart = getStart();
			return true;
		} else {
			this.setProduct(UnitID.NONE);
			this.setProductWeight(UnitID.NONE);
			setStart(1);
			maxStart = getStart();
		}
		return false;
	}

	@Override
	public abstract void tick(double t);

	@Override
	public abstract void render(Render r);

	@Override
	public abstract void dropDownDecide(DropDown d);

	@Override
	public abstract void dropDownRender(Render r, DropDown d);

	public abstract void addProduct();

	public abstract void decideNewProduct();

}
