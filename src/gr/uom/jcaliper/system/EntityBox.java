package gr.uom.jcaliper.system;

/**
 * @author Panagiotis Kouros
 */
public class EntityBox extends CratAttribute {

	public EntityBox(int id, String name, SystemClass originClass) {
		super(id, name, originClass);
	}

	@Override
	public String showBox(IEntityPool pool) {
		return name + "(" + box.showNamesUnboxed(pool) + ")";
	}

	@Override
	public final boolean isAtom() {
		return false;
	}

	@Override
	public final boolean isBox() {
		return true;
	}

}
