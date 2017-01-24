package gr.uom.jcaliper.system;

/**
 * Any entity collection that implements some methods
 *
 * @author Panagiotis Kouros
 */
public interface IEntityPool {

    public EntitySet getEntitySet();

    public CratEntity getEntity(int entityId);

    public int getTotalEntities();

}
