package gr.uom.jcaliper.system;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;

import gr.uom.java.ast.ASTReader;
import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.CompilationUnitCache;
import gr.uom.java.ast.FieldObject;
import gr.uom.java.ast.MethodObject;
import gr.uom.java.ast.SystemObject;
import gr.uom.java.ast.association.Association;
import gr.uom.java.distance.MyAttribute;
import gr.uom.java.distance.MyClass;
import gr.uom.java.distance.MyMethod;
import gr.uom.java.distance.MySystem;
import gr.uom.jcaliper.loggers.ActivityLogger;

/**
 * @author Panagiotis Kouros
 */

public class CratSystemCreator {

	private IJavaProject selectedProject;
	private IPackageFragment selectedPackage;

	// new
	private String systemName;
	private int systemType;
	private Logger activityLogger;
	private TreeMap<Integer, CratEntity> entities = new TreeMap<Integer, CratEntity>();
	private TreeMap<Integer, SystemClass> classes = new TreeMap<Integer, SystemClass>();
	private TreeMap<Integer, CratPackage> packages = new TreeMap<Integer, CratPackage>();

	private TreeMap<Integer, String> superclass = new TreeMap<Integer, String>();
	private TreeMap<Integer, Set<String>> superclasses = new TreeMap<Integer, Set<String>>();
	private TreeMap<Integer, Set<String>> containers = new TreeMap<Integer, Set<String>>();

	private TreeMap<String, CratPackage> packageIndex = new TreeMap<String, CratPackage>();

	private TreeMap<String, SystemClass> classIndex = new TreeMap<String, SystemClass>();
	private TreeMap<Integer, Set<String>> classSets = new TreeMap<Integer, Set<String>>();
	private TreeMap<String, Integer> entityIndex = new TreeMap<String, Integer>();
	private TreeMap<Integer, String> entityUniqueNames = new TreeMap<Integer, String>();
	private TreeMap<Integer, Set<String>> entitySets = new TreeMap<Integer, Set<String>>();

	public CratSystemCreator(IJavaProject selectedProject, IPackageFragment selectedPackage) {
		super();
		this.selectedProject = selectedProject;
		this.selectedPackage = selectedPackage;
		activityLogger = ActivityLogger.getInstance().getLogger();
	}

	public CratSystem getCratSystem() {
		info("-----------------------------------------------------\n");
		info(((selectedPackage != null) ? "package " + selectedPackage.getElementName()
				: "project " + selectedProject.getElementName()) + "\n");
		info("-----------------------------------------------------\n");
		info(String.format("\nPlease wait while analyzing the selected %s...",
				(selectedPackage != null) ? "package" : "project"));
		getDataFromAST();
		info("Done.\nPreparing data structures...");
		removeOrphanEntities();
		reindexEntities();

		// Completing class information
		for (SystemClass sysClass : classIndex.values()) {
			int classId = sysClass.getId();
			// Updating superclass/subclass info
			String superClassName = superclass.get(classId);
			if (superClassName != null) {
				SystemClass superClass = classIndex.get(superClassName);
				sysClass.setSuperclass(superClass);
				if (superClass != null)
					superClass.getSubclasses().add(sysClass);
			}
			Set<String> supers = superclasses.get(classId);
			for (String supName : supers)
				if (supName != null) {
					SystemClass supClass = classIndex.get(supName);
					if (supClass != null) {
						sysClass.getSuperclasses().add(supClass);
						supClass.getSubclasses().add(sysClass);
					}
				}
			// Updating containers
			Set<String> contNames = containers.get(classId);
			for (String contName : contNames)
				if (contName != null) {
					SystemClass contClass = classIndex.get(contName);
					if (contClass != null)
						sysClass.getContainers().add(contClass);
				}

			// Updating entity set
			Set<String> namesSet = classSets.get(sysClass.getId());
			for (String entName : namesSet)
				sysClass.add(entityIndex.get(entName));
		}

		// Identifying internal classes and packages
		int packageId = 1;
		for (SystemClass sysClass : classIndex.values()) {
			String pathName = getPathName(sysClass.getJavaPath());
			SystemClass externalClass = classIndex.get(pathName);
			if (externalClass != null) { // sysClass is internal
				sysClass.setExternal(externalClass);
				externalClass.getInternals().add(sysClass);
			} else {
				CratPackage mypackage = packageIndex.get(pathName);
				if (mypackage == null) {
					mypackage = new CratPackage(packageId, pathName);
					packages.put(packageId, mypackage);
					packageIndex.put(pathName, mypackage);
					packageId++;
				}
				sysClass.setPackage(mypackage);
				mypackage.put(sysClass.getId(), sysClass);
			}
		}

		// Updating internal classes and packages
		for (SystemClass cl : classes.values())
			if (cl.isInternal()) {
				CratPackage mypackage = cl.getExternal().getPackage();
				mypackage.put(cl.getId(), cl);
				cl.setPackage(mypackage);
			}

		// Completing entity information
		for (CratEntity entity : entities.values()) {
			Set<String> namesSet = entitySets.get(entity.getId());
			for (String entName : namesSet)
				if (entityIndex.containsKey(entName))
					entity.getEntitySet().add(entityIndex.get(entName));
			// else
			// System.err.format("WARNING: unexpected name '%s'\n", entName);
		}

		// Creating craSystem
		CratSystem system = new CratSystem(systemName, systemType);
		system.setPackages(packages);
		system.setClasses(classes);
		system.setEntities(entities);
		system.updateDataStructures();
		info("Done.\nBuilding protected model...\n\n");
		return system;
	}

	private String getTinyName(String fullName) {
		// Try to find last period
		int pos = fullName.lastIndexOf(".");
		if (pos < 0)
			return fullName;
		else
			return fullName.substring(pos + 1);
	}

	private String getPathName(String fullName) {
		// Try to find last period
		int pos = fullName.lastIndexOf(".");
		if (pos < 0)
			return "";
		else
			return fullName.substring(0, pos);
	}

	public void getDataFromAST() {
		CompilationUnitCache.getInstance().clearCache();
		if (selectedPackage != null) {
			new ASTReader(selectedPackage);
			systemType = CratSystem.SYSTEM_PACKAGE;
			systemName = selectedPackage.getElementName();
		} else {
			new ASTReader(selectedProject);
			systemType = CratSystem.SYSTEM_PROJECT;
			systemName = selectedProject.getElementName();
		}

		// Useful maps to rescan System Classes by lexicographical order
		TreeMap<String, MyClass> myClassMap = new TreeMap<String, MyClass>();
		TreeMap<String, ClassObject> classObjectMap = new TreeMap<String, ClassObject>();

		SystemObject systemObject = ASTReader.getSystemObject();
		MySystem mySystem = new MySystem(systemObject);

		// First scan to get only class names and AST classes
		Iterator<MyClass> classIt = mySystem.getClassIterator();
		while (classIt.hasNext()) {
			MyClass myClass = classIt.next();
			String className = myClass.getName();
			SystemClass sysClass = new SystemClass(0, getTinyName(className), className);
			classIndex.put(className, sysClass);
			myClassMap.put(className, myClass);
			classObjectMap.put(className, myClass.getClassObject());
		}

		// Main scan
		int classId = 1, entityId = 1;
		for (SystemClass sysClass : classIndex.values()) {
			sysClass.setId(classId);
			String className = sysClass.getJavaPath();
			MyClass myClass = myClassMap.get(className);
			ClassObject co = classObjectMap.get(className);
			classes.put(classId, sysClass);
			classIndex.put(className, sysClass);
			sysClass.setStatic(co.isStatic());
			sysClass.setAbstract(co.isAbstract());
			sysClass.setInterface(co.isInterface());
			// Some class data are stored to be used later
			HashSet<String> supers = new HashSet<String>();
			ListIterator<String> superClassIterator = co.getSuperclassIterator();
			while (superClassIterator.hasNext())
				supers.add(superClassIterator.next());
			superclasses.put(classId, supers);
			superclass.put(classId, myClass.getSuperclass());
			HashSet<String> contAssociations = new HashSet<String>();
			for (Association association : mySystem.getAssociationsOfClass(co))
				if (association.isContainer())
					contAssociations.add(association.getTo());
			containers.put(classId, contAssociations);
			classSets.put(classId, myClass.getEntitySet());

			// Get entities: attributes
			ListIterator<MyAttribute> attributeIterator = myClass.getAttributeIterator();
			while (attributeIterator.hasNext()) {
				MyAttribute myAttribute = attributeIterator.next();
				String entityName = myAttribute.getName();
				CratAttribute sysAttrib = new CratAttribute(entityId, entityName, sysClass);
				// info(String.format("%s %B\n", myAttribute,myAttribute.isReference()));
				if (!myAttribute.isReference()) {
					sysClass.add(entityId);
					entities.put(entityId, sysAttrib);
					// Some attribute data are stored to be used later
					String uniqueName = myAttribute.toString();
					entityIndex.put(uniqueName, entityId);
					entityUniqueNames.put(entityId, uniqueName);
					entitySets.put(entityId, myAttribute.getEntitySet());
					entityId++;
				}
			}
			ListIterator<FieldObject> fieldIterator = co.getFieldIterator();
			while (fieldIterator.hasNext()) {
				FieldObject fo = fieldIterator.next();
				String fullName = fo.getClassName() + "::" + fo.getType() + " " + fo.getName();
				// info(fullName+"\n");
				if (entityIndex.containsKey(fullName)) {
					int entId = entityIndex.get(fullName);
					CratEntity entity = entities.get(entId);
					entity.setStatic(fo.isStatic());
					// TODO get more information
				}
			}

			// Get entities: methods
			ListIterator<MyMethod> methodIterator = myClass.getMethodIterator();
			while (methodIterator.hasNext()) {
				MyMethod myMethod = methodIterator.next();
				MethodObject mo = myMethod.getMethodObject();
				String entityName = mo.getName();
				CratMethod sysMethod = new CratMethod(entityId, entityName, sysClass);
				// String fullName = mo.getClassName() + "::" + mo.getAccess() + " " + mo.getName();
				// info(fullName+"\n");
				sysClass.add(entityId);
				entities.put(entityId, sysMethod);
				if (myMethod.getMethodName().equals(getTinyName(myClass.getName())))
					sysMethod.setConstructor(true);
				sysMethod.setAbstract(myMethod.isAbstract());
				sysMethod.setSynchronized(mo.isSynchronized());
				sysMethod.setStatic(mo.isStatic());
				// Some method data are stored for later use
				String uniqueName = myMethod.toString();
				entityIndex.put(uniqueName, entityId);
				entityUniqueNames.put(entityId, uniqueName);
				entitySets.put(entityId, myMethod.getEntitySet());
				entityId++;
			}
			classId++;
		}
	}

	void removeOrphanEntities() {
		// Find entities with empty sets
		// that are not members of other sets
		Set<String> haveEmptySet = new HashSet<String>();
		Set<String> areSetMembers = new HashSet<String>();
		for (String entityName : entityIndex.keySet()) {
			int entityId = entityIndex.get(entityName);
			Set<String> entitySet = entitySets.get(entityId);
			if (entitySet.size() == 0)
				haveEmptySet.add(entityName);
			areSetMembers.addAll(entitySet);
		}
		Set<String> orphans = haveEmptySet;
		orphans.removeAll(areSetMembers); // exclude set members
		// Delete orphans from all data structures
		// info("Orphans:" + orphans + "\n");
		for (String entityName : orphans) {
			int entityId = entityIndex.get(entityName);
			CratEntity entity = entities.get(entityId);
			entity.getOriginClass().remove(entityId);
			entities.remove(entityId);
			entitySets.remove(entityId);
			entityIndex.remove(entityName);
		}
		for (Set<String> classSet : classSets.values())
			classSet.removeAll(orphans);

		for (Set<String> entitySet : entitySets.values())
			entitySet.removeAll(orphans);
	}

	void reindexEntities() {
		TreeMap<Integer, CratEntity> newEntities = new TreeMap<Integer, CratEntity>();
		TreeMap<String, Integer> newEntityIndex = new TreeMap<String, Integer>();
		TreeMap<Integer, Set<String>> newEntitySets = new TreeMap<Integer, Set<String>>();
		int newId = 1;
		for (Map.Entry<Integer, CratEntity> entry : entities.entrySet()) {
			int oldId = entry.getKey();
			CratEntity entity = entry.getValue();
			String entityName = entityUniqueNames.get(oldId);
			Set<String> entitySet = entitySets.get(oldId);
			entity.setId(newId);
			newEntities.put(newId, entity);
			newEntityIndex.put(entityName, newId);
			newEntitySets.put(newId, entitySet);
			SystemClass origin = entity.getOriginClass();
			origin.remove(oldId);
			origin.add(newId);
			newId++;
		}
		entities = newEntities;
		entityIndex = newEntityIndex;
		entitySets = newEntitySets;
	}

	private void info(String message) {
		activityLogger.log(Level.INFO, message);
	}

}
