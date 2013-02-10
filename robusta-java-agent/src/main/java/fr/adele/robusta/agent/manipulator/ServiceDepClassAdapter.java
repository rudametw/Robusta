package fr.adele.robusta.agent.manipulator;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ServiceDepClassAdapter extends ClassAdapter{
	
	public Set<String> dependencies = null;
	public Set<String> interfacesAndSuperclasses = null;
	public Set<String> ignoredClasses = null;
	
	public ServiceDepClassAdapter(ClassVisitor cv, Set<String> ignoredClasses){
		super(cv);
		this.dependencies = new HashSet<String>();
		this.interfacesAndSuperclasses = new HashSet<String>();
		this.ignoredClasses = ignoredClasses;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// Here we can find the superclass name and the implemented interfaces
		
		// Adds the name of the class in the ignored classes
		ignoredClasses.add("L" + name + ";");
		
		if (superName != null) {
			if (!ignoredClasses.contains("L"+superName+";")) {
				dependencies.add(new String("L"+superName+";"));
				interfacesAndSuperclasses.add(new String("L"+superName+";"));
			}
		}
		
		if (interfaces.length > 0) {
			for (String interfaceName : interfaces) {
				if (!ignoredClasses.contains("L"+interfaceName+";")) {
					dependencies.add(new String("L"+interfaceName+";"));
					interfacesAndSuperclasses.add(new String("L"+interfaceName+";"));
				}
			}
		}
		
		cv.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public void visitEnd() {
		// Adding one field per dependency
		for (String dependencyClass : dependencies){
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "__robustaField"+Utils.getFriendlyName(dependencyClass), dependencyClass, null, null);
			if (fv != null){
				fv.visitEnd();
			}
		}
		
		// Printing results
		System.out.println("-- ALL DEPENDENCIES --");
		Utils.print(dependencies);
		System.out.println("-- SUPER DEPENDENCIES --");
		Utils.print(interfacesAndSuperclasses);
		
		cv.visitEnd();
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
		// For each field, we are going to retrieve its dependencies		
		dependencies.addAll(Utils.getClassNamesFromFieldDesc(desc, ignoredClasses));
		if (signature != null)
			dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature, ignoredClasses));
		
		return cv.visitField(access, name, desc, signature, value);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// We must parse both the method description and the method instructions
		
		// First the method description
		dependencies.addAll(Utils.getClassNamesFromMethodDesc(desc, ignoredClasses));
		if (signature != null)
			dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature, ignoredClasses));

		if (exceptions != null)
			for (String exception : exceptions)
				if (!ignoredClasses.contains(exception))
					dependencies.add("L"+exception+ ";");
		
		// Now the method content
		MethodVisitor mv;
		mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if (mv != null) {
			mv = new MethodDepAdapter(mv, this);
		}
		return mv;
	}
	
}
