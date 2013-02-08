package fr.adele.robusta.dependencygraph;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * This class stores information regarding the dependencies that this class has among other classes.
 *
 * @author rudametw
 */
public class ClassDescription {

	// SuperClass (always one, at least object)
	private final Class<?> superClass;

	// implemented interfaces List
	private final List<Class<?>> interfaces;

	// Referenced Classes List
	private final List<Class<?>> references;

	// ReferencedBy Classes List
	private List<Class<?>> referencedBy;

	// ReferencedBy Classes List
	private List<Class<?>> subClasses;

	//what about innerclasses?

	// public ClassDescription(Class<?> superClass, List<Class<?>> interfaces, List<Class<?>> subClasses, List<Class<?>>
	// references, List<Class<?>> referencedBy) {
	public ClassDescription(Class<?> clazz) {
		super();
		this.superClass = getSuperClass(clazz);
		this.interfaces = getInterfaces(clazz);
		this.subClasses = getSubClasses(clazz);
		this.references = getReferences(clazz);
		// this.referencedBy = referencedBy(clazz);
	}

	private List<Class<?>> getReferences(Class<?> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Class<?>> getSubClasses(Class<?> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Class<?>> getInterfaces(Class<?> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	private Class<?> getSuperClass(Class<?> clazz) {
		Class<?> superClass = clazz.getSuperclass();
		return superClass;
	}

	public static String spyFields(Object obj) throws IllegalAccessException {
		StringBuffer buffer = new StringBuffer();
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field f : fields) {
			if (!Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				Object value = f.get(obj);
				buffer.append(f.getType().getName());
				buffer.append(" ");
				buffer.append(f.getName());
				buffer.append("=");
				buffer.append("" + value);
				buffer.append("\n");
			}
		}
		return buffer.toString();
	}

}
