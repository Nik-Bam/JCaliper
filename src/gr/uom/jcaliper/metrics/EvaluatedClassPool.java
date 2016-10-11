package gr.uom.jcaliper.metrics;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Panagiotis Kouros
 */
public class EvaluatedClassPool extends TreeMap<Long, EvaluatedClass> {

	private TreeSet<EvaluatedClass> indexByEvaluation;

	public EvaluatedClassPool(boolean biggerValuesAreBetter) {
		super();
		if (biggerValuesAreBetter)
			indexByEvaluation = new TreeSet<EvaluatedClass>(new Descending());
		else
			indexByEvaluation = new TreeSet<EvaluatedClass>(new Ascending());
	}

	@Override
	public EvaluatedClass put(Long hash, EvaluatedClass evaluated) {
		indexByEvaluation.add(evaluated);
		return super.put(hash, evaluated);
	}

	@Override
	public EvaluatedClass remove(Object hash) {
		EvaluatedClass removed = super.remove(hash);
		indexByEvaluation.remove(removed);
		return removed;
	}

	private class Ascending implements Comparator<EvaluatedClass> {
		@Override
		public int compare(EvaluatedClass evalClass1, EvaluatedClass evalClass2) {
			double diff = evalClass1.getEvaluation() - evalClass2.getEvaluation();
			if (diff < -1e-10)
				return -1;
			else if (diff > 1e-10)
				return 1;
			else if (evalClass1.getHash() > evalClass2.getHash())
				return -1;
			else if (evalClass1.getHash() < evalClass2.getHash())
				return 1;
			return 0;
		}
	}

	private class Descending implements Comparator<EvaluatedClass> {
		@Override
		public int compare(EvaluatedClass evalClass1, EvaluatedClass evalClass2) {
			double diff = evalClass1.getEvaluation() - evalClass2.getEvaluation();
			if (diff > 1e-10)
				return -1;
			else if (diff < -1e-10)
				return 1;
			else if (evalClass1.getHash() > evalClass2.getHash())
				return -1;
			else if (evalClass1.getHash() < evalClass2.getHash())
				return 1;
			return 0;
		}
	}

	public String getTopEvaluatedClasses(int howMany) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Top %d of evaluated classes\n", howMany));
		for (EvaluatedClass cl : indexByEvaluation) {
			if (cl.getHash() != 0) // exclude empty class
				sb.append(String.format("Fittness value = %8.6f\t%s\n", cl.getEvaluation(), cl));
			if (--howMany <= 0)
				break;
		}
		return sb.toString();
	}

	public String getTopEvaluatedClassesWithDetails() {
		StringBuilder sb = new StringBuilder();
		sb.append("Evaluated classes\n");
		for (EvaluatedClass cl : indexByEvaluation) {
			sb.append(String.format("Fittness value = %10.8f\t%s\n", cl.getEvaluation(), cl));
			for (int entId : cl.candidatesForExit())
				sb.append(String.format("Exit  gain for %d = %10.8f\n", entId,
						cl.getExitGain(entId)));
			for (int entId : cl.candidatesForEntry())
				sb.append(String.format("Entry gain for %d = %10.8f\n", entId,
						cl.getEntryGain(entId)));
		}
		return sb.toString();
	}

	private static final long serialVersionUID = 1L;

}
