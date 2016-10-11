package gr.uom.jcaliper.heuristics;

import java.util.Collection;

public interface IStateSpaceExplorer {

	// Problem information

	public int getProblemSize();

	public double getMaxDeterioration();

	// Moving in state space

	public void moveTo(IProblemState problemState);

	public void doMove(IMove move);

	public IProblemState getCurrentState();

	public IProblemState getInitialState();

	// Evaluating states or candidate moves

	public double evaluate(IMove move);

	public IMove getBestMove();

	public IMove getAMoveBetterThan(double threshold);

	// Getting all possible moves

	public int getNumOfFeasibleMoves();

	/**
	 * Returns a list of all feasible moves
	 * The moves are sorted by evaluation (best is first)
	 */
	public Collection<? extends IMove> getEvaluatedMoves();

	// Getting random moves or states

	public IMove getRandomMove();

	public RandomMoveGenerator getRandomMoveGenerator();

	public Collection<? extends IProblemState> getSomeRandomStates(int howMany);

	public boolean currentStateIsLocalOptimum();

}
