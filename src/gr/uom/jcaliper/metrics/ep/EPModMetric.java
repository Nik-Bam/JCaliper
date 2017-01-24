/**
 *
 */
package gr.uom.jcaliper.metrics.ep;

import gr.uom.jcaliper.explorer.CraCase;
import gr.uom.jcaliper.preferences.Preferences;
import gr.uom.jcaliper.system.HashedClass;

/**
 * The Entity Placement metric
 *
 * @author Panagiotis Kouros
 */
public class EPModMetric extends EPMetric {

    public EPModMetric(CraCase craCase) {
        super(craCase);
        shortName = "EP(mod)";
        name = "Entity Placement (modified)";
        // TODO to complete information
        info = "Entity Placement metric...";
    }

    @Override
    protected void initializeCalculator() {
        if (craCase.isHybrid())
            calculation = new HybricCalculation();
        else
            calculation = new ClassicCalculation();
    }

    // modified implements for interface ICalculationMethod

    private final class ClassicCalculation implements ICalculation {
        @Override
        public EPCalculator getCalculator(HashedClass prototype, EPMetric metric) {
            return new EPModCalculatorClassic(prototype, metric);
        }
    }

    private final class HybricCalculation implements ICalculation {
        @Override
        public EPCalculator getCalculator(HashedClass prototype, EPMetric metric) {
            return new EPModCalculatorHybrid(prototype, metric);
        }
    }

    // Don't modify next line. Change the static value in class Preferences
    protected static final boolean PRINT_DEBUG_INFO = Preferences.PRINT_DEBUG_INFO;

}
