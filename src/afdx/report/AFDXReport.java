package afdx.report;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.xy.YIntervalSeries;

import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Route;
import com.net2plan.utils.Triple;

import afdx.afdx.AFDXParameters;
import afdx.afdx.AFDXTools;
import afdx.afdx.VL;
import afdx.afdx.VLLatency;
import afdx.algorithm.FIFONCAlgorithm;
import afdx.algorithm.FIFOTAAlgorithm;
import afdx.simulator.AFDXBasicSimulator;

public class AFDXReport implements IReport {

	@Override
	public String executeReport(NetPlan netPlan, Map<String, String> reportParameters,
			Map<String, String> net2planParameters) {

		List<VL> routeParameters = getRouteParameters(netPlan);
		List<VL> treeParameters = getTreeParameters(netPlan);

		return printReport(netPlan, routeParameters, treeParameters);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters() {

		return null;
	}

	public List<VL> getRouteParameters(NetPlan netPlan) {
		List<VL> parameters = new LinkedList<VL>();

		for (Route route : netPlan.getRoutes()) {
			String name = route.getAttribute(AFDXParameters.ATT_VL_NAME);
			if (name == null)
				name = "VL_unicast_" + route.getIndex();
			VL vl = new VL(route);
			vl.setName(name);
			parameters.add(vl);
		}

		System.out.println("VLs routes" + parameters.size());
		return parameters;
	}

	public List<VL> getTreeParameters(NetPlan netPlan) {
		List<VL> parameters = new LinkedList<VL>();

		for (MulticastTree tree : netPlan.getMulticastTrees()) {
			String name = tree.getAttribute(AFDXParameters.ATT_VL_NAME);
			if (name == null)
				name = "VL_multicast_" + tree.getIndex();
			VL vl = new VL(tree);
			vl.setName(name);
			parameters.add(vl);
		}

		System.out.println("VLs trees" + parameters.size());

		return parameters;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	private String printReport(NetPlan netPlan, List<VL> vlRouteParameters, List<VL> vlTreeParameters) {
		StringBuilder out = new StringBuilder();

		out.append("<html>");
		out.append("<head><title>AFDX Network Configuration report</title></head>");
		out.append("<html><body>");
		out.append("<h1>AFDX Network Configuration report</h1>");
		out.append(
				"<p>This report checks the correctness of the AFDX network configuration design loaded in net2plan.</p>");
		out.append("<ul>");
		out.append(
				"<li>The Part 1 shows the configuration table analysed and shows all the information loaded in Net2Plan. It alse checks the validity of the configuration table base on Arinc 664 part 7. In case that some parameter is not fulfiling the Standard is shown in red.</li>");
		out.append(
				"<li>The Part 2 shows the simulation results. It shows in two tables, one for Unicast VLs and another for multicast VLs. The resutls are shown in a table having differents columns for TA Algirthm, NC Algoritm and Simulations. TA and NC are compare with the simulation worst case by a percentage. In case a simulation worst case value is bigger than TA o NC calculated value is shown in red.</li>");
		out.append("</ul>");
		out.append("<h2>Part 1: AFDX Configuration Table</h2>");

		out.append("<h3>Switches</h3>");
		out.append("<p>Here is shown the switches in the AFDX network and the port occupacy of each.</p>");
		out.append("<table border='1'>");
		out.append(
				"<tr><th><b>Switch Name</b></th><th><b>Number of ports</b></th><th><b>Connected ports</b></th></tr>");

		for (Node node : netPlan.getNodes()) {
			String name = node.getName();
			if (!name.contains(AFDXParameters.SWITCH_NAME))
				continue;

			int ports = Integer.parseInt(node.getAttribute(AFDXParameters.ATT_SWITCH_PORTS));
			int connectedPorts = node.getOutgoingLinks().size();
			out.append("<tr><td>").append(name).append("</td>");
			out.append("<td align=\"center\">").append(ports).append("</td>");
			out.append("<td align=\"center\">");
			if (connectedPorts > ports)
				out.append(setErrorColor(connectedPorts + ""));
			else
				out.append(connectedPorts + "");
			out.append("</td></tr>");
		}
		out.append("</table>");

		out.append("<h3>End Systems</h3>");
		out.append("<p>Here is shown the LRU in the AFDX network and the number of VL starting in this LRU.</p>");
		out.append("<table border='1'>");
		out.append("<tr><th><b>ES Name</b></th><th><b>Number of VLs</b></th></tr>");

		for (Node node : netPlan.getNodes()) {
			String name = node.getName();
			if (name.contains(AFDXParameters.SWITCH_NAME))
				continue;

			int numberVls = node.getOutgoingRoutes().size() + node.getOutgoingMulticastTrees().size();
			out.append("<tr><td>").append(name).append("</td>");
			out.append("<td align=\"center\">");
			if (numberVls > AFDXParameters.MaxNumberOfVLPerES)
				out.append(setErrorColor(numberVls + ""));
			else
				out.append(numberVls + "");
			out.append("</td></tr>");
		}
		int vls = vlRouteParameters.size() + vlTreeParameters.size();
		out.append("<tr><td><b>").append("TOTAL").append("</b></td>");
		out.append("<td align=\"center\">");
		if (vls > AFDXParameters.MaxNumberOfVLs)
			out.append(setErrorColor(vls + ""));
		else
			out.append(vls);
		out.append("</td></tr>");
		out.append("</table>");

		out.append("<h3>Unicast Virtual Links</h3>");
		out.append("<p>Here is shown the Unicast VL loaded in the configuration table with its parameters.</p>");
		out.append("<table border='1'>");
		out.append(
				"<tr><th><b>VL Name</b></th><th><b>Source LRU</b></th><th><b>Destination LRU</b></th><b>BAG (ms)</b></th><th><b>L Max (bytes)</b></th><th><b>Max. Bandwidth (Kbps)</b></th></tr>");

		for (VL vl : vlRouteParameters) {
			String name = vl.getName();
			out.append("<tr>");
			out.append("<td>").append(name).append("</td>");
			out.append("<td>").append(vl.getRoute().getIngressNode().getName()).append("</td>");
			out.append("<td>").append(vl.getRoute().getEgressNode().getName()).append("</td>");
			out.append("<td align=\"center\">").append(vl.getBagMs()).append("</td>");
			out.append("<td align=\"center\">").append(vl.getLmaxIPPacket()).append("</td>");
			out.append("<td align=\"center\">").append(AFDXTools.df_2.format(vl.getLmaxIPPacket() * 8 / vl.getBagMs()))
					.append("</td>");
			out.append("</tr>");
		}
		out.append("</table>");

		out.append("<h3>Multicast Virtual Links</h3>");
		out.append("<p>Here is shown the Multicast VL loaded in the configuration table with its parameters.</p>");
		out.append("<table border='1'>");
		out.append(
				"<tr><th><b>VL Name</b></th><th><b>Source LRU</b></th><th><b>Destination LRU</b></th><b>BAG (ms)</b></th><th><b>L Max (bytes)</b></th><th><b>Max. Bandwidth (Kbps)</b></th></tr>");

		for (VL vl : vlTreeParameters) {
			String name = vl.getName();
			String destiny = null;
			for (Node node : vl.getTree().getEgressNodes()) {
				if (destiny == null)
					destiny = node.getName();
				else
					destiny = destiny + ", " + node.getName();
			}
			out.append("<tr>");
			out.append("<td>").append(name).append("</td>");
			out.append("<td>").append(vl.getTree().getIngressNode().getName()).append("</td>");
			out.append("<td>").append(destiny).append("</td>");
			out.append("<td align=\"center\">").append(vl.getBagMs()).append("</td>");
			out.append("<td align=\"center\">").append(vl.getLmaxIPPacket()).append("</td>");
			out.append("<td align=\"center\">").append(AFDXTools.df_2.format(vl.getLmaxIPPacket() * 8 / vl.getBagMs()))
					.append("</td>");
			out.append("</tr>");
		}
		out.append("</table>");

		out.append("<h3>Network Links</h3>");
		out.append(
				"<p>Here is shown the links for the AFDX network. Some information is show to checks the occupacy and the number of VLs crossing each link.</p>");
		out.append("<table border='1'>");
		out.append(
				"<tr><th><b>Link index</b></th><th><b>Origin Node</b></th><th><b>Destination Node</b></th><th><b>Capacity (Mbps)</b></th><th><b>Max ocupacy (Kbps)</b></th><th><b>Crossing Routes</b></th><th><b>Crossing Trees</b></th></tr>");

		for (Link link : netPlan.getLinks()) {
			out.append("<tr>");
			out.append("<td>").append(link.getIndex()).append("</td>");
			out.append("<td>").append(link.getOriginNode().getName()).append("</td>");
			out.append("<td>").append(link.getDestinationNode().getName()).append("</td>");
			out.append("<td align=\"center\">").append(link.getCapacity() / 1000000).append("</td>");
			out.append("<td align=\"center\">")
					.append(AFDXTools.df_2
							.format(link.getUtilizationNotIncludingProtectionSegments() * link.getCapacity() / 1000))
					.append(" (" + AFDXTools.df_2.format(100 * link.getUtilizationNotIncludingProtectionSegments())
							+ "%)")
					.append("</td>");
			out.append("<td align=\"center\">").append(link.getTraversingRoutes().size()).append("</td>");
			out.append("<td align=\"center\">").append(link.getTraversingTrees().size()).append("</td>");
			out.append("</tr>");
		}
		out.append("</table>");

		out.append("<h2>Part 2: AFDX Configuration Results</h2>");
		out.append(
				"<p>Here is shown the results from Off-Line and On-Line AFDX library. The TA Delay column shows the delay for each VL base on TA Algoritm. There is a % meaning the % over the worst case simulation delay. The NC Delay shows the similar information for Networ Calculus Algoritm. Finally there are three columns for simulation showing the worst case, the best case and the aritmetic mean. In case a TA or NC value is smaller than simulation worst case value the delay is shown in red.</p>");
		out.append("<p>After each table a graphics is showing the values to show visually the results</p>");
		out.append("<h2>Simulation information</h2>");
		try {
			out.append("<p>This network and configuration table has been simulated for "
					+ AFDXTools.df_3.format(netPlan.getAttribute(AFDXParameters.ATT_SIM_TIME))
					+ " ms. During this time the simulator has generated the following number of packets</p>");

		} catch (Exception e) {
		}
		out.append("<table border='1'>");
		try {
			out.append("<tr><th align=\"left\"><b>Simulation time (ms)</b></th><th>"
					+ AFDXTools.df_3.format(netPlan.getAttribute(AFDXParameters.ATT_SIM_TIME)) + "</th></tr>");

		} catch (Exception e) {
		}
		out.append("<tr><th align=\"left\"><b>Unicast Packets</b></th><th>"
				+ netPlan.getAttribute(AFDXParameters.ATT_SIM_ROUTE_COUNTER) + "</th></tr>");
		out.append("<tr><th align=\"left\"><b>Multicast Packets</b></th><th>"
				+ netPlan.getAttribute(AFDXParameters.ATT_SIM_TREE_COUNTER) + "</th></tr>");
		out.append("<tr><th align=\"left\"><b>Multicast destination Packets</b></th><th>"
				+ netPlan.getAttribute(AFDXParameters.ATT_SIM_TREE_DESTINATION_COUNTER) + "</th></tr>");
		out.append("</table>");

		out.append("<h3>Unicast Virtual Links</h3>");
		out.append("<table border='1'>");
		out.append(
				"<tr><th><b>VL Name</b></th><th><b>Source LRU</b></th><th><b>Destination LRU</b></th></th><th><b>E/S Jitter (ms)</b></th><th><b>TA Delay (ms)(% Max SIM)</b></th><th><b>NC Delay (ms)</b></th><th><b>SIM Max Delay (ms)</b></th><th><b>SIM Mean Delay (ms)</b></th><th><b>SIM Min Delay (ms)</b></th><th><b>Min Delay (ms)</b></th></tr>");

		List<VLLatency> sim = new ArrayList<VLLatency>();
		List<VLLatency> ta = new ArrayList<VLLatency>();
		List<VLLatency> nc = new ArrayList<VLLatency>();
		List<VLLatency> minimum = new ArrayList<VLLatency>();

		for (VL vl : vlRouteParameters) {
			String name = vl.getName();
			Route route = netPlan.getRoute(vl.getRoute().getIndex());

			// JITTER
			String attribute = AFDXParameters.ATT_VL_JITTER.replace("XX",
					route.getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			String jitter = AFDXTools.df_3
					.format(Double.parseDouble(netPlan.getRoute(vl.getRoute().getIndex()).getAttribute(attribute)));

			// TA
			attribute = AFDXParameters.ATT_VL_DST_DELAY.replace("XX",
					route.getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", "" + route.getEgressNode().getName());
			String delayTA = AFDXTools.df_3.format(Double.parseDouble(
					netPlan.getRoute(vl.getRoute().getIndex()).getAttribute(FIFOTAAlgorithm.prefix + attribute)));
			double delayTADouble = Double.parseDouble(delayTA);

			ta.add(new VLLatency(vl.getRoute().getIndex(), delayTADouble, delayTADouble, delayTADouble));

			// MINIMUM DELAY
			attribute = AFDXParameters.ATT_VL_DST_DELAY_MIN.replace("XX",
					route.getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", "" + route.getEgressNode().getName());
			String delayMin = AFDXTools.df_3
					.format(Double.parseDouble(netPlan.getRoute(vl.getRoute().getIndex()).getAttribute(attribute)));
			double delayMinDouble = Double.parseDouble(delayMin);

			minimum.add(new VLLatency(vl.getRoute().getIndex(), delayMinDouble, delayMinDouble, delayMinDouble));

			// NC
			attribute = AFDXParameters.ATT_VL_DST_DELAY.replace("XX",
					route.getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", "" + route.getEgressNode().getName());
			String delayNC = AFDXTools.df_3.format(Double.parseDouble(
					netPlan.getRoute(vl.getRoute().getIndex()).getAttribute(FIFONCAlgorithm.prefix + attribute)));
			double delayNCDouble = Double.parseDouble(delayNC);

			nc.add(new VLLatency(vl.getRoute().getIndex(), delayNCDouble, delayNCDouble, delayNCDouble));

			// SIM
			String delayMaxSIM;
			try {
				attribute = AFDXParameters.ATT_VL_DST_DELAY_MAX.replace("XX",
						route.getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", "" + route.getEgressNode().getName());
				delayMaxSIM = AFDXTools.df_3.format(Double.parseDouble(netPlan.getRoute(vl.getRoute().getIndex())
						.getAttribute(AFDXBasicSimulator.prefix + attribute)));
			} catch (Exception e) {
				delayMaxSIM = "-1";
				e.printStackTrace();
			}
			double delayMaxSIMDouble = -999999;
			try {
				delayMaxSIMDouble = Double.parseDouble(delayMaxSIM);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String delayMeanSIM;
			try {
				attribute = AFDXParameters.ATT_VL_DST_DELAY_MEAN.replace("XX",
						route.getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", "" + route.getEgressNode().getName());
				delayMeanSIM = AFDXTools.df_3.format(Double.parseDouble(netPlan.getRoute(vl.getRoute().getIndex())
						.getAttribute(AFDXBasicSimulator.prefix + attribute)));
			} catch (Exception e) {
				delayMeanSIM = "-1";
				e.printStackTrace();
			}
			double delayMeanSIMDouble = -999999;
			try {
				delayMeanSIMDouble = Double.parseDouble(delayMeanSIM);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String delayMinSIM;
			try {
				attribute = AFDXParameters.ATT_VL_DST_DELAY_MIN.replace("XX",
						route.getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", "" + route.getEgressNode().getName());
				delayMinSIM = AFDXTools.df_3.format(Double.parseDouble(netPlan.getRoute(vl.getRoute().getIndex())
						.getAttribute(AFDXBasicSimulator.prefix + attribute)));
			} catch (Exception e) {
				delayMinSIM = "-1";
				e.printStackTrace();
			}
			double delayMinSIMDouble = -999999;
			try {
				delayMinSIMDouble = Double.parseDouble(delayMinSIM);
			} catch (Exception e) {
				e.printStackTrace();
			}

			sim.add(new VLLatency(vl.getRoute().getIndex(), delayMeanSIMDouble, delayMinSIMDouble, delayMaxSIMDouble));

			out.append("<tr>");
			out.append("<td>").append(name).append("</td>");
			out.append("<td>").append(vl.getRoute().getIngressNode().getName()).append("</td>");
			out.append("<td>").append(vl.getRoute().getEgressNode().getName()).append("</td>");

			// JITTER
			out.append("<td align=\"center\">");
			if (Double.parseDouble(jitter) > AFDXParameters.MaxJitterInMsPerES)
				out.append(setErrorColor(jitter));
			else
				out.append(jitter);
			out.append("</td>");

			// TA
			out.append("<td align=\"center\">");
			String text = delayTA + " ("
					+ AFDXTools.df_2.format(100 * (delayTADouble - delayMaxSIMDouble) / delayMaxSIMDouble) + " %)";
			if (delayMaxSIMDouble > delayTADouble)
				out.append(setErrorColor(text));
			else if (delayMaxSIMDouble == delayTADouble)
				out.append(setExactColor(text));
			else
				out.append(text);
			out.append("</td>");

			// NC
			out.append("<td align=\"center\">");
			text = delayNC + " (" + AFDXTools.df_2.format(100 * (delayNCDouble - delayMaxSIMDouble) / delayMaxSIMDouble)
					+ " %)";
			if (delayMaxSIMDouble > delayNCDouble)
				out.append(setErrorColor(text));
			else if (delayMaxSIMDouble == delayNCDouble)
				out.append(setExactColor(text));
			else
				out.append(text);
			out.append("</td>");

			// SIM
			out.append("<td align=\"center\">");
			text = delayMaxSIM + " ("
					+ AFDXTools.df_2.format(100 * ((delayMaxSIMDouble - delayMeanSIMDouble) / delayMeanSIMDouble))
					+ "%)";
			out.append(text);
			out.append("</td>");

			out.append("<td align=\"center\">");
			out.append(delayMeanSIM);
			out.append("</td>");

			out.append("<td align=\"center\">");
			text = delayMinSIM + " ("
					+ AFDXTools.df_2.format(100 * ((delayMinSIMDouble - delayMeanSIMDouble) / delayMinSIMDouble))
					+ "%)";
			out.append(text);
			out.append("</td>");

			// MINIMUM Delay
			out.append("<td align=\"center\">");
			text = delayMin + " (" + AFDXTools.df_2.format(100 * (1 - delayMinSIMDouble / delayMinDouble)) + " %)";
			if (delayMinSIMDouble < delayMinDouble)
				out.append(setErrorColor(text));
			else if (delayMinSIMDouble == delayMinDouble)
				out.append(setExactColor(text));
			else
				out.append(text);
			out.append("</td>");

			out.append("</tr>");
		}
		out.append("</table>");

		List<YIntervalSeries> serie = new ArrayList<YIntervalSeries>();
		YIntervalSeries SimSerie = new YIntervalSeries("Sim");
		serie.add(SimSerie);
		YIntervalSeries TASerie = new YIntervalSeries("TA");
		serie.add(TASerie);
		YIntervalSeries NCSerie = new YIntervalSeries("NC");
		serie.add(NCSerie);
		YIntervalSeries MinSerie = new YIntervalSeries("Min");
		serie.add(MinSerie);
		double min = 0, max = -1;

		ta.sort(new VLLatency(0, 0, 0, 0));
		int pos = 0;

		for (VLLatency vllatency : ta) {
			int index = vllatency.getVLID();

			TASerie.add(pos, vllatency.getMean(), vllatency.getMin(), vllatency.getMax());
			min = vllatency.getMean() < min ? vllatency.getMean() : min;
			max = vllatency.getMean() > max ? vllatency.getMean() : max;

			NCSerie.add(pos, nc.get(index).getMean(), nc.get(index).getMin(), nc.get(index).getMax());
			min = nc.get(index).getMean() < min ? nc.get(index).getMean() : min;
			max = nc.get(index).getMean() > max ? nc.get(index).getMean() : max;

			MinSerie.add(pos, minimum.get(index).getMean(), minimum.get(index).getMin(), minimum.get(index).getMax());
			min = nc.get(index).getMean() < min ? nc.get(index).getMean() : min;
			max = nc.get(index).getMean() > max ? nc.get(index).getMean() : max;

			SimSerie.add(pos, sim.get(index).getMean(), sim.get(index).getMin(), sim.get(index).getMax());
			min = sim.get(index).getMin() < min ? sim.get(index).getMin() : min;
			max = sim.get(index).getMax() > max ? sim.get(index).getMax() : max;

			pos++;
		}

		String filename = "Unicast_Graphs";
		DrawGraphs graphs = new DrawGraphs();
		graphs.xAxisLabel = "Vl Paths";
		graphs.yAxisLabel = "Latency (ms)";

		if (max >= min) {
			graphs.saveGraph2jpg(filename, serie, min, max, vlRouteParameters.size() * 5);

			String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			path = path.substring(0, path.lastIndexOf("/") + 1);
			String dir = path + filename + ".jpg";

			out.append("<img src=" + dir + " alt=\"Unicast Graphs\" style=\"width:1200px;height:500px;\">");
		}
		out.append("<h3>Multicast Virtual Links</h3>");
		out.append("<table border='1'>");
		out.append(
				"<tr><th><b>VL Name</b></th><th><b>Source LRU</b></th><th><b>Destination LRU</b></th></th><th><b>E/S Jitter (ms)</b></th><th><b>TA Delay (ms)(% Max SIM)</b></th><th><b>NC Delay (ms)</b></th><th><b>SIM Max Delay (ms)</b></th><th><b>SIM Mean Delay (ms)</b></th><th><b>SIM Min Delay (ms)</b></th><th><b>Min Delay (ms)</b></th></tr>");

		int vl_number = 0;

		sim = new ArrayList<VLLatency>();
		ta = new ArrayList<VLLatency>();
		nc = new ArrayList<VLLatency>();
		minimum = new ArrayList<VLLatency>();

		for (VL vl : vlTreeParameters) {
			String name = vl.getName();

			Set<Node> egressNodes = vl.getTree().getEgressNodes();
			int n = 0;
			for (Node node : egressNodes) {
				String destiny = node.getName();

				// JITTER
				String attribute = AFDXParameters.ATT_VL_JITTER.replace("XX",
						vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				String jitter = AFDXTools.df_3.format(
						Double.parseDouble(netPlan.getMulticastTree(vl.getTree().getIndex()).getAttribute(attribute)));

				// TA
				attribute = AFDXParameters.ATT_VL_DST_DELAY.replace("XX",
						vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", node.getName());

				String delayTA = AFDXTools.df_3.format(Double.parseDouble(netPlan
						.getMulticastTree(vl.getTree().getIndex()).getAttribute(FIFOTAAlgorithm.prefix + attribute)));
				double delayTADouble = Double.parseDouble(delayTA);

				TASerie.add(vl_number, delayTADouble, delayTADouble, delayTADouble);
				min = delayTADouble < min ? delayTADouble : min;
				max = delayTADouble > max ? delayTADouble : max;

				ta.add(new VLLatency(vl_number, delayTADouble, delayTADouble, delayTADouble));

				// MINIMUM DELAY
				attribute = AFDXParameters.ATT_VL_DST_DELAY_MIN.replace("XX",
						vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", node.getName());
				String delayMin = AFDXTools.df_3.format(
						Double.parseDouble(netPlan.getMulticastTree(vl.getTree().getIndex()).getAttribute(attribute)));
				double delayMinDouble = Double.parseDouble(delayMin);

				minimum.add(new VLLatency(vl.getTree().getIndex(), delayMinDouble, delayMinDouble, delayMinDouble));

				// NC
				attribute = AFDXParameters.ATT_VL_DST_DELAY.replace("XX",
						vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", node.getName());

				String delayNC = AFDXTools.df_3.format(Double.parseDouble(netPlan
						.getMulticastTree(vl.getTree().getIndex()).getAttribute(FIFONCAlgorithm.prefix + attribute)));
				double delayNCDouble = Double.parseDouble(delayNC);

				NCSerie.add(vl_number, delayNCDouble, delayNCDouble, delayNCDouble);
				min = delayNCDouble < min ? delayNCDouble : min;
				max = delayNCDouble > max ? delayNCDouble : max;

				nc.add(new VLLatency(vl_number, delayNCDouble, delayNCDouble, delayNCDouble));

				// SIM
				String delayMaxSIMDelay;
				try {
					attribute = AFDXParameters.ATT_VL_DST_DELAY_MAX.replace("XX",
							vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
					attribute = attribute.replace("YY", "" + node.getName());
					delayMaxSIMDelay = AFDXTools.df_3
							.format(Double.parseDouble(netPlan.getMulticastTree(vl.getTree().getIndex())
									.getAttribute(AFDXBasicSimulator.prefix + attribute)));
				} catch (Exception e) {
					delayMaxSIMDelay = "-1";
				}
				double delayMaxSIMDouble = -999999;
				try {
					delayMaxSIMDouble = Double.parseDouble(delayMaxSIMDelay);
				} catch (Exception e1) {
				}

				String delayMeanDelay;
				try {
					attribute = AFDXParameters.ATT_VL_DST_DELAY_MEAN.replace("XX",
							vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
					attribute = attribute.replace("YY", "" + node.getName());
					delayMeanDelay = AFDXTools.df_3
							.format(Double.parseDouble(netPlan.getMulticastTree(vl.getTree().getIndex())
									.getAttribute(AFDXBasicSimulator.prefix + attribute)));
				} catch (Exception e) {
					delayMeanDelay = "-1";
				}
				double delayMeanSIMDouble = -999999;
				try {
					delayMeanSIMDouble = Double.parseDouble(delayMeanDelay);
				} catch (NumberFormatException e1) {
				}

				String delayMinSIMDelay;
				try {
					attribute = AFDXParameters.ATT_VL_DST_DELAY_MIN.replace("XX",
							vl.getTree().getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
					attribute = attribute.replace("YY", "" + node.getName());
					delayMinSIMDelay = AFDXTools.df_3
							.format(Double.parseDouble(netPlan.getMulticastTree(vl.getTree().getIndex())
									.getAttribute(AFDXBasicSimulator.prefix + attribute)));
				} catch (Exception e) {
					delayMinSIMDelay = "-1";
				}
				double delayMinSIMDouble = -999999;
				try {
					delayMinSIMDouble = Double.parseDouble(delayMinSIMDelay);
				} catch (NumberFormatException e) {
				}

				SimSerie.add(vl_number++, delayMeanSIMDouble, delayMinSIMDouble, delayMaxSIMDouble);
				min = delayMinSIMDouble < min ? delayMinSIMDouble : min;
				max = delayMaxSIMDouble > max ? delayMaxSIMDouble : max;

				sim.add(new VLLatency(vl_number, delayMeanSIMDouble, delayMinSIMDouble, delayMaxSIMDouble));

				out.append("<tr>");
				out.append("<td>");
				if (n == 0)
					out.append(name);
				out.append("</td>");
				out.append("<td>").append(vl.getTree().getIngressNode().getName()).append("</td>");
				out.append("<td>").append(destiny).append("</td>");

				// JITTER
				out.append("<td align=\"center\">");
				if (Double.parseDouble(jitter) > AFDXParameters.MaxJitterInMsPerES)
					out.append(setErrorColor(jitter));
				else
					out.append(jitter);
				out.append("</td>");

				// TA
				out.append("<td align=\"center\">");
				String text = delayTA + " ("
						+ AFDXTools.df_2.format(100 * (delayTADouble - delayMaxSIMDouble) / delayMaxSIMDouble) + " %)";
				if (delayMaxSIMDouble > delayTADouble)
					out.append(setErrorColor(text));
				else if (delayMaxSIMDouble == delayTADouble)
					out.append(setExactColor(text));
				else
					out.append(text);
				out.append("</td>");

				// NC
				out.append("<td align=\"center\">");
				if (delayMaxSIMDouble > delayNCDouble)
					out.append(setErrorColor(delayNC));
				else if (delayMaxSIMDouble == delayNCDouble)
					out.append(setExactColor(delayNC));
				else
					out.append(delayNC + " ("
							+ AFDXTools.df_2.format(100 * (delayNCDouble - delayMaxSIMDouble) / delayMaxSIMDouble)
							+ " %)");
				out.append("</td>");

				// SIM
				out.append("<td align=\"center\">");
				text = delayMaxSIMDelay + " ("
						+ AFDXTools.df_2.format(100 * ((delayMaxSIMDouble - delayMeanSIMDouble) / delayMeanSIMDouble))
						+ "%)";
				out.append(text);
				out.append("</td>");

				out.append("<td align=\"center\">");
				out.append(delayMeanDelay);
				out.append("</td>");

				out.append("<td align=\"center\">");
				text = delayMinSIMDelay + " ("
						+ AFDXTools.df_2.format(100 * ((delayMinSIMDouble - delayMeanSIMDouble) / delayMinSIMDouble))
						+ "%)";
				out.append(text);
				out.append("</td>");

				// MINIMUM Delay
				out.append("<td align=\"center\">");
				text = delayMin + " (" + AFDXTools.df_2.format(100 * (1 - delayMinSIMDouble / delayMinDouble)) + " %)";
				if (delayMinSIMDouble < delayMinDouble)
					out.append(setErrorColor(text));
				else if (delayMinSIMDouble == delayMinDouble)
					out.append(setExactColor(text));
				else
					out.append(text);
				out.append("</td>");

				out.append("</tr>");

				n++;
			}
		}
		out.append("</table>");

		serie = new ArrayList<YIntervalSeries>();
		SimSerie = new YIntervalSeries("Sim");
		serie.add(SimSerie);
		TASerie = new YIntervalSeries("TA");
		serie.add(TASerie);
		MinSerie = new YIntervalSeries("Minimum");
		serie.add(MinSerie);
		NCSerie = new YIntervalSeries("NC");
		serie.add(NCSerie);
		min = 0;
		max = -1;

		ta.sort(new VLLatency(0, 0, 0, 0));
		pos = 0;
		for (VLLatency vllatency : ta) {
			int index = vllatency.getVLID();

			TASerie.add(pos, vllatency.getMean(), vllatency.getMin(), vllatency.getMax());
			min = vllatency.getMean() < min ? vllatency.getMean() : min;
			max = vllatency.getMean() > max ? vllatency.getMean() : max;

			NCSerie.add(pos, nc.get(index).getMean(), nc.get(index).getMin(), nc.get(index).getMax());
			min = nc.get(index).getMean() < min ? nc.get(index).getMean() : min;
			max = nc.get(index).getMean() > max ? nc.get(index).getMean() : max;

			MinSerie.add(pos, minimum.get(index).getMean(), minimum.get(index).getMin(), minimum.get(index).getMax());
			min = nc.get(index).getMean() < min ? nc.get(index).getMean() : min;
			max = nc.get(index).getMean() > max ? nc.get(index).getMean() : max;

			SimSerie.add(pos, sim.get(index).getMean(), sim.get(index).getMin(), sim.get(index).getMax());
			min = sim.get(index).getMin() < min ? sim.get(index).getMin() : min;
			max = sim.get(index).getMax() > max ? sim.get(index).getMax() : max;

			pos++;
		}

		filename = "Multicast_Graphs";
		graphs = new DrawGraphs();
		graphs.xAxisLabel = "Vl Paths";
		graphs.yAxisLabel = "Latency (ms)";

		if (max >= min) {
			graphs.saveGraph2jpg(filename, serie, min, max, vl_number * 5);

			String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			path = path.substring(0, path.lastIndexOf("/") + 1);
			String dir = path + filename + ".jpg";

			out.append("<img src=" + dir + " alt=\"Multicast Graphs\" style=\"width:1200px;height:500px;\">");
		}
		out.append("</body></html>");

		List<String> serieNames = new ArrayList<String>();
		serieNames.add("Mean Sim Delay");
		return out.toString();
	}

	private String setExactColor(String text) {
		return "<font color=\"green\">" + text + "</font>";
	}

	private String setErrorColor(String text) {
		return "<font color=\"red\">" + text + "</font>";
	}
}
