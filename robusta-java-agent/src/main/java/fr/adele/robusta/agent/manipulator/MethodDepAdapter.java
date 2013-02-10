package fr.adele.robusta.agent.manipulator;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

public class MethodDepAdapter extends MethodAdapter {

	//public static Set<String> ignoredClasses;
	//public Set<String> dependencies;
	public ServiceDepClassAdapter classAdapter;
	
	public MethodDepAdapter(MethodVisitor mv, ServiceDepClassAdapter classAdapter) {
		super(mv);
		
		// I need the classAdapter reference in order to update the class dependencies. Ugly, but it works.
		this.classAdapter = classAdapter;
	}

	// I will jump the annotations, frames and unrelated things, we can implement it if you really need it.
	// public void visitInsn(int opcode) - Useless, we can not inspect the stack to see the real operands
	// public void visitIntInsn(int opcode, int intoperand) - Useless - possible instructions: BIPUSH, SIPUSH or NEWARRAY 
	// public void visitJumpInsn(int opcode, Label label) - Useless - they point towards a label
	// public void visitLabel(Label label) - Useless - it declares a new label
	// public void visitLineNumber(int line, Label startLabel) - Useless.
	// public void visitMaxs(int maxStack, int maxLocalVariables) - Useless, that's an information for the JVM about the stack
	// public void visitTableSwitchInsn(int minValue, int maxValue, Label default, Label[] handlersLabels) - Useless - it's for switches
	// public void visitLookupSwitchInsn(Label default, int[] keyValues, Label[] handlersLabels) - Useless - Same as above
	// public void visitVarInsn(int opcode, int localVariableIndex) - It doesn't inform the type; used to load and store variables from the stack
	
	public void visitLdcInsn(Object constant) {
		if (constant.toString().startsWith("L") && constant.toString().endsWith(";"))
			// It's a classname
			if (!classAdapter.ignoredClasses.contains(constant.toString()))
					classAdapter.dependencies.add(constant.toString());
		mv.visitLdcInsn(constant);
	}

	// Instructions: GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD - It cans manipulate fields from other classes as well
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {		
		classAdapter.dependencies.addAll(Utils.getClassNamesFromFieldDesc(desc, classAdapter.ignoredClasses));
		mv.visitFieldInsn(opcode, owner, name, desc);
	}

	// We can find more information about the local variables of a method
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		classAdapter.dependencies.addAll(Utils.getClassNamesFromFieldDesc(desc, classAdapter.ignoredClasses));
		if (signature != null)
			classAdapter.dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature, classAdapter.ignoredClasses));
		mv.visitLocalVariable(name, desc, signature, start, end, index);
	}

	// Method owner and method description may contain type references
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (!classAdapter.ignoredClasses.contains("L" + owner + ";")) {
			classAdapter.dependencies.add("L"+owner+";");
		}
		classAdapter.dependencies.addAll(Utils.getClassNamesFromMethodDesc(desc, classAdapter.ignoredClasses));
		mv.visitMethodInsn(opcode, owner, name, desc);
	}

	// To find the name of exception types
	// type = null -> is a "finally" block
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		if (!classAdapter.ignoredClasses.contains("L" + type + ";") && (type != null)) {
			classAdapter.dependencies.add("L"+type+";");
		}
		mv.visitTryCatchBlock(start, end, handler, type);
	}
	
	// The most useful type of instructions -> NEW, ANEWARRAY, CHECKCAST or INSTANCEOF
	// Some check cast instructions have real descriptions
	@Override
	public void visitTypeInsn(int opcode, String type) {
		if (!type.startsWith("[")) { 
			if (!classAdapter.ignoredClasses.contains("L"+ type + ";"))
				classAdapter.dependencies.add("L"+type+";");
		} else {
			classAdapter.dependencies.addAll(Utils.getClassNamesFromFieldDesc(type, classAdapter.ignoredClasses));
		}		
					
		mv.visitTypeInsn(opcode, type);

	}
	
	// Used to define arrays whose type is not primitive
	@Override
	public void visitMultiANewArrayInsn(String desc, int dimensions){
		classAdapter.dependencies.addAll(Utils.getClassNamesFromFieldDesc(desc, classAdapter.ignoredClasses));
		mv.visitMultiANewArrayInsn(desc, dimensions);
	}
	
	@Override
	public void visitEnd(){
		mv.visitEnd();
	}

}
