package fr.adele.robusta.agent.manipulator;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ServiceDepClassAdapter extends ClassAdapter{
	
	public Set<Dependency> dependencies = null;
	//public Set<String> interfacesAndSuperclasses = null;
	public Set<String> ignoredClasses = null;
	
	
	public ServiceDepClassAdapter(ClassVisitor cv, Set<String> ignoredClasses){
		super(cv);
		this.dependencies = new HashSet<Dependency>();
		//this.interfacesAndSuperclasses = new HashSet<String>();
		this.ignoredClasses = ignoredClasses;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// Here we can find the superclass name and the implemented interfaces
		
		// Adds the name of the class in the ignored classes
		ignoredClasses.add("L" + name + ";");
		
		if (superName != null) {
			if (!ignoredClasses.contains("L"+superName+";")) {
				dependencies.add(new Dependency("L"+superName+";", "", Dependency.superClassDependency));
				//interfacesAndSuperclasses.add(new String("L"+superName+";"));
			}
		}
		
		if (interfaces.length > 0) {
			for (String interfaceName : interfaces) {
				if (!ignoredClasses.contains("L"+interfaceName+";")) {
					dependencies.add(new Dependency("L"+interfaceName+";", "", Dependency.interfaceDependency));
					//interfacesAndSuperclasses.add(new String("L"+interfaceName+";"));
				}
			}
		}
		
		cv.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public void visitEnd() {
		// Adding one field per dependency
		for (Dependency dependency : dependencies){
			String fieldName = "__robusta_" + dependency.getModifier().trim() + "_" + 
					dependency.getOrigin().trim() + "_" + Utils.getFriendlyName(dependency.getClassName());
			System.out.println(fieldName);
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, fieldName, dependency.getClassName(), null, null);
			if (fv != null){
				fv.visitEnd();
			}
		}
		
		// Printing results
		//System.out.println("-- ALL DEPENDENCIES --");
		//Utils.print(dependencies);
		//System.out.println("-- SUPER DEPENDENCIES --");
		//Utils.print(interfacesAndSuperclasses);
		
		cv.visitEnd();
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
		// For each field, we are going to retrieve its dependencies
		String modifier = Utils.getModifier(access);
		dependencies.addAll(Utils.getClassNamesFromFieldDesc(desc, ignoredClasses, modifier, Dependency.fieldDependency));
		if (signature != null)
			dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature, ignoredClasses, modifier, Dependency.fieldDependency));
		
		return cv.visitField(access, name, desc, signature, value);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// We must parse both the method description and the method instructions		
		String modifier = Utils.getModifier(access);
		
		// First the method description
		int returnIndex = desc.indexOf(")");
		
		dependencies.addAll(Utils.getClassNamesFromMethodDesc(desc.substring(0, returnIndex), ignoredClasses, modifier, Dependency.methodParamDependency));
		//System.out.println("Return = " + desc.substring(returnIndex));
		dependencies.addAll(Utils.getClassNamesFromMethodDesc(desc.substring(returnIndex), ignoredClasses, modifier, Dependency.methodReturnDependency));
		
		if (signature != null) {
			returnIndex = signature.indexOf(")");
			dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature.substring(0, returnIndex), ignoredClasses, modifier, Dependency.methodParamDependency));
			//System.out.println("Return = " + signature.substring(returnIndex));
			dependencies.addAll(Utils.getClassNamesFromMethodDesc(signature.substring(returnIndex), ignoredClasses, modifier, Dependency.methodReturnDependency));
		}

		if (exceptions != null)
			for (String exception : exceptions)
				if (!ignoredClasses.contains(exception))
					dependencies.add(new Dependency("L"+exception+ ";", modifier, Dependency.exceptionDependency));
		
		// Now the method content
		MethodVisitor mv;
		mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if (mv != null) {
			mv = new MethodDepAdapter(mv, this, modifier);
		}
		return mv;
	}
	
}
