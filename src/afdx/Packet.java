package afdx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Node;

public class Packet {
	final private List<Node> arrivalNode;
	final private List<Double> arrivalTimeToNode;
	final private double arrivalTimeToRegulator;
	final private Node egressNode;
	final private VL vl;
	private double leavingNetworkTime;
	final private List<Double> queue;
	final private List<Double> latency;
	private double lastLatency;
	private double lastJitter;
	private Link previousLink;
	public boolean regulated = false;
	private boolean technologyLatencyWaiting = true;
	private int protocol = AFDXParameters.UDPProtocol;
	// private List<Set<Route>> routesInTheLink;
	// private List<Set<MulticastTree>> treesInTheLink;
	private List<List<List<VL>>> vlPerLinks;
	private int biggerIPPacketPreceding = 0;

	private Packet(Packet p) {
		this.arrivalNode = new LinkedList<Node>(p.arrivalNode);
		this.arrivalTimeToNode = new LinkedList<Double>(p.arrivalTimeToNode);
		this.arrivalTimeToRegulator = p.arrivalTimeToRegulator;
		this.leavingNetworkTime = p.leavingNetworkTime;
		this.egressNode = p.egressNode;
		this.vl = p.vl;
		this.queue = new LinkedList<Double>(p.queue);
		this.latency = new LinkedList<Double>(p.latency);
		this.lastLatency = p.lastLatency;
		this.lastJitter = p.lastJitter;
		this.previousLink = p.previousLink;
		this.regulated = p.regulated;
		this.technologyLatencyWaiting = p.technologyLatencyWaiting;
		this.protocol = p.protocol;
		this.vlPerLinks = new ArrayList<List<List<VL>>>(p.vlPerLinks);
		this.biggerIPPacketPreceding = p.biggerIPPacketPreceding;
	}

	public Packet(VL vl, Node egressNode, double time) {
		this.arrivalNode = new LinkedList<Node>();
		this.arrivalTimeToNode = new LinkedList<Double>();
		this.arrivalTimeToRegulator = time;
		this.leavingNetworkTime = -1;
		this.egressNode = egressNode;
		this.vl = vl;
		this.queue = new LinkedList<Double>();
		this.latency = new LinkedList<Double>();
		this.previousLink = null;
		this.vlPerLinks = new ArrayList<List<List<VL>>>();
	}

	public Packet copy() {
		return new Packet(this);
	}

	public List<Double> getArrivalTimesToNodes() {
		return arrivalTimeToNode;
	}

	public double getArrivalTimeToRegulator() {
		return arrivalTimeToRegulator;
	}

	public List<Node> getArrivalNodes() {
		return arrivalNode;
	}

	public VL getVl() {
		return vl;
	}

	public Link getPreviousLink() {
		return previousLink;
	}

	public void setPreviousLink(Link previousLink) {
		this.previousLink = previousLink;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public double getLeavingNetworkTime() {
		return leavingNetworkTime;
	}

	public void setLeavingNetworkTime(double leavingNetworkTime) {
		this.leavingNetworkTime = leavingNetworkTime;
	}

	public boolean isTechnologyLatencyWaiting() {
		return technologyLatencyWaiting;
	}

	public void setTechnologyLatencyWaiting(boolean technologyLatencyWaiting) {
		this.technologyLatencyWaiting = technologyLatencyWaiting;
	}

	public List<List<List<VL>>> getVlPerLinks() {
		return vlPerLinks;
	}

	public void setVlPerLinks(List<List<List<VL>>> packetLengthsPerLinks) {
		this.vlPerLinks = packetLengthsPerLinks;
	}

	public List<Double> getLatency() {
		return latency;
	}

	public double getLastLatency() {
		return lastLatency;
	}

	public void setLastLatency(double lastLatency) {
		this.lastLatency = lastLatency;
	}

	public double getLastJitter() {
		return lastJitter;
	}

	public void setLastJitter(double lastJitter) {
		this.lastJitter = lastJitter;
	}

	public Node getEgressNode() {
		return egressNode;
	}

	public int getBiggerIPPacketPreceding() {
		return biggerIPPacketPreceding;
	}

	public void setBiggerIPPacketPreceding(int biggerIPPacketPreceding) {
		this.biggerIPPacketPreceding = biggerIPPacketPreceding;
	}

	public boolean isVLInLink(VL vl, int linkNumber) {
		if (linkNumber < 0)
			return false;

		List<List<VL>> vls = vlPerLinks.get(linkNumber);
		for (List<VL> list : vls) {
			for (VL vl2 : list) {
				if (vl2.isThisVL(vl)) {
					return true;
				}
			}
		}

		return false;
	}

	public double getMinimumLatencyInMs() {
		double result = 0;

		List<Link> links;

		if (vl.getRoute() != null)
			links = vl.getRoute().getSeqLinksRealPath();
		else
			links = vl.getTree().getSeqLinksToEgressNode(egressNode);

		for (Link link : links) {
			double nodeServiceTimeInMs = AFDXParameters.TLSwInMs;
			if (links.get(0) == link)
				nodeServiceTimeInMs = AFDXParameters.TLTxInMs;

			result += nodeServiceTimeInMs;

			result += 1000 * 8 * (AFDXParameters.ETHHeaderBytes + vl.getLmaxIPPacket()) / link.getCapacity();
		}

		result += AFDXParameters.TLRxInMs;

		return result;
	}

	public void print() {
		if (vl.getRoute() != null) {
			System.out.println("Print route");
			System.out.println("Route index " + vl.getRoute().getIndex());
			System.out.println("\nRoute parameters:");
			System.out.println(
					"\t L MAX bytes: \t" + vl.getRoute().getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
			System.out.println("\t BAG ms: \t" + vl.getRoute().getDemand().getAttribute(AFDXParameters.ATT_VL_BAG_MS));
		} else {
			System.out.println("Print tree");
			System.out.println("Tree index " + vl.getTree().getIndex());
			System.out.println("\nTree parameters:");
			System.out.println("\t L MAX bytes: \t"
					+ vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
			System.out.println(
					"\t BAG ms: \t" + vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_BAG_MS));
		}

		System.out.println("Bigger Packet preceding " + biggerIPPacketPreceding);

		System.out.println("\nArrival time to nodes");
		for (int i = 0; i < arrivalNode.size(); i++) {
			System.out.println("\tNode: " + arrivalNode.get(i).getName() + "\tArrival time: "
					+ AFDXTools.df_5.format(arrivalTimeToNode.get(i)) + " ("
					+ AFDXTools.df_5.format(((i == 0 ? 0 : arrivalTimeToNode.get(i) - arrivalTimeToNode.get(i - 1))))
					+ ")");

		}
		System.out.println("\n\tLatency " + AFDXTools.df_5.format((leavingNetworkTime - arrivalTimeToNode.get(0))));

		System.out.println("\nVLs in the links");
		for (int i = 0; i < vlPerLinks.size(); i++) {
			List<List<VL>> lengths = vlPerLinks.get(i);

			System.out.println("\nLink " + i);

			for (List<VL> vlList : lengths) {
				for (VL vl : vlList) {
					if (vl.getRoute() != null)
						System.out.println("\n\tRoute index " + vl.getRoute().getIndex() + " max packet lenght "
								+ vl.getLmax() + " Bytes IP Packet Size " + vl.getLmaxIPPacket() + " Bytes");
					else
						System.out.println("\n\tTree index " + vl.getTree().getIndex() + " max packet lenght "
								+ vl.getLmax() + " Bytes IP Packet Size " + vl.getLmaxIPPacket() + " Bytes");
				}
			}
		}

		System.out.println();
	}

}
