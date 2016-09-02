package afdx.afdx;

import java.util.Comparator;

public class VLLatency implements Comparator<VLLatency> {
	private final int VLID;
	private final double max, min, mean;

	public VLLatency(int vLID, double mean, double min, double max) {
		super();
		VLID = vLID;
		this.max = max;
		this.min = min;
		this.mean = mean;
	}

	public int getVLID() {
		return VLID;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public double getMean() {
		return mean;
	}

	@Override
	public int compare(VLLatency v1, VLLatency v2) {
		int result = 0;

		if (v2.getMean() > v1.getMean())
			result = -1;
		else if (v2.getMean() < v1.getMean())
			result = 1;

		return result;
	}

}
