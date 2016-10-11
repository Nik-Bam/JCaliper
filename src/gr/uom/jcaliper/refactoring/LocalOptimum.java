package gr.uom.jcaliper.refactoring;

import java.util.ArrayList;

import gr.uom.jcaliper.system.CratSystem;
import gr.uom.jcaliper.system.SystemClass;
import gr.uom.jcaliper.util.IDescribable;

/**
 * @author Panagiotis Kouros
 */
public class LocalOptimum implements IDescribable {

	private ArrayList<RefactoredClass> refClasses;
	private ArrayList<ExtractClass> extractClassRefs;
	private ArrayList<MoveEntity> moveEntityRefs;
	private ArrayList<Refactoring> customRefs;
	private int numOfMoves;
	private int refactoringSteps;
	private double evaluation;
	private long hash;
	private CratSystem system;

	public LocalOptimum(ArrayList<RefactoredClass> refClasses, ArrayList<ExtractClass> extractClassRefs,
			ArrayList<MoveEntity> moveEntityRefs, double evaluation, CratSystem system) {
		super();
		this.refClasses = refClasses;
		this.extractClassRefs = extractClassRefs;
		this.moveEntityRefs = moveEntityRefs;
		this.evaluation = evaluation;
		this.system = system;
		numOfMoves = moveEntityRefs.stream().mapToInt(MoveEntity::getRefMoves).sum();
		customRefs = new ArrayList<>();

		updateRefactoringSteps();
		updateHash();
	}

	// presentation methods

	@Override
	public String oneLineDescription() {
		return String.format("\nEXTRACT CLASS REFACTORINGS (%d new classes, %d steps):\n", extractClassRefs.size(),
				refactoringSteps - numOfMoves) + "\n"
				+ String.format("\nMOVE ENTITY REFACTORINGS (%d steps):\n", numOfMoves);
	}

	@Override
	public String shortDescription() {
		StringBuilder sb = new StringBuilder();

		sb.append("\nREFACTORED CLASS RESPONSIBILITY ASSIGNMENT:\n");

		String currentPackage = "";
		for (RefactoredClass refCl : refClasses) {
			if (refCl.size() > 0) {
				SystemClass sysCl = refCl.getOrigin();
				String sysPackage = sysCl.getPackage().getName();
				if (!currentPackage.equals(sysPackage)) {
					currentPackage = sysPackage;
					sb.append("\n*** package '").append(currentPackage).append("'\n");
				}
				sb.append(String.format("\t%s = %s\n", refCl.getName(), refCl.showNamesSetUnboxed(system)));
			}
		}
		sb.append("\n\nSUGGESTED REFACTORINGS IN ORDER OF IMPROVEMENT:\n\n");

		customRefs.sort(Refactoring.DESC_COMPARATOR);

		for (Refactoring ref : customRefs) {
			sb.append(ref.shortDescription(this)).append("\n");
		}
		return sb.toString();
	}

	@Override
	public String detailedDescription() {
		StringBuilder sb = new StringBuilder();
		if (extractClassRefs.size() > 0) {
			sb.append(String.format("\n\nEXTRACT CLASS REFACTORINGS (%d new classes, %d steps):\n",
					extractClassRefs.size(), refactoringSteps - numOfMoves));
		}
		String currentPackage = "";
		for (ExtractClass ref : extractClassRefs) {
			String refPackage = ref.getRefPackage().getName();
			if (!currentPackage.equals(refPackage)) {
				currentPackage = refPackage;
				sb.append("\n*** in package '").append(currentPackage).append("'\n");
			}
			sb.append(ref.detailedDescription(this));
		}
		if (numOfMoves > 0) {
			sb.append(String.format("\n\nMOVE ENTITY REFACTORINGS (%d steps):\n", numOfMoves));
		}
		currentPackage = "";
		for (MoveEntity ref : moveEntityRefs) {
			String refPackage = ref.getRefPackage().getName();
			if (!currentPackage.equals(refPackage)) {
				currentPackage = refPackage;
				sb.append("\n*** in package '").append(currentPackage).append("'\n");
			}
			sb.append(ref.detailedDescription(this));
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1); // remove last newline
		}
		return sb.toString();
	}

	/**
	 * @return the refClasses
	 */
	public ArrayList<RefactoredClass> getRefClasses() {
		return refClasses;
	}

	/**
	 * @return the extractClassRefs
	 */
	public ArrayList<ExtractClass> getExtractClassRefs() {
		return extractClassRefs;
	}

	/**
	 * @return the moveEntityRefs
	 */
	public ArrayList<MoveEntity> getMoveEntityRefs() {
		return moveEntityRefs;
	}

	/**
	 * @return the refactoringSteps
	 */
	public int getRefactoringSteps() {
		return refactoringSteps;
	}

	/**
	 * @return the system
	 */
	public CratSystem getSystem() {
		return system;
	}

	/**
	 * @return the evaluation
	 */
	public double getEvaluation() {
		return evaluation;
	}

	public ArrayList<Refactoring> getCustomRefs() {
		return customRefs;
	}

	private void updateRefactoringSteps() {
		refactoringSteps = extractClassRefs.size();
		for (ExtractClass cl : extractClassRefs) {
			refactoringSteps += cl.getRefMoves();
		}
		refactoringSteps += numOfMoves;
	}

	private void updateHash() {
		hash = 0;
		for (RefactoredClass refCl : refClasses) {
			hash = ((hash << 3) - hash) + refCl.getHash(); // 7 * hash + classHash
		}
	}
}
