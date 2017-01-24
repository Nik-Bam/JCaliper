package gr.uom.jcaliper.filters;

import gr.uom.jcaliper.explorer.CratState;
import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.refactoring.RefactoredClass;
import gr.uom.jcaliper.system.CratEntity;
import gr.uom.jcaliper.system.CratSystem;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.SystemClass;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

//TOOD Rework
public class SingleEntityClassFilter implements IFilter {

    @Override
    public void applyFilter(CratState state, CratSystem system) {
        ArrayList<Long> toBeFiltered = new ArrayList<Long>();

        for (Map.Entry<Long, EvaluatedClass> entry : state.entrySet()) {
            long key = entry.getKey();
            EvaluatedClass e = entry.getValue();

            //TODO Filter 1 entity + static entities
            if (e.size() == 1) { // Filter classes with only one entity left
                toBeFiltered.add(key);
                RefactoredClass refCl = new RefactoredClass(e, system);
                // Do not filter unchanged classes (classes that had one entity in the first place)
                for (SystemClass s : system.getClasses().values()) {
                    if (refCl.getHash() == s.getHash()) {
                        toBeFiltered.remove(e);
                        break;
                    }
                }
            }
        }
        for (long id : toBeFiltered) {
            int entityID = state.get(id).iterator().next();    // Get the only entity of the evaluated class
            CratEntity entity = system.getEntity(entityID);
            ConcurrentMap<Long, AtomicInteger> relativeClasses = new ConcurrentHashMap<Long, AtomicInteger>();
            int max = -1;
            long classHash = findOriginEvClass(state, system, entity);

            EntitySet boxedRelatives = entity.getRelatives().boxed(system);
            for (int i : boxedRelatives) {
                long key = findEvaluatedClass(state, system.getEntity(i));

                relativeClasses.putIfAbsent(key, new AtomicInteger(0));
                relativeClasses.get(key).incrementAndGet();
            }
            for (Map.Entry<Long, AtomicInteger> entry : relativeClasses.entrySet()) {
                long key = entry.getKey();
                int value = entry.getValue().intValue();

                if (value > max) {
                    max = value;
                    classHash = key;
                }
            }
            if (id != classHash) {
                state.remove(id);
                state.get(classHash).add(entityID);
            }
        }
    }

    private long findOriginEvClass(CratState state, CratSystem system, CratEntity entity) {
        ConcurrentMap<Long, AtomicInteger> occurencies = new ConcurrentHashMap<Long, AtomicInteger>();
        int max = -1;
        long classHash = -1;
        int originClassHash = entity.getOriginClass().getId();

        for (Map.Entry<Long, EvaluatedClass> entry : state.entrySet()) {
            long key = entry.getKey();
            EvaluatedClass value = entry.getValue();

            for (int i : value.unbox(system)) {
                if (system.getEntity(i).getOriginClass().getId() == originClassHash) {
                    occurencies.putIfAbsent(key, new AtomicInteger(0));
                    occurencies.get(key).incrementAndGet();
                }
            }
        }
        for (Map.Entry<Long, AtomicInteger> entry : occurencies.entrySet()) {
            long key = entry.getKey();
            int value = entry.getValue().intValue();

            if (value > max) {
                max = value;
                classHash = key;
            }
        }
        return classHash;
    }

    private long findEvaluatedClass(CratState state, CratEntity entity) {
        for (Map.Entry<Long, EvaluatedClass> entry : state.entrySet()) {
            long key = entry.getKey();
            EvaluatedClass value = entry.getValue();

            for (int id : value) {
                if (id == entity.getId()) {
                    return key;
                }
            }
        }
        return -1;    // Should never return -1
    }
}
