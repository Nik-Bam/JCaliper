package gr.uom.jcaliper.refactoring;

import gr.uom.jcaliper.explorer.CratState;
import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.system.CratEntity;
import gr.uom.jcaliper.system.CratSystem;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.SystemClass;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author Panagiotis Kouros
 */
public class LocalOptimumCreator {

    // auxiliary data structures
    private TreeMap<Long, RefactoredClass> refClasses;
    private TreeMap<Long, SystemClass> refOrigins;
    private TreeMap<Long, String> refNames;
    private TreeMap<Long, Boolean> isAssigned;
    private int[] assignments;
    // data structures of new local optimum
    private ArrayList<RefactoredClass> refClassesOrdered;
    private ArrayList<ExtractClass> extractClassRefs;
    private ArrayList<MoveEntity> moveEntityRefs;
    private CratSystem system;

    public LocalOptimumCreator(CratSystem system) {
        super();
        this.system = system;
    }

    public LocalOptimum createLocalOptimum(CratState state) {
        // get refactored classes from state
        refClasses = new TreeMap<Long, RefactoredClass>();
        for (EvaluatedClass evCl : state.getClasses()) {
            RefactoredClass refCl = new RefactoredClass(evCl, system);
            refClasses.put(refCl.getHash(), refCl);
        }
        // update origin system classes and names
        updateOriginsAndNames();
        for (RefactoredClass refCl : refClasses.values()) {
            refCl.setName(refNames.get(refCl.getHash()));
            refCl.setOrigin(refOrigins.get(refCl.getHash()));
        }
        // order refactored classes by java path
        reorderRefClasses();
        // create refactoring data structures
        updateRefactorings();
        // create local optimum
        return new LocalOptimum(refClassesOrdered, extractClassRefs, moveEntityRefs, state.getEvaluation(), system);
    }

    // private methods

    private void updateOriginsAndNames() {
        refOrigins = new TreeMap<Long, SystemClass>();
        refNames = new TreeMap<Long, String>();
        isAssigned = new TreeMap<Long, Boolean>();
        assignments = new int[system.getClasses().size() + 1]; // ids start from 1

        for (RefactoredClass refCl : refClasses.values())
            isAssigned.put(refCl.getHash(), false);
        // Step 1: search for unchanged classes
        for (SystemClass sysCl : system.getClasses().values()) {
            long sysHash = sysCl.getHash();
            if (refClasses.containsKey(sysHash)) {
                refOrigins.put(sysHash, sysCl);
                refNames.put(sysHash, sysCl.getName());
                isAssigned.put(sysHash, true);
                assignments[sysCl.getId()]++;
            }
        }
        // Step 2: search for immovable entities
        for (RefactoredClass refCl : refClasses.values()) {
            if (!isAssigned.get(refCl.getHash())) {
                for (int entId : refCl) {
                    CratEntity entity = system.getEntity(entId);
                    if (!entity.isMovable()) {
                        SystemClass sysCl = entity.getOriginClass();
                        refOrigins.put(refCl.getHash(), sysCl);
                        refNames.put(refCl.getHash(), sysCl.getName());
                        isAssigned.put(refCl.getHash(), true);
                        assignments[sysCl.getId()]++;
                        break;
                    }
                }
            }
        }
        // Step 3: put unassigned ref. classes in Array
        // and use name similarity criteria to assign names
        ArrayList<RefactoredClass> refPending = new ArrayList<RefactoredClass>();
        for (RefactoredClass refCl : refClasses.values()) {
            if (!isAssigned.containsKey(refCl.getHash()) || !isAssigned.get(refCl.getHash())) {
                refCl.setIsNewClass(true);
                refPending.add(refCl);
            }
        }
        if (refPending.size() > 0) {
            getOriginClassByNameSimilarity(refPending);
            // Finally, give names to any not yet assigned new classes
            int index = 1;
            for (RefactoredClass refCl : refClasses.values())
                if (!refOrigins.containsKey(refCl.getHash())) {
                    refOrigins.put(refCl.getHash(), null);        // Extracted new classes have null origin class
                    refNames.put(refCl.getHash(), String.format("NewClass%d", index++));
                }
        }
    }

    private void getOriginClassByNameSimilarity(ArrayList<RefactoredClass> refPending) {
        int nRefPending = refPending.size();
        int nSysClasses = system.getClasses().size() + 1; // ids start from 1
        // Create similarity matrix
        double[][] similarity = new double[nRefPending][nSysClasses];
        for (int r = 0; r < nRefPending; r++) {
            RefactoredClass refCl = refPending.get(r);
            for (int entId : refCl) {
                CratEntity entity = system.getEntity(entId);
                int s = entity.getOriginClass().getId();
                similarity[r][s] += entity.getSimilarityToOriginClass();
            }
        }
        int maxRefId = 0, maxSysId = 0;
        double maxSim;
        do {
            // find maximum similarity
            maxSim = 0;
            for (int refId = 0; refId < nRefPending; refId++)
                for (int sysId = 0; sysId < nSysClasses; sysId++)
                    if (maxSim < similarity[refId][sysId]) {
                        maxRefId = refId;
                        maxSysId = sysId;
                        maxSim = similarity[refId][sysId];
                    }
            // update refOrigin and refName. Clear row of maxRefId
            if (maxSim > 0) {
                RefactoredClass refCl = refPending.get(maxRefId);
                SystemClass sysCl = system.getSystemClass(maxSysId);
                String refName = sysCl.getName();
                if (assignments[maxSysId] > 0) {
                    String candName = getCandidateName(refCl);
                    if (!candName.equals(""))
                        assignments[maxSysId]--; // prevent increment
                    else {
                        candName = "Methods";
                        if (assignments[maxSysId] > 1)
                            candName += Integer.toString(assignments[maxSysId] - 1);
                    }
                    refName = refName + candName;
                }
                refOrigins.put(refCl.getHash(), sysCl);
                refNames.put(refCl.getHash(), refName);
                assignments[maxSysId]++;
                for (int sysId = 0; sysId < nSysClasses; sysId++)
                    similarity[maxRefId][sysId] = 0.0;
            }
        } while (maxSim > 0);
    }

    private String getCandidateName(RefactoredClass refCl) {
        // find the attribute with the most relatives 'classmates'
        int maxId = 0, maxRel = 0;
        CratEntity entity = null;
        for (int entId : refCl) {
            entity = system.getEntity(entId);
            if (entity.isAttribute()) {
                int commons = (refCl.intersection(entity.getEntitySet())).size();
                if (maxRel < commons) {
                    maxId = entId;
                    maxRel = commons;
                }
            }
        }
        // if no attribute found, it is a class of methods
        if (maxRel == 0)
            return "";
        // Uppercase only the first letter of entity's name
        entity = system.getEntity(maxId);
        String candName = entity.getName();
        if (candName.length() > 1)
            candName = candName.substring(0, 1).toUpperCase() + candName.substring(1);
        else
            candName = candName.toUpperCase();
        return candName;
    }

    /**
     * @return the system
     */
    public CratSystem getSystem() {
        return system;
    }

    private void reorderRefClasses() {
        TreeMap<String, RefactoredClass> ordered = new TreeMap<String, RefactoredClass>();
        for (RefactoredClass refCl : refClasses.values()) {
            SystemClass sysCl = refOrigins.get(refCl.getHash());
            String fullName = sysCl.getPackage().getName() + "." + refNames.get(refCl.getHash());
            ordered.put(fullName, refCl);
        }
        refClassesOrdered = new ArrayList<RefactoredClass>(ordered.values());
    }

    private void updateRefactorings() {
        // get extract class refactorings
        extractClassRefs = new ArrayList<ExtractClass>();
        for (RefactoredClass refCl : refClassesOrdered) {
            SystemClass sysCl = refOrigins.get(refCl.getHash());
            String refName = refNames.get(refCl.getHash());
            if (!refName.equals(sysCl.getName())) {
                extractClassRefs.add(new ExtractClass(sysCl, refCl));
            }
        }
        // get move entity refactorings
        TreeMap<String, MoveEntity> ordered = new TreeMap<String, MoveEntity>();
        for (RefactoredClass refCl : refClassesOrdered) {
            SystemClass sysCl = refOrigins.get(refCl.getHash());
            EntitySet boxedMoves = refCl.difference(sysCl).boxed(system);

            for (int entId : boxedMoves) {
                CratEntity entity = system.getEntity(entId);
                SystemClass origin = entity.getOriginClass();
                MoveEntity refactoring = new MoveEntity(entity, origin, refCl);
                String fullName = origin.getJavaPath() + "." + entity.getName();
                ordered.put(fullName, refactoring);
            }
        }
        moveEntityRefs = new ArrayList<MoveEntity>(ordered.values());
    }

}
