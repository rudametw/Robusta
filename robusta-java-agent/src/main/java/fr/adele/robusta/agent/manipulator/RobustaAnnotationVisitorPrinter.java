package fr.adele.robusta.agent.manipulator;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

public class RobustaAnnotationVisitorPrinter extends ClassAdapter {

    private String robustaAnnotation = "Lfr/adele/robusta/annotations/Robusta;";

    private String classDependencyAnnotation = "Lfr/adele/robusta/annotations/ClassDependency;";

    public RobustaAnnotationVisitorPrinter(ClassVisitor cv) {
        super(cv);
    }

    /**
     * Method used to print all annotations of a class.
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        System.out.println("ANNOTATION->DESC:" + desc + " visible:" + visible);

        if (desc.equals(robustaAnnotation)) {
            System.out.println("  @ROBUSTA: ");
            // cv.visitAnnotation(arg0, arg1)
            // Checkout nested annotation here!

            return new PrintAnnotationVisitor();

            // AnnotationVisitor av = cv.visitAnnotation(robustaAnnotation,
            // true);
            //
            // System.out.println("AV: " + av);
            //
            // AnnotationVisitor av2 = av.visitArray("dependencies");
            // System.out.println("AV2: " + av2);
            //
            // AnnotationVisitor av3 = av2.visitAnnotation("dependencies",
            // classDependencyAnnotation);
            //
            // System.out.println("AV3: " + av3);

        }
        return null;
        // return cv.visitAnnotation(desc, visible);

        // For each field, we are going to retrieve its dependencies
        // dependencies.addAll(Utils.getClassNamesFromFieldDesc(desc,
        // ignoredClasses));
        // if (signature != null)
        // dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature,
        // ignoredClasses));
        //
        // return cv.visitField(access, name, desc, signature, value);
        // return null;
    }
}
