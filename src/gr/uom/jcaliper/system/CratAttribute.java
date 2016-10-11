package gr.uom.jcaliper.system;

/**
 * @author Panagiotis Kouros
 */
public class CratAttribute extends CratEntity {

	/**
	 * @param id
	 * @param name
	 * @param originClass
	 */
	public CratAttribute(int id, String name, SystemClass originClass) {
		super(id, name, originClass);
	}

	@Override
	public final boolean isAttribute() {
		return true;
	}

	@Override
	protected void updateMovability() {
		/*
		 * An attribute is "immovable" when
		 * - belongs to a superclass
		 * - belongs to an Interface
		 * - is 'orphan'(= has empty entitySet and no relatives)
		 * - is static (experimental)
		 */
		if (originClass.isSuperclass() || originClass.isInterface() || isOrphan() || isStatic())
			movable = false;
		else
			movable = true;
	};

	@Override
	public String propertiesToText() {
		StringBuilder sb = new StringBuilder();
		if (isStatic())
			sb.append("static ");
		sb.append("attribute");
		if (!isMovable())
			sb.append(" (immovable)");
		return sb.toString();
	}

	@Override
	public String getType() {
		return "attribute ";
	}

	@Override
	public void updateSimilarity() {
		double similarity = Dice.StringSimilarity(name.toUpperCase(), originClass.getName()
				.toUpperCase());
		similarityToOriginClass = 5 + (10 * similarity);
	}

}
