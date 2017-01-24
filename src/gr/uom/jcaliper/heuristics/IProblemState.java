package gr.uom.jcaliper.heuristics;

public interface IProblemState {

    public IProblemState clone();

    public double getEvaluation();

    public boolean isBetterThan(double threshold);

    public long getHash();

}
