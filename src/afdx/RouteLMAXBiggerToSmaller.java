package afdx;

import java.util.Comparator;

import com.net2plan.interfaces.networkDesign.Route;

public class RouteLMAXBiggerToSmaller implements Comparator<Route> {

	@Override
	public int compare(Route r1, Route r2) {
		return Integer.parseInt(r2.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES))
				- Integer.parseInt(r1.getDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
	}
}
