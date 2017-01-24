package gr.uom.jcaliper.refactoring;

import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.system.HashedClass;
import gr.uom.jcaliper.system.IEntityPool;
import gr.uom.jcaliper.system.SystemClass;

/**
 * @author Panagiotis Kouros
 */
public class RefactoredClass extends HashedClass {

    private double evaluation;
    private String name = null;
    private SystemClass origin = null;
    private boolean isNewClass = false;

    public RefactoredClass(EvaluatedClass prototype, IEntityPool entities) {
        super(prototype.unbox(entities));
        evaluation = prototype.getEvaluation();
    }

    /**
     * @return the evaluation
     */
    public double getEvaluation() {
        return evaluation;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the origin
     */
    public SystemClass getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(SystemClass origin) {
        this.origin = origin;
    }

    public boolean isNewClass() {
        return isNewClass;
    }

    public void setIsNewClass(boolean isNewClass) {
        this.isNewClass = isNewClass;
    }

    private static final long serialVersionUID = 1L;

}
