/***********************************************************************
   The IEEE hereby grants a general, royalty-free license to copy, distribute,
   display and make derivative works from this material, for all purposes,
   provided that any use of the material contains the following
   attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
   Should you require additional information, contact the Manager, Standards
   Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
***********************************************************************/
/***********************************************************************
   IEEE 1516.1 High Level Architecture Interface Specification C++ API
   File: RTI/Enums.h
***********************************************************************/

#ifndef RTI_Enums_h
#define RTI_Enums_h

#include <RTI/SpecificConfig.h>

namespace rti1516e
{
   enum CallbackModel
   {
      HLA_IMMEDIATE,
      HLA_EVOKED
   };

   enum OrderType
   {
      RECEIVE = 1,
      TIMESTAMP = 2
   };

   enum ResignAction
   {
      UNCONDITIONALLY_DIVEST_ATTRIBUTES,
      DELETE_OBJECTS,
      CANCEL_PENDING_OWNERSHIP_ACQUISITIONS,
      DELETE_OBJECTS_THEN_DIVEST,
      CANCEL_THEN_DELETE_THEN_DIVEST,
      NO_ACTION
   };

   enum RestoreFailureReason
   {
      RTI_UNABLE_TO_RESTORE,
      FEDERATE_REPORTED_FAILURE_DURING_RESTORE,
      FEDERATE_RESIGNED_DURING_RESTORE,
      RTI_DETECTED_FAILURE_DURING_RESTORE,
      RESTORE_ABORTED
   };

   enum RestoreStatus
   {
      NO_RESTORE_IN_PROGRESS,
      FEDERATE_RESTORE_REQUEST_PENDING,
      FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN,
      FEDERATE_PREPARED_TO_RESTORE,
      FEDERATE_RESTORING,
      FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE
   };

   enum SaveFailureReason
   {
      RTI_UNABLE_TO_SAVE,
      FEDERATE_REPORTED_FAILURE_DURING_SAVE,
      FEDERATE_RESIGNED_DURING_SAVE,
      RTI_DETECTED_FAILURE_DURING_SAVE,
      SAVE_TIME_CANNOT_BE_HONORED,
      SAVE_ABORTED
   };

   enum SaveStatus
   {
      NO_SAVE_IN_PROGRESS,
      FEDERATE_INSTRUCTED_TO_SAVE,
      FEDERATE_SAVING,
      FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE
   };

   enum ServiceGroup
   {
      FEDERATION_MANAGEMENT,
      DECLARATION_MANAGEMENT,
      OBJECT_MANAGEMENT,
      OWNERSHIP_MANAGEMENT,
      TIME_MANAGEMENT,
      DATA_DISTRIBUTION_MANAGEMENT,
      SUPPORT_SERVICES
   };

   enum SynchronizationPointFailureReason
   {
      SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE,
      SYNCHRONIZATION_SET_MEMBER_NOT_JOINED
   };

   enum TransportationType
   {
      RELIABLE = 1,
      BEST_EFFORT = 2
   };
}

#endif // RTI_Enums_h
