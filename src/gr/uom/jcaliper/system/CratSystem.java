package gr.uom.jcaliper.system;

import gr.uom.jcaliper.util.IDescribable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Panagiotis Kouros
 */
public class CratSystem implements IEntityPool, IDescribable {

	private String name;
	private int systemType;
	private TreeMap<Integer, CratPackage> packages = new TreeMap<Integer, CratPackage>();
	// unboxed system
	private TreeMap<Integer, SystemClass> classes = new TreeMap<Integer, SystemClass>();
	private TreeMap<Integer, CratEntity> entities = new TreeMap<Integer, CratEntity>();
	private EntitySet systemEntities;
	// boxed system
	private TreeMap<Integer, CratClass> boxedClasses = new TreeMap<Integer, CratClass>();
	private TreeMap<Integer, CratEntity> boxedEntities;
	private EntitySet boxedSystemEntities;
	// overloaded methods
	// TreeMap<ClassID, TreeMap<MethodID, MethodEntity>>
	private TreeMap<Integer, TreeMap<Integer, CratEntity>> overloadMethods = new TreeMap<Integer, TreeMap<Integer, CratEntity>>();

	/**
	 * @param name
	 * @param systemType
	 */
	public CratSystem(String name, int systemType) {
		super();
		this.name = name;
		this.systemType = systemType;
	}

	public void addSystemClass(SystemClass newClass) {
		classes.put(newClass.getId(), newClass);
		newClass.updateHash();
	}

	public SystemClass getSystemClass(int classId) {
		return classes.get(classId);
	}

	public void addEntity(CratEntity entity) {
		entities.put(entity.getId(), entity);
	}

	@Override
	public EntitySet getEntitySet() {
		return systemEntities;
	}

	public EntitySet getBoxedEntitySet() {
		return boxedSystemEntities;
	}

	@Override
	public CratEntity getEntity(int entityId) {
		return entities.get(entityId);
	}

	public void updateDataStructures() {
		// Update packages
		for (CratPackage pkg : packages.values()) {
			pkg.setSystem(this);
		}
		// Update classes
		for (SystemClass cl : classes.values()) {
			cl.setEntities(this);
			cl.updateTargetability();
			cl.updateHash();
		}
		// Update entities
		for (CratEntity e : entities.values()) {
			int entityId = e.getId();
			e.box.add(entityId);
			e.originBox = entityId;
			e.getEntitySet().remove(entityId); // ignore recursion
			// update relatives
			for (int rel : e.getEntitySet()) {
				entities.get(rel).getRelatives().add(entityId);
			}
			e.updateForbiddenClassmates();
			e.updateMovability();
			e.updateSimilarity();
		}
		// get active entities of unboxed system
		systemEntities = new EntitySet(entities.keySet());
		// Update forbidden classmates for non-target classes
		reupdateForbiddenClassmates();
		// Create data structures for boxed system
		initializeBoxedStructures();
	}

	private void reupdateForbiddenClassmates() {
		EntitySet nonTargetElements = new EntitySet();
		for (SystemClass sysCl : classes.values()) {
			if (!sysCl.isTarget()) {
				nonTargetElements.addAll(sysCl);
			}
		}
		for (SystemClass sysCl : classes.values()) {
			if (!sysCl.isTarget()) {
				EntitySet forbidden = systemEntities.difference(sysCl);
				for (int entId : sysCl) {
					getEntity(entId).getForbiddenClassmates().addAll(forbidden);
				}
			} else {
				for (int entId : sysCl) {
					CratEntity entity = getEntity(entId);
					for (int forbId : entity.getForbiddenClassmates()) {
						CratEntity forbiddenEntity = getEntity(forbId);
						forbiddenEntity.getForbiddenClassmates().add(entId);
					}
					EntitySet forbiddenSet = nonTargetElements.intersection(entity.getEntitySet());
					EntitySet forbiddenRel = nonTargetElements.intersection(entity.getRelatives());
					entity.getForbiddenClassmates().addAll(forbiddenSet);
					entity.getForbiddenClassmates().addAll(forbiddenRel);
				}
			}
		}
	}

	// Methods for boxed entities and classes

	private void initializeBoxedStructures() {
		// Get all entity boxes
		ArrayList<EntityBox> boxes = new ArrayList<EntityBox>();
		int entId = entities.size() + 1; // first available id

		checkForOverloads(entities);
		String previousEntityId = "";

		for (CratEntity entity : entities.values()) {
			if (!previousEntityId.equals(entity.getClassLevelId())) { // TODO Fancier  workaround
				if (canBeAlteredToBox(entity)) {
					EntityBox boxed = createBoxedEntity(entId, entity);
					boxes.add(boxed);
					entId++;
				}
			}
			previousEntityId = entity.getClassLevelId();
		}
		// Add entity boxes to entities and update originBox properties
		for (EntityBox entity : boxes) {
			int boxId = entity.getId();
			entities.put(boxId, entity);
			entity.originBox = boxId;
			for (int elemId : entity.box) {
				getEntity(elemId).originBox = boxId;
			}
		}
		// initialize boxed classes
		for (SystemClass sysCl : classes.values()) {
			boxedClasses.put(sysCl.getId(), new CratClass(sysCl.boxed(this)));
		}
		// create boxed entities collection
		boxedEntities = deepCopyOfEntities(entities.values());
		// get active entities of unboxed system
		boxedSystemEntities = new EntitySet(entities.keySet());
		boxedSystemEntities = boxedSystemEntities.boxed(this);
		// reupdate all entity sets to be 'boxed'
		for (CratEntity entity : boxedEntities.values()) {
			entity.entitySet = entity.entitySet.boxed(this);
			entity.relatives = entity.relatives.boxed(this);
			entity.forbiddenClassmates = entity.forbiddenClassmates.boxed(this);
		}
		// remove entity from its entity set
		for (CratEntity entity : boxedEntities.values()) {
			entity.entitySet.remove(entity.id);
		}
	}

	private TreeMap<Integer, CratEntity> deepCopyOfEntities(Collection<CratEntity> prototypes) {
		TreeMap<Integer, CratEntity> deepCopy = new TreeMap<Integer, CratEntity>();
		CratEntity clone;
		for (CratEntity prototype : prototypes) {
			if (prototype.isMethod()) {
				clone = new CratMethod(prototype.id, prototype.name, prototype.originClass);
			} else if (prototype.isBox()) {
				clone = new EntityBox(prototype.id, prototype.name, prototype.originClass);
			} else {
				clone = new CratAttribute(prototype.id, prototype.name, prototype.originClass);
			}
			clone.box = new EntitySet(prototype.box);
			clone.entitySet = new EntitySet(prototype.entitySet);
			clone.relatives = new EntitySet(prototype.relatives);
			clone.forbiddenClassmates = new EntitySet(prototype.forbiddenClassmates);
			clone.movable = prototype.movable;
			clone.originBox = prototype.originBox;
			clone.properties = prototype.properties;
			clone.similarityToOriginClass = prototype.similarityToOriginClass;
			deepCopy.put(clone.id, clone);
		}
		return deepCopy;
	}

	private EntityBox createBoxedEntity(int newId, CratEntity entity) { // TODO Check for bugs (Difference between entitySet and relatives)
		EntitySet box = new EntitySet(entity.box);
		EntitySet boxRelatives = new EntitySet(entity.relatives);
		EntitySet boxForbidden = new EntitySet(entity.forbiddenClassmates);
		boolean boxIsMovable = entity.isMovable();
		double similarityToOriginClass = 0.0;
		// add box members
		SystemClass origin = entity.originClass;

		if (entity.isOverloaded()) {
			TreeMap<Integer, CratEntity> boxMembersMap = overloadMethods.get(origin.getId());
			for (Map.Entry<Integer, CratEntity> entry : boxMembersMap.entrySet()) {
				int methodID = entry.getKey();
				CratEntity method = entry.getValue();

				if (method.getName().equals(entity.getName()) && methodID != entity.getId()) {
					box.add(methodID);
					boxRelatives.addAll(method.relatives);
					boxForbidden.addAll(method.forbiddenClassmates);
					boxIsMovable &= method.isMovable();
					similarityToOriginClass += method.similarityToOriginClass;
				}
			}

		} else {
			EntitySet candidateMembers = origin.intersection(entity.entitySet);
			// iterate while finding new members
			boolean foundNewMembers;
			do {
				foundNewMembers = false;
				for (int candId : candidateMembers) {
					CratEntity candEntity = getEntity(candId);
					EntitySet candEntitySet = candEntity.entitySet;
					if (box.containsAll(candEntitySet) && !candEntity.isOverloaded()) {
						foundNewMembers = true;
						box.add(candId);
						boxRelatives.addAll(candEntity.relatives);
						boxForbidden.addAll(candEntity.forbiddenClassmates);
						boxIsMovable &= candEntity.isMovable();
						similarityToOriginClass += candEntity.similarityToOriginClass;
					}
				}
				candidateMembers.removeAll(box);
			} while (foundNewMembers);
		}

		// create the new box entity instance
		String boxName = String.format("%s+", entity.getName());
		EntityBox boxedEntity = new EntityBox(newId, boxName, entity.originClass);
		boxedEntity.box = box;
		boxedEntity.entitySet = new EntitySet(entity.entitySet);
		boxedEntity.entitySet.add(entity.id);
		boxedEntity.relatives = boxRelatives;
		boxedEntity.forbiddenClassmates = boxForbidden;
		boxedEntity.movable = boxIsMovable;
		boxedEntity.similarityToOriginClass = similarityToOriginClass;
		return boxedEntity;
	}

	private void checkForOverloads(TreeMap<Integer, CratEntity> entities) {
		TreeMap<Integer, CratEntity> innerMap;
		for (CratEntity entity : entities.values()) {
			if (entity.isMethod() && !entity.isOverloaded()) {
				CratMethod method = (CratMethod) entity;
				SystemClass origin = method.originClass;

				for (Integer i : origin) {
					CratEntity classEntity = this.getEntity(i);

					if (classEntity.isMethod() && classEntity.getName().equals(method.getName())
							&& classEntity.getId() != method.getId()) {
						if (overloadMethods.get(origin.getId()) == null) {
							innerMap = new TreeMap<Integer, CratEntity>();

							overloadMethods.put(origin.getId(), innerMap);
						}
						((CratMethod) classEntity).setOverloaded(true);
						overloadMethods.get(origin.getId()).put(classEntity.getId(), classEntity);
						if (!method.isOverloaded()) {
							method.setOverloaded(true);
							overloadMethods.get(origin.getId()).put(method.getId(), method);
						}
					}
				}
			}
		}
	}

	private boolean canBeAlteredToBox(CratEntity entity) {
		//TODO Box "1" entity + static entities = 1 box
		if (entity.isMethod()) {
			return entity.isOverloaded();
		} else {
			// search for candidate box members
			SystemClass origin = entity.originClass;
			EntitySet candidateMembers = origin.intersection(entity.entitySet);

			for (int candId : candidateMembers) {
				CratEntity candEntity = getEntity(candId);
				EntitySet candEntitySet = candEntity.entitySet;

				if ((candEntitySet.size() == 1) && candEntitySet.contains(entity.id)) {
					return true;
				}
			}
			return false;
		}
	}

	// Various presentation methods

	public String getEntitySetsDetails() {
		StringBuilder sb = new StringBuilder();
		for (CratEntity e : entities.values()) {
			sb.append(String.format("%s: %s\n", e.getName(), e.propertiesToText()));
			sb.append(String.format("S(%d)=%-20s\tS(%s)=%s\n", e.getId(), e.getEntitySet(), e.getName(),
					e.getEntitySet().showNamesSet(this)));
			sb.append(String.format("R(%d)=%-20s\tR(%s)=%s\n", e.getId(), e.getRelatives(), e.getName(),
					e.getRelatives().showNamesSet(this)));
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1); // remove last newline
		}
		return sb.toString();
	}

	public String getClassSetsDetails() {
		StringBuilder sb = new StringBuilder();
		for (SystemClass c : classes.values()) {
			sb.append(String.format("%s: %s %s\n", c.getName(), c.getJavaPath(), c.propertiesToText()));
			// sb.append(String.format("Hash code: %d\n", c.longHashCode()));
			sb.append(String.format("S(%d)=%-20s\tS(%s)=%s\n", c.getId(), c, c.getName(), c.showNamesSet(this)));
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1); // remove last newline
		}
		return sb.toString();
	}

	public String getPackagesDetails() {
		StringBuilder sb = new StringBuilder();
		for (CratPackage pkg : packages.values()) {
			sb.append(String.format("%s: %s \n", pkg.getName(), pkg));
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1); // remove last newline
		}
		return sb.toString();
	}

	@Override
	public String oneLineDescription() {
		return shortDescription();
	}

	@Override
	public String shortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(systemTypeName()).append(name);
		sb.append(String.format("\n%d entities ", getTotalEntities()));
		sb.append(String.format("(%d attributes + ", howManyAttributes()));
		sb.append(String.format("%d methods) ", howManyMethods()));
		sb.append(String.format("in %d classes, ", classes.size()));
		sb.append(String.format("%d packages", packages.size()));
		return sb.toString();
	}

	@Override
	public String detailedDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(systemTypeName()).append(name);
		sb.append(String.format("\n\nPackages\n%s\n", getPackagesDetails()));
		sb.append(String.format("\nClass Sets\n%s\n", getClassSetsDetails()));
		sb.append(String.format("\nEntity Sets\n%s\n", getEntitySetsDetails()));
		sb.append(String.format("\n%s", shortDescription()));
		return sb.toString();
	}

	// Setters and getters

	private int howManyMethods() {
		int numOfMethods = 0;
		for (int entId : systemEntities) {
			if (getEntity(entId).isMethod()) {
				numOfMethods++;
			}
		}
		return numOfMethods;
	}

	private int howManyAttributes() {
		int numOfAttrib = 0;
		for (int entId : systemEntities) {
			if (getEntity(entId).isAttribute()) {
				numOfAttrib++;
			}
		}
		return numOfAttrib;
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
	public String getFullName() {
		return systemTypeName() + name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the systemType
	 */
	public int getSystemType() {
		return systemType;
	}

	/**
	 * @param systemType the systemType to set
	 */
	public void setSystemType(int systemType) {
		this.systemType = systemType;
	}

	@Override
	public int getTotalEntities() {
		return systemEntities.size();
	}

	public int getTotalBoxedEntities() {
		return boxedSystemEntities.size();
	}

	public TreeMap<Integer, CratEntity> getEntities() {
		return entities;
	}

	public TreeMap<Integer, CratEntity> getBoxedEntities() {
		return boxedEntities;
	}

	public TreeMap<Integer, CratEntity> getHybridEntities() {
		TreeMap<Integer, CratEntity> hybridEntities = deepCopyOfEntities(boxedEntities.values());
		for (CratEntity entity : hybridEntities.values()) {
			entity.entitySet = getEntity(entity.id).entitySet;
		}
		return hybridEntities;
	}

	/**
	 * @param entities the entities to set
	 */
	public void setEntities(TreeMap<Integer, CratEntity> entities) {
		this.entities = entities;
	}

	/**
	 * @return the classes
	 */
	public TreeMap<Integer, SystemClass> getClasses() {
		return classes;
	}

	public TreeMap<Integer, CratClass> getBoxedClasses() {
		return boxedClasses;
	}

	/**
	 * @param classes the classes to set
	 */
	public void setClasses(TreeMap<Integer, SystemClass> classes) {
		this.classes = classes;
	}

	/**
	 * @param packages the packages to set
	 */
	public void setPackages(TreeMap<Integer, CratPackage> packages) {
		this.packages = packages;
	}

	/**
	 * @return the packages
	 */
	public TreeMap<Integer, CratPackage> getPackages() {
		return packages;
	}

	private String systemTypeName() {
		if (systemType == SYSTEM_PROJECT) {
			return "project ";
		}
		if (systemType == SYSTEM_PACKAGE) {
			return "package ";
		}
		return "";
	}

	// System Types
	public static final int SYSTEM_PROJECT = 0;
	public static final int SYSTEM_PACKAGE = 1;

}
