/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.rti.services.mom.data;

import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.rti.services.mom.incoming.MomSendInteractionHandler;

/**
 * Internal exception type. Indicates that an error occurred during the processing of a MOM request
 * interaction. This exception will be reported back to the federate as a HLAreportMOMexception via
 * the {@link MomSendInteractionHandler#reportMomException(SendInteraction, MomException)} method.
 */
public class MomException extends Exception
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean parameterError;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomException( String message, boolean parameterError )
	{
		super( message );
		this.parameterError = parameterError;
	}
	
	public MomException( String message, boolean parameterError, Throwable cause )
	{
		super( message, cause );
		this.parameterError = parameterError;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean isParameterError()
	{
		return this.parameterError;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}