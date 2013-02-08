package fr.adele.robusta;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.Ansi;

import com.google.common.base.Throwables;

import fr.adele.robusta.agent.RobustaJavaAgent;
import fr.adele.robusta.dependencygraph.ClassLoaderUtils;
import fr.adele.robusta.dependencygraph.ClassTree;
import fr.adele.robusta.dependencygraph.ClassUtils;
import fr.adele.robusta.internal.util.AnsiPrintToolkit;

@Component
@Command(name = "class",
		scope = "robusta",
		description = "Command to get all loaded classes and dump them to console")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class ClassAction implements Action {

	@Option(name = "-sort",
			aliases = { "--sort" },
			required = false,
			description = "Sort output")
	private boolean sort = false;

	@Option(name = "-debug",
			aliases = { "--debug" },
			required = false,
			description = "Include debugging output")
	private boolean debug = false;

	@Option(name = "-v",
			aliases = { "--verbose" },
			required = false,
			description = "Verbose output")
	private boolean verbose = false;

	@Option(name = "-s",
			aliases = { "--stats" },
			required = false,
			description = "Print stats regarding number of classes and classloaders")
	private boolean stats = false;

	@Option(name = "-c",
			aliases = { "--classes" },
			required = false,
			description = "Print all classes")
	private boolean classes = false;

	@Option(name = "-cl",
			aliases = { "--include-classloader" },
			required = false,
			description = "Print all classes")
	private boolean classesWithCl = false;

	@Option(name = "-d",
			aliases = { "--duplicates" },
			required = false,
			description = "Print duplicated classes (using class names to sort)")
	private boolean duplicates = false;

	@Option(name = "-D",
			aliases = { "--duplicates-by-cl" },
			required = false,
			description = "Print duplicated classes (using classloader to sort)")
	private boolean duplicatesByCL = false;

	@Option(name = "-n",
			aliases = { "--show-numbers" },
			required = false,
			description = "Show line numbers")
	private boolean numbers = false;

	@Option(name = "-a",
			aliases = { "--all" },
			required = false,
			description = "Dump all information")
	private boolean all = false;

	@Option(name = "-t",
			aliases = { "--tree" },
			required = false,
			description = "Dump all information")
	private boolean tree = false;

	@Option(name = "-gc",
			aliases = { "--garbage-collection" },
			required = false,
			description = "Instruct JVM to attempt garbage collection *before* calculating dependencies (this cannot guarantee GC --> see Java Spec)")
	private boolean gc = false;

	/* Used for colorful output */
	AnsiPrintToolkit toolkit;
	Ansi buffer;

	public Object execute(final CommandSession session) throws Exception {
		toolkit = new AnsiPrintToolkit();
		buffer = toolkit.getBuffer();

		try {
			if (debug) {
				verbose = true;
			}

			// Garbage collect BEFORE any calculations
			if (gc) {
				collectGarbage();
			}

			if (all) {
				stats = true;
				classes = true;
				numbers = true;
				classesWithCl = true;
			}

			// If nothing is set, we should print stats
			if (!classes && !stats && !duplicates && !duplicatesByCL && !gc && !tree) {
				stats = true;
			}

			if (classes) {
				dumpAllClasses();
			}

			if (tree) {
				printTreeNodes();
			}
			if (duplicates || duplicatesByCL) {
				printDuplicateClasses();
			}

			if (stats) {
				printStats();
			}

		} catch (final Throwable e) {
			// Send stacktrace to buffer!
			final String stackTrace = Throwables.getStackTraceAsString(e);
			toolkit.red(stackTrace);
			toolkit.eol();
		} finally {
			// Flush buffer to console
			final PrintStream stream = System.out;
			stream.println(toolkit.getBuffer().toString());

			// release buffer and toolkit (just in case)
			toolkit = null;
			buffer = null;
		}
		return null;
	}

	private void printTreeNodes() {
		ClassTree tree1 = new ClassTree(fr.adele.robusta.dependencygraph.ClassloaderNode.class);
		ClassTree tree2 = new ClassTree(ClassTree.class);
		printTreeNode(tree1);
		printTreeNode(tree2);
		printTreeNode(new ClassTree(HashMap.class));
		printTreeNode(new ClassTree(Class.class));
	}
	private void printTreeNode(ClassTree tree) {
		toolkit.subtitle("Printing tree");
		List<Class<?>> children = tree.getOrderedList();

		for (Object o : children) {
			Class<?> child = (Class<?>) o;
			toolkit.white(child.getName());
			toolkit.eol();
		}
	}

	private void collectGarbage() {
		final long time1 = System.currentTimeMillis();
		toolkit.title("Garbage Collection");

		System.gc();
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.gc();

		final long time2 = System.currentTimeMillis();
		toolkit.indent(2);
		buffer.a("Garbage Collection in " + (time2 - time1) + " miliseconds");
		toolkit.eol();
	}

	private int countDuplicates() {
		final List<String> allClasses = new ArrayList<String>();
		Set<String> classes;

		for (final Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {
			allClasses.add(clazz.getName());
		}

		classes = new HashSet<String>(allClasses);

		return (allClasses.size() - classes.size());
	}

	private void dumpAllClasses() {
		int i = 0;
		for (final Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {
			if (numbers) {
				// System.out.print(++i + ":");
				buffer.a(++i);
				toolkit.separator();
			}
			if (classesWithCl) {
				if (clazz.getClassLoader() != null) {
					// System.out.print(clazz.getClassLoader().toString() + ":");
					buffer.a(clazz.getClassLoader().toString());
					toolkit.separator();
				} else {
					buffer.a("bootstrap");
					// toolkit.eol();
					toolkit.separator();
				}
			}
			buffer.a(clazz.getName());
			toolkit.eol();
		}
	}

	private Set<ClassLoader> getAllClassloaders() {

		final Set<ClassLoader> allClassloaders = new HashSet<ClassLoader>();
		Set<ClassLoader> newFound = new HashSet<ClassLoader>(getInitialClassloaders());

		if (verbose) {
			toolkit.title("Find missing classloaders (they appear in red)");
		}

		// breaks loop when no more classloaders are found
		boolean found_new_loader = false;

		// used for stats only
		int iterations = 0;

		if (newFound.size() > 0) {
			found_new_loader = true;
		}
		if (debug) {
			toolkit.debug("Initial ClassLoader size: " + newFound.size());
		}

		while (found_new_loader) {

			final List<ClassLoader> currentClassloaders = new ArrayList<ClassLoader>(newFound);

			// Start with an empty new found list!
			newFound = new HashSet<ClassLoader>();

			for (final ClassLoader loader : currentClassloaders) {
				final ClassLoader parent = ClassLoaderUtils.getClassLoaderParent(loader);
				final ClassLoader loaderLoader = ClassLoaderUtils.getClassLoaderLoader(loader);

				final String loaderName = ClassLoaderUtils.getClassLoaderName(loader);
				final String parentName = ClassLoaderUtils.getClassLoaderParentName(loader);
				final String loaderLoaderName = ClassLoaderUtils.getClassLoaderLoaderName(loader);

				iterations++;

				if (debug) {
					toolkit.debug("NAMES loader: " + loader + " loaderLoader: " + loaderLoader + " parent: " + parent);
				}

				// If new classloader found!
				// if (!currentClassloaders.contains(loaderLoader)) {
				if (!isClassLoaderKnown(allClassloaders, currentClassloaders, newFound, loaderLoader)) {
					if (verbose) {
						toolkit.urgent("NEW Missing ClassLoaderLoader FOUND -->" + loaderLoaderName + "value: " + loaderLoader);
					}
					newFound.add(loaderLoader);
					if (debug) {
						toolkit.debug("Initial ClassLoader size: " + currentClassloaders.size() + " New found list of Classloaders: " + newFound.size());
					}
				} else {
					// classloaders.
					if (debug) {
						toolkit.blue("OLDCLL--> " + loaderLoaderName);
					}
				}

				// If new classloader found!
				// if (!classloaders.contains(parent)) { // New classloader!
				if (!isClassLoaderKnown(allClassloaders, currentClassloaders, newFound, parent)) {
					toolkit.indent();
					if (verbose) {
						toolkit.urgent("NEW Missing ParentClassLoader FOUND --> " + parentName + " value: " + parent);
					}
					newFound.add(parent);
					if (debug) {
						toolkit.debug("Initial ClassLoader size: " + currentClassloaders.size() + " New found list of Classloaders: " + newFound.size());
					}
				} else {
					if (debug) {
						toolkit.indent();
						toolkit.blue("OLDPCL--> " + parentName);
						toolkit.eol();
					}
				}

				if (verbose) {
					if (loaderLoader == parent) {
						toolkit.green("[SAME]");
					} else {
						toolkit.cyan("[DIFF]");
					}
				}
				if (verbose) {
					ClassLoaderUtils.printClassloaderListEntry(toolkit, verbose, loaderName, parentName, loaderLoaderName);
				}
			}
			if (debug) {
				toolkit.urgent("Initial ClassLoader size: " + currentClassloaders.size() + " New found list of Classloaders: " + newFound.size());
				toolkit.debug("Iterations: " + iterations);
				toolkit.debug("Last value added to currentClassLoaders: " + currentClassloaders.get(currentClassloaders.size() - 1));
			}

			for (final ClassLoader loader : newFound) {
				final String loaderName = ClassLoaderUtils.getClassLoaderName(loader);
				final String parentName = ClassLoaderUtils.getClassLoaderParentName(loader);
				final String loaderLoaderName = ClassLoaderUtils.getClassLoaderLoaderName(loader);
				if (debug) {
					toolkit.yellow("New Found entry to be checked: ");
				}
				if (debug) {
					ClassLoaderUtils.printClassloaderListEntry(toolkit, debug, loaderName, parentName, loaderLoaderName);
				}
			}

			allClassloaders.addAll(currentClassloaders);// Save current set of classloaders

			if (newFound.size() == 0) {
				found_new_loader = false;
			}
		}
		return allClassloaders;
	}

	/*
	 * This method gets all loaded classes from JVM agent and for each class gets its classloader. Not all classloaders
	 * are found, a few are missing and require navegating the classloader tree.
	 */
	private Set<ClassLoader> getInitialClassloaders() {

		if (verbose) {
			toolkit.title("Getting initial classloaders");
		}

		final Set<ClassLoader> classloaders = new HashSet<ClassLoader>();

		for (final Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {
			final ClassLoader classloader = clazz.getClassLoader();
			boolean isAdded;

			if (!classloaders.add(classloader)) { // add classloader to set
				isAdded = false;
			} else {
				isAdded = true;
			}

			if (verbose) { // Print loaders that are added and are not
				if (!isAdded) {
					if (debug) {
						toolkit.yellow("Classloader not added:" + classloader + ":clazz=" + clazz);
						toolkit.eol();
					}
				} else {
					toolkit.green("Classloader added:" + classloader + ":clazz=" + clazz);
					toolkit.eol();
				}
			}
		}
		if (debug) {
			toolkit.eol();
			buffer.a("Number of initial class loaders found: " + classloaders.size());
			toolkit.eol();
		}
		return classloaders;
	}

	private boolean isClassLoaderKnown(final Collection<ClassLoader> c1, final Collection<ClassLoader> c2, final Collection<ClassLoader> c3, final ClassLoader loader) {
		if (c1.contains(loader) || c2.contains(loader) || c3.contains(loader)) {
			return true;
		} else {
			return false;
		}
	}

	private void printDuplicateClasses() {
		// Are there any duplicates?
		if (countDuplicates() == 0) {
			return;
		}

		// A set does not allow duplicates...
		final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

		// A set that does allow duplicates
		final List<Class<?>> duplicates = new ArrayList<Class<?>>();

		for (final Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {

			// found duplicate
			if (classes.containsKey(clazz.getName())) {

				// Add class to duplicates if it is the first time we find it
				if (!duplicates.contains(classes.get(clazz.getName()))) {
					System.out.println("Added to duplicates (original):" + classes.get(clazz.getName()));
					duplicates.add(classes.get(clazz.getName()));
				} else {
					System.out.println("Already added to duplicates:" + classes.get(clazz.getName()));
				}

				// Add current class to duplicates, don't add to other set
				System.out.println("Added to duplicates:" + classes.get(clazz.getName()));
				duplicates.add(clazz);
			} else {
				// put class into list
				classes.put(clazz.getName(), clazz);
			}
		}
		// Collections.sort
		if (!duplicatesByCL) {
			Collections.sort(duplicates, ClassUtils.ClassComparator);
		} else {
			Collections.sort(duplicates, ClassLoaderUtils.ClassloaderComparator);
		}

		// Print duplicates
		int i = 0;
		for (final Class<?> clazz : duplicates) {
			if (numbers) {
				System.out.print((i++ / 2) + ":");
			}
			if (classesWithCl) {
				if (clazz.getClassLoader() != null) {
					System.out.print(clazz.getClassLoader().toString() + ":");
				} else {
					System.out.print("null:");
				}
			}
			System.out.println(clazz.getName());
		}
		System.out.println("Total number of duplicate classes (from comparison): " + duplicates.size());
	}

	private void printStats() {

		final int indent = 2;

		// Do this before printing stats to avoid mixed output messages
		final Set<ClassLoader> allClassloaders = getAllClassloaders();

		// title
		toolkit.eol();
		toolkit.title("Statistics");
		toolkit.eol();

		// Count classes
		toolkit.indent(indent);
		buffer.a("Total number of classes: ");
		toolkit.bold(RobustaJavaAgent.getInstrumentation().getAllLoadedClasses().length);
		toolkit.eol();

		// Count duplicates
		toolkit.indent(indent);
		buffer.a("Total number of duplicated classes: ");
		toolkit.bold(countDuplicates());
		toolkit.eol();

		// Count classloaders
		toolkit.indent(indent);
		buffer.a("Total number of classloaders (including hidden): ");
		toolkit.bold(allClassloaders.size());
		toolkit.eol();

	}
}
