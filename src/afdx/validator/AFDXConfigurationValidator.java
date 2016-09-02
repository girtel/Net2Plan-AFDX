package afdx.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastDemand;
import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Route;

import afdx.afdx.AFDXParameters;
import afdx.afdx.AFDXTools;
import afdx.afdx.VL;

public class AFDXConfigurationValidator {
	public boolean AFDXValidation(NetPlan netPlan) {
		boolean result = true;

		// check links capacity
		result = checkLinkCapacity(netPlan);

		// Check max number of VLs
		result = checkMaxNumberVLPerES(netPlan);

		// Check max jitter at each E/S
		result = checkMaxESJitter(netPlan);

		return result;
	}

	private boolean checkLinkCapacity(NetPlan netPlan) {
		boolean result = true;

		for (Link link : netPlan.getLinks())
			if (link.getCapacity() < link.getUtilizationNotIncludingProtectionSegments()) {
				result = false;
				String text = "Link index " + link.getIndex() + " in "
						+ (link.getUtilizationNotIncludingProtectionSegments() - link.getCapacity()) + " bps";
				System.out.println(text);
			}

		return result;
	}

	private boolean checkMaxNumberVLPerES(NetPlan netPlan) {
		boolean result = true;

		Set<Node> nodes = new HashSet<Node>();

		List<Demand> demands = netPlan.getDemands();
		for (Demand demand : demands) {
			Node ingressNode = demand.getIngressNode();
			if (nodes.contains(ingressNode))
				continue;

			int VLs = 0;
			VLs += ingressNode.getOutgoingDemands().size();
			VLs += ingressNode.getOutgoingMulticastDemands().size();

			if (VLs > AFDXParameters.MaxNumberOfVLPerES)
				nodes.add(ingressNode);
		}

		List<MulticastDemand> multicastDemands = netPlan.getMulticastDemands();
		for (MulticastDemand demand : multicastDemands) {
			Node ingressNode = demand.getIngressNode();
			if (nodes.contains(ingressNode))
				continue;

			int VLs = 0;
			VLs += ingressNode.getOutgoingDemands().size();
			VLs += ingressNode.getOutgoingMulticastDemands().size();

			if (VLs > AFDXParameters.MaxNumberOfVLPerES)
				nodes.add(ingressNode);
		}

		if (nodes.size() > 0) {
			result = false;
			for (Node node : nodes) {
				System.out.println("Node " + node.getName() + " exceeds the maximum number of Virtual links");
			}
		}

		return result;
	}

	private boolean checkMaxESJitter(NetPlan netPlan) {
		boolean result = true;

		double jitterInMs;

		List<Demand> demands = netPlan.getDemands();
		for (Demand demand : demands) {
			jitterInMs = 0.04;

			Link link = demand.getRoutes().iterator().next().getSeqLinksRealPath().get(0);

			Set<Route> routes = link.getTraversingRoutes();
			for (Route route : routes) {
				VL vl = new VL(route);

				if (route.getDemand() == demand)
					continue;

				jitterInMs += 1000 * 8 * (vl.getLmaxIPPacket() + AFDXParameters.IFGBytes) / link.getCapacity();
			}

			Set<MulticastTree> trees = link.getTraversingTrees();
			for (MulticastTree tree : trees) {
				VL vl = new VL(tree);

				jitterInMs += 1000 * 8 * (vl.getLmaxIPPacket() + AFDXParameters.IFGBytes) / link.getCapacity();
			}

			if (jitterInMs > AFDXParameters.MaxJitterInMsPerES) {
				result = false;

				System.out.println("VL Unicast from " + demand.getIngressNode().getName() + " to "
						+ demand.getEgressNode().getName() + " exceeds the maximum jitter by a jitter of "
						+ AFDXTools.df_5.format(jitterInMs) + " ms");
			}
		}

		List<MulticastDemand> multicastDemands = netPlan.getMulticastDemands();
		for (MulticastDemand demand : multicastDemands) {
			jitterInMs = 0.04;

			Link link = demand.getMulticastTrees().iterator().next()
					.getSeqLinksToEgressNode(demand.getEgressNodes().iterator().next()).get(0);

			Set<Route> routes = link.getTraversingRoutes();
			for (Route route : routes) {
				VL vl = new VL(route);

				jitterInMs += 1000 * 8 * (vl.getLmaxIPPacket() + AFDXParameters.IFGBytes) / link.getCapacity();
			}

			Set<MulticastTree> trees = link.getTraversingTrees();
			for (MulticastTree tree : trees) {
				VL vl = new VL(tree);

				if (tree.getMulticastDemand() == demand)
					continue;

				jitterInMs += 1000 * 8 * (vl.getLmaxIPPacket() + AFDXParameters.IFGBytes) / link.getCapacity();
			}

			if (jitterInMs > AFDXParameters.MaxJitterInMsPerES) {
				result = false;

				String text = "VL Multicast from " + demand.getIngressNode().getName() + " to ";
				Set<Node> nodes = demand.getEgressNodes();
				for (Node node : nodes) {
					text += node.getName() + ",";
				}
				text = text.substring(0, text.length() - 1);
				text += " exceeds the maximum jitter by a jitter of " + AFDXTools.df_5.format(jitterInMs) + " ms";
				System.out.println(text);
			}
		}

		return result;
	}
}
