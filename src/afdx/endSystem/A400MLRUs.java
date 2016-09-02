package afdx.endSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Triple;

import afdx.afdx.AFDXParameters;

public class A400MLRUs implements IAlgorithm {

	@Override
	public String executeAlgorithm(NetPlan netPlan,
			Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {

		double nodeLinkCapacity = 10.0E6;// 10 Mbps
		double lengthInKm = 0.05;// 50 meters

		String path = getClass().getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		path = path.substring(0, path.lastIndexOf("/") + 1);

		try {
			String content = new String(Files.readAllBytes(Paths.get(path
					+ "A400MLRU.csv")));

			String lines[] = content.split("[\\r\\n]+");

			double Xmin = 10000, Xmax = -10000;
			double Ymin = 10000, Ymax = -10000;

			List<Node> switches = netPlan.getNodes();
			int numberOfSitches = netPlan.getNodes().size();
			int links[] = { 1, 1, 1, 1, 1, 1, 1, 1 };

			for (Node node : switches) {
				if (Xmin > node.getXYPositionMap().getX())
					Xmin = node.getXYPositionMap().getX();

				if (Xmax < node.getXYPositionMap().getX())
					Xmax = node.getXYPositionMap().getX();

				if (Ymin > node.getXYPositionMap().getY())
					Ymin = node.getXYPositionMap().getY();

				if (Ymax < node.getXYPositionMap().getY())
					Ymax = node.getXYPositionMap().getY();
			}

			List<String> fields = new ArrayList<String>(Arrays.asList(lines[0]
					.split(";")));

			for (int i = 1; i < lines.length; i++) {
				String[] params = lines[i].split(";");

				//Adding LRUs
				String[] nodesToLink = params[fields
						.indexOf(AFDXParameters.CSV_LINKS_FIELD)].split(",");

				Node nodeDestination = netPlan
						.getNodeByName(AFDXParameters.SWITCH_NAME
								+ nodesToLink[0]);
				int ports = Integer.parseInt(nodeDestination
						.getAttribute(AFDXParameters.ATT_SWITCH_PORTS));

				if (nodeDestination.getIncomingLinks().size() >= ports) {
					System.out.println("Max number of ports reached for node "
							+ nodeDestination.getName());
					nodeDestination = null;
				}

				double x, y;
				x = nodeDestination.getXYPositionMap().getX();
				int s = Integer.parseInt(nodesToLink[0]);
				if (nodesToLink.length < numberOfSitches)
					links[s - 1]++;

				if (x <= Xmin)
					x -= 20 * links[s - 1];
				if (x >= Xmax)
					x += 20 * links[s - 1];

				y = nodeDestination.getXYPositionMap().getY();
				if (y <= Ymin)
					y -= 20 * links[s - 1];
				if (y >= Ymax)
					y += 20 * links[s - 1];

				if (nodesToLink.length >= numberOfSitches) {
					x = Xmin + (Xmax - Xmin) / 2;
					y = Ymin + (Ymax - Ymin) / 2;
				}

				Node nodeOrigin = netPlan.addNode(x, y,
						params[fields.indexOf(AFDXParameters.CSV_NAME_FIELD)],
						null);

				netPlan.addLink(nodeOrigin, nodeDestination, nodeLinkCapacity,
						lengthInKm,
						AFDXParameters.propagationSpeedInKmPerSecond, null);
				netPlan.addLink(nodeDestination, nodeOrigin, nodeLinkCapacity,
						lengthInKm,
						AFDXParameters.propagationSpeedInKmPerSecond, null);

				//Adding LRU Links to switches
				boolean first = true;
				for (String link : nodesToLink) {
					if (first) {
						first = false;
						continue;
					}

					nodeDestination = netPlan
							.getNodeByName(AFDXParameters.SWITCH_NAME + link);

					netPlan.addLink(nodeOrigin, nodeDestination,
							nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
					netPlan.addLink(nodeDestination, nodeOrigin,
							nodeLinkCapacity, lengthInKm,
							AFDXParameters.propagationSpeedInKmPerSecond, null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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
