package utility;

public class Vector {

	double x, y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector(Vector vector) {
		this.x = vector.x;
		this.y = vector.y;
	}

	public double getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public Vector normalize() {
		double magnitude = Math.sqrt(x * x + y * y);
		if (magnitude == 0) {
			return new Vector(0, 0);
		}
		x /= magnitude;
		y /= magnitude;
		return new Vector(x, y);
	}

	public Vector scalar(double t) {
		return new Vector(x * t, y * t);
	}

	public float getRadian() {
		if(y == 0) {
			if(x == 0)
				return 0;
			if (x > 0)
				return -(float)Math.PI/2;
			return (float)Math.PI/2;
		}
		return (float) (Math.atan(x / y));
	}

	public double dot(Vector v) {
		// Projects one vector onto another, giving a scalar value
		return x * v.x + y * v.y;
	}

	public Vector addVec(Vector v) {
		return new Vector(x+v.x,y+v.y);
	}
	
	public Vector subVec(Vector v) {
		return new Vector(x-v.x, y-v.y);
	}
	
	public double magnitude(){
		return x * x + y * y;
	}
}
