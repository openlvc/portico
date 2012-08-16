/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: AttributeSetRegionSetPairList.java

package hla.rti1516e;

import java.util.List;
import java.io.Serializable;

/**
 * This packages the attributes supplied to the RTI for various DDM services with
 * the regions to be used with the attributes.
 * Elements are AttributeRegionAssociations.
 * All operations are required, none optional.
 * add(), addAll(), and set() should throw IllegalArgumentException to enforce
 * type of elements.
 */
public interface AttributeSetRegionSetPairList
   extends List<AttributeRegionAssociation>, Cloneable, Serializable {
}

//end AttributeSetRegionSetPairList


