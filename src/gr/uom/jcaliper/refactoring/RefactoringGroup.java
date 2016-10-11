package gr.uom.jcaliper.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gr.uom.jcaliper.executor.ExecutionManager;
import gr.uom.jcaliper.explorer.CratExplorer;
import gr.uom.jcaliper.explorer.CratState;

public class RefactoringGroup extends Refactoring {

	private ArrayList<? extends Refactoring> refactorings;

	public RefactoringGroup(List<? extends Refactoring> refactorings) {
		super();
		this.refactorings = (ArrayList<? extends Refactoring>) refactorings;
		refMoves = refactorings.stream().mapToInt(Refactoring::getRefMoves).sum();

		updateInitialEvaluation();
	}

	public RefactoringGroup(Refactoring... refactorings) {
		this(Arrays.asList(refactorings));
	}

	@Override
	public void updateInitialEvaluation() {
		CratExplorer explorer = ExecutionManager.getInstance().getExplorer();
		CratState initial = ((CratState) explorer.getInitialState()).clone();
		double initialEvaluation = initial.getEvaluation();
		double finalEvaluation = applyRefactoring(initial).getEvaluation();

		systemBenefit = (initialEvaluation - finalEvaluation);
		systemBenefit = systemBenefit == 0 ? 0 : systemBenefit / refMoves;
	}

	@Override
	public CratState applyRefactoring(CratState initial) {
		CratState currentState = initial;

		for (Refactoring ref : refactorings) {
			currentState = ref.applyRefactoring(currentState);
		}
		return currentState;
	}

	@Override
	public String detailedDescription(LocalOptimum optimum) {
		StringBuilder strB = new StringBuilder();

		for (Refactoring ref : refactorings) {
			strB.append(ref.detailedDescription(optimum));
		}
		return String.valueOf(strB);
	}

	@Override
	public String shortDescription(LocalOptimum optimum) {
		StringBuilder strB = new StringBuilder();

		for (Refactoring ref : refactorings) {
			strB.append(ref.oneLineDescription(optimum));
		}
		strB.setLength(strB.length() - 1);
		strB.append(String.format("\nImprovement: %.6f\n", systemBenefit));

		return String.valueOf(strB);
	}

	@Override
	public String oneLineDescription(LocalOptimum optimum) {
		StringBuilder strB = new StringBuilder();

		for (Refactoring ref : refactorings) {
			strB.append(ref.shortDescription(optimum));
		}
		return String.valueOf(strB);
	}

	public ArrayList<? extends Refactoring> getRefactorings() {
		return refactorings;
	}

	@Override
	public String toString() {
		return "RefactoringGroup [refactorings=" + refactorings + ", systemBenefit=" + systemBenefit + "]";
	}
}
