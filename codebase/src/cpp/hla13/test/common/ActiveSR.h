/*
 *   Copyright 2009 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
#ifndef ACTIVESR_H_
#define ACTIVESR_H_

#include "Common.h"

////////////////////////////////////////////////
// enum to reprepresent save/restore statuses //
////////////////////////////////////////////////
enum SRStatus{ UNINITIATED, INITIATED, BEGUN, COMPLETED_SUCCESS, COMPLETED_FAILURE };

class ActiveSR
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		std::string label;
		SRStatus status;
		int federateHandle; // used on restore requests

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		ActiveSR();
		virtual ~ActiveSR();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void reset();
		void initiate( const char* label );
		void initiate( const char* label, int federateHandle );
		void begun();
		void success();
		void failure();
		RTI::Boolean isInitiated( const char* expectedLabel );
		RTI::Boolean isBegun();
		RTI::Boolean isSuccess();
		RTI::Boolean isFailure();
		RTI::Boolean isComplete();
		int getFederateHandle();
	
	private:
		void setLabel( const char* newLabel );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

#endif /* ACTIVESR_H_ */
