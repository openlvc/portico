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
package org.portico.lrc.notifications;

import org.portico.lrc.model.ObjectModel;
import org.portico2.common.services.federation.msg.RoleCall;

/**
 * Implements all the {@link INotificationListener} methods with an empty body. Extend this class
 * and override the methods you're interested in.
 */
public class NullNotificationListener implements INotificationListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void localFederateJoinedFederation( int federateHandle,
	                                           String federateName,
	                                           String federateType,
	                                           String federationName,
	                                           ObjectModel fom )
	{
		
	}
	
	public void remoteFederateJoinedFederation( RoleCall federateStatus )
	{
		
	}
	
	public void localFederateResignedFromFederation()
	{
		
	}

	public void remoteFederateResignedFromFederation( int federateHandle, String federateName )
	{
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
