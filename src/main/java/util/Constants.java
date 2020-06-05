package util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Constants
{

    public static final String ALL_RESULTS_FILE = "RESULTS_SUMMARY.csv";

    public static Map<String,Double> CHANGE_FACTORS = new HashMap<>();
    
	//penalties given to changes made to css properties, used to compute distortion part of fitness function 
    public static Map<String,Double> CHANGE_PENALTY = new HashMap<>();

	public static Map<String,Integer> TIME_OUTS_SEC = new HashMap<>();

	// shorthands
	public static final String PADDING_LEFT_RIGHT = "padding-left-right";
	public static final String PADDING_TOP_BOTTOM = "padding-top-bottom";
	public static final String MARGIN_LEFT_RIGHT = "margin-left-right";
	public static final String MARGIN_TOP_BOTTOM = "margin-top-bottom";
	public static final List<String> SHORTHAND_CSS_PROPERTIES = Arrays.asList(PADDING_LEFT_RIGHT, PADDING_TOP_BOTTOM, MARGIN_LEFT_RIGHT, MARGIN_TOP_BOTTOM);
	public static final Map<String, List<String>> ACTUAL_CSS_PROPERTIES_FOR_SHORTHANDS = new HashMap<>();
	
	
	public static final double POWER_FACTOR_AESTHETIC = 1; // this should be 2 in case of polynomial fitness function

	static{
		CHANGE_FACTORS.put("font-size", 1.0);
		CHANGE_FACTORS.put("width", 4.0);
		CHANGE_FACTORS.put("height", 4.0);
		CHANGE_FACTORS.put("padding-left", 1.0);
		CHANGE_FACTORS.put("padding-right", 1.0);
		CHANGE_FACTORS.put("padding-top", 1.0);
		CHANGE_FACTORS.put("padding-bottom", 1.0);
		CHANGE_FACTORS.put("margin-left", 1.0);
		CHANGE_FACTORS.put("margin-right", 1.0);
		CHANGE_FACTORS.put("margin-top", 1.0);
		CHANGE_FACTORS.put("margin-bottom", 1.0);
		CHANGE_FACTORS.put(PADDING_LEFT_RIGHT, 1.0);
		CHANGE_FACTORS.put(PADDING_TOP_BOTTOM, 1.0);
		CHANGE_FACTORS.put(MARGIN_LEFT_RIGHT, 1.0);
		CHANGE_FACTORS.put(MARGIN_TOP_BOTTOM, 1.0);
		
		CHANGE_PENALTY.put("font-size", 4.0);
		CHANGE_PENALTY.put("width", 1.0);
		CHANGE_PENALTY.put("height", 1.0);
		CHANGE_PENALTY.put("padding-left", 0.1);
		CHANGE_PENALTY.put("padding-right", 0.1);
		CHANGE_PENALTY.put("padding-top", 0.1);
		CHANGE_PENALTY.put("padding-bottom", 0.1);
		CHANGE_PENALTY.put("margin-left", 0.1);
		CHANGE_PENALTY.put("margin-right", 0.1);
		CHANGE_PENALTY.put("margin-top", 0.1);
		CHANGE_PENALTY.put("margin-bottom", 0.1);
		CHANGE_PENALTY.put(PADDING_LEFT_RIGHT, 0.1);
		CHANGE_PENALTY.put(PADDING_TOP_BOTTOM, 0.1);
		CHANGE_PENALTY.put(MARGIN_LEFT_RIGHT, 0.1);
		CHANGE_PENALTY.put(MARGIN_TOP_BOTTOM, 0.1);

		ACTUAL_CSS_PROPERTIES_FOR_SHORTHANDS.put(PADDING_LEFT_RIGHT, Arrays.asList("padding-left", "padding-right"));
		ACTUAL_CSS_PROPERTIES_FOR_SHORTHANDS.put(PADDING_TOP_BOTTOM, Arrays.asList("padding-top", "padding-bottom"));
		ACTUAL_CSS_PROPERTIES_FOR_SHORTHANDS.put(MARGIN_LEFT_RIGHT, Arrays.asList("margin-left", "margin-right"));
		ACTUAL_CSS_PROPERTIES_FOR_SHORTHANDS.put(MARGIN_TOP_BOTTOM, Arrays.asList("margin-top", "margin-bottom"));
		
		TIME_OUTS_SEC.put("hotwire", 292);
		TIME_OUTS_SEC.put("westin", 140);
		TIME_OUTS_SEC.put("hightail", 73);
		TIME_OUTS_SEC.put("akamai", 153);
		TIME_OUTS_SEC.put("caLottery", 80);
		TIME_OUTS_SEC.put("designSponge", 1032);
		TIME_OUTS_SEC.put("dmv", 488);
		TIME_OUTS_SEC.put("els", 116);
		TIME_OUTS_SEC.put("facebookLogin", 339);
		TIME_OUTS_SEC.put("flynas", 333);
		TIME_OUTS_SEC.put("googleEarth", 144);
		TIME_OUTS_SEC.put("googleLogin", 84);
		TIME_OUTS_SEC.put("ixigo", 621);
		TIME_OUTS_SEC.put("linkedin", 100);
		TIME_OUTS_SEC.put("museum", 354);
		TIME_OUTS_SEC.put("mplay", 896);
		TIME_OUTS_SEC.put("qualitrol", 105);
		TIME_OUTS_SEC.put("rentalCars", 504);
		TIME_OUTS_SEC.put("skype", 109);
		TIME_OUTS_SEC.put("skyScanner", 86);
		TIME_OUTS_SEC.put("doctor", 82);
		TIME_OUTS_SEC.put("twitterHelp", 75);
		TIME_OUTS_SEC.put("worldsBest", 276);
	}
	
	
	public static final boolean RUN_IN_DEBUG_MODE = true;
	public static final List<String> CSS_PROPERTIES_MASTER_LIST = Arrays.asList(PADDING_LEFT_RIGHT, MARGIN_LEFT_RIGHT, "width", "font-size", "height", PADDING_TOP_BOTTOM, MARGIN_TOP_BOTTOM);
	public static final int ELEMENT_SIZE_THRESHOLD = 5;
	public static final String CLUSTER_PREFIX = "C";
	
	// APPROACH 1
	//public static final double DEGREE_OF_CHANGE_MIN = -1.0;
	//public static final double DEGREE_OF_CHANGE_MAX = 1.0;
	public static double FITNESS_FUNCTION_WEIGHT_STRUCTURE = 1.0;
	public static double FITNESS_FUNCTION_WEIGHT_AESTHETIC = 0.0;
	
	public static boolean CROSSOVER_WEIGHTED_AVERAGE = false;
	public static boolean CROSSOVER_SWAP = true;
	
	public static final int POPULATION_SIZE_APPROACH1_PHASE1 = 100;
	public static final int MAX_GENERATIONS_APPROACH1_PHASE1 = 20;
	public static final int SATURATION_POINT_APPROACH1_PHASE1 = 2;
	public static final double CROSSOVER_RATE_PHASE1 = 0.0;
	public static final double MUTATION_RATE_PHASE1 = 1.0;
	public static final double AVM_RATE_PHASE1 = 0.01;
	
	public static final int POPULATION_SIZE_APPROACH1_PHASE2 = 2;
	public static final int MAX_GENERATIONS_APPROACH1_PHASE2 = 2;
	public static final int SATURATION_POINT_APPROACH1_PHASE2 = 1;
	
	public static final int MAX_ITERATIONS_APPROACH1 = 1;
	public static final int SATURATION_POINT_APPROACH1 = 5;
	
	// AVM search
    public static final int[] EXPLORATORY_MOVES_ARR = {-1, 1};
    public static final int PATTERN_BASE = 2;
	public static final boolean ISOLATE_AVM = true;
	
	//mutation will be performed with a range of increasing or decreasing the element to this factor; 
	public static final double MUTATION_MIN_MAX_FACTOR = 0.5;

	public static final boolean IS_RANDOM_SEARCH = false;




	public static void printConfig()
	{
		Constants c = new Constants();
		System.out.println(c + "\n");
	}
	
	@Override
	public String toString()
	{
		return "Constants [RUN_IN_DEBUG_MODE=" + RUN_IN_DEBUG_MODE + ", CSS_PROPERTIES_MASTER_LIST=" + CSS_PROPERTIES_MASTER_LIST + ", CROSSOVER_RATE=" + CROSSOVER_RATE_PHASE1 + ", MUTATION_RATE=" + MUTATION_RATE_PHASE1 + ", AVM_RATE=" + AVM_RATE_PHASE1 + ", ISOLATE_AVM="+ ISOLATE_AVM + ", FITNESS_FUNCTION_WEIGHT_STRUCTURE=" + FITNESS_FUNCTION_WEIGHT_STRUCTURE
				+ ", FITNESS_FUNCTION_WEIGHT_AESTHETIC=" + FITNESS_FUNCTION_WEIGHT_AESTHETIC + ", POPULATION_SIZE_APPROACH1_PHASE1=" + POPULATION_SIZE_APPROACH1_PHASE1 + ", MAX_GENERATIONS_APPROACH1_PHASE1="
				+ MAX_GENERATIONS_APPROACH1_PHASE1 + ", SATURATION_POINT_APPROACH1_PHASE1=" + SATURATION_POINT_APPROACH1_PHASE1 + ", POPULATION_SIZE_APPROACH1_PHASE2=" + POPULATION_SIZE_APPROACH1_PHASE2 + ", MAX_GENERATIONS_APPROACH1_PHASE2=" + MAX_GENERATIONS_APPROACH1_PHASE2 + ", SATURATION_POINT_APPROACH1_PHASE2="
				+ SATURATION_POINT_APPROACH1_PHASE2 + ", MAX_ITERATIONS_APPROACH1=" + MAX_ITERATIONS_APPROACH1 + ", SATURATION_POINT_APPROACH1=" + SATURATION_POINT_APPROACH1 + "]";

	}
	
	
}
