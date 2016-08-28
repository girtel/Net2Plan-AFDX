package afdx.topology;

import java.util.List;
import java.util.Map;

import afdx.AFDXParameters;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Triple;

;

public class A400MTopology implements IAlgorithm {

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {

		// Airbus topology
		int nodes = 8;
		String nodePorts = "24";
		double nodeLinkCapacity = 10.0E6;
		double lengthInKm = 0.05;// 50 meters

		// attributes

		// Creation of switches
		int xPos = 0;
		for (int i = 1; i < nodes + 1; i++) {
			if (i % 2 == 1)
				xPos += 100;

			Node node = netPlan.addNode(xPos, i % 2 == 1 ? 100 : 0, AFDXParameters.SWITCH_NAME + i, null);
			node.setAttribute(AFDXParameters.ATT_SWITCH_PORTS, nodePorts);
			node.setAttribute(AFDXParameters.ATT_SWITCH_PORT_CAPACITY, "" + nodeLinkCapacity);
		}

		// creation of links between switches
		for (int i = 1; i < netPlan.getNumberOfNodes() + 1; i++) {
			Node nodeFrom = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + i);

			if (nodeFrom != null) {
				for (int j = i + 1; j < i + 3; j++) {
					Node nodeTo = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + (j));

					if (nodeTo != null) {
						netPlan.addLink(nodeFrom, nodeTo, nodeLinkCapacity, lengthInKm,
								AFDXParameters.propagationSpeedInKmPerSecond, null);
						netPlan.addLink(nodeTo, nodeFrom, nodeLinkCapacity, lengthInKm,
								AFDXParameters.propagationSpeedInKmPerSecond, null);
					}
				}

				Node nodeTo = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + (i + 4));

				if (nodeTo != null) {
					netPlan.addLink(nodeFrom, nodeTo, nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
					netPlan.addLink(nodeTo, nodeFrom, nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
				}

				nodeTo = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + (i + 6));

				if (nodeTo != null) {
					netPlan.addLink(nodeFrom, nodeTo, nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
					netPlan.addLink(nodeTo, nodeFrom, nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
				}

				if (i % 2 == 1) {
					nodeTo = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + (i + 3));

					if (nodeTo != null) {
						netPlan.addLink(nodeFrom, nodeTo, nodeLinkCapacity, lengthInKm,
								AFDXParameters.propagationSpeedInKmPerSecond, null);
						netPlan.addLink(nodeTo, nodeFrom, nodeLinkCapacity, lengthInKm,
								AFDXParameters.propagationSpeedInKmPerSecond, null);
					}
				}
			}

		}
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
