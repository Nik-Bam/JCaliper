package gr.uom.jcaliper.metrics;

import gr.uom.jcaliper.system.HashedClass;

/**
 * @author Panagiotis Kouros
 */
public abstract class MetricCalculator extends EvaluatedClass {

	protected final Metric metric;

	protected MetricCalculator(HashedClass prototype, Metric metric) {
		super(prototype);
		this.metric = metric;
	}

	protected abstract void calculateClassEvaluation();

	protected abstract void calculateMoveGains();

	private static final long serialVersionUID = 1L;

}
