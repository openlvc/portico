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
	
	// Non-Federation Messages (000-009)
	Connect                 ( (short)0 ),
	Disconnect              ( (short)1 ),
	RtiProbe                ( (short)2 ),
	CreateFederation        ( (short)3 ),
	JoinFederation          ( (short)4 ),
	ResignFederation        ( (short)5 ),
	DestroyFederation       ( (short)6 ),
	ListFederations         ( (short)7 ),
	Authenticate            ( (short)8 ),   // Used by Authentication protocol

	// Non-HLA Messages (010-039)
	SuccessResponse         ( (short)14 ),
	ErrorResponse           ( (short)15 ),
	
	// Synchronization Points (040-049)
	RegisterSyncPoint       ( (short)40 ),  // Register
	RegisterSyncPointResult ( (short)41 ),  // Deprecated
	AnnounceSyncPoint       ( (short)42 ),  // Callback
	AchieveSyncPoint        ( (short)43 ),  // Send
	FederationSynchronized  ( (short)44 ),  // Callback
	
	// Publish and Subscribe (050-059)
	PublishObjectClass      ( (short)50 ),
	PublishInteraction      ( (short)51 ),
	UnpublishObjectClass    ( (short)52 ),
	UnpublishInteraction    ( (short)53 ),
	SubscribeObjectClass    ( (short)54 ),
	SubscribeInteraction    ( (short)55 ),
	UnsubscribeObjectClass  ( (short)56 ),
	UnsubscribeInteraction  ( (short)57 ),
	
	// Object Management (060-079)
	RegisterObject          ( (short)60 ),
	DiscoverObject          ( (short)61 ),
	UpdateAttributes        ( (short)254, true ), // 254
	SendInteraction         ( (short)255, true ), // 255
	DeleteObject            ( (short)64 ),
	LocalDeleteObject       ( (short)65 ),
	ReserveObjectName       ( (short)66 ),
	ReserveObjectNameResult ( (short)67 ),
	RequestObjectUpdate     ( (short)68 ),
	RequestClassUpdate      ( (short)69 ),

	// Save Restore (080-099)
	SaveRequest             ( (short)86 ),  // Start things off
	SaveInitiate            ( (short)81 ),  // Callback -- everyone should start saving
	SaveBegun               ( (short)82 ),  // Federate has begun its save
	SaveComplete            ( (short)83 ),  // Federate has completed its safe OK (true/false)
	FederationSaved         ( (short)84 ),  // Callback -- everyone saved (true/false)
	SaveStatusQuery         ( (short)85 ),  // Query the save status of all federates
	SaveQueryResponse       ( (short)86 ),  // Callback -- save status of everyone

	RestoreRequest          ( (short)90 ),  // RestoreRequest
	RestoreRequestResult    ( (short)91 ),  // Callback (true/false)
	RestoreBegun            ( (short)92 ),  // Callback -- restore process is starting
	RestoreInitiate         ( (short)93 ),  // Callback -- restore yourself with given params
	RestoreComplete         ( (short)94 ),  // Restore Completed / RestoreNotComplete calls
	RestoreAbort            ( (short)95 ),  // Abort the restore process
	FederationRestored      ( (short)96 ),  // Callback -- everyone restored (true/false)
	RestoreStatusQuery      ( (short)97 ),  // Query the restore status of all federates
	RestoreQueryResponse    ( (short)98 ),  // Callback -- restore status of everyone
	
	// Ownership Management (100-119)
	AttributeDivest         ( (short)100 ),
	AttributeAcquire        ( (short)101 ),
	AttributeRelease        ( (short)102 ),
	AttributesUnavailable   ( (short)103 ),
	CancelAcquire           ( (short)104 ),
	CancelDivest            ( (short)105 ),
	CancelConfirmation      ( (short)106 ),
	OwnershipAcquired       ( (short)107 ),
	DivestConfirmation      ( (short)108 ),
	QueryOwnership          ( (short)109 ),
	QueryOwnershipResponse  ( (short)110 ),
	
	// Time Management (120-139)
	EnableTimeConstrained   ( (short)120 ),
	DisableTimeConstrained  ( (short)121 ),
	EnableTimeRegulation    ( (short)122 ),
	DisableTimeRegulation   ( (short)123 ),
	ModifyLookahead         ( (short)124 ),
	TimeAdvanceRequest      ( (short)125 ),
	TimeAdvanceGrant        ( (short)126 ),
	NextEventRequest        ( (short)127 ),
	FlushQueueRequest       ( (short)128 ),
	QueryGALT               ( (short)129 ),
	EnableAsynchDelivery    ( (short)130 ),
	DisableAsynchDelivery   ( (short)131 ),

	// Data Distribution Management (140-149)
	CreateRegion            ( (short)140 ),
	DeleteRegion            ( (short)141 ),
	ModifyRegion            ( (short)142 ),
	AssociateRegion         ( (short)143 ),
	UnassociateRegion       ( (short)144 ),

	
	// Management Object Model (150-159)
	SetServiceReporting     ( (short)150 ),
	SetExceptionReporting   ( (short)151 );
	
	// Reserved for future use (160-253)
	
	// RESERVED (UPDATE/SEND - 254, 255 - Above)
	
	// Messages that are for processing within a specific federation
	// ...
	// ...
	

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private short id;
	private boolean isDataMessage;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private MessageType( short id )
	{
		this.id = id;
	}
	
	private MessageType( short id, boolean isDataMessage )
	{
		this( id );
		this.isDataMessage = isDataMessage;
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
		return id > 10;
	}

	public boolean isDataMessage()
	{
		return this.isDataMessage;
	}
	
	public final short getId()
	{
		return this.id;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static MessageType fromId( int id )
	{
		// if it is a priority message, just jump straight to it
		switch( id )
		{
			case 254: return UpdateAttributes;
			case 255: return SendInteraction;
			case  60: return RegisterObject;
			case  61: return DiscoverObject;
			case  64: return DeleteObject;
			default: break;
		}

		// loop through all the other types
		for( MessageType type : MessageType.values() )
		{
			if( type.id == id )
				return type;
		}

		throw new IllegalArgumentException( "MessageType id not known: "+id );
	}

}
