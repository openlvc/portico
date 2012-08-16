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
   File: RTI/exception.h
***********************************************************************/

#ifndef  RTI_Exception_h
#define  RTI_Exception_h

#include <RTI/SpecificConfig.h>
#include <string>

// The RTI::exception class follows the interface of the C++ standard exception
// class.  The key method, what, returns a null terminated character string that
// describes details of the exception that has occured.

namespace rti1516e
{
   class RTI_EXPORT Exception
   {
      public:
      Exception ();

      Exception (
         Exception const & rhs);

      Exception &
      operator= (
         Exception const & rhs);

      virtual
      ~Exception ();
      // throw();

      virtual
      std::wstring
      what () const
         throw() = 0;
   };

   // Output operator for Exceptions
   std::wostream RTI_EXPORT &
   operator<< (
   std::wostream &,
   Exception const &);

   #define RTI_EXCEPTION(A)               \
   class RTI_EXPORT A : public Exception  \
   {                                      \
      public:                             \
      A (                                 \
         std::wstring const & message)    \
         throw();                         \
                                          \
      std::wstring                        \
      what () const                       \
         throw();                         \
                                          \
      private:                            \
      std::wstring _msg;                  \
   };

   RTI_EXCEPTION(AlreadyConnected)
   RTI_EXCEPTION(AsynchronousDeliveryAlreadyDisabled)
   RTI_EXCEPTION(AsynchronousDeliveryAlreadyEnabled)
   RTI_EXCEPTION(AttributeAcquisitionWasNotCanceled)
   RTI_EXCEPTION(AttributeAcquisitionWasNotRequested)
   RTI_EXCEPTION(AttributeAlreadyBeingAcquired)
   RTI_EXCEPTION(AttributeAlreadyBeingChanged)
   RTI_EXCEPTION(AttributeAlreadyBeingDivested)
   RTI_EXCEPTION(AttributeAlreadyOwned)
   RTI_EXCEPTION(AttributeDivestitureWasNotRequested)
   RTI_EXCEPTION(AttributeNotDefined)
   RTI_EXCEPTION(AttributeNotOwned)
   RTI_EXCEPTION(AttributeNotPublished)
   RTI_EXCEPTION(AttributeNotRecognized)
   RTI_EXCEPTION(AttributeNotSubscribed)
   RTI_EXCEPTION(AttributeRelevanceAdvisorySwitchIsOff)
   RTI_EXCEPTION(AttributeRelevanceAdvisorySwitchIsOn)
   RTI_EXCEPTION(AttributeScopeAdvisorySwitchIsOff)
   RTI_EXCEPTION(AttributeScopeAdvisorySwitchIsOn)
   RTI_EXCEPTION(BadInitializationParameter)
   RTI_EXCEPTION(CallNotAllowedFromWithinCallback)
   RTI_EXCEPTION(ConnectionFailed)
   RTI_EXCEPTION(CouldNotCreateLogicalTimeFactory)
   RTI_EXCEPTION(CouldNotDecode)
   RTI_EXCEPTION(CouldNotDiscover)
   RTI_EXCEPTION(CouldNotEncode)
   RTI_EXCEPTION(CouldNotOpenFDD)
   RTI_EXCEPTION(CouldNotOpenMIM)
   RTI_EXCEPTION(CouldNotInitiateRestore)
   RTI_EXCEPTION(DeletePrivilegeNotHeld)
   RTI_EXCEPTION(DesignatorIsHLAstandardMIM)
   RTI_EXCEPTION(RequestForTimeConstrainedPending)
   RTI_EXCEPTION(NoRequestToEnableTimeConstrainedWasPending)
   RTI_EXCEPTION(RequestForTimeRegulationPending)
   RTI_EXCEPTION(NoRequestToEnableTimeRegulationWasPending)
   RTI_EXCEPTION(NoFederateWillingToAcquireAttribute)
   RTI_EXCEPTION(ErrorReadingFDD)
   RTI_EXCEPTION(ErrorReadingMIM)
   RTI_EXCEPTION(FederateAlreadyExecutionMember)
   RTI_EXCEPTION(FederateHandleNotKnown)
   RTI_EXCEPTION(FederateHasNotBegunSave)
   RTI_EXCEPTION(FederateInternalError)
   RTI_EXCEPTION(FederateIsExecutionMember)
   RTI_EXCEPTION(FederateNameAlreadyInUse)
   RTI_EXCEPTION(FederateNotExecutionMember)
   RTI_EXCEPTION(FederateOwnsAttributes)
   RTI_EXCEPTION(FederateServiceInvocationsAreBeingReportedViaMOM)
   RTI_EXCEPTION(FederateUnableToUseTime)
   RTI_EXCEPTION(FederatesCurrentlyJoined)
   RTI_EXCEPTION(FederationExecutionAlreadyExists)
   RTI_EXCEPTION(FederationExecutionDoesNotExist)
   RTI_EXCEPTION(IllegalName)
   RTI_EXCEPTION(IllegalTimeArithmetic)
   RTI_EXCEPTION(InconsistentFDD)
   RTI_EXCEPTION(InteractionClassAlreadyBeingChanged)
   RTI_EXCEPTION(InteractionClassNotDefined)
   RTI_EXCEPTION(InteractionClassNotPublished)
   RTI_EXCEPTION(InteractionClassNotRecognized)
   RTI_EXCEPTION(InteractionClassNotSubscribed)
   RTI_EXCEPTION(InteractionParameterNotDefined)
   RTI_EXCEPTION(InteractionParameterNotRecognized)
   RTI_EXCEPTION(InteractionRelevanceAdvisorySwitchIsOff)
   RTI_EXCEPTION(InteractionRelevanceAdvisorySwitchIsOn)
   RTI_EXCEPTION(InTimeAdvancingState)
   RTI_EXCEPTION(InvalidAttributeHandle)
   RTI_EXCEPTION(InvalidDimensionHandle)
   RTI_EXCEPTION(InvalidFederateHandle)
   RTI_EXCEPTION(InvalidInteractionClassHandle)
   RTI_EXCEPTION(InvalidLocalSettingsDesignator)
   RTI_EXCEPTION(InvalidLogicalTime)
   RTI_EXCEPTION(InvalidLogicalTimeInterval)
   RTI_EXCEPTION(InvalidLookahead)
   RTI_EXCEPTION(InvalidObjectClassHandle)
   RTI_EXCEPTION(InvalidOrderName)
   RTI_EXCEPTION(InvalidOrderType)
   RTI_EXCEPTION(InvalidParameterHandle)
   RTI_EXCEPTION(InvalidRangeBound)
   RTI_EXCEPTION(InvalidRegion)
   RTI_EXCEPTION(InvalidResignAction)
   RTI_EXCEPTION(InvalidRegionContext)
   RTI_EXCEPTION(InvalidMessageRetractionHandle)
   RTI_EXCEPTION(InvalidServiceGroup)
   RTI_EXCEPTION(InvalidTransportationName)
   RTI_EXCEPTION(InvalidTransportationType)
   RTI_EXCEPTION(InvalidUpdateRateDesignator)
   RTI_EXCEPTION(JoinedFederateIsNotInTimeAdvancingState)
   RTI_EXCEPTION(LogicalTimeAlreadyPassed)
   RTI_EXCEPTION(MessageCanNoLongerBeRetracted)
   RTI_EXCEPTION(NameNotFound)
   RTI_EXCEPTION(NameSetWasEmpty)
   RTI_EXCEPTION(NoAcquisitionPending)
   RTI_EXCEPTION(NotConnected)
   RTI_EXCEPTION(ObjectClassNotDefined)
   RTI_EXCEPTION(ObjectClassNotKnown)
   RTI_EXCEPTION(ObjectClassNotPublished)
   RTI_EXCEPTION(ObjectClassRelevanceAdvisorySwitchIsOff)
   RTI_EXCEPTION(ObjectClassRelevanceAdvisorySwitchIsOn)
   RTI_EXCEPTION(ObjectInstanceNameInUse)
   RTI_EXCEPTION(ObjectInstanceNameNotReserved)
   RTI_EXCEPTION(ObjectInstanceNotKnown)
   RTI_EXCEPTION(OwnershipAcquisitionPending)
   RTI_EXCEPTION(RTIinternalError)
   RTI_EXCEPTION(RegionDoesNotContainSpecifiedDimension)
   RTI_EXCEPTION(RegionInUseForUpdateOrSubscription)
   RTI_EXCEPTION(RegionNotCreatedByThisFederate)
   RTI_EXCEPTION(RestoreInProgress)
   RTI_EXCEPTION(RestoreNotInProgress)
   RTI_EXCEPTION(RestoreNotRequested)
   RTI_EXCEPTION(SaveInProgress)
   RTI_EXCEPTION(SaveNotInProgress)
   RTI_EXCEPTION(SaveNotInitiated)
   RTI_EXCEPTION(SpecifiedSaveLabelDoesNotExist)
   RTI_EXCEPTION(SynchronizationPointLabelNotAnnounced)
   RTI_EXCEPTION(TimeConstrainedAlreadyEnabled)
   RTI_EXCEPTION(TimeConstrainedIsNotEnabled)
   RTI_EXCEPTION(TimeRegulationAlreadyEnabled)
   RTI_EXCEPTION(TimeRegulationIsNotEnabled)
   RTI_EXCEPTION(UnableToPerformSave)
   RTI_EXCEPTION(UnknownName)
   RTI_EXCEPTION(UnsupportedCallbackModel)
   RTI_EXCEPTION(InternalError)
   #undef RTI_EXCEPTION
}

#endif // RTI_exception_h
