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
 * @see FederateAmbassador#synchronizationPointRegistrationFailed
 */

public enum SynchronizationPointFailureReason {
   SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE,
   SYNCHRONIZATION_SET_MEMBER_NOT_JOINED
}
