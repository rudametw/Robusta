package fr.adele.robusta.agent;

import java.lang.instrument.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RobustaJavaAgent {

    private static final Object INSTRUMENTATION_UUID = UUID.fromString("021df202-6bb0-11e2-8f99-5c260a385954");

    public static volatile AtomicLong CLASS_COUNT = new AtomicLong();
    public static volatile AtomicLong MODIFIED_CLASS_COUNT = new AtomicLong();
    public static volatile AtomicLong IGNORED_CLASS_COUNT = new AtomicLong();
    public static volatile AtomicLong REDEFINED_CLASS_COUNT = new AtomicLong();

    public static Instrumentation getInstrumentation() {
		Instrumentation inst = (Instrumentation) System.getProperties().get(RobustaJavaAgent.INSTRUMENTATION_UUID);

		// System.out.println("Instrumentation = " + inst);
		// try {
		// System.out.println("ROBUSTA: Total number of classes: " + inst.getAllLoadedClasses().length);
		// } catch (NullPointerException e) {
		// System.err.println("ROBUSTA: inst is not valid, no classes");
		// return null;
		// }

		System.out.println("[ROBUSTA] Total number of loaded classes intercepted: " + CLASS_COUNT);

		return inst;
	}

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("ROBUSTA: RobustaJavaAgent" + inst.getClass() + ": " + inst);
        // RobustaJavaAgent.inst = inst;

        inst.addTransformer(new ClassAnalyzer());
        System.out.println("[ROBUSTA] Added ClassAnalyzer");

        System.out.println("[ROBUSTA] Total number of loaded classes intercepted: " + CLASS_COUNT);

        try {
            System.out.println("ROBUSTA: Total number of classes: " + inst.getAllLoadedClasses().length);
        } catch (NullPointerException e) {
            System.err.println("ROBUSTA: inst is not valid, no classes");
            return;
        }

        // Make instrumentation reachable from Robusta Shelbie Command
        System.getProperties().put(INSTRUMENTATION_UUID, inst);

    }
}
