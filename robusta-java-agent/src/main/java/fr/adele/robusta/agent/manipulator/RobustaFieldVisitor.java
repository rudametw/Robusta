package fr.adele.robusta.agent.manipulator;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

public class RobustaFieldVisitor extends ClassAdapter {

    public RobustaFieldVisitor(ClassVisitor cv) {
        super(cv);
    }

    // public Set<String> dependencies = null;
    //
    // public Set<String> interfacesAndSuperclasses = null;
    //
    // public Set<String> ignoredClasses = null;

    // public RobustaFieldVisitor(ClassVisitor cv, Set<String> ignoredClasses) {
    // super(cv);
    // this.dependencies = new HashSet<String>();
    // this.interfacesAndSuperclasses = new HashSet<String>();
    // this.ignoredClasses = ignoredClasses;
    // }

    /**
     * Method used to print all fields of a class.
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        System.out.println("ACCESS:" + access + " NAME:" + name + " DESC:" + desc + " SIGNATURE:" + signature
                           + " VALUE:" + value);
        // For each field, we are going to retrieve its dependencies
        // dependencies.addAll(Utils.getClassNamesFromFieldDesc(desc,
        // ignoredClasses));
        // if (signature != null)
        // dependencies.addAll(Utils.getClassNamesFromFieldDesc(signature,
        // ignoredClasses));
        //
        return cv.visitField(access, name, desc, signature, value);
        //return null;
    }
}
