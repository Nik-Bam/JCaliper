package gr.uom.jcaliper.metrics;

import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.HashedClass;

import java.util.TreeMap;

/**
 * @author Panagiotis Kouros
 */
public class EvaluatedClass extends HashedClass {

    protected int classId = -1;

    protected double evaluation;
    protected TreeMap<Integer, Double> exitGain;
    protected TreeMap<Integer, Double> entryGain;

    public EvaluatedClass(EvaluatedClass evaluated) {
        super(evaluated);
        evaluation = evaluated.getEvaluation();
        exitGain = evaluated.getExitCosts();
        entryGain = evaluated.getEntryCosts();
    }

    public EvaluatedClass(HashedClass unevaluated) {
        super(unevaluated);
        hash = unevaluated.getHash();
        exitGain = new TreeMap<Integer, Double>();
        entryGain = new TreeMap<Integer, Double>();
    }

    public double getEvaluation() {
        return evaluation;
    }

    /**
     * @param evaluation the evaluation to set
     */
    public void setEvaluation(double evaluation) {
        this.evaluation = evaluation;
    }

    public double getExitGain(int entityId) {
        Double result = exitGain.get(entityId);
        if (result != null) {
            return result;
        }
        return 0;
    }

    public double getEntryGain(int entityId) {
        Double result = entryGain.get(entityId);
        if (result != null) {
            return result;
        }
        return 0;
    }

    public EntitySet candidatesForExit() {
        return new EntitySet(exitGain.keySet());
    }

    public EntitySet candidatesForEntry() {
        return new EntitySet(entryGain.keySet());
    }

    private TreeMap<Integer, Double> getExitCosts() {
        return exitGain;
    }

    private TreeMap<Integer, Double> getEntryCosts() {
        return entryGain;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int id) {
        this.classId = id;
    }

    private static final long serialVersionUID = 1L;

}
