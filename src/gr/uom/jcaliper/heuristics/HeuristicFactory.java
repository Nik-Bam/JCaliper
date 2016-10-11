/**
 * 
 */
package gr.uom.jcaliper.heuristics;

public class HeuristicFactory {

	// Supported heuristic algorithms
	public static final int HILL_CLIMBING_STEEPEST = 0;
	public static final int HILL_CLIMBING_FIRST_CHOICE = 1;
	public static final int TABU_SEARCH = 2;
	public static final int TABU_SEARCH_DYNAMIC = 3;
	public static final int SIMULATED_ANNEALING = 4;
	// public static final int TABU_SEARCH_RANDOMIZED = 5;

	private static final int MIN_VALID_HEURISTIC_ID = 0;
	private static final int MAX_VALID_HEURISTIC_ID = 4;

	public static boolean isValidHeuristicId(int heuristicId) {
		return (heuristicId >= MIN_VALID_HEURISTIC_ID) && (heuristicId <= MAX_VALID_HEURISTIC_ID);
	}

	public static SearchAlgorithm getHeuristic(int heuristicId, IStateSpaceExplorer explorer,
			IProblemState startingPoint, IOptimaPool pool, ISearchLogger logger) {
		SearchAlgorithm heuristic = null;
		switch (heuristicId) {
		case HILL_CLIMBING_STEEPEST:
			heuristic = new HillClimbing(explorer, startingPoint, pool, logger,
					HillClimbing.USE_HC_STEEPEST);
			break;
		case HILL_CLIMBING_FIRST_CHOICE:
			heuristic = new HillClimbing(explorer, startingPoint, pool, logger,
					HillClimbing.USE_HC_FIRST_CHOICE);
			;
			break;
		case TABU_SEARCH:
			heuristic = new TabuSearch(explorer, startingPoint, pool, logger);
			;
			break;
		case TABU_SEARCH_DYNAMIC:
			heuristic = new TabuSearchDynamic(explorer, startingPoint, pool, logger);
			;
			break;
		case SIMULATED_ANNEALING:
			heuristic = new SimulatedAnnealing(explorer, startingPoint, pool, logger);
			;
			break;

		default:
			break;
		}
		return heuristic;
	}
}
