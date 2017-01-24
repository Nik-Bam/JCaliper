package gr.uom.jcaliper.heuristics;

public class HillClimbing extends SearchAlgorithm {

    // What Hill Climbing algorithm to use ?
    public static final int USE_HC_FIRST_CHOICE = 1;
    public static final int USE_HC_STEEPEST = 2;
    private int heuristicId;

    private int useHillClimber = USE_HC_FIRST_CHOICE;

    /**
     * @param explorer
     * @param pool
     * @param moveLogger
     */
    public HillClimbing(IStateSpaceExplorer explorer, IOptimaPool pool, ISearchLogger logger) {
        super(explorer, pool, logger);
        setUseHillClimbing(USE_HC_FIRST_CHOICE);
    }

    /**
     * @param explorer
     * @param startingPoint
     * @param pool
     * @param moveLogger
     */
    public HillClimbing(IStateSpaceExplorer explorer, IProblemState startingPoint,
                        IOptimaPool pool, ISearchLogger logger) {
        super(explorer, startingPoint, pool, logger);
        setUseHillClimbing(USE_HC_FIRST_CHOICE);
    }

    /**
     * @param explorer
     * @param pool
     * @param moveLogger
     * @param useHillClimber
     */
    public HillClimbing(IStateSpaceExplorer explorer, IOptimaPool pool, ISearchLogger logger,
                        int useHillClimber) {
        super(explorer, pool, logger);
        setUseHillClimbing(useHillClimber);
    }

    /**
     * @param explorer
     * @param startingPoint
     * @param pool
     * @param moveLogger
     * @param useHillClimber
     */
    public HillClimbing(IStateSpaceExplorer explorer, IProblemState startingPoint,
                        IOptimaPool pool, ISearchLogger moveLogger, int useHillClimber) {
        super(explorer, startingPoint, pool, moveLogger);
        setUseHillClimbing(useHillClimber);
    }

    /**
     * @param useHillClimbing the useHillClimbing to set
     */
    public void setUseHillClimbing(int useHillClimber) {
        this.useHillClimber = useHillClimber;
        if (useHillClimber == USE_HC_STEEPEST) {
            name = "Hill Climbing (Steepest)";
            shortName = "HC_ST";
            heuristicId = HeuristicFactory.HILL_CLIMBING_STEEPEST;
        } else { // default
            name = "Hill Climbing (First Choice)";
            shortName = "HC_FC";
            heuristicId = HeuristicFactory.HILL_CLIMBING_FIRST_CHOICE;
        }
    }

    @Override
    public IProblemState getOptimum() {
        startTimer();
        logStart();
        if (useHillClimber == USE_HC_STEEPEST)
            bestSolution = steepest().clone();
        else
            bestSolution = firstChoice().clone();
        storeBestSolution();
        logEnd();
        return bestSolution;
    }

    // H.C. First Choice Implementation
    private IProblemState firstChoice() {
        do {
            IMove nextMove = getABetterMove();
            if ((nextMove != null) && nextMove.isBetter())
                doMove(nextMove);
            else
                return currentState;
            if (timeOver())
                return currentState;
        } while (true);
    }

    // H.C. Steepest Ascent Implementation
    private IProblemState steepest() {
        do {
            IMove nextMove = getBestMove();
            if ((nextMove != null) && nextMove.isBetter())
                doMove(nextMove);
            else
                return currentState;
            if (timeOver())
                return currentState;
        } while (true);
    }

    @Override
    public int getAlgorithmId() {
        return heuristicId;
    }

}
