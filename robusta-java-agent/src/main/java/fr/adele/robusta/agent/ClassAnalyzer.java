package fr.adele.robusta.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ClassAnalyzer implements ClassFileTransformer {

    private String[] ignore = new String[] {"sun/", "java/", "javax/"};

    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {

        // System.out.println("[ROBUSTA] ANALYZER TRANSFORM");

        RobustaJavaAgent.CLASS_COUNT++;

        printLoadedClassInfo(loader, className, classBeingRedefined);

        // CHECK AGAINST IGNORED CLASSES BEFORE PERFORMING MANIPULATION!!!
        for (int i = 0; i < ignore.length; i++) {
            if (className.startsWith(ignore[i])) {
                return null;
            }
        }

        // TODO: PERFORM INSTRUMENTATION HERE:

        // Read class.
            // Get super class
            // Get implemented interfaces
            // Get ALL referenced classes

        //RETURN MODIFIED CLASS
        return null;
    }

    private static void printLoadedClassInfo(final ClassLoader loader, final String className,
            final Class<?> classBeingRedefined) {

        final String loaderName;

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
        } catch (final Exception e) { // Should no longer happen
            e.printStackTrace();
        }

    }

    public static String padRight(final String s, final int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(final String s, final int n) {
        return String.format("%1$" + n + "s", s);
    }

}
