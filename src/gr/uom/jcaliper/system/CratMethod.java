package gr.uom.jcaliper.system;

/**
 * @author Panagiotis Kouros
 */
public class CratMethod extends CratEntity {

    /**
     * @param id
     * @param name
     * @param originClass
     */
    public CratMethod(int id, String name, SystemClass originClass) {
        super(id, name, originClass);
    }

    @Override
    public boolean isMethod() {
        return true;
    }

    public void setOverloaded(boolean overloaded) {
        this.overloaded = overloaded;
    }

    @Override
    protected void updateMovability() {
        /*
         * A method is "immovable" when
		 * - is a constructor
		 * - calls superclass methods
		 * - is synchronized
		 * - is abstract
		 * - is 'orphan'(= has empty entitySet and no relatives)
		 * - is static (experimental)
		 */
        detectCallsToSuperclassEntities();
        if (isConstructor() || usesSuperclassEntities() || isSynchronized() || isAbstractMethod() || isOrphan()
                || isStatic())
            movable = false;
        else
            movable = true;
    }

    ;

    @Override
    public String propertiesToText() {
        StringBuilder sb = new StringBuilder();
        if (isSynchronized())
            sb.append("synchronized ");
        if (isAbstractMethod())
            sb.append("abstract ");
        if (isStatic())
            sb.append("static ");
        if (isAttribute())
            sb.append("attribute");
        else if (isConstructor())
            sb.append("constructor");
        else
            sb.append("method");
        // if (isGetter())
        // sb.append(", getter");
        // if (isSetter())
        // sb.append(", setter");
        // if (isCollectionAdder())
        // sb.append(", collection adder");
        if (!isMovable())
            sb.append(" (immovable)");
        return sb.toString();
    }

    @Override
    public String getType() {
        return "method ";
    }

    @Override
    public String toString() {
        return name + "()";
    }

    @Override
    public void updateSimilarity() {
        double similarity = Dice.StringSimilarity(name.toUpperCase(), originClass.getName().toUpperCase());
        similarityToOriginClass = 1 + (10 * similarity);
    }

    private void detectCallsToSuperclassEntities() {
        boolean callFound = false;
        SystemClass current = originClass;
        while (current.isSubclass()) {
            SystemClass superClass = current.getSuperclass();
            if (superClass != null) {
                EntitySet common = entitySet.intersection(superClass);
                callFound = (common.size() > 0);
                if (callFound)
                    break;
                current = superClass;
            } else
                break;
        }
        setCallsSuperclassMethod(callFound);
    }

}
