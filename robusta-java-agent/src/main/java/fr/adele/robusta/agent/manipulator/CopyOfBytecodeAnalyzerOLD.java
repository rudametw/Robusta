package fr.adele.robusta.agent.manipulator;

//TODO: DELETE

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class CopyOfBytecodeAnalyzerOLD {

    public static void main(final String[] args) {
        // Args = Array of class files

        byte[] newByteCode = null;

        // Opens the class-file
        for (final String classFile : args) {
            final File file = new File(classFile);
            try {
                FileInputStream fin = null;
                fin = new FileInputStream(file);
                final byte[] byteCode = new byte[(int) file.length()];
                // byte[] buf = new byte[(int) jarentry.getSize()];
                int offset = 0, numRead = 0;
                while ((offset < byteCode.length)
                       && ((numRead = fin.read(byteCode, offset, byteCode.length - offset)) >= 0)) {
                    offset += numRead;
                }

                // Up to you to create your set of ignored classes properly :P
                final Set<String> ignoredClasses = new HashSet<String>();
                ignoredClasses.add("Ljava/lang/Object;");
                ignoredClasses.add(CopyOfBytecodeAnalyzerOLD.class.getCanonicalName());
                // Usually here byteCode contains the bytecode of the class
                final ClassReader cr = new ClassReader(byteCode);
                final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                final ClassAdapter ca = new ServiceDepClassAdapter(cw, ignoredClasses);
                cr.accept(ca, 0);

                // Modified bytecode (with the fields)
                // @SuppressWarnings("unused")
                newByteCode = cw.toByteArray();

                // new Class();
                // BytecodeAnalyer.class.getClassLoader().loadClass(name);

                // Closes the
                fin.close();

                System.out.println("NewByteCode: " + newByteCode);
                printClassFields(newByteCode);

            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                newByteCode = null;
            }
        }
    }

    public static void printClassFields(final byte[] newByteCode) {
        System.out.println("List of Fields");
        if (newByteCode == null) {
            System.out.println("Bytecode is NULL");
            return;
        }

        final ClassReader cr = new ClassReader(newByteCode);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassAdapter ca = new RobustaFieldVisitor(cw);
        cr.accept(ca, 0);
    }

}
