/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e;

import hla.rti1516e.exceptions.CouldNotDecode;

/**
 * Enum used to specify order type.
 *
 * @see RTIambassador#changeAttributeOrderType(ObjectInstanceHandle,AttributeHandleSet,OrderType)
 * @see RTIambassador#changeInteractionOrderType(InteractionClassHandle,OrderType)
 */
public enum OrderType {
   RECEIVE(1),
   TIMESTAMP(2);
   private final int _value;

   OrderType(int value)
   {
      _value = value;
   }

   public int encodedLength()
   {
      return 1;
   }

   public void encode(byte[] buffer, int offset)
   {
      buffer[offset] = (byte) _value;
   }

   public static OrderType decode(byte[] buffer, int offset)
      throws CouldNotDecode
   {
      int value = buffer[offset];
      switch (value) {
         case 1:
            return RECEIVE;
         case 2:
            return TIMESTAMP;
         default:
            throw new CouldNotDecode("Cannot decode OrderType");
      }
   }
}
