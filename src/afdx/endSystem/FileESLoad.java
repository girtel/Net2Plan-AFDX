package afdx.endSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import afdx.AFDXParameters;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

public class FileESLoad implements IAlgorithm {
	private InputParameter fileName = new InputParameter(AFDXParameters.SIM_FILE_NAME, "", "");

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {

		String fileName = "0_ES";
		String sufix = algorithmParameters.get(AFDXParameters.SIM_FILE_NAME);
		if (!sufix.equals(""))
			fileName += "_" + sufix;
		fileName += ".csv";

		double lengthInKm = 0.05;// 50 meters
		int space = 100;

		String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.substring(0, path.lastIndexOf("/") + 1);

		try {
			String content = new String(Files.readAllBytes(Paths.get(path + fileName)));

			String lines[] = content.split("[\\r\\n]+");

			List<String> fields = new ArrayList<String>(Arrays.asList(lines[0].split(";")));

			Hashtable<String, String> node_id_name = loadSwitchNames(netPlan, fileName);

			boolean firstLine = true;
			for (String line : lines) {
				if (firstLine) {
					firstLine = false;
					continue;
				}

				String[] params = line.split(";");

				// Adding LRUs
				String[] nodesToLink = params[fields.indexOf(AFDXParameters.CSV_LINKS_FIELD)].split(",");

				int xPos = (int) (Double.parseDouble(params[fields.indexOf(AFDXParameters.CSV_X_POS_FIELD)]) * space);
				int yPos = (int) (Double.parseDouble(params[fields.indexOf(AFDXParameters.CSV_Y_POS_FIELD)]) * space);

				Node nodeOrigin = netPlan.addNode(xPos, yPos, params[fields.indexOf(AFDXParameters.CSV_NAME_FIELD)],
						null);

				for (String node : nodesToLink) {
					Node nodeDestination = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + node);
					int ports = Integer.parseInt(nodeDestination.getAttribute(AFDXParameters.ATT_SWITCH_PORTS));

					if (nodeDestination.getIncomingLinks().size() >= ports) {
						System.out.println("Max number of ports reached for node " + nodeDestination.getName());
						nodeDestination = null;
					}

					String link_direction = params[fields.indexOf(AFDXParameters.CSV_LINK_DIRECTION_FIELD)];
					if (link_direction.equals("o") || link_direction.equals("b"))
						netPlan.addLink(nodeOrigin, nodeDestination,
								Double.parseDouble(params[fields.indexOf(AFDXParameters.CSV_LINK_CAPACITY_FIELD)]),
								lengthInKm, AFDXParameters.propagationSpeedInKmPerSecond, null);
					if (link_direction.equals("i") || link_direction.equals("b"))
						netPlan.addLink(nodeDestination, nodeOrigin,
								Double.parseDouble(params[fields.indexOf(AFDXParameters.CSV_LINK_CAPACITY_FIELD)]),
								lengthInKm, AFDXParameters.propagationSpeedInKmPerSecond, null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return null;
	}

	public Hashtable<String, String> loadSwitchNames(NetPlan netPlan, String fileName) {
		Hashtable<String, String> result = new Hashtable<String, String>();

		String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.substring(0, path.lastIndexOf("/") + 1);

		try {
			String content = new String(Files.readAllBytes(Paths.get(path + fileName)));

			String lines[] = content.split("[\\r\\n]+");

			List<String> fields = new ArrayList<String>(Arrays.asList(lines[0].split(";")));

			boolean firstLine = true;
			for (String line : lines) {
				if (firstLine) {
					firstLine = false;
					continue;
				}

				String[] params = line.split(";");

				result.put(params[fields.indexOf(AFDXParameters.CSV_ID_FIELD)],
						params[fields.indexOf(AFDXParameters.CSV_NAME_FIELD)]);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters() {
		return InputParameter.getInformationAllInputParameterFieldsOfObject(this);
	}

}
