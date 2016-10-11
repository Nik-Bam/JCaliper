package gr.uom.jcaliper.heuristics;

/**
 * @author Panagiotis Kouros
 */
public interface IMove {

	public void setMoveId(int moveId);

	public int getMoveId();

	public double getMoveGain();

	public boolean isBetterThan(double threshold);

	public boolean isBetter();

	public int getTabuAttribute();

}
