package gr.uom.jcaliper.heuristics;

import gr.uom.jcaliper.loggers.ActivityLogger;
import gr.uom.jcaliper.plugin.Activator;
import gr.uom.jcaliper.preferences.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;

import java.util.Collection;

/**
 * @author Panagiotis Kouros
 */
public abstract class SearchAlgorithm {

    protected String name;
    protected String shortName;

    // Common for all algorithms
    protected IStateSpaceExplorer explorer;
    protected IOptimaPool pool = null;
    protected IProblemState currentState;
    protected IProblemState bestSolution;

    // Logging variables
    protected int moveId = -1; // on moveTo(initial): moveId=0
    protected long startTime;
    protected long stopTime;
    protected long logTime;
    protected ISearchLogger logger = null;

    // CONSTRUCTORS

    /**
     * @param explorer
     * @param pool
     * @param moveLogger
     */
    public SearchAlgorithm(IStateSpaceExplorer explorer, IOptimaPool pool, ISearchLogger moveLogger) {
        this.explorer = explorer;
        moveTo(explorer.getInitialState().clone());
        bestSolution = currentState.clone();
        this.pool = pool;
        logger = moveLogger;
        initializeTimeLimit();
    }

    /**
     * @param explorer
     * @param startingPoint
     * @param pool
     * @param moveLogger
     */
    public SearchAlgorithm(IStateSpaceExplorer explorer, IProblemState startingPoint, IOptimaPool pool,
                           ISearchLogger moveLogger) {
        this.explorer = explorer;
        moveTo(startingPoint.clone());
        bestSolution = currentState.clone();
        this.pool = pool;
        logger = moveLogger;
        initializeTimeLimit();
    }

    // Each algorithm overrides these methods
    // with their special implementation
    public abstract int getAlgorithmId();

    public abstract IProblemState getOptimum();

    // SOME IMPLEMENTED UTILITIES - ALL ALGORITHMS CAN USE THEM

    // For moving: moveTo() and makeMove()

    /**
     * Used for a move to a completely new position
     *
     * @param problemState
     */
    protected void moveTo(IProblemState problemState) {
        moveId++;
        explorer.moveTo(problemState);
        currentState = explorer.getCurrentState();
    }

    /**
     * Used for moves to neighbor positions
     *
     * @param move the move
     */
    protected void doMove(IMove move) {
        moveId++;
        move.setMoveId(moveId);
        explorer.doMove(move);
        currentState = explorer.getCurrentState();
        if (logger != null)
            logger.logMove(this, currentState, bestSolution, move, elapsedTime());
        if (currentStateIsLocalOptimum()) {
            storeSolution(currentState);
            logLocalOptimum("reached ");
        }
        // System.out.format("EPS=%10.8f\t%d moved from %s to %s\t%s\n",
        // currentState.getEvaluation(),
        // ((CratMove) move).getMoving(), ((CratMove) move).getFrom(),
        // ((CratMove) move).getTo(), currentState);
    }

    protected void storeBestSolution() {
        if (pool != null)
            pool.storeLocalOptimum(this, bestSolution, moveId, elapsedTime());
    }

    protected void storeSolution(IProblemState state) {
        if (pool != null)
            pool.storeLocalOptimum(this, state, moveId, elapsedTime());
    }

    protected IProblemState improveSolution(IProblemState solution) {
        if (solution != null)
            return (solution);
        // Do hill climbing steepest
        // TODO clone explorer
        HillClimbing hillClimber = new HillClimbing(explorer, solution, pool, logger);
        hillClimber.setMoveId(moveId);
        hillClimber.setUseHillClimbing(HillClimbing.USE_HC_STEEPEST);
        IProblemState steepestBest = hillClimber.getOptimum();
        storeSolution(steepestBest);
        currentState = steepestBest;
        logLocalOptimum(String.format("Improved by %s", hillClimber.getShortName()));
        // Do hill climbing first choice
        hillClimber = new HillClimbing(explorer, solution, pool, logger);
        hillClimber.setMoveId(moveId);
        hillClimber.setUseHillClimbing(HillClimbing.USE_HC_FIRST_CHOICE);
        IProblemState firstChoiceBest = hillClimber.getOptimum();
        storeSolution(firstChoiceBest);
        currentState = firstChoiceBest;
        logLocalOptimum(String.format("Improved by %s", hillClimber.getShortName()));

        if (steepestBest.isBetterThan(firstChoiceBest.getEvaluation()))
            return steepestBest;
        else
            return firstChoiceBest;
    }

    // Delegating explorer methods

    protected int getProblemSize() {
        return explorer.getProblemSize();
    }

    protected double getMaxDeterioration() {
        return explorer.getMaxDeterioration();
    }

    protected boolean currentStateIsLocalOptimum() {
        return explorer.currentStateIsLocalOptimum();
    }

    protected double evaluate(IMove move) {
        return explorer.evaluate(move);
    }

    public IMove getBestMove() {
        return explorer.getBestMove();
    }

    ;

    public IMove getABetterMove() {
        return explorer.getAMoveBetterThan(0);
    }

    public int getNumOfFeasibleMoves() {
        return explorer.getNumOfFeasibleMoves();
    }

    public Collection<? extends IMove> getEvaluatedMoves() {
        return explorer.getEvaluatedMoves();
    }

    public IMove getRandomMove() {
        return explorer.getRandomMove();
    }

    ;

    public RandomMoveGenerator getRandomMoveGenerator() {
        return explorer.getRandomMoveGenerator();
    }

    ;

    public Collection<? extends IProblemState> getSomeRandomStates(int howMany) {
        return explorer.getSomeRandomStates(howMany);
    }

    ;

    // Logging methods

    protected void logStart() {
        String message = String.format("\n*** %s started   : initial fitness value: %8.6f", shortName,
                currentState.getEvaluation());
        ActivityLogger.info(message);
        if (logger != null)
            logger.logStart(this, currentState, moveId, elapsedTime());
    }

    protected void logJump(String message) {
        if (logger != null)
            logger.logJump(this, currentState, bestSolution, moveId, elapsedTime(), message);
    }

    protected void logLocalOptimum(String message) {
        if (logger != null)
            logger.logLocalOptimum(this, currentState, bestSolution, moveId, elapsedTime(), message);
    }

    protected void logInfo(String message) {
        if (logger != null)
            logger.logInfo(this, currentState, bestSolution, moveId, elapsedTime(), message);
    }

    protected void logLabel(String labelText) {
        if (logger != null)
            logger.logLabel(this, currentState, bestSolution, moveId, elapsedTime(), labelText);
    }

    protected void logEnd() {
        String message = String.format("\n*** %s completed : optimum fitness value: %8.6f", shortName,
                bestSolution.getEvaluation());
        ActivityLogger.info(message);
        if (logger != null)
            logger.logEnd(this, currentState, bestSolution, moveId, elapsedTime());
    }

    // Timer methods

    protected void startTimer() {
        startTime = System.currentTimeMillis();
        stopTime = startTime + MAX_RUNNING_TIME * 1000;
        logTime = startTime + LOG_INTERVAL; // is already in millis
    }

    ;

    protected long elapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    protected boolean timeOver() {
        final String MSG_TIMEOVER = "*** TimeOver!";
        final String MSG_ABORTED = "*** Interrupted by user!";
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > logTime) {
            String message = String.format("\n*** %s is running: %d moves, best fitness: %8.6f", shortName, moveId,
                    (bestSolution.isBetterThan(currentState.getEvaluation()) ? bestSolution.getEvaluation()
                            : currentState.getEvaluation()));
            ActivityLogger.info(message);
            logTime += LOG_INTERVAL;
        }

        if (LIMIT_TIME && (currentTimeMillis > stopTime)) {
            ActivityLogger.info("\n" + MSG_TIMEOVER + "\n");
            logInfo(MSG_TIMEOVER);
            return true;
        }

        if (ABORTED) {
            ActivityLogger.info("\n" + MSG_ABORTED + "\n");
            logInfo(MSG_ABORTED);
            return true;
        }

        return false;
    }

    // Getters and Setters

    /**
     * @return the explorer
     */
    public IStateSpaceExplorer getExplorer() {
        return explorer;
    }

    /**
     * @param explorer the explorer to set
     */
    public void setExplorer(IStateSpaceExplorer explorer) {
        this.explorer = explorer;
    }

    /**
     * @return the pool
     */
    public IOptimaPool getPool() {
        return pool;
    }

    /**
     * @param pool the pool to set
     */
    public void setPool(IOptimaPool pool) {
        this.pool = pool;
    }

    /**
     * @param moveId the moveId to set
     */
    public void setMoveId(int moveId) {
        this.moveId = moveId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    public int getNumOfPerfomedMoves() {
        return moveId;
    }

    private void initializeTimeLimit() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        LIMIT_TIME = store.getBoolean(Preferences.P_SEARCH_LIMIT_TIME);
        MAX_RUNNING_TIME = store.getInt(Preferences.P_SEARCH_MAX_RUNNING_TIME);
        LOG_INTERVAL = 30000; // 30sec
    }

    // if LIMIT_TIME is true,
    // algorithm stops after MAX_RUNNING_TIME milliseconds
    public boolean LIMIT_TIME;
    public int MAX_RUNNING_TIME;
    public long LOG_INTERVAL;

    public static boolean ABORTED = false;
}
