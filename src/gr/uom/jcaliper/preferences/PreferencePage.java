package gr.uom.jcaliper.preferences;

import gr.uom.jcaliper.plugin.Activator;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.io.File;
import java.io.IOException;

/**
 * @author Panagiotis Kouros
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor useForSysHCSteepestEditor;
    private BooleanFieldEditor useForSysHCFirstChoiceEditor;
    private BooleanFieldEditor useForSysHCTabuSearchEditor;
    private BooleanFieldEditor useForSysHCTabuSearchDynEditor;
    private BooleanFieldEditor useForSysHCSimAnnealingEditor;
    private BooleanFieldEditor doPreoptimizeEditor;
    private BooleanFieldEditor logResultsEditor;
    private DirectoryFieldEditor logPathEditor;
    private FileFieldEditor logResultsFileEditor;
    private BooleanFieldEditor limitTimeEditor;
    private IntegerFieldEditor maxRunningTimeEditor;

    public PreferencePage() {
        super(GRID);
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    @Override
    public void createFieldEditors() {

        useForSysHCSteepestEditor = new BooleanFieldEditor(Preferences.P_USE4SYS_HILL_CLIMBING_STEEPEST,
                "Use &Hill Climbing - Steepest Asc./Descent", getFieldEditorParent());
        addField(useForSysHCSteepestEditor);

        useForSysHCFirstChoiceEditor = new BooleanFieldEditor(Preferences.P_USE4SYS_HILL_CLIMBING_FIRST_CHOICE,
                "Use Hill Climbing - &First Choice", getFieldEditorParent());
        addField(useForSysHCFirstChoiceEditor);

        useForSysHCTabuSearchEditor = new BooleanFieldEditor(Preferences.P_USE4SYS_TABU_SEARCH,
                "Use &Tabu Search w Static Tenure", getFieldEditorParent());
        addField(useForSysHCTabuSearchEditor);

        useForSysHCTabuSearchDynEditor = new BooleanFieldEditor(Preferences.P_USE4SYS_TABU_SEARCH_DYNAMIC,
                "Use Tabu Search w &Dynamic Tenure", getFieldEditorParent());
        addField(useForSysHCTabuSearchDynEditor);

        useForSysHCSimAnnealingEditor = new BooleanFieldEditor(Preferences.P_USE4SYS_SIMULATED_ANNEALING,
                "Use &Simulated Annealing", getFieldEditorParent());
        addField(useForSysHCSimAnnealingEditor);

        doPreoptimizeEditor = new BooleanFieldEditor(Preferences.P_DO_PREOPTIMIZE,
                "&Pre-optimize at class/package level", getFieldEditorParent());
        addField(doPreoptimizeEditor);

        logResultsEditor = new BooleanFieldEditor(Preferences.P_LOG_RESULTS, "&Log searching moves and results",
                getFieldEditorParent());
        addField(logResultsEditor);

        logPathEditor = new DirectoryFieldEditor(Preferences.P_LOG_PATH, "Moves log path:", getFieldEditorParent());
        logPathEditor.setEnabled(Activator.getDefault().getPreferenceStore().getBoolean(Preferences.P_LOG_RESULTS),
                getFieldEditorParent());
        logPathEditor.setEmptyStringAllowed(true);
        addField(logPathEditor);

        logResultsFileEditor = new FileFieldEditor(Preferences.P_LOG_RESULTS_FILE, "Results log file:",
                getFieldEditorParent());
        logResultsFileEditor.setEnabled(
                Activator.getDefault().getPreferenceStore().getBoolean(Preferences.P_LOG_RESULTS),
                getFieldEditorParent());
        logResultsFileEditor.setEmptyStringAllowed(true);
        addField(logResultsFileEditor);

        limitTimeEditor = new BooleanFieldEditor(Preferences.P_SEARCH_LIMIT_TIME, "&Limit running time",
                getFieldEditorParent());
        addField(limitTimeEditor);

        maxRunningTimeEditor = new IntegerFieldEditor(Preferences.P_SEARCH_MAX_RUNNING_TIME,
                "Ma&x time per algorithm (sec)", getFieldEditorParent());
        maxRunningTimeEditor.setEnabled(
                Activator.getDefault().getPreferenceStore().getBoolean(Preferences.P_SEARCH_LIMIT_TIME),
                getFieldEditorParent());
        maxRunningTimeEditor.setEmptyStringAllowed(false);
        addField(maxRunningTimeEditor);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)
                && ((event.getSource() == logResultsEditor) || (event.getSource() == limitTimeEditor))) {
            updateDependentFields();
        }
    }

    private void updateDependentFields() {
        logPathEditor.setEmptyStringAllowed(!logResultsEditor.getBooleanValue());
        logResultsFileEditor.setEmptyStringAllowed(!logResultsEditor.getBooleanValue());

        logPathEditor.setEnabled(logResultsEditor.getBooleanValue(), getFieldEditorParent());
        logResultsFileEditor.setEnabled(logResultsEditor.getBooleanValue(), getFieldEditorParent());
        maxRunningTimeEditor.setEnabled(limitTimeEditor.getBooleanValue(), getFieldEditorParent());

        if (logResultsEditor.getBooleanValue()) {
            if (!logPathEditor.isValid() || !logResultsFileEditor.isValid() || logPathEditor.getStringValue().isEmpty()
                    || logResultsFileEditor.getStringValue().isEmpty()) {
                createDirectories();
            }
        } else {
            if (!logPathEditor.isValid() || !logResultsFileEditor.isValid()) {
                logPathEditor.loadDefault();
                logResultsFileEditor.loadDefault();
            }
        }
    }

    private void createDirectories() {
        String logDir = System.getProperty("user.home") + File.separator + "JCaliper";
        new File(logDir).mkdirs();

        File logFile = new File(logDir + File.separator + "JCaliper_Results.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logPathEditor.setStringValue(logDir);
        logResultsFileEditor.setStringValue(logFile.getAbsolutePath());
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        updateDependentFields();
    }

    ;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Class Responsibility Assignment Tool Preferences");
    }
}
