/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: RTIexception.java
package hla.rti1516e.exceptions;

/**
 * Superclass of all exceptions thrown by the RTI.
 * All RTI exceptions must be caught or specified.
 */
public class RTIexception extends Exception {
   public RTIexception(String msg)
   {
      super(msg);
   }

   public RTIexception(String message, Throwable cause)
   {
      super(message, cause);
   }
   
   public RTIexception(Throwable cause)
   {
	   super(cause);
   }
}
