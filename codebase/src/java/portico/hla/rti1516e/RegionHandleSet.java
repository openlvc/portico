/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: RegionHandleSet.java

package hla.rti1516e;

import java.util.Set;
import java.io.Serializable;

/**
 * All Set operations are required, none are optional.
 * add() and remove() should throw IllegalArgumentException if the argument is not
 * a RegionHandle.
 * addAll(), removeAll() and retainAll() should throw IllegalArgumentException if
 * the argument is not a RegionHandleSet.
 */

public interface RegionHandleSet
   extends Set<RegionHandle>, Cloneable, Serializable {
}

//end RegionHandleSet

