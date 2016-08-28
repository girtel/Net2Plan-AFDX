package afdx.algorithm;

public class NCFunction {
	// Demand function
	final double d_m;// bps
	final double d_y0;// bits

	// Service function
	final double s_m;// bps
	final double s_x0;// ms

	public NCFunction(double d_m, double d_y0, double s_m, double s_x0) {
		if (d_m >= s_m)
			throw new RuntimeException("Demand rate is highe than Service Rate");

		this.d_m = d_m;
		this.d_y0 = d_y0;
		this.s_m = s_m;
		this.s_x0 = s_x0;
	}

	public NCFunction delayMs(double delayInMs) {
		if (d_m >= s_m)
			throw new RuntimeException("Demand rate is highe than Service Rate");

		double d_y = d_y0 + d_m * delayInMs;

		NCFunction result = new NCFunction(this.d_m, d_y, this.s_m, this.s_x0);

		return result;
	}

	public double getMaxDelayInMs() {
		double result = s_x0;

		double t = d_y0 / s_m;

		result += t;

		return result;
	}

	public double getDelayInMs(double time) {
		double result = 0;

		double s_y = s_m * time;

		if (s_y < d_y0)
			result = time;
		else {
			double t = s_y / d_m;
			result = time - t;
		}

		return result;
	}

	public double getMaxQueueSizeInBits() {
		double result = 0;

		double d_y = d_m * s_x0 + d_y0;

		result = d_y;

		return result;
	}

	public double getQueueSizeInBits(double time) {
		double result = 0;

		if (time < s_x0)
			result = d_m * s_x0 + d_y0;
		else {
			double d_y = d_m * s_x0 + d_y0;
			double s_y = s_m * time;
			result = d_y - s_y;
		}

		return result;
	}
}
