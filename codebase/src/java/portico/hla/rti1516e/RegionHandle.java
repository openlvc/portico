/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: RegionHandle.java

package hla.rti1516e;

import java.io.Serializable;


public interface RegionHandle extends Serializable {

   /**
    * @return true if this refers to the same Region as other handle
    */
   boolean equals(Object otherRegionHandle);

   /**
    * @return int. All instances that refer to the same Region should return the
    *         same hashcode.
    */
   int hashCode();

   String toString();

}

//end RegionHandle
