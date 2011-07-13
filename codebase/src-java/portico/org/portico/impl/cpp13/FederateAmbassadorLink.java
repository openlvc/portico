/*
 *   Copyright 2008 The Portico Project
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
package org.portico.impl.cpp13;

public class FederateAmbassadorLink
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
	public FederateAmbassadorLink()
	{
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public native void announceSynchronizationPoint( int id,
	                                                 String synchronizationPointLabel,
	                                                 byte[] userSuppliedTag );

	public native void attributeIsNotOwned( int id, int theObject, int theAttribute );

	public native void attributeOwnedByRTI( int id, int theObject, int theAttribute );

	public native void attributeOwnershipAcquisitionNotification( int id, int theObject, int[] secured );

	public native void attributeOwnershipDivestitureNotification( int id, int theObject, int[] released );

	public native void attributeOwnershipUnavailable( int id, int theObject, int[] theAttributes );

	public native void attributesInScope( int id, int theObject, int[] theAttributes );

	public native void attributesOutOfScope( int id, int theObject, int[] attributes );

	public native void confirmAttributeOwnershipAcquisitionCancellation( int id,
	                                                                     int theObject,
	                                                                     int[] theAttributes );

	public native void discoverObjectInstance( int id, int theObject, int theClass, String objName );

	public native void federationNotRestored( int id );

	public native void federationNotSaved( int id );

	public native void federationRestoreBegun( int id );

	public native void federationRestored( int id );

	public native void federationSaved( int id );

	public native void federationSynchronized( int id, String synchronizationPointLabel );

	public native void informAttributeOwnership( int id,
	                                             int theObject,
	                                             int theAttribute,
	                                             int theOwner );

	public native void initiateFederateRestore( int id,
	                                            String label,
	                                            int federateHandle );

	public native void initiateFederateSave( int id, String label );

	public native void provideAttributeValueUpdate( int id, int theObject, int[] handles );

	public native void receiveInteraction( int id,
	                                       int interactionClass,
	                                       int[] handles,
	                                       byte[][] values,
	                                       byte[] userSuppliedTag );

	public native void receiveInteraction( int id,
	                                       int interactionClass,
	                                       int[] handles,
	                                       byte[][] values,
	                                       byte[] userSuppliedTag,
	                                       double theTime,
	                                       int eventRetractionHandle );

	public native void reflectAttributeValues( int id,
	                                           int theObject,
	                                           int[] handles,
	                                           byte[][] values,
	                                           byte[] userSuppliedTag );

	public native void reflectAttributeValues( int id,
	                                           int theObject,
	                                           int[] handles,
	                                           byte[][] values,
	                                           byte[] userSuppliedTag,
	                                           double theTime,
	                                           int retractionHandle );

	public native void removeObjectInstance( int id, int theObject, byte[] userSuppliedTag );

	public native void removeObjectInstance( int id, 
	                                         int theObject,
	                                         byte[] userSuppliedTag,
	                                         double theTime,
	                                         int retractionHandle );

	public native void requestAttributeOwnershipAssumption( int id, 
	                                                        int theObject,
	                                                        int[] theAttributes,
	                                                        byte[] tag );

	public native void requestAttributeOwnershipRelease( int id, 
	                                                     int theObject,
	                                                     int[] theAttributes,
	                                                     byte[] tag );

	public native void requestFederationRestoreFailed( int id, String label, String reason );

	public native void requestFederationRestoreSucceeded( int id, String label );

	public native void requestRetraction( int id, int retractionHandle );

	public native void startRegistrationForObjectClass( int id, int theClass );

	public native void stopRegistrationForObjectClass( int id, int theClass );

	public native void synchronizationPointRegistrationFailed( int id, String label );

	public native void synchronizationPointRegistrationSucceeded( int id, String label );

	public native void timeAdvanceGrant( int id, double theTime );

	public native void timeConstrainedEnabled( int id, double theTime );

	public native void timeRegulationEnabled( int id, double theTime );

	public native void turnInteractionsOff( int id, int theHandle );

	public native void turnInteractionsOn( int id, int theHandle );

	public native void turnUpdatesOffForObjectInstance( int id,  int theObject, int[] attributes );

	public native void turnUpdatesOnForObjectInstance( int id, int theObject, int[] attributes );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
