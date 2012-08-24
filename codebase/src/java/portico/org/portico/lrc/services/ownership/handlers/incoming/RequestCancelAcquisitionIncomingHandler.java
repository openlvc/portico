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
package org.portico.lrc.services.ownership.handlers.incoming;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.CancelAcquire;
import org.portico.lrc.services.ownership.msg.CancelConfirmation;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7,
                messages=CancelAcquire.class)
public class RequestCancelAcquisitionIncomingHandler extends LRCMessageHandler
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		CancelAcquire request = context.getRequest( CancelAcquire.class, this );
		vetoIfMessageFromUs( request ); // we already know about it
		
		int federate = request.getSourceFederate();
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Cancel ownership acquisition of attributes "+
			              acMoniker(attributes)+" of object ["+objectMoniker(objectHandle)+
			              "] by federate ["+moniker(federate)+"]" );
		}
		
		// notify the ownership manager that the acquisition has been cancelled
		ownership.cancelAcquisition( objectHandle, attributes );
		
		// send back a cancellation confirmation for any attributes that were owned locally
		Set<Integer> owned = new HashSet<Integer>();
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance != null )
		{
			for( Integer attributeHandle : attributes )
			{
				ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
				if( attributeInstance.isOwnedBy(federateHandle()) )
					owned.add( attributeHandle );
			}
		}
		
		if( owned.isEmpty() == false )
		{
			if( logger.isDebugEnabled() )
			{
				logger.debug( "Broadcast confirmation of CANCELLED ownership-acquisition attempt for "+
				              "locally-owned attributes "+acMoniker(owned)+" of object ["+
				              objectMoniker(objectHandle)+"] by ["+moniker(federate)+"]" );
			}

			CancelConfirmation confirmation = new CancelConfirmation( objectHandle, owned );
			fill( confirmation, federate ); // fill with our details and target federate
			connection.broadcast( confirmation );
		}

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
