package gr.uom.jcaliper.heuristics;

import gr.uom.jcaliper.preferences.Preferences;

import java.util.Random;

/**
 * @author Panagiotis Kouros
 */
public class SimulatedAnnealing extends SearchAlgorithm {

    private final static Random random = new Random(0);
    private RandomMoveGenerator moveGen;

    /**
     * @param explorer
     * @param pool
     * @param moveLogger
     */
    public SimulatedAnnealing(IStateSpaceExplorer explorer, IOptimaPool pool, ISearchLogger logger) {
        super(explorer, pool, logger);
        name = "Simulated Annealing";
        shortName = "SA";
    }

    /**
     * @param explorer
     * @param startingPoint
     * @param pool
     * @param moveLogger
     */
    public SimulatedAnnealing(IStateSpaceExplorer explorer, IProblemState startingPoint,
                              IOptimaPool pool, ISearchLogger logger) {
        super(explorer, startingPoint, pool, logger);
        name = "Simulated Annealing";
        shortName = "SA";
    }

    @Override
    public IProblemState getOptimum() {
        startTimer();
        logStart();

        // The temperature
        double T = defineInitialTemperature(); // T(0)
        // The number of trials on each temperature level
        int N = 100 * getProblemSize(); // N(0)
        if (N > MAX_INITIAL_NUMBER_OF_TRIALS)
            N = MAX_INITIAL_NUMBER_OF_TRIALS;
        // The temperature levels counter
        int k = 1;
        // The most recent level with solution improvement
        int lastLevelWithImprovement = 0;
        int levelsWithoutImprovement;
        moveGen = getRandomMoveGenerator();

        do { // temperature level trials
            logInfo(String.format("Temperature Level %d", k));
            logLabel(String.format("%d", k));
            for (int trial = 1; trial <= N; trial++) {
                IMove candidate = moveGen.getRandomMove();
                if (candidate == null)
                    return bestSolution; // no feasible moves
                if (candidate.isBetter()) {
                    // Always accept better candidate
                    doMove(candidate);
                    if (currentState.isBetterThan(bestSolution.getEvaluation())) {
                        bestSolution = currentState.clone();
                        lastLevelWithImprovement = k;
                    }
                } else {
                    // Accept candidate move with Boltzmann probability
                    if (moveIsAccepted(candidate, T)) {
                        doMove(candidate);
                    }
                }

                if (timeOver())
                    return bestSolution;
            }
            levelsWithoutImprovement = k - lastLevelWithImprovement;
            k++;
            N *= TRIALS_INCREASING_RATE; // trials on next level
            T *= COOLING_RATE; // Temperature on next level
        } while (levelsWithoutImprovement <= MAX_LEVELS_WITHOUT_IMPROVEMENT);
        // bestSolution = improveSolution(bestSolution);
        logEnd();
        bestSolution = improveSolution(bestSolution);
        return bestSolution;
    }

    protected boolean moveIsAccepted(IMove move, double T) {
        double deltaEvaluation = -Math.abs(move.getMoveGain());
        double acceptionProbability = Math.exp(deltaEvaluation / T);
        return (random.nextDouble() < acceptionProbability);
    }

    protected double defineInitialTemperature() {
        return getMaxDeterioration() / -Math.log(INITIAL_ACCEPTANCE);
    }

    @Override
    protected void doMove(IMove move) {
        super.doMove(move);
        moveGen = getRandomMoveGenerator();
        // TODO use move statistics to self-adapt temperature
    }

    @Override
    public int getAlgorithmId() {
        return HeuristicFactory.SIMULATED_ANNEALING;
    }

    // Don't modify next lines. Change the static values in class Preferences
    private static final double INITIAL_ACCEPTANCE = Preferences.INITIAL_ACCEPTANCE;
    private static final int MAX_INITIAL_NUMBER_OF_TRIALS = Preferences.MAX_INITIAL_NUMBER_OF_TRIALS;
    private static final double TRIALS_INCREASING_RATE = Preferences.TRIALS_INCREASING_RATE;
    private static final double COOLING_RATE = Preferences.COOLING_RATE;
    private static final int MAX_LEVELS_WITHOUT_IMPROVEMENT = Preferences.MAX_UNPRODUCTIVE_LEVELS;

}
