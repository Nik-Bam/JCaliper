/**
 *
 */
package gr.uom.jcaliper.system;

import java.util.Collection;
import java.util.Set;

/**
 * @author Panagiotis Kouros
 */
public class HashedClass extends CratClass {

    protected long hash;

    // constructors

    /**
     * @param id
     */
    public HashedClass(int id) {
        super(id);
        updateHash();
    }

    /**
     * @param collection
     */
    public HashedClass(Collection<? extends Integer> collection) {
        super(collection);
        updateHash();
    }

    /**
     * @param array
     */
    public HashedClass(int[] array) {
        super(array);
        updateHash();
    }

    public HashedClass(Set<? extends Integer> set) {
        super(set);
        updateHash();
    }

    public HashedClass(HashedClass hashed) {
        super(hashed);
        hash = hashed.hash;
    }

    // getters and setters

    public long getHash() {
        return hash;
    }

    ;

    public void updateHash() {
        hash = calculateHash();
    }

    private static final long serialVersionUID = 1L;

}
