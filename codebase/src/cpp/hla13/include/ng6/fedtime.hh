#ifndef FedTime_h
#define FedTime_h

#include <RTI.hh>
#include <sys/types.h>
#include <string.h>

class RTI_EXPORT_FEDTIME RTIfedTime : public RTI::FedTime {
//-----------------------------------------------------------------
// Constructors and Destructors
//-----------------------------------------------------------------
public:
  RTIfedTime();
  RTIfedTime(const RTI::Double&);
  RTIfedTime(const RTI::FedTime&);
  RTIfedTime(const RTIfedTime&);
  virtual ~RTIfedTime();

//-----------------------------------------------------------------
// Overloaded functions from RTI::FedTime
//-----------------------------------------------------------------
public:
  virtual void                setZero();
  virtual RTI::Boolean        isZero();
  virtual void                setEpsilon();
  virtual void                setPositiveInfinity();
  virtual RTI::Boolean        isPositiveInfinity();
  virtual int                 encodedLength() const;
  virtual void                encode(char *buff) const;
  virtual int                 getPrintableLength() const;
  virtual void                getPrintableString(char*);

//-----------------------------------------------------------------
// Overloaded operators from RTI::FedTime
//-----------------------------------------------------------------
public:
  virtual RTI::FedTime& operator+= (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator-= (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);
  
  virtual RTI::Boolean operator<= (const RTI::FedTime&) const
    throw (RTI::InvalidFederationTime);

  virtual RTI::Boolean operator< (const RTI::FedTime&) const
    throw (RTI::InvalidFederationTime);

   virtual RTI::Boolean operator>= (const RTI::FedTime&) const
    throw (RTI::InvalidFederationTime);
  
  virtual RTI::Boolean operator> (const RTI::FedTime&) const
    throw (RTI::InvalidFederationTime);

  virtual RTI::Boolean operator== (const RTI::FedTime&) const
    throw (RTI::InvalidFederationTime);
  
  virtual RTI::FedTime& operator= (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);

//-----------------------------------------------------------------
// Implementation functions
//-----------------------------------------------------------------
public:
  virtual RTI::Double         getTime() const;

//-----------------------------------------------------------------
// Implementation operators
//-----------------------------------------------------------------
  virtual RTI::Boolean operator== (const RTI::Double&) const
    throw (RTI::InvalidFederationTime);

  virtual RTI::Boolean operator!= (const RTI::FedTime&) const
    throw (RTI::InvalidFederationTime);

  virtual RTI::Boolean operator!= (const RTI::Double&) const
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator= (const RTIfedTime&)
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator= (const RTI::Double&)
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator*= (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator/= (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator+= (const RTI::Double&)
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator-= (const RTI::Double&)
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator*= (const RTI::Double&)
    throw (RTI::InvalidFederationTime);

  virtual RTI::FedTime& operator/= (const RTI::Double&)
    throw (RTI::InvalidFederationTime);

  virtual RTIfedTime operator+ (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);

  virtual RTIfedTime operator+ (const RTI::Double&)
    throw (RTI::InvalidFederationTime);

  virtual RTIfedTime operator- (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);

  virtual RTIfedTime operator- (const RTI::Double&)
    throw (RTI::InvalidFederationTime);

  virtual RTIfedTime operator* (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);

  virtual RTIfedTime operator* (const RTI::Double&)
    throw (RTI::InvalidFederationTime);

  virtual RTIfedTime operator/ (const RTI::FedTime&)
    throw (RTI::InvalidFederationTime);

  virtual RTIfedTime operator/ (const RTI::Double&)
    throw (RTI::InvalidFederationTime);
  
//-----------------------------------------------------------------
// Implementation friends
//-----------------------------------------------------------------
public:
  //
  // RTI_STD was added for the RTI 1.3NG to allow the use of the Standard C++ 
  // ostream or to use the legacy ostream.  The issue concerns whether ostream
  // is in the global namespace or in namespace std.
  //
  friend RTI_STD::ostream RTI_EXPORT & operator<< (RTI_STD::ostream&, const RTI::FedTime&);

//-----------------------------------------------------------------
// Implementation member variables
//-----------------------------------------------------------------
private:
  RTI::Double                 _fedTime;
  RTI::Double                 _zero;
  RTI::Double                 _epsilon;
  RTI::Double                 _positiveInfinity;
};

//-----------------------------------------------------------------
// Global operators
//-----------------------------------------------------------------

RTIfedTime operator+ (const RTI::Double&, const RTI::FedTime&);
RTIfedTime operator- (const RTI::Double&, const RTI::FedTime&);
RTIfedTime operator* (const RTI::Double&, const RTI::FedTime&);
RTIfedTime operator/ (const RTI::Double&, const RTI::FedTime&);

#endif
