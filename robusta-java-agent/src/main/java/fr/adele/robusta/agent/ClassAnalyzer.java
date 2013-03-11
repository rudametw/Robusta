package fr.adele.robusta.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import fr.adele.robusta.agent.manipulator.RobustaAnnotationVisitorPrinter;
import fr.adele.robusta.agent.manipulator.RobustaFieldVisitor;
import fr.adele.robusta.agent.manipulator.ServiceDepClassAdapter;

public class ClassAnalyzer implements ClassFileTransformer {

    // private final String[] ignore = new String[] {"sun/", "com/sun/",
    // "java/", "javax/" ,
    // "fr/adele/robusta/dependencygraph/ClassLoaderUtils$LOADER_HIERARCHY"};

//    private final String[] ignore = new String[] {"sun/", "com/sun/", "java/", "javax/", "org/apache/felix/utils/collections/DictionaryAsMap"};
    private final String[] ignore = new String[] {};

    // private final String[] ignore = new String[] {"sun/", "com/sun/",
    // "java/", "javax/", "org/ow2/chameleon/core/Main",
    // "org/osgi/framework/BundleException", "org/ow2/chameleon/core",
    // "org/slf4j", "ch/qos", "org/osgi/", "org/apache/felix",
    // "org/ungoverned/osgi", "org/mortbay/jetty", "org/json/",
    // "org/objectweb/asm", "jline/console/completer/Completer",
    // "org/mortbay/", "org/ow2/shelbie/core/internal/extension/",
    // "org/ow2/shelbie", "org/apache/commons/io/", "org/apache/felix/",
    // "jline/console/completer"};

    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {

        // System.out.println("[ROBUSTA] ANALYZER TRANSFORM");

        long initial_time = System.currentTimeMillis();

        if (classBeingRedefined != null) {
            System.out.println("[ROBUSTA] Class Redefinition or retransformation: " + className);
            System.err.println("[ROBUSTA] Class Redefinition or retransformation: " + className);
            RobustaJavaAgent.REDEFINED_CLASS_COUNT.incrementAndGet();
            // System.out.println("[ROBUSTA] Exiting retransform");
            // return null;
        }

        RobustaJavaAgent.CLASS_COUNT.getAndIncrement();

        printLoadedClassInfo(loader, className, classBeingRedefined);

        // TODO: CHECK BRACKETS

        // CHECK AGAINST IGNORED CLASSES BEFORE PERFORMING MANIPULATION!!!
        for (int i = 0; i < ignore.length; i++) {
            if (className.startsWith(ignore[i])) {
                RobustaJavaAgent.IGNORED_CLASS_COUNT.incrementAndGet();
                if (className.startsWith("fr/adele/robusta/dependencygraph/ClassLoaderUtils$LOADER_HIERARCHY")) {
                    System.out.println("[ROBUSTA] DO NOT INSTRUMENT. FOUND: " + className);
                    // System.err.println("[ROBUSTA] DO NOT INSTRUMENT. FOUND: "
                    // + className);
                }
                System.out.println("[ROBUSTA] DO NOT INSTRUMENT. FOUND: " + className);
                return null;
            }
        }
        RobustaJavaAgent.MODIFIED_CLASS_COUNT.incrementAndGet();

        byte[] newByteCode = null;
        try {
            newByteCode = manipulateBytecode(className, classfileBuffer);
        } catch (Throwable t) {
            System.out.println("ERROR: BYTECODE MANIPULATION FAAAAILED");
            System.err.println("ERROR: BYTECODE MANIPULATION FAAAAILED");
            t.printStackTrace();
        }

        long final_time = System.currentTimeMillis();
        long duration = final_time - initial_time;

        // if (verbose) {
        System.out.println("[ROBUSTA][TIME] " + duration + " miliseconds");

        return newByteCode;

        // Read class.
        // Get super class
        // Get implemented interfaces
        // Get ALL referenced classes

        // RETURN MODIFIED CLASS
        // return null;
    }

    private byte[] manipulateBytecode(final String className, final byte[] byteCode) {

        byte[] newByteCode;

        // Classes to be ignored and not included into __robustaFields
        final Set<String> ignoredClasses = new HashSet<String>();

        // Self class.
        final String ignoredClassName = "L" + className + ";";

        // Add self class to ignored
        ignoredClasses.add(ignoredClassName);
        // ignoredClasses.add("Lorg/apache/felix/utils/collections/DictionaryAsMap;");
        // ignoredClasses.add("Ljava/lang/Object;");
        // ignoredClasses.add(BytecodeAnalyzer.class.getCanonicalName());

        System.out.println("[ROBUSTA] Modifying class: " + className);

        // Usually here byteCode contains the bytecode of the class
        final ClassReader cr = new ClassReader(byteCode);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassAdapter ca = new ServiceDepClassAdapter(cw, ignoredClasses);
        cr.accept(ca, 0);

        newByteCode = cw.toByteArray();

        System.out.println("NewByteCode: " + newByteCode);
        // printClassFields(newByteCode);
        printClassAnnotations(newByteCode);
        return newByteCode;

    }

    public static void printClassFields(final byte[] newByteCode) {
        System.out.println("*** List of Fields ***");
        if (newByteCode == null) {
            System.out.println("Bytecode is NULL");
            return;
        }

        final ClassReader cr = new ClassReader(newByteCode);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassAdapter ca = new RobustaFieldVisitor(cw);
        cr.accept(ca, 0);
    }

    public static void printClassAnnotations(final byte[] newByteCode) {
        System.out.println("*** List of Annotations ***");
        if (newByteCode == null) {
            System.out.println("Bytecode is NULL");
            return;
        }

        final ClassReader cr = new ClassReader(newByteCode);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassAdapter ca = new RobustaAnnotationVisitorPrinter(cw);
        cr.accept(ca, 0);
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
