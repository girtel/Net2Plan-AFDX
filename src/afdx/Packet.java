package afdx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Route;

public class Packet {
	final private List<Node> arrivalNode;
	final private List<Double> arrivalTimeToNode;
	final private double arrivalTimeToRegulator;
	final private Route route;
	final private MulticastTree tree;
	final private Node egressNode;
	final private VL vl;
	private double leavingNetworkTime;
	final private List<Double> latency;
	private double lastLatency;
	private double lastJitter;
	private Link previousLink;
	public boolean regulated = false;
	private boolean technologyLatencyWaiting = true;
	private int protocol = AFDXParameters.UDPProtocol;
	private List<Set<Route>> routesInTheLink;
	private List<Set<MulticastTree>> treesInTheLink;
	private List<List<List<Integer>>> packetLengthsPerLinks;
	private int biggerPacketpreceding;

	private Packet(Packet p) {
		this.arrivalNode = new LinkedList<Node>(p.arrivalNode);
		this.arrivalTimeToNode = new LinkedList<Double>(p.arrivalTimeToNode);
		this.arrivalTimeToRegulator = p.arrivalTimeToRegulator;
		this.leavingNetworkTime = p.leavingNetworkTime;
		this.route = p.route;
		this.tree = p.tree;
		this.egressNode = p.egressNode;
		this.vl = p.vl;
		this.latency = new LinkedList<Double>(p.latency);
		this.lastLatency = p.lastLatency;
		this.lastJitter = p.lastJitter;
		this.previousLink = p.previousLink;
		this.regulated = p.regulated;
		this.technologyLatencyWaiting = p.technologyLatencyWaiting;
		this.protocol = p.protocol;
		this.routesInTheLink = new LinkedList<Set<Route>>(p.routesInTheLink);
		this.treesInTheLink = new LinkedList<Set<MulticastTree>>(p.treesInTheLink);
		this.packetLengthsPerLinks = new ArrayList<List<List<Integer>>>(p.packetLengthsPerLinks);
		this.biggerPacketpreceding = p.biggerPacketpreceding;
	}

	public Packet(Route route, VL vl, double time) {
		if (route == null)
			throw new RuntimeException("Bad");
		this.arrivalNode = new LinkedList<Node>();
		this.arrivalTimeToNode = new LinkedList<Double>();
		this.arrivalTimeToRegulator = time;
		this.leavingNetworkTime = -1;
		this.route = route;
		this.tree = null;
		this.egressNode = route.getEgressNode();
		this.vl = vl;
		this.latency = new LinkedList<Double>();
		this.previousLink = null;
		this.routesInTheLink = new ArrayList<Set<Route>>();
		this.treesInTheLink = new ArrayList<Set<MulticastTree>>();
		this.packetLengthsPerLinks = new ArrayList<List<List<Integer>>>();
		this.biggerPacketpreceding = 0;
	}

	public Packet(MulticastTree tree, Node egressNode, VL vl, double time) {
		if (tree == null)
			throw new RuntimeException("Bad");
		this.arrivalNode = new LinkedList<Node>();
		this.arrivalTimeToNode = new LinkedList<Double>();
		this.arrivalTimeToRegulator = time;
		this.leavingNetworkTime = -1;
		this.route = null;
		this.tree = tree;
		this.egressNode = egressNode;
		this.vl = vl;
		this.latency = new LinkedList<Double>();
		this.previousLink = null;
		this.routesInTheLink = new ArrayList<Set<Route>>();
		this.treesInTheLink = new ArrayList<Set<MulticastTree>>();
		this.packetLengthsPerLinks = new ArrayList<List<List<Integer>>>();
		this.biggerPacketpreceding = 0;
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

	public Route getRoute() {
		return route;
	}

	public MulticastTree getMulticastTree() {
		return tree;
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

	public List<Set<Route>> getRoutesInTheLink() {
		return routesInTheLink;
	}

	public void setRoutesInTheLink(List<Set<Route>> routesInTheLink) {
		this.routesInTheLink = routesInTheLink;
	}

	public List<Set<MulticastTree>> getTreesInTheLink() {
		return treesInTheLink;
	}

	public void setTreesInTheLink(List<Set<MulticastTree>> treesInTheLink) {
		this.treesInTheLink = treesInTheLink;
	}

	public List<List<List<Integer>>> getpacketLengthsPerLinks() {
		return packetLengthsPerLinks;
	}

	public void setpacketLengthsPerLinks(List<List<List<Integer>>> packetLengthsPerLinks) {
		this.packetLengthsPerLinks = packetLengthsPerLinks;
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

	public int getBiggerPacketpreceding() {
		return biggerPacketpreceding;
	}

	public void setBiggerPacketpreceding(int biggerPacketpreceding) {
		this.biggerPacketpreceding = biggerPacketpreceding;
	}

	public double getMinimumLatencyInMs() {
		double result = 0;

		int l_max = 0;
		if (route != null)
			l_max = Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
		else
			l_max = Integer.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));

		List<Link> links;

		if (route != null)
			links = route.getSeqLinksRealPath();
		else
			links = tree.getSeqLinksToEgressNode(egressNode);

		for (Link link : links) {
			double nodeServiceTimeInMs = AFDXParameters.TLSwInMs;
			if (links.get(0) == link)
				nodeServiceTimeInMs = AFDXParameters.TLTxInMs;

			result += nodeServiceTimeInMs;

			result += 1000 * 8 * (AFDXParameters.ETHHeaderBytes + AFDXParameters.IPHeaderBytes
					+ AFDXParameters.UDPHeaderBytes + l_max) / link.getCapacity();
		}

		result += AFDXParameters.TLRxInMs;

		return result;
	}

	public void print() {
		if (route != null) {
			System.out.println("Print route");
			System.out.println("Route index " + route.getIndex());
			System.out.println("\nRoute parameters:");
			System.out
					.println("\t L MAX bytes: \t" + route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
			System.out.println("\t BAG ms: \t" + route.getDemand().getAttribute(AFDXParameters.ATT_VL_BAG_MS));
		} else {
			System.out.println("Print tree");
			System.out.println("Tree index " + tree.getIndex());
			System.out.println("\nTree parameters:");
			System.out.println(
					"\t L MAX bytes: \t" + tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
			System.out.println("\t BAG ms: \t" + tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_BAG_MS));
		}

		System.out.println("Bigger Packet preceding " + biggerPacketpreceding);

		System.out.println("\nArrival time to nodes");
		for (int i = 0; i < arrivalNode.size(); i++) {
			System.out.println("\tNode: " + arrivalNode.get(i).getName() + "\tArrival time: "
					+ AFDXTools.df_5.format(arrivalTimeToNode.get(i)) + " ("
					+ AFDXTools.df_5.format(((i == 0 ? 0 : arrivalTimeToNode.get(i) - arrivalTimeToNode.get(i - 1))))
					+ ")");

		}
		System.out.println("\n\tLatency " + AFDXTools.df_5.format((leavingNetworkTime - arrivalTimeToNode.get(0))));

		System.out.println("\nRoutes and Trees in Link");
		for (int i = 0; i < routesInTheLink.size(); i++) {
			Set<Route> routes = routesInTheLink.get(i);
			Link link;
			if (route != null) {
				link = this.route.getSeqLinksRealPath().get(i);
				System.out.println("\n\tRoutes in link " + link.getIndex());
			} else {
				link = this.tree.getSeqLinksToEgressNode(arrivalNode.get(arrivalNode.size() - 1)).get(i);
				System.out.println("\n\tRoutes in link " + link.getIndex());
			}
			System.out.print("\t\t");

			for (Route route2 : routes) {
				Link previousLink = null;
				if (i > 0)
					previousLink = route2.getSeqLinksRealPath().get(route2.getSeqLinksRealPath().indexOf(link) - 1);

				System.out.print(route2.getIndex() + " L_max("
						+ route2.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES) + ")"
						+ (previousLink != null ? " Previous Link " + previousLink.getIndex() : "") + " - ");
			}

			Set<MulticastTree> trees = treesInTheLink.get(i);
			System.out.println("\n\tTrees in link " + link.getIndex());
			System.out.print("\t\t");
			for (MulticastTree tree2 : trees) {
				Link previousLink = null;
				if (i > 0)
					for (Node node : tree2.getEgressNodes()) {
						List<Link> links = tree2.getSeqLinksToEgressNode(node);
						if (links.contains(link)) {
							previousLink = links.get(links.indexOf(link) - 1);
							break;
						}
					}

				System.out.print(tree2.getIndex() + " L_max("
						+ tree2.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES) + ")"
						+ (previousLink != null ? " Previous Link " + previousLink.getIndex() : "") + " - ");
			}
		}

		System.out.println("\npackets length in the links");
		for (int i = 0; i < packetLengthsPerLinks.size(); i++) {
			List<List<Integer>> lengths = packetLengthsPerLinks.get(i);

			System.out.println("\nLink " + i);

			for (List<Integer> integerList : lengths) {
				for (Integer integer : integerList) {
					System.out.println("\n\tPacket lenght in link " + integer);
				}
			}
		}

		System.out.println();
	}

}
