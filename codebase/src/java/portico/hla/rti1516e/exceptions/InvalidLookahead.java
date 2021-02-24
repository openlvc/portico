/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e.exceptions;


/**
 * Public exception class InvalidLookahead
 */

public final class InvalidLookahead extends RTIexception {
   public InvalidLookahead(String msg)
   {
      super(msg);
   }

   public InvalidLookahead(String message, Throwable cause)
   {
      super(message, cause);
   }

   public InvalidLookahead(Throwable cause)
   {
      super(cause);
   }
}
