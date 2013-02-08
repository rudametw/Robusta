package fr.adele.robusta.dependencygraph;

/*
 * The contents of this file are subject to the Sapient Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://carbon.sf.net/License.html.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is The Carbon Component Framework.
 *
 * The Initial Developer of the Original Code is Sapient Corporation
 *
 * Copyright (C) 2003 Sapient Corporation. All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class creates a tree structure that maps inheritance hierarchies of
 * classes. A developer can place any number of classes into this object and
 * retrieve the closest super class or the class itself.
 *
 *
 * Copyright 2001 Sapient
 * @since EJFW 2.7
 * @author Greg Hinkle, January 2001
 * @version $Revision: 1.4 $($Author: dvoet $ / $Date: 2003/05/05 21:21:23 $)
 */
public class ClassTree {

    protected ClassTreeNode bottom;


    /**
     * Constructs a ClassTree that represents all classes and interfaces that
     * are generalizations of the provided class. This ends up with a tree
     * structure of the inheritance hierarchy for that provided class all the
     * way up to java.lang.Object.
     * @param specificClass The class to build the tree for.
     */
    public ClassTree(Class specificClass) {
        this.bottom = ClassTreeNode.buildNode(specificClass);

    }

    public ClassTreeNode getBottom() {
        return this.bottom;
    }


    /**
     * Constructs an ordered list starting at the highest (most general) class
     * in the tree and moving down the tree, ensuring no generalization comes
     * after one of its specializations.
     * @return a list ordered as above
     */
    public List getOrderedList() {
        List list = new ArrayList();
        list.add(getBottom());

        buildList(getBottom(),list);

        Collections.sort(list);

        // Refactor list into a list of classes from a list of ClassTreeNodes
        for (int i = 0; i < list.size(); i++) {
            ClassTreeNode node = (ClassTreeNode) list.get(i);
            list.set(i,node.getObjectClass());
        }

        // Reverse the list so that the top class in the hierarchy comes first
        Collections.reverse(list);

        return list;
    }

    /**
     * Build breadth first in order to maintain sudo ordering as per
     * class declarations (i.e. if A implements B, C... B is closer in the
     * chain to A than C is, because B comes first in the implements clause.
     *
     * Note that the list coming out here is preordered, but not natural
     * ordered. (i.e. some classes are out of order in relation to classes
     * they have direct relationships with. This is later fixed by a sort
     * on the list by natural ordering. Collecitons.sort, does preserve
     * the preordering for nodes that have no relationship.
     *
     * @param node the node to be browsed.
     * @param output this list is altered to add the contents as they are
     *   browsed in breadth-first order. Start with a list containing only
     *   the bottom node.
     */
    private void buildList(ClassTreeNode node, List output) {

        for (int i = 0; i < node.getParents().size(); i++) {
            ClassTreeNode parent = (ClassTreeNode) node.getParents().get(i);
            if (!output.contains(parent)) {
                output.add(parent);
            }
        }

        List parents = node.getParents();
        for (int i = 0; i < parents.size(); i++) {
            ClassTreeNode parent = (ClassTreeNode) parents.get(i);
            buildList(parent, output);
        }
    }



    /**
     * Inner class representing each node in the tree. Holds references to the
     * nodes children, parent and provides the Comparable interface for sorting
     * by inheritance hierarchy.
     */
    public static class ClassTreeNode implements Comparable {
        /** The class of this node */
        protected Class objectClass;

        /** The map of children classes to their class names */
        protected List children;

        /** A reference to the parent node of this node */
        protected List parents;

        /**
         * Constructs a ClassTreeNode with the given Class.
         *
         * @param objectClass the Class of the node
         */
        public ClassTreeNode(Class objectClass) {
            this.children = new ArrayList();
            this.objectClass = objectClass;
            this.parents = new ArrayList();


        }

        public static ClassTreeNode buildNode(Class objectClass) {
            Map allNodes = new HashMap();
            return buildNode(objectClass, allNodes);
        }

        protected static ClassTreeNode buildNode(Class objectClass, Map allNodes) {
            ClassTreeNode node;
            if (allNodes.containsKey(objectClass)) {
                node = (ClassTreeNode) allNodes.get(objectClass);
            } else {
                node = new ClassTreeNode(objectClass);
                allNodes.put(objectClass, node);
            }

            // Add the implemented interfaces...
            Class[] superInterfaces = objectClass.getInterfaces();
            for (int i = 0; i < superInterfaces.length; i++) {
                Class superInterface = superInterfaces[i];
                ClassTreeNode parent = buildNode(superInterface);
                node.addParent(parent);
            }

            // Add the superclass after the interfaces...
            Class superClass = objectClass.getSuperclass();
            if (superClass != null) {
                ClassTreeNode parent = buildNode(superClass);
                node.addParent(parent);
            }
            return node;
        }


        public List getParents() {
            return this.parents;
        }

        public void addParent(ClassTreeNode node) {
            this.parents.add(node);
            node.addChild(this);
        }

        public boolean removeChild(ClassTreeNode node) {
            return this.children.remove(node);
        }
        public void addChild(ClassTreeNode node) {
            this.children.add(node);
        }

        public List getChildren() {
            return this.children;
        }

        public boolean equals(Object obj) {
            return ((ClassTreeNode)obj).getObjectClass().equals(this.objectClass);
        }

        public Class getObjectClass() {
            return this.objectClass;
        }

        public String getClassName() {
            return this.objectClass.getName();
        }


        public int hashCode() {
            return this.objectClass.hashCode();
        }

        /**
         * Compares one class to another class by their inheritance tree.
         *
         * @return an integer representing the comparison results as follows:<br>
         *    2  if this is a subclass of past in object<br>
         *    -2 if this is a superclass of past in object<br>
         *    0 if they are not related (and in relation to sorting, equal)<br>
         *    0  if they are the same<br>
         */
        public int compareTo(Object obj) {
            Class objClass = ((ClassTreeNode)obj).getObjectClass();

            if (objClass.equals(this.objectClass)) {
                return 0;
            } else if (this.objectClass.isAssignableFrom(objClass)) {
                return 2;
            } else if (objClass.isAssignableFrom(this.objectClass)) {
                return -2;
            } else {
                return 0;
            }
        }
    } // End of ClassTree$ClassTreeNode


}