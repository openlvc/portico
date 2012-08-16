/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e;

/**
 * An enumerated type (not a Java Enumeration!)
 */

public enum RestoreStatus {
   NO_RESTORE_IN_PROGRESS, 
   FEDERATE_RESTORE_REQUEST_PENDING, 
   FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN,
   FEDERATE_PREPARED_TO_RESTORE, 
   FEDERATE_RESTORING, 
   FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE
}
