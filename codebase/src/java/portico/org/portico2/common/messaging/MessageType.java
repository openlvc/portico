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
	Connect                 ( (short)0 ),
	Disconnect              ( (short)1 ),
	RtiProbe                ( (short)2 ),
	CreateFederation        ( (short)3 ),
	JoinFederation          ( (short)4 ),
	ResignFederation        ( (short)5 ),
	DestroyFederation       ( (short)6 ),
	ListFederations         ( (short)7 ),
	
	// Non-HLA Messages
	SuccessResponse         ( (short)100 ),
	ErrorResponse           ( (short)101 ),
	
	// Synchronization Points
	RegisterSyncPoint       ( (short)110 ),  // Register
	RegisterSyncPointResult ( (short)111 ),  // Deprecated
	AnnounceSyncPoint       ( (short)112 ),  // Callback
	AchieveSyncPoint        ( (short)113 ),  // Send
	FederationSynchronized  ( (short)114 ),  // Callback
	
	// Save Restore
	SaveRequest             ( (short)120 ),  // Start things off
	SaveInitiate            ( (short)121 ),  // Callback -- everyone should start saving
	SaveBegun               ( (short)122 ),  // Federate has begun its save
	SaveComplete            ( (short)123 ),  // Federate has completed its safe OK (true/false)
	FederationSaved         ( (short)124 ),  // Callback -- everyone saved (true/false)
	SaveStatusQuery         ( (short)125 ),  // Query the save status of all federates
	SaveQueryResponse       ( (short)126 ),  // Callback -- save status of everyone

	RestoreRequest          ( (short)130 ),  // RestoreRequest
	RestoreRequestResult    ( (short)131 ),  // Callback (true/false)
	RestoreBegun            ( (short)132 ),  // Callback -- restore process is starting
	RestoreInitiate         ( (short)133 ),  // Callback -- restore yourself with given params
	RestoreComplete         ( (short)134 ),  // Restore Completed / RestoreNotComplete calls
	RestoreAbort            ( (short)135 ),  // Abort the restore process
	FederationRestored      ( (short)136 ),  // Callback -- everyone restored (true/false)
	RestoreStatusQuery      ( (short)137 ),  // Query the restore status of all federates
	RestoreQueryResponse    ( (short)138 ),  // Callback -- restore status of everyone
	
	// Publish and Subscribe
	PublishObjectClass      ( (short)140 ),
	PublishInteraction      ( (short)141 ),
	UnpublishObjectClass    ( (short)142 ),
	UnpublishInteraction    ( (short)143 ),
	SubscribeObjectClass    ( (short)144 ),
	SubscribeInteraction    ( (short)145 ),
	UnsubscribeObjectClass  ( (short)146 ),
	UnsubscribeInteraction  ( (short)147 ),
	
	// Object Management
	RegisterObject          ( (short)160 ),
	ReserveObjectName       ( (short)161 ),
	ReserveObjectNameResult ( (short)162 ),
	DiscoverObject          ( (short)163 ),
	UpdateAttributes        ( (short)164 ),
	SendInteraction         ( (short)165 ),
	DeleteObject            ( (short)166 ),
	LocalDeleteObject       ( (short)167 ),
	RequestObjectUpdate     ( (short)168 ),
	RequestClassUpdate      ( (short)169 ),

	// Ownership Management
	AttributeDivest         ( (short)180 ),
	AttributeAcquire        ( (short)181 ),
	AttributeRelease        ( (short)182 ),
	AttributesUnavailable   ( (short)183 ),
	CancelAcquire           ( (short)184 ),
	CancelDivest            ( (short)185 ),
	CancelConfirmation      ( (short)186 ),
	OwnershipAcquired       ( (short)187 ),
	DivestConfirmation      ( (short)188 ),
	QueryOwnership          ( (short)189 ),
	QueryOwnershipResponse  ( (short)190 ),
	
	// Time Management
	EnableTimeConstrained   ( (short)200 ),
	DisableTimeConstrained  ( (short)201 ),
	EnableTimeRegulation    ( (short)202 ),
	DisableTimeRegulation   ( (short)203 ),
	ModifyLookahead         ( (short)204 ),
	TimeAdvanceRequest      ( (short)205 ),
	TimeAdvanceGrant        ( (short)206 ),
	NextEventRequest        ( (short)207 ),
	FlushQueueRequest       ( (short)208 ),
	QueryGALT               ( (short)209 ),
	EnableAsynchDelivery    ( (short)210 ),
	DisableAsynchDelivery   ( (short)211 ),

	// Data Distribution Management
	CreateRegion            ( (short)230 ),
	DeleteRegion            ( (short)231 ),
	ModifyRegion            ( (short)232 ),
	AssociateRegion         ( (short)233 ),
	UnassociateRegion       ( (short)234 );

	// Messages that are for processing within a specific federation
	// ...
	// ...
	

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private short id;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private MessageType( short id )
	{
		this.id = id;
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

	public short getId()
	{
		return this.id;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
