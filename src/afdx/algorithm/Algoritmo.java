package afdx.algorithm;

import java.util.List;
import java.util.Map;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.Triple;

public class Algoritmo implements IAlgorithm {

	@Override
	public String executeAlgorithm(NetPlan n2p, Map<String, String> arg1,
			Map<String, String> arg2) {
		System.out.println("Algotritmo");
		return null;

/*		Set<Long> VLs = n2p.getDemandIds();
		List<Long> list = new ArrayList<Long>(VLs);

		Set<Long> routes = n2p.getRouteIds();

		Set<String> VLqeueuWorstCase;

		for (long i = 0; i < VLs.size(); i++) {
			VLqeueuWorstCase = new HashSet<String>();

			long VLId = list.get((int) i);
			System.out.println("VL: " + VLId);

			Set<Long> demandRoute = n2p.getDemandRoutes(VLId);
			List<Long> demandRouteList = new ArrayList<Long>(demandRoute);
			List<Long> demandRouteNodes = n2p
					.getRouteSequenceOfNodes(demandRouteList.get(0));

			System.out
					.println("VL Number of Nodes: " + demandRouteNodes.size());

			for (long j = 1; j < demandRouteNodes.size(); j++) {
				if (demandRouteNodes.size() > 2
						&& j < demandRouteNodes.size() - 1) {
					long node = demandRouteNodes.get((int) j);
					long nextNode = demandRouteNodes.get((int) j + 1);
					System.out.println("\tVL link(nodes): " + node + "->"
							+ nextNode);
					String linkString = ":" + node + ":" + nextNode + ":";

					for (Long route : routes) {
						if (VLId == n2p.getRouteDemand(route))
							continue;
						System.out.println("\t\tRoute: " + route);
						List<Long> routeVector = n2p
								.getRouteSequenceOfNodes(route);
						String routeVectorString = getRouteVectorString(routeVector);
						System.out.println("\t\t\tlinkString: " + linkString);
						System.out.println("\t\t\trouteVectorString: "
								+ routeVectorString);

						String VLAtribute1 = n2p.getDemandAttribute(
								n2p.getRouteDemand(route), "VL");
						String VLAtribute = n2p.getDemandAttribute(VLId, "VL");
						if (!VLAtribute.equals(VLAtribute1)
								&& routeVectorString.contains(linkString)) {
							System.out.println("\t VL conflict: "
									+ n2p.getDemandAttribute(
											n2p.getRouteDemand(route), "VL"));
							VLqeueuWorstCase.add(n2p.getDemandAttribute(
									n2p.getRouteDemand(route), "VL"));
						}
					}
				}
			}

			setVLqeueuWorstCase(n2p, VLId, VLqeueuWorstCase);
			setVLLatencyWorstCase(n2p, VLId, VLqeueuWorstCase);

			System.out.println("");
			System.out.println("******");
			System.out.println("");
		}

		return null;
	}

	public String getRouteVectorString(List<Long> demandRoute) {
		String result = ":";

		for (int i = 0; i < demandRoute.size(); i++) {
			result = result + demandRoute.get(i) + ":";
		}

		return result;
	}

	public void setVLqeueuWorstCase(NetPlan n2p, long VLId,
			Set<String> VLqeueuWorstCase) {
		List<String> VLqeueuWorstCaseList = new ArrayList<String>(
				VLqeueuWorstCase);

		String VLqeueuWorstCaseString = ":";
		for (String string : VLqeueuWorstCaseList) {
			VLqeueuWorstCaseString = VLqeueuWorstCaseString + string + ":";
		}

		n2p.setDemandAttribute(VLId,
				AFDXParameters.PARAMETER_LINK_QUEUE_WORST_CASE,
				VLqeueuWorstCaseString);
	}

	public void setVLLatencyWorstCase(NetPlan n2p, long VLId,
			Set<String> VLqeueuWorstCase) {

		n2p.setDemandAttribute(
				VLId,
				AFDXParameters.PARAMETER_LINK_DELAY_WORST_CASE,
				""
						+ ((double) ((int) (calculateLatencyWorstCase(n2p, VLId) * 1.0E4)) / 1.0E4));
	}

	public double calculateLatencyWorstCase(NetPlan n2p, long VLId) {
		double result = -1;// ms

		double M = Double.parseDouble(n2p.getDemandAttribute(VLId,
				AFDXParameters.PARAMETER_LINK_MAX_SIZE))
				+ AFDXParameters.UDPHeader;// Bytes

		double Tmin_gap = 96 / AFDXParameters.linkCapacity;// s

		Set<Long> demandRoute = n2p.getDemandRoutes(VLId);
		List<Long> demandRouteList = new ArrayList<Long>(demandRoute);
		List<Long> demandRouteNodes = n2p
				.getRouteSequenceOfNodes(demandRouteList.get(0));

		System.out.println("M " + M);
		System.out.println("IFG " + Tmin_gap);
		System.out.println("Nbw " + AFDXParameters.linkCapacity);
		System.out
				.println("demandRouteNodes.size() " + demandRouteNodes.size());

		// Latency La = Ts + Tm1 + Tsw + ( 8 x M )/Nbw + Tm2 + Tr
		result = AFDXParameters.TLs + AFDXParameters.TLsw + (8 * M)
				/ AFDXParameters.linkCapacity + (demandRouteNodes.size() - 1)
				* (M / AFDXParameters.linkCapacity) + AFDXParameters.TLs;

		System.out.println("Ts (ms) " + AFDXParameters.TLs * 1.0E3);
		System.out.println("Tsw (ms) " + AFDXParameters.TLsw * 1.0E3);
		System.out.println("Tr (ms) " + AFDXParameters.TLr * 1.0E3);
		System.out.println("(8 * M) / Nbw (ms) " + (8 * M)
				/ AFDXParameters.linkCapacity * 1.0E3);
		System.out.println("(demandRouteNodes.size() - 1)* (M / Nbw) (ms) "
				+ (demandRouteNodes.size() - 1)
				* (M / AFDXParameters.linkCapacity) * 1.0E3);
		System.out.println("D (ms) " + result * 1.0E3);
		n2p.setDemandAttribute(VLId, AFDXParameters.PARAMETER_LINK_DELAY, ""
				+ ((double) ((int) (result * 1.0E7)) / 1.0E4));

		// Jitter Tj = (8 x M) / Nbw + Tmin_gap
		double Tj = 0;

		String VLqueueWorstCaseString = n2p.getDemandAttribute(VLId,
				AFDXParameters.PARAMETER_LINK_QUEUE_WORST_CASE);
		String VLqueueWorstCase[] = VLqueueWorstCaseString.split(":");
		for (String string : VLqueueWorstCase) {
			try {
				double M_length = Long.parseLong(n2p.getDemandAttribute(
						Long.parseLong(string),
						AFDXParameters.PARAMETER_LINK_MAX_SIZE));
				Tj = Tj + (8 * M_length) / AFDXParameters.linkCapacity
						+ Tmin_gap;
			} catch (Exception e) {
				System.out.println("Error parsing Long");
			}
		}

		n2p.setDemandAttribute(VLId, AFDXParameters.PARAMETER_LINK_JITTER, ""
				+ ((double) ((int) (Tj * 1.0E7)) / 1.0E4));

		System.out.println("Tj (ms) " + Tj * 1.0E3);

		return (result + Tj) * 1.0E3;*/
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters() {
		return null;
	}

}
