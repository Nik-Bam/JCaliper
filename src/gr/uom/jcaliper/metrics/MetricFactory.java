package gr.uom.jcaliper.metrics;

import gr.uom.jcaliper.explorer.CraCase;
import gr.uom.jcaliper.metrics.ep.EPMetric;
import gr.uom.jcaliper.metrics.ep.EPModMetric;

/**
 * @author Panagiotis Kouros
 */
public class MetricFactory {

	// Supported metrics
	public static final int EP = 1;
	public static final int EP_MOD = 2;

	public static Metric getMetric(int metricId, CraCase craCase) {
		switch (metricId) {
		case EP:
			return new EPMetric(craCase);
		case EP_MOD:
			return new EPModMetric(craCase);
		default:
			return null;
		}
	}

}
