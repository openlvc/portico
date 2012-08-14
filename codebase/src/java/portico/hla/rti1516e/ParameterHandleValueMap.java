/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: ParameterHandleValueMap.java
package hla.rti1516e;

import hla.rti1516e.encoding.ByteWrapper;

import java.util.Map;
import java.io.Serializable;

/**
 * Keys are ParameterHandles; values are byte[].
 * All operations are required, none optional.
 * Null mappings are not allowed.
 * put(), putAll(), and remove() should throw IllegalArgumentException to enforce
 * types of keys and mappings.
 */
public interface ParameterHandleValueMap
   extends Map<ParameterHandle, byte[]>, Cloneable, Serializable {

   /**
    * Returns a reference to the value to which this map maps the specified key.
    * Returns <tt>null</tt> if the map contains no mapping for this key.
    *
    * @param key key whose associated value is to be returned.
    * @return a reference to the value to which this map maps the specified key, or
    *         <tt>null</tt> if the map contains no mapping for this key.
    */
   ByteWrapper getValueReference(ParameterHandle key);

   /**
    * Returns the specified reference updated to the value to which this map
    * maps the specified key.
    * Returns <tt>null</tt> if the map contains no mapping for this key.
    *
    * @param key key whose associated value is to be returned.
    * @return the specified reference updated to the value to which this map maps the
    *         specified key, or <tt>null</tt> if the map contains no mapping for this key.
    */
   ByteWrapper getValueReference(ParameterHandle key, ByteWrapper byteWrapper);
}

//end ParameterHandleValueMap
