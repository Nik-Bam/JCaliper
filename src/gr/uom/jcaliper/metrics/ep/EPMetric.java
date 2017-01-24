/**
 *
 */
package gr.uom.jcaliper.metrics.ep;

import gr.uom.jcaliper.explorer.CraCase;
import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.metrics.Metric;
import gr.uom.jcaliper.preferences.Preferences;
import gr.uom.jcaliper.system.HashedClass;

/**
 * The Entity Placement metric
 *
 * @author Panagiotis Kouros
 */
public class EPMetric extends Metric {

    protected ICalculation calculation;

    public EPMetric(CraCase craCase) {
        super(craCase);
        shortName = "EP";
        name = "Entity Placement";
        // TODO to complete information
        info = "Entity Placement metric...";
    }

    @Override
    public boolean toBeMaximized() {
        return false;
    }

    @Override
    protected void initializeCalculator() {
        if (craCase.isHybrid())
            calculation = new HybricCalculation();
        else
            calculation = new ClassicCalculation();
    }

    @Override
    protected final double calculateClassEvaluation(HashedClass prototype) {
        EPCalculator calcClass = calculation.getCalculator(prototype, this);
        calcClass.calculateClassEvaluation();
        return calcClass.getEvaluation();
    }

    @Override
    protected final EvaluatedClass createEvaluatedClass(HashedClass prototype) {
        if (PRINT_DEBUG_INFO)
            System.out.format("Creating Evaluated Class: %s\n", prototype);
        EPCalculator calcClass = calculation.getCalculator(prototype, this);
        long hash = calcClass.getHash();
        Double storedValue = getStoredValue(hash);
        if (storedValue != null)
            calcClass.setEvaluation(storedValue);
        else {
            calcClass.calculateClassEvaluation();
            storedValues.put(hash, calcClass.getEvaluation());
        }
        calcClass.calculateMoveGains();
        return new EvaluatedClass(calcClass);
    }

    protected interface ICalculation {
        public EPCalculator getCalculator(HashedClass prototype, EPMetric metric);
    }

    private final class ClassicCalculation implements ICalculation {
        @Override
        public EPCalculator getCalculator(HashedClass prototype, EPMetric metric) {
            return new EPCalculatorClassic(prototype, metric);
        }
    }

    private final class HybricCalculation implements ICalculation {
        @Override
        public EPCalculator getCalculator(HashedClass prototype, EPMetric metric) {
            return new EPCalculatorHybrid(prototype, metric);
        }
    }

    // Don't modify next line. Change the static value in class Preferences
    protected static final boolean PRINT_DEBUG_INFO = Preferences.PRINT_DEBUG_INFO;

}
