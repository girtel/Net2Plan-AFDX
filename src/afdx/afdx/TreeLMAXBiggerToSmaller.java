package afdx.afdx;

import java.util.Comparator;

import com.net2plan.interfaces.networkDesign.MulticastTree;

public class TreeLMAXBiggerToSmaller implements Comparator<MulticastTree> {

	@Override
	public int compare(MulticastTree t1, MulticastTree t2) {
		return Integer.parseInt(t2.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES))
				- Integer.parseInt(t1.getMulticastDemand().getAttribute(AFDXParameters.ATT_VL_L_MAX_BYTES));
	}
}
