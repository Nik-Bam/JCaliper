package gr.uom.jcaliper.preferences;

import gr.uom.jcaliper.explorer.CraCase;
import gr.uom.jcaliper.metrics.MetricFactory;

/**
 * Constant definitions for plug-in preferences
 * 
 * @author Panagiotis Kouros
 */
public class Preferences {

	// Handle the problem case to optimize as unboxed (plain), boxed or hybrid
	// (uncomment one line)
	public static final int CASE_TYPE = CraCase.CRACASE_BOXED;
	// public static final int CASE_TYPE = CraCase.CRACASE_UNBOXED;
	// public static final int CASE_TYPE = CraCase.CRACASE_HYBRID;

	// Metric to use (uncomment one line)
	public static final int SELECTED_METRIC = MetricFactory.EP;
	// public static final int SELECTED_METRIC = MetricFactory.EP_MOD;

	// Print detailed description of calculations
	// public static final boolean PRINT_DEBUG_INFO = true;
	public static final boolean PRINT_DEBUG_INFO = false;

	// TABU SEARCH
	public static final int MAX_UNPRODUCTIVE_STEPS = 10000;

	// SIMULATED ANNEALING
	// 1. Initial Temperature
	// On start, let's accept the maximum deterioration uphill move
	// with probability A% (suggested value is usually 85%)
	public static final double INITIAL_ACCEPTANCE = 0.30; // i.e. A%=85%
	// 2. Number of trials on each temperature level
	public static final int MAX_INITIAL_NUMBER_OF_TRIALS = 10000; // N(0)
	public static final double TRIALS_INCREASING_RATE = 1.05; // use: >= 1.0
	// 3. Rate of cooling
	public static final double COOLING_RATE = 0.90; // use: 0.80-0.99
	// 4. Stopping criterion
	public static final int MAX_UNPRODUCTIVE_LEVELS = 3;

	public static final String P_LOG_RESULTS = "logResultsPreference";
	public static final String P_LOG_PATH = "logPathPreference";
	public static final String P_LOG_RESULTS_FILE = "logResultsFilePreference";

	public static final String P_DO_PREOPTIMIZE = "doPreoptimizePreference";
	public static final String P_USE4SYS_HILL_CLIMBING_STEEPEST = "useForSysHillClimbingSteepest";
	public static final String P_USE4SYS_HILL_CLIMBING_FIRST_CHOICE = "useForSysHillClimbingFirstChoice";
	public static final String P_USE4SYS_TABU_SEARCH = "useForSysTabuSearch";
	public static final String P_USE4SYS_TABU_SEARCH_DYNAMIC = "useForSysTabuSearchDynamic";
	public static final String P_USE4SYS_SIMULATED_ANNEALING = "useForSysSimulatedAnnealing";

	public static final String P_HC_MAX_CLIMBING_TIMES = "HC_MaximumClimbingTimes";
	public static final String P_SEARCH_LIMIT_TIME = "Search_LimitTime";
	public static final String P_SEARCH_MAX_RUNNING_TIME = "Search_MaxRunningTime";

}
