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
package org.portico2.common.messaging;

public enum MessageType
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	
	// Federation Management	
	Disconnect              ( (short)-1, null ),
	Connect                 ( (short)0, null ),
	CreateFederation        ( (short)1, null ),
	JoinFederation          ( (short)2, null ),
	ResignFederation        ( (short)3, null ),
	DestroyFederation       ( (short)4, null ),
	ListFederations         ( (short)5, null ),
	RtiProbe                ( (short)6, null ),
	
	// Synchronization Points
	RegisterSyncPoint       ( (short)110, null ),  // Register
	RegisterSyncPointResult ( (short)111, null ),  // Deprecated
	AnnounceSyncPoint       ( (short)112, null ),  // Callback
	AchieveSyncPoint        ( (short)113, null ),  // Send
	FederationSynchronized  ( (short)114, null ),  // Callback
	
	// Save Restore
	SaveRequest             ( (short)120, null ),  // Start things off
	SaveInitiate            ( (short)121, null ),  // Callback -- everyone should start saving
	SaveBegun               ( (short)122, null ),  // Federate has begun its save
	SaveComplete            ( (short)123, null ),  // Federate has completed its safe OK (true/false)
	FederationSaved         ( (short)124, null ),  // Callback -- everyone saved (true/false)
	SaveStatusQuery         ( (short)125, null ),  // Query the save status of all federates
	SaveQueryResponse       ( (short)126, null ),  // Callback -- save status of everyone

	RestoreRequest          ( (short)130, null ),  // RestoreRequest
	RestoreRequestResult    ( (short)131, null ),  // Callback (true/false)
	RestoreBegun            ( (short)132, null ),  // Callback -- restore process is starting
	RestoreInitiate         ( (short)133, null ),  // Callback -- restore yourself with given params
	RestoreComplete         ( (short)134, null ),  // Restore Completed / RestoreNotComplete calls
	RestoreAbort            ( (short)135, null ),  // Abort the restore process
	FederationRestored      ( (short)136, null ),  // Callback -- everyone restored (true/false)
	RestoreStatusQuery      ( (short)137, null ),  // Query the restore status of all federates
	RestoreQueryResponse    ( (short)138, null ),  // Callback -- restore status of everyone
	
	// Publish and Subscribe
	PublishObjectClass      ( (short)140, null ),
	PublishInteraction      ( (short)141, null ),
	UnpublishObjectClass    ( (short)142, null ),
	UnpublishInteraction    ( (short)143, null ),
	SubscribeObjectClass    ( (short)144, null ),
	SubscribeInteraction    ( (short)145, null ),
	UnsubscribeObjectClass  ( (short)146, null ),
	UnsubscribeInteraction  ( (short)147, null ),
	
	// Object Management
	RegisterObject          ( (short)160, null ),
	ReserveObjectName       ( (short)161, null ),
	ReserveObjectNameResult ( (short)162, null ),
	DiscoverObject          ( (short)163, null ),
	UpdateAttributes        ( (short)164, null ),
	SendInteraction         ( (short)165, null ),
	DeleteObject            ( (short)166, null ),
	LocalDeleteObject       ( (short)167, null ),
	RequestObjectUpdate     ( (short)168, null ),
	RequestClassUpdate      ( (short)169, null ),

	// Ownership Management
	AttributeDivest         ( (short)180, null ),
	AttributeAcquire        ( (short)181, null ),
	AttributeRelease        ( (short)182, null ),
	AttributesUnavailable   ( (short)183, null ),
	CancelAcquire           ( (short)184, null ),
	CancelDivest            ( (short)185, null ),
	CancelConfirmation      ( (short)186, null ),
	OwnershipAcquired       ( (short)187, null ),
	DivestConfirmation      ( (short)188, null ),
	QueryOwnership          ( (short)189, null ),
	QueryOwnershipResponse  ( (short)190, null ),
	
	// Time Management
	EnableTimeConstrained   ( (short)200, null ),
	DisableTimeConstrained  ( (short)201, null ),
	EnableTimeRegulation    ( (short)202, null ),
	DisableTimeRegulation   ( (short)203, null ),
	ModifyLookahead         ( (short)204, null ),
	TimeAdvanceRequest      ( (short)205, null ),
	TimeAdvanceGrant        ( (short)206, null ),
	NextEventRequest        ( (short)207, null ),
	FlushQueueRequest       ( (short)208, null ),
	QueryGALT               ( (short)209, null ),
	EnableAsynchDelivery    ( (short)210, null ),
	DisableAsynchDelivery   ( (short)211, null ),

	// Data Distribution Management
	CreateRegion            ( (short)230, null ),
	DeleteRegion            ( (short)231, null ),
	ModifyRegion            ( (short)232, null ),
	AssociateRegion         ( (short)233, null ),
	UnassociateRegion       ( (short)234, null );

	// Messages that are for processing within a specific federation
	// ...
	// ...
	

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private short id;
	private Class<?> implementingClass;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private MessageType( short id, Class<?> implementingClass )
	{
		this.id = id;
		this.implementingClass = implementingClass;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * @return True if this message is meant for handling entirely within a specific federation.
	 *         False if it is handled "above" a federation. Messages like "create", "destroy",
	 *         "list federations" require processing outside the bubble of a Federation, and thus
	 *         are handled by the RTI itself. All other messages are forwarded to a specific
	 *         federation for internal handling.
	 */
	public boolean isFederationMessage()
	{
		return id > 99;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
