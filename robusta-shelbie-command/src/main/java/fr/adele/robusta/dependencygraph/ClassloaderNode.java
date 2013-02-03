package fr.adele.robusta.dependencygraph;

import java.util.ArrayList;
import java.util.List;
import java.lang.ClassLoader;

/**
 * Class representing classloader node in the tree. Holds references to the classloaders children and parent, and
 * provides the Comparable interface for sorting by inheritance hierarchy.
 */

public class ClassloaderNode {
	// implements Comparable{

	private final ClassLoader loader;

	// private final ClassloaderNode parent;
	private ClassloaderNode parent;

	private List<ClassloaderNode> children;

	public ClassloaderNode(final ClassLoader loader, final ClassloaderNode parent) {
		this.loader = loader;
		this.parent = parent;

		if (parent != null)
			parent.addChild(this);
	}

	public ClassloaderNode(final ClassLoader loader) {
		this.loader = loader;
	}

	public boolean addChild(final ClassloaderNode clNode) {
		if (children == null) {
			children = new ArrayList<ClassloaderNode>();
		}
		return children.add(clNode);
	}

	public ClassloaderNode getParent() {
		return parent;
	}

	public void setParent(ClassloaderNode parent) {
		if (parent != null)
			parent.addChild(this);
		this.parent = parent;
	}

	public List<ClassloaderNode> getChildren() {
		return this.children;
	}

	public boolean equals(final Object obj) {
		return ((ClassloaderNode) obj).getClassloader().equals(this.loader);
	}

	public ClassLoader getClassloader() {
		return this.loader;
	}

	public String getName() {
		if (loader != null)
			return this.loader.toString();
		return "Bootstrap (NULL): System Classloader";
	}

	public int hashCode() {
		return this.loader.hashCode();
	}

	public String toString() {
		return new String(loader + ":" + parent + ":" + children.toString());
	}

	/**
	 * Compares one class to another class by their inheritance tree.
	 *
	 * @return an integer representing the comparison results as follows:<br>
	 *         2 if this is a subclass of past in object<br>
	 *         -2 if this is a superclass of past in object<br>
	 *         0 if they are not related (and in relation to sorting, equal)<br>
	 *         0 if they are the same<br>
	 */
	// public int compareTo(Object obj) {
	// ClassLoader objLoader = ((ClassloaderNode) obj).getClassloader();
	//
	// if (objLoader.equals(this.loader)) {
	// return 0;
	// } else if (this.loader.isAssignableFrom(objLoader)) {
	// return 2;
	// } else if (objLoader.isAssignableFrom(this.objectClass)) {
	// return -2;
	// } else {
	// return 0;
	// }
	// }
}
