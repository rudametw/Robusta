/**
 * Copyright 2010 OW2 Shelbie
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.adele.robusta;

import java.awt.Toolkit;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import fr.adele.robusta.dependencygraph.ClassloaderNode;
import fr.adele.robusta.internal.util.AnsiPrintToolkit;

@Component
@Command(name = "dump",
		scope = "robusta",
		description = "Command to get all loaded classes and dump them to console")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class DumpAction implements Action {

	@Option(name = "-t",
			aliases = { "--tree", "--classloader-tree" },
			required = false,
			description = "Print classloader tree")
	private boolean tree = false;

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

	// Store instrumentation object obtained from Robusta Java Agent
	// private Instrumentation inst;

	public Object execute(CommandSession session) throws Exception {

		AnsiPrintToolkit toolkit = new AnsiPrintToolkit();

		// inst = RobustaJavaAgent.getInstrumentation();

		// Garbage collect BEFORE any calculations
		if (gc) {
			collectGarbage(toolkit);
		}

		if (all) {
			stats = true;
			classes = true;
			tree = true;
			numbers = true;
			classesWithCl = true;
		}

		// If nothing is set, we should print stats
		if (!classes && !tree && !stats && !duplicates && !duplicatesByCL) {
			stats = true;
		}

		if (classes) {
			dumpAllClasses(toolkit);
		}

		if (duplicates || duplicatesByCL) {
			printDuplicateClasses();
		}

		if (tree) {
			try {
				printClassloaderTree(toolkit);
			} catch (RuntimeException e) {
				toolkit.getBuffer().a(e.toString() + e.fillInStackTrace());
				e.printStackTrace();
			}
		}

		if (stats) {
			printStats(toolkit);
		}

		// Flush buffer to console
		PrintStream stream = System.out;
		stream.println(toolkit.getBuffer().toString());

		return null;
	}

	private void collectGarbage(AnsiPrintToolkit toolkit) {
		Ansi buffer = toolkit.getBuffer();
		long time1 = System.currentTimeMillis();
		toolkit.title("*** Garbage Collection ***");

		System.gc();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.gc();

		long time2 = System.currentTimeMillis();
		toolkit.indent(2);
		buffer.a("Garbage Collection in " + (time2 - time1) + " miliseconds");
		toolkit.eol();
	}

	// TODO: Method Not tested or verified
	// private Set<String> getClassloaderNames() {
	// Set<String> classloaderNames = new HashSet<String>();
	//
	// Set<ClassLoader> classloaders = getClassloaders();
	//
	// for (ClassLoader loader : classloaders) {
	// String classloaderName;
	//
	// if (loader != null) {
	// classloaderName = loader.toString();
	// } else {
	// classloaderName = "null";
	// }
	// classloaderNames.add(classloaderName);
	// }
	//
	// return classloaderNames;
	// }

	private Set<ClassLoader> getClassloaders(AnsiPrintToolkit toolkit) {
		Set<ClassLoader> classloaders = new HashSet<ClassLoader>();

		int classesByNull = 0;
		for (Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {
			ClassLoader classloader = clazz.getClassLoader();
			// if (classloader == null) {
			// toolkit.yellow("Bootstrap classloader found:" + classloader + ":clazz=" + clazz);
			// toolkit.eol();
			// classesByNull++;
			// }
			// toolkit.green("Classloader added:" + classloader);
			// toolkit.eol();
			classloaders.add(classloader);

		}

		toolkit.eol();
		toolkit.title("****** Making sure I got all classloaders! ****** \n Comparing parents + getClassloader()");
		for (ClassLoader loader : classloaders) {
			ClassLoader parent = null;
			ClassLoader loaderLoader = null;

			if (loader != null) {
				parent = loader.getParent();
				loaderLoader = loader.getClass().getClassLoader();
			}

			String loaderName;
			if (loader != null)
				loaderName = loader.toString();
			else
				loaderName = "bootstrap (NULL)";

			String parentName;
			if (parent != null)
				parentName = parent.toString();
			else
				parentName = "null-parent";

			String loaderLoaderName;
			if (loaderLoader != null)
				loaderLoaderName = loaderLoader.toString();
			else
				loaderLoaderName = "null-loader";

			if (loaderLoader == parent)
				toolkit.magenta("SAME-->");
			else
				toolkit.yellow("DIFF-->");

			int padsize1 = 80;
			int padsize2 = 53;
			toolkit.white("loader: ");
			toolkit.bold(AnsiPrintToolkit.padRight(loaderName, padsize1));
			toolkit.white(" parent: ");
			toolkit.bold(AnsiPrintToolkit.padRight(parentName, padsize2));
			toolkit.white(" loaderLoader: ");

			toolkit.bold(AnsiPrintToolkit.padRight(loaderLoaderName, 1));

			toolkit.eol();

			// if (parent != null)
			// toolkit.bold(parent.toString());
			// else
			// toolkit.bold("null");
			//
			// toolkit.white(" loaderLoader: ");
			// if (loaderLoader != null)
			// toolkit.bold(loaderLoader.toString());
			// else
			// toolkit.bold("null");
			//
			// if (loaderLoader == parent)
			// toolkit.red("<-- HERE");
			// toolkit.eol();
		}

		// toolkit.title("Bootstrap (NULL) CLASSLOADER LOADED:" + classesByNull + " out of " +
		// RobustaJavaAgent.getInstrumentation().getAllLoadedClasses().length);

		// TODO: It's possible that getAllLoadedClasses() does not get me all classloaders.
		// I Might have to go through the CL and add parents too just to be safe...!
		toolkit.cyan("Number of classloaders found in getClassloaders: " + classloaders.size());
		toolkit.eol();

		return classloaders;// includes NULL value
	}

	private Map<ClassLoader, ClassloaderNode> calculateClassloaderGraph(AnsiPrintToolkit toolkit) {
		toolkit.red("CalculateClassloaderGraph");
		toolkit.eol();

		Set<ClassLoader> classloaders = getClassloaders(toolkit);

		Map<ClassLoader, ClassloaderNode> classloaderGraph = new HashMap<ClassLoader, ClassloaderNode>();

		toolkit.red("CalculateClassloaderGraph");
		toolkit.eol();

		int countLoop = 0, countAdded = 0;

		for (ClassLoader loader : classloaders) {
			toolkit.green("loader:" + loader);
			toolkit.eol();
			countLoop++;

			// check if node already exists
			if (classloaderGraph.containsKey(loader)) {
				ClassloaderNode node = classloaderGraph.get(loader);

				if (loader == null) { // we don't need to do anything, bootstrap node is already created and doesn't
										// have a parent
					toolkit.title("Found BOOTSTRAP (NULL) classloader!");
					continue;
				}

				// check if parent is set
				if (node.getParent() == null) {

					// check if parent node exists
					if (classloaderGraph.containsKey(loader.getParent())) {
						ClassloaderNode parentNode = classloaderGraph.get(loader.getParent());

						// set parent
						node.setParent(parentNode);

					} else {
						// create parent and set
						ClassloaderNode parentNode = new ClassloaderNode(loader.getParent());
						node.setParent(parentNode);
						// classloaderGraph.put(loader, node);
						classloaderGraph.put(parentNode.getClassloader(), parentNode);
						countAdded++;
					}
				} else { // not necessary because node and parent already exist (not sure if this can even happen!
					toolkit.title("ERROR: Node and parent already set!");
					toolkit.eol();
				}
			} else { // node doesn't exist yet

				if (loader == null) {
					ClassloaderNode node = new ClassloaderNode(loader, null);// has no parent node
					toolkit.title("Added bootstrap to map!");
					classloaderGraph.put(loader, node);
					countAdded++;
					continue;
				}

				// check to see if parent exists, if not create parent node first!
				if (classloaderGraph.containsKey(loader.getParent())) {
					// if (classloaderGraph.get(loader.getParent()) == null){
					//
					// }
					ClassloaderNode parentNode = classloaderGraph.get(loader.getParent());
					ClassloaderNode node = new ClassloaderNode(loader, parentNode);
					classloaderGraph.put(loader, node);
					countAdded++;
				} else {
					// empty parent for him, we'll get him later in the loop!
					ClassloaderNode parentNode = new ClassloaderNode(loader.getParent());
					ClassloaderNode node = new ClassloaderNode(loader, parentNode);
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

		toolkit.title("********");
		toolkit.title("********");
		// toolkit.eol();

		for (ClassLoader loader : classloaders) {
			toolkit.title("loader1:" + loader);
		}
		// stream.println(toolkit.getBuffer().toString());

		toolkit.title("********");
		toolkit.title("********");
		// toolkit.eol();

		for (ClassloaderNode node : classloaderGraph.values()) {
			toolkit.magenta("loader1:" + node.getClassloader());
			toolkit.eol();
		}
		// stream.println(toolkit.getBuffer().toString());

		toolkit.title("********");
		toolkit.title("********");
		toolkit.eol();

		return classloaderGraph;
	}

	private void printClassloaderTree(AnsiPrintToolkit toolkit) {
		toolkit.red("printClassloaderTree");
		toolkit.eol();

		Map<ClassLoader, ClassloaderNode> clGraph = calculateClassloaderGraph(toolkit);
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
		for (ClassloaderNode node : clGraph.values()) {

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
		printClassloaderNode(toolkit, toplevelNode, 2);

		// // Print classloaders
		// int i = 0;
		// for (String loader : classloaders) {
		// if (numbers) {
		// System.out.print(++i + ":");
		// }
		// System.out.println(loader);
		// }
	}

	private void printClassloaderNode(AnsiPrintToolkit toolkit, ClassloaderNode node, int indents) {
		Ansi buffer = toolkit.getBuffer();
		// for (int i=0 ; i < indents ; i++){}
		toolkit.indent(indents);

		buffer.a(node.getName());
		toolkit.eol();

		if (node.getChildren() == null)
			return;

		for (ClassloaderNode child : node.getChildren()) {
			printClassloaderNode(toolkit, child, (indents) + 2);
		}
	}

	private void dumpAllClasses(AnsiPrintToolkit toolkit) {

		Ansi buffer = toolkit.getBuffer();

		int i = 0;
		for (Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {
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

	public static final Comparator<Class<?>> classComparator = new Comparator<Class<?>>() {
		// @Override (whyyy doesn't it work?
		public int compare(Class<?> c1, Class<?> c2) {
			return c1.getName().compareTo(c2.getName());
		}
		// @Override
		// public int compare(Object c1, Object c2) {
		// return c1.getName().compareTo(c2.getName());
		// }
	};

	public static final Comparator<Class<?>> classloaderComparator = new Comparator<Class<?>>() {
		// @Override (whyyy doesn't it work? I should have to override...
		public int compare(Class<?> c1, Class<?> c2) {
			String cl1 = c1.getClassLoader().toString();
			String cl2 = c2.getClassLoader().toString();

			return cl1.compareTo(cl2);
		}
	};

	private void printDuplicateClasses() {
		// Are there any duplicates?
		if (countDuplicates() == 0)
			return;

		// A set does not allow duplicates...
		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

		// A set that does allow duplicates
		List<Class<?>> duplicates = new ArrayList<Class<?>>();

		for (Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {

			// found duplicate
			if (classes.containsKey(clazz.getName())) {

				// Add class to duplicates if it is the first time we find it
				if (!duplicates.contains(classes.get(clazz.getName()))) {
					System.out.println("Added to duplicates (original):" + classes.get(clazz.getName()));
					duplicates.add(classes.get(clazz.getName()));
				} else
					System.out.println("Already added to duplicates:" + classes.get(clazz.getName()));

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
			Collections.sort((List<Class<?>>) duplicates, classComparator);
		} else {
			Collections.sort((List<Class<?>>) duplicates, classloaderComparator);
		}

		// Print duplicates
		int i = 0;
		for (Class<?> clazz : duplicates) {
			if (numbers) {
				System.out.print(i++ / 2 + ":");
			}
			if (classesWithCl) {
				if (clazz.getClassLoader() != null)
					System.out.print(clazz.getClassLoader().toString() + ":");
				else
					System.out.print("null:");
			}
			System.out.println(clazz.getName());
		}
		System.out.println("Total number of duplicate classes (from comparison): " + duplicates.size());
	}

	private void printStats(AnsiPrintToolkit toolkit) {

		Ansi buffer = toolkit.getBuffer();
		int indent = 2;

		// title
		toolkit.eol();
		toolkit.title(" *** Statistics *** ");
		toolkit.eol();

		// Count classes
		toolkit.indent(indent);
		buffer.a("Total number of classes: ");
		toolkit.bold(RobustaJavaAgent.getInstrumentation().getAllLoadedClasses().length);
		toolkit.eol();

		// Count classloaders
		toolkit.indent(indent);
		buffer.a("Total number of classloaders: ");
		toolkit.bold(getClassloaders(toolkit).size());
		toolkit.eol();

		// Count duplicates
		toolkit.indent(indent);
		buffer.a("Total number of duplicated classes: ");
		toolkit.bold(countDuplicates());
		toolkit.eol();
	}

	private int countDuplicates() {
		List<String> allClasses = new ArrayList<String>();
		Set<String> classes;

		for (Class<?> clazz : RobustaJavaAgent.getInstrumentation().getAllLoadedClasses()) {
			allClasses.add(clazz.getName());
		}

		classes = new HashSet<String>(allClasses);

		return (allClasses.size() - classes.size());
	}

}
