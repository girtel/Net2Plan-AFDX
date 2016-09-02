package afdx.endSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Triple;

import afdx.afdx.AFDXParameters;

public class RandomES implements IAlgorithm {

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {

		double nodeLinkCapacity = 10.0E6;
		double lengthInKm = 0.05;// 50 meters

		int ESnumber = 100;
		// check network port capacity
		if (netPlan.getNumberOfNodes()
				* Integer.parseInt(netPlan.getAttribute(AFDXParameters.ATT_SWITCH_PORTS)) < ESnumber) {
			System.out.println("Not enough ports in the network");
			return null;
		}

		double Xmin = 10000, Xmax = -10000;
		double Ymin = 10000, Ymax = -10000;

		ArrayList<Long> nodes = netPlan.getNodeIds();
		for (long nodeId : nodes) {
			Node node = netPlan.getNode((int) nodeId);

			if (Xmin > node.getXYPositionMap().getX())
				Xmin = node.getXYPositionMap().getX();

			if (Xmax < node.getXYPositionMap().getX())
				Xmax = node.getXYPositionMap().getX();

			if (Ymin > node.getXYPositionMap().getY())
				Ymin = node.getXYPositionMap().getY();

			if (Ymax < node.getXYPositionMap().getY())
				Ymax = node.getXYPositionMap().getY();
		}

		for (int i = 0; i < ESnumber; i++) {

			Node node = null;
			do {
				int nodeToAdd = new Random().nextInt(nodes.size());
				node = netPlan.getNode(nodes.get(nodeToAdd).intValue());
			} while (node.getOutgoingLinksAllLayers().size() >= Integer
					.parseInt(netPlan.getAttribute(AFDXParameters.ATT_SWITCH_PORTS)));

			double x, y;
			x = node.getXYPositionMap().getX();
			if (x <= Xmin)
				x -= 100;
			if (x >= Xmax)
				x += 100;

			y = node.getXYPositionMap().getY();
			if (y <= Ymin)
				y -= 100;
			if (y >= Ymax)
				y += 100;

			Node ES = netPlan.addNode(x, y, AFDXParameters.ATT_ES_NAME + i, null);

			netPlan.addLink(ES, node, nodeLinkCapacity, lengthInKm, AFDXParameters.propagationSpeedInKmPerSecond, null);
			netPlan.addLink(node, ES, nodeLinkCapacity, lengthInKm, AFDXParameters.propagationSpeedInKmPerSecond, null);
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
