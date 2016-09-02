package afdx.algorithm;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Route;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

import afdx.AFDXParameters;
import afdx.AFDXTools;
import afdx.Packet;
import afdx.VL;
import afdx.validator.AFDXConfigurationValidator;

public class FIFOTAAlgorithm implements IAlgorithm {
	public static final String prefix = "TA_";
	private InputParameter printRouteIndex = new InputParameter(AFDXParameters.SIM_PARAM_PRINT_ROUTE_INDEX, (double) -1,
			"");
	private InputParameter printTreeIndex = new InputParameter(AFDXParameters.SIM_PARAM_PRINT_TREE_INDEX, (double) -1,
			"");
	private InputParameter printlatency = new InputParameter(AFDXParameters.SIM_PARAM_MIN_LATENCY_TO_PRINT_ROUTE,
			(double) -1, "");
	private InputParameter grouping = new InputParameter(AFDXParameters.SIM_PARAM_GROUPING, "N", "");

	private Map<String, String> algorithmParameters;

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {
		String result = "";

		this.algorithmParameters = algorithmParameters;

		// Static Validation of AFDX Configuration table
		AFDXConfigurationValidator configurationValidator = new AFDXConfigurationValidator();
		if (!configurationValidator.AFDXValidation(netPlan))
			result += " : " + "Validation Error, check console.";

		// Each route
		List<Route> routes = netPlan.getRoutes();

		// each route
		for (Route route : routes) {
			// We use a packet to record the performances
			Packet packet = new Packet(new VL(route), route.getEgressNode(), 0);

			cal(packet);
		}

		// Each tree
		List<MulticastTree> trees = netPlan.getMulticastTrees();

		for (MulticastTree tree : trees) {
			Set<Node> egressNodes = tree.getEgressNodes();

			for (Node egressNode : egressNodes) {
				// We use a packet to record the performances
				Packet packet = new Packet(new VL(tree), egressNode, 0);

				cal(packet);
			}
		}

		return result;
	}

	private void cal(Packet packet) {
		// Sequence of links in the path
		List<Link> demandLinks;
		if (packet.getVl().getRoute() != null)
			demandLinks = packet.getVl().getRoute().getSeqLinksRealPath();
		else
			demandLinks = packet.getVl().getTree().getSeqLinksToEgressNode(packet.getEgressNode());

		// For each link in the path
		for (Link link : demandLinks) {
			// We find the other VLs sharing the link
			// if
			// (algorithmParameters.get(AFDXParameters.SIM_PARAM_GROUPING).equals("Y"))
			// AFDXTools.findVLsCrossingLinkGrouping(link, packet);
			// else
			AFDXTools.findVLsCrossingLink(link, packet);

			// Update the last crossed link in the packet
			packet.setPreviousLink(link);
		}

		// Once the Packet arrive to destination, we calculate latency
		double latencyInMs;
		if (algorithmParameters.get(AFDXParameters.SIM_PARAM_GROUPING).equals("N"))
			latencyInMs = calculateLatency(packet);
		else
			latencyInMs = calculateLatencyGrouping(packet);

		packet.setLastLatency(latencyInMs);

		// and Jitter
		double jitterInMs = AFDXTools.calculateJitterInMs(packet);
		packet.setLastJitter(jitterInMs);

		// Store the results in the route attributes
		AFDXTools.setAttibutes(packet, prefix);

		printResults(packet, algorithmParameters);
	}

	private double calculateLatency(Packet packet) {
		List<Link> links;

		if (packet.getVl().getRoute() != null) {
			links = packet.getVl().getRoute().getInitialSequenceOfLinks();
		} else {
			links = packet.getVl().getTree().getSeqLinksToEgressNode(packet.getEgressNode());
		}

		double latencyInMs = 0.0;
		packet.getArrivalTimesToNodes().add(latencyInMs);
		packet.getArrivalNodes().add(links.get(0).getOriginNode());

		List<List<List<VL>>> hops = packet.getVlPerLinks();
		int hopNumber = 0;

		for (List<List<VL>> vlPerLinksList : hops) {
			int biggerIPPacket = 0;

			// For each node, we calculate the accumulate latency
			double totalBitsQueue = 0;

			// We calculate the total byte to send through the output link
			// having into account the header and the IFG
			for (List<VL> vlPerLinks : vlPerLinksList)
				for (VL vl : vlPerLinks) {
					if (packet.isVLInLink(vl, hopNumber - 1))
						continue;

					if (vl.getLmaxIPPacket() > packet.getVl().getLmaxIPPacket()
							&& vl.getLmaxIPPacket() > biggerIPPacket)
						biggerIPPacket = vl.getLmaxIPPacket();

					totalBitsQueue += 8
							* (AFDXParameters.ETHHeaderBytes + vl.getLmaxIPPacket() + AFDXParameters.IFGBytes);
				}

			// We add the current VL packet length without IFG
			totalBitsQueue += 8 * (AFDXParameters.ETHHeaderBytes + packet.getVl().getLmaxIPPacket());

			// We add the technology latency
			double nodeServiceTimeInMs;
			if (hopNumber == 0)
				nodeServiceTimeInMs = AFDXParameters.TLTxInMs;
			else
				nodeServiceTimeInMs = AFDXParameters.TLSwInMs;

			if (packet.getBiggerIPPacketPreceding() > 0)
				totalBitsQueue += 8 * (AFDXParameters.ETHHeaderBytes + packet.getBiggerIPPacketPreceding()
						- packet.getVl().getLmaxIPPacket() + AFDXParameters.IFGBytes);

			latencyInMs += 1000 * totalBitsQueue / links.get(hopNumber).getCapacity();

			latencyInMs += nodeServiceTimeInMs;

			packet.getArrivalNodes().add(links.get(hopNumber).getDestinationNode());
			packet.getArrivalTimesToNodes().add(latencyInMs);

			if (biggerIPPacket > packet.getBiggerIPPacketPreceding())
				packet.setBiggerIPPacketPreceding(biggerIPPacket);

			try {
				String routesString = algorithmParameters.get(AFDXParameters.SIM_PARAM_PRINT_ROUTE_INDEX);
				String routes2[] = routesString.split(",");
				for (String string : routes2)
					if (packet.getVl().getRoute().getIndex() == Double.parseDouble(string))
						System.out.println("getBiggerIPPacketPreceding " + packet.getBiggerIPPacketPreceding());
			} catch (Exception e) {
			}

			hopNumber++;
		}

		packet.setLeavingNetworkTime(latencyInMs + AFDXParameters.TLRxInMs);

		return packet.getLeavingNetworkTime();
	}

	private double calculateLatencyGrouping(Packet packet) {
		List<Link> links;

		if (packet.getVl().getRoute() != null) {
			links = packet.getVl().getRoute().getInitialSequenceOfLinks();
		} else {
			links = packet.getVl().getTree().getSeqLinksToEgressNode(packet.getEgressNode());
		}

		double cummulatedLatencyInMs = 0.0;
		packet.getArrivalTimesToNodes().add(cummulatedLatencyInMs);
		packet.getArrivalNodes().add(links.get(0).getOriginNode());

		List<List<List<VL>>> hops = packet.getVlPerLinks();
		int hopNumber = 0;

		// Current VL packet length without IFG hopNumber times
		double VLPacketSize = 1000 * 8 * (AFDXParameters.ETHHeaderBytes + packet.getVl().getLmaxIPPacket());

		for (List<List<VL>> packetsPerInputLinks : hops) {
			int biggerIPPacket = 0;

			double lastLatency = cummulatedLatencyInMs;
			for (List<VL> packetsPerInputLink : packetsPerInputLinks) {

				// For each node, we calculate the accumulate latency
				double totalBitsQueue = 0;

				// We calculate the total byte to send through the output link
				// having into account the header and the IFG
				for (VL vl : packetsPerInputLink) {
					if (vl.getLmaxIPPacket() > packet.getVl().getLmaxIPPacket()
							&& vl.getLmaxIPPacket() > biggerIPPacket)
						biggerIPPacket = vl.getLmaxIPPacket();

					totalBitsQueue += 8
							* (AFDXParameters.ETHHeaderBytes + vl.getLmaxIPPacket() + AFDXParameters.IFGBytes);
				}

				double currentInputLinkLatency = 1000 * totalBitsQueue / (links.get(hopNumber).getCapacity());
				if (hopNumber == 0) {
					cummulatedLatencyInMs += currentInputLinkLatency;
				} else {
					double maxSingleCurrentInputLinkLatency = 0;
					if (packetsPerInputLink.size() > 0)
						maxSingleCurrentInputLinkLatency = 1000 * 8 * (AFDXParameters.ETHHeaderBytes
								+ packetsPerInputLink.get(0).getLmaxIPPacket() + AFDXParameters.IFGBytes)
								/ (links.get(hopNumber).getCapacity());

					if (lastLatency >= currentInputLinkLatency) {
						// System.out.println("maxSingle " +
						// packetsPerInputLink.get(0));
						// System.out.println("maxSingleCurrentInputLinkLatency
						// " + maxSingleCurrentInputLinkLatency);
						// System.out.println("Route " + r + " hops " +
						// hopNumber + " 1 \t" + cummulatedLatencyInMs + "\t"
						// + lastLatency + "\t" + currentInputLinkLatency + "\t"
						// + maxSingleCurrentInputLinkLatency);
						cummulatedLatencyInMs += currentInputLinkLatency;
					} else if (lastLatency >= currentInputLinkLatency - maxSingleCurrentInputLinkLatency) {
						// System.out.println("maxSingle " +
						// packetsPerInputLink.get(0));
						// System.out.println("maxSingleCurrentInputLinkLatency
						// " + maxSingleCurrentInputLinkLatency);
						// System.out.println("Route " + r + " hops " +
						// hopNumber + " 2 \t" + cummulatedLatencyInMs + "\t"
						// + lastLatency + "\t" + currentInputLinkLatency + "\t"
						// + maxSingleCurrentInputLinkLatency);
						cummulatedLatencyInMs += currentInputLinkLatency;
					} else {
						// System.out.println("maxSingle " +
						// packetsPerInputLink.get(0));
						// System.out.println("maxSingleCurrentInputLinkLatency
						// " + maxSingleCurrentInputLinkLatency);
						// System.out.println("Route " + r + " hops " +
						// hopNumber + " 3 \t" + cummulatedLatencyInMs + "\t"
						// + lastLatency + "\t" + currentInputLinkLatency + "\t"
						// + maxSingleCurrentInputLinkLatency);
						cummulatedLatencyInMs += maxSingleCurrentInputLinkLatency
								+ ((currentInputLinkLatency - maxSingleCurrentInputLinkLatency < cummulatedLatencyInMs)
										? currentInputLinkLatency - maxSingleCurrentInputLinkLatency
										: cummulatedLatencyInMs);
					}
				}
			}

			// We add the Tx technology latency
			double nodeServiceTimeInMs;
			if (hopNumber == 0)
				nodeServiceTimeInMs = AFDXParameters.TLTxInMs;
			else
				nodeServiceTimeInMs = AFDXParameters.TLTxInMs + AFDXParameters.TLSwInMs * (hopNumber);

			if (packet.getBiggerIPPacketPreceding() > 0)
				cummulatedLatencyInMs += 1000 * 8
						* (AFDXParameters.ETHHeaderBytes + packet.getBiggerIPPacketPreceding()
								- packet.getVl().getLmaxIPPacket() + AFDXParameters.IFGBytes)
						/ links.get(hopNumber).getCapacity();

			packet.getArrivalNodes().add(links.get(hopNumber).getDestinationNode());
			packet.getArrivalTimesToNodes().add(cummulatedLatencyInMs + nodeServiceTimeInMs
					+ (VLPacketSize * (hopNumber + 1) / (links.get(hopNumber).getCapacity())));

			if (biggerIPPacket > packet.getBiggerIPPacketPreceding())
				packet.setBiggerIPPacketPreceding(biggerIPPacket);

			hopNumber++;
		}

		packet.setLeavingNetworkTime(packet.getArrivalTimesToNodes().get(packet.getArrivalTimesToNodes().size() - 1)
				+ AFDXParameters.TLRxInMs);

		return packet.getLeavingNetworkTime();
	}

	private double calculateLatencyGrouping1(Packet packet) {
		List<Link> links;

		if (packet.getVl().getRoute() != null) {
			links = packet.getVl().getRoute().getInitialSequenceOfLinks();
		} else {
			links = packet.getVl().getTree().getSeqLinksToEgressNode(packet.getEgressNode());
		}

		double cummulatedLatencyInMs = 0.0;
		packet.getArrivalTimesToNodes().add(cummulatedLatencyInMs);
		packet.getArrivalNodes().add(links.get(0).getOriginNode());

		List<List<List<VL>>> hops = packet.getVlPerLinks();
		int hopNumber = 0;

		// Current VL packet length without IFG hopNumber times
		double VLPacketSize = 1000 * 8 * (AFDXParameters.ETHHeaderBytes + packet.getVl().getLmaxIPPacket());

		for (List<List<VL>> packetsPerInputLinks : hops) {
			int biggerPacket = 0;

			double lastLatency = cummulatedLatencyInMs;
			for (List<VL> packetsPerInputLink : packetsPerInputLinks) {

				// For each node, we calculate the accumulate latency
				double totalBitsQueue = 0;

				// We calculate the total byte to send through the output link
				// having into account the header and the IFG
				for (VL vl : packetsPerInputLink) {
					if (vl.getLmaxIPPacket() > packet.getVl().getLmaxIPPacket() && vl.getLmaxIPPacket() > biggerPacket)
						biggerPacket = vl.getLmaxIPPacket();

					totalBitsQueue += 8
							* (AFDXParameters.ETHHeaderBytes + vl.getLmaxIPPacket() + AFDXParameters.IFGBytes);
				}

				double currentInputLinkLatency = 1000 * totalBitsQueue / (links.get(hopNumber).getCapacity());
				if (hopNumber == 0) {
					cummulatedLatencyInMs += currentInputLinkLatency;
				} else {
					double maxSingleCurrentInputLinkLatency = 0;
					if (packetsPerInputLink.size() > 0)
						maxSingleCurrentInputLinkLatency = 1000 * 8 * (AFDXParameters.ETHHeaderBytes
								+ packetsPerInputLink.get(0).getLmaxIPPacket() + AFDXParameters.IFGBytes)
								/ (links.get(hopNumber).getCapacity());

					if (lastLatency >= currentInputLinkLatency) {
						// System.out.println("maxSingle " +
						// packetsPerInputLink.get(0));
						// System.out.println("maxSingleCurrentInputLinkLatency
						// " + maxSingleCurrentInputLinkLatency);
						// System.out.println("Route " + r + " hops " +
						// hopNumber + " 1 \t" + cummulatedLatencyInMs + "\t"
						// + lastLatency + "\t" + currentInputLinkLatency + "\t"
						// + maxSingleCurrentInputLinkLatency);
						cummulatedLatencyInMs += currentInputLinkLatency;
					} else if (lastLatency >= currentInputLinkLatency - maxSingleCurrentInputLinkLatency) {
						// System.out.println("maxSingle " +
						// packetsPerInputLink.get(0));
						// System.out.println("maxSingleCurrentInputLinkLatency
						// " + maxSingleCurrentInputLinkLatency);
						// System.out.println("Route " + r + " hops " +
						// hopNumber + " 2 \t" + cummulatedLatencyInMs + "\t"
						// + lastLatency + "\t" + currentInputLinkLatency + "\t"
						// + maxSingleCurrentInputLinkLatency);
						cummulatedLatencyInMs += currentInputLinkLatency;
					} else {
						// System.out.println("maxSingle " +
						// packetsPerInputLink.get(0));
						// System.out.println("maxSingleCurrentInputLinkLatency
						// " + maxSingleCurrentInputLinkLatency);
						// System.out.println("Route " + r + " hops " +
						// hopNumber + " 3 \t" + cummulatedLatencyInMs + "\t"
						// + lastLatency + "\t" + currentInputLinkLatency + "\t"
						// + maxSingleCurrentInputLinkLatency);
						cummulatedLatencyInMs += maxSingleCurrentInputLinkLatency
								+ ((currentInputLinkLatency - maxSingleCurrentInputLinkLatency < cummulatedLatencyInMs)
										? currentInputLinkLatency - maxSingleCurrentInputLinkLatency
										: cummulatedLatencyInMs);
					}
				}
			}

			if (hopNumber > 0 && packet.getBiggerIPPacketPreceding() > 0)
				cummulatedLatencyInMs += 1000 * 8
						* (AFDXParameters.ETHHeaderBytes + packet.getBiggerIPPacketPreceding()
								- packet.getVl().getLmaxIPPacket() + AFDXParameters.IFGBytes)
						/ links.get(hopNumber).getCapacity();

			// We add the Tx technology latency
			double nodeServiceTimeInMs;
			if (hopNumber == 0)
				nodeServiceTimeInMs = AFDXParameters.TLTxInMs;
			else
				nodeServiceTimeInMs = AFDXParameters.TLTxInMs + AFDXParameters.TLSwInMs * (hopNumber);

			packet.getArrivalNodes().add(links.get(hopNumber).getDestinationNode());
			packet.getArrivalTimesToNodes().add(cummulatedLatencyInMs + nodeServiceTimeInMs
					+ (VLPacketSize * (hopNumber + 1) / (links.get(hopNumber).getCapacity())));

			if (biggerPacket > packet.getBiggerIPPacketPreceding())
				packet.setBiggerIPPacketPreceding(biggerPacket);

			hopNumber++;
		}

		packet.setLeavingNetworkTime(packet.getArrivalTimesToNodes().get(packet.getArrivalTimesToNodes().size() - 1)
				+ AFDXParameters.TLRxInMs);

		return packet.getLeavingNetworkTime();
	}

	public void addRoutesAndTressInNode(Link routeLink, Route route, Packet packet) {
		Set<Route> routes = new HashSet<Route>();
		Set<MulticastTree> trees = new HashSet<MulticastTree>();

		if (routeLink.getOriginNode() == route.getIngressNode()) {
			// First Link
			// add all routes but this
			routes = new HashSet<Route>(routeLink.getTraversingRoutes());
			routes.remove(route);

			// add all trees
			trees = new HashSet<MulticastTree>(routeLink.getTraversingTrees());
		} else {
			// no First Link
			Set<Link> links = routeLink.getOriginNode().getIncomingLinks();

			for (Link link : links) {
				if (link == packet.getPreviousLink())
					continue;

				// grouping
				Route routeWithLmax = findRouteWithLmaxInLink(link, routeLink);
				MulticastTree treeWithLmax = findTreeWithLmax(link, routeLink);

				if (routeWithLmax == null) {
					if (treeWithLmax != null)
						trees.add(treeWithLmax);
				} else {
					if (treeWithLmax == null)
						routes.add(routeWithLmax);
					else {
						if (Integer.parseInt(
								routeWithLmax.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)) > Integer
										.parseInt(treeWithLmax.getMulticastDemand()
												.getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)))
							routes.add(routeWithLmax);
						else
							trees.add(treeWithLmax);
					}
				}
				// if (routes.size() > 0 || trees.size() > 0) {
				// Set<Route> routesInPreviousLink = packet.getRoutesInThePath()
				// .get(packet.getRoutesInThePath().size() - 1);
				// routes.addAll(routesInPreviousLink);
				//
				// Set<MulticastTree> TreesInPreviousLink =
				// packet.getTreesInThePath()
				// .get(packet.getTreesInThePath().size() - 1);
				// trees.addAll(TreesInPreviousLink);
				// }
			}
		}
	}

	public void addRoutesAndTressInNode(Link treeLink, MulticastTree tree, Packet packet) {
		Set<Route> routes = new HashSet<Route>();
		Set<MulticastTree> trees = new HashSet<MulticastTree>();

		if (treeLink.getOriginNode() == tree.getIngressNode()) {
			// First Link
			// add all routes
			routes = new HashSet<Route>(treeLink.getTraversingRoutes());

			// add all trees but this
			trees = new HashSet<MulticastTree>(treeLink.getTraversingTrees());
			trees.remove(tree);
		} else {
			// no First Link
			Set<Link> links = treeLink.getOriginNode().getIncomingLinks();

			// All link arriving the same node that link
			for (Link link : links) {
				// avoiding link
				if (treeLink == packet.getPreviousLink())
					continue;

				// grouping
				Route routeWithLmax = findRouteWithLmaxInLink(link, treeLink);
				MulticastTree treeWithLmax = findTreeWithLmax(link, treeLink);

				if (routeWithLmax == null) {
					if (treeWithLmax != null)
						trees.add(treeWithLmax);
				} else {
					if (treeWithLmax == null)
						routes.add(routeWithLmax);
					else {
						if (Integer.parseInt(
								routeWithLmax.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)) > Integer
										.parseInt(treeWithLmax.getMulticastDemand()
												.getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)))
							routes.add(routeWithLmax);
						else
							trees.add(treeWithLmax);
					}
				}
				// if (routes.size() > 0 || trees.size() > 0) {
				// Set<Route> routesInPreviousLink = packet.getRoutesInThePath()
				// .get(packet.getRoutesInThePath().size() - 1);
				// routes.addAll(routesInPreviousLink);
				//
				// Set<MulticastTree> TreesInPreviousLink =
				// packet.getTreesInThePath()
				// .get(packet.getTreesInThePath().size() - 1);
				// trees.addAll(TreesInPreviousLink);
				// }
			}
		}
	}

	public Route findRouteWithLmaxInLink(Link inputLink, Link outputLink) {
		Route result = null;

		Set<Route> routes = inputLink.getTraversingRoutes();

		if (routes != null) {
			int l = 0;
			for (Route route : routes) {
				if (route.getInitialSequenceOfLinks().contains(outputLink))
					if (Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)) > l) {
						l = Integer.parseInt(route.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
						result = route;
					}
			}
		}

		return result;
	}

	public MulticastTree findTreeWithLmax(Link inputLink, Link outputLink) {
		MulticastTree result = null;

		Set<MulticastTree> trees = inputLink.getTraversingTrees();

		if (trees != null) {
			int l = 0;
			for (MulticastTree tree : trees) {
				if (tree.getLinkSet().contains(outputLink))
					if (Integer
							.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES)) > l) {
						l = Integer.parseInt(tree.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
						result = tree;
					}
			}
		}

		return result;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters() {
		return InputParameter.getInformationAllInputParameterFieldsOfObject(this);
	}

	private void printResults(Packet packet, Map<String, String> algorithmParameters) {
		// console printing results
		if (packet.getVl().getRoute() != null) {
			String routesString = algorithmParameters.get(AFDXParameters.SIM_PARAM_PRINT_ROUTE_INDEX);
			String routes2[] = routesString.split(",");
			for (String string : routes2) {
				if (string.equals(packet.getVl().getRoute().getIndex() + "")) {
					if (packet.getVl().getRoute().getIndex() == Double.parseDouble(string)) {
						if (packet.getLastLatency() > Double.parseDouble(
								algorithmParameters.get(AFDXParameters.SIM_PARAM_MIN_LATENCY_TO_PRINT_ROUTE))) {
							packet.print();
						}
					}
				}
			}
		} else {
			String treesString = algorithmParameters.get(AFDXParameters.SIM_PARAM_PRINT_TREE_INDEX);
			String trees2[] = treesString.split(",");
			for (String string : trees2) {
				if (string.equals(packet.getVl().getTree().getIndex() + ""))
					if (packet.getVl().getTree().getIndex() == Double.parseDouble(string))
						if (packet.getLastLatency() > Double.parseDouble(
								algorithmParameters.get(AFDXParameters.SIM_PARAM_MIN_LATENCY_TO_PRINT_ROUTE))) {
							packet.print();
						}
			}
		}
	}

}
