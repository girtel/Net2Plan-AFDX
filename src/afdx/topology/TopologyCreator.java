package afdx.topology;

import java.util.List;
import java.util.Map;

import afdx.AFDXParameters;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Triple;

;

public class TopologyCreator implements IAlgorithm {

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {

		// Airbus topology
		int nodes = 8;
		String nodePorts = "24";
		double nodeLinkCapacity = 10.0E6;
		double lengthInKm = 0.05;// 50 meters

		// attributes
		netPlan.setAttribute(AFDXParameters.ATT_SWITCH_PORTS, nodePorts);
		netPlan.setAttribute(AFDXParameters.ATT_SWITCH_PORT_CAPACITY, "" + nodeLinkCapacity);

		// Creation of switches
		int xPos = 0;
		for (int i = 1; i < nodes + 1; i++) {
			if (i % 2 == 1)
				xPos += 100;

			netPlan.addNode(xPos, i % 2 == 1 ? 100 : 0, AFDXParameters.SWITCH_NAME + i, null);
		}

		// creation of links between switches
		for (int i = 1; i < netPlan.getNumberOfNodes() + 1; i++) {
			Node nodeFrom = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + i);

			if (nodeFrom != null) {
				Node nodeTo = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + (i + 1));

				// Links between consecutive nodes
				if (nodeTo != null) {
					netPlan.addLink(nodeFrom, nodeTo, nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
					netPlan.addLink(nodeTo, nodeFrom, nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
				}

				// Links to the third consecutive node from even nodes
				if (i % 2 == 1) {
					nodeTo = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + (i + 3));

					if (nodeTo != null) {
						netPlan.addLink(nodeFrom, nodeTo, nodeLinkCapacity, lengthInKm,
								AFDXParameters.propagationSpeedInKmPerSecond, null);
						netPlan.addLink(nodeTo, nodeFrom, nodeLinkCapacity, lengthInKm,
								AFDXParameters.propagationSpeedInKmPerSecond, null);
					}

				}

				// Links to the second consecutive node
				nodeTo = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + (i + 2));

				if (nodeTo != null) {
					netPlan.addLink(nodeFrom, nodeTo, nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
					netPlan.addLink(nodeTo, nodeFrom, nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
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
