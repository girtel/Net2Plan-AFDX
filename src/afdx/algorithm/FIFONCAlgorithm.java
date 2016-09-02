package afdx.algorithm;

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

import afdx.afdx.AFDXParameters;
import afdx.afdx.AFDXTools;
import afdx.afdx.Packet;
import afdx.afdx.VL;
import afdx.validator.AFDXConfigurationValidator;

public class FIFONCAlgorithm implements IAlgorithm {
	public static final String prefix = "NC_";
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

		for (Route route : routes) {
			// We use a packet to record the performances
			Packet packet = new Packet(new VL(route), route.getEgressNode(), 0);

			cal(packet);
		}

		// each tree
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
			// We find the others VLs sharing the link
			// if
			// (algorithmParameters.get(AFDXParameters.SIM_PARAM_GROUPING).equals("Y"))
			// AFDXTools.findVLsCrossingLinkGrouping(link, packet);
			// else
			AFDXTools.findVLsCrossingLink(link, packet);

			// Update the last crossed link in the packet
			packet.setPreviousLink(link);
		}

		// Once the Packet arrive to destination, we calculate latency
		double latencyInMs = calculateLatency(packet, algorithmParameters);
		packet.setLastLatency(latencyInMs);

		// and Jitter
		double jitterInMs = AFDXTools.calculateJitterInMs(packet);
		packet.setLastJitter(jitterInMs);

		// Store the results in the route attributes
		AFDXTools.setAttibutes(packet, prefix);

		printResults(packet, algorithmParameters);
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters() {
		return InputParameter.getInformationAllInputParameterFieldsOfObject(this);
	}

	private double calculateLatency(Packet packet, Map<String, String> algorithmParameters) {
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

		for (List<List<VL>> vlPerLinks : hops) {
			int biggerIPPacket = 0;

			// For each node, we calculate the accumulate latency
			double totalBitsQueue = 0;

			// We calculate the total byte to send through the output link
			// having into account the header and the IFG
			for (List<VL> vlList : vlPerLinks) {
				for (VL vl : vlList) {
					if (packet.isVLInLink(vl, hopNumber - 1))
						continue;

					if (vl.getLmaxIPPacket() > packet.getVl().getLmaxIPPacket()
							&& vl.getLmaxIPPacket() > biggerIPPacket)
						biggerIPPacket = vl.getLmaxIPPacket();

					totalBitsQueue += 8
							* (AFDXParameters.ETHHeaderBytes + vl.getLmaxIPPacket() + AFDXParameters.IFGBytes);
				}
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

			double m = 8 * (AFDXParameters.ETHHeaderBytes + packet.getVl().getLmaxIPPacket())
					/ packet.getVl().getBagMs();

			// We apply the NC calculation
			NCFunction ncf = new NCFunction(m, totalBitsQueue, links.get(hopNumber).getCapacity() / 1000,
					nodeServiceTimeInMs);
			latencyInMs += ncf.getMaxDelayInMs();

			// Record the results of this node
			packet.getArrivalNodes().add(links.get(hopNumber).getDestinationNode());
			packet.getArrivalTimesToNodes().add(latencyInMs);

			if (biggerIPPacket > packet.getBiggerIPPacketPreceding())
				packet.setBiggerIPPacketPreceding(biggerIPPacket);

			try {
				String routesString = algorithmParameters.get(AFDXParameters.SIM_PARAM_PRINT_ROUTE_INDEX);
				String routes2[] = routesString.split(",");
				for (String string : routes2)
					if (packet.getVl().getRoute().getIndex() == Double.parseDouble(string))
						System.out.println("getBiggerIPPacketpreceding " + packet.getBiggerIPPacketPreceding());
			} catch (Exception e) {
			}

			hopNumber++;
		}

		// We add the Rx technology latency to the total packet latency
		packet.setLeavingNetworkTime(latencyInMs + AFDXParameters.TLRxInMs);

		return packet.getLeavingNetworkTime();
	}

	private Route findRouteWithLmaxInLink(Link inputLink, Link outputLink) {
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

	private MulticastTree findTreeWithLmax(Link inputLink, Link outputLink) {
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
							System.out.println("Print route");
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
							System.out.println("Print tree");
							packet.print();
						}
			}
		}
	}
}
