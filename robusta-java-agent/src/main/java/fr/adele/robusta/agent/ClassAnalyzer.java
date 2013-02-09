package fr.adele.robusta.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ClassAnalyzer implements ClassFileTransformer {

    // public ClassAnalyzer() {
    // super();
    // System.out.println("[ROBUSTA] ANALYZER INSTANTIATED");
    // }
    // @Override
    // public byte[] transform(ClassLoader loader, String className, Class
    // classBeingRedefined,
    // ProtectionDomain protectionDomain, byte[] classfileBuffer) throws
    // IllegalClassFormatException {

    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {

        // public byte[] transform(final ClassLoader loader, final String
        // className, final Class<?> classBeingRedefined,
        // final ProtectionDomain protectionDomain, final byte[]
        // classfileBuffer) throws IllegalClassFormatException {

        // System.out.println("[ROBUSTA] ANALYZER TRANSFORM");

        RobustaJavaAgent.CLASS_COUNT++;

        String loaderName;
        if (loader == null) {
            loaderName = "bootstrap-classloader";
        } else {
            loaderName = loader.getClass().getName();
        }

        String classBeingRedefinedName;
        if (classBeingRedefined == null) {
            classBeingRedefinedName = "null";
        } else {
            classBeingRedefinedName = loader.getClass().getName();
        }

        try {
            System.out.println("[ROBUSTA] [Load] threadID=" + padRight(Thread.currentThread().toString(), 38)
                    + " classRedefined=" + padRight(classBeingRedefinedName, 5) + " className="
                    + padRight(className, 85) + " classloader=" + loaderName);
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static String padRight(final String s, final int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(final String s, final int n) {
        return String.format("%1$" + n + "s", s);
    }

    // public byte[] transform(ClassLoader loader, String className, Class<?>
    // classBeingRedefined,
    // ProtectionDomain protectionDomain, byte[] classfileBuffer) throws
    // IllegalClassFormatException {
    // // TODO Auto-generated method stub
    // return null;
    // }

}
