package gr.uom.jcaliper.explorer;

import java.util.ArrayList;
import java.util.Collection;

import gr.uom.jcaliper.heuristics.IMove;
import gr.uom.jcaliper.heuristics.IProblemState;
import gr.uom.jcaliper.heuristics.IStateSpaceExplorer;
import gr.uom.jcaliper.heuristics.RandomMoveGenerator;
import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.metrics.Metric;
import gr.uom.jcaliper.preferences.Preferences;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.HashedClass;

/**
 * @author Panagiotis Kouros
 */
public class CratExplorer implements IStateSpaceExplorer {

	private CraCase craCase;
	private Navigator navigator;
	private Metric metric;
	private CratState currentState;

	public CratExplorer(CraCase craCase, Metric metric) {
		this.craCase = craCase;
		this.metric = metric;
		currentState = craCase.getInitial().clone();
		EvaluatedClass emptyClass = getEmptyClass();
		navigator = new Navigator(metric.toBeMaximized(), emptyClass);
		navigator.newCurrentState(currentState);
		// System.out.println(metric.getEvaluatedClassesWithDetails());
	}

	public CratExplorer(IStateSpaceExplorer explorer) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void moveTo(IProblemState problemState) {
		currentState = (CratState) problemState;
		navigator.newCurrentState(currentState);
	}

	@Override
	public void doMove(IMove move) {
		// System.out.println(metric.getTopEvaluatedClasses(10));
		CratMove cMove = (CratMove) move;
		EvaluatedClass origin = cMove.getFrom();
		EvaluatedClass target = cMove.getTo();
		int moving = cMove.getMoving();
		if (PRINT_DEBUG_INFO) {
			System.out.format("> Moving %d from %s to %s\n", moving, origin, target);
		}
		// Get the new classes
		HashedClass hashed = new HashedClass(origin.without(moving));
		EvaluatedClass new1 = metric.getEvaluatedClass(hashed);
		new1.setClassId(cMove.getFrom().getClassId());		// paidaki
		hashed = new HashedClass(target.plus(moving));
		EvaluatedClass new2 = metric.getEvaluatedClass(hashed);
		new2.setClassId(cMove.getTo().getClassId());		// paidaki
		// Delete old classes
		currentState.remove(origin.getHash());
		navigator.removeClass(origin);
		currentState.remove(target.getHash());
		navigator.removeClass(target);
		// Add new classes
		if (new1.size() > 0) {		// do not add empty classes to state
			currentState.put(new1.getHash(), new1);
		}
		currentState.put(new2.getHash(), new2);
		currentState.evaluation += (new1.getEvaluation() + new2.getEvaluation()) - origin.getEvaluation()
				- target.getEvaluation();
		navigator.addClass(new1);
		navigator.addClass(new2);
		// System.out.println(metric.getEvaluatedClassesWithDetails());
	}

	public CraCase getCraCase() {
		return craCase;
	}

	public Metric getMetric() {
		return metric;
	}

	@Override
	public IProblemState getCurrentState() {
		return currentState;
	}

	@Override
	public IProblemState getInitialState() {
		return craCase.getInitial();
	}

	@Override
	public double evaluate(IMove move) {
		return ((CratMove) move).getMoveGain();
	}

	@Override
	public IMove getBestMove() {
		return navigator.getBestMove();
	}

	@Override
	public IMove getAMoveBetterThan(double threshold) {
		return navigator.getAMoveBetterThan(threshold);
	}

	@Override
	public int getNumOfFeasibleMoves() {
		return navigator.getNumOfFeasibleMoves();
	}

	@Override
	public Collection<? extends IMove> getEvaluatedMoves() {
		// System.out.println(navigator.getTopEvaluatedMoves(10));
		return navigator.moves;
	}

	@Override
	public IMove getRandomMove() {
		return navigator.getRandomMove();
	}

	@Override
	public Collection<? extends IProblemState> getSomeRandomStates(int numOfStates) {
		ArrayList<CratState> states = new ArrayList<CratState>();
		states.add(craCase.getInitial());
		return states;
		// TODO create some feasible initial states (ILS?)
	}

	@Override
	public RandomMoveGenerator getRandomMoveGenerator() {
		return navigator.getRandomMoveGenerator();
	}

	public void clear() {
		if (navigator != null) {
			navigator.clear();
		}
		if (metric != null) {
			metric.clear();
		}
	}

	@Override
	public void finalize() throws Throwable {
		clear();
		super.finalize();
	}

	public EvaluatedClass getEmptyClass() {
		HashedClass empty = new HashedClass(new EntitySet());
		return metric.getEvaluatedClass(empty);
	}

	public String getNavigatorStatistics() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Min deterioration: %14.12f\n", navigator.getMinDeterioration()));
		sb.append(String.format("Max deterioration: %14.12f\n", navigator.getMaxDeterioration()));
		return sb.toString();
	}

	@Override
	public int getProblemSize() {
		return craCase.getProblemSize();
	}

	@Override
	public double getMaxDeterioration() {
		return (2.0) / craCase.getTotalEntities();
	}

	@Override
	public boolean currentStateIsLocalOptimum() {
		return (navigator.getNumOfImprovingMoves() == 0);
	}

	// Don't modify next line. Change the static value in class Preferences
	protected static final boolean PRINT_DEBUG_INFO = Preferences.PRINT_DEBUG_INFO;

}
