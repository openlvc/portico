//File baseTypes13.h
//Included in RTI13.h

#ifndef BASETYPES13_H_INCLUDED
#define BASETYPES13_H_INCLUDED

#ifndef NULL
	#define NULL (0)
#endif

namespace rti13
{
	typedef unsigned short UShort;
	typedef short Short;

	#if defined(RTI_64_BIT_LONG)
		typedef unsigned int ULong;
		typedef int Long;
	#else
		typedef unsigned long ULong;
		typedef long Long;
	#endif

	typedef double Double;
	typedef float Float;

	enum Boolean 
	{
		RTI_FALSE = 0,
		RTI_TRUE
	};

	class RTI_EXPORT Exception 
	{
		public:
			ULong _serial;
			char* _reason;
			const char* _name;

			Exception (const char *reason);
			Exception (ULong serial, const char *reason=NULL);
			Exception (const Exception &toCopy);
			virtual ~Exception ();
			Exception & operator = (const Exception &);
			virtual Exception * cloneSelf() const throw() = 0;
			virtual void throwSelf() const = 0;
	};
	
	#define RTI_EXCEPT(A) \
	class A : public Exception { \
		public: \
			static RTI_EXPORT const char* _ex; \
			A (const char *reason) : Exception (reason) { _name = _ex; } \
			A (ULong serial, const char *reason=NULL) \
			: Exception (serial, reason) { _name = _ex; } \
			A (A const & toCopy) : Exception(toCopy) { _name = _ex; } \
			Exception * cloneSelf() const throw() { return (new A(_reason)); } \
			void throwSelf() const { throw *this; } \
	};

	
} // End of namespace rti13
#endif // BASETYPES13_H_INCLUDED
