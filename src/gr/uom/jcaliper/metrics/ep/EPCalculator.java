package gr.uom.jcaliper.metrics.ep;

import gr.uom.jcaliper.metrics.Metric;
import gr.uom.jcaliper.metrics.MetricCalculator;
import gr.uom.jcaliper.preferences.Preferences;
import gr.uom.jcaliper.system.CratEntity;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.HashedClass;
import gr.uom.jcaliper.system.IEntityPool;

/**
 * Data structures designed to speed up Entity Placement calculation
 * 
 * @author Panagiotis Kouros
 */
public abstract class EPCalculator extends MetricCalculator {

	protected final IEntityPool entities;
	protected final int nEntities;

	protected EntitySet internalRelatives;
	protected EntitySet externalRelatives;
	protected EntitySet welcome = new EntitySet();

	protected EPCalculator(HashedClass prototype, Metric metric) {
		super(prototype, metric);
		entities = metric.getCraCase();
		nEntities = metric.getCraCase().getTotalEntities();
		initializeRelatives();
	}

	// Implemented methods

	@Override
	protected void calculateClassEvaluation() {
		evaluation = calculateClassEvaluation(this, internalRelatives, externalRelatives);
	}

	@Override
	protected void calculateMoveGains() {
		initializeWelcome();
		for (int entId : exitCandidates())
			exitGain.put(entId, exitGainOf(entId));
		for (int entId : entryCandidates())
			entryGain.put(entId, entryGainOf(entId));
	}

	// Abstract methods

	protected abstract void initializeWelcome();

	protected abstract double calculateClassEvaluation(EntitySet theClass, EntitySet intRelatives,
			EntitySet extRelatives);

	// EntitySets: Relatives and Welcome

	private final void initializeRelatives() {
		EntitySet relatives = new EntitySet();
		for (int entityId : this) {
			CratEntity entity = entities.getEntity(entityId);
			relatives.addAll(entity.getRelatives());
		}
		internalRelatives = intersection(relatives);
		externalRelatives = relatives.difference(internalRelatives);
	}

	// Entities candidate to be moved

	private final EntitySet exitCandidates() {
		EntitySet candidates = new EntitySet();
		for (int entId : this)
			if (entities.getEntity(entId).isMovable())
				candidates.add(entId);
		return candidates;
	}

	private final EntitySet entryCandidates() {
		EntitySet candidates = new EntitySet();
		for (int entId : welcome)
			if (entities.getEntity(entId).isMovable())
				candidates.add(entId);
		return candidates;
	}

	// Concrete calculation methods

	protected final double innerDistancesTotal(EntitySet theClass, EntitySet intRelatives) {
		int classSize = theClass.size() - 1; // exclude the measured entity
		double innerSimilaritiesTotal = 0.0;
		for (int entId : intRelatives)
			innerSimilaritiesTotal += similarity(entId, theClass, classSize);
		double innerDistancesTotal = theClass.size() - innerSimilaritiesTotal;
		if (PRINT_DEBUG_INFO)
			System.out.format("\t\tInnerDistance = (%d-%8.6f)/%d = %8.6f\n", theClass.size(),
					innerSimilaritiesTotal, theClass.size(), innerDistancesTotal / theClass.size());
		return innerDistancesTotal;
	}

	protected final double outerDistancesTotal(EntitySet theClass, EntitySet extRelatives,
			int nExternal) {
		int classSize = theClass.size(); // the full class size
		double outerSimilaritiesTotal = 0.0;
		for (int entId : extRelatives)
			outerSimilaritiesTotal += similarity(entId, theClass, classSize);
		double outerDistancesTotal = nExternal - outerSimilaritiesTotal;
		if (outerDistancesTotal < 1e-10) // if is 0
			outerDistancesTotal = 1.0 / nEntities; // set it to any too small number
		if (PRINT_DEBUG_INFO)
			System.out.format("\t\tOuterDistance = (%d-%8.6f)/%d = %8.6f\n", nExternal,
					outerSimilaritiesTotal, nExternal, outerDistancesTotal / nExternal);
		return outerDistancesTotal;
	}

	protected final double similarity(int entId, EntitySet theClass, int classSize) {
		EntitySet entitySet = entities.getEntity(entId).getEntitySet();
		int intersectionSize = theClass.intersection(entitySet).size();
		if (intersectionSize == 0)
			return 0.0;
		int unionSize = (classSize + entitySet.size()) - intersectionSize;
		double similarity = ((double) intersectionSize) / unionSize;
		if (PRINT_DEBUG_INFO)
			System.out.format("\t\t\tsim(%d,%s) = %d/%d = %8.6f\t\tS(%d)=%s\n", entId, theClass,
					intersectionSize, unionSize, similarity, entId, entitySet);
		return similarity;
	}

	private final double exitGainOf(int moving) {
		if (PRINT_DEBUG_INFO)
			System.out.format("\t\tcalculating exit gain of %d...\n", moving);
		// Construct the resulting new class
		EntitySet newClass = without(moving);
		long hash = newClass.calculateHash();
		// Try to get a known class evaluation
		Double newEvaluation = metric.getStoredValue(hash);
		// If not available, calculate and store it
		if (newEvaluation == null) {
			EntitySet newIntRelatives = internalRelatives.without(moving);
			EntitySet newExtRelatives = externalRelatives.plus(moving);
			newEvaluation = calculateClassEvaluation(newClass, newIntRelatives, newExtRelatives);
			metric.storeValue(hash, newEvaluation);
		}
		if (PRINT_DEBUG_INFO)
			System.out.format("\t\texit gain of %d = %8.6f\n", moving, newEvaluation - evaluation);
		return (newEvaluation - evaluation);
	}

	private final double entryGainOf(int moving) {
		if (PRINT_DEBUG_INFO)
			System.out.format("\t\tcalculating entry gain of %d...\n", moving);
		// Construct the resulting new class
		EntitySet newClass = plus(moving);
		long hash = newClass.calculateHash();
		// Try to get a known class evaluation
		Double newEvaluation = metric.getStoredValue(hash);
		// If not available, calculate and store it
		if (newEvaluation == null) {
			EntitySet entityRelatives = entities.getEntity(moving).getRelatives();
			EntitySet newIntRelatives = entityRelatives.intersection(newClass);
			newIntRelatives.addAll(internalRelatives);
			newIntRelatives.add(moving);
			EntitySet newExtRelatives = entityRelatives.difference(newClass);
			newExtRelatives.addAll(externalRelatives);
			newExtRelatives.remove(moving);
			newEvaluation = calculateClassEvaluation(newClass, newIntRelatives, newExtRelatives);
			metric.storeValue(hash, newEvaluation);
		}
		if (PRINT_DEBUG_INFO)
			System.out.format("\t\tentry gain of %d = %8.6f\n", moving, newEvaluation - evaluation);
		return (newEvaluation - evaluation);
	}

	// Don't modify next line. Change the static value in class Preferences
	private static final boolean PRINT_DEBUG_INFO = Preferences.PRINT_DEBUG_INFO;

	private static final long serialVersionUID = 1L;

}
