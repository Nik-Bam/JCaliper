package gr.uom.jcaliper.heuristics;

public interface IOptimaPool {

    void storeLocalOptimum(SearchAlgorithm algorithm, IProblemState optimum, int moveId,
                           long time);

}
