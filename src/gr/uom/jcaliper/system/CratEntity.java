package gr.uom.jcaliper.system;

/**
 * @author Panagiotis Kouros
 */
public abstract class CratEntity {

	protected int id;
	protected String name;
	protected EntitySet box = new EntitySet();
	protected EntitySet entitySet = new EntitySet();
	protected EntitySet relatives = new EntitySet();
	protected EntitySet forbiddenClassmates = new EntitySet();
	protected boolean movable = true;
	protected SystemClass originClass;
	protected int originBox;
	protected int properties;
	protected double similarityToOriginClass;
	protected boolean overloaded = false;

	public CratEntity(int id, String name, SystemClass originClass) {
		this.id = id;
		this.name = name;
		this.originClass = originClass;
	}

	// Methods to be overridden in subclasses

	public boolean isAttribute() {
		return false;
	}

	public boolean isMethod() {
		return false;
	}

	public boolean isOverloaded() {
		return overloaded;
	}

	protected abstract void updateMovability();

	public abstract String propertiesToText();

	public abstract void updateSimilarity();

	// Overriden only for the methods

	protected void updateForbiddenClassmates() {
		/*
		 * Forbidden classmates: - all members of superclasses - all members of
		 * subclasses - all members of internal/external classes (experimental)
		 * - all members of associated containers (arrays, collections, etc.)
		 */
		for (SystemClass superclass : originClass.getSuperclasses())
			forbiddenClassmates.addAll(superclass);
		for (SystemClass subclass : originClass.getSubclasses())
			forbiddenClassmates.addAll(subclass);

		if (originClass.isInternal())
			forbiddenClassmates.addAll(originClass.getExternal());
		if (originClass.hasInternals())
			for (SystemClass internal : originClass.getInternals())
				forbiddenClassmates.addAll(internal);

		for (SystemClass container : originClass.getContainers())
			forbiddenClassmates.addAll(container);
	}

	// Presentation methods

	public String showBox(IEntityPool pool) {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	// Getters and Setters

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
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
	 * @return the name
	 */
	public abstract String getType();

	/**
	 * @return the full class path
	 */
	public String getClassPath() {
		return originClass.getJavaPath() + "::" + name;
	}

	public final boolean isMovable() {
		return movable;
	}

	/**
	 * @param movable
	 *            the movable to set
	 */
	public void setMovable(boolean movable) {
		this.movable = movable;
	}

	public final void setSynchronized(boolean flag) {
		if (flag)
			properties |= ENTITY_IS_SYNCHRONIZED;
		else
			properties &= ~ENTITY_IS_SYNCHRONIZED;
	}

	public final boolean isSynchronized() {
		return ((properties & ENTITY_IS_SYNCHRONIZED) != 0);
	}

	public final void setAbstract(boolean flag) {
		if (flag)
			properties |= ENTITY_IS_ABSTRACT;
		else
			properties &= ~ENTITY_IS_ABSTRACT;
	}

	public final boolean isAbstractMethod() {
		return ((properties & ENTITY_IS_ABSTRACT) != 0);
	}

	public final void setStatic(boolean flag) {
		if (flag)
			properties |= ENTITY_IS_STATIC;
		else
			properties &= ~ENTITY_IS_STATIC;
	}

	public final boolean isStatic() {
		return ((properties & ENTITY_IS_STATIC) != 0);
	}

	public final void setConstructor(boolean flag) {
		if (flag)
			properties |= ENTITY_IS_CONSTRUCTOR;
		else
			properties &= ~ENTITY_IS_CONSTRUCTOR;
	}

	public final boolean isConstructor() {
		return ((properties & ENTITY_IS_CONSTRUCTOR) != 0);
	}

	public final void setCallsSuperclassMethod(boolean flag) {
		if (flag)
			properties |= ENTITY_USES_SUPERCLASS_ENTITIES;
		else
			properties &= ~ENTITY_USES_SUPERCLASS_ENTITIES;
	}

	public final boolean usesSuperclassEntities() {
		return ((properties & ENTITY_USES_SUPERCLASS_ENTITIES) != 0);
	}

	/**
	 * @return the entitySet
	 */
	public EntitySet getEntitySet() {
		return entitySet;
	}

	/**
	 * @param entitySet
	 *            the entitySet to set
	 */
	public void setEntitySet(EntitySet entitySet) {
		this.entitySet = entitySet;
	}

	/**
	 * @return the relatives
	 */
	public EntitySet getRelatives() {
		return relatives;
	}

	/**
	 * @param relatives
	 *            the relatives to set
	 */
	public void setRelatives(EntitySet relatives) {
		this.relatives = relatives;
	}

	/**
	 * @return the originClass
	 */
	public SystemClass getOriginClass() {
		return originClass;
	}

	/**
	 * @param originClass
	 *            the originClass to set
	 */
	public void setOriginClass(SystemClass originClass) {
		this.originClass = originClass;
	}

	/**
	 * @return the originBox
	 */
	public int getOriginBox() {
		return originBox;
	}

	/**
	 * @param originBox
	 *            the originBox to set
	 */
	public void setOriginBox(int originBox) {
		this.originBox = originBox;
	}

	/**
	 * @return the forbiddenClassmates
	 */
	public EntitySet getForbiddenClassmates() {
		return forbiddenClassmates;
	}

	/**
	 * @param forbiddenClassmates
	 *            the forbiddenClassmates to set
	 */
	public void setForbiddenClassmates(EntitySet forbiddenClassmates) {
		this.forbiddenClassmates = forbiddenClassmates;
	}

	/**
	 * @return the complex
	 */
	public boolean isOrphan() {
		return (entitySet.size() < 1) && (relatives.size() == 0);
	}

	/**
	 * @return a name-based id that uniquely identifies the entity in the class
	 */
	public String getClassLevelId() {
		return name + "@" + originClass.id;
	}

	public boolean isAtom() {
		return true;
	}

	public boolean isBox() {
		return false;
	}

	public EntitySet getBoxElements() {
		return box;
	}

	/**
	 * @return the similarityToOriginClass
	 */
	public double getSimilarityToOriginClass() {
		return similarityToOriginClass;
	}

	protected static final int ENTITY_IS_SYNCHRONIZED = 1 << 0;
	protected static final int ENTITY_IS_ABSTRACT = 1 << 1;
	protected static final int ENTITY_IS_STATIC = 1 << 2;
	protected static final int ENTITY_IS_CONSTRUCTOR = 1 << 3;
	protected static final int ENTITY_USES_SUPERCLASS_ENTITIES = 1 << 4;

	// In future we could handle the following properties
	// protected static final int ENTITY_IS_GETTER = 1 << 5;
	// protected static final int ENTITY_IS_SETTER = 1 << 6;
	// protected static final int ENTITY_IS_COLLECTION_ADDER = 1 << 7;
	// protected static final int ENTITY_IS_DELEGATE = 1 << 8;

}
