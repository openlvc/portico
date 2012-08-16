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
 *
 * @see FederateAmbassador#federationNotSaved
 */

public enum SaveFailureReason {
   RTI_UNABLE_TO_SAVE,
   FEDERATE_REPORTED_FAILURE_DURING_SAVE,
   FEDERATE_RESIGNED_DURING_SAVE,
   RTI_DETECTED_FAILURE_DURING_SAVE,
   SAVE_TIME_CANNOT_BE_HONORED,
   SAVE_ABORTED
}
