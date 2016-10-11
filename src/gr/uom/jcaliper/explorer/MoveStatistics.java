package gr.uom.jcaliper.explorer;

/**
 * @author Panagiotis Kouros
 */
public class MoveStatistics {

	public int nFeasibleMoves;

	public int nGoodMoves;

	public double bestGain;

	public double worstGain;

	public double gainAverage;

	public double gainStdDev;

	/**
	 * @param nFeasibleMoves
	 * @param nGoodMoves
	 * @param bestGain
	 * @param worstGain
	 * @param gainAverage
	 * @param gainStdDev
	 */
	public MoveStatistics(int nFeasibleMoves, int nGoodMoves, double bestGain, double worstGain,
			double gainAverage, double gainStdDev) {
		super();
		this.nFeasibleMoves = nFeasibleMoves;
		this.nGoodMoves = nGoodMoves;
		this.bestGain = bestGain;
		this.worstGain = worstGain;
		this.gainAverage = gainAverage;
		this.gainStdDev = gainStdDev;
	}

}
