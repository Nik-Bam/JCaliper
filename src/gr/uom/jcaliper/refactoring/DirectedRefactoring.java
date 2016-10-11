package gr.uom.jcaliper.refactoring;

import gr.uom.jcaliper.system.CratPackage;
import gr.uom.jcaliper.system.SystemClass;

public abstract class DirectedRefactoring extends Refactoring {

	protected SystemClass origin;
	protected RefactoredClass target;
	protected CratPackage refPackage;

	public DirectedRefactoring(SystemClass origin, RefactoredClass target) {
		super();
		this.origin = origin;
		this.target = target;
		refPackage = origin.getPackage();
	}

	public SystemClass getOrigin() {
		return origin;
	}

	public void setOrigin(SystemClass origin) {
		this.origin = origin;
	}

	public RefactoredClass getTarget() {
		return target;
	}

	public void setTarget(RefactoredClass target) {
		this.target = target;
	}

	public CratPackage getRefPackage() {
		return refPackage;
	}

	public void setRefPackage(CratPackage refPackage) {
		this.refPackage = refPackage;
	}

	public double getSystemBenefit() {
		return systemBenefit;
	}

	public void setSystemBenefit(double systemBenefit) {
		this.systemBenefit = systemBenefit;
	}

	@Override
	public String toString() {
		return "DirectedRefactoring [origin=" + origin + ", target=" + target + ", systemBenefit=" + systemBenefit
				+ "]";
	}
}
