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
package org.portico.lrc.services.saverestore.msg;

/**
 * This message type is exactly the same as its parent class {@link RestoreFederation}. The
 * reason it is a separate type is that it is so it can be handled by separate handlers. The
 * RestoreRequestStatus messages are broadcast out to a federation, but instances of this message
 * are generated and processed only within a local LRC to signal either the success of failure of
 * a particular request to trigger a federation restore. By having two separate classes, the
 * incoming sink can process them using two separate handlers.
 */
public class RestoreRequestResult extends RestoreFederation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RestoreRequestResult( String label )
	{
		super( label );
	}
	
	public RestoreRequestResult( String label, boolean successful, String failureReason )
	{
		super( label, successful, failureReason );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
