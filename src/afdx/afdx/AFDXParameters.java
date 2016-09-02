package afdx.afdx;

public class AFDXParameters {
	
	public static double propagationSpeedInKmPerSecond = 300.0E6;
	// public static double attributes = ;
	// public static double optionalLayerParameter = ;
	public static int UDPProtocol = 0;
	public static int TCPProtocol = 1;

	public static int ETHHeaderBytes = 24;// bytes
	public static int IPHeaderBytes = 20;// bytes
	public static int UDPHeaderBytes = 8;// bytes
	// Technology latency for Transmiters in seconds
	public static double TLTxInMs = 0.15;// ms
	// Technology latency for Receivers in seconds
	public static double TLRxInMs = 0.15;// ms
	// Technology latency for switches in seconds
	public static double TLSwInMs = 0.1;// ms
	public static double IFGBytes = 12;// bytes
	public static int MaxNumberOfVLs = 4096;
	public static int MaxNumberOfVLPerES = 128;
	public static double MaxJitterInMsPerES = 0.5;

	public static int ATT_PERVL_PACKET_ARRIVAL_TYPE_FULL = 0;
	public static int ATT_PERVL_PACKET_ARRIVAL_TYPE_EXPONENTIAL = 1;

	public static String PARAMETER_LINK_DELAY = "D";
	public static String PARAMETER_LINK_JITTER = "JITTER";
	public static String PARAMETER_LINK_TRANSMITION_JITTER = "JITTER_TX";
	public static String PARAMETER_LINK_VL_ID = "VL";
	public static String PARAMETER_LINK_MAX_SIZE = "L_MAX";
	public static String PARAMETER_LINK_QUEUE_WORST_CASE = "WORST_CASE_QUEUE";
	public static String PARAMETER_LINK_DELAY_WORST_CASE = "WORST_CASE_D";
	public static String SWITCH_NAME = "SWITCH_";

	// atributtes names
	public static String ATT_VL_ID = "VL_ID";
	public static String ATT_VL_NAME = "VL_NAME";
	public static String ATT_VL_JITTER = "VL_XX_JITTER";
	public static String ATT_VL_DST_DELAY = "VL_XX_DST_YY_DELAY";
	public static String ATT_VL_DST_DELAY_MIN = "VL_XX_DST_YY_DELAY_MIN";
	public static String ATT_VL_DST_DELAY_MEAN = "VL_XX_DST_YY_DELAY_MEAN";
	public static String ATT_VL_DST_DELAY_MAX = "VL_XX_DST_YY_DELAY_MAX";
	public static String ATT_VL_BAG_MS = "BAG";
	public static String ATT_VL_L_MIN_BYTES = "L_MIN";
	public static String ATT_VL_L_MAX_BYTES = "L_MAX";
	public static String ATT_ES_NAME = "ES";
	public static String ATT_SWITCH_ID = "SWITCH_ID";
	public static String ATT_SWITCH_LATENCY = "SWITCH_LATENCY";
	public static String ATT_SWITCH_PORTS = "SWITCH_PORTS";
	public static String ATT_SWITCH_PORT_CAPACITY = "SWITCH_PORT_CAPACITY";
	public static String ATT_REGULATOR_QUEUE_SIZE = "REGULATOR_QUEUE_SIZE";
	public static String ATT_PERVL_PACKET_ARRIVAL_TYPE = "PER_VL_PACKET_ARRIVAL_TYPE";
	public static String ATT_SIM_ROUTE_COUNTER = "ROUTE_COUNTER";
	public static String ATT_SIM_TREE_COUNTER = "TREE_COUNTER";
	public static String ATT_SIM_TREE_DESTINATION_COUNTER = "TREE_DESTINATION_COUNTER";
	public static String ATT_SIM_TIME = "SIMTIME";

	// csv fields
	public static String CSV_ID_FIELD = "id";
	public static String CSV_NAME_FIELD = "name";
	public static String CSV_LINKS_FIELD = "links";
	public static String CSV_X_POS_FIELD = "x";
	public static String CSV_Y_POS_FIELD = "y";
	public static String CSV_LINK_CAPACITY_FIELD = "link_capacity";
	public static String CSV_LINK_DIRECTION_FIELD = "link_direction";
	public static String CSV_PORTS_FIELD = "ports";
	public static String CSV_BAG_FIELD = "bag";
	public static String CSV_L_MIN_FIELD = "l_min";
	public static String CSV_L_MAX_FIELD = "l_max";
	public static String CSV_SRC_FIELD = "src";
	public static String CSV_DSTS_FIELD = "dsts";
	public static String CSV_ROUTES_FIELD = "routes";
	public static String CSV_TYPE_FIELD = "type";
	public static String CSV_PROTOCOL = "protocol";
	public static String CSV_SUB_VL_NUMBER_FIELD = "sub_vl_number";
	public static String CSV_SKEW_MAX_FIELD = "skew_max";
	public static String CSV_NETWORK_SELECTION_FIELD = "network_selection";
	public static String CSV_REDUNDANCY_MANAGMENT_FIELD = "redundancy_managment";
	public static String CSV_INTEGRITY_TEST_FIELD = "integrity_test";
	public static String CSV_REGULATOR_QUEUE_SIZE = "regulator_queue_size";
	public static String CSV_PERVL_PACKET_ARRIVAL_TYPE = "per_vl_packet_arrival_type";

	// simulator parameters
	public static String SIM_FILE_NAME = "Name of the File";
	public static String SIM_PARAM_SYNC_EVENTS = "Syncronized Events";
	public static String SIM_PARAM_DESVIATION = "Generation Desviation %";
	public static String SIM_PARAM_PRINT_ROUTE_INDEX = "Print Route index";
	public static String SIM_PARAM_PRINT_TREE_INDEX = "Print Tree index";
	public static String SIM_PARAM_MIN_LATENCY_TO_PRINT_ROUTE = "Minimum Latency to print Route";
	public static String SIM_PARAM_MIN_LATENCY_TO_PRINT_TREE = "Minimum Latency to print Tree";
	public static String SIM_PARAM_GROUPING = "Grouping algorithm (Y/N)";
}
