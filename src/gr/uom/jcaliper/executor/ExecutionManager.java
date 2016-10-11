package gr.uom.jcaliper.executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.IPreferenceStore;

import gr.uom.jcaliper.explorer.CraCase;
import gr.uom.jcaliper.explorer.CratExplorer;
import gr.uom.jcaliper.explorer.CratState;
import gr.uom.jcaliper.filters.SingleEntityClassFilter;
import gr.uom.jcaliper.heuristics.HeuristicFactory;
import gr.uom.jcaliper.heuristics.IOptimaPool;
import gr.uom.jcaliper.heuristics.SearchAlgorithm;
import gr.uom.jcaliper.loggers.ActivityLogger;
import gr.uom.jcaliper.loggers.MoveLogger;
import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.metrics.Metric;
import gr.uom.jcaliper.metrics.MetricFactory;
import gr.uom.jcaliper.plugin.Activator;
import gr.uom.jcaliper.plugin.ResultsTable;
import gr.uom.jcaliper.preferences.Preferences;
import gr.uom.jcaliper.refactoring.LocalOptimum;
import gr.uom.jcaliper.refactoring.LocalOptimumCreator;
import gr.uom.jcaliper.refactoring.RefactoringGroup;
import gr.uom.jcaliper.system.CratClass;
import gr.uom.jcaliper.system.CratSystem;
import gr.uom.jcaliper.system.HashedClass;

/**
 * @author Panagiotis Kouros
 */
public class ExecutionManager {

	// Singleton
	private static ExecutionManager INSTANCE;

	private CratSystem system;
	private CraCase craCase;
	private Metric metric;
	private IOptimaPool pool = null;
	private MoveLogger moveLogger = null;
	private Logger activityLogger = null;
	private CratExplorer explorer;
	private CratState initial;
	private OptimizingScenario scenario;

	private ArrayList<ExecutionSummary> results;
	private ResultsTable resultsTable;
	private long startTime;
	private long stopTime;
	private Logger resultLogger;
	private int progressNom, progressDenom;

	// private Logger moveLogger;

	private ExecutionManager() {
		super();
		updatePreferences();
		activityLogger = ActivityLogger.getInstance().getLogger();
		if (LOG_RESULTS) {
			initializeResultLogger();
		}
	}

	public static ExecutionManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ExecutionManager();
		}
		return INSTANCE;
	}

	public static ExecutionManager getInstance(CratSystem system) {
		if (INSTANCE == null) {
			INSTANCE = new ExecutionManager();
		}
		INSTANCE.setSystem(system);
		return INSTANCE;
	}

	/**
	 * @return the results
	 */
	public ArrayList<ExecutionSummary> getResults() {
		if (results == null) {
			results = new ArrayList<ExecutionSummary>();
		}
		return results;
	}

	public void clearResults() {
		results = new ArrayList<ExecutionSummary>();
	}

	public ArrayList<ExecutionSummary> runHeuristics() {
		updatePreferences();
		scenario = new OptimizingScenario();
		// results = new ArrayList<ExecutionSummary>();
		if (resultsTable != null) {
			resultsTable.updateViewer();
		}

		if (LOG_RESULTS) {
			moveLogger = new MoveLogger(craCase.getName(), craCase.getInitial().getEvaluation());
			// logger.logResults(craCase.getPresentation());
			moveLogger.logResults(craCase.shortDescription() + "\n");
		}
		// info(craCase.getPresentation);
		info(craCase.shortDescription());

		// Pre-optimization of initial state
		if (DO_PREOPTIMIZE) {
			info("\nPreoptimization\n---------------\n");
			if (craCase.getCaseType() == CraCase.CRACASE_PACKAGE) {
				progressNom = 0;
				progressDenom = craCase.getNumOfClasses();
				initial = preoptimizeClasses(craCase.getClasses());
			} else {
				progressNom = 0;
				progressDenom = craCase.getNumOfClasses() + craCase.getNumOfPackages();
				initial = preoptimizePackages();
			}
		}

		// Main heuristics run (applied on system)
		optimizeSystem();

		if (LOG_RESULTS) {
			moveLogger.logResults(metric.getCacheMemoryStatistics() + "\n" + explorer.getNavigatorStatistics());
			moveLogger.flushAll();
		}
		info("\n" + metric.getCacheMemoryStatistics());
		// info(explorer.getNavigatorStatistics() + "\n");
		if (resultsTable != null) {
			resultsTable.updateViewer();
		}
		if (explorer != null) {
			explorer.clear();
		}
		return results;
	}

	private void setSystem(CratSystem system) {
		if (explorer != null) {
			explorer.clear();
		}
		this.system = system;
		craCase = new CraCase(system, CASE_TYPE);
		metric = MetricFactory.getMetric(SELECTED_METRIC, craCase);
		explorer = new CratExplorer(craCase, metric);
		initial = craCase.getInitial();
	}

	private CratState preoptimizeClasses(Map<Integer, ? extends CratClass> classSet) {
		// start with an empty class collection
		ArrayList<EvaluatedClass> optimized = new ArrayList<EvaluatedClass>();
		// preoptimize classes
		if (classSet.size() > 0) {
			for (Map.Entry<Integer, ? extends CratClass> entry : classSet.entrySet()) {
				int classId = entry.getKey();
				CratClass curClass = entry.getValue();
				String className = craCase.getClassName(classId);
				info(String.format("[%4.1f%%] preoptimizing class %s (%d entities) ",
						(100.0 * progressNom) / progressDenom, className, curClass.size()));
				// Don't deconstruct class
				// CratState curState = new CratState(curClass, metric);
				// Deconstruct class
				CratState curState = stateFromClass(curClass);
				double curEvaluation = curState.getEvaluation();
				CratState bestOptimum = curState;
				for (int methodId : scenario.getAlgorithmsForClass()) {
					SearchAlgorithm heuristic = HeuristicFactory.getHeuristic(methodId, explorer, curState, null, null);
					// info(String.format("%s..", heuristic.getShortName()));
					CratState optimum = (CratState) heuristic.getOptimum();
					// info(String.format(": %+8.6f %s\n",
					// optimum.getEvaluation() - curEvaluation,
					// optimum));
					if (optimum.isBetterThan(bestOptimum.getEvaluation())) {
						bestOptimum = optimum;
					}
				}
				info(String.format(": %+8.6f\n", bestOptimum.getEvaluation() - curEvaluation));
				optimized.addAll(bestOptimum.values());
				progressNom++;
			}
		}
		return new CratState(optimized, metric);
	}

	private CratState stateFromClass(CratClass cl) {
		ArrayList<EvaluatedClass> deconstructed = new ArrayList<EvaluatedClass>();
		CratClass clClone = new CratClass(cl);

		for (int entId : cl) {
			if (craCase.getEntity(entId).isMovable()) {
				HashedClass hashed = new HashedClass(new CratClass(entId));
				EvaluatedClass evaluated = metric.getEvaluatedClass(hashed);
				deconstructed.add(evaluated);
				clClone.remove(entId);
			}
		}
		if (clClone.size() != 0) {
			HashedClass hashed = new HashedClass(clClone);
			EvaluatedClass evaluated = metric.getEvaluatedClass(hashed);
			deconstructed.add(evaluated);
		}
		return new CratState(deconstructed, metric);
	}

	private CratState preoptimizePackage(String packageName, TreeMap<Integer, CratClass> packageClasses) {
		CratState packageInitial = preoptimizeClasses(packageClasses);
		if (packageInitial.size() == 0) {
			return packageInitial;
		}
		info(String.format("[%4.1f%%] preoptimizing package %s...", (100.0 * progressNom) / progressDenom,
				packageName));
		double curEvaluation = packageInitial.getEvaluation();
		CratState bestOptimum = packageInitial;
		for (int methodId : scenario.getAlgorithmsForPackage()) {
			SearchAlgorithm heuristic = HeuristicFactory.getHeuristic(methodId, explorer, packageInitial, null, null);
			// info(String.format("%s..", heuristic.getShortName()));
			CratState optimum = (CratState) heuristic.getOptimum();
			// info(String.format(": %+8.6f %s\n", optimum.getEvaluation() -
			// curEvaluation,
			// optimum));
			if (optimum.isBetterThan(bestOptimum.getEvaluation())) {
				bestOptimum = optimum;
			}
		}
		info(String.format(": %+8.6f\n", bestOptimum.getEvaluation() - curEvaluation));
		progressNom++;
		return bestOptimum;
	}

	private CratState preoptimizePackages() {
		// start with an empty class collection
		ArrayList<EvaluatedClass> optimized = new ArrayList<EvaluatedClass>();
		TreeMap<Integer, CratClass> packageClasses = new TreeMap<Integer, CratClass>();
		String currentPackage = "";
		for (Map.Entry<Integer, ? extends CratClass> entry : craCase.getClasses().entrySet()) {
			int classId = entry.getKey();
			CratClass curClass = entry.getValue();
			String classPackage = craCase.getPackageNameOf(classId);
			if (!currentPackage.equals(classPackage)) {
				CratState packageOptimized = preoptimizePackage(currentPackage, packageClasses);
				optimized.addAll(packageOptimized.values());
				packageClasses = new TreeMap<Integer, CratClass>();
				currentPackage = classPackage;
			}
			packageClasses.put(classId, curClass);
		}
		// preoptimize last package
		CratState packageOptimized = preoptimizePackage(currentPackage, packageClasses);
		optimized.addAll(packageOptimized.values());
		return new CratState(optimized, metric);
	}

	private void optimizeSystem() {
		info("\n System optimization\n-------------------");
		CratState bestOfAll = craCase.getInitial();
		String bestAlgorithm = "";
		SingleEntityClassFilter classFilter = new SingleEntityClassFilter();

		for (int methodId : scenario.getAlgorithmsForSystem()) {
			CratState starting;
			if (methodId == HeuristicFactory.SIMULATED_ANNEALING) {
				starting = craCase.getInitial();
			} else {
				starting = initial;
			}

			SearchAlgorithm heuristic = HeuristicFactory.getHeuristic(methodId, explorer, starting, pool, moveLogger);

			// Record execution results and performance metrics
			ExecutionSummary curExecSum = new ExecutionSummary(craCase);
			curExecSum.setRunning(true);
			curExecSum.setHeuristicName(heuristic.getName());
			curExecSum.setHeuristicShortName(heuristic.getShortName());
			addResult(curExecSum);
			if (resultsTable != null) {
				resultsTable.updateViewer();
			}

			// Run heuristic
			int valueSearchesBefore = metric.getValueSearches();
			startTimer();
			CratState best = (CratState) heuristic.getOptimum();

			classFilter.applyFilter(best, system);		// TODO paidaki - Filter the optimum with custom filters
			stopTimer();
			// TODO correct calculation of evaluation and moves
			int valueSearchesAfter = metric.getValueSearches();
			int totalMoves = heuristic.getNumOfPerfomedMoves();
			int statesEvaluated = ((valueSearchesAfter - valueSearchesBefore) / 2) + totalMoves;

			LocalOptimumCreator locOptCreator = new LocalOptimumCreator(system);
			LocalOptimum global = locOptCreator.createLocalOptimum(best);

			groupRefactorings(global);		// paidaki

			curExecSum.setRefactored(global);
			curExecSum.setRunningTime(getRunningTime());
			curExecSum.setStatesEvaluated(statesEvaluated);
			curExecSum.setTotalMoves(totalMoves);
			curExecSum.setRunning(false);

			if (resultsTable != null) {
				resultsTable.updateViewer();
			}

			// info(curExecSum.getFullPresentation());
			// info("\n" + curExecSum.getOneLinePresentation());
			if (LOG_RESULTS) {
				moveLogger.logResults(curExecSum.detailedDescription());
				resultLogger.info(curExecSum.oneLineDescription() + craCase.getName());
			}
			if (best.isBetterThan(bestOfAll.getEvaluation())) {
				bestOfAll = best;
				bestAlgorithm = heuristic.getShortName();
			}
		}
		if (LOG_RESULTS) {
			moveLogger.logResults(
					String.format("(one of) best is %s : %8.6f\n", bestAlgorithm, bestOfAll.getEvaluation()));
		}
	}

	private void groupRefactorings(LocalOptimum optimum) {
		List<RefactoringGroup> groups = new ArrayList<>();

		optimum.getMoveEntityRefs().stream()
				.collect(Collectors.groupingBy(e -> e.getOrigin().getId(),
						Collectors.groupingBy(e -> e.getTarget().getOrigin().getId())))
				.values().stream()
				.map(item -> item.values().stream().map(RefactoringGroup::new).collect(Collectors.toList()))
				.forEach(groups::addAll);

		optimum.getCustomRefs().addAll(groups);
		optimum.getCustomRefs().addAll(optimum.getExtractClassRefs());
	}

	private void info(String message) {
		activityLogger.log(Level.INFO, message);
	}

	public void initializeResultLogger() {
		updatePreferences();
		resultLogger = Logger.getLogger("jcaliper.results");
		FileHandler fh;
		try {
			// This block configures the logger with handler and formatter
			fh = new FileHandler(LOG_RESULTS_FILE, true); // append
			resultLogger.addHandler(fh);
			resultLogger.setLevel(Level.ALL);
			MyTinyFormatter formatter = new MyTinyFormatter();
			fh.setFormatter(formatter);
			for (Handler iHandler : resultLogger.getParent().getHandlers()) {
				resultLogger.getParent().removeHandler(iHandler);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class MyTinyFormatter extends Formatter {
		@Override
		public String format(final LogRecord r) {
			return r.getMessage() + '\n';
		}
	}

	public CratSystem getSystem() {
		return system;
	}

	public CratExplorer getExplorer() {
		return explorer;
	}

	/**
	 * @param table
	 *            the resultTable to set
	 */
	public void setResultsTable(ResultsTable table) {
		resultsTable = table;
	}

	public double getRunningTime() {
		return (stopTime - startTime) / 1000.0;
	}

	private void startTimer() {
		startTime = System.currentTimeMillis();
	};

	private void stopTimer() {
		stopTime = System.currentTimeMillis();
	}

	public void addResult(ExecutionSummary result) {
		results.add(result);
		if (resultsTable != null) {
			resultsTable.updateViewer();
		}
	}

	private void updatePreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		DO_PREOPTIMIZE = store.getBoolean(Preferences.P_DO_PREOPTIMIZE);
		boolean new_LOG_RESULTS = store.getBoolean(Preferences.P_LOG_RESULTS);
		File new_LOG_RESULTS_FILE = new File(store.getString(Preferences.P_LOG_RESULTS_FILE));
		if (new_LOG_RESULTS && new_LOG_RESULTS_FILE.exists()) {
			if (!LOG_RESULTS || !(LOG_RESULTS_FILE.equals(new_LOG_RESULTS_FILE.getAbsolutePath()))) {
				LOG_RESULTS = new_LOG_RESULTS;
				LOG_RESULTS_FILE = new_LOG_RESULTS_FILE.getAbsolutePath();
				initializeResultLogger();
			}
		} else {
			new_LOG_RESULTS = false;
			store.setToDefault(Preferences.P_LOG_RESULTS);
			store.setToDefault(Preferences.P_LOG_PATH);
			store.setToDefault(Preferences.P_LOG_RESULTS_FILE);
		}
		LOG_RESULTS = new_LOG_RESULTS;
		LOG_RESULTS_FILE = new_LOG_RESULTS_FILE.getAbsolutePath();
	}

	// Don't modify next lines. Change the static values in class Preferences
	private static final int CASE_TYPE = Preferences.CASE_TYPE;
	private static final int SELECTED_METRIC = Preferences.SELECTED_METRIC;
	private static boolean DO_PREOPTIMIZE;
	private static boolean LOG_RESULTS = false;
	private static String LOG_RESULTS_FILE = "";

}
