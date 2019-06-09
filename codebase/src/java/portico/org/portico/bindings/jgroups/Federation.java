/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.util.Util;
import org.portico.bindings.jgroups.channel.Channel;
import org.portico.bindings.jgroups.channel.Manifest;
import org.portico.bindings.jgroups.wan.local.Gateway;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederatesCurrentlyJoined;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JResignAction;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.messaging.ErrorResponse;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.federation.msg.ResignFederation;

/**
 * Class represents a link to a particular federation. This contains the actual
 * {@link Channel} that is used for communication and potentially an instance of 
 * the {@link Gateway} for a WAN connection if relevant. Instances of this
 * class are created and stored in a connection even before we know there is actually
 * a federate operating on the channel (we need to connect to find this out, and
 * this class is created to hold the connection).
 * 
 * When a federate joins a federation, it's {@link LRC} is handed to that {@link Federation}
 * instance which in turn allows messages to be routed back to it. On resignation, this link
 * is removed.
 */
public class Federation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	// Connection State
	private String fedname;
	private boolean connected;
	private UUID uuid;
	private Manifest manifest;

	// Active Federation Connection
	public LRC joinedLRC;
	
	// Local Connection
	private Channel channel;
	
	// WAN properties
	private boolean wanEnabled;
	private Gateway gateway;

	// write metadata about incoming/outgoing message flow
	private Auditor auditor;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Federation( String name )
	{
		this.logger = LogManager.getFormatterLogger( "portico.lrc.jgroups" );
		
		// Connection State
		this.fedname = name;
		this.connected = false;
		this.uuid = UUID.randomUUID();
		this.manifest = null; // set during connection as part of the findCoordinator() method

		// Active Federation Connection
		this.joinedLRC = null; // set during sendJoinFederation, cleared in sendResignFederation
		
		// Local Connection
		this.channel = null;
		
		// WAN properties
		this.wanEnabled = Configuration.isWanEnabled();
		this.gateway = null;

		// create this, but leave as disabled for now - gets turned on in joinFederation
		this.auditor = new Auditor();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////
	/// Lifecycle Methods  ///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	public void connect() throws JRTIinternalError
	{
		if( this.connected )
			return;

		// connect to the main federation channel
		this.channel = new Channel( this );
		this.channel.connect();
		
		// create the WAN gateway - but don't enable it unless we need it
		this.gateway = new Gateway( this );
		if( this.wanEnabled )
			this.gateway.connect();

		// find the federation coordinator
		try
		{
			this.findCoordinator();
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( e.getMessage(), e );
		}
		
		this.connected = true;
	}
	
	public void disconnect()
	{
		if( this.connected == false )
			return;
		
		this.channel.disconnect();
		if( this.gateway != null )
			this.gateway.disconnect();

		// clear out any connection we have to an LRC
		if( this.joinedLRC != null )
			this.joinedLRC = null;
		
		this.connected = false;
	}
	
	public boolean isConnected()
	{
		return this.connected;
	}
	
	/**
	 * Finds the federation co-ordinator and gets the manifest from them. If there is no
	 * co-ordinator, step up and be one!
	 */
	private void findCoordinator() throws Exception
	{
		// 1. Send out the Coordinator discovery request to see if anyone is already in the role
		channel.sendFindCoordinator();
		
		// 2. Allow some time for the responses to come in
		PorticoConstants.sleep( 2000 ); // FIXME replace with configurable -- especially for WAN
		
		// 3. Check to see if we have a manifest. If there is a coordinator out there, they
		//    will have sent this through to us. If there isn't one out there, we will have
		//    to take up the role and create out own Manifest.
		if( manifest == null )
		{
			logger.info( "No co-ordinator found - appointing myself!" );
			this.manifest = new Manifest( this.fedname, this.uuid );
			this.manifest.setCoordinator( this.uuid );
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////
	/// Message Sending Methods  /////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	public final void send( PorticoMessage message ) throws JRTIinternalError
	{
		// turn the PorticoMessage into a JGroups Message
		byte[] payload = MessageHelpers.deflate( message );

		// Log an audit message for the send
		if( auditor.isRecording() )
			auditor.sent( message, payload.length );

		// Send the message
		channel.send( payload );
	}
	
	//////////////////////////////////////////////////////////////////////////////
	/// Federation Lifecycle Methods  ////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	/**
	 * Checks to make sure that the group we are connected to does not already contain
	 * a federation. If it does not, the FOM is sent out to all participants so that
	 * they can record it and register a federation as active.
	 * 
	 * @throws Exception If there is a problem sending the message out
	 * @throws JFederationExecutionAlreadyExists If there is a federation already in the channel
	 */
	public void sendCreateFederation( ObjectModel fom ) throws JFederationExecutionAlreadyExists,
	                                                           Exception
	{
		logger.debug( "REQUEST createFederation: name="+fedname );

		// make sure we're not already connected to an active federation
		if( manifest.containsFederation() )
		{
			logger.error( "FAILURE createFederation: already exists, name="+fedname );
			throw new JFederationExecutionAlreadyExists( "federation exists: "+fedname );
		}
		
		// send out create federation call and get an ack from everyone
		byte[] buffer = Util.objectToByteBuffer( fom );
		channel.sendCreateFederation( buffer );
		
		logger.info( "SUCCESS createFederation: name=" + fedname );
	}

	/**
	 * Validates that the federation exists to join and then checks the name requested by the
	 * federate. If there is a joined federate with the same name AND unique names are *required*
	 * (RID option), an exception will be thrown, otherwise the name of the federate will be
	 * modified to put a number on the end in the format: "federateName-federateHandle".
	 * 
	 * The string returned by this method is the name that the federate has been given, which
	 * in most cases will be the one that they requested.
	 * 
	 * @param federateName The name the federate wishes to use
	 * @param lrc The {@link LRC} this federate should ultimately route incoming messages to
	 * @return The name that the federate has been given
	 * @throws JFederationExecutionDoesNotExist If there is no active federation
	 * @throws JFederateAlreadyExecutionMember If the name is taken and unique name are required
	 * @throws Exception If there is a comms problem sending the message
	 */
	public String sendJoinFederation( String federateName, LRC lrc )
		throws JFederationExecutionDoesNotExist,
		       JFederateAlreadyExecutionMember,
		       Exception
	{
		logger.debug( "REQUEST joinFederation: federate="+federateName+", federation="+fedname );

		// check to see if there is an active federation
		if( manifest.containsFederation() == false )
		{
			logger.info( "FAILURE joinFederation: federation doesn't exist, name="+fedname );
			throw new JFederationExecutionDoesNotExist( "federation doesn't exist: "+fedname );
		}

		// check to see if someone already exists with the same name
		// If we have been configured to allow non-unique names, check, but don't error,
		// just augment the name from "federateName" to "federateName (handle)"
		if( manifest.containsFederate(federateName) )
		{
			if( PorticoConstants.isUniqueFederateNamesRequired() )
			{
				logger.info( "FAILURE joinFederation: federate="+federateName+", federation="+
				             fedname+": Federate name in use" );
				throw new JFederateAlreadyExecutionMember( "federate name in use: "+federateName );
			}
			else
			{
				federateName += " ("+manifest.getLocalFederateHandle()+")";
				logger.warn( "WARNING joinFederation: name in use, changed to "+federateName );
			}
		}

		// store the LRC locally so that we can route incoming messages to it
		this.joinedLRC = lrc;

		// Enable the auditor if we are configured to use it
		if( Configuration.isAuditorEnabled() )
			this.auditor.startAuditing( fedname, federateName, lrc );
		
		// send the message out
		channel.sendJoinFederation( federateName.getBytes() );
		
		logger.info( "SUCCESS Joined federation with name="+federateName );
		return federateName;	
	}

	/**
	 * Sends the resignation notification out with the appropriate header so that it is
	 * detected by other channel members, allowing them to update their manifest appropriately.
	 * 
	 * After this method returns, the local connection will unlink itself from the {@link LRC}
	 * it had previously tied into as part of {@link #joinFederation(String, LRC)}.
	 * 
	 * @param resignMessage The Portico message for the resignation, contains resign action
	 * @throws Exception If there is a communications problem
	 */
	public void sendResignFederation( PorticoMessage resignMessage ) throws Exception
	{
		// get the federate name before we send and cause it to be removed from the manifest
		String federateName = this.manifest.getLocalFederateName();
		logger.debug( "REQUEST resignFederation: federate="+federateName+", federation="+fedname );

		byte[] payload = MessageHelpers.deflate( resignMessage );
		
		// send to the local channel
		channel.sendResignFederation( payload );
		
		// disconnect from the WAN gateway if need be
		if( gateway != null )
			gateway.disconnect();
		
		// all done, disconnect our incoming receiver
		// we received the message sent above as well, so the receiver will update the
		// manifest as it updates it for any federate resignation
		logger.info( "SUCCESS Federate ["+federateName+"] resigned from ["+fedname+"]" );
		this.joinedLRC = null;
		this.auditor.stopAuditing();
	}

	/**
	 * Notifies all other connections on the channel that the federation should be destroyed.
	 * Checks are done first to make sure the channel is in a state to service this request,
	 * including a check to make sure there actually is an active federation to destroy, and
	 * that there are no federates joined to it.
	 * 
	 * @throws Exception
	 */
	public void sendDestroyFederation() throws JFederationExecutionDoesNotExist,
	                                           JFederatesCurrentlyJoined,
	                                           Exception
	{
		logger.debug( "REQUEST destroyFederation: name="+fedname );
		
		// check to make sure there is a federation to destroy
		if( manifest.containsFederation() == false )
		{
			// Look, let's just let this slide. No exception needed. There is an active
			// channel, so there MAY have been a federation before everyone resigned. If
			// we throw an exception we'll give good federates trying to clean things up
			// a potentially false picture (and maybe cause them to exit).
			// 
			// The only thing that will get upset is our unit tests, and we'll fix them.
			// In practical terms, NOT throwing an exception here does no harm.
			logger.warn( "destoryFederation() called on channel where there is no active federation, ignoring" );
			return;
		}

		// check to make sure there are no federates still joined
		if( manifest.getFederateHandles().size() > 0 )
		{
			String stillJoined = manifest.getFederateHandles().toString();
			logger.info( "FAILURE destroyFederation ["+fedname+"]: federates still joined "+
			             stillJoined );
			throw new JFederatesCurrentlyJoined( "Federates still joined: "+stillJoined );
		}

		// send the notification out to all receivers and wait for acknowledgement from each
		channel.sendDestroyFederation( fedname.getBytes() );
		
		// we don't need to update the manifest directly here, that will be done by
		// out message listener, which will have received the message we sent above

		logger.info( "SUCCESS destroyFederation: name="+fedname );
	}

	//////////////////////////////////////////////////////////////////////////////
	/// Incoming Message Methods  ////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	public final void receiveAsynchronous( byte[] payload )
	{
		if( this.joinedLRC == null )
			return;
		
		try
		{
			// fetch the payload from the message
			PorticoMessage message = MessageHelpers.inflate( payload,
			                                                 PorticoMessage.class,
			                                                 joinedLRC );
			
			// if we get null back, it means we should stop processing now
			if( message == null )
					return;
			
			// log an audit entry for the reception
			if( auditor.isRecording() )
				auditor.received( message, payload.length );
			
			// shove into our queue for later processing
			joinedLRC.getState().getQueue().offer( message );
		}
		catch( Exception e )
		{
			// somewhere between when we we first checked that the LRC wasn't null and
			// the time we came to use it, it became null (due to resignation). Roll with
			// if and just skip over. No point worring about being unable to process a
			// message from a federate we just resigned from anyway.
			if( joinedLRC == null )
				return;
			else
				logger.error( "Error processing received message: " + e.getMessage()+", size="+payload.length, e );
		}
	}
	
	public Object receiveSynchronous( byte[] payload )
	{
		if( this.joinedLRC == null )
			return null; // ignore

		try
		{
			// fetch the payload from the message
			PorticoMessage message = MessageHelpers.inflate( payload, PorticoMessage.class );
			
    		// log an audit entry for the reception
    		if( auditor.isRecording() )
    			auditor.received( message, payload.length );

			MessageContext context = new org.portico.utils.messaging.MessageContext( message );
			joinedLRC.getIncomingSink().process( context );
			return context.getResponse();
		}
		catch( Exception e )
		{
			logger.error( "Error processing receive message: " + e.getMessage(), e );
			return new ErrorResponse( e );
		}
	}

	//////////////////////////////////////////////////////////////
	/// Incoming Control Message Handlers  ///////////////////////
	//////////////////////////////////////////////////////////////
	/**
	 * Someone has send a "FindCoordinator" request. If we are the coordinator, respond with
	 * our manifest, otherwise disregard.
	 */
	public void receiveFindCoordinator( UUID sender, byte[] payload )
	{
		// disregard if this is our own - we won't have a manifest to draw info from
		if( this.manifest == null )
			return;

		if( manifest.isCoordinator() )
		{
        	// read off the UUID of the requester
        	logger.debug( "Received request for manifest from "+sender );
        
        	// tell the manifest a member has connected - this is where the handle is assigned
        	manifest.memberConnectedToChannel( sender );
        	
        	// send the response
			try
			{
				byte[] buffer = Util.objectToByteBuffer( this.manifest );
				channel.sendSetManifest( buffer );
				logger.debug( "Sent manifest ("+buffer.length+"b) to "+sender );
			}
			catch( Exception e )
			{
				logger.error( "Error while sending manifest to ["+sender+"]: "+e.getMessage(), e );
			}
		}
	}

	public void receiveSetManifest( UUID sender, byte[] payload )
	{
		// if we already have a manifest, AND we are the coordinator, ignore as we sent this out
		// otherwise, take the updated manifest
		if( manifest != null && manifest.isCoordinator() )
			return;
		
		logger.debug( "Received updated manifest from "+sender );
		
		try
		{
			Manifest manifest = (Manifest)Util.objectFromByteBuffer( payload );
			manifest.setLocalUUID( this.uuid );
			this.manifest = manifest;
			logger.debug( "Installed new manifest (follows)" );
			logger.debug( manifest );
		}
		catch( Exception e )
		{
			logger.error( "Error installing new manifest: "+e.getMessage(), e );
		}
	}

	public void receiveCreateFederation( UUID sender, byte[] payload )
	{
		// tell our Manifest that a federation has been created using the FOM in the payload
		logger.debug( "Received federation creation notification: federation="+fedname+
		              ", fomSize="+payload.length+"b, source="+sender );
		
		// May have a problem when a bunch of federates start up and we get this
		// callback before we've completed our search for the coordinator (and thus
		// don't have a manifest). Don't laugh - it DID happen.
		if( manifest == null )
			return;
		
		try
		{
			// turn the buffer into a FOM and store it on the federation manifest
			manifest.federationCreated( (ObjectModel)Util.objectFromByteBuffer(payload) );
			logger.info( "Federation ["+fedname+"] has been created" );
		}
		catch( Exception e )
		{
			logger.error( "Error installing FOM for federation ["+fedname+"]: "+
			              "this federate will not be able to join the federation", e );
		}
	}

	public void receiveJoinFederation( UUID sender, byte[] payload )
	{
		// tell our Manifest that a federate has joined the federation
		String federateName = new String( payload );
		logger.debug( "Received federate join notification: federate="+federateName+
		              ", federation="+fedname+", source="+sender );
		
		manifest.federateJoined( sender, federateName );
		logger.info( "Federate ["+federateName+"] joined federation ["+fedname+"]" );
	}
	
	public void receiveResignFederation( UUID sender, byte[] payload )
	{
		// tell our Manifest that a federate has resigned from the federation
		receiveAsynchronous( payload );

		// log the resignation of the federate with the manifest
		// process is still a member of the channel, just no longer part of the federation
		String federateName = manifest.getFederateName( sender );
		manifest.federateResigned( sender );
		logger.info( "Federate ["+federateName+"] has resigned from ["+fedname+"]" );

	}
	
	public void receiveDestroyFederation( UUID sender, byte[] payload )
	{
		// tell our Manifest that a federate has destroyed the federation
		String federationName = new String( payload );
		logger.trace( "Received federate destroy notification: federation="+federationName+
		              ", source="+sender );
		
		if( manifest.containsFederation() == false )
		{
			logger.error( "Member ["+sender+"] apparently destroyed federation ["+
			              federationName+"], but we didn't know it existed, ignoring..." );
		}
		else if( manifest.getFederateHandles().size() > 0 )
		{
			logger.error( "Connection ["+sender+"] apparnetly destoryed federation ["+
			              federationName+"], but we still have active federates, ignoring... " );
		}
		else
		{
			manifest.federationDestroyed();
			logger.info( "Federation ["+federationName+"] has been destroyed" );
		}
	}

	public void receiveGoodbye( UUID leaver, byte[] payload )
	{
		logger.trace( "Received goodbye notification: channel="+fedname+", from="+leaver );

		//
		// was this federate joined (or just a member)?
		//
		if( manifest.isJoinedFederate(leaver) )
		{
			int federateHandle = manifest.getFederateHandle( leaver );
			String federateName = manifest.getFederateName( leaver );
			
			// synthesize a resign notification
			ResignFederation resign =
				new ResignFederation( JResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES );
			resign.setSourceFederate( federateHandle );
			resign.setFederateName( federateName );
			resign.setFederationName( fedname );
			resign.setImmediateProcessingFlag( true );
			
			// Turn the message into a byte[] -- this is what the receive method wants even
			// thought it is going to turn it right back into a PorticoMessage. Given how
			// infrequent we will be doing this it isn't a big deal
			this.receiveResignFederation( leaver, MessageHelpers.deflate(resign) );
			
			logger.info( "Federate ["+federateName+","+federateHandle+
			             "] disconnected, synthesized resign message. All done." );
		}

		//
		// Remove the leaver from the manifest. This will also update
		// our idea about who the coordinator is if that is necessary.
		//
		manifest.memberLeftChannel( leaver );
	}

	/** Confirmation that a federate has left when we did not expect */
	public void receiveCrashed( UUID crashed )
	{
		// If we're not the coordinator this should never have been called.
		// The only time this could be called is if the coordinator was the
		// one who crashed AND we are the new JGroups coordinator (congrats)
		if( manifest.getCoordinator().equals(crashed) )
		{
			// The king is dead, all hail... us!
			// What do we do now?
			logger.warn( "Coordinator crashed. We are the new coordinator. The kind it dead, long live the king." );
			try
			{
				channel.sendCrashedFederate( crashed );
			}
			catch( Exception e )
			{
				logger.error( "Exception while telling federation about crashed coordinator, expect inconsistency", e );
			}

			return;
		}

		// if we're not the coordinator then none of this mess is ours to worry about
		if( manifest.isCoordinator() == false )
			return;

		// was the swine even joined!?
		if( manifest.isJoinedFederate(crashed) == false )
		{
			// a connection crashed, but it wasn't yet a joined federate
			logger.warn( "Unknown channel member crashed. Don't think it was a federate. Ignoring. uuid="+crashed );
			return;
		}

		// OK - we have a crashed federate, we are the coordinator, it's up to us
		try
		{
			String name = manifest.getFederateName( crashed );
			logger.warn( "Federate ["+name+"] has crashed. Sending fake resignation because it was too rude to." );
			channel.sendCrashedFederate( crashed );
		}
		catch( Exception e )
		{
			logger.error( "Exception while telling federation about a crashed federate, expect inconsistency", e );
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////
	/// Getter and Setter Methods  ///////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	public String getFederationName()
	{
		return this.fedname;
	}

	public Manifest getManifest()
	{
		return this.manifest;
	}
	
	public boolean isWanEnabled()
	{
		return this.wanEnabled;
	}
	
	public Logger getLogger()
	{
		return this.logger;
	}

	public UUID getLocalUUID()
	{
		return this.uuid;
	}
	
	public Channel getChannel()
	{
		return this.channel;
	}

	public Gateway getGateway()
	{
		return this.gateway;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
