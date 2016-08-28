package test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastDemand;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Triple;

public class test1 implements IAlgorithm {

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {

		netPlan.addNode(0, 0, "nodo_1", null);
		netPlan.addNode(0, 100, "nodo_2", null);
		netPlan.addNode(0, 200, "nodo_3", null);
		netPlan.addNode(100, 100, "nodo_4", null);

		Node nodoS = netPlan.getNodeByName("nodo_1");
		Node nodoD = netPlan.getNodeByName("nodo_2");
		Link l12 = netPlan.addLink(nodoS, nodoD, 1, 1, 1, null);

		nodoS = netPlan.getNodeByName("nodo_1");
		nodoD = netPlan.getNodeByName("nodo_3");
		Link l13 = netPlan.addLink(nodoS, nodoD, 1, 1, 1, null);

		nodoS = netPlan.getNodeByName("nodo_1");
		nodoD = netPlan.getNodeByName("nodo_4");
		Link l14 = netPlan.addLink(nodoS, nodoD, 1, 1, 1, null);

		nodoS = netPlan.getNodeByName("nodo_2");
		nodoD = netPlan.getNodeByName("nodo_3");
		Link l23 = netPlan.addLink(nodoS, nodoD, 1, 1, 1, null);

		nodoS = netPlan.getNodeByName("nodo_4");
		nodoD = netPlan.getNodeByName("nodo_3");
		Link l43 = netPlan.addLink(nodoS, nodoD, 1, 1, 1, null);

		nodoS = netPlan.getNodeByName("nodo_1");
		Set<Node> nodes = new HashSet<Node>();
		nodoD = netPlan.getNodeByName("nodo_3");
		nodes.add(nodoD);
		nodoD = netPlan.getNodeByName("nodo_4");
		nodes.add(nodoD);
		MulticastDemand demand = netPlan.addMulticastDemand(nodoS, nodes, 1, null);

		Set<Link> links = new HashSet<Link>();
		links.add(l12);
		links.add(l23);
		links.add(l14);
		netPlan.addMulticastTree(demand, 1, 1, links, null);

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
