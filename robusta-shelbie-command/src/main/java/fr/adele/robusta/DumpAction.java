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

import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
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

	private Instrumentation inst;

	/**
	 * Arguments are un-named values.
	 */
	// @Argument(multiValued = true,
	// description = "The name of one or more person(s).")
	// private List<String> who;

	public Object execute(CommandSession session) throws Exception {

		AnsiPrintToolkit toolkit = new AnsiPrintToolkit();

		inst = (Instrumentation) RobustaJavaAgent.getInstrumentation();
		// System.out.println("Instrumentation = " + inst);

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
		if (!classes && !tree && !stats && !duplicates && !duplicatesByCL) {
			stats = true;
		}
		// if (classes || classesWithCl) {
		if (classes) {
			dumpAllClasses(toolkit);
		}
		if (duplicates || duplicatesByCL) {
			printDuplicateClasses();
		}
		if (tree) {
			// Directly print the message using System.out or System.err
			// System.out.println("Dump classloader tree ");
			printClassloaderTree();
		}
		if (stats) {
			// System.out.println("Print stats");
			// throw new Exception("Unknown language");
			printStats(toolkit);
		}

		PrintStream stream = System.out;

		stream.println(toolkit.getBuffer().toString());

		return null;

	}

	// private void error(AnsiPrintToolkit toolkit, String message) {
	// // Use error stream
	// PrintStream stream = System.err;
	// Ansi buffer = toolkit.getBuffer();
	//
	// // Creates an error message
	// buffer.a(" [");
	// toolkit.red("ERROR");
	// buffer.a("] ");
	//
	// buffer.a("Instance '");
	// toolkit.italic(message);
	// buffer.a("' was not found.\n");
	// // Flush buffer's content
	// stream.println(toolkit.getBuffer().toString());
	// }

	private void collectGarbage(AnsiPrintToolkit toolkit) {
		Ansi buffer = toolkit.getBuffer();
		long time1 = System.currentTimeMillis();
		toolkit.title("*** Garbage Collection ***");
		System.gc();
		long time2 = System.currentTimeMillis();
		toolkit.indent(2);
		buffer.a("Garbage Collection in " + (time2 - time1) + " miliseconds");
		toolkit.eol();
	}

	private Set<String> getClassloaders() {
		Set<String> classloaders = new HashSet<String>();

		for (Class<?> clazz : inst.getAllLoadedClasses()) {
			String classloader;
			if (clazz.getClassLoader() != null) {
				classloader = clazz.getClassLoader().toString();
			} else {
				classloader = "null";
			}
			classloaders.add(classloader);
		}

		return classloaders;
	}

	private void printClassloaderTree() {
		Set<String> classloaders = getClassloaders();

		// Print classloaders
		int i = 0;
		for (String loader : classloaders) {
			if (numbers) {
				System.out.print(++i + ":");
			}
			System.out.println(loader);
		}
	}

	private void dumpAllClasses(AnsiPrintToolkit toolkit) {

		Ansi buffer = toolkit.getBuffer();

		int i = 0;
		for (Class<?> clazz : inst.getAllLoadedClasses()) {
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
					buffer.a("null");
					toolkit.separator();
				}
			}
			// This line adds class | interface before the getName()
			// System.out.print(clazz.toString() + ":");
			// System.out.println(clazz.getName());
			// toolkit.bold("§");
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

		for (Class<?> clazz : inst.getAllLoadedClasses()) {

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

		// title
		toolkit.eol();
		toolkit.title(" *** Statistics *** ");
		toolkit.eol();

		int indent = 2;

		// Count classes
		// System.out.println("Total number of classes: " + inst.getAllLoadedClasses().length);
		toolkit.indent(indent);
		buffer.a("Total number of classes: ");
		toolkit.bold(inst.getAllLoadedClasses().length);
		toolkit.eol();

		// Count classloaders
		// System.out.println("Total number of classloaders: " + null);
		toolkit.indent(indent);
		buffer.a("Total number of classes: ");
		toolkit.bold(inst.getAllLoadedClasses().length);
		toolkit.eol();

		// Count duplicates
		// System.out.println("Total number of duplicate classes: " + countDuplicates());
		toolkit.indent(indent);
		buffer.a("Total number of duplicate classes: ");
		toolkit.bold(countDuplicates());
		toolkit.eol();
	}

	private int countDuplicates() {
		List<String> allClasses = new ArrayList<String>();
		Set<String> classes;

		for (Class<?> clazz : inst.getAllLoadedClasses()) {
			allClasses.add(clazz.getName());
		}

		classes = new HashSet<String>(allClasses);

		if (classes.size() < allClasses.size()) {
			/* There are duplicates */
		}

		return allClasses.size() - classes.size();
	}

}
