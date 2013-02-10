package fr.adele.robusta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.adele.robusta.dependencygraph.ClassDescription;

public class EmptyClass {

	List<Class<?>> mylist = null;
	Set<ClassDescription> descriptions;

	public void method(){

	    //TRICKY ONE HERE... generic not kept in Bytecode
	    new ArrayList<fr.adele.robusta.ClassAction>();

	    return;
	}
}
