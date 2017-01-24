package gr.uom.jcaliper.filters;

import gr.uom.jcaliper.explorer.CratState;
import gr.uom.jcaliper.system.CratSystem;

public interface IFilter {

    public void applyFilter(CratState state, CratSystem system);

}
