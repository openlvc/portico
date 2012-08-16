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
 * Interface for the HLA data type HLAunicodeString.
 */
public interface HLAunicodeString extends DataElement {

   /**
    * Returns the string value of this element.
    *
    * @return string value
    */
   String getValue();

   /**
    * Sets the string value of this element.
    *
    * @param value new value
    */
   void setValue(String value);
}
