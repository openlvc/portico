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
 * Throughout a federation, a number of interesting events can occur. This interface describes
 * those events that may be of interest to particular components (be they core components or
 * extensions to the Portico framework).
 * <p/>
 * Components interested in hearing about various events should implement this interface and either
 * register themselves explicitly with a {@link NotificationManager} or through configuration via
 * the {@link NotificationListener} annotation.
 * <p/>
 * <b>IMPLEMENTATION NOTES:</b> If you are using the {@link NotificationListener} annotation so
 * that Portico can automatically locate and load your listener, you <b>*MUST*</b> include a
 * no-arg constructor in your listeners. If you do not, the listener will not be loaded.
 */
public interface INotificationListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * This notification is called when the local federate joins a federation.
	 * 
	 * @param federateHandle The handle of the local federate
	 * @param federateName The name of the local federate
	 * @param federateType The type of the local federate
	 * @param federationName The name of the federation being joined to
	 * @param fom The federation object model of the federation.
	 */
	public void localFederateJoinedFederation( int federateHandle,
	                                           String federateName,
	                                           String federateType,
	                                           String federationName,
	                                           ObjectModel fom );

	/**
	 * This notification is called when a remote federate joins the federation that the current
	 * federate is joined to. Information about the new federate (handle, name, time status, etc...)
	 * is stored inside the {@link RoleCall} parameter.
	 */
	public void remoteFederateJoinedFederation( RoleCall federateStatus );

	/**
	 * This notificatoin is called when the local federate resigns from a federation.
	 */
	public void localFederateResignedFromFederation();
	
	/**
	 * This notification is called when a remote federate resigns from the federation. The handle
	 * of the federate that resigned it passed so that it can be identified.
	 */
	public void remoteFederateResignedFromFederation( int federateHandle, String federateName );
}
