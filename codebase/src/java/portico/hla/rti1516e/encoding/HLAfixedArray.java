/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e.encoding;

import java.util.Iterator;

/**
 * Interface for the HLA data type HLAfixedArray.
 */
public interface HLAfixedArray<T extends DataElement> extends DataElement, Iterable<T> {

   /**
    * Returns the number of elements in this fixed array.
    *
    * @return the number of elements in this fixed array
    */
   int size();

   /**
    * Returns the element at the specified <code>index</code>.
    *
    * @param index index of element to get
    *
    * @return the element at the specified <code>index</code>
    */
   T get(int index);

   /**
    * Returns an iterator for the elements in this fixed array.
    *
    * @return an iterator for the elements in this fixed array
    */
   Iterator<T> iterator();
}
