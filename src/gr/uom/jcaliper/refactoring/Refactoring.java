package gr.uom.jcaliper.refactoring;

import java.util.Comparator;

import gr.uom.jcaliper.explorer.CratState;
import gr.uom.jcaliper.util.IDescribableParam;

public abstract class Refactoring implements IDescribableParam {

	public static final Comparator<Refactoring> ASC_COMPARATOR = new Comparator<Refactoring>() {
		@Override
		public int compare(Refactoring o1, Refactoring o2) {
			return ((Double) o1.systemBenefit).compareTo(o2.systemBenefit);
		}
	};

	public static final Comparator<Refactoring> DESC_COMPARATOR = new Comparator<Refactoring>() {
		@Override
		public int compare(Refactoring o1, Refactoring o2) {
			return ((Double) o2.systemBenefit).compareTo(o1.systemBenefit);
		}
	};

	protected int refMoves;
	protected double systemBenefit;

	public Refactoring() {
		refMoves = 0;
		systemBenefit = 0;
	}

	public abstract void updateInitialEvaluation();

	public abstract CratState applyRefactoring(CratState initial);

	@Override
	public abstract String detailedDescription(LocalOptimum optimum);

	@Override
	public abstract String shortDescription(LocalOptimum optimum);

	@Override
	public abstract String oneLineDescription(LocalOptimum optimum);

	public int getRefMoves() {
		return refMoves;
	}

	public void setRefMoves(int refMoves) {
		this.refMoves = refMoves;
	}

	@Override
	public String toString() {
		return "Refactoring [systemBenefit=" + systemBenefit + "]";
	}
}
