package afdx.vl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastDemand;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

import afdx.afdx.AFDXParameters;

public class FileVLLoad implements IAlgorithm {
	private InputParameter fileName = new InputParameter(AFDXParameters.SIM_FILE_NAME, "", "");

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {

		String fileName = "0_VL.csv";
		String folderName = algorithmParameters.get(AFDXParameters.SIM_FILE_NAME);

		String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.substring(0, path.lastIndexOf("/") + 1);

		try {
			String content = new String(Files.readAllBytes(Paths.get(path + (!folderName.equals("")
					? AFDXParameters.CONFIGURATION_TABLES_FOLDER + File.separator + folderName + File.separator : "")
					+ fileName)));

			String lines[] = content.split("[\\r\\n]+");

			List<String> fields = new ArrayList<String>(Arrays.asList(lines[0].split(";")));

			boolean firstLine = true;
			for (String line : lines) {
				if (firstLine) {
					firstLine = false;
					continue;
				}
				String[] params = line.split(";");

				// Params
				String VL_ES_Tx = params[fields.indexOf(AFDXParameters.CSV_SRC_FIELD)];
				String VL_ES_Rxs[] = params[fields.indexOf(AFDXParameters.CSV_DSTS_FIELD)].split(",");
				String VL_routes[] = null;
				try {
					VL_routes = params[fields.indexOf(AFDXParameters.CSV_ROUTES_FIELD)].split(":");
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Adding VLs as demands or multi demands
				Demand demand = null;
				MulticastDemand multicastDemand = null;

				Node lruSrcNode = netPlan.getNodeByName(params[fields.indexOf(AFDXParameters.CSV_SRC_FIELD)]);

				String[] lruDstNames = params[fields.indexOf(AFDXParameters.CSV_DSTS_FIELD)].split(",");

				if (lruDstNames.length == 1) {
					// unicast
					Node lruDstNode = netPlan.getNodeByName(lruDstNames[0]);
					demand = netPlan.addDemand(lruSrcNode, lruDstNode, 0, null);
					demand.setAttribute(AFDXParameters.ATT_VL_ID, params[fields.indexOf(AFDXParameters.CSV_ID_FIELD)]);
					demand.setAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES,
							params[fields.indexOf(AFDXParameters.CSV_L_MAX_FIELD)]);
					demand.setAttribute(AFDXParameters.ATT_VL_BAG_MS,
							params[fields.indexOf(AFDXParameters.CSV_BAG_FIELD)]);
					demand.setAttribute(AFDXParameters.ATT_PERVL_PACKET_ARRIVAL_TYPE,
							params[fields.indexOf(AFDXParameters.CSV_PERVL_PACKET_ARRIVAL_TYPE)]);
					demand.setAttribute(AFDXParameters.ATT_REGULATOR_QUEUE_SIZE,
							params[fields.indexOf(AFDXParameters.CSV_REGULATOR_QUEUE_SIZE)]);
					int packetSize = Integer.parseInt(params[fields.indexOf(AFDXParameters.CSV_L_MAX_FIELD)]);
					if (params[fields.indexOf(AFDXParameters.CSV_PROTOCOL)].equals(AFDXParameters.UDPProtocol + ""))
						packetSize += AFDXParameters.UDPHeaderBytes;
					packetSize += AFDXParameters.IPHeaderBytes;
					packetSize += AFDXParameters.ETHHeaderBytes + AFDXParameters.IFGBytes;
					demand.setOfferedTraffic(1000 * 8 * packetSize
							/ Double.parseDouble(params[fields.indexOf(AFDXParameters.CSV_BAG_FIELD)]));
				} else {
					// multicast
					Set<Node> nodes = new HashSet<Node>();
					for (String node : lruDstNames) {
						Node lruDstNode = netPlan.getNodeByName(node);

						nodes.add(lruDstNode);
					}

					try {
						multicastDemand = netPlan.addMulticastDemand(lruSrcNode, nodes, 0, null);
					} catch (Exception e) {
						throw new RuntimeException("Error creating multicast demand, review the csv file.");
					}
					multicastDemand.setAttribute(AFDXParameters.ATT_VL_ID,
							params[fields.indexOf(AFDXParameters.CSV_ID_FIELD)]);
					multicastDemand.setAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES,
							params[fields.indexOf(AFDXParameters.CSV_L_MAX_FIELD)]);
					multicastDemand.setAttribute(AFDXParameters.ATT_VL_BAG_MS,
							params[fields.indexOf(AFDXParameters.CSV_BAG_FIELD)]);
					multicastDemand.setAttribute(AFDXParameters.ATT_PERVL_PACKET_ARRIVAL_TYPE,
							params[fields.indexOf(AFDXParameters.CSV_PERVL_PACKET_ARRIVAL_TYPE)]);
					multicastDemand.setAttribute(AFDXParameters.ATT_REGULATOR_QUEUE_SIZE,
							params[fields.indexOf(AFDXParameters.CSV_REGULATOR_QUEUE_SIZE)]);
					int packetSize = Integer.parseInt(params[fields.indexOf(AFDXParameters.CSV_L_MAX_FIELD)]);
					if (params[fields.indexOf(AFDXParameters.CSV_PROTOCOL)].equals(AFDXParameters.UDPProtocol + ""))
						packetSize += AFDXParameters.UDPHeaderBytes;
					packetSize += AFDXParameters.IPHeaderBytes;
					packetSize += AFDXParameters.ETHHeaderBytes + AFDXParameters.IFGBytes;
					multicastDemand.setOfferedTraffic(1000 * 8 * packetSize
							/ Double.parseDouble(params[fields.indexOf(AFDXParameters.CSV_BAG_FIELD)]));
				}

				if (VL_routes == null || VL_routes.length < VL_ES_Rxs.length || VL_routes[0].length() == 0) {
					if (demand != null) {
					} else if (multicastDemand != null) {
					}
				} else {
					// Adding routes to each demand or multi demand
					if (VL_routes.length == 1) {
						// unicast
						String route[] = VL_routes[0].split(",");

						List<Link> links = new ArrayList<Link>();

						// Add Link from transmitter ES to first switch
						Node node_tx = netPlan.getNodeByName(VL_ES_Tx);
						Set<Link> node_tx_links = node_tx.getOutgoingLinks();

						for (Link link : node_tx_links) {
							if (link.getDestinationNode().getName().equals(AFDXParameters.SWITCH_NAME + route[0])) {
								links.add(link);
								break;
							} else
								// error
								;
						}

						// add switch links
						for (int j = 0; j < route.length - 1; j++) {
							Node node_origin = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + route[j]);

							Set<Link> node_origin_links = node_origin.getOutgoingLinks();
							for (Link link : node_origin_links) {
								if (link.getDestinationNode().getName()
										.equals(AFDXParameters.SWITCH_NAME + route[j + 1])) {
									links.add(link);
									break;
								} else
									// error
									;
							}
						}

						// Add Link from last switch to receiver ES
						Node last_switch = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + route[route.length - 1]);
						Set<Link> node_rx_links = last_switch.getOutgoingLinks();

						for (Link link : node_rx_links) {
							if (link.getDestinationNode().getName().equals(VL_ES_Rxs[0])) {
								links.add(link);
								break;
							} else
								// error
								;
						}

						// Add route to topology
						netPlan.addRoute(demand, 0, 0, links, null);
					} else {
						// multicast
						Set<Link> links = new HashSet<Link>();

						// Add Link from transmitter ES to first switch
						Node node_tx = netPlan.getNodeByName(VL_ES_Tx);
						Set<Link> node_tx_links = node_tx.getOutgoingLinks();
						links.add(node_tx_links.iterator().next());

						// add switch links
						for (int k = 0; k < VL_routes.length; k++) {
							String route[] = VL_routes[k].split(",");

							for (int j = 0; j < route.length - 1; j++) {
								Node node_origin = netPlan.getNodeByName(AFDXParameters.SWITCH_NAME + route[j]);

								Set<Link> node_origin_links = node_origin.getOutgoingLinks();
								for (Link link : node_origin_links) {
									if (link.getDestinationNode().getName()
											.equals(AFDXParameters.SWITCH_NAME + route[j + 1])) {
										links.add(link);
										break;
									} else
										// error
										;
								}
							}

							// Add Link from last switch to receiver ES
							Node last_switch = netPlan
									.getNodeByName(AFDXParameters.SWITCH_NAME + route[route.length - 1]);
							Set<Link> node_rx_links = last_switch.getOutgoingLinks();

							for (Link link : node_rx_links) {
								if (link.getDestinationNode().getName().equals(VL_ES_Rxs[k])) {
									links.add(link);
									break;
								} else
									// error
									;
							}
						}

						// Add tree to topology
						netPlan.addMulticastTree(multicastDemand, 0, 0, links, null);
					}
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
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters() {
		return InputParameter.getInformationAllInputParameterFieldsOfObject(this);
	}

}
