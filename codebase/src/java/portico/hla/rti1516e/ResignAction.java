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
 * Enum used to select action taken by resignFederationExecution.
 *
 * @see RTIambassador#resignFederationExecution
 */
public enum ResignAction {
   UNCONDITIONALLY_DIVEST_ATTRIBUTES,
   DELETE_OBJECTS,
   CANCEL_PENDING_OWNERSHIP_ACQUISITIONS,
   DELETE_OBJECTS_THEN_DIVEST,
   CANCEL_THEN_DELETE_THEN_DIVEST,
   NO_ACTION,
}
