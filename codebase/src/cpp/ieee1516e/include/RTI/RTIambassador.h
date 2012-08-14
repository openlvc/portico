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
   File: RTI/RTIambassador.h
***********************************************************************/

// This interface is used to access the services of the RTI.

#ifndef RTI_RTIambassador_h
#define RTI_RTIambassador_h

namespace rti1516e
{
   class FederateAmbassador;
   class LogicalTime;
   class LogicalTimeFactory;
   class LogicalTimeInterval;
   class RangeBounds;
}

#include <RTI/SpecificConfig.h>
#include <string>
#include <RTI/Typedefs.h>
#include <RTI/Exception.h>

namespace rti1516e
{
   class RTI_EXPORT RTIambassador
   {
   protected:
      RTIambassador ()
          throw ();

   public:
      virtual ~RTIambassador ();

      // 4.2
      virtual void connect (
         FederateAmbassador & federateAmbassador,
         CallbackModel theCallbackModel,
         std::wstring const & localSettingsDesignator=L"")
         throw (
            ConnectionFailed,
            InvalidLocalSettingsDesignator,
            UnsupportedCallbackModel,
            AlreadyConnected,
            CallNotAllowedFromWithinCallback,
            RTIinternalError) = 0;

      // 4.3
      virtual void disconnect ()
         throw (
            FederateIsExecutionMember,
            CallNotAllowedFromWithinCallback,
            RTIinternalError) = 0;

      // 4.5
      virtual void createFederationExecution (
         std::wstring const & federationExecutionName,
         std::wstring const & fomModule,
         std::wstring const & logicalTimeImplementationName = L"")
         throw (
            CouldNotCreateLogicalTimeFactory,
            InconsistentFDD,
            ErrorReadingFDD,
            CouldNotOpenFDD,
            FederationExecutionAlreadyExists,
            NotConnected,
            RTIinternalError) = 0;

      virtual void createFederationExecution (
         std::wstring const & federationExecutionName,
         std::vector<std::wstring> const & fomModules,
         std::wstring const & logicalTimeImplementationName = L"")
         throw (
            CouldNotCreateLogicalTimeFactory,
            InconsistentFDD,
            ErrorReadingFDD,
            CouldNotOpenFDD,
            FederationExecutionAlreadyExists,
            NotConnected,
            RTIinternalError) = 0;

      virtual void createFederationExecutionWithMIM (
         std::wstring const & federationExecutionName,
         std::vector<std::wstring> const & fomModules,
         std::wstring const & mimModule,
         std::wstring const & logicalTimeImplementationName = L"")
         throw (
            CouldNotCreateLogicalTimeFactory,
            InconsistentFDD,
            ErrorReadingFDD,
            CouldNotOpenFDD,
            DesignatorIsHLAstandardMIM,
            ErrorReadingMIM,
            CouldNotOpenMIM,
            FederationExecutionAlreadyExists,
            NotConnected,
            RTIinternalError) = 0;

      // 4.6
      virtual void destroyFederationExecution (
         std::wstring const & federationExecutionName)
         throw (
            FederatesCurrentlyJoined,
            FederationExecutionDoesNotExist,
            NotConnected,
            RTIinternalError) = 0;

      // 4.7
      virtual void listFederationExecutions ()
         throw (
            NotConnected,
            RTIinternalError) = 0;

      // 4.9
      virtual FederateHandle joinFederationExecution (
         std::wstring const & federateType,
         std::wstring const & federationExecutionName,
         std::vector<std::wstring> const & additionalFomModules=std::vector<std::wstring>())
         throw (
            CouldNotCreateLogicalTimeFactory,
            FederationExecutionDoesNotExist,
            InconsistentFDD,
            ErrorReadingFDD,
            CouldNotOpenFDD,
            SaveInProgress,
            RestoreInProgress,
            FederateAlreadyExecutionMember,
            NotConnected,
            CallNotAllowedFromWithinCallback,
            RTIinternalError) = 0;

      virtual FederateHandle joinFederationExecution (
         std::wstring const & federateName,
         std::wstring const & federateType,
         std::wstring const & federationExecutionName,
         std::vector<std::wstring> const & additionalFomModules=std::vector<std::wstring>())
         throw (
            CouldNotCreateLogicalTimeFactory,
            FederateNameAlreadyInUse,
            FederationExecutionDoesNotExist,
         InconsistentFDD,
            ErrorReadingFDD,
            CouldNotOpenFDD,
            SaveInProgress,
            RestoreInProgress,
            FederateAlreadyExecutionMember,
            NotConnected,
            CallNotAllowedFromWithinCallback,
            RTIinternalError) = 0;

      // 4.10
      virtual void resignFederationExecution (
         ResignAction resignAction)
         throw (
            InvalidResignAction,
            OwnershipAcquisitionPending,
            FederateOwnsAttributes,
            FederateNotExecutionMember,
            NotConnected,
            CallNotAllowedFromWithinCallback,
            RTIinternalError) = 0;

      // 4.11
      virtual void registerFederationSynchronizationPoint (
         std::wstring const & label,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual void registerFederationSynchronizationPoint (
         std::wstring const & label,
         VariableLengthData const & theUserSuppliedTag,
         FederateHandleSet const & synchronizationSet)
         throw (
            InvalidFederateHandle,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.14
      virtual void synchronizationPointAchieved (
         std::wstring const & label,
         bool successfully = true)
         throw (
            SynchronizationPointLabelNotAnnounced,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.16
      virtual void requestFederationSave (
         std::wstring const & label)
         throw (
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual void requestFederationSave (
         std::wstring const & label,
         LogicalTime const & theTime)
         throw (
            LogicalTimeAlreadyPassed,
            InvalidLogicalTime,
            FederateUnableToUseTime,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.18
      virtual void federateSaveBegun ()
         throw (
            SaveNotInitiated,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.19
      virtual void federateSaveComplete ()
         throw (
            FederateHasNotBegunSave,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual void federateSaveNotComplete ()
         throw (
            FederateHasNotBegunSave,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.21
      virtual void abortFederationSave ()
         throw (
            SaveNotInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.22
      virtual void queryFederationSaveStatus ()
         throw (
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.24
      virtual void requestFederationRestore (
         std::wstring const & label)
         throw (
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.28
      virtual void federateRestoreComplete ()
         throw (
            RestoreNotRequested,
            SaveInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual void federateRestoreNotComplete ()
         throw (
            RestoreNotRequested,
            SaveInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.30
      virtual void abortFederationRestore ()
         throw (
            RestoreNotInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 4.31
      virtual void queryFederationRestoreStatus ()
         throw (
            SaveInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      /////////////////////////////////////
      // Declaration Management Services //
      /////////////////////////////////////

      // 5.2
      virtual void publishObjectClassAttributes (
         ObjectClassHandle theClass,
         AttributeHandleSet const & attributeList)
         throw (
            AttributeNotDefined,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 5.3
      virtual void unpublishObjectClass (
         ObjectClassHandle theClass)
         throw (
            OwnershipAcquisitionPending,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual void unpublishObjectClassAttributes (
         ObjectClassHandle theClass,
         AttributeHandleSet const & attributeList)
         throw (
            OwnershipAcquisitionPending,
            AttributeNotDefined,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 5.4
      virtual void publishInteractionClass (
         InteractionClassHandle theInteraction)
         throw (
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 5.5
      virtual void unpublishInteractionClass (
         InteractionClassHandle theInteraction)
         throw (
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 5.6
      virtual void subscribeObjectClassAttributes (
         ObjectClassHandle theClass,
         AttributeHandleSet const & attributeList,
         bool active = true,
         std::wstring const & updateRateDesignator = L"")
         throw (
            AttributeNotDefined,
            ObjectClassNotDefined,
            InvalidUpdateRateDesignator,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 5.7
      virtual void unsubscribeObjectClass (
         ObjectClassHandle theClass)
         throw (
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual void unsubscribeObjectClassAttributes (
         ObjectClassHandle theClass,
         AttributeHandleSet const & attributeList)
         throw (
            AttributeNotDefined,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 5.8
      virtual void subscribeInteractionClass (
         InteractionClassHandle theClass,
         bool active = true)
         throw (
            FederateServiceInvocationsAreBeingReportedViaMOM,
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 5.9
      virtual void unsubscribeInteractionClass (
         InteractionClassHandle theClass)
         throw (
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      ////////////////////////////////
      // Object Management Services //
      ////////////////////////////////

      // 6.2
      virtual void reserveObjectInstanceName (
         std::wstring const & theObjectInstanceName)
         throw (
            IllegalName,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.4
      virtual void releaseObjectInstanceName (
         std::wstring const & theObjectInstanceName)
         throw (
            ObjectInstanceNameNotReserved,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.5
      virtual void reserveMultipleObjectInstanceName (
         std::set<std::wstring> const & theObjectInstanceNames)
         throw (
            IllegalName,
            NameSetWasEmpty,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.7
      virtual void releaseMultipleObjectInstanceName (
         std::set<std::wstring> const & theObjectInstanceNames)
         throw (
            ObjectInstanceNameNotReserved,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.8
      virtual ObjectInstanceHandle registerObjectInstance (
         ObjectClassHandle theClass)
         throw (
            ObjectClassNotPublished,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual ObjectInstanceHandle registerObjectInstance (
         ObjectClassHandle theClass,
         std::wstring const & theObjectInstanceName)
         throw (
            ObjectInstanceNameInUse,
            ObjectInstanceNameNotReserved,
            ObjectClassNotPublished,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.10
      virtual void updateAttributeValues (
         ObjectInstanceHandle theObject,
         AttributeHandleValueMap const & theAttributeValues,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual MessageRetractionHandle updateAttributeValues (
         ObjectInstanceHandle theObject,
         AttributeHandleValueMap const & theAttributeValues,
         VariableLengthData const & theUserSuppliedTag,
         LogicalTime const & theTime)
         throw (
            InvalidLogicalTime,
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.12
      virtual void sendInteraction (
         InteractionClassHandle theInteraction,
         ParameterHandleValueMap const & theParameterValues,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            InteractionClassNotPublished,
            InteractionParameterNotDefined,
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual MessageRetractionHandle sendInteraction (
         InteractionClassHandle theInteraction,
         ParameterHandleValueMap const & theParameterValues,
         VariableLengthData const & theUserSuppliedTag,
         LogicalTime const & theTime)
         throw (
            InvalidLogicalTime,
            InteractionClassNotPublished,
            InteractionParameterNotDefined,
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.14
      virtual void deleteObjectInstance (
         ObjectInstanceHandle theObject,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            DeletePrivilegeNotHeld,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual MessageRetractionHandle deleteObjectInstance (
         ObjectInstanceHandle theObject,
         VariableLengthData const & theUserSuppliedTag,
         LogicalTime  const & theTime)
         throw (
            InvalidLogicalTime,
            DeletePrivilegeNotHeld,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.16
      virtual void localDeleteObjectInstance (
         ObjectInstanceHandle theObject)
         throw (
            OwnershipAcquisitionPending,
            FederateOwnsAttributes,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.19
      virtual void requestAttributeValueUpdate (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual void requestAttributeValueUpdate (
         ObjectClassHandle theClass,
         AttributeHandleSet const & theAttributes,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            AttributeNotDefined,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.23
      virtual void requestAttributeTransportationTypeChange (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes,
         TransportationType theType)
         throw (
            AttributeAlreadyBeingChanged,
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            InvalidTransportationType,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.25
      virtual void queryAttributeTransportationType (
         ObjectInstanceHandle theObject,
         AttributeHandle theAttribute)
         throw (
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.27
      virtual void requestInteractionTransportationTypeChange (
         InteractionClassHandle theClass,
         TransportationType theType)
         throw (
            InteractionClassAlreadyBeingChanged,
            InteractionClassNotPublished,
            InteractionClassNotDefined,
            InvalidTransportationType,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 6.29
      virtual void queryInteractionTransportationType (
         FederateHandle theFederate,
         InteractionClassHandle theInteraction)
         throw (
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;


      ///////////////////////////////////
      // Ownership Management Services //
      ///////////////////////////////////

      // 7.2
      virtual void unconditionalAttributeOwnershipDivestiture (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes)
         throw (
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.3
      virtual void negotiatedAttributeOwnershipDivestiture (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            AttributeAlreadyBeingDivested,
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.6
      virtual void confirmDivestiture (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & confirmedAttributes,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            NoAcquisitionPending,
            AttributeDivestitureWasNotRequested,
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.8
      virtual void attributeOwnershipAcquisition (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & desiredAttributes,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            AttributeNotPublished,
            ObjectClassNotPublished,
            FederateOwnsAttributes,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.9
      virtual void attributeOwnershipAcquisitionIfAvailable (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & desiredAttributes)
         throw (
            AttributeAlreadyBeingAcquired,
            AttributeNotPublished,
            ObjectClassNotPublished,
            FederateOwnsAttributes,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.12
      virtual void attributeOwnershipReleaseDenied (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes)
         throw (
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.13
      virtual void attributeOwnershipDivestitureIfWanted (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes,
         AttributeHandleSet & theDivestedAttributes) // filled by RTI
         throw (
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.14
      virtual void cancelNegotiatedAttributeOwnershipDivestiture (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes)
         throw (
            AttributeDivestitureWasNotRequested,
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.15
      virtual void cancelAttributeOwnershipAcquisition (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes)
         throw (
            AttributeAcquisitionWasNotRequested,
            AttributeAlreadyOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.17
      virtual void queryAttributeOwnership (
         ObjectInstanceHandle theObject,
         AttributeHandle theAttribute)
         throw (
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 7.19
      virtual bool isAttributeOwnedByFederate (
         ObjectInstanceHandle theObject,
         AttributeHandle theAttribute)
         throw (
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      //////////////////////////////
      // Time Management Services //
      //////////////////////////////

      // 8.2
      virtual void enableTimeRegulation (
         LogicalTimeInterval const & theLookahead)
         throw (
            InvalidLookahead,
            InTimeAdvancingState,
            RequestForTimeRegulationPending,
            TimeRegulationAlreadyEnabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.4
      virtual void disableTimeRegulation ()
         throw (
            TimeRegulationIsNotEnabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.5
      virtual void enableTimeConstrained ()
         throw (
            InTimeAdvancingState,
            RequestForTimeConstrainedPending,
            TimeConstrainedAlreadyEnabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.7
      virtual void disableTimeConstrained ()
         throw (
            TimeConstrainedIsNotEnabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.8
      virtual void timeAdvanceRequest (
         LogicalTime const & theTime)
         throw (
            LogicalTimeAlreadyPassed,
            InvalidLogicalTime,
            InTimeAdvancingState,
            RequestForTimeRegulationPending,
            RequestForTimeConstrainedPending,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.9
      virtual void timeAdvanceRequestAvailable (
         LogicalTime const & theTime)
         throw (
            LogicalTimeAlreadyPassed,
            InvalidLogicalTime,
            InTimeAdvancingState,
            RequestForTimeRegulationPending,
            RequestForTimeConstrainedPending,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.10
      virtual void nextMessageRequest (
         LogicalTime const & theTime)
         throw (
            LogicalTimeAlreadyPassed,
            InvalidLogicalTime,
            InTimeAdvancingState,
            RequestForTimeRegulationPending,
            RequestForTimeConstrainedPending,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.11
      virtual void nextMessageRequestAvailable (
         LogicalTime const & theTime)
         throw (
            LogicalTimeAlreadyPassed,
            InvalidLogicalTime,
            InTimeAdvancingState,
            RequestForTimeRegulationPending,
            RequestForTimeConstrainedPending,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.12
      virtual void flushQueueRequest (
         LogicalTime const & theTime)
         throw (
            LogicalTimeAlreadyPassed,
            InvalidLogicalTime,
            InTimeAdvancingState,
            RequestForTimeRegulationPending,
            RequestForTimeConstrainedPending,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.14
      virtual void enableAsynchronousDelivery ()
         throw (
            AsynchronousDeliveryAlreadyEnabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.15
      virtual void disableAsynchronousDelivery ()
         throw (
            AsynchronousDeliveryAlreadyDisabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.16
      virtual bool queryGALT (LogicalTime & theTime)
         throw (
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.17
      virtual void queryLogicalTime (LogicalTime & theTime)
         throw (
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.18
      virtual bool queryLITS (LogicalTime & theTime)
         throw (
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.19
      virtual void modifyLookahead (
         LogicalTimeInterval const & theLookahead)
         throw (
            InvalidLookahead,
            InTimeAdvancingState,
            TimeRegulationIsNotEnabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.20
      virtual void queryLookahead (LogicalTimeInterval & interval)
         throw (
            TimeRegulationIsNotEnabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.21
      virtual void retract (
         MessageRetractionHandle theHandle)
         throw (
            MessageCanNoLongerBeRetracted,
            InvalidMessageRetractionHandle,
            TimeRegulationIsNotEnabled,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.23
      virtual void changeAttributeOrderType (
         ObjectInstanceHandle theObject,
         AttributeHandleSet const & theAttributes,
         OrderType theType)
         throw (
            AttributeNotOwned,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 8.24
      virtual void changeInteractionOrderType (
         InteractionClassHandle theClass,
         OrderType theType)
         throw (
            InteractionClassNotPublished,
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      //////////////////////////////////
      // Data Distribution Management //
      //////////////////////////////////

      // 9.2
      virtual RegionHandle createRegion (
         DimensionHandleSet const & theDimensions)
         throw (
            InvalidDimensionHandle,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.3
      virtual void commitRegionModifications (
         RegionHandleSet const & theRegionHandleSet)
         throw (
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.4
      virtual void deleteRegion (
         RegionHandle const & theRegion)
         throw (
            RegionInUseForUpdateOrSubscription,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.5
      virtual ObjectInstanceHandle registerObjectInstanceWithRegions (
         ObjectClassHandle theClass,
         AttributeHandleSetRegionHandleSetPairVector const &
         theAttributeHandleSetRegionHandleSetPairVector)
         throw (
            InvalidRegionContext,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            AttributeNotPublished,
            ObjectClassNotPublished,
            AttributeNotDefined,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual ObjectInstanceHandle registerObjectInstanceWithRegions (
         ObjectClassHandle theClass,
         AttributeHandleSetRegionHandleSetPairVector const &
         theAttributeHandleSetRegionHandleSetPairVector,
         std::wstring const & theObjectInstanceName)
         throw (
            ObjectInstanceNameInUse,
            ObjectInstanceNameNotReserved,
            InvalidRegionContext,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            AttributeNotPublished,
            ObjectClassNotPublished,
            AttributeNotDefined,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.6
      virtual void associateRegionsForUpdates (
         ObjectInstanceHandle theObject,
         AttributeHandleSetRegionHandleSetPairVector const &
         theAttributeHandleSetRegionHandleSetPairVector)
         throw (
            InvalidRegionContext,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.7
      virtual void unassociateRegionsForUpdates (
         ObjectInstanceHandle theObject,
         AttributeHandleSetRegionHandleSetPairVector const &
         theAttributeHandleSetRegionHandleSetPairVector)
         throw (
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            AttributeNotDefined,
            ObjectInstanceNotKnown,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.8
      virtual void subscribeObjectClassAttributesWithRegions (
         ObjectClassHandle theClass,
         AttributeHandleSetRegionHandleSetPairVector const &
         theAttributeHandleSetRegionHandleSetPairVector,
         bool active = true,
         std::wstring const & updateRateDesignator = L"")
         throw (
            InvalidRegionContext,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            AttributeNotDefined,
            ObjectClassNotDefined,
            InvalidUpdateRateDesignator,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;


      // 9.9
      virtual void unsubscribeObjectClassAttributesWithRegions (
         ObjectClassHandle theClass,
         AttributeHandleSetRegionHandleSetPairVector const &
         theAttributeHandleSetRegionHandleSetPairVector)
         throw (
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            AttributeNotDefined,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.10
      virtual void subscribeInteractionClassWithRegions (
         InteractionClassHandle theClass,
         RegionHandleSet const & theRegionHandleSet,
         bool active = true)
         throw (
            FederateServiceInvocationsAreBeingReportedViaMOM,
            InvalidRegionContext,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.11
      virtual void unsubscribeInteractionClassWithRegions (
         InteractionClassHandle theClass,
         RegionHandleSet const & theRegionHandleSet)
         throw (
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.12
      virtual void sendInteractionWithRegions (
         InteractionClassHandle theInteraction,
         ParameterHandleValueMap const & theParameterValues,
         RegionHandleSet const & theRegionHandleSet,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            InvalidRegionContext,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            InteractionClassNotPublished,
            InteractionParameterNotDefined,
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual MessageRetractionHandle sendInteractionWithRegions (
         InteractionClassHandle theInteraction,
         ParameterHandleValueMap const & theParameterValues,
         RegionHandleSet const & theRegionHandleSet,
         VariableLengthData const & theUserSuppliedTag,
         LogicalTime const & theTime)
         throw (
            InvalidLogicalTime,
            InvalidRegionContext,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            InteractionClassNotPublished,
            InteractionParameterNotDefined,
            InteractionClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 9.13
      virtual void requestAttributeValueUpdateWithRegions (
         ObjectClassHandle theClass,
         AttributeHandleSetRegionHandleSetPairVector const & theSet,
         VariableLengthData const & theUserSuppliedTag)
         throw (
            InvalidRegionContext,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            AttributeNotDefined,
            ObjectClassNotDefined,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      //////////////////////////
      // RTI Support Services //
      //////////////////////////

      // 10.2
      virtual ResignAction getAutomaticResignDirective ()
         throw (
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.3
      virtual void setAutomaticResignDirective (
         ResignAction resignAction)
         throw (
            InvalidResignAction,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.4
      virtual FederateHandle getFederateHandle (
         std::wstring const & theName)
         throw (
            NameNotFound,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.5
      virtual std::wstring getFederateName (
         FederateHandle theHandle)
         throw (
            InvalidFederateHandle,
            FederateHandleNotKnown,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.6
      virtual ObjectClassHandle getObjectClassHandle (
         std::wstring const & theName)
         throw (
            NameNotFound,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.7
      virtual std::wstring getObjectClassName (
         ObjectClassHandle theHandle)
         throw (
            InvalidObjectClassHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.8
      virtual ObjectClassHandle getKnownObjectClassHandle (
         ObjectInstanceHandle theObject)
         throw (
            ObjectInstanceNotKnown,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.9
      virtual ObjectInstanceHandle getObjectInstanceHandle (
         std::wstring const & theName)
         throw (
            ObjectInstanceNotKnown,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.10
      virtual std::wstring getObjectInstanceName (
         ObjectInstanceHandle theHandle)
         throw (
            ObjectInstanceNotKnown,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.11
      virtual AttributeHandle getAttributeHandle (
         ObjectClassHandle whichClass,
         std::wstring const & theAttributeName)
         throw (
            NameNotFound,
            InvalidObjectClassHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.12
      virtual std::wstring getAttributeName (
         ObjectClassHandle whichClass,
         AttributeHandle theHandle)
         throw (
            AttributeNotDefined,
            InvalidAttributeHandle,
            InvalidObjectClassHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.13
      virtual double getUpdateRateValue (
         std::wstring const & updateRateDesignator)
         throw (
            InvalidUpdateRateDesignator,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.14
      virtual double getUpdateRateValueForAttribute (
         ObjectInstanceHandle theObject,
         AttributeHandle theAttribute)
         throw (
            ObjectInstanceNotKnown,
            AttributeNotDefined,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.15
      virtual InteractionClassHandle getInteractionClassHandle (
         std::wstring const & theName)
         throw (
            NameNotFound,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.16
      virtual std::wstring getInteractionClassName (
         InteractionClassHandle theHandle)
         throw (
            InvalidInteractionClassHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.17
      virtual ParameterHandle getParameterHandle (
         InteractionClassHandle whichClass,
         std::wstring const & theName)
         throw (
            NameNotFound,
            InvalidInteractionClassHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.18
      virtual std::wstring getParameterName (
         InteractionClassHandle whichClass,
         ParameterHandle theHandle)
         throw (
            InteractionParameterNotDefined,
            InvalidParameterHandle,
            InvalidInteractionClassHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.19
      virtual OrderType getOrderType (
         std::wstring const & orderName)
         throw (
            InvalidOrderName,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.20
      virtual std::wstring getOrderName (
         OrderType orderType)
         throw (
            InvalidOrderType,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.21
      virtual TransportationType getTransportationType (
         std::wstring const & transportationName)
         throw (
            InvalidTransportationName,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.22
      virtual std::wstring getTransportationName (
         TransportationType transportationType)
         throw (
            InvalidTransportationType,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.23
      virtual DimensionHandleSet getAvailableDimensionsForClassAttribute (
         ObjectClassHandle theClass,
         AttributeHandle theHandle)
         throw (
            AttributeNotDefined,
            InvalidAttributeHandle,
            InvalidObjectClassHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.24
      virtual DimensionHandleSet getAvailableDimensionsForInteractionClass (
         InteractionClassHandle theClass)
         throw (
            InvalidInteractionClassHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.25
      virtual DimensionHandle getDimensionHandle (
         std::wstring const & theName)
         throw (
            NameNotFound,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.26
      virtual std::wstring getDimensionName (
         DimensionHandle theHandle)
         throw (
            InvalidDimensionHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.27
      virtual unsigned long getDimensionUpperBound (
         DimensionHandle theHandle)
         throw (
            InvalidDimensionHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.28
      virtual DimensionHandleSet getDimensionHandleSet (
         RegionHandle theRegionHandle)
         throw (
            InvalidRegion,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.29
      virtual RangeBounds getRangeBounds (
         RegionHandle theRegionHandle,
         DimensionHandle theDimensionHandle)
         throw (
            RegionDoesNotContainSpecifiedDimension,
            InvalidRegion,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.30
      virtual void setRangeBounds (
         RegionHandle theRegionHandle,
         DimensionHandle theDimensionHandle,
         RangeBounds const & theRangeBounds)
         throw (
            InvalidRangeBound,
            RegionDoesNotContainSpecifiedDimension,
            RegionNotCreatedByThisFederate,
            InvalidRegion,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.31
      virtual unsigned long normalizeFederateHandle (
         FederateHandle theFederateHandle)
         throw (
            InvalidFederateHandle,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.32
      virtual unsigned long normalizeServiceGroup (
         ServiceGroup theServiceGroup)
         throw (
            InvalidServiceGroup,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.33
      virtual void enableObjectClassRelevanceAdvisorySwitch ()
         throw (
            ObjectClassRelevanceAdvisorySwitchIsOn,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.34
      virtual void disableObjectClassRelevanceAdvisorySwitch ()
         throw (
            ObjectClassRelevanceAdvisorySwitchIsOff,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.35
      virtual void enableAttributeRelevanceAdvisorySwitch ()
         throw (
            AttributeRelevanceAdvisorySwitchIsOn,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.36
      virtual void disableAttributeRelevanceAdvisorySwitch ()
         throw (
            AttributeRelevanceAdvisorySwitchIsOff,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.37
      virtual void enableAttributeScopeAdvisorySwitch ()
         throw (
            AttributeScopeAdvisorySwitchIsOn,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.38
      virtual void disableAttributeScopeAdvisorySwitch ()
         throw (
            AttributeScopeAdvisorySwitchIsOff,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.39
      virtual void enableInteractionRelevanceAdvisorySwitch ()
         throw (
            InteractionRelevanceAdvisorySwitchIsOn,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.40
      virtual void disableInteractionRelevanceAdvisorySwitch ()
         throw (
            InteractionRelevanceAdvisorySwitchIsOff,
            SaveInProgress,
            RestoreInProgress,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // 10.41
      virtual bool evokeCallback (
         double approximateMinimumTimeInSeconds)
         throw (
            CallNotAllowedFromWithinCallback,
            RTIinternalError) = 0;

      // 10.42
      virtual bool evokeMultipleCallbacks (
         double approximateMinimumTimeInSeconds,
         double approximateMaximumTimeInSeconds)
         throw (
            CallNotAllowedFromWithinCallback,
            RTIinternalError) = 0;

      // 10.43
      virtual void enableCallbacks ()
         throw (
            SaveInProgress,
            RestoreInProgress,
            RTIinternalError) = 0;

      // 10.44
      virtual void disableCallbacks ()
         throw (
            SaveInProgress,
            RestoreInProgress,
            RTIinternalError) = 0;

      // API-specific services

      // Return instance of time factory being used by the federation
      virtual std::auto_ptr<LogicalTimeFactory> getTimeFactory () const
         throw (
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      // Decode handles
      virtual FederateHandle decodeFederateHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual ObjectClassHandle decodeObjectClassHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual InteractionClassHandle decodeInteractionClassHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual ObjectInstanceHandle decodeObjectInstanceHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual AttributeHandle decodeAttributeHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual ParameterHandle decodeParameterHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual DimensionHandle decodeDimensionHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual MessageRetractionHandle decodeMessageRetractionHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;

      virtual RegionHandle decodeRegionHandle (
         VariableLengthData const & encodedValue) const
         throw (
            CouldNotDecode,
            FederateNotExecutionMember,
            NotConnected,
            RTIinternalError) = 0;
   };
}

#endif // RTI_RTIambassador_h
