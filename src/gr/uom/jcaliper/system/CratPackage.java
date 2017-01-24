package gr.uom.jcaliper.system;

import java.util.TreeMap;

/**
 * @author Panagiotis Kouros
 */
public class CratPackage extends TreeMap<Integer, SystemClass> {

    private int id;
    private String name;
    private CratSystem system;

    /**
     * @param name
     */
    public CratPackage(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
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
     * @return the craCase
     */
    public CratSystem getSystem() {
        return system;
    }

    /**
     * @param craCase the craCase to set
     */
    public void setSystem(CratSystem system) {
        this.system = system;
    }

    private static final long serialVersionUID = 1L;
}
