package afdx.simulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Route;
import com.net2plan.interfaces.simulation.IEventGenerator;
import com.net2plan.interfaces.simulation.SimEvent;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

import afdx.afdx.AFDXParameters;
import afdx.afdx.AFDXTools;
import afdx.afdx.Packet;
import afdx.afdx.VL;
import cern.jet.random.Exponential;

public class AFDXBasicSimulator extends IEventGenerator {
	int testLink = -1;
	public static final String prefix = "SIM_";

	private InputParameter eventSync = new InputParameter(AFDXParameters.SIM_PARAM_SYNC_EVENTS, (int) 1,
			"If 1 the events are generated in the same time, otherwise a random (0-1) is used to desviate the generation time.");
	private InputParameter generationDesviationx = new InputParameter(AFDXParameters.SIM_PARAM_DESVIATION, (double) 0,
			"If 1 the events are generated in the same time, otherwise a random (0-1) is used to desviate the generation time.");
	private InputParameter printRouteIndex = new InputParameter(AFDXParameters.SIM_PARAM_PRINT_ROUTE_INDEX, (double) -1,
			"");
	private InputParameter minLatencyToPrintRouteIndex = new InputParameter(
			AFDXParameters.SIM_PARAM_MIN_LATENCY_TO_PRINT_ROUTE, (double) -1, "");
	private InputParameter printTreeIndex = new InputParameter(AFDXParameters.SIM_PARAM_PRINT_TREE_INDEX, (double) -1,
			"");
	private InputParameter minLatencyToPrintTreeIndex = new InputParameter(
			AFDXParameters.SIM_PARAM_MIN_LATENCY_TO_PRINT_TREE, (double) -1, "");

	private NetPlan net2plan;
	private Map<String, String> algorithmParameters;

	// for each route, a list of pending packets to be regulated
	private List<LinkedList<Packet>> routeVLRegulatorQueue;
	private List<Double> routeNextServiceTimeVLRegulator;
	private List<SummaryStatistics> statsRoute;
	private List<Double> jitterRoute;

	// for each tree, a list of pending packets to be regulated
	private List<LinkedList<Packet>> treeVLRegulatorQueue;
	private List<Double> nextServiceTimePerVLTreeRegulator;
	private List<List<SummaryStatistics>> statsTree;
	private List<Double> jitterTree;

	private List<LinkedList<Packet>> linkQueue;
	private List<Double> linkNextServiceTime;

	private List<VL> VL_routes;
	private List<VL> VL_trees;

	long routesCounter;
	long treesCounter;
	long treeDestinationCounter;

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters() {
		return InputParameter.getInformationAllInputParameterFieldsOfObject(this);
	}

	@Override
	public String finish(StringBuilder output, double simTime) {
		System.out.println("Simulation finished");

		int index = 0;
		for (SummaryStatistics summaryStatistics : statsRoute) {
			double max = summaryStatistics.getMax();
			double mean = summaryStatistics.getMean();
			double min = summaryStatistics.getMin();

			// System.out.println(df_3.format(simTime) + "\tRoute:\t" +
			// net2plan.getRoute(index).getIngressNode().getName()
			// + "\t" + net2plan.getRoute(index).getEgressNode().getName() +
			// "\tMinimum Latency=\t"
			// + df_3.format(min) + "\tMean Latency=\t" + df_3.format(mean) +
			// "\tMax latency=\t"
			// + df_3.format(max));

			// SimTime
			String attribute = AFDXParameters.ATT_SIM_TIME;

			net2plan.setAttribute(attribute, simTime + "");

			// Route counter
			attribute = AFDXParameters.ATT_SIM_ROUTE_COUNTER;

			net2plan.setAttribute(attribute, routesCounter + "");

			// Jitter maximum
			attribute = AFDXParameters.ATT_VL_DST_JITTER.replace("XX",
					net2plan.getRoute(index).getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", net2plan.getRoute(index).getEgressNode().getName());

			net2plan.getRoute(index).setAttribute(prefix + attribute, jitterRoute.get(index) + "");

			// Latency minimum
			attribute = AFDXParameters.ATT_VL_DST_DELAY_MIN.replace("XX",
					net2plan.getRoute(index).getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", net2plan.getRoute(index).getEgressNode().getName());

			net2plan.getRoute(index).setAttribute(prefix + attribute, min + "");

			// Latency mean
			attribute = AFDXParameters.ATT_VL_DST_DELAY_MEAN.replace("XX",
					net2plan.getRoute(index).getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", net2plan.getRoute(index).getEgressNode().getName());

			net2plan.getRoute(index).setAttribute(prefix + attribute, mean + "");

			// Latency Maximum
			attribute = AFDXParameters.ATT_VL_DST_DELAY_MAX.replace("XX",
					net2plan.getRoute(index).getDemand().getAttribute(AFDXParameters.ATT_VL_ID));
			attribute = attribute.replace("YY", net2plan.getRoute(index).getEgressNode().getName());

			net2plan.getRoute(index).setAttribute(prefix + attribute, max + "");

			index++;
		}

		index = 0;
		for (List<SummaryStatistics> summaryStatisticsList : statsTree) {
			Set<Node> nodes = net2plan.getMulticastTree(index).getEgressNodes();
			Iterator<Node> i = nodes.iterator();
			for (SummaryStatistics summaryStatistics : summaryStatisticsList) {
				double max = summaryStatistics.getMax();
				double mean = summaryStatistics.getMean();
				double min = summaryStatistics.getMin();

				Node egressNode = i.next();
				// System.out.println(
				// df_3.format(simTime) + "\tTree:\t" +
				// net2plan.getMulticastTree(index).getIngressNode().getName()
				// + "\t" + egressNode.getName() + "\tMinimum Latency=\t" +
				// df_3.format(min)
				// + "\tMean Latency=\t" + df_3.format(mean) + "\tMax
				// latency=\t" + df_3.format(max));

				// Route counter
				String attribute = AFDXParameters.ATT_SIM_TREE_COUNTER;

				net2plan.setAttribute(attribute, treesCounter + "");

				attribute = AFDXParameters.ATT_SIM_TREE_DESTINATION_COUNTER;

				net2plan.setAttribute(attribute, treeDestinationCounter + "");

				// Jitter maximum
				attribute = AFDXParameters.ATT_VL_DST_JITTER.replace("XX",
						net2plan.getMulticastTree(index).getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", egressNode.getName());

				net2plan.getMulticastTree(index).setAttribute(prefix + attribute, jitterTree.get(index) + "");

				// Latency minimum
				attribute = AFDXParameters.ATT_VL_DST_DELAY_MIN.replace("XX",
						net2plan.getMulticastTree(index).getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", egressNode.getName());

				net2plan.getMulticastTree(index).setAttribute(prefix + attribute, min + "");

				// Latency mean
				attribute = AFDXParameters.ATT_VL_DST_DELAY_MEAN.replace("XX",
						net2plan.getMulticastTree(index).getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", egressNode.getName());

				net2plan.getMulticastTree(index).setAttribute(prefix + attribute, mean + "");

				// Latency maximum
				attribute = AFDXParameters.ATT_VL_DST_DELAY_MAX.replace("XX",
						net2plan.getMulticastTree(index).getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_ID));
				attribute = attribute.replace("YY", egressNode.getName());

				net2plan.getMulticastTree(index).setAttribute(prefix + attribute, max + "");
			}
			index++;
		}

		return super.finish(output, simTime);
	}

	@Override
	public void initialize(NetPlan currentNetPlan, Map<String, String> algorithmParameters,
			Map<String, String> simulationParameters, Map<String, String> net2planParameters) {
		routesCounter = 0;
		treesCounter = 0;
		treeDestinationCounter = 0;

		this.net2plan = currentNetPlan;
		this.algorithmParameters = algorithmParameters;

		this.VL_routes = new ArrayList<VL>();

		// VL source queues
		this.routeVLRegulatorQueue = new ArrayList<LinkedList<Packet>>();
		for (Route r : currentNetPlan.getRoutes())
			routeVLRegulatorQueue.add(new LinkedList<Packet>());
		this.jitterRoute = new ArrayList<Double>();
		this.routeNextServiceTimeVLRegulator = new ArrayList<Double>();
		statsRoute = new ArrayList<SummaryStatistics>();

		// links queues
		this.linkQueue = new ArrayList<LinkedList<Packet>>();
		linkNextServiceTime = new ArrayList<Double>();
		for (Link l : currentNetPlan.getLinks()) {
			linkQueue.add(new LinkedList<Packet>());
			linkNextServiceTime.add(new Double(0));
		}

		// for each route
		for (int routeIndex = 0; routeIndex < currentNetPlan.getNumberOfRoutes(); routeIndex++) {
			final Route route = currentNetPlan.getRoute(routeIndex);

			// simulates the configuration table loading
			VL vl = new VL(route);
			VL_routes.add(vl);

			/* generate event for myself to produce next unregulated packet */
			double delta = Math.random();
			if (algorithmParameters.get(AFDXParameters.SIM_PARAM_SYNC_EVENTS).equals("1"))
				delta = 0;
			double generationTime = delta / 1000;
			Packet packet = new Packet(vl, route.getEgressNode(), generationTime);
			this.scheduleEvent(new SimEvent(generationTime, SimEvent.DestinationModule.EVENT_GENERATOR, 0, packet));

			// reset the next service time for VL at regulators
			jitterRoute.add(new Double(0));
			routeNextServiceTimeVLRegulator.add(new Double(0));
			statsRoute.add(new SummaryStatistics());
		}

		this.VL_trees = new ArrayList<VL>();

		// VL source queues
		this.treeVLRegulatorQueue = new ArrayList<LinkedList<Packet>>();
		for (MulticastTree t : currentNetPlan.getMulticastTrees())
			treeVLRegulatorQueue.add(new LinkedList<Packet>());
		this.jitterTree = new ArrayList<Double>();
		this.nextServiceTimePerVLTreeRegulator = new ArrayList<Double>();
		statsTree = new ArrayList<>();

		// for each tree
		for (int treeIndex = 0; treeIndex < currentNetPlan.getNumberOfMulticastTrees(); treeIndex++) {
			final MulticastTree tree = currentNetPlan.getMulticastTree(treeIndex);

			// simulates the configuration table loading
			VL vl = new VL(tree);
			VL_trees.add(vl);

			/* generate event for myself to produce next unregulated packet */
			double delta = Math.random();
			if (algorithmParameters.get(AFDXParameters.SIM_PARAM_SYNC_EVENTS).equals("1"))
				delta = 0;
			double generationTime = delta / 1000;
			Packet packet = new Packet(vl, null, generationTime);
			this.scheduleEvent(new SimEvent(generationTime, SimEvent.DestinationModule.EVENT_GENERATOR, 0, packet));

			// reset the next service time for VL at regulators
			jitterTree.add(new Double(0));
			nextServiceTimePerVLTreeRegulator.add(new Double(0));
			statsTree.add(new ArrayList<SummaryStatistics>());
			for (Node n : tree.getEgressNodes()) {
				statsTree.get(treeIndex).add(new SummaryStatistics());
			}
		}
	}

	@Override
	public void processEvent(NetPlan net2plan, SimEvent simEvent) {
		final double simTime = simEvent.getEventTime();

		Packet packet = (Packet) simEvent.getEventObject();

		if (packet.getVl().getTree() == null) {
			// process a route packet
			processRoutePacket(packet, simTime);
		} else {
			// process a tree packet
			processTreePacket(packet, simTime);
		}

	}

	private void processRoutePacket(Packet packet, double simTime) {
		int crossedNodes = packet.getArrivalNodes().size();
		Route route = packet.getVl().getRoute();
		int routeIndex = packet.getVl().getRoute().getIndex();

		if (!packet.regulated) {
			// new packet
			routeVLRegulatorQueue.get(routeIndex).add(packet);
			double nextRegulatorServiceTime = (double) routeNextServiceTimeVLRegulator.get(routeIndex);

			double timeLeavingRegulator = 0;
			if (simTime < nextRegulatorServiceTime) {
				// the packet has to wait in the regulator
				timeLeavingRegulator = nextRegulatorServiceTime;
				nextRegulatorServiceTime += packet.getVl().getBagMs() / 1000;
			} else {
				// the packet can be sent to the multiplexer
				timeLeavingRegulator = simTime;
				nextRegulatorServiceTime = simTime + packet.getVl().getBagMs() / 1000;
			}

			// event for the packet leaving the regulator
			packet.regulated = true;
			this.scheduleEvent(
					new SimEvent(timeLeavingRegulator, SimEvent.DestinationModule.EVENT_GENERATOR, 0, packet));

			routeNextServiceTimeVLRegulator.set(routeIndex, nextRegulatorServiceTime);

			// generation next packet of this VL
			double desviation = Math.random() > 0.5 ? 1 : -1;
			desviation *= packet.getVl().getBagMs() / 1000
					* Double.parseDouble(algorithmParameters.get(AFDXParameters.SIM_PARAM_DESVIATION));

			double timeNextPacket = simTime + sampleInterArrivalTime(packet.getVl()) + desviation;

			Packet newPacket = new Packet(packet.getVl(), packet.getEgressNode(), timeNextPacket);

			this.scheduleEvent(new SimEvent(timeNextPacket, SimEvent.DestinationModule.EVENT_GENERATOR, 0, newPacket));
		} else {
			Node arrivalNode;
			if (packet.getPreviousLink() == null)
				arrivalNode = route.getIngressNode();
			else
				arrivalNode = packet.getPreviousLink().getDestinationNode();

			if (packet.isTechnologyLatencyWaiting()) {
				packet.getArrivalNodes().add(arrivalNode);
				packet.getArrivalTimesToNodes().add(simTime * 1000);

				if (packet.getPreviousLink() == null) {
					int pos = routeVLRegulatorQueue.get(routeIndex).indexOf(packet);

					if (pos != 0)
						throw new RuntimeException("route: The packer is not the first, pos=" + pos);

					routeVLRegulatorQueue.get(routeIndex).remove(pos);
				} else {
					int pos = linkQueue.get(packet.getPreviousLink().getIndex()).indexOf(packet);

					if (pos != 0)
						throw new RuntimeException("route: The packer is not the first, pos=" + pos);

					linkQueue.get(packet.getPreviousLink().getIndex()).remove(pos);
					if (packet.getPreviousLink().getIndex() == testLink)
						printLinkStatus(simTime, packet);
				}

				double TechnologyLatency = 0;

				if (packet.getPreviousLink() == null)
					TechnologyLatency = AFDXParameters.TLTxInMs / 1000;
				else if (crossedNodes == route.getNumberOfHops())
					TechnologyLatency = AFDXParameters.TLRxInMs / 1000;
				else
					TechnologyLatency = AFDXParameters.TLSwInMs / 1000;

				packet.setTechnologyLatencyWaiting(false);

				this.scheduleEvent(new SimEvent(simTime + TechnologyLatency, SimEvent.DestinationModule.EVENT_GENERATOR,
						0, packet));
			} else {
				if (crossedNodes <= route.getNumberOfHops()) {
					Link link = route.getInitialSequenceOfLinks().get(crossedNodes - 1);
					sendPacketThroughLink(simTime, packet, link);
				} else {
					// packet arrives destination, perform calculations
					packet.setLeavingNetworkTime(simTime * 1000);

					double packetLatency = packet.getLeavingNetworkTime() - packet.getArrivalTimesToNodes().get(0);

					String routesString = algorithmParameters.get(AFDXParameters.SIM_PARAM_PRINT_ROUTE_INDEX);
					String routes[] = routesString.split(",");
					for (String string : routes) {
						if (string.equals(route.getIndex() + ""))
							if (packetLatency > Double.parseDouble(
									algorithmParameters.get(AFDXParameters.SIM_PARAM_MIN_LATENCY_TO_PRINT_ROUTE)))
								packet.print();
					}

					routesCounter++;
					statsRoute.get(routeIndex).addValue(packetLatency);
				}
			}
		}
	}

	private void processTreePacket(Packet packet, double simTime) {
		int crossedNodes = packet.getArrivalNodes().size();
		MulticastTree tree = packet.getVl().getTree();
		int treeIndex = packet.getVl().getTree().getIndex();

		if (!packet.regulated) {
			// new packet
			treesCounter++;
			treeVLRegulatorQueue.get(treeIndex).add(packet);
			double nextRegulatorServiceTime = (double) nextServiceTimePerVLTreeRegulator.get(treeIndex);

			double timeLeavingRegulator;
			if (simTime < nextRegulatorServiceTime) {
				// the packet has to wait in the regulator

				timeLeavingRegulator = nextRegulatorServiceTime;
				nextRegulatorServiceTime += packet.getVl().getBagMs() / 1000;
			} else {
				// the packet can be sent to the multiplexer

				timeLeavingRegulator = simTime;
				nextRegulatorServiceTime = simTime + packet.getVl().getBagMs() / 1000;
			}

			// event for the packet leaving the regulator
			packet.regulated = true;
			this.scheduleEvent(
					new SimEvent(timeLeavingRegulator, SimEvent.DestinationModule.EVENT_GENERATOR, 0, packet));

			nextServiceTimePerVLTreeRegulator.set(treeIndex, nextRegulatorServiceTime);

			// generation next packet of this VL
			double desviation = Math.random() > 0.5 ? 1 : -1;
			desviation *= packet.getVl().getBagMs() / 1000
					* Double.parseDouble(algorithmParameters.get(AFDXParameters.SIM_PARAM_DESVIATION));

			double timeNextPacket = simTime + sampleInterArrivalTime(packet.getVl()) + desviation;

			Packet newPacket = new Packet(packet.getVl(), packet.getEgressNode(), timeNextPacket);

			this.scheduleEvent(new SimEvent(timeNextPacket, SimEvent.DestinationModule.EVENT_GENERATOR, 0, newPacket));
		} else {
			Node arrivalNode;
			if (packet.getPreviousLink() == null)
				arrivalNode = tree.getIngressNode();
			else
				arrivalNode = packet.getPreviousLink().getDestinationNode();

			if (packet.isTechnologyLatencyWaiting()) {
				packet.getArrivalNodes().add(arrivalNode);
				packet.getArrivalTimesToNodes().add(simTime * 1000);

				if (packet.getPreviousLink() == null) {
					int pos = treeVLRegulatorQueue.get(treeIndex).indexOf(packet);

					if (pos != 0)
						throw new RuntimeException("tree: The packet is not the first, pos=" + pos);

					treeVLRegulatorQueue.get(treeIndex).remove(pos);
				} else {
					int pos = linkQueue.get(packet.getPreviousLink().getIndex()).indexOf(packet);

					if (pos != 0)
						throw new RuntimeException("tree: The packet is not the first, pos=" + pos);

					linkQueue.get(packet.getPreviousLink().getIndex()).remove(pos);
					if (packet.getPreviousLink().getIndex() == testLink)
						printLinkStatus(simTime, packet);
				}

				double TechnologyLatency = 0;

				Set<Link> outputLinks = tree.getOutputLinkOfNode(arrivalNode);
				if (packet.getPreviousLink() == null)
					TechnologyLatency = AFDXParameters.TLTxInMs / 1000;
				else if (outputLinks.size() > 0)
					TechnologyLatency = AFDXParameters.TLRxInMs / 1000;
				else
					TechnologyLatency = AFDXParameters.TLSwInMs / 1000;

				packet.setTechnologyLatencyWaiting(false);

				this.scheduleEvent(new SimEvent(simTime + TechnologyLatency, SimEvent.DestinationModule.EVENT_GENERATOR,
						0, packet));
			} else {
				if (crossedNodes == 1)
					arrivalNode = tree.getIngressNode();
				else
					arrivalNode = packet.getPreviousLink().getDestinationNode();

				Set<Link> outputLinks = tree.getOutputLinkOfNode(arrivalNode);
				if (outputLinks.size() > 0) {
					for (Link outputLink : outputLinks)
						sendPacketThroughLink(simTime, packet, outputLink);
				} else {
					// packet arrives destination, perform calculations
					packet.setLeavingNetworkTime(simTime * 1000);

					double packetLatency = packet.getLeavingNetworkTime() - packet.getArrivalTimesToNodes().get(0);

					int egressNodeindex = 0;
					for (Node node2 : tree.getEgressNodes()) {
						if (node2 == arrivalNode)
							break;
						egressNodeindex++;

					}

					String treesString = algorithmParameters.get(AFDXParameters.SIM_PARAM_PRINT_TREE_INDEX);
					String trees[] = treesString.split(",");
					for (String string : trees) {
						if (string.equals(tree.getIndex() + ""))
							if (packetLatency > Double.parseDouble(
									algorithmParameters.get(AFDXParameters.SIM_PARAM_MIN_LATENCY_TO_PRINT_ROUTE)))
								packet.print();
					}

					treeDestinationCounter++;
					statsTree.get(treeIndex).get(egressNodeindex).addValue(packetLatency);
				}
			}
		}
	}

	private void sendPacketThroughLink(double simTime, Packet packet, Link link) {
		Packet packetCopy = packet.copy();

		if (packetCopy.getArrivalNodes().size() == 1) {
			double jitter;
			if (simTime < linkNextServiceTime.get(link.getIndex()))
				jitter = 1000 * linkNextServiceTime.get(link.getIndex()) - packetCopy.getArrivalTimesToNodes().get(0)
						- AFDXParameters.TLTxInMs;
			else
				jitter = 1000 * simTime - packetCopy.getArrivalTimesToNodes().get(0) - AFDXParameters.TLTxInMs;
			packetCopy.setLastJitter(jitter);

			if (packetCopy.getVl().getRoute() != null) {
				if (jitter > jitterRoute.get(packetCopy.getVl().getRoute().getIndex()))
					jitterRoute.set(packetCopy.getVl().getRoute().getIndex(), jitter);
			} else {
				if (jitter > jitterTree.get(packetCopy.getVl().getTree().getIndex()))
					jitterTree.set(packetCopy.getVl().getTree().getIndex(), jitter);
			}
		}

		packetCopy.setTechnologyLatencyWaiting(true);

		packetCopy.setPreviousLink(link);

		linkQueue.get(link.getIndex()).add(packetCopy);
		double linkTxTime = linkTxTime(packetCopy, link);

		if (simTime < linkNextServiceTime.get(link.getIndex()))
			linkTxTime += linkNextServiceTime.get(link.getIndex());
		else
			linkTxTime += simTime;

		this.scheduleEvent(new SimEvent(linkTxTime, SimEvent.DestinationModule.EVENT_GENERATOR, 0, packetCopy));

		linkNextServiceTime.set(link.getIndex(), (linkTxTime + IFGServiceTime(link)));

		try {
			if (link.getIndex() == testLink) {
				printLinkStatus(simTime, packetCopy, link);
			}
		} catch (Exception e) {
			printLinkStatus(simTime, packetCopy, link);
		}
	}

	private double sampleInterArrivalTime(VL vl) {
		double time;
		if (vl.getArrivalType() == AFDXParameters.ATT_PERVL_PACKET_ARRIVAL_TYPE_FULL)
			time = vl.getBagMs() / 1000;
		else if (vl.getArrivalType() == AFDXParameters.ATT_PERVL_PACKET_ARRIVAL_TYPE_EXPONENTIAL) {
			time = Exponential.staticNextDouble(1 / vl.getBagMs() / 1000);
			time = vl.getBagMs() / 1000;
		} else
			throw new RuntimeException("Bad");

		return time;
	}

	private double linkTxTime(Packet packet, Link link) {
		// if (link.getIndex() == 31)
		// System.out.println("link queue " +
		// linkQueue.get(link.getIndex()).size());

		double serviceTime = 0;

		if (packet.getProtocol() == AFDXParameters.UDPProtocol) {
			serviceTime += 8 * (AFDXParameters.ETHHeaderBytes + packet.getVl().getLmaxIPPacket()) / link.getCapacity();
		} else
			throw new RuntimeException("Bad");

		return serviceTime;
	}

	private double IFGServiceTime(Link link) {
		double serviceTime = 0;

		serviceTime = 8 * AFDXParameters.IFGBytes / link.getCapacity();

		return serviceTime;
	}

	private void printLinkStatus(double simTime, Packet packet) {
		if (packet.getVl().getRoute() != null)
			System.out.println("\t" + AFDXTools.df_5.format(simTime * 1000) + " -------->link queue "
					+ linkQueue.get(packet.getPreviousLink().getIndex()).size() + " Route "
					+ packet.getVl().getRoute().getIndex() + " nextservicetime "
					+ AFDXTools.df_5.format(linkNextServiceTime.get(packet.getPreviousLink().getIndex()) * 1000));
		else
			System.out.println("\t" + AFDXTools.df_5.format(simTime * 1000) + " -------->link queue "
					+ linkQueue.get(packet.getPreviousLink().getIndex()).size() + " Tree "
					+ packet.getVl().getTree().getIndex() + " nextservicetime "
					+ AFDXTools.df_5.format(linkNextServiceTime.get(packet.getPreviousLink().getIndex()) * 1000));

	}

	private void printLinkStatus(double simTime, Packet packet, Link link) {
		if (packet.getVl().getRoute() != null)
			System.out.println(AFDXTools.df_5.format(simTime * 1000) + "--------> link queue "
					+ linkQueue.get(link.getIndex()).size() + " Route " + packet.getVl().getRoute().getIndex()
					+ " nextservicetime " + AFDXTools.df_5.format(linkNextServiceTime.get(link.getIndex()) * 1000));
		else
			System.out.println(AFDXTools.df_5.format(simTime * 1000) + "--------> link queue "
					+ linkQueue.get(link.getIndex()).size() + " Tree " + packet.getVl().getTree().getIndex()
					+ " nextservicetime " + AFDXTools.df_5.format(linkNextServiceTime.get(link.getIndex()) * 1000));

	}

}
