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
 * Interface for the HLA data type HLAfixedRecord.
 */
public interface HLAfixedRecord extends DataElement, Iterable<DataElement> {

   /**
    * Adds an element to this fixed record.
    *
    * @param dataElement element to add
    */
   void add(DataElement dataElement);

   /**
    * Returns the number of elements in this fixed record.
    *
    * @return the number of elements in this fixed record
    */
   int size();

   /**
    * Returns element at the specified index.
    *
    * @param index index of element to get
    *
    * @return the element at the specified <code>index</code>
    */
   DataElement get(int index);

   /**
    * Returns an iterator for the elements in this fixed record.
    *
    * @return an iterator for the elements in this fixed record.
    */
   Iterator<DataElement> iterator();
}
