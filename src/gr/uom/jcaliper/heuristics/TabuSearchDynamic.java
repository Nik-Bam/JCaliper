package gr.uom.jcaliper.heuristics;

/**
 * @author Panagiotis Kouros
 */
public class TabuSearchDynamic extends SearchAlgorithm {

    // The tabu memory
    private TabuMemory tabuMemory = new TabuMemory();

    // Tenure parameters
    private int tabuTenure;
    private int minTenure;
    private int maxTenure;
    private int increaseStep;
    private int decreaseStep;
    private boolean increaseTenure = true;

    // stop conditions
    private int maxUnproductiveMoves;

    /**
     * @param explorer
     * @param pool
     * @param moveLogger
     */
    public TabuSearchDynamic(IStateSpaceExplorer explorer, IOptimaPool pool, ISearchLogger logger) {
        super(explorer, pool, logger);
        name = "Tabu Search - Dynamic Tenure";
        shortName = "TS_DYN";
        initializeTabuParameters();
    }

    /**
     * @param explorer
     * @param startingPoint
     * @param pool
     * @param moveLogger
     */
    public TabuSearchDynamic(IStateSpaceExplorer explorer, IProblemState startingPoint,
                             IOptimaPool pool, ISearchLogger logger) {
        super(explorer, startingPoint, pool, logger);
        name = "Tabu Search - Dynamic Tenure";
        shortName = "TS_DYN";
        initializeTabuParameters();
    }

    @Override
    public IProblemState getOptimum() {
        startTimer();
        logStart();

        int unproductiveMoves = 0;
        do {
            IMove bestMove = getBestPermittedMove();
            if (bestMove == null) // no more feasible moves
                break;
            doMove(bestMove);
            if (currentState.isBetterThan(bestSolution.getEvaluation())) {
                bestSolution = currentState.clone();
                unproductiveMoves = 0;
            } else
                unproductiveMoves++;
            if (currentStateIsLocalOptimum())
                redefineTenure();
        } while ((unproductiveMoves < maxUnproductiveMoves) && !timeOver());

        logEnd();
        bestSolution = improveSolution(bestSolution);
        return bestSolution;
    }

    private void initializeTabuParameters() {
        final int TENURE_STEPS = 20;
        int size = getProblemSize();
        minTenure = (int) Math.sqrt(size);
        maxTenure = 5 * minTenure;
        if (maxTenure > size)
            maxTenure = size;
        tabuTenure = minTenure;
        tabuMemory.setTenure(tabuTenure);
        int step = (maxTenure - minTenure) / TENURE_STEPS;
        if (step < 1)
            step = 1;
        increaseStep = 3 * step;
        decreaseStep = 2 * step;
        maxUnproductiveMoves = 50 * minTenure;
    }

    private void redefineTenure() {
        if (increaseTenure) {
            tabuTenure += increaseStep;
            if (tabuTenure > maxTenure)
                tabuTenure = minTenure;
        } else {
            tabuTenure -= decreaseStep;
            if (tabuTenure < minTenure)
                tabuTenure = minTenure;
        }
        tabuMemory.setTenure(tabuTenure);
        logInfo(String.format("Tabu tenure = %d", tabuTenure));
        increaseTenure = !increaseTenure; // toggle
    }

    public void setTabuTenure(int tenure) {
        tabuMemory.setTenure(tabuTenure);
        logInfo(String.format("Tabu tenure = %d", tabuTenure));
    }

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

    private boolean isPermitted(IMove move) {
        return tabuMemory.isPermitted(moveId, move);
    }

    ;

    @Override
    public int getAlgorithmId() {
        return HeuristicFactory.TABU_SEARCH_DYNAMIC;
    }

}
