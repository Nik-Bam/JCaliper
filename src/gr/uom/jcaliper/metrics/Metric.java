/**
 * 
 */
package gr.uom.jcaliper.metrics;

import gr.uom.jcaliper.explorer.CraCase;
import gr.uom.jcaliper.explorer.CratState;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.HashedClass;

import java.util.TreeMap;

/**
 * The interface of all fitness metrics
 * 
 * @author Panagiotis Kouros
 */
public abstract class Metric {

	protected String name;
	protected String shortName;
	protected String info;
	protected CraCase craCase;

	protected EvaluatedClassPool storedClasses;
	protected TreeMap<Long, Double> storedValues = new TreeMap<Long, Double>();

	private int valuesHits = 0;
	private int valuesMisses = 0;
	private int classesHits = 0;
	private int classesMisses = 0;

	public Metric(CraCase craCase) {
		this.craCase = craCase;
		storedClasses = new EvaluatedClassPool(toBeMaximized());
		initializeCalculator();
		createEvaluatedEmpty();
		craCase.setInitial(new CratState(craCase, this));
	}

	// Methods to be overridden

	public abstract boolean toBeMaximized();

	protected abstract void initializeCalculator();

	protected abstract double calculateClassEvaluation(HashedClass hashed);

	protected abstract EvaluatedClass createEvaluatedClass(HashedClass hashed);

	// Concrete public methods

	public final Double evaluateClass(HashedClass hashed) {
		long hash = hashed.getHash();
		Double evaluation = getStoredValue(hash);
		if (evaluation == null) {
			evaluation = calculateClassEvaluation(hashed);
			storedValues.put(hash, evaluation);
		}
		return evaluation;
	}

	public final EvaluatedClass getEvaluatedClass(HashedClass hashed) {
		long hash = hashed.getHash();
		if (storedClasses.containsKey(hash)) {
			classesHits++;
			return (storedClasses.get(hash));
		} else {
			classesMisses++;
			EvaluatedClass evaluated = createEvaluatedClass(hashed);
			storedClasses.put(hash, evaluated);
			return evaluated;
		}
	}

	public final Double getStoredValue(long hash) {
		if (storedValues.containsKey(hash)) {
			valuesHits++;
			return storedValues.get(hash);
		} else {
			valuesMisses++;
			return null;
		}
	}

	public final void storeValue(long hash, double value) {
		storedValues.put(hash, value);
	}

	public final void clear() {
		if (storedValues != null)
			storedValues.clear();
	}

	@Override
	public void finalize() throws Throwable {
		clear();
		super.finalize();
	}

	// Some getters

	public final String getName() {
		return name;
	}

	public final String getShortName() {
		return shortName;
	}

	public final String getInfo() {
		return info;
	}

	public final CraCase getCraCase() {
		return craCase;
	}

	// Cache memory information

	public int getValueSearches() {
		return valuesHits + valuesMisses;
	}

	public int getClassSearches() {
		return classesHits + classesMisses;
	}

	// Cache memory presentation

	public String getCacheMemoryStatistics() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nCache memory statistics\n");
		sb.append("-----------------------\n");
		sb.append(getStoredClassesStatistics()).append("\n\n");
		sb.append(getStoredValuesStatistics()).append("\n\n");
		// sb.append(getTopEvaluatedClasses(20)).append("\n");
		return sb.toString();
	}

	public String getStoredValuesStatistics() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Stored Evaluations: %d\n", storedValues.size()));
		int calls = valuesHits + valuesMisses;
		double hitsPerc = (100.0 * valuesHits) / calls;
		double missesPerc = (100.0 * valuesMisses) / calls;
		sb.append(String.format("%d searches: %d Hits (%4.2f%%), %d Misses (%4.2f%%)", calls,
				valuesHits, hitsPerc, valuesMisses, missesPerc));
		return sb.toString();
	}

	public String getStoredClassesStatistics() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Stored Classes: %d\n", storedClasses.size()));
		int calls = classesHits + classesMisses;
		double hitsPerc = (100.0 * classesHits) / calls;
		double missesPerc = (100.0 * classesMisses) / calls;
		sb.append(String.format("%d searches: %d Hits (%4.2f%%), %d Misses (%4.2f%%)", calls,
				classesHits, hitsPerc, classesMisses, missesPerc));
		return sb.toString();
	}

	public String getTopEvaluatedClasses(int howMany) {
		return storedClasses.getTopEvaluatedClasses(howMany);
	}

	public String getEvaluatedClassesWithDetails() {
		return storedClasses.getTopEvaluatedClassesWithDetails();
	}

	// private methods

	private final void createEvaluatedEmpty() {
		EvaluatedClass empty = getEvaluatedClass(new HashedClass(new EntitySet()));
		for (int entityId : craCase.getEntitySet()) {
			HashedClass oneMember = new HashedClass(entityId);
			double evaluation = evaluateClass(oneMember);
			empty.entryGain.put(entityId, evaluation);
		}
	}

}
