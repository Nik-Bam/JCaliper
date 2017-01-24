package gr.uom.jcaliper.metrics.ep;

import gr.uom.jcaliper.preferences.Preferences;
import gr.uom.jcaliper.system.CratEntity;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.HashedClass;

/**
 * Data structures designed to speed up Entity Placement calculation
 *
 * @author Panagiotis Kouros
 */
public class EPModCalculatorHybrid extends EPCalculator {

    protected EPModCalculatorHybrid(HashedClass prototype, EPMetric metric) {
        super(prototype, metric);
    }

    // EntitySets: overridden method for Welcome

    @Override
    protected void initializeWelcome() {
        EntitySet forbidden = new EntitySet();
        for (int entityId : this) {
            CratEntity entity = entities.getEntity(entityId);
            welcome.addAll(entity.getEntitySet());
            forbidden.addAll(entity.getForbiddenClassmates());
        }
        // hybrid system: welcome must be boxed
        welcome = welcome.boxed(entities);
        welcome.removeAll(this);
        welcome.addAll(externalRelatives);
        welcome.removeAll(forbidden);
    }

    // Overridden calculation methods

    @Override
    protected double calculateClassEvaluation(EntitySet theClass, EntitySet intRelatives,
                                              EntitySet extRelatives) {
        if (PRINT_DEBUG_INFO)
            System.out.format("\tEvaluating Class: %s\n", theClass);
        // hybrid: all sets are sent to calculation methods unboxed
        EntitySet unboxed = theClass.unbox(entities);
        double innerDistancesTotal = innerDistancesTotal(unboxed, intRelatives.unbox(entities));
        double result = innerDistancesTotal / nEntities;
        EntitySet extRelativesUnboxed = extRelatives.unbox(entities);
        // The EP_MOD modification
        // (for EP is: nExternal = nEntities - unboxed.size();)
        int nExternal = extRelativesUnboxed.size();
        if (nExternal > 0)
            result *= nExternal / outerDistancesTotal(unboxed, extRelativesUnboxed, nExternal);
        if (PRINT_DEBUG_INFO)
            System.out.format("\t\tEvaluation = %8.6f\t(EP=%8.6f)\n", result,
                    (unboxed.size() > 0) ? (result * nEntities) / unboxed.size() : 0);
        return result;
    }

    private static final long serialVersionUID = 1L;

    // Don't modify next line. Change the static value in class Preferences
    private static final boolean PRINT_DEBUG_INFO = Preferences.PRINT_DEBUG_INFO;

}
