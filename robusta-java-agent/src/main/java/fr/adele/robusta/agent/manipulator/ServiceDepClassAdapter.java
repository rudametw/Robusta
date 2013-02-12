package fr.adele.robusta.agent.manipulator;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ServiceDepClassAdapter extends ClassAdapter {

    public Set<Dependency> dependencies = null;

    String CLASS_NAME = null;

    // public Set<String> interfacesAndSuperclasses = null;
    public Set<String> ignoredClasses = null;

    public ServiceDepClassAdapter(final ClassVisitor cv, final Set<String> ignoredClasses) {
        super(cv);
        dependencies = new HashSet<Dependency>();
        // this.interfacesAndSuperclasses = new HashSet<String>();
        this.ignoredClasses = ignoredClasses;
    }

//    public boolean isContained(String checker){
//
//    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
                      final String superName, final String[] interfaces) {
        // Here we can find the superclass name and the implemented interfaces

        CLASS_NAME = name;

        // Adds the name of the class in the ignored classes
        ignoredClasses.add("L" + name + ";");

        if (superName != null) {
            if (!ignoredClasses.contains("L" + superName + ";")) {
                dependencies.add(new Dependency("L" + superName + ";", "", Dependency.superClassDependency));
                // interfacesAndSuperclasses.add(new String("L"+superName+";"));
            }
        }

        if (interfaces.length > 0) {
            for (final String interfaceName : interfaces) {
                if (!ignoredClasses.contains("L" + interfaceName + ";")) {
                    dependencies.add(new Dependency("L" + interfaceName + ";", "", Dependency.interfaceDependency));
                    // interfacesAndSuperclasses.add(new
                    // String("L"+interfaceName+";"));
                }
            }
        }

        // int a = Opcodes.V1_5;

        int v = (version & 0xFF) < Opcodes.V1_5 ? Opcodes.V1_5 : version;

        System.out.println("*****VERSION IS HERE: " + (version & 0xFF));

        cv.visit(v, access, name, signature, superName, interfaces);
    }

    private String robustaAnnotation = "Lfr/adele/robusta/annotations/Robusta;";

    private String classDependencyAnnotation = "Lfr/adele/robusta/annotations/ClassDependency;";

    @Override
    public void visitEnd() {
        // Adding one field per dependency
        // for (Dependency dependency : dependencies){
        // String fieldName = "__robusta_" + dependency.getModifier().trim() +
        // "_" +
        // dependency.getOrigin().trim() + "_" +
        // Utils.getFriendlyName(dependency.getClassName());
        // System.out.println(fieldName);
        // FieldVisitor fv =
        // cv.visitField(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, fieldName,
        // dependency.getClassName(), null, null);
        // if (fv != null){
        // fv.visitEnd();
        // }
        // }

        // //Remove these lines to continue adding annotations
        // cv.visitEnd();
        // if(true) return;

        // ///// TODO: ADD ANNOTATIONS TO CLASS HEEERREEEEE //////////////
        final AnnotationVisitor av = cv.visitAnnotation(robustaAnnotation, true);

        final AnnotationVisitor av1 = av.visitArray("value");

        for (final Dependency dependency : dependencies) {
            // System.out.println();

            if(Type.getType(dependency.getClassName()).getClassName().startsWith("[")){
//                System.out.println();
                continue;

            }

            final AnnotationVisitor av2 = av1.visitAnnotation(null, classDependencyAnnotation);

            // Simple version
            // System.out.print("[ROBUSTA] ADDING DEPENDENCY: ");
            // System.out.print(" IN CLASS: " + CLASS_NAME);
            // System.out.print("  className: " + "CLASSNAME  ");
            // System.out.print("modifier: " + "MODIFIER   ");
            // System.out.print("origin: " + "ORIGIN   ");
            // System.out.print("clazz: " + String.class.getName() + " ### ");

            try {
                System.out.println("clazz: " + Type.getType(String.class));
            } catch (Throwable t) {
                System.out.println(" CAAAQUEEEOOOO");
                t.printStackTrace();
            }

            System.out.print("[ROBUSTA] ADDING DEPENDENCY: ");
            System.out.print(" IN CLASS: " + CLASS_NAME);
            System.out.print("  className: " + Type.getType(dependency.getClassName()).getClassName());
            System.out.print("  modifier: " + dependency.getModifier());
            System.out.print("  origin: " + dependency.getOrigin());
            System.out.print("  clazz: " + Type.getType(dependency.getClassName()));

            av2.visit("className", Type.getType(dependency.getClassName()).getClassName());
            av2.visit("modifier", dependency.getModifier());
            av2.visit("origin", dependency.getOrigin());
            av2.visit("clazz", Type.getType(dependency.getClassName()));
            // av2.visit("clazz", null);

            // av2.visit("className", "CLASSNAME");
            // av2.visit("modifier", "MODIFIER");
            // av2.visit("origin", "ORIGIN");
            // av2.visit("clazz", Type.getType(String.class));
            // // av2.visit("clazz", "L");
            av2.visitEnd();
            //
            // break;
        }

        av1.visitEnd();
        av.visitEnd();

        // Printing results
        // System.out.println("-- ALL DEPENDENCIES --");
        // Utils.print(dependencies);
        // System.out.println("-- SUPER DEPENDENCIES --");
        // Utils.print(interfacesAndSuperclasses);

        // BROKEN
        // cv.visitField(Opcodes.ACC_PRIVATE, "broken", "broken2", null, null);
        cv.visitEnd();
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
                                   final Object value) {
        // For each field, we are going to retrieve its dependencies
        final String modifier = Utils.getModifier(access);
        dependencies.addAll(Utils.getClassNamesFromFieldDesc(desc, ignoredClasses, modifier, Dependency.fieldDependency));
        if (signature != null) {
            dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature, ignoredClasses, modifier,
                                                                 Dependency.fieldDependency));
        }

        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                                     final String[] exceptions) {
        // We must parse both the method description and the method instructions
        final String modifier = Utils.getModifier(access);

        // First the method description
        int returnIndex = desc.indexOf(")");

        dependencies.addAll(Utils.getClassNamesFromMethodDesc(desc.substring(0, returnIndex), ignoredClasses, modifier,
                                                              Dependency.methodParamDependency));
        // System.out.println("Return = " + desc.substring(returnIndex));
        dependencies.addAll(Utils.getClassNamesFromMethodDesc(desc.substring(returnIndex), ignoredClasses, modifier,
                                                              Dependency.methodReturnDependency));

        if (signature != null) {
            returnIndex = signature.indexOf(")");
            dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature.substring(0, returnIndex), ignoredClasses,
                                                                 modifier, Dependency.methodParamDependency));
            // System.out.println("Return = " +
            // signature.substring(returnIndex));
            dependencies.addAll(Utils.getClassNamesFromMethodDesc(signature.substring(returnIndex), ignoredClasses,
                                                                  modifier, Dependency.methodReturnDependency));
        }

        if (exceptions != null) {
            for (final String exception : exceptions) {
                if (!ignoredClasses.contains(exception)) {
                    dependencies.add(new Dependency("L" + exception + ";", modifier, Dependency.exceptionDependency));
                }
            }
        }

        // Now the method content
        MethodVisitor mv;
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new MethodDepAdapter(mv, this, modifier);
        }
        return mv;
    }

}
