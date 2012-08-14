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
 * Interface used to populate arrays.
 * <p/>
 * This example decodes a variable array of HLAinteger32BE using
 * a factory.
 * <pre>
 * DataElementFactory factory = new DataElementFactory()
 * {
 *    public DataElement createElement(int index)
 *    {
 *       return encoderFactory.createHLAinteger32BE();
 *    }
 * };
 * HLAvariableArray post = encoderFactory.createHLAvariableArray(factory);
 * post.decode(bytes);
 * </pre>
 */
public interface DataElementFactory<T extends DataElement> {
   /**
    * Creates an element appropriate for the specified index.
    *
    * @param index Position in array that this element will take.
    * @return Element
    */
   T createElement(int index);
}
