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
package org.portico2.rti;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JFederateNameAlreadyInUse;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JFederatesCurrentlyJoined;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.PorticoConstants;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.ResignFederation;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.federation.FederationManager;

public class RtiInbox
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private RTI rti;
	private FederationManager federationManager;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected RtiInbox( RTI rti )
	{
		this.rti = rti;
		this.federationManager = rti.getFederationManager();
		this.logger = rti.getLogger();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// message receiving methods //////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public void receiveControlMessage( MessageContext context,
	                                   RtiConnection connection ) throws JRTIinternalError
	{
		PorticoMessage request = context.getRequest();
		
		// if the message is a "federation internal" one, find the federation and route it there
		if( request.getType().isFederationMessage() )
		{
			////////////////////////////////////////////////////////////
			// Federation Message - Find federation and forward to it //
			////////////////////////////////////////////////////////////
			try
			{
				int federationHandle = request.getTargetFederation();
				Federation targetFederation = federationManager.getFederation( federationHandle );
				
				if( targetFederation != null )
				{
					// Requesting federate must be a member of the target federation to submit federation 
					// messages to it
					int sourceFederate = request.getSourceFederate();
					if( sourceFederate == PorticoConstants.RTI_HANDLE || 
						targetFederation.containsFederate(sourceFederate) )
					{
						targetFederation.getIncomingSink().process( context );
					}
					else
					{
						String message = String.format( "Federate [%d] is not a member of federation [%s]", 
						                                sourceFederate,
						                                targetFederation.getFederationName() );
						context.error( new JFederateNotExecutionMember(message) );
					}
				}
				else
				{
					context.error( new JRTIinternalError("No federation with handle: "+federationHandle) );
				}
			}
			catch( Exception e )
			{
				context.error( e );
			}
		}
		else
		{
			///////////////////////////////////////////////////
			// Non-Federation Message - Process at RTI level //
			///////////////////////////////////////////////////
			try
			{
    			switch( request.getType() )
    			{
    				case Connect:
    					connect( context, connection );
    					break;
    				case Disconnect:
    					context.success();
    				case CreateFederation:
    					createFederation( context );
    					break;
    				case DestroyFederation:
    					destroyFederation( context );
    					break;
    				case JoinFederation:
    					joinFederation( context, connection );
    					break;
    				case ResignFederation:
    					resignFederation( context ); // pass through to federation
    					break;
    				case ListFederations:
    					throw new JRTIinternalError( "Message not supported yet: "+request.getType() );
    				case RtiProbe:
    					rtiProbe( context );
    					break;
    				default:
    					throw new JRTIinternalError( "Unknown control message type: "+request.getType() );
    			}
			}
			catch( Exception e )
			{
				// store the exception as an error result in the message context
				context.error( e );
			}
		}
	}

	/**
	 * This method will first find the {@link Federation} representing the target of the data
	 * messages and it will then forward the message to all the {@link IConnection}s associated
	 * with that federation (with the exception of the source - no loopback will happen).
	 * 
	 * @param message The message that was received
	 * @param sender  The connection from which the message was received
	 * @throws JRTIinternalError If the target federation is not known
	 */
	public void receiveDataMessage( PorticoMessage message, RtiConnection sender )
		throws JRTIinternalError
	{
		// find the federation this message is for
		Federation targetFederation = federationManager.getFederation( message.getTargetFederation() );
		if( targetFederation == null )
		{
			throw new JRTIinternalError( "No federation with handle [%d] (msg=%s)",
			                             message.getTargetFederation(),
			                             message.getClass().getSimpleName() );
		}
		
		// Requesting federate must be a member of the target federation to submit federation 
		// messages to it
		int sourceFederate = message.getSourceFederate();
		if( sourceFederate == PorticoConstants.RTI_HANDLE || 
			targetFederation.containsFederate(message.getSourceFederate()) )
		{
			targetFederation.queueDataMessage( message, sender );
		}
		else
		{
			String exMessage = String.format( "Federate [%d] is not a member of federation [%s]", 
			                                  sourceFederate,
			                                  targetFederation.getFederationName() );
			throw new JFederateNotExecutionMember( exMessage );
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Federation Management Methods   //////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	private void rtiProbe( MessageContext context )
	{
		logger.debug( "RTI Probe has been received; responding..." );
		context.success( "I am Spartacus!" );
	}
	
	private synchronized void connect( MessageContext context, RtiConnection connection )
	{
		logger.info( "Application has connected via network %s (type:%s)",
		             connection.getName(),
		             connection.getTransportType() );

		context.success();
	}

	private synchronized void createFederation( MessageContext context )
	{
		CreateFederation request = context.getRequest( CreateFederation.class );
		String name = request.getFederationName();
		ObjectModel fom = request.getModel();
		HLAVersion hlaVersion = request.getHlaVersion();
		
		// Check to see if the name is taken
		if( federationManager.containsFederation(name) )
		{
			context.error( new JFederationExecutionAlreadyExists("name="+name) );
			return;
		}

		// Create the federation object and store it
		logger.info( "ATTEMPT Creating federation name="+name );
		
		Federation federation = federationManager.createFederation( rti, name, fom, hlaVersion );
		federation.addRawFomModules( request.getRawFomModules() );
		
		logger.info( "SUCCESS Created federation name="+name );
		context.success( CreateFederation.KEY_FEDERATION_HANDLE, federation.getFederationHandle() );
		context.success( CreateFederation.KEY_FEDERATION_NAME, federation.getFederationName() );
		context.success( CreateFederation.KEY_FOM, federation.getFOM() );
	}

	/**
	 * Create a new {@link Federate} and have it join the {@link Federation} identified in the
	 * message. by its fedration name.
	 * 
	 * @param context The message containing the join information
	 * @param connection The connection that this request came from (so we can route calls back)
	 * @throws JFederationExecutionDoesNotExist We don't have a federation with the name listed
	 * @throws JFederateNameAlreadyInUse A joined federate is already using the requested name
	 */
	private synchronized void joinFederation( MessageContext context, RtiConnection connection )
		throws JFederationExecutionDoesNotExist, 
		       JFederateNameAlreadyInUse
	{
		try
		{
			JoinFederation request = context.getRequest( JoinFederation.class );
			Federation federation = getFederation( request.getFederationName() );
			request.setConnection( connection ); // we need the connection when joining
    		
    		// Hand the message off to the federation's incoming sink
    		federation.getIncomingSink().process( context );
    		
    		if( context.isSuccessResponse() )
    		{
    			logger.info( "Federate [%s] joined federation via connection %s (type:%s)",
    			             request.getFederateName(),
    			             connection.getName(),
    			             connection.getTransportType() );
    		}
		}
		catch( Exception e )
		{
			// Set an error on the context
			context.error( e );
		}
	}
	
	private synchronized void resignFederation( MessageContext context )
	{
		try
		{
			ResignFederation request = context.getRequest( ResignFederation.class );
			Federation federation = getFederation( request.getFederationName() );
    		
    		// Hand the message off to the federation's incoming sink
    		federation.getIncomingSink().process( context );
		}
		catch( Exception e )
		{
			// Set an error on the context
			context.error( e );
		}
	}

	private synchronized void destroyFederation( MessageContext context )
	{
		DestroyFederation request = context.getRequest( DestroyFederation.class );
		String name = request.getFederationName();
		
		// Create the federation object and store it
		logger.info( "ATTEMPT Destroy federation name="+name );

		try
		{
			// Find the federation
			Federation federation = getFederation( name );

			// Check to see if it has federates
			if( federation.containsFederates() )
			{
				logger.error( "FAILURE Can't destroy federation, it has federates joined: name="+name );
				throw new JFederatesCurrentlyJoined( "Federates joined to federation ["+name+"]" );
			}

			// Remove the federation
			federationManager.destroyFederation( federation );
			logger.info( "SUCCESS Destroyed federation name=" + name );
			context.success();
		}
		catch( Exception e )
		{
			context.error( e );
		}
	}

	/**
	 * Fetch the federation from the manager, checking to make sure it exists and throwing an
	 * exception if it does not.
	 * 
	 * @param federationName Name of the federation
	 * @return The known/active federation from within the {@link FederationManager}
	 * @throws JFederationExecutionDoesNotExist If the federation with the name cannot be found
	 */
	private final Federation getFederation( String federationName )
		throws JFederationExecutionDoesNotExist
	{
		Federation federation = federationManager.getFederation( federationName );
		if( federation != null )
			return federation;
		else
			throw new JFederationExecutionDoesNotExist( federationName );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
