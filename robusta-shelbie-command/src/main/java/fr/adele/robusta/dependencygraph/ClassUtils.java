package fr.adele.robusta.dependencygraph;

import java.util.Comparator;

public class ClassUtils {

	public static final Comparator<Class<?>> ClassComparator = new Comparator<Class<?>>() {

		// @Override (whyyy doesn't it work?)
		public int compare(final Class<?> c1, final Class<?> c2) {
			return c1.getName().compareTo(c2.getName());
		}
		// @Override
		// public int compare(Object c1, Object c2) {
		// return c1.getName().compareTo(c2.getName());
		// }
	};
}
