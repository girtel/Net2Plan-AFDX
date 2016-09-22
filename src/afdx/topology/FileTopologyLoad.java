package afdx.topology;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

import afdx.afdx.AFDXParameters;

public class FileTopologyLoad implements IAlgorithm {
	private InputParameter fileName = new InputParameter(AFDXParameters.SIM_FILE_NAME, "", "");

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {
		String fileName = "0_TOPOLOGY.csv";
		String folderName = algorithmParameters.get(AFDXParameters.SIM_FILE_NAME);
		fileName = (!folderName.equals("")
				? AFDXParameters.CONFIGURATION_TABLES_FOLDER + File.separator + folderName + File.separator : "")
				+ fileName;

		double lengthInKm = 0.05;// 50 meters
		int space = 100;

		String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.substring(0, path.lastIndexOf("/") + 1);

		try {
			String content = new String(Files.readAllBytes(Paths.get(path + fileName)));

			String lines[] = content.split("[\\r\\n]+");

			List<String> fields = new ArrayList<String>(Arrays.asList(lines[0].split(";")));

			Hashtable<String, String> node_id_name = new Hashtable<String, String>();

			boolean firstLine = true;
			for (String line : lines) {
				if (firstLine) {
					firstLine = false;
					continue;
				}

				String[] params = line.split(";");

				int xPos = Integer.parseInt(params[fields.indexOf(AFDXParameters.CSV_X_POS_FIELD)]) * space;
				int yPos = Integer.parseInt(params[fields.indexOf(AFDXParameters.CSV_Y_POS_FIELD)]) * space;

				Node node = netPlan.addNode(xPos, yPos, params[fields.indexOf(AFDXParameters.CSV_NAME_FIELD)], null);
				node.setAttribute(AFDXParameters.ATT_SWITCH_ID, params[fields.indexOf(AFDXParameters.CSV_ID_FIELD)]);
				node.setAttribute(AFDXParameters.ATT_SWITCH_PORTS,
						params[fields.indexOf(AFDXParameters.CSV_PORTS_FIELD)]);
				node.setAttribute(AFDXParameters.ATT_SWITCH_PORT_CAPACITY,
						params[fields.indexOf(AFDXParameters.CSV_LINK_CAPACITY_FIELD)]);

				node_id_name.put(params[fields.indexOf(AFDXParameters.CSV_ID_FIELD)],
						params[fields.indexOf(AFDXParameters.CSV_NAME_FIELD)]);
			}

			firstLine = true;
			for (String line : lines) {
				if (firstLine) {
					firstLine = false;
					continue;
				}

				String[] params = line.split(";");

				String[] links = params[fields.indexOf(AFDXParameters.CSV_LINKS_FIELD)].split(",");
				for (String link : links) {
					Node src_node = netPlan.getNodeByName(params[fields.indexOf(AFDXParameters.CSV_NAME_FIELD)]);
					Node dst_node = netPlan.getNodeByName(node_id_name.get(link));
					netPlan.addLink(src_node, dst_node,
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
