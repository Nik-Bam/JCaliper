package gr.uom.jcaliper.system;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Set of entity IDs
 *
 * @author Panagiotis Kouros
 */
public class EntitySet extends TreeSet<Integer> implements Comparable<EntitySet> {

	// Constructors

	/**
	 * Creates an empty set of entity IDs
	 */
	public EntitySet() {
		super();
	}

	/**
	 * Creates a new set of entity IDs with only one element
	 */
	public EntitySet(int id) {
		super();
		add(id);
	}

	/**
	 * Creates a new entity set from any collection of entity IDs
	 *
	 * @param collection
	 *            a collection of entity IDs
	 */
	public EntitySet(Collection<? extends Integer> collection) {
		super(collection);
	}

	/**
	 * Creates a new entity set from any set of entity IDs
	 *
	 * @param set
	 *            a set of entity IDs
	 */
	public EntitySet(Set<? extends Integer> set) {
		super(set);
	}

	/**
	 * Creates a new entity set from any array of entity IDs
	 *
	 * @param set
	 *            an array of entity IDs
	 */
	public EntitySet(int[] array) {
		super();
		for (int id : array) {
			add(id);
		}
	}

	// Methods

	@Override
	public EntitySet clone() {
		return (EntitySet) super.clone(); // a shallow copy
	}

	/**
	 * @return string of all entity IDs separated by comma
	 */
	public String signature() {
		if (size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(size() << 2); // 4*size()
		for (int e : this) {
			sb.append(e).append(',');
		}
		sb.setLength(sb.length() - 1); // delete last comma
		return sb.toString();
	}

	public EntitySet unbox(IEntityPool pool) {
		EntitySet unboxed = new EntitySet();
		for (int id : this) {
			unboxed.addAll(pool.getEntity(id).getBoxElements());
		}
		return unboxed;
	}

	public EntitySet boxed(IEntityPool pool) {
		EntitySet boxed = new EntitySet();
		for (int id : this) {
			boxed.add(pool.getEntity(id).getOriginBox());
		}
		return boxed;
	}

	public void addAll(int[] array) {
		for (int id : array) {
			add(id);
		}
	}

	// Presentation with IDs

	/**
	 * @return string of all entity IDs separated by comma
	 */
	public String showIds() {
		return signature();
	}

	/**
	 * @return a presentation of entity IDs set
	 */
	public String showIdsSet() {
		return "{" + signature() + "}";
	}

	/**
	 * @param pool
	 *            reference collection to get entity details from
	 * @return string of all box-entity IDs separated by comma
	 */
	public String showIdsUnboxed(IEntityPool pool) {
		return unbox(pool).signature();
	}

	/**
	 * @param pool
	 *            reference collection to get entity details from
	 * @return a presentation of box-entities IDs set
	 */
	public String showIdsSetUnboxed(IEntityPool pool) {
		return "{" + showIdsUnboxed(pool) + "}";
	}

	@Override
	public String toString() {
		return "{" + signature() + "}";
	}

	// Presentation with entity names

	/**
	 * @param pool
	 *            reference collection to get entity details from
	 * @return string of all entity names separated by comma
	 */
	public String showNames(IEntityPool pool) {
		if (size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(size() << 4); // 16 * size
		// show in order: attributes, constructors, methods
		for (int id : this) {
			CratEntity entity = pool.getEntity(id);
			if (entity.isAttribute()) {
				sb.append(entity).append(", ");
			}
		}
		for (int id : this) {
			CratEntity entity = pool.getEntity(id);
			if (entity.isConstructor()) {
				sb.append(entity).append(", ");
			}
		}
		for (int id : this) {
			CratEntity entity = pool.getEntity(id);
			if (entity.isMethod() && !entity.isConstructor()) {
				sb.append(entity).append(", ");
			}
		}
		sb.setLength(sb.length() - 2); // delete last comma
		return sb.toString();
	}

	/**
	 * @param pool
	 *            reference collection to get entity details from
	 * @return a presentation of entity names set
	 */
	public String showNamesSet(IEntityPool pool) {
		return "{" + showNames(pool) + "}";
	}

	/**
	 * @param pool
	 *            reference collection to get entity details from
	 * @return string of box-entity names separated by comma
	 */
	public String showNamesUnboxed(IEntityPool pool) {
		return unbox(pool).showNames(pool);
	}

	/**
	 * @param pool
	 *            reference collection to get entity details from
	 * @return a presentation of box-entity names set
	 */
	public String showNamesSetUnboxed(IEntityPool pool) {
		return "{" + unbox(pool).showNames(pool) + "}";
	}

	/**
	 * The long hash code is generated from set elements and set signature
	 *
	 * @return the long hash code
	 */
	public long calculateHash() {
		// Most Significant part = hash of set members
		// Less Significant part = hash of set signature
		int hash = 0;
		for (int e : this) {
			hash = ((hash << 5) - hash) + e; // 31 * hash + id
		}
		return ((long) hash << 32) + signature().hashCode();
	}

	// Set operations

	public EntitySet union(EntitySet other) {
		EntitySet union = new EntitySet(this);
		union.addAll(other);
		return union;
	}

	public EntitySet difference(EntitySet other) {
		EntitySet difference = new EntitySet(this);
		difference.removeAll(other);
		return difference;
	}

	public EntitySet intersection(EntitySet other) {
		EntitySet intersection = new EntitySet(this);
		intersection.retainAll(other);
		return intersection;
	}

	// create new set while including/excluding element

	public EntitySet plus(int element) {
		EntitySet union = new EntitySet(this);
		union.add(element);
		return union;
	}

	public EntitySet without(int element) {
		EntitySet difference = new EntitySet(this);
		difference.remove(element);
		return difference;
	}

	// Similarity indexes

	/**
	 * Calculates the Jaccard similarity to another set
	 *
	 * @param other
	 *            the other set
	 * @return the Jaccard similarity index
	 */
	public double jaccardSimilarityIndex(EntitySet other) {
		int sizeTotal = size() + other.size();
		if (sizeTotal == 0) {
			return 0.0;
		}
		int commonElements = intersection(other).size();
		return (double) commonElements / (sizeTotal - commonElements);
	}

	// Distance indexes

	/**
	 * Calculates the Jaccard distance to another set
	 *
	 * @param other
	 *            the other set
	 * @return the Jaccard distance
	 */
	public double jaccardDistance(EntitySet other) {
		return 1.0 - jaccardSimilarityIndex(other);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public int compareTo(EntitySet that) {
		if (size() < that.size()) {
			return -1;
		} else if (size() > that.size()) {
			return 1;
		} else {
			Iterator<Integer> thisIterator = iterator();
			Iterator<Integer> thatIterator = that.iterator();
			while (thisIterator.hasNext()) {
				int thisNext = thisIterator.next();
				int thatNext = thatIterator.next();
				if (thisNext < thatNext) {
					return -1;
				} else if (thisNext > thatNext) {
					return 1;
				}
			}
		}
		return 0;
	}
}
