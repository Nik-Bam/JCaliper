package gr.uom.jcaliper.heuristics;

public interface ISearchLogger {

	// Logging methods

	public void logStart(SearchAlgorithm algorithm, IProblemState startingState,
			int moveId, long time);

	public void logMove(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, IMove move, long time);

	public void logJump(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time, String info);

	public void logLocalOptimum(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time, String info);

	public void logInfo(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time, String info);

	public void logLabel(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time, String labelText);

	public void logEnd(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time);

}
