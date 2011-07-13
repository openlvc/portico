/***********************************************************************
  IEEE 1516.1 High Level Architecture Interface Specification C++ API
  File: RTI/Enums.h
***********************************************************************/

#ifndef RTI_Enums_h
#define RTI_Enums_h

#include <RTI/SpecificConfig.h>

namespace rti1516
{
   enum OrderType
   {
      RECEIVE,
      TIMESTAMP
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
      RTI_DETECTED_FAILURE_DURING_RESTORE
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
      SAVE_TIME_CANNOT_BE_HONORED
   };

   enum SaveStatus
   {
      NO_SAVE_IN_PROGRESS,
      FEDERATE_INSTRUCTED_TO_SAVE,
      FEDERATE_SAVING,
      FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE
   };

   enum ServiceGroupIndicator
   {
      FEDERATION_MANAGEMENT,
      DECLARATION_MANAGEMENT,
      OBJECT_MANAGEMENT,
      OWNERSHIP_MANAGEMENT,
      TIME_MANAGEMENT,
      DATA_DISTRIBUTION_MANAGEMENT,
      SUPPORT_SERVICES
   };

   enum SynchronizationFailureReason
   {
      SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE,
      SYNCHRONIZATION_SET_MEMBER_NOT_JOINED,
      FEDERATE_RESIGNED_DURING_SYNCHRONIZATION, 
      RTI_DETECTED_FAILURE_DURING_SYNCHRONIZATION,
      SYNCHRONIZATION_TIME_CANNOT_BE_HONORED
   };

   enum TransportationType
   {
      RELIABLE, 
      BEST_EFFORT 
   };
}

#endif // RTI_Enums_h
