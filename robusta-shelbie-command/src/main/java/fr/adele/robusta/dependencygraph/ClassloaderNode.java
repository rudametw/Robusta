package fr.adele.robusta.dependencygraph;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleReference;

/**
 * Class representing a classloader node in the classloader tree. Holds references to the classloaders children and
 * parent, and provides the Comparable interface for sorting by inheritance hierarchy.
 *
 * @author rudametw
 *
 */
public class ClassloaderNode implements Comparable<ClassloaderNode> {
	// implements Comparable{

	public BundleReference getBundleReference() {
		return bundleReference;
	}

	private final ClassLoader loader;

	private ClassloaderNode parent;

	private ClassloaderNode loaderLoader;

	private final boolean isBundle;

	private BundleReference bundleReference;

	private final List<ClassloaderNode> childrenParent = new ArrayList<ClassloaderNode>();

	private final List<ClassloaderNode> childrenLoader = new ArrayList<ClassloaderNode>();

	// public ClassloaderNode(final ClassLoader loader, final ClassloaderNode parent, final ClassloaderNode
	// loaderLoader) {
	// public ClassloaderNode(final ClassLoader loader, final ClassloaderNode parent) {
	//
	// this.loader = loader;
	// this.parent = parent;
	// // this.loaderLoader = loaderLoader;
	//
	// if (parent != null)
	// parent.addChild(this);
	// }

	// TODO: Can loader => NULL?
	public ClassloaderNode(final ClassLoader loader) {
		this.loader = loader;

		isBundle = ClassLoaderUtils.checkIsBundle(loader);

		if (isBundle) {
			try {
				bundleReference = (BundleReference) loader;
			} catch (Exception e) { // This should never happen in Felix
				e.printStackTrace();
				bundleReference = null;
			}
		} else
			bundleReference = null;
	}

	// private boolean checkIsBundle(final ClassLoader loader) {
	// if(loader instanceof BundleWiring){
	// return true;
	// }
	// return false;
	// }

	public boolean addChild(final ClassloaderNode clNode) {
		return childrenParent.add(clNode);
	}

	public boolean addChildLoader(final ClassloaderNode clNode) {
		return childrenLoader.add(clNode);
	}

	public ClassloaderNode getParent() {
		return parent;
	}

	public void setParent(ClassloaderNode parent) {
		if (parent != null)
			parent.addChild(this);
		this.parent = parent;
	}

	public ClassloaderNode getLoaderLoader() {
		return loaderLoader;
	}

	public void setLoaderLoader(ClassloaderNode loaderLoader) {
		if (loaderLoader != null)
			loaderLoader.addChildLoader(this);
		this.loaderLoader = loaderLoader;
	}

	public List<ClassloaderNode> getChildrenParent() {
		return this.childrenParent;
	}

	public List<ClassloaderNode> getChildrenLoader() {
		return this.childrenLoader;
	}

	public boolean equals(final Object obj) {
		if (obj == null)
			return false;
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
		// return new String(loader + ":" + parent + ":" + children.toString() + ":" + childrenLoader.toString());
		return new String(this.getName() + ":" + parent.getName() + ":" + loaderLoader.getName() + ":" + "childrenLoader=" + childrenParent.toString() + ":" + "childrenLoader="
				+ childrenLoader.toString());

	}

	public int compareTo(ClassloaderNode node) {
		return node.getName().compareTo(getName());// compare string names
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
