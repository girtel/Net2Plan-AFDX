package afdx;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Route;

public class AFDXTools {
	public static DecimalFormat df_2 = new DecimalFormat("###.##");
	public static DecimalFormat df_3 = new DecimalFormat("###.###");
	public static DecimalFormat df_5 = new DecimalFormat("###.#####");

	public static List<Set<Route>> getRoutesCrossingLink(Link outgoingLink, Packet packet) {
		List<Set<Route>> result;

		result = new ArrayList<Set<Route>>();

		if (packet.getpacketLengthsPerLinks().size() == 0) {
			Set<Route> routes = new HashSet<Route>();
			routes.addAll(outgoingLink.getTraversingRoutes());
			if (packet.getRoute() != null)
				routes.remove(packet.getRoute());
			result.add(routes);
		} else {
			Set<Link> links = outgoingLink.getOriginNode().getIncomingLinks();

			for (Link link : links) {
				if (packet.getRoute() != null && packet.getRoute().getSeqLinksRealPath().contains(link))
					continue;

				SortedSet<Route> routesFromLink = new TreeSet<>(new RouteLMAXBiggerToSmaller());
				Set<Route> routes = link.getTraversingRoutes();
				for (Route route : routes)
					if (route.getSeqLinksRealPath().contains(outgoingLink))
						routesFromLink.add(route);

				result.add(routesFromLink);
			}
		}

		return result;
	}

	public static List<Set<MulticastTree>> getTreesCrossingLink(Link outgoingLink, Packet packet) {
		List<Set<MulticastTree>> result;

		result = new ArrayList<Set<MulticastTree>>();

		if (packet.getpacketLengthsPerLinks().size() == 0) {
			Set<MulticastTree> trees = new HashSet<MulticastTree>();
			trees.addAll(outgoingLink.getTraversingTrees());
			if (packet.getMulticastTree() != null)
				trees.remove(packet.getMulticastTree());
			result.add(trees);
		} else {
			Set<Link> links = outgoingLink.getOriginNode().getIncomingLinks();

			for (Link link : links) {
				if (packet.getMulticastTree() != null && packet.getMulticastTree().getLinkSet().contains(link))
					continue;

				SortedSet<MulticastTree> treessFromLink = new TreeSet<>(new TreeLMAXBiggerToSmaller());
				Set<MulticastTree> trees = link.getTraversingTrees();
				for (MulticastTree tree : trees)
					if (tree.getLinkSet().contains(outgoingLink))
						treessFromLink.add(tree);

				result.add(treessFromLink);
			}
		}

		return result;
	}

	public static void findVLsCrossingLink(Link link, Packet packet) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();

		if (packet.getPreviousLink() == null) {
			List<Integer> packetLengthsPerLinks = new ArrayList<Integer>();

			Set<Route> routesCrossingLink = link.getTraversingRoutes();
			for (Route route : routesCrossingLink) {
				if (packet.getRoute() != null && packet.getRoute() == route)
					continue;
				packetLengthsPerLinks
						.add(Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)));
			}
			Set<MulticastTree> treesCrossingLink = link.getTraversingTrees();
			for (MulticastTree tree : treesCrossingLink) {
				if (packet.getMulticastTree() != null && packet.getMulticastTree() == tree)
					continue;
				packetLengthsPerLinks.add(
						Integer.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)));
			}

			Collections.sort(packetLengthsPerLinks);
			Collections.reverse(packetLengthsPerLinks);

			// if (packetLengthsPerLinks.size() > 0)
			result.add(packetLengthsPerLinks);
		} else {
			Set<Link> inputLinks = link.getOriginNode().getIncomingLinks();

			for (Link inputLink : inputLinks) {
				List<Integer> packetLengthsPerLinks = new ArrayList<Integer>();

				if (packet.getRoute() != null && packet.getRoute().getSeqLinksRealPath().contains(inputLink))
					continue;
				else if (packet.getMulticastTree() != null
						&& packet.getMulticastTree().getLinkSet().contains(inputLink))
					continue;

				Set<Route> routesCrossingLink = inputLink.getTraversingRoutes();
				for (Route route : routesCrossingLink) {
					if (route.getSeqLinksRealPath().contains(link)) {
						packetLengthsPerLinks.add(
								Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)));
					}
				}

				Set<MulticastTree> treesCrossingLink = inputLink.getTraversingTrees();
				for (MulticastTree tree : treesCrossingLink) {
					if (tree.getLinkSet().contains(link)) {
						packetLengthsPerLinks.add(Integer
								.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)));
					}
				}

				Collections.sort(packetLengthsPerLinks);
				Collections.reverse(packetLengthsPerLinks);

				// if (packetLengthsPerLinks.size() > 0)
				result.add(packetLengthsPerLinks);
			}
		}

		packet.getpacketLengthsPerLinks().add(result);

		return;
		// }

		// Node originNode = link.getOriginNode();
		//
		// Set<Link> links = originNode.getIncomingLinks();
		//
		// for (Link link2 : links) {
		// if (link2 == packet.getPreviousLink())
		// continue;
		//
		// packetLengthsPerLinks = new ArrayList<Integer>();
		//
		// Set<Route> routes = link2.getTraversingRoutes();
		// for (Route route : routes) {
		// if (route.getSeqLinksRealPath().contains(link))
		// packetLengthsPerLinks
		// .add(Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)));
		// }
		//
		// Set<MulticastTree> trees = link2.getTraversingTrees();
		// for (MulticastTree tree : trees) {
		// if (tree.getLinkSet().contains(link))
		// packetLengthsPerLinks.add(Integer
		// .parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)));
		// }
		//
		// Collections.sort(packetLengthsPerLinks);
		// Collections.reverse(packetLengthsPerLinks);
		//
		// result.add(packetLengthsPerLinks);
		// }
		//
		// packet.getpacketLengthsPerLinks().add(result);
	}

	// public static void findVLsCrossingLinkGrouping(Link link, Packet packet)
	// {
	// List<Integer> packetLengthsPerLinks = new ArrayList<Integer>();
	//
	// List<List<Integer>> packetLengthsPerLinks1 = new
	// ArrayList<List<Integer>>();
	//
	// List<Set<Route>> routesCrossingLink = getRoutesCrossingLink(link,
	// packet);
	// List<Set<MulticastTree>> treesCrossingLink = getTreesCrossingLink(link,
	// packet);
	//
	// if (packet.getPreviousLink() == null) {
	// for (Set<Route> routeSet : routesCrossingLink) {
	// for (Route route : routeSet) {
	// packetLengthsPerLinks
	// .add((int) (AFDXParameters.UDPHeaderBytes
	// + Integer
	// .parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES))
	// + AFDXParameters.IFGBytes));
	// }
	// }
	//
	// for (Set<MulticastTree> treeSet : treesCrossingLink) {
	// for (MulticastTree tree : treeSet) {
	// packetLengthsPerLinks.add((int) (AFDXParameters.UDPHeaderBytes
	// + Integer
	// .parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES))
	// + AFDXParameters.IFGBytes));
	// }
	// }
	//
	// Collections.sort(packetLengthsPerLinks);
	// Collections.reverse(packetLengthsPerLinks);
	//
	// packet.getpacketLengthsPerLinks().add(packetLengthsPerLinks);
	//
	// return;
	// }
	//
	// for (int i = 0; i < routesCrossingLink.size(); i++) {
	// List<Integer> lengths = new ArrayList<Integer>();
	// for (Route route : routesCrossingLink.get(i)) {
	// if (route.getSeqLinksRealPath().contains(packet.getPreviousLink()))
	// break;
	// lengths.add((int) (AFDXParameters.UDPHeaderBytes
	// +
	// Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES))
	// + AFDXParameters.IFGBytes));
	// }
	// for (MulticastTree tree : treesCrossingLink.get(i)) {
	// if (tree.getLinkSet().contains(packet.getPreviousLink()))
	// break;
	// lengths.add(
	// (int) (AFDXParameters.UDPHeaderBytes
	// + Integer.parseInt(
	// tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES))
	// + AFDXParameters.IFGBytes));
	// }
	// Collections.sort(lengths);
	// Collections.reverse(lengths);
	//
	// packetLengthsPerLinks1.add(lengths);
	// }
	//
	// for (List<Integer> lmaxs : packetLengthsPerLinks1) {
	// if (lmaxs.size() > 0) {
	// packetLengthsPerLinks.add(lmaxs.get(0));
	// lmaxs.remove(0);
	// }
	// }
	//
	// List<Integer> temp = new ArrayList<Integer>();
	// for (List<Integer> lmaxs : packetLengthsPerLinks1) {
	// int count = 0;
	// if (lmaxs.size() > 0) {
	// for (Integer integer : lmaxs) {
	// count += integer;
	// }
	// }
	// temp.add(count);
	// }
	// Collections.sort(temp);
	// Collections.reverse(temp);
	//
	// List<Integer> beforeList =
	// packet.getpacketLengthsPerLinks().get(packet.getpacketLengthsPerLinks().size()
	// - 1);
	// int before = 0;
	// for (Integer integer : beforeList) {
	// before += integer;
	// }
	//
	// boolean firstBigger = true;
	// for (Integer integer : temp) {
	// if (integer > before)
	// if (firstBigger) {
	// firstBigger = false;
	// packetLengthsPerLinks.add(before);
	// continue;
	// }
	// packetLengthsPerLinks.add(integer);
	// }
	//
	// packet.getpacketLengthsPerLinks().add(packetLengthsPerLinks);
	// }

	public static double calculateJitterInMs(Packet packet) {
		// Jitter
		double jitterInMs = 0;

		Link link;
		if (packet.getRoute() != null)
			link = packet.getRoute().getSeqLinksRealPath().get(0);
		else
			link = packet.getMulticastTree().getSeqLinksToEgressNode(packet.getEgressNode()).get(0);

		Set<Route> routes = link.getTraversingRoutes();
		for (Route route : routes) {
			if (packet.getRoute() != null && packet.getRoute() == route)
				continue;

			jitterInMs += 1000 * 8
					* (AFDXParameters.IPHeaderBytes + AFDXParameters.UDPHeaderBytes
							+ Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES))
							+ AFDXParameters.IFGBytes)
					/ link.getCapacity();
		}

		Set<MulticastTree> trees = link.getTraversingTrees();
		for (MulticastTree tree : trees) {
			if (packet.getMulticastTree() != null && packet.getMulticastTree() == tree)
				continue;

			jitterInMs += 1000 * 8
					* (AFDXParameters.IPHeaderBytes + AFDXParameters.UDPHeaderBytes
							+ Integer
									.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES))
							+ AFDXParameters.IFGBytes)
					/ link.getCapacity();
		}

		return 0.04 + jitterInMs;
	}

	public static void setAttibutes(Packet packet, String prefix) {
		String attribute;
		if (packet.getRoute() != null) {
			// Latency
			attribute = AFDXParameters.ATT_VL_DST_DELAY.replace("XX",
					packet.getRoute().getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", "" + packet.getRoute().getEgressNode().getName());

			packet.getRoute().setAttribute(prefix + attribute, packet.getLastLatency() + "");

			// Minimum delay
			attribute = AFDXParameters.ATT_VL_DST_DELAY_MIN.replace("XX",
					packet.getRoute().getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", "" + packet.getEgressNode().getName());
			packet.getRoute().setAttribute(attribute, packet.getMinimumLatencyInMs() + "");

			// Jitter
			attribute = AFDXParameters.ATT_VL_JITTER.replace("XX",
					packet.getRoute().getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			packet.getRoute().setAttribute(attribute, packet.getLastJitter() + "");
		} else {
			// Latency
			attribute = AFDXParameters.ATT_VL_DST_DELAY.replace("XX",
					packet.getMulticastTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", "" + packet.getEgressNode().getName());
			packet.getMulticastTree().setAttribute(prefix + attribute, packet.getLastLatency() + "");

			// Minimum delay
			attribute = AFDXParameters.ATT_VL_DST_DELAY_MIN.replace("XX",
					packet.getMulticastTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", "" + packet.getEgressNode().getName());
			packet.getMulticastTree().setAttribute(attribute, packet.getMinimumLatencyInMs() + "");

			// Jitter
			attribute = AFDXParameters.ATT_VL_JITTER.replace("XX",
					packet.getMulticastTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			packet.getMulticastTree().setAttribute(attribute, packet.getLastJitter() + "");
		}

	}
}
