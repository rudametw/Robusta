package fr.adele.robusta.agent.manipulator;

import org.objectweb.asm.AnnotationVisitor;

public class PrintAnnotationVisitor implements AnnotationVisitor {

    final int paddingClazz = 40;

    final int padding = 20;

    public void visit(String arg0, Object arg1) {

        final int paddingSize;

        // System.out.print("ARG0:" + arg0 + "  ARG1:" + arg1);
        // if (arg0 == null) {
        //
        // }

        if (arg0.equals("clazz") || arg0.equals("className"))
            paddingSize = paddingClazz;
        else
            paddingSize = padding;

        String paddedString = padRight(arg1.toString(), paddingSize);

        System.out.print(arg0 + "=" + paddedString);

        if (arg0.equals("clazz"))
            System.out.println(" )");
        else
            System.out.print(" ");
    }

    public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
        // System.out.println("VISIT ANNOTATION: arg0:" + arg0 + " Object:" +
        // arg1);
        // System.out.println();
        // System.out.print("        @ClassDependency(" + arg1);
        System.out.print("        @ClassDependency(   ");
        return this;
    }

    public AnnotationVisitor visitArray(String arg0) {
        // System.out.println("VISIT ARRAY: arg0:" + arg0);
        return this;
    }

    public void visitEnd() {
        // System.out.println(")");
        // System.out.println();

    }

    // NEVER CALLED FOR ROBUSTA
    public void visitEnum(String arg0, String arg1, String arg2) {
        System.out.println("ENUM=arg0:" + arg0 + " arg1:" + arg1 + " arg2");
    }

    // Make output pretty
    public static String padRight(final String s, final int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(final String s, final int n) {
        return String.format("%1$" + n + "s", s);
    }
}
