package gr.uom.jcaliper.refactoring;

import gr.uom.jcaliper.executor.ExecutionManager;
import gr.uom.jcaliper.explorer.CratExplorer;
import gr.uom.jcaliper.explorer.CratMove;
import gr.uom.jcaliper.explorer.CratState;
import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.metrics.Metric;
import gr.uom.jcaliper.system.CratEntity;
import gr.uom.jcaliper.system.CratSystem;
import gr.uom.jcaliper.system.SystemClass;

/**
 * @author Panagiotis Kouros
 */
public class MoveEntity extends DirectedRefactoring {

    protected CratEntity entity;

    /**
     * @param origin
     * @param target
     * @param entities
     */
    public MoveEntity(CratEntity entity, SystemClass origin, RefactoredClass target) {
        super(origin, target);
        this.entity = entity;
        refMoves = entity.getBoxElements().size();

        updateInitialEvaluation();
    }

    @Override
    public void updateInitialEvaluation() {
        CratExplorer explorer = ExecutionManager.getInstance().getExplorer();
        CratState initial = ((CratState) explorer.getInitialState()).clone();
        double initialEvaluation = initial.getEvaluation();
        double finalEvaluation = applyRefactoring(initial).getEvaluation();

        systemBenefit = (initialEvaluation - finalEvaluation);
        systemBenefit = systemBenefit == 0 ? 0 : systemBenefit / refMoves;
    }

    @Override
    public CratState applyRefactoring(CratState initial) {
        CratSystem system = ExecutionManager.getInstance().getSystem();
        CratExplorer explorer = ExecutionManager.getInstance().getExplorer();
        Metric metric = explorer.getMetric();

        CratState finalState;
        CratState backup = (CratState) explorer.getCurrentState();
        EvaluatedClass evFrom = initial.findEvaluatedClass(entity);
        EvaluatedClass evTo = target.isNewClass() ? explorer.getEmptyClass()
                : initial.findEvaluatedClass(target.getOrigin().boxed(system));

        if (evTo == null) {
            evTo = initial.findEvaluatedClass(target.getOrigin().getId());
        }

        explorer.moveTo(initial);

        evFrom.setClassId(origin.getId());
        evTo.setClassId(target.isNewClass() ? -1 : target.getOrigin().getId());

        explorer.doMove(new CratMove(0, entity.getId(), evFrom, evTo, metric.toBeMaximized()));

        finalState = (CratState) explorer.getCurrentState();

        explorer.moveTo(backup);

        return finalState;
    }

    @Override
    public String detailedDescription(LocalOptimum optimum) {
        StringBuilder strB = new StringBuilder();

        for (int id : entity.getBoxElements()) {
            CratEntity ent = optimum.getSystem().getEntity(id);

            strB.append(String.format("Move %s '%s' from '%s' to '%s'\n", ent.isAttribute() ? "field" : "method",
                    ent.getName(), origin.getName(), target.getName()));
        }
        return String.valueOf(strB);
    }

    @Override
    public String shortDescription(LocalOptimum optimum) {
        StringBuilder strB = new StringBuilder();
        CratSystem system = optimum.getSystem();

        strB.append(String.format("Move entit%s '%s' from '%s' to '%s'\nImprovement: %.6f\n",
                (entity.getBoxElements().size() > 1) ? "ies" : "y", entity.getBoxElements().showNamesUnboxed(system),
                origin.getName(), target.getName(), systemBenefit));

        return String.valueOf(strB);
    }

    @Override
    public String oneLineDescription(LocalOptimum optimum) {
        StringBuilder strB = new StringBuilder();
        CratSystem system = optimum.getSystem();

        strB.append(String.format("Move entit%s '%s' from '%s' to '%s'\n",
                (entity.getBoxElements().size() > 1) ? "ies" : "y", entity.getBoxElements().showNamesUnboxed(system),
                origin.getName(), target.getName()));

        return String.valueOf(strB);
    }

    public CratEntity getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "MoveEntity [entity=" + entity.getId() + "-" + entity + ", origin=" + origin + ", target=" + target
                + ", systemBenefit=" + systemBenefit + "]";
    }
}
