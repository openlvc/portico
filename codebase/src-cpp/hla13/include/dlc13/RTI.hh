//File RTI.hh

// This file exists solely to provide compile-time
// backwards-compatibility for federates built before
// the existence of the Dynamic-Link-Compatible RTI API.
// New federates should be including RTI13.h rather than
// this file, RTI.hh.

#ifndef RTI_BACKCOMPAT_hh
#define RTI_BACKCOMPAT_hh

#include "RTI13.h"

// The following alias allows old code to continue to reference RTI
// classes by the old names (qualified by RTI:: rather than rti13::).
namespace RTI = rti13;

// Map old macros to new static member functions of Region class
#define MAX_EXTENT (RTI::Region::getMaxExtent())
#define MIN_EXTENT (RTI::Region::getMinExtent())

#endif // RTI_BACKCOMPAT_hh
