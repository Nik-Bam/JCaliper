package gr.uom.jcaliper.preferences;

import gr.uom.jcaliper.plugin.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 *
 * @author Panagiotis Kouros
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        store.setDefault(Preferences.P_LOG_RESULTS, false);

        store.setDefault(Preferences.P_LOG_PATH, "");
        store.setDefault(Preferences.P_LOG_RESULTS_FILE, "");

        store.setDefault(Preferences.P_DO_PREOPTIMIZE, true);
        store.setDefault(Preferences.P_USE4SYS_HILL_CLIMBING_STEEPEST, true);
        store.setDefault(Preferences.P_USE4SYS_HILL_CLIMBING_FIRST_CHOICE, true);
        store.setDefault(Preferences.P_USE4SYS_TABU_SEARCH, true);
        store.setDefault(Preferences.P_USE4SYS_TABU_SEARCH_DYNAMIC, true);
        store.setDefault(Preferences.P_USE4SYS_SIMULATED_ANNEALING, false);

        store.setDefault(Preferences.P_SEARCH_LIMIT_TIME, false);
        store.setDefault(Preferences.P_HC_MAX_CLIMBING_TIMES, 10);
        store.setDefault(Preferences.P_SEARCH_MAX_RUNNING_TIME, 600);

    }

}
