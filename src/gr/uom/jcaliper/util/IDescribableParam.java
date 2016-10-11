package gr.uom.jcaliper.util;

import gr.uom.jcaliper.refactoring.LocalOptimum;

public interface IDescribableParam {

	public String detailedDescription(LocalOptimum optimum);

	public String shortDescription(LocalOptimum optimum);

	public String oneLineDescription(LocalOptimum optimum);

}
