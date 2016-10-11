package gr.uom.jcaliper.loggers;

import gr.uom.jcaliper.heuristics.HeuristicFactory;
import gr.uom.jcaliper.heuristics.IMove;
import gr.uom.jcaliper.heuristics.IProblemState;
import gr.uom.jcaliper.heuristics.ISearchLogger;
import gr.uom.jcaliper.heuristics.SearchAlgorithm;
import gr.uom.jcaliper.plugin.Activator;
import gr.uom.jcaliper.preferences.Preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Panagiotis Kouros
 */
public class MoveLogger implements ISearchLogger {

	private static final String whereRuns = "at_i7";

	private String logPath;
	private ISearchLogger nextLogger = null;
	private BufferedWriter logORG;
	private BufferedWriter logHCS;
	private BufferedWriter logHCF;
	private BufferedWriter logTS;
	private BufferedWriter logTSD;
	private BufferedWriter logSA;
	private BufferedWriter logOPT;
	private BufferedWriter logLAB;
	private BufferedWriter logRES;
	private BufferedWriter logWriter = null;

	public MoveLogger() {
		Date now = new Date(System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
		logPath = getLogDirectory() + "//" + whereRuns + "_" + df.format(now);
		initializeLogFiles();
		logOriginal("0,000000 (unnamed)");
	}

	public MoveLogger(String projectName, double initialFittness) {
		Date now = new Date(System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
		logPath = getLogDirectory() + "//" + whereRuns + "_" + projectName + "_" + df.format(now);
		initializeLogFiles();
		logOriginal(String.format("%10.8f\t%s\n", initialFittness, projectName));
	}

	private void initializeLogFiles() {
		logORG = createLogFile("ORIG.dat");
		logHCS = createLogFile("HCS.dat");
		logHCF = createLogFile("HCF.dat");
		logTS = createLogFile("TS.dat");
		logTSD = createLogFile("TSD.dat");
		logSA = createLogFile("SA.dat");
		logOPT = createLogFile("OPT.dat");
		logLAB = createLogFile("LAB.dat");
		logRES = createLogFile("RESULTS.txt");
	}

	private BufferedWriter createLogFile(String fileName) {
		BufferedWriter bw = null;
		try {
			File dir = new File(logPath);
			if (!dir.exists() && !dir.mkdirs())
				System.err.format("Unable to create %s\n", dir.getAbsolutePath());
			File file = new File(dir, fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bw;
	}

	private void logOriginal(String message) {
		if (logORG != null)
			try {
				logORG.write(message);
			} catch (IOException e) {
				System.err.println("Unable to write to ORG file.");
				e.printStackTrace();
			}
	}

	public void logResults(String message) {
		final String separator = String.format("\n%050d\n\n", 0).replace('0', '=');
		if (logRES != null)
			try {
				logRES.write(message);
				logRES.write(separator);
			} catch (IOException e) {
				System.err.println("Unable to write to RES file.");
				e.printStackTrace();
			}
	}

	private void logWrite(String message) {
		if (logWriter != null)
			try {
				logWriter.write(message);
			} catch (IOException e) {
				System.err.println("Unable to write to log file.");
				e.printStackTrace();
			}
	}

	@Override
	public void logStart(SearchAlgorithm algorithm, IProblemState startingState, int moveId,
			long time) {
		if (algorithm.getAlgorithmId() == HeuristicFactory.HILL_CLIMBING_FIRST_CHOICE)
			logWriter = logHCF;
		else if (algorithm.getAlgorithmId() == HeuristicFactory.HILL_CLIMBING_STEEPEST)
			logWriter = logHCS;
		else if (algorithm.getAlgorithmId() == HeuristicFactory.TABU_SEARCH)
			logWriter = logTS;
		else if (algorithm.getAlgorithmId() == HeuristicFactory.TABU_SEARCH_DYNAMIC)
			logWriter = logTSD;
		else if (algorithm.getAlgorithmId() == HeuristicFactory.SIMULATED_ANNEALING)
			logWriter = logSA;
		else
			logWriter = null;
		if (logWriter != null) {
			logWrite("\n");
			logWrite(String.format("#Move \tCurrent Best Time\n", ""));
			logWrite(String.format("%06d\t%10.8f\t%10.8f\t%06d\n", moveId,
					startingState.getEvaluation(), startingState.getEvaluation(), time));
		}
		if (nextLogger != null)
			nextLogger.logStart(algorithm, startingState, moveId, time);
	}

	@Override
	public void logMove(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, IMove move, long time) {
		logWrite(String.format("%06d\t%10.8f\t%10.8f\t%06d\n", move.getMoveId(),
				currentState.getEvaluation(), bestSolution.getEvaluation(), time));
		if (nextLogger != null)
			nextLogger.logMove(algorithm, currentState, bestSolution, move, time);
	}

	@Override
	public void logJump(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time, String info) {
		logWrite("\n");
		if (nextLogger != null)
			nextLogger.logJump(algorithm, currentState, bestSolution, moveId, time, info);
	}

	@Override
	public void logLocalOptimum(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time, String info) {
		try {
			logOPT.write(String.format("%06d\t%10.8f\t%06d\t%s: %s %d\n", moveId,
					currentState.getEvaluation(), time, algorithm.getShortName(), info,
					currentState.getHash()));
		} catch (IOException e) {
			System.err.println("Unable to write to log file.");
			e.printStackTrace();
		}
		if (nextLogger != null)
			nextLogger.logLocalOptimum(algorithm, currentState, bestSolution, moveId, time, info);
	}

	@Override
	public void logInfo(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time, String info) {
		logWrite(String.format("%06d\t%10.8f\t%10.8f\t%06d\t%s\n", moveId,
				currentState.getEvaluation(), bestSolution.getEvaluation(), time, info));
		if (nextLogger != null)
			nextLogger.logInfo(algorithm, currentState, bestSolution, moveId, time, info);
	}

	@Override
	public void logLabel(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time, String labelText) {
		try {
			logLAB.write(String.format("%06d\t%10.8f\t%06d\t%s\n", moveId,
					currentState.getEvaluation(), time, labelText));
		} catch (IOException e) {
			System.err.println("Unable to write to log file.");
			e.printStackTrace();
		}
		if (nextLogger != null)
			nextLogger.logLabel(algorithm, currentState, bestSolution, moveId, time, labelText);
	}

	@Override
	public void logEnd(SearchAlgorithm algorithm, IProblemState currentState,
			IProblemState bestSolution, int moveId, long time) {
		try {
			if (logWriter != null)
				logWriter.flush();
		} catch (IOException e) {
			System.err.println("Unable to close log file.");
			e.printStackTrace();
		}
		logWriter = null;
		if (nextLogger != null)
			nextLogger.logEnd(algorithm, currentState, bestSolution, moveId, time);
	}

	public void flushAll() {
		try {
			if (logORG != null)
				logORG.flush();
			if (logHCS != null)
				logHCS.flush();
			if (logHCF != null)
				logHCF.flush();
			if (logTS != null)
				logTS.flush();
			if (logTSD != null)
				logTSD.flush();
			if (logSA != null)
				logSA.flush();
			if (logOPT != null)
				logOPT.flush();
			if (logLAB != null)
				logLAB.flush();
			if (logRES != null)
				logRES.flush();
		} catch (IOException e) {
			System.err.println("Unable to flush log files.");
			e.printStackTrace();
		}

	}

	private String getLogDirectory() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(Preferences.P_LOG_PATH);
	}

}
