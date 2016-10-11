package gr.uom.jcaliper.explorer;

import gr.uom.jcaliper.heuristics.IMove;
import gr.uom.jcaliper.metrics.EvaluatedClass;

/**
 * @author Panagiotis Kouros
 */
public class CratMove implements IMove {

	private int moveId;
	final protected int moving;
	final protected EvaluatedClass from;
	final protected EvaluatedClass to;
	final protected ComparisonPolicy comparisonPolicy;
	protected double gain;

	/**
	 * @param moving
	 * @param from
	 * @param to
	 * @param cost
	 */
	public CratMove(int moveId, int moving, EvaluatedClass from, EvaluatedClass to, boolean biggerValueIsBetter) {
		this.moveId = moveId;
		this.moving = moving;
		this.from = from;
		this.to = to;
		gain = from.getExitGain(moving) + to.getEntryGain(moving);
		if (biggerValueIsBetter) {
			comparisonPolicy = new BiggerValueIsBetter();
		} else {
			comparisonPolicy = new SmallerValueIsBetter();
		}
	}

	@Override
	public void setMoveId(int moveId) {
		this.moveId = moveId;
	}

	@Override
	public int getMoveId() {
		return moveId;
	}

	@Override
	public double getMoveGain() {
		return gain;
	}

	@Override
	public boolean isBetterThan(double threshold) {
		return comparisonPolicy.compare(gain, threshold);
	}

	@Override
	public int getTabuAttribute() {
		return moving;
	}

	@Override
	public boolean isBetter() {
		return isBetterThan(0.0);
	}

	/**
	 * @return the moving entity
	 */
	public int getMoving() {
		return moving;
	}

	/**
	 * @return the from
	 */
	public EvaluatedClass getFrom() {
		return from;
	}

	/**
	 * @return the to
	 */
	public EvaluatedClass getTo() {
		return to;
	}

	private interface ComparisonPolicy {
		public boolean compare(double value1, double value2);
	}

	private class BiggerValueIsBetter implements ComparisonPolicy {
		@Override
		public boolean compare(double value1, double value2) {
			return ((value1 - value2) > 1e-10);
		}
	}

	private class SmallerValueIsBetter implements ComparisonPolicy {
		@Override
		public boolean compare(double value1, double value2) {
			return ((value2 - value1) > 1e-10);
		}
	}

}
