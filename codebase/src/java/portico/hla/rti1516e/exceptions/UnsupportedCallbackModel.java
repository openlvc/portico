/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: UnsupportedCallbackModel.java
package hla.rti1516e.exceptions;


/**
 * Public exception class UnsupportedCallbackModel
 */
public final class UnsupportedCallbackModel extends RTIexception {
   public UnsupportedCallbackModel(String msg)
   {
      super(msg);
   }

   public UnsupportedCallbackModel(String message, Throwable cause)
   {
      super(message, cause);
   }

   public UnsupportedCallbackModel(Throwable cause)
   {
      super(cause);
   }
}
