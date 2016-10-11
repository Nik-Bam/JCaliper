package gr.uom.jcaliper.explorer;

import java.util.TreeMap;
import java.util.TreeSet;

import gr.uom.jcaliper.metrics.EvaluatedClass;
import gr.uom.jcaliper.system.CratClass;
import gr.uom.jcaliper.system.CratEntity;
import gr.uom.jcaliper.system.CratPackage;
import gr.uom.jcaliper.system.CratSystem;
import gr.uom.jcaliper.system.EntitySet;
import gr.uom.jcaliper.system.IEntityPool;
import gr.uom.jcaliper.system.SystemClass;
import gr.uom.jcaliper.util.IDescribable;

/**
 * @author Panagiotis Kouros
 */
public class CraCase implements IEntityPool, IDescribable {

	private final String name;
	private final CratSystem system;
	private final int type; // project or package or class
	private final int boxType;
	private final int totalEntities;
	private final EntitySet systemEntities;
	private final TreeMap<Integer, CratEntity> entities;
	private final TreeMap<Integer, ? extends CratClass> classes;
	private CratState initial;

	public CraCase(CratSystem system, int boxType) {
		super();
		this.system = system;
		this.boxType = boxType;
		name = system.getName();
		if (system.getSystemType() == CratSystem.SYSTEM_PROJECT)
			type = CraCase.CRACASE_PROJECT;
		else
			type = CraCase.CRACASE_PACKAGE;
		if (isUnboxed()) {
			classes = system.getClasses();
			entities = system.getEntities();
			systemEntities = system.getEntitySet();
			totalEntities = system.getTotalEntities();
		} else if (isBoxed()) {
			classes = system.getBoxedClasses();
			entities = system.getBoxedEntities();
			systemEntities = system.getBoxedEntitySet();
			totalEntities = system.getTotalBoxedEntities();
		} else { // isHybrid
			classes = system.getBoxedClasses();
			entities = system.getHybridEntities();
			systemEntities = system.getBoxedEntitySet();
			totalEntities = system.getTotalEntities();
		}
	}

	// Presentation methods

	public String getEntitySetsDetails() {
		StringBuilder sb = new StringBuilder();
		for (int entId : systemEntities) {
			CratEntity e = getEntity(entId);
			sb.append(String.format("%s: %s\n", e.getName(), e.propertiesToText()));
			sb.append(String.format("S(%d)=%-20s\tS(%s)=%s\n", e.getId(), e.getEntitySet(), e.getName(),
					e.getEntitySet().showNamesSet(this)));
			sb.append(String.format("R(%d)=%-20s\tR(%s)=%s\n", e.getId(), e.getRelatives(), e.getName(),
					e.getRelatives().showNamesSet(this)));
			sb.append(String.format("F(%d)=%-20s\tF(%s)=%s\n", e.getId(), e.getForbiddenClassmates(), e.getName(),
					e.getForbiddenClassmates().showNamesSet(this)));
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1); // remove last newline
		return sb.toString();
	}

	public String getEntityBoxesDetails() {
		StringBuilder sb = new StringBuilder();
		for (int entId : systemEntities) {
			CratEntity e = getEntity(entId);
			if (e.isBox())
				sb.append(String.format("B(%d)=%-20s\tB(%s)=%s\n", e.getId(), e.getBoxElements(), e.getName(),
						e.getBoxElements().showNamesSet(this)));
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1); // remove last newline
		return sb.toString();
	}

	public String getClassSetsDetails() {
		StringBuilder sb = new StringBuilder();
		for (int clId : classes.keySet()) {
			SystemClass sysCl = system.getClasses().get(clId);
			sb.append(String.format("%s: %s %s\n", sysCl.getName(), sysCl.getJavaPath(), sysCl.propertiesToText()));
			CratClass cl = classes.get(clId);
			sb.append(String.format("S(%d)=%-20s\tS(%s)=%s\n", clId, cl, sysCl.getName(), cl.showNamesSet(this)));
			EntitySet rel = getClassRelatives(cl);
			sb.append(String.format("R(%d)=%-20s\tR(%s)=%s\n", clId, rel, sysCl.getName(), rel.showNamesSet(this)));
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1); // remove last newline
		return sb.toString();
	}

	public String getPackagesDetails() {
		TreeSet<Integer> packageIds = new TreeSet<Integer>();
		for (int clId : classes.keySet()) {
			CratPackage pkg = system.getClasses().get(clId).getPackage();
			packageIds.add(pkg.getId());
		}
		StringBuilder sb = new StringBuilder();
		for (int pkgId : packageIds) {
			CratPackage pkg = system.getPackages().get(pkgId);
			sb.append(String.format("%s: %s \n", pkg.getName(), pkg));
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1); // remove last newline
		return sb.toString();
	}

	@Override
	public String oneLineDescription() {
		return shortDescription();
	}

	@Override
	public String shortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Original system: ").append(system.shortDescription());
		sb.append("\n\nProtected model: ").append(systemTypeName()).append(name);
		sb.append(String.format("\n%d entities ", totalEntities));
		sb.append(String.format("(%d attributes + ", howManyAttributes()));
		sb.append(String.format("%d methods) ", howManyMethods()));
		sb.append(String.format("in %d classes, ", classes.size()));
		sb.append(String.format("%d packages\n", howManyPackages()));
		return sb.toString();
	}

	@Override
	public String detailedDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(systemTypeName()).append(name);
		sb.append(String.format("\n\nPackages\n%s\n", getPackagesDetails()));
		sb.append(String.format("\nClass Sets\n%s\n", getClassSetsDetails()));
		sb.append(String.format("\nEntity Sets\n%s\n", getEntitySetsDetails()));
		if (!isUnboxed())
			sb.append(String.format("\nEntity Boxes\n%s\n", getEntityBoxesDetails()));
		sb.append(String.format("\n%s", shortDescription()));
		sb.append(String.format("\nInitial Evaluation = %8.6f\n\n", initial.getEvaluation()));
		return sb.toString();
	}

	private int howManyMethods() {
		int numOfMethods = 0;
		for (int entId : systemEntities)
			if (getEntity(entId).isMethod())
				numOfMethods++;
		return numOfMethods;
	}

	private int howManyAttributes() {
		int numOfAttrib = 0;
		for (int entId : systemEntities)
			if (getEntity(entId).isAttribute())
				numOfAttrib++;
		return numOfAttrib;
	}

	private int howManyPackages() {
		TreeSet<Integer> packageIds = new TreeSet<Integer>();
		for (int clId : classes.keySet()) {
			CratPackage pkg = system.getClasses().get(clId).getPackage();
			packageIds.add(pkg.getId());
		}
		return packageIds.size();
	}

	private EntitySet getClassRelatives(CratClass cl) {
		EntitySet relatives = new EntitySet();
		for (int entId : cl) {
			CratEntity entity = getEntity(entId);
			relatives.addAll(entity.getRelatives());
		}
		return relatives;
	}

	// Getters and Setters

	public void setInitial(CratState initial) {
		this.initial = initial;
	}

	public CratState getInitial() {
		return initial;
	}

	@Override
	public EntitySet getEntitySet() {
		return systemEntities;
	}

	@Override
	public CratEntity getEntity(int entityId) {
		return entities.get(entityId);
	}

	@Override
	public int getTotalEntities() {
		return totalEntities;
	}

	public int getSystemTotalEntities() {
		return system.getTotalEntities();
	}

	public int getProblemSize() {
		int size = 0;
		for (EvaluatedClass cl : initial.getClasses())
			size += cl.size();
		return size;
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

	public boolean isBoxed() {
		return boxType == CRACASE_BOXED;
	}

	public boolean isUnboxed() {
		return boxType == CRACASE_UNBOXED;
	}

	public boolean isHybrid() {
		return boxType == CRACASE_HYBRID;
	}

	public TreeMap<Integer, ? extends CratClass> getClasses() {
		return classes;
	}

	public int getNumOfClasses() {
		return classes.size();
	}

	public int getNumOfPackages() {
		return system.getPackages().size();
	}

	public String getClassName(int classId) {
		SystemClass sysCl = system.getClasses().get(classId);
		if (sysCl != null)
			return sysCl.getName();
		else
			return "";
	}

	public String getPackageNameOf(int classId) {
		SystemClass sysCl = system.getClasses().get(classId);
		if (sysCl != null)
			return sysCl.getPackage().getName();
		else
			return "";
	}

	public int getCaseType() {
		return type;
	}

	private String systemTypeName() {
		if (type == CRACASE_PROJECT)
			return "project ";
		if (type == CRACASE_PACKAGE)
			return "package ";
		if (type == CRACASE_CLASS)
			return "class ";
		return "";
	}

	// System Types
	public static final int CRACASE_PROJECT = 0;
	public static final int CRACASE_PACKAGE = 1;
	public static final int CRACASE_CLASS = 2;

	// Boxing Types
	public static final int CRACASE_UNBOXED = 0;
	public static final int CRACASE_BOXED = 1;
	public static final int CRACASE_HYBRID = 2;

}
