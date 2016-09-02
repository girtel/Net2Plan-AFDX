package afdx.afdx;

import java.util.HashSet;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Route;

public class VL implements Comparable<VL> {
	private String name;
	private Route route;
	private MulticastTree tree;

	public VL(Route route) {
		super();
		this.route = route;
		this.tree = null;
	}

	public VL(MulticastTree tree) {
		super();
		this.route = null;
		this.tree = tree;
	}

	public double getBagMs() {
		if (route != null)
			return Double.parseDouble(route.getDemand().getAttribute(AFDXParameters.ATT_VL_BAG_MS));
		else
			return Double.parseDouble(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_BAG_MS));
	}

	public int getArrivalType() {
		if (route != null)
			return Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_PERVL_PACKET_ARRIVAL_TYPE));
		else
			return Integer
					.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_PERVL_PACKET_ARRIVAL_TYPE));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Node getSource() {
		if (route != null)
			return route.getIngressNode();
		else
			return tree.getIngressNode();
	}

	public Set<Node> getDestiny() {
		if (route != null) {
			Set<Node> dest = new HashSet<Node>();
			dest.add(route.getEgressNode());
			return dest;
		} else
			return tree.getEgressNodes();
	}

	public int getLmax() {
		if (route != null)
			return Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
		else
			return Integer.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
	}

	public int getLmaxIPPacket() {
		int result = (int) (AFDXParameters.IPHeaderBytes + AFDXParameters.UDPHeaderBytes);

		if (route != null)
			result += Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
		else
			result += Integer.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));

		return result;
	}

	public Route getRoute() {
		return route;
	}

	public MulticastTree getTree() {
		return tree;
	}

	public boolean isThisVL(VL vl) {
		boolean result = false;

		try {
			if (this.route.getIndex() == vl.getRoute().getIndex())
				result = true;
		} catch (Exception e) {
			try {
				if (this.tree.getIndex() == vl.getTree().getIndex())
					result = true;
			} catch (Exception e1) {
			}
		}

		return result;
	}

	@Override
	public int compareTo(VL o) {
		int result = 1;

		if (getLmax() < o.getLmax())
			result = -1;

		return result;
	}

}
