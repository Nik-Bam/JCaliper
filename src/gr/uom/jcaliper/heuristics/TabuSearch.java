package gr.uom.jcaliper.heuristics;

/**
 * @author Panagiotis Kouros
 */
public class TabuSearch extends SearchAlgorithm {

	private TabuMemory tabuMemory = new TabuMemory();
	private int tabuTenure;
	private int maxUnproductiveMoves;

	/**
	 * @param explorer
	 * @param pool
	 * @param moveLogger
	 */
	public TabuSearch(IStateSpaceExplorer explorer, IOptimaPool pool, ISearchLogger logger) {
		super(explorer, pool, logger);
		name = "Tabu Search";
		shortName = "TS";
		tabuTenure = 5 * (int) Math.sqrt(getProblemSize());
		tabuMemory.setTenure(tabuTenure);
		maxUnproductiveMoves = 200 * (int) Math.sqrt(getProblemSize());
	}

	/**
	 * @param explorer
	 * @param startingPoint
	 * @param pool
	 * @param moveLogger
	 */
	public TabuSearch(IStateSpaceExplorer explorer, IProblemState startingPoint, IOptimaPool pool,
			ISearchLogger logger) {
		super(explorer, startingPoint, pool, logger);
		name = "Tabu Search";
		shortName = "TS";
		tabuTenure = 5 * (int) Math.sqrt(getProblemSize());
		tabuMemory.setTenure(tabuTenure);
		maxUnproductiveMoves = 50 * (int) Math.sqrt(getProblemSize());
	}

	@Override
	public IProblemState getOptimum() {
		startTimer();
		logStart();

		int stepsWithoutImprovement = 0;
		do {
			IMove bestMove = getBestPermittedMove();
			if (bestMove == null) // no available moves
				break;
			doMove(bestMove);
			if (currentState.isBetterThan(bestSolution.getEvaluation())) {
				bestSolution = currentState.clone();
				stepsWithoutImprovement = 0;
			} else
				stepsWithoutImprovement++;
			// TODO tabu tenure comparison
			// updateTabuTenure();
			if (timeOver())
				break;
		} while (stepsWithoutImprovement < maxUnproductiveMoves);
		// improveBestSolution();
		logEnd();
		bestSolution = improveSolution(bestSolution);
		return bestSolution;
	}

	/**
	 * @param tabuMemory
	 *            the tabuMemory to set
	 */
	public void setTabuMemory(TabuMemory tabuMemory) {
		this.tabuMemory = tabuMemory;
	}

	@Override
	protected void doMove(IMove move) {
		super.doMove(move);
		tabuMemory.update(moveId, move);
	}

	private IMove getBestPermittedMove() {
		// 'Aspiration': when a move leads to new best solution, permit it
		double aspirationThreshold = bestSolution.getEvaluation() - currentState.getEvaluation();
		for (IMove move : getEvaluatedMoves())
			if (move.isBetterThan(aspirationThreshold) || isPermitted(move))
				return move;
		// If permitted move not found, retry without restrictions
		for (IMove move : getEvaluatedMoves())
			return move;
		// Obviously there are no feasible moves
		return null;
	}

	protected void updateTabuTenure() {
		tabuMemory.setTenure(tabuTenure);
	}

	private boolean isPermitted(IMove move) {
		return tabuMemory.isPermitted(moveId, move);
	};

	@Override
	public int getAlgorithmId() {
		return HeuristicFactory.TABU_SEARCH;
	}

}
