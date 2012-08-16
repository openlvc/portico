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
 * Interface for the HLA data type HLAfloat64LE.
 */
public interface HLAfloat64LE extends DataElement {

   /**
    * Returns the double value of this element.
    *
    * @return double value
    */
   double getValue();

   /**
    * Sets the double value of this element.
    *
    * @param value new value
    */
   void setValue(double value);
}
