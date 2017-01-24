package gr.uom.jcaliper.system;

import java.util.Collection;
import java.util.Set;

/**
 * @author Panagiotis Kouros
 */
public class CratClass extends EntitySet {

    // Constructors

    /**
     *
     */
    public CratClass() {
        super();
    }

    /**
     * @param id
     */
    public CratClass(int id) {
        super(id);
    }

    /**
     * @param collection
     */
    public CratClass(Collection<? extends Integer> collection) {
        super(collection);
    }

    /**
     * @param array
     */
    public CratClass(int[] array) {
        super(array);
    }

    /**
     * @param set
     */
    public CratClass(Set<? extends Integer> set) {
        super(set);
    }

    // Methods

    // Presentation
    @Override
    public String toString() {
        return showIdsSet();
    }

    public String showDetails() {
        return String.format("%-20s", showIdsSet());
    }

    private static final long serialVersionUID = 1L;

}
