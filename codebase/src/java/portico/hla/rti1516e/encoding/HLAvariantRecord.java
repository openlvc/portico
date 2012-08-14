/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e.encoding;

/**
 * Interface for the HLA data type HLAvariantRecord.
 */
public interface HLAvariantRecord<T extends DataElement> extends DataElement {
   /**
    * Associates the data element for a specified discriminant.
    *
    * @param discriminant discriminant to associate data element with
    * @param dataElement  data element to associate the discriminant with
    */
   void setVariant(T discriminant, DataElement dataElement);

   /**
    * Sets the active discriminant.
    *
    * @param discriminant active discriminant
    */
   void setDiscriminant(T discriminant);

   /**
    * Returns the active discriminant.
    *
    * @return the active discriminant
    */
   T getDiscriminant();

   /**
    * Returns element associated with the active discriminant.
    *
    * @return value
    */
   DataElement getValue();
}
