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
package org.portico.impl.hla1516.handlers;

import java.util.Map;

import org.portico.impl.hla1516.Impl1516Helper;
import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;

/**
 * This handler generates IEEE1516 callbacks for synchronization point announcements.
 */
@MessageHandler(modules="lrc1516-callback",
                keywords="lrc1516",
                sinks="incoming",
                priority=3,
                messages=RegisterSyncPoint.class)
public class SyncAnnounceCallbackHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl1516Helper helper;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.helper = (Impl1516Helper)lrc.getSpecHelper();
	}
	
	public void process( MessageContext context ) throws Exception
	{
		RegisterSyncPoint request = context.getRequest( RegisterSyncPoint.class, this );
		
		// make sure we are in the announcement group
		if( request.getFederateSet() != null &&
			request.getFederateSet().isEmpty() == false &&
			request.getFederateSet().contains(lrcState.getFederateHandle()) == false )
		{
			// we're not in the set, ignore the request
			context.success();
			return;
		}
		
		logger.trace( "CALLBACK announceSynchronizationPoint(label="+request.getLabel()+")" );
		helper.getFederateAmbassador().announceSynchronizationPoint( request.getLabel(),
		                                                             request.getTag() );

		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
