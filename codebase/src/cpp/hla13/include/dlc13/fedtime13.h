//
// This header is used for compatibility with 1.3NG
//
#ifndef FedTime_h
#define FedTime_h

#include "RTI13.h"
#include <sys/types.h>

class RTI_EXPORT_FEDTIME RTIfedTime : public rti13::FedTime
{
	//-----------------------------------------------------------------
	// Constructors and Destructors
	//-----------------------------------------------------------------
	public:
		RTIfedTime();
		RTIfedTime(const rti13::Double&);
		RTIfedTime(const rti13::FedTime&);
		RTIfedTime(const RTIfedTime&);
		virtual ~RTIfedTime();
	
	//-----------------------------------------------------------------
	// Overloaded functions from rti13::FedTime
	//-----------------------------------------------------------------
	public:
		virtual void                setZero();
		virtual rti13::Boolean      isZero();
		virtual void                setEpsilon();
		virtual void                setPositiveInfinity();
		virtual rti13::Boolean      isPositiveInfinity();
		virtual int                 encodedLength() const;
		virtual void                encode(char *buff) const;
		virtual int                 getPrintableLength() const;
		virtual void                getPrintableString(char*) const;
	
	//-----------------------------------------------------------------
	// Overloaded operators from rti13::FedTime
	//-----------------------------------------------------------------
	public:
		virtual rti13::FedTime& operator+= (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator-= (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::Boolean operator<= (const rti13::FedTime&) const
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::Boolean operator< (const rti13::FedTime&) const
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::Boolean operator>= (const rti13::FedTime&) const
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::Boolean operator> (const rti13::FedTime&) const
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::Boolean operator== (const rti13::FedTime&) const
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator= (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
	
	//-----------------------------------------------------------------
	// Implementation functions
	//-----------------------------------------------------------------
	public:
		virtual rti13::Double         getTime() const;
	
	//-----------------------------------------------------------------
	// Implementation operators
	//-----------------------------------------------------------------
		virtual rti13::Boolean operator== (const rti13::Double&) const
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::Boolean operator!= (const rti13::FedTime&) const
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::Boolean operator!= (const rti13::Double&) const
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator= (const RTIfedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator= (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator*= (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator/= (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator+= (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator-= (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator*= (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
		
		virtual rti13::FedTime& operator/= (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
		
		virtual RTIfedTime operator+ (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual RTIfedTime operator+ (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
		
		virtual RTIfedTime operator- (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual RTIfedTime operator- (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
		
		virtual RTIfedTime operator* (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual RTIfedTime operator* (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
		
		virtual RTIfedTime operator/ (const rti13::FedTime&)
			throw (rti13::InvalidFederationTime);
		
		virtual RTIfedTime operator/ (const rti13::Double&)
			throw (rti13::InvalidFederationTime);
	  
	//-----------------------------------------------------------------
	// Implementation friends
	//-----------------------------------------------------------------
	public:
		//
		// RTI_STD was added for the RTI 1.3NG to allow the use of the Standard C++ 
		// ostream or to use the legacy ostream.  The issue concerns whether ostream
		// is in the global namespace or in namespace std.
		//
		friend RTI_STD::ostream RTI_EXPORT & operator<< (RTI_STD::ostream&, const rti13::FedTime&);
	
	//-----------------------------------------------------------------
	// Implementation member variables
	//-----------------------------------------------------------------
	private:
		rti13::Double                 _fedTime;
		rti13::Double                 _zero;
		rti13::Double                 _epsilon;
		rti13::Double                 _positiveInfinity;
};

//-----------------------------------------------------------------
// Global operators
//-----------------------------------------------------------------

RTIfedTime operator+ (const rti13::Double&, const rti13::FedTime&);
RTIfedTime operator- (const rti13::Double&, const rti13::FedTime&);
RTIfedTime operator* (const rti13::Double&, const rti13::FedTime&);
RTIfedTime operator/ (const rti13::Double&, const rti13::FedTime&);

#endif


