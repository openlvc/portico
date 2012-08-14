/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: FederateRestoreStatus.java
package hla.rti1516e;

import java.io.Serializable;

/**
 * Array of these records returned by (4.25) federationRestoreStatusResponse
 */
public final class FederateRestoreStatus
   implements Serializable {
   public FederateRestoreStatus(FederateHandle preRestoreHandle, FederateHandle postRestoreHandle, RestoreStatus rs)
   {
      this.preRestoreHandle = preRestoreHandle;
      this.postRestoreHandle = postRestoreHandle;
      status = rs;
   }

   public final FederateHandle preRestoreHandle;
   public final FederateHandle postRestoreHandle;
   public final RestoreStatus status;
}

//end FederateRestoreStatus
