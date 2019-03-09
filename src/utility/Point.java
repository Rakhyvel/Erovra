package utility;

public class Point {
	double x,y;
	public Point(double x, double y) {
		this.setX(x);
		this.setY(y);
	}
	public Point(Point p) {
		this.setX(p.getX());
		this.setY(p.getY());
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public Point addPoint(Point p2) {
		return new Point(x+p2.x,y+p2.y);
	}
	public Point addVector(Vector v) {
		return new Point(x+v.x,y+v.y);
	}
	public double getDist(Point p2) {
		return (x-p2.x)*(x-p2.x)+(y-p2.y)*(y-p2.y);
	}
	public Vector getTargetVector(Point p) {
		if(getDist(p) == 0)
			return new Vector(0,0);
		return new Vector(p.x-x,p.y-y);
	}
	public Vector subVec(Point p) {
		return new Vector(p.x-x,p.y-y);
	}
	public double getCabDist(Point p2) {
		return Math.abs(x-p2.x)+Math.abs(y-p2.y);
	}
	public Point multScalar(double scalar){
		x = x*scalar;
		y = y*scalar;
		return this;
	}
}
