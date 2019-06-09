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
package org.portico2.rti.cli.command;

import org.portico.lrc.compat.JResignAction;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.portico2.common.services.federation.msg.ResignFederation;
import org.portico2.rti.RTI;
import org.portico2.rti.cli.RtiCli;
import org.portico2.rti.cli.fs.FSContext;
import org.portico2.rti.cli.fs.FSContextFactory;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.federation.Federation;

/**
 * Removes a federation or federate, depending on the context provided
 * <p/>
 * If a federate context is provided, then the RTI will resign the federate from the federation
 * <p/>
 * If a federation context is provided, then the RTI will attempt to destory the federation, which must
 * be empty.
 * <p/>
 * <b>Expected usage:</b> rm context
 */
public class RemoveContext implements ICommand
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
	@Override
	public void execute( RtiCli container, String... args )
	{
		RTI rti = container.getRti();
		FSContext context = container.getCurrentContext();
		if( args.length > 0 )
			context = FSContextFactory.fromPath( container, args[0] );
		
		if( !container.isValidContext(context) )
			throw new IllegalArgumentException( "Path does not exist: " + FSContext.getContextPath(context) );
			
		switch( context.getType() )
		{
			case Federation:
				removeFederation( container, context );
				break;
			case Federate:
				removeFederate( container, context );
				break;
			default:
				throw new IllegalArgumentException( FSContext.getContextPath(context) + " is read only" );
		}
	}

	private void removeFederation( RtiCli container, FSContext context )
	{
		RTI rti = container.getRti();
		Federation federation = rti.getFederationManager().getFederation( context.getName() );
		if( federation == null )
			throw new IllegalArgumentException( FSContext.getContextPath(context) + " is not a federation" );
		
		DestroyFederation destroy = new DestroyFederation( federation.getFederationName() );
		MessageContext msgContext = new MessageContext( destroy );
		rti.getInbox().receiveControlMessage( msgContext, null );
		
		if( msgContext.isErrorResponse() )
			throw msgContext.getErrorResponseException();
	}
	
	private void removeFederate( RtiCli container, FSContext context )
	{
		RTI rti = container.getRti();
		Federation federation = rti.getFederationManager().getFederation( context.getParent().getName() );
		if( federation == null )
			throw new IllegalArgumentException( FSContext.getContextPath(context.getParent()) + " is not a federation" );
		
		Federate federate = federation.getFederate( context.getName() );
		if( federate == null )
			throw new IllegalArgumentException( FSContext.getContextPath(context) + " is not a federate" );
		
		ResignFederation resign = new ResignFederation( JResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES );
		resign.setFederateName( federate.getFederateName() );
		resign.setFederateType( federate.getFederateType() );
		resign.setSourceFederate( federation.getFederationHandle() );
		resign.setFederationName( federation.getFederationName() );
		resign.setTargetFederation( federation.getFederationHandle() );
		
		MessageContext msgContext = new MessageContext( resign );
		rti.getInbox().receiveControlMessage( msgContext, null );
		
		if( msgContext.isErrorResponse() )
			throw msgContext.getErrorResponseException();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
