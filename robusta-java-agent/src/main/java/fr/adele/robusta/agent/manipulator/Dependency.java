package fr.adele.robusta.agent.manipulator;

public class Dependency {
	private String className;
	private String modifier; // Possible values: Private, Static, Public, Empty ("")
	private String origin; // Field, Method Parameter, Method Body, Superclass, Interface, Exception
	
	public final static String interfaceDependency = "Interface";
	public final static String superClassDependency = "SuperClass";
	public final static String fieldDependency = "Field";
	public final static String methodParamDependency = "Parameter";
	public final static String methodReturnDependency = "Return";
	public final static String methodBodyDependency = "Body";
	public final static String exceptionDependency = "Exception";
	
	public final static String noModifier = "Default";
	public final static String publicModifier = "Public";
	public final static String privateModifier = "Private";
	public final static String protectedModifier = "Protected";
	public final static String staticModifier = "Static";
	public final static String finalModifier = "Final";
	
	public Dependency(String className, String modifier, String origin) {
		super();
		this.className = className;
		this.modifier = modifier;
		this.origin = origin;
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		Dependency d = (Dependency) o;
		if (this.getClassName().equals(d.getClassName()) &&
				this.getModifier().equals(d.getModifier()) &&
				this.getOrigin().equals(d.getOrigin()))
			return true;
		return false;
	}
	
	@Override
	public int hashCode(){
		return this.className.hashCode() * this.getModifier().hashCode() * this.origin.hashCode();
	}
	
	
}
