package gr.uom.jcaliper.system;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Panagiotis Kouros
 */
public class SystemClass extends CratClass {

    protected int id;
    protected String name;
    protected String javaPath;
    protected CratPackage mypackage;
    protected IEntityPool entities;
    protected int properties;
    protected SystemClass superclass;
    protected HashSet<SystemClass> subclasses = new HashSet<SystemClass>();
    protected HashSet<SystemClass> superclasses = new HashSet<SystemClass>();
    protected SystemClass external;
    protected HashSet<SystemClass> internals = new HashSet<SystemClass>();
    protected HashSet<SystemClass> containers = new HashSet<SystemClass>();
    protected boolean isTarget;
    private long hash;
    private ArrayList<CratEntity> systemMethods;

    public SystemClass(int id, String name, String javaPath) {
        super();
        this.id = id;
        this.name = name;
        this.javaPath = javaPath;
    }

    public void updateTargetability() {
        /*
         * A Class cannot be a move target when - is Interface - is internal class
		 */
        if (isInterface() || isInternal())
            isTarget = false;
        else
            isTarget = true;
    }

    public final void setStatic(boolean flag) {
        if (flag)
            properties |= CLASS_IS_STATIC;
        else
            properties &= ~CLASS_IS_STATIC;
        updateTargetability();
    }

    public boolean isStatic() {
        return ((properties & CLASS_IS_STATIC) != 0);
    }

    public final void setInterface(boolean flag) {
        if (flag)
            properties |= CLASS_IS_INTERFACE;
        else
            properties &= ~CLASS_IS_INTERFACE;
        updateTargetability();
    }

    public boolean isInterface() {
        return ((properties & CLASS_IS_INTERFACE) != 0);
    }

    public final void setAbstract(boolean flag) {
        if (flag)
            properties |= CLASS_IS_ABSTRACT;
        else
            properties &= ~CLASS_IS_ABSTRACT;
        updateTargetability();
    }

    public boolean isAbstract() {
        return ((properties & CLASS_IS_ABSTRACT) != 0);
    }

    public boolean isSuperclass() {
        return (subclasses.size() > 0);
    }

    public boolean isSubclass() {
        return (superclasses.size() > 0);
    }

    public boolean isInternal() {
        return (external != null);
    }

    public boolean hasInternals() {
        return (internals.size() > 0);
    }

    public EntitySet getAttributes() {
        EntitySet attributes = new EntitySet();
        for (int entityId : this) {
            CratEntity entity = entities.getEntity(entityId);
            if (entity.isAttribute())
                attributes.add(entityId);
        }
        return attributes;
    }

    public EntitySet getMethods() {
        EntitySet methods = new EntitySet();
        for (int entityId : this) {
            CratEntity entity = entities.getEntity(entityId);
            if (entity.isMethod())
                systemMethods.add(entity);
            methods.add(entityId);
        }
        for (int i = 0; i <= systemMethods.size(); i++) {
            for (int j = 0; j <= systemMethods.size(); j++) {
                if ((systemMethods.get(j).getName().equals(systemMethods.get(i).getName())) &&
                        (systemMethods.get(i).getId() != systemMethods.get(j).getId())) {

                }

            }
        }
        return methods;
    }

    public EntitySet getConstructors() {
        EntitySet constructors = new EntitySet();
        for (int entityId : this) {
            CratEntity entity = entities.getEntity(entityId);
            if (entity.isConstructor())
                constructors.add(entityId);
        }
        return constructors;
    }

    // Getters and Setters

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
     * @return the javaPath
     */
    public String getJavaPath() {
        return javaPath;
    }

    /**
     * @return the classPath
     */
    public String getJavaPathWithType() {
        return (isInterface() ? "interface " : "class ") + javaPath;
    }

    /**
     * @return the entities
     */
    public IEntityPool getEntities() {
        return entities;
    }

    /**
     * @param entities the entities to set
     */
    public void setEntities(IEntityPool entities) {
        this.entities = entities;
    }

    /**
     * @return the mypackage
     */
    public CratPackage getPackage() {
        return mypackage;
    }

    /**
     * @param mypackage the mypackage to set
     */
    public void setPackage(CratPackage mypackage) {
        this.mypackage = mypackage;
    }

    /**
     * @return the superclass
     */
    public SystemClass getSuperclass() {
        return superclass;
    }

    /**
     * @param superclass the superclass to set
     */
    public void setSuperclass(SystemClass superclass) {
        this.superclass = superclass;
    }

    /**
     * @return the subclasses
     */
    public HashSet<SystemClass> getSubclasses() {
        return subclasses;
    }

    /**
     * @param subclasses the subclasses to set
     */
    public void setSubclasses(HashSet<SystemClass> subclasses) {
        this.subclasses = subclasses;
    }

    /**
     * @return the superclasses
     */
    public HashSet<SystemClass> getSuperclasses() {
        return superclasses;
    }

    /**
     * @param superclasses the superclasses to set
     */
    public void setSuperclasses(HashSet<SystemClass> superclasses) {
        this.superclasses = superclasses;
    }

    /**
     * @return the containers
     */
    public HashSet<SystemClass> getContainers() {
        return containers;
    }

    /**
     * @return the external
     */
    public SystemClass getExternal() {
        return external;
    }

    /**
     * @param external the external to set
     */
    public void setExternal(SystemClass external) {
        this.external = external;
    }

    /**
     * @return the internals
     */
    public HashSet<SystemClass> getInternals() {
        return internals;
    }

    /**
     * @param internals the internals to set
     */
    public void setInternals(HashSet<SystemClass> internals) {
        this.internals = internals;
    }

    /**
     * @return the target
     */
    public boolean isTarget() {
        return isTarget;
    }

    public static final int CLASS_IS_STATIC = 1 << 0;
    public static final int CLASS_IS_INTERFACE = 1 << 1;
    public static final int CLASS_IS_ABSTRACT = 1 << 2;

    public long getHash() {
        return hash;
    }

    public void updateHash() {
        hash = calculateHash();
    }

    // Presentation
    @Override
    public String toString() {
        return showIdsSet();
    }

    @Override
    public String showDetails() {
        return String.format("%-20s\t%-30s\tID=%d", showIdsSet(), showIdsSetUnboxed(entities),
                getHash());
    }

    public String propertiesToText() {
        StringBuilder sb = new StringBuilder();
        if (isStatic())
            sb.append("static ");
        if (isAbstract())
            sb.append("abstract ");
        if (isInternal())
            sb.append("internal ");
        if (isInterface())
            sb.append("interface ");
        else
            sb.append("class ");
        if (!isTarget())
            sb.append("(not a target)");
        return sb.toString();
    }

    private static final long serialVersionUID = 1L;

}
