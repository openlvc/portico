/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: RangeBounds.java

/**
 * Record returned by (10.31) getRangeBounds
 */

package hla.rti1516e;

import java.io.Serializable;

public final class RangeBounds
   implements Serializable {
   public RangeBounds(long l, long u)
   {
      lower = l;
      upper = u;
   }

   public final long lower;
   public final long upper;

   public boolean equals(Object o)
   {
      if (this == o) {
         return true;
      }
      if (!(o instanceof RangeBounds)) {
         return false;
      }

      final RangeBounds rangeBounds = (RangeBounds) o;

      if (lower != rangeBounds.lower) {
         return false;
      }
      if (upper != rangeBounds.upper) {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (int) (lower ^ (lower >>> 32));
      result = 29 * result + (int) (upper ^ (upper >>> 32));
      return result;
   }
}

//end RangeBounds
