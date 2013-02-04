package fr.adele.robusta;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
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

import fr.adele.robusta.dependencygraph.ClassloaderNode;
import fr.adele.robusta.internal.util.AnsiPrintToolkit;

@Component
@Command(name = "dump",
		scope = "robusta",
		description = "Command to get all loaded classes and dump them to console")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class DumpAction implements Action {

	@Option(name = "-t",
			aliases = { "--tree", "--classloader-loading-tree" },
			required = false,
			description = "Print classloader tree")
	private boolean treeLoading = false;

	@Option(name = "-T",
			aliases = { "--tree", "--classloader-delegation-tree" },
			required = false,
			description = "Print classloader tree")
	private boolean treeDelegation = false;

	@Option(name = "-l",
			aliases = { "--list", "--classloader-list" },
			required = false,
			description = "Print classloader tree")
	private boolean list = false;

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

	@Option(name = "-gc",
			aliases = { "--garbage-collection" },
			required = false,
			description = "Instruct JVM to attempt garbage collection *before* calculating dependencies (this cannot guarantee GC --> see Java Spec)")
	private boolean gc = false;

	/* Used for colorful output */
	AnsiPrintToolkit toolkit;
	Ansi buffer;

	public static final Comparator<Class<?>> classComparator = new Comparator<Class<?>>() {
		// @Override (whyyy doesn't it work?
		public int compare(final Class<?> c1, final Class<?> c2) {
			return c1.getName().compareTo(c2.getName());
		}
		// @Override
		// public int compare(Object c1, Object c2) {
		// return c1.getName().compareTo(c2.getName());
		// }
	};

	public static final Comparator<Class<?>> classloaderComparator = new Comparator<Class<?>>() {
		// @Override (whyyy doesn't it work? I should have to override...
		public int compare(final Class<?> c1, final Class<?> c2) {
			final String cl1 = c1.getClassLoader().toString();
			final String cl2 = c2.getClassLoader().toString();

			return cl1.compareTo(cl2);
		}
	};

	private Map<ClassLoader, ClassloaderNode> calculateClassloaderGraph() {
		toolkit.red("CalculateClassloaderGraph");
		toolkit.eol();

		final Set<ClassLoader> classloaders = getAllClassloaders();

		final Map<ClassLoader, ClassloaderNode> classloaderGraph = new HashMap<ClassLoader, ClassloaderNode>();

		toolkit.red("CalculateClassloaderGraph");
		toolkit.eol();

		int countLoop = 0, countAdded = 0;

		for (final ClassLoader loader : classloaders) {
			toolkit.green("loader:" + loader);
			toolkit.eol();
			countLoop++;

			// check if node already exists
			if (classloaderGraph.containsKey(loader)) {
				final ClassloaderNode node = classloaderGraph.get(loader);

				if (loader == null) { // we don't need to do anything, bootstrap node is already created and doesn't
										// have a parent
					toolkit.subtitle("Found BOOTSTRAP (NULL) classloader!");
					continue;
				}

				// check if parent is set
				if (node.getParent() == null) {

					// check if parent node exists
					if (classloaderGraph.containsKey(loader.getParent())) {
						final ClassloaderNode parentNode = classloaderGraph.get(loader.getParent());

						// set parent
						node.setParent(parentNode);

					} else {
						// create parent and set
						final ClassloaderNode parentNode = new ClassloaderNode(loader.getParent());
						node.setParent(parentNode);
						// classloaderGraph.put(loader, node);
						classloaderGraph.put(parentNode.getClassloader(), parentNode);
						countAdded++;
					}
				} else { // not necessary because node and parent already exist (not sure if this can even happen!)
					toolkit.urgent("ERROR: Node and parent already set!");
					toolkit.eol();
				}
			} else { // node doesn't exist yet

				if (loader == null) {
					final ClassloaderNode node = new ClassloaderNode(loader, null);// has no parent node
					toolkit.subtitle("Added bootstrap to map!");
					classloaderGraph.put(loader, node);
					countAdded++;
					continue;
				}

				// check to see if parent exists, if not create parent node first!
				if (classloaderGraph.containsKey(loader.getParent())) {
					// if (classloaderGraph.get(loader.getParent()) == null){
					//
					// }
					final ClassloaderNode parentNode = classloaderGraph.get(loader.getParent());
					final ClassloaderNode node = new ClassloaderNode(loader, parentNode);
					classloaderGraph.put(loader, node);
					countAdded++;
				} else {
					// empty parent for him, we'll get him later in the loop!
					final ClassloaderNode parentNode = new ClassloaderNode(loader.getParent());
					final ClassloaderNode node = new ClassloaderNode(loader, parentNode);
					classloaderGraph.put(loader, node);
					classloaderGraph.put(parentNode.getClassloader(), parentNode);
					countAdded++;
					countAdded++;
				}
			}
		}

		// PrintStream stream = System.out;
		// stream.println(toolkit.getBuffer().toString());
		toolkit.cyan("CountLoop: " + countLoop + " CountAdded: " + countAdded);
		toolkit.eol();

		toolkit.cyan("Number of classloaders found in getClassloaders: " + classloaders.size());
		toolkit.eol();
		toolkit.cyan("Number of classloaders maintained in calculateClassloaderGraph: " + classloaderGraph.size());
		toolkit.eol();
		toolkit.cyan("Number of classloaders maintained in set calculateClassloaderGraph: " + classloaderGraph.values().size());
		toolkit.eol();

		// toolkit.title("********");
		// toolkit.title("********");
		// // toolkit.eol();
		//
		// for (ClassLoader loader : classloaders) {
		// toolkit.title("loader1:" + loader);
		// }
		// // stream.println(toolkit.getBuffer().toString());
		//
		// toolkit.title("********");
		// toolkit.title("********");
		// // toolkit.eol();
		//
		// for (ClassloaderNode node : classloaderGraph.values()) {
		// toolkit.magenta("loader1:" + node.getClassloader());
		// toolkit.eol();
		// }
		// // stream.println(toolkit.getBuffer().toString());
		//
		// toolkit.title("********");
		// toolkit.title("********");
		// toolkit.eol();

		return classloaderGraph;
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
				treeLoading = true;
				treeDelegation = true;
				list = true;
				numbers = true;
				classesWithCl = true;
			}

			// If nothing is set, we should print stats
			if (!classes && !treeDelegation && !treeLoading && !stats && !duplicates && !duplicatesByCL && !gc && !list) {
				stats = true;
			}

			if (classes) {
				dumpAllClasses();
			}

			if (duplicates || duplicatesByCL) {
				printDuplicateClasses();
			}

			if (list) {
				printClassloaderList();
			}
			if (treeDelegation) {
				printClassloaderDelegationTree();
			}
			if (treeLoading) {
				// printClassloaderTreeLoading();
				throw new UnsupportedOperationException();
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
				final ClassLoader parent = getClassLoaderParent(loader);
				final ClassLoader loaderLoader = getClassLoaderLoader(loader);

				final String loaderName = getClassLoaderName(loader);
				final String parentName = getClassLoaderParentName(loader);
				final String loaderLoaderName = getClassLoaderLoaderName(loader);

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
					printClassloaderListEntry(loaderName, parentName, loaderLoaderName);
				}
			}
			if (debug) {
				toolkit.urgent("Initial ClassLoader size: " + currentClassloaders.size() + " New found list of Classloaders: " + newFound.size());
				toolkit.debug("Iterations: " + iterations);
				toolkit.debug("Last value added to currentClassLoaders: " + currentClassloaders.get(currentClassloaders.size() - 1));
			}

			for (final ClassLoader loader : newFound) {
				final String loaderName = getClassLoaderName(loader);
				final String parentName = getClassLoaderParentName(loader);
				final String loaderLoaderName = getClassLoaderLoaderName(loader);
				if (debug) {
					toolkit.yellow("New Found entry to be checked: ");
				}
				if (debug) {
					printClassloaderListEntry(loaderName, parentName, loaderLoaderName);
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
	 * DO NOT USE: to be removed
	 */
	private Set<ClassLoader> getAllClassloaders_OLD() {
		final Set<ClassLoader> initialClassloaders = getInitialClassloaders();
		final List<ClassLoader> classloaders = new ArrayList<ClassLoader>(initialClassloaders);
		final List<ClassLoader> newFound = new ArrayList<ClassLoader>();
		// final List<ClassLoader> classloaders = new ArrayList<ClassLoader>(this.getInitialClassloaders());

		if (debug) {
			toolkit.blue("Initial ClassLoader size: " + initialClassloaders.size() + " ArrayList of Classloaders: " + classloaders.size());
			toolkit.eol();
		}
		final ListIterator<ClassLoader> iter = classloaders.listIterator();

		int iterations = 0;
		// for (final ClassLoader loader : classloaders) {
		while (iter.hasNext()) {
			iterations++;
			final ClassLoader loader = iter.next();
			final ClassLoader parent = getClassLoaderParent(loader);
			final ClassLoader loaderLoader = getClassLoaderLoader(loader);

			final String loaderName = getClassLoaderName(loader);
			final String parentName = getClassLoaderParentName(loader);
			final String loaderLoaderName = getClassLoaderLoaderName(loader);
			if (debug) {
				toolkit.debug("NAMES loader: " + loader + " loaderLoader: " + loaderLoader + " parent: " + parent);
			}
			if (!classloaders.contains(loaderLoader)) { // New classloader found!
				toolkit.urgent("NEW Missing ClassLoaderLoader FOUND -->" + loaderLoaderName + "value: " + loaderLoader);
				// classloaders.add(loader); // this will be included into the iteraction
				iter.add(loaderLoader); // this will be included into the iteraction
				newFound.add(loaderLoader);
				toolkit.urgent("Initial ClassLoader size: " + initialClassloaders.size() + " ArrayList of Classloaders: " + classloaders.size());
			} else {
				// classloaders.
				if (debug) {
					toolkit.blue("OLDCLL--> " + loaderLoaderName);
				}
			}
			if (!classloaders.contains(parent)) { // New classloader!
				toolkit.indent();
				toolkit.urgent("NEW Missing ParentClassLoader FOUND --> " + parentName + " value: " + parent);
				// toolkit.urgent(" PARENT: " + parent.);
				// classloaders.add(loader); // this will be included into the iteraction
				iter.add(parent); // this will be included into the iteraction
				newFound.add(parent);
				toolkit.urgent("Initial ClassLoader size: " + initialClassloaders.size() + " ArrayList of Classloaders: " + classloaders.size());
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

			printClassloaderListEntry(loaderName, parentName, loaderLoaderName);
		}
		if (debug) {
			toolkit.debug("Initial ClassLoader size: " + initialClassloaders.size() + " ArrayList of Classloaders New size is: " + classloaders.size());
			toolkit.debug("Iterations: " + iterations);
			toolkit.debug("Last value: " + classloaders.get(classloaders.size() - 1));
			// toolkit.debug("Last value: " + classloaders.get(classloaders.size()));
		}

		for (final ClassLoader loader : newFound) {
			// final ClassLoader parent = getClassLoaderParent(loader);
			// final ClassLoader loaderLoader = getClassLoaderLoader(loader);

			final String loaderName = getClassLoaderName(loader);
			final String parentName = getClassLoaderParentName(loader);
			final String loaderLoaderName = getClassLoaderLoaderName(loader);

			printClassloaderListEntry(loaderName, parentName, loaderLoaderName);
		}

		return null;
	}

	private ClassLoader getClassLoaderLoader(final ClassLoader loader) {
		final ClassLoader loaderLoader;
		if (loader != null) {
			loaderLoader = loader.getClass().getClassLoader();
		} else {
			loaderLoader = null;
		}
		return loaderLoader;
	}

	private String getClassLoaderLoaderName(final ClassLoader loader) {
		final ClassLoader loaderLoader;
		if (loader != null) {
			loaderLoader = loader.getClass().getClassLoader();
		} else {
			loaderLoader = null;
		}
		return getGenericClassLoaderName(loaderLoader, "null-loader");
	}

	private String getClassLoaderName(final ClassLoader loader) {
		return getGenericClassLoaderName(loader, "bootstrap (NULL)");
	}

	private ClassLoader getClassLoaderParent(final ClassLoader loader) {
		final ClassLoader parent;
		if (loader != null) {
			parent = loader.getParent();
		} else {
			parent = null;
		}
		return parent;
	}

	private String getClassLoaderParentName(final ClassLoader loader) {
		final ClassLoader parent;
		if (loader != null) {
			parent = loader.getParent();
		} else {
			parent = null;
		}
		return getGenericClassLoaderName(parent, "null-parent");
	}

	private Set<ClassLoader> getClassloaders_OLD() {
		final Set<ClassLoader> classloaders = new HashSet<ClassLoader>();

		for (final Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {
			final ClassLoader classloader = clazz.getClassLoader();
			if (!classloaders.add(classloader)) {
				toolkit.yellow("Classloader not added:" + classloader + ":clazz=" + clazz);
				toolkit.eol();
			} else {
				toolkit.green("Classloader added:" + classloader + ":clazz=" + clazz);
				toolkit.eol();
			}

		}
		printClassloaderList(classloaders);
		toolkit.eol();
		toolkit.title("Making sure I got all classloaders!");
		toolkit.subtitle("Comparing parents + getClassloader()");
		for (final ClassLoader loader : classloaders) {
			ClassLoader parent = null;
			ClassLoader loaderLoader = null;

			if (loader != null) {
				parent = loader.getParent();
				loaderLoader = loader.getClass().getClassLoader();
			}

			String loaderName;
			if (loader != null) {
				loaderName = loader.toString();
			} else {
				loaderName = "bootstrap (NULL)";
			}

			String parentName;
			if (parent != null) {
				parentName = parent.toString();
			} else {
				parentName = "null-parent";
			}

			String loaderLoaderName;
			if (loaderLoader != null) {
				loaderLoaderName = loaderLoader.toString();
			} else {
				loaderLoaderName = "null-loader";
			}

			if (!classloaders.contains(loaderLoader)) {
				toolkit.red("NEWCLL->" + loaderLoaderName);
				toolkit.eol();
			} else {
				// classloaders.
				toolkit.blue("OLDCLL-->" + loaderLoaderName);
				// toolkit.eol();
			}
			if (!classloaders.contains(parent)) {
				toolkit.indent();
				toolkit.red("NEWPCL->" + parentName);
				toolkit.eol();
			} else {
				toolkit.indent();
				toolkit.green("OLDPCL-->" + parentName);
				toolkit.eol();
			}

			if (loaderLoader == parent) {
				toolkit.magenta("SAME-->");
			} else {
				toolkit.yellow("DIFF-->");
			}

			final int padsize1 = 80;
			final int padsize2 = 53;
			toolkit.white("loader: ");
			toolkit.bold(AnsiPrintToolkit.padRight(loaderName, padsize1));
			toolkit.white(" parent: ");
			toolkit.bold(AnsiPrintToolkit.padRight(parentName, padsize2));
			toolkit.white(" loaderLoader: ");

			toolkit.bold(AnsiPrintToolkit.padRight(loaderLoaderName, 1));

			toolkit.eol();
		}

		// TODO: It's possible that getAllLoadedClasses() does not get me all classloaders.
		// I Might have to go through the CL and add parents too just to be safe...!
		toolkit.cyan("Number of classloaders found in getClassloaders: " + classloaders.size());
		toolkit.eol();

		printClassloaderList(classloaders);

		return classloaders;// includes NULL value
	}

	private String getGenericClassLoaderName(final ClassLoader loader, final String nullValueString) {
		String loaderName;
		if (loader != null) {
			loaderName = loader.toString();
		} else {
			loaderName = nullValueString;
		}
		return loaderName;
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

	private void printClassloaderDelegationTree() {
		toolkit.red("printClassloaderTree");
		toolkit.eol();

		final Map<ClassLoader, ClassloaderNode> clGraph = calculateClassloaderGraph();
		toolkit.red("printClassloaderTree");
		toolkit.eol();

		// find top classloader, named null!
		toolkit.red("find top classloader, named null");
		toolkit.eol();
		// TODO: just get null from map, don't need to search around.
		if (clGraph.containsKey(null)) {
			toolkit.red("ERROR: Found null classloader");
			toolkit.eol();
		}
		toolkit.red("find top classloader, named null v2");

		ClassloaderNode toplevelNode = null;
		boolean found = false;
		for (final ClassloaderNode node : clGraph.values()) {

			if (node == null) {
				toolkit.red("ERROR: Found null node, shouldn't be here");
				toolkit.eol();
				continue;
			}

			// if (node.getClassloader().getParent() == null) {
			if (node.getClassloader() == null) {
				toolkit.green("Found toplevel cl");
				toolkit.eol();
				toplevelNode = node;
				found = true;
				if (found) {
					toolkit.red("ERROR: Found another null classloaders");
					toolkit.eol();
				}
				// break;
			}
		}
		toolkit.eol();
		toolkit.title("*** Classloader hierarchy ***");
		printClassloaderNode(toplevelNode, 2);
	}

	private void printClassloaderList() {
		Set<ClassLoader> classloaders = getAllClassloaders();

		printClassloaderList(classloaders);

	}

	private void printClassloaderList(final Set<ClassLoader> classloaders) {
		// toolkit.eol();
		toolkit.title("ClassLoader List showing Parent and Loading classloaders");

		for (final ClassLoader loader : classloaders) {
			final ClassLoader parent = getClassLoaderParent(loader);
			final ClassLoader loaderLoader = getClassLoaderLoader(loader);

			final String loaderName = getClassLoaderName(loader);
			final String parentName = getClassLoaderParentName(loader);
			final String loaderLoaderName = getClassLoaderLoaderName(loader);

			if (verbose) {
				if (loaderLoader == parent) {
					toolkit.green("[SAME]");
				} else {
					toolkit.cyan("[DIFF]");
				}
			}
			printClassloaderListEntry(loaderName, parentName, loaderLoaderName);
		}
		if (verbose)
			toolkit.subtitle("Number of classloaders " + classloaders.size());

	}

	private void printClassloaderListEntry(final String loaderName, final String type, final int padsize) {
		toolkit.white(type);
		toolkit.bold(AnsiPrintToolkit.padRight(loaderName, padsize));
	}

	private void printClassloaderListEntry(final String loaderName, final String parentName, final String loaderLoaderName) {
		final int padsize1 = 81;
		final int padsize2 = 53;
		final String typeLoader = " loader: ";
		final String typeParent = " parent: ";
		final String typeLoaderLoader = " loaderLoader: ";

		printClassloaderListEntry(loaderName, typeLoader, padsize1);
		printClassloaderListEntry(parentName, typeParent, padsize2);
		printClassloaderListEntry(loaderLoaderName, typeLoaderLoader, 1);

		toolkit.eol();
	}

	private void printClassloaderNode(final ClassloaderNode node, final int indents) {
		// for (int i=0 ; i < indents ; i++){}
		toolkit.indent(indents);

		buffer.a(node.getName());
		toolkit.eol();

		if (node.getChildren() == null) {
			return;
		}

		for (final ClassloaderNode child : node.getChildren()) {
			printClassloaderNode(child, (indents) + 2);
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
			Collections.sort(duplicates, classComparator);
		} else {
			Collections.sort(duplicates, classloaderComparator);
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
