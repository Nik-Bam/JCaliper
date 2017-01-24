package gr.uom.jcaliper.refactoring;

import gr.uom.jcaliper.executor.ExecutionManager;
import gr.uom.jcaliper.explorer.CratExplorer;
import gr.uom.jcaliper.explorer.CratMove;
import gr.uom.jcaliper.explorer.CratState;
import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.metrics.Metric;
import gr.uom.jcaliper.system.CratEntity;
import gr.uom.jcaliper.system.CratSystem;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.SystemClass;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Panagiotis Kouros
 */
public class ExtractClass extends DirectedRefactoring {

    protected EntitySet extracted;
    protected List<MoveEntity> moves;

    /**
     * @param origin
     * @param target
     * @param extracted
     */
    public ExtractClass(SystemClass origin, RefactoredClass target) {
        super(origin, target);
        extracted = origin.intersection(target);
        refMoves = extracted.size();

        updateInitialEvaluation();
    }

    @Override
    public void updateInitialEvaluation() {
        CratSystem system = ExecutionManager.getInstance().getSystem();
        CratExplorer explorer = ExecutionManager.getInstance().getExplorer();
        CratState initial = ((CratState) explorer.getInitialState()).clone();
        double initialEvaluation = initial.getEvaluation();

        moves = new ArrayList<>();

        for (Integer id : extracted.boxed(system)) {
            CratEntity entity = system.getEntity(id);
            moves.add(new MoveEntity(entity, entity.getOriginClass(), target));
        }
        double finalEvaluation = applyRefactoring(initial).getEvaluation();

        systemBenefit = (initialEvaluation - finalEvaluation);
        systemBenefit = systemBenefit == 0 ? 0 : systemBenefit / refMoves;
    }

    @Override
    public CratState applyRefactoring(CratState initial) {
        CratExplorer explorer = ExecutionManager.getInstance().getExplorer();
        Metric metric = explorer.getMetric();

        CratState finalState;
        CratState backup = (CratState) explorer.getCurrentState();
        EvaluatedClass evFrom;
        EvaluatedClass evTo = explorer.getEmptyClass();

        explorer.moveTo(initial);

        for (MoveEntity me : moves) {
            evFrom = ((CratState) explorer.getCurrentState()).findEvaluatedClass(me.entity);

            evFrom.setClassId(origin.getId());
            evTo.setClassId(-1);

            explorer.doMove(new CratMove(0, me.entity.getId(), evFrom, evTo, metric.toBeMaximized()));

            evTo = initial.findEvaluatedClass(me.entity);
        }
        finalState = (CratState) explorer.getCurrentState();

        explorer.moveTo(backup);

        return finalState;
    }

    @Override
    public String detailedDescription(LocalOptimum optimum) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Extract Class '%s' from '%s'\n\tby moving %d entit%s: '%s'\n", target.getName(),
                origin.getName(), refMoves, (refMoves > 1) ? "ies" : "y",
                extracted.showNamesUnboxed(optimum.getSystem())));

        return sb.toString();
    }

    @Override
    public String shortDescription(LocalOptimum optimum) {
        String descr = detailedDescription(optimum);
        return descr.substring(0, descr.length() - 1) + String.format("\nImprovement: %.6f\n", systemBenefit);
    }

    @Override
    public String oneLineDescription(LocalOptimum optimum) {
        return detailedDescription(optimum);
    }

    public EntitySet getExtracted() {
        return extracted;
    }

    public List<MoveEntity> getMoves() {
        return moves;
    }

    @Override
    public String toString() {
        return "ExtractClass [extracted=" + extracted + ", origin=" + origin + ", target=" + target + ", systemBenefit="
                + systemBenefit + "]";
    }
}
