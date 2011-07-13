//File RTItypes.hh
//Included in RTI.hh

#ifndef RTITYPES_HH_INCLUDED
#define RTITYPES_HH_INCLUDED


#define MAX_FEDERATION                "macro variable is deprecated" 
#define MAX_FEDERATE                  "macro variable is deprecated"
#define MAX_NAME_LENGTH               "macro variable is deprecated"
#define MAX_SPACES                    "macro variable is deprecated"
#define MAX_OBJECT_CLASSES            "macro variable is deprecated"
#define MAX_INTERACTION_CLASSES       "macro variable is deprecated"
#define MAX_ATTRIBUTES_PER_CLASS      "macro variable is deprecated"
#define MAX_PARAMETERS_PER_CLASS      "macro variable is deprecated"
#define MAX_DIMENSIONS_PER_SPACE      "macro variable is deprecated"
#define DEFAULT_SPACE_NAME            "defaultSpace"
#define DEFAULT_SPACE_DIMENSION_NAME  "dimension"
#define MAX_USER_TAG_LENGTH           "macro variable is deprecated"

// RTI-1.3NG note: none of these versions are used by RTI-1.3NG; they
// are here strictly for conformance with the C++ interface
// specification
#define RTI_VERSION                   "RTI-1.3NGv5-mc02"
#define RTI_INTERNAL_VERSION          "1.3NG"

#define RTI_MAJOR_VERSION             1
#define RTI_MINOR_VERSION             3
#define RTI_RELEASE                   0

#define RTI_INTERNAL_MAJOR_VERSION    1
#define RTI_INTERNAL_MINOR_VERSION    3
#define RTI_INTERNAL_RELEASE          0

#define MAX_EXTENT                    ((RTI::ULong) ~0UL)
#define MIN_EXTENT                    ((RTI::ULong) 0UL)

RTI_EXCEPT(ArrayIndexOutOfBounds)
RTI_EXCEPT(AsynchronousDeliveryAlreadyDisabled)
RTI_EXCEPT(AsynchronousDeliveryAlreadyEnabled)  
RTI_EXCEPT(AttributeAcquisitionWasNotRequested)
RTI_EXCEPT(AttributeAcquisitionWasNotCanceled)  
RTI_EXCEPT(AttributeAlreadyBeingAcquired)  
RTI_EXCEPT(AttributeAlreadyBeingDivested)  
RTI_EXCEPT(AttributeAlreadyOwned)
RTI_EXCEPT(AttributeDivestitureWasNotRequested)  
RTI_EXCEPT(AttributeNotDefined)
RTI_EXCEPT(AttributeNotKnown)
RTI_EXCEPT(AttributeNotOwned)
RTI_EXCEPT(AttributeNotPublished)
RTI_EXCEPT(ConcurrentAccessAttempted)
RTI_EXCEPT(CouldNotDiscover)
RTI_EXCEPT(CouldNotOpenFED)
RTI_EXCEPT(CouldNotRestore)
RTI_EXCEPT(DeletePrivilegeNotHeld)
RTI_EXCEPT(DimensionNotDefined)
RTI_EXCEPT(EnableTimeConstrainedPending)
RTI_EXCEPT(EnableTimeConstrainedWasNotPending)
RTI_EXCEPT(EnableTimeRegulationPending)
RTI_EXCEPT(EnableTimeRegulationWasNotPending)  
RTI_EXCEPT(ErrorReadingFED)
RTI_EXCEPT(EventNotKnown)
RTI_EXCEPT(FederateAlreadyExecutionMember)
RTI_EXCEPT(FederateInternalError)
RTI_EXCEPT(FederateLoggingServiceCalls)
RTI_EXCEPT(FederateNotExecutionMember)
RTI_EXCEPT(FederateOwnsAttributes)
RTI_EXCEPT(FederateWasNotAskedToReleaseAttribute)  
RTI_EXCEPT(FederatesCurrentlyJoined)
RTI_EXCEPT(FederationExecutionAlreadyExists)
RTI_EXCEPT(FederationExecutionDoesNotExist)
RTI_EXCEPT(FederationTimeAlreadyPassed)
RTI_EXCEPT(HandleValuePairMaximumExceeded)
RTI_EXCEPT(InteractionClassNotDefined)
RTI_EXCEPT(InteractionClassNotKnown)
RTI_EXCEPT(InteractionClassNotPublished)
RTI_EXCEPT(InteractionClassNotSubscribed)  
RTI_EXCEPT(InteractionParameterNotDefined)
RTI_EXCEPT(InteractionParameterNotKnown)
RTI_EXCEPT(InvalidExtents)
RTI_EXCEPT(InvalidFederationTime)
RTI_EXCEPT(InvalidHandleValuePairSetContext)
RTI_EXCEPT(InvalidLookahead)
RTI_EXCEPT(InvalidOrderingHandle)
RTI_EXCEPT(InvalidRegionContext)  
RTI_EXCEPT(InvalidResignAction)
RTI_EXCEPT(InvalidRetractionHandle)
RTI_EXCEPT(InvalidTransportationHandle)
RTI_EXCEPT(MemoryExhausted)
RTI_EXCEPT(NameNotFound)
RTI_EXCEPT(ObjectClassNotDefined)
RTI_EXCEPT(ObjectClassNotKnown)
RTI_EXCEPT(ObjectClassNotPublished)
RTI_EXCEPT(ObjectClassNotSubscribed)
RTI_EXCEPT(ObjectNotKnown)
RTI_EXCEPT(ObjectAlreadyRegistered)
RTI_EXCEPT(OwnershipAcquisitionPending)
RTI_EXCEPT(RegionInUse)
RTI_EXCEPT(RegionNotKnown)
RTI_EXCEPT(RestoreInProgress)  
RTI_EXCEPT(RestoreNotRequested)
RTI_EXCEPT(RTIinternalError)
RTI_EXCEPT(SpaceNotDefined)
RTI_EXCEPT(SaveInProgress)
RTI_EXCEPT(SaveNotInitiated)
RTI_EXCEPT(SpecifiedSaveLabelDoesNotExist)
RTI_EXCEPT(SynchronizationPointLabelWasNotAnnounced)  
RTI_EXCEPT(TimeAdvanceAlreadyInProgress)
RTI_EXCEPT(TimeAdvanceWasNotInProgress)
RTI_EXCEPT(TimeConstrainedAlreadyEnabled)
RTI_EXCEPT(TimeConstrainedWasNotEnabled)
RTI_EXCEPT(TimeRegulationAlreadyEnabled)
RTI_EXCEPT(TimeRegulationWasNotEnabled)
RTI_EXCEPT(UnableToPerformSave)
RTI_EXCEPT(ValueCountExceeded)
RTI_EXCEPT(ValueLengthExceeded)
  
enum ResignAction {
  RELEASE_ATTRIBUTES = 1,
  DELETE_OBJECTS,
  DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES,
  NO_ACTION
};

class Region;

class FederateAmbassador;

typedef FederateAmbassador *FederateAmbassadorPtr;

typedef Long SpaceHandle;

typedef ULong ObjectClassHandle;

typedef ULong InteractionClassHandle;

typedef ULong ExtentIndex;

typedef ULong Handle;

typedef Handle AttributeHandle;

typedef Handle ParameterHandle;

typedef Handle ObjectHandle;

typedef Handle DimensionHandle;

typedef ULong FederateHandle;

typedef Handle TransportationHandle;

typedef TransportationHandle TransportType;

typedef Handle OrderingHandle;

typedef OrderingHandle OrderType;

typedef ULong FederateID;

typedef ULong UniqueID;

typedef Double TickTime;

typedef ULong RegionToken;

class RTI_EXPORT AttributeHandleValuePairSet {
// Instances of class HandleValuePairSet are the containers used to pass
// object attribute values and interaction parameter values between the
// Fedrate and the RTI.  These containers hold sets of attribute/parameter
// values indexed by their attribute/parameter handle.  Instances of this
// class are provided to the RTI in the Update Attribute Values and Send
// Interaction service invocations.  Instances of this class are provided
// to the Federate in the Reflect Attribute Values and Receive Interaction
// service invocations.  When instances of HandleValuePairSet are provided
// to the Federate by the RTI, the memory used to store attribute/parameter
// values is valid for use by the federate only within the scope of the
// Reflect Attribute Values or Receive Interaction service invocation.
// Symmetrically, for instances of HandleValuePairSet provided by the Federate
// to the RTI, the memory used to store attribute/parameter values is valid for
// use by the RTI only within the scope of the Update Attribute Values or Send
// Interaction service invocation.  
public:
  virtual ~AttributeHandleValuePairSet() { ; }

  virtual ULong size() const = 0;
  
  virtual Handle getHandle(
    ULong i) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual ULong getValueLength(
    ULong i) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual void getValue(
    ULong i,
    char*      buff,
    ULong&     valueLength) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual char *getValuePointer(
    ULong i,
    ULong&     valueLength) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual TransportType getTransportType( ULong i) const
    throw (
      ArrayIndexOutOfBounds,
      InvalidHandleValuePairSetContext) = 0;
  
  virtual OrderType getOrderType( ULong i) const
    throw (
      ArrayIndexOutOfBounds,
      InvalidHandleValuePairSetContext) = 0;
  
  virtual Region *getRegion(
    ULong i) const
    throw (
      ArrayIndexOutOfBounds,
      InvalidHandleValuePairSetContext) = 0;
  
  virtual void add(
    Handle      h,
    const char* buff,
    ULong       valueLength)
    throw (
      ValueLengthExceeded,
      ValueCountExceeded) = 0;
  
  virtual void remove(		// not guaranteed safe while iterating
    Handle      h)
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual void moveFrom(
    const AttributeHandleValuePairSet& ahvps,
    ULong&               i)
    throw (
      ValueCountExceeded,
      ArrayIndexOutOfBounds) = 0;
  
  virtual void empty() = 0; // Empty the Set without deallocating space.

  virtual ULong start() const = 0;
  virtual ULong valid(ULong i) const = 0;
  virtual ULong next(ULong i) const = 0;
};


class RTI_EXPORT AttributeSetFactory {
public:
  static AttributeHandleValuePairSet* create(
    ULong count)
    throw (
      MemoryExhausted,
      ValueCountExceeded,
      HandleValuePairMaximumExceeded);
};

class RTI_EXPORT AttributeHandleSet {
public:
  virtual ~AttributeHandleSet() { ; }

  virtual ULong size() const = 0;

  virtual AttributeHandle getHandle(ULong i) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual void add(AttributeHandle h)
    throw (
      ArrayIndexOutOfBounds,
      AttributeNotDefined) = 0;
  
  virtual void remove(AttributeHandle h)
    throw (			// not guaranteed safe while iterating
      AttributeNotDefined) = 0;
  
  virtual void empty() = 0; // Empty the Set

  virtual Boolean isEmpty() const = 0;  //is set empty?
  virtual Boolean isMember(AttributeHandle h) const = 0;
};


class RTI_EXPORT AttributeHandleSetFactory {
public:
  static AttributeHandleSet* create(
    ULong count)
    throw(
      MemoryExhausted,
      ValueCountExceeded);
};

class RTI_EXPORT FederateHandleSet {
public:
  virtual ULong size() const = 0;

  virtual FederateHandle getHandle(ULong i) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual void add(FederateHandle h)
    throw (
      ValueCountExceeded) = 0;
  
  virtual void remove(FederateHandle h)
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual void empty() = 0; // Empty the set without deallocating space.

  virtual Boolean isMember(FederateHandle h) const = 0;
};

class RTI_EXPORT FederateHandleSetFactory {
public:
  static FederateHandleSet* create(ULong count)
    throw (
      MemoryExhausted,
      ValueCountExceeded);
};


class RTI_EXPORT ParameterHandleValuePairSet {
// Instances of class HandleValuePairSet are the containers used to pass
// object attribute values and interaction parameter values between the
// Fedrate and the RTI.  These containers hold sets of attribute/parameter
// values indexed by their attribute/parameter handle.  Instances of this
// class are provided to the RTI in the Update Attribute Values and Send
// Interaction service invocations.  Instances of this class are provided
// to the Federate in the Reflect Attribute Values and Receive Interaction
// service invocations.  When instances of HandleValuePairSet are provided
// to the Federate by the RTI, the memory used to store attribute/parameter
// values is valid for use by the federate only within the scope of the
// Reflect Attribute Values or Receive Interaction service invocation.
// Symmetrically, for instances of HandleValuePairSet provided by the Federate
// to the RTI, the memory used to store attribute/parameter values is valid for
// use by the RTI only within the scope of the Update Attribute Values or Send
// Interaction service invocation.  
public:
  virtual ~ParameterHandleValuePairSet() { ; }

  virtual ULong size() const = 0;
  
  virtual Handle getHandle(
    ULong i) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual ULong getValueLength(
    ULong i) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual void getValue(
    ULong i,
    char*      buff,
    ULong&     valueLength) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual char *getValuePointer(
    ULong i,
    ULong&     valueLength) const
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual TransportType getTransportType(void) const
    throw ( InvalidHandleValuePairSetContext) = 0;
  
  virtual OrderType getOrderType(void) const
    throw ( InvalidHandleValuePairSetContext) = 0;
  
  virtual Region *getRegion(void) const
    throw ( InvalidHandleValuePairSetContext) = 0;
  
  virtual void add(
    Handle      h,
    const char* buff,
    ULong       valueLength)
    throw (
      ValueLengthExceeded,
      ValueCountExceeded) = 0;
  
  virtual void remove(		// not guaranteed safe while iterating
    Handle      h)
    throw (
      ArrayIndexOutOfBounds) = 0;
  
  virtual void moveFrom(
    const ParameterHandleValuePairSet& phvps,
    ULong&               i)
    throw (
      ValueCountExceeded,
      ArrayIndexOutOfBounds) = 0;
  
  virtual void empty() = 0; // Empty the Set without deallocating space.

  virtual ULong start() const = 0;
  virtual ULong valid(ULong i) const = 0;
  virtual ULong next(ULong i) const = 0;
};

class RTI_EXPORT ParameterSetFactory {
public:
  static ParameterHandleValuePairSet* create(ULong count)
    throw (
      MemoryExhausted,
      ValueCountExceeded,
      HandleValuePairMaximumExceeded);
};

class RTI_EXPORT Region {
public:

  virtual ~Region() { }
  
  virtual ULong getRangeLowerBound(
    ExtentIndex     theExtent,
    DimensionHandle theDimension) const
    throw (
      ArrayIndexOutOfBounds) = 0;

  virtual ULong getRangeUpperBound(
    ExtentIndex     theExtent,
    DimensionHandle theDimension) const
    throw (
      ArrayIndexOutOfBounds) = 0;

  virtual void setRangeLowerBound(
    ExtentIndex     theExtent,
    DimensionHandle theDimension,
    ULong           theLowerBound)
    throw (
      ArrayIndexOutOfBounds) = 0;

  virtual void setRangeUpperBound(
    ExtentIndex     theExtent,
    DimensionHandle theDimension,
    ULong           theUpperBound)
    throw (
      ArrayIndexOutOfBounds) = 0;

  virtual SpaceHandle getSpaceHandle() const
    throw (
      ) = 0;

  virtual ULong getNumberOfExtents() const
    throw (
      ) = 0;

  virtual ULong getRangeLowerBoundNotificationLimit(
    ExtentIndex     theExtent,
    DimensionHandle theDimension) const
    throw (
      ArrayIndexOutOfBounds) = 0;

  virtual ULong getRangeUpperBoundNotificationLimit(
    ExtentIndex     theExtent,
    DimensionHandle theDimension) const
    throw (
      ArrayIndexOutOfBounds) = 0;

};

class RTI_EXPORT_FEDTIME FedTime {
public:
  virtual ~FedTime();

  virtual void setZero() = 0;

  virtual Boolean isZero() = 0;
  
  virtual void setEpsilon() = 0;

  virtual void setPositiveInfinity() = 0;

  virtual Boolean isPositiveInfinity() = 0;
  
  virtual FedTime& operator+= (const FedTime&)
    throw (
      InvalidFederationTime) = 0;

  virtual FedTime& operator-= (const FedTime&)
    throw (
      InvalidFederationTime) = 0;
  
  virtual Boolean operator<= (const FedTime&) const
    throw (
      InvalidFederationTime) = 0;

  virtual Boolean operator< (const FedTime&) const
    throw (
      InvalidFederationTime) = 0;

   virtual Boolean operator>= (const FedTime&) const
    throw (
      InvalidFederationTime) = 0;
  
  virtual Boolean operator> (const FedTime&) const
    throw (
      InvalidFederationTime) = 0;

  virtual Boolean operator== (const FedTime&) const
    throw (
      InvalidFederationTime) = 0;
  
  virtual FedTime& operator= (const FedTime&)
    throw (
      InvalidFederationTime) = 0;

  //return bytes needed to encode
  virtual int encodedLength() const = 0;
  
  //encode into suppled buffer
  virtual void encode(char *buff) const = 0;
  
  virtual int getPrintableLength() const = 0;

  virtual void getPrintableString(char*) = 0;
  
};

class RTI_EXPORT_FEDTIME FedTimeFactory {
public:
  static FedTime* makeZero()
    throw (
      MemoryExhausted);

  static FedTime* decode(const char *buf)
    throw (
      MemoryExhausted);
};

struct EventRetractionHandle_s {
  UniqueID              theSerialNumber;
  FederateHandle        sendingFederate;
};
typedef struct EventRetractionHandle_s EventRetractionHandle;


#endif	/*RTITYPES_HH_INCLUDED*/
