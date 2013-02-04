package fr.adele.robusta.dependencygraph;

import java.util.Comparator;
import java.util.Set;

import fr.adele.robusta.internal.util.AnsiPrintToolkit;

public class ClassLoaderUtils {

	public static enum LOADER_HIERARCHY {
		LOADER, PARENT
	}

	public static final Comparator<Class<?>> ClassloaderComparator = new Comparator<Class<?>>() {
		// @Override (whyyy doesn't it work? I should have to override...
		public int compare(final Class<?> c1, final Class<?> c2) {
			final String cl1 = c1.getClassLoader().toString();
			final String cl2 = c2.getClassLoader().toString();

			return cl1.compareTo(cl2);
		}
	};

	public static final Comparator<Class<?>> ClassloaderNodeComparator = new Comparator<Class<?>>() {
		// @Override (whyyy doesn't it work? I should have to override...
		public int compare(final Class<?> c1, final Class<?> c2) {
			final String cl1 = c1.getName();
			final String cl2 = c2.getName();

			return cl1.compareTo(cl2);
		}
	};

	public static void printClassloaderList(final AnsiPrintToolkit toolkit, final boolean verbose, boolean numbers, final Set<ClassLoader> classloaders) {
		// toolkit.eol();
		toolkit.title("ClassLoader List showing Parent and Loading classloaders");

		int i = 0;

		for (final ClassLoader loader : classloaders) {
			final ClassLoader parent = getClassLoaderParent(loader);
			final ClassLoader loaderLoader = getClassLoaderLoader(loader);

			final String loaderName = getClassLoaderName(loader);
			final String parentName = getClassLoaderParentName(loader);
			final String loaderLoaderName = getClassLoaderLoaderName(loader);

			if (numbers) {
				printNumber(toolkit, ++i);
			}

			if (verbose) {
				if (loaderLoader == parent) {
					toolkit.green("[SAME]");
				} else {
					toolkit.cyan("[DIFF]");
				}
			}
			printClassloaderListEntry(toolkit, verbose, loaderName, parentName, loaderLoaderName);
		}
		if (verbose) {
			toolkit.subtitle("Number of classloaders " + classloaders.size());
		}
	}

	public static void printNumber(AnsiPrintToolkit toolkit, int i) {
		toolkit.white(AnsiPrintToolkit.padLeft(new Integer(i).toString(), 4));
		toolkit.separator();
	}

	public static void printNumberBrackets(AnsiPrintToolkit toolkit, int i) {
		toolkit.getBuffer().a("[");
		toolkit.white(AnsiPrintToolkit.padLeft(new Integer(i).toString(), 3));
		toolkit.getBuffer().a("] ");
	}

	public static void printClassloaderListEntry(final AnsiPrintToolkit toolkit, final boolean verbose, final String loaderName, final String type, final int padsize) {
		toolkit.white(type);
		toolkit.bold(AnsiPrintToolkit.padRight(loaderName, padsize));
	}

	public static void printClassloaderListEntry(final AnsiPrintToolkit toolkit, final boolean verbose, final String loaderName, final String parentName,
			final String loaderLoaderName) {
		final int padsize1 = 81;
		final int padsize2 = 53;
		final String typeLoader = " loader: ";
		final String typeParent = " parent: ";
		final String typeLoaderLoader = " loaderLoader: ";

		printClassloaderListEntry(toolkit, verbose, loaderName, typeLoader, padsize1);
		printClassloaderListEntry(toolkit, verbose, parentName, typeParent, padsize2);
		printClassloaderListEntry(toolkit, verbose, loaderLoaderName, typeLoaderLoader, 1);

		toolkit.eol();
	}

	public static ClassLoader getClassLoaderLoader(final ClassLoader loader) {
		final ClassLoader loaderLoader;
		if (loader != null) {
			loaderLoader = loader.getClass().getClassLoader();
		} else {
			loaderLoader = null;
		}
		return loaderLoader;
	}

	public static String getClassLoaderLoaderName(final ClassLoader loader) {
		final ClassLoader loaderLoader;
		if (loader != null) {
			loaderLoader = loader.getClass().getClassLoader();
		} else {
			loaderLoader = null;
		}
		return getGenericClassLoaderName(loaderLoader, "null-loader");
	}

	public static String getClassLoaderName(final ClassLoader loader) {
		return getGenericClassLoaderName(loader, "bootstrap (NULL)");
	}

	public static ClassLoader getClassLoaderParent(final ClassLoader loader) {
		final ClassLoader parent;
		if (loader != null) {
			parent = loader.getParent();
		} else {
			parent = null;
		}
		return parent;
	}

	public static String getClassLoaderParentName(final ClassLoader loader) {
		final ClassLoader parent;
		if (loader != null) {
			parent = loader.getParent();
		} else {
			parent = null;
		}
		return getGenericClassLoaderName(parent, "null-parent");
	}

	public static String getGenericClassLoaderName(final ClassLoader loader, final String nullValueString) {
		String loaderName;
		if (loader != null) {
			loaderName = loader.toString();
		} else {
			loaderName = nullValueString;
		}
		return loaderName;
	}

}
