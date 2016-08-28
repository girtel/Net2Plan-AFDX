package afdx.vl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.Triple;

public class SRG implements IAlgorithm {

	@Override
	public String executeAlgorithm(NetPlan netPlan,
			Map<String, String> algorithmParameters,
			Map<String, String> net2planParameters) {

		//set SGR by switch with name switch_x
		Set<Link> link;

		return null;
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
