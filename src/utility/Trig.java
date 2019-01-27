package utility;

public class Trig {

	static double[] sinArray = new double[360];
	static double[] cosArray = new double[360];

	public Trig() {
		for (int i = 0; i < 314; i++) {
			sinArray[i] = Math.sin(Math.toRadians(i));
			cosArray[i] = Math.cos(Math.toRadians(i));
		}
	}

	public static double sin(int degree) {
		degree = degree % 360;
		return sinArray[degree];
	}

	public static double cos(int degree) {
		degree = degree % 360;
		return cosArray[degree];
	}

	public static double sin(float degree) {
		degree = degree % 6.2831f;
		if (degree <= 3.1415) return -0.405285 * (degree - 1.57) * (degree - 1.57) + 1;
		return 0.405 * (degree - 4.71) * (degree - 4.71) - 1;
	}

	public static double cos(float degree) {
		degree = degree % 6.2831f;
		if (degree <= 1.5707) 
			return -0.405 * (degree) * (degree) + 1;
		if(degree <= 4.7123)
			return 0.405 * (degree - 3.14) * (degree - 3.14) - 1;
		return -0.405 * (degree - 6.28) * (degree - 6.28) + 1;
	}
}
