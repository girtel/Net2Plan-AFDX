package afdx;

public class VL {
	private String name;
	private String source;
	private String destiny[];
	private int lmax;
	final int lengthBytes;
	final double bagMs;
	final int arrivalType;
	private int routeIndex = -1;
	private int treeIndex = -1;

	public VL(int lengthBytes, double bagMs, int arrivalType) {
		super();
		this.lengthBytes = lengthBytes;
		this.bagMs = bagMs;
		this.arrivalType = arrivalType;
	}

	public int getLengthBytes() {
		return lengthBytes;
	}

	public double getBagMs() {
		return bagMs;
	}

	public int getArrivalType() {
		return arrivalType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String[] getDestiny() {
		return destiny;
	}

	public void setDestiny(String[] destiny) {
		this.destiny = destiny;
	}

	public int getLmax() {
		return lmax;
	}

	public void setLmax(int lmax) {
		this.lmax = lmax;
	}

	public int getRouteIndex() {
		return routeIndex;
	}

	public void setRouteIndex(int routeIndex) {
		this.routeIndex = routeIndex;
	}

	public int getTreeIndex() {
		return treeIndex;
	}

	public void setTreeIndex(int treeIndex) {
		this.treeIndex = treeIndex;
	}

}
