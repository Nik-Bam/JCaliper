package gr.uom.jcaliper.executor;

import gr.uom.jcaliper.heuristics.HeuristicFactory;
import gr.uom.jcaliper.plugin.Activator;
import gr.uom.jcaliper.preferences.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;

import java.util.TreeSet;

/**
 * @author Panagiotis Kouros
 */
public class OptimizingScenario {

    protected TreeSet<Integer> algorithmsForSystem = new TreeSet<Integer>();
    protected TreeSet<Integer> algorithmsForPackage = new TreeSet<Integer>();
    protected TreeSet<Integer> algorithmsForClass = new TreeSet<Integer>();

    public OptimizingScenario() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        if (store.getBoolean(Preferences.P_USE4SYS_HILL_CLIMBING_STEEPEST))
            algorithmsForSystem.add(HeuristicFactory.HILL_CLIMBING_STEEPEST);
        if (store.getBoolean(Preferences.P_USE4SYS_HILL_CLIMBING_FIRST_CHOICE))
            algorithmsForSystem.add(HeuristicFactory.HILL_CLIMBING_FIRST_CHOICE);
        if (store.getBoolean(Preferences.P_USE4SYS_TABU_SEARCH))
            algorithmsForSystem.add(HeuristicFactory.TABU_SEARCH);
        if (store.getBoolean(Preferences.P_USE4SYS_TABU_SEARCH_DYNAMIC))
            algorithmsForSystem.add(HeuristicFactory.TABU_SEARCH_DYNAMIC);
        if (store.getBoolean(Preferences.P_USE4SYS_SIMULATED_ANNEALING))
            algorithmsForSystem.add(HeuristicFactory.SIMULATED_ANNEALING);

        algorithmsForPackage.add(HeuristicFactory.HILL_CLIMBING_STEEPEST);
        algorithmsForPackage.add(HeuristicFactory.HILL_CLIMBING_FIRST_CHOICE);
        algorithmsForPackage.add(HeuristicFactory.TABU_SEARCH);
        algorithmsForPackage.add(HeuristicFactory.TABU_SEARCH_DYNAMIC);
        // algorithmsForPackage.add(HeuristicFactory.SIMULATED_ANNEALING);

        algorithmsForClass.add(HeuristicFactory.HILL_CLIMBING_STEEPEST);
        algorithmsForClass.add(HeuristicFactory.HILL_CLIMBING_FIRST_CHOICE);
        algorithmsForClass.add(HeuristicFactory.TABU_SEARCH);
        algorithmsForClass.add(HeuristicFactory.TABU_SEARCH_DYNAMIC);
        // algorithmsForClass.add(HeuristicFactory.SIMULATED_ANNEALING);
    }

    /**
     * @return the algorithms
     */
    public TreeSet<Integer> getAlgorithmsForSystem() {
        return algorithmsForSystem;
    }

    /**
     * @param algorithms the algorithms to set
     */
    public void setAlgorithmsForSystem(TreeSet<Integer> algorithms) {
        algorithmsForSystem = algorithms;
    }

    /**
     * @return the algorithmsForClass
     */
    public TreeSet<Integer> getAlgorithmsForClass() {
        return algorithmsForClass;
    }

    /**
     * @param algorithmsForClass the algorithmsForClass to set
     */
    public void setAlgorithmsForClass(TreeSet<Integer> algorithmsForClass) {
        this.algorithmsForClass = algorithmsForClass;
    }

    /**
     * @return the algorithmsForPackage
     */
    public TreeSet<Integer> getAlgorithmsForPackage() {
        return algorithmsForPackage;
    }

    /**
     * @param algorithmsForPackage the algorithmsForPackage to set
     */
    public void setAlgorithmsForPackage(TreeSet<Integer> algorithmsForPackage) {
        this.algorithmsForPackage = algorithmsForPackage;
    }

}
