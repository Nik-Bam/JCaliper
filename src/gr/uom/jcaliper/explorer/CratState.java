package gr.uom.jcaliper.explorer;

import java.util.Collection;
import java.util.TreeMap;

import gr.uom.jcaliper.heuristics.IProblemState;
import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.metrics.Metric;
import gr.uom.jcaliper.system.CratClass;
import gr.uom.jcaliper.system.CratEntity;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.HashedClass;

/**
 * @author Panagiotis Kouros
 */
public class CratState extends TreeMap<Long, EvaluatedClass> implements IProblemState {

	protected final ComparisonPolicy comparisonPolicy;
	protected double evaluation;
	protected Metric metric;

	public CratState(CraCase craCase, Metric metric) {
		super();
		this.metric = metric;
		if (metric.toBeMaximized()) {
			comparisonPolicy = new BiggerValuesAreBetter();
		} else {
			comparisonPolicy = new SmallerValuesAreBetter();
		}
		for (CratClass cl : craCase.getClasses().values()) {
			if (cl.size() > 0) {
				HashedClass hashed = new HashedClass(cl);
				EvaluatedClass evalClass = metric.getEvaluatedClass(hashed);
				put(evalClass.getHash(), evalClass);
			}
		}
		updateEvaluation();
	}

	public CratState(EvaluatedClass evaluated, Metric metric) {
		super();
		this.metric = metric;
		if (metric.toBeMaximized()) {
			comparisonPolicy = new BiggerValuesAreBetter();
		} else {
			comparisonPolicy = new SmallerValuesAreBetter();
		}
		put(evaluated.getHash(), evaluated);
		updateEvaluation();
	}

	public CratState(Collection<EvaluatedClass> classes, Metric metric) {
		super();
		this.metric = metric;
		if (metric.toBeMaximized()) {
			comparisonPolicy = new BiggerValuesAreBetter();
		} else {
			comparisonPolicy = new SmallerValuesAreBetter();
		}
		for (EvaluatedClass evaluated : classes) {
			put(evaluated.getHash(), evaluated);
		}
		updateEvaluation();
	}

	public CratState(CratClass unhashed, Metric metric) {
		super();
		this.metric = metric;
		if (metric.toBeMaximized()) {
			comparisonPolicy = new BiggerValuesAreBetter();
		} else {
			comparisonPolicy = new SmallerValuesAreBetter();
		}
		HashedClass hashed = new HashedClass(unhashed);
		EvaluatedClass evalClass = metric.getEvaluatedClass(hashed);
		put(evalClass.getHash(), evalClass);
		updateEvaluation();
	}

	public CratState(CratState prototype) {
		comparisonPolicy = prototype.comparisonPolicy;
		evaluation = prototype.evaluation;
		putAll(prototype);
	}

	public EvaluatedClass findEvaluatedClass(CratEntity entity) {
		for (EvaluatedClass ev : values()) {
			if (ev.contains(entity.getId())) {
				return ev;
			}
		}
		return null;
	}

	public EvaluatedClass findEvaluatedClass(int classId) {
		for (EvaluatedClass ev : values()) {
			if (ev.getClassId() == classId) {
				return ev;
			}
		}
		return null;
	}

	public EvaluatedClass findEvaluatedClass(EntitySet entities) {
		for (EvaluatedClass ev : values()) {
			if (ev.containsAll(entities)) {
				return ev;
			}
		}
		return null;
	}

	@Override
	public CratState clone() {
		return new CratState(this);
	}

	@Override
	public boolean isBetterThan(double threshold) {
		return comparisonPolicy.compare(evaluation, threshold);
	}

	@Override
	public double getEvaluation() {
		return evaluation;
	}

	@Override
	public long getHash() {
		long hash = 0;
		for (EvaluatedClass cl : values()) {
			hash = ((hash << 3) - hash) + cl.getHash(); // 7 * hash + classHash
		}
		return hash;
	}

	private void updateEvaluation() {
		evaluation = 0.0;
		for (EvaluatedClass cl : values()) {
			evaluation += cl.getEvaluation();
		}
	}

	public Collection<EvaluatedClass> getClasses() {
		return values();
	}

	public String showClasses() {
		StringBuilder sb = new StringBuilder();
		for (EvaluatedClass cl : values()) {
			sb.append(cl);
		}
		return sb.toString();
	}

	public String showDetails() {
		StringBuilder sb = new StringBuilder();
		for (EvaluatedClass cl : values()) {
			sb.append(cl.showDetails()).append('\n');
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		if (size() == 0) {
			return "{}";
		}
		StringBuilder sb = new StringBuilder(size() << 2); // 4*size()
		sb.append("{");
		for (CratClass e : values()) {
			sb.append(e).append('|');
		}
		sb.setLength(sb.length() - 1); // delete last character
		sb.append("}");
		return sb.toString();
	}

	public int getNumOfClasses() {
		return size();
	}

	// internal interface and classes

	private interface ComparisonPolicy {
		public boolean compare(double value1, double value2);
	}

	private class BiggerValuesAreBetter implements ComparisonPolicy {
		@Override
		public boolean compare(double value1, double value2) {
			return ((value1 - value2) > 1e-10);
		}
	}

	private class SmallerValuesAreBetter implements ComparisonPolicy {
		@Override
		public boolean compare(double value1, double value2) {
			return ((value2 - value1) > 1e-10);
		}
	}

	private static final long serialVersionUID = 1L;

}