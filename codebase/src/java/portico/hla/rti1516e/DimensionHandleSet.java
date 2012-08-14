/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: DimensionHandleSet.java

package hla.rti1516e;

import java.io.Serializable;import java.util.Set;

/**
 * All Set operations are required, none are optional.
 * add() and remove() should throw IllegalArgumentException if the argument is not
 * a DimensionHandle.
 * addAll(), removeAll() and retainAll() should throw IllegalArgumentException if
 * the argument is not a DimensionHandleSet.
 */

public interface DimensionHandleSet
   extends Set<DimensionHandle>, Cloneable, Serializable {
}

//end DimensionHandleSet

