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

import java.util.List;
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
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.messaging.ErrorResponse;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.PorticoMessage;

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
		logger.debug( "Attempting to find Federation Co-ordinator..." );
		
		// 1. Send out the Coordinator discovery request to see if anyone is already in the role.
		//    We don't send explicit to WAN. All our traffic loops back and we send from there.
		channel.sendFindCoordinator();
		
		// 2. Allow a reasonable amount of time for a response to come back in from the network.
		//    Defaults to 1000ms, but should be increased if using site-to-site connectivity.
		PorticoConstants.sleep( Configuration.getResponseTimeout() );
		
		// 3. Check to see if we have a manifest. If there is a coordinator out there, they
		//    will have sent this through to us. If there isn't one out there, we will have
		//    to take up the role and create out own Manifest.
		if( manifest == null )
		{
			logger.warn( "No co-ordinator found - appointing myself!" );
			this.manifest = new Manifest( this.fedname, this.uuid );
			this.manifest.setCoordinator( this.uuid );
		}
		else
		{
			logger.info( "Found co-ordinator: "+this.manifest.getCoordinator() );
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
		
		// TODO: Add check/wait here to see that it actually turns up
		
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
	 * @param joinModules The FOM modules that this federate is seeking to join with
	 * @param lrc The {@link LRC} this federate should ultimately route incoming messages to
	 * @return The name that the federate has been given
	 * @throws JFederationExecutionDoesNotExist If there is no active federation
	 * @throws JFederateAlreadyExecutionMember If the name is taken and unique name are required
	 * @throws Exception If there is a comms problem sending the message
	 */
	public String sendJoinFederation( String federateName, List<ObjectModel> joinModules, LRC lrc )
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

		// send the message out
		JoinRequest request = new JoinRequest( federateName, joinModules );
		channel.sendJoinFederation( Util.objectToByteBuffer(request) );
		
		// wait to see if we get acknowledgement from the coordinator
		PorticoConstants.sleep( Configuration.getResponseTimeout() );
		if( manifest.isLocalFederateJoined() == false )
			throw new JRTIinternalError( "Federation coordinator never acknowledged that we joined" );
		
		// store the LRC locally so that we can route incoming messages to it
		this.joinedLRC = lrc;

		// Enable the auditor if we are configured to use it
		if( Configuration.isAuditorEnabled() )
			this.auditor.startAuditing( fedname, federateName, lrc );
		
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
			logger.error( "FAILURE destoryFederation ["+fedname+"]: doesn't exist" );
			throw new JFederationExecutionDoesNotExist( "doesn't exist: "+fedname );
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
	 * Sent our manifest out to the federation. If the manifest DOES NOT list us as the coordinator
	 * we won't take any action at all.
	 */
	private void sendSetManifest()
	{
		if( this.manifest.isCoordinator() == false )
			return;
		
    	// send the response
		try
		{
			byte[] buffer = Util.objectToByteBuffer( this.manifest );
			channel.sendSetManifest( buffer );
		}
		catch( Exception e )
		{
			logger.error( "Error while sending manifest: "+e.getMessage(), e );
		}
	}
	
	/**
	 * Someone has send a "FindCoordinator" request. If we are the coordinator, respond with
	 * our manifest, otherwise disregard.
	 */
	public void receiveFindCoordinator( UUID sender, byte[] payload )
	{
		// disregard if this is our own - we won't have a manifest to draw info from
		if( this.manifest == null )
			return;

		// Am I the coordinator? If I am I better respond
		if( manifest.isCoordinator(this.uuid) )
		{
        	// read off the UUID of the requester
        	logger.debug( "Received request for manifest from "+sender );
        	System.out.println( "I am coordinator - received request for me from "+sender );
        
        	// tell the manifest a member has connected - this is where the handle is assigned
        	manifest.memberConnectedToChannel( sender );
        	
        	// send the response
        	this.sendSetManifest();
		}
	}

	@SuppressWarnings("deprecation")
	public synchronized void receiveSetManifest( UUID sender, byte[] payload )
	{
		// if we already have a manifest, AND we are the coordinator, ignore as we sent this out
		// otherwise, take the updated manifest
		if( manifest != null && manifest.isCoordinator() )
			return;

		logger.debug( "Received updated manifest from "+sender );
		
		try
		{
			// Get the manifest
			Manifest manifest = (Manifest)Util.objectFromByteBuffer( payload );
			
			// If this manifest is from someone who WAS NOT previously our coordinator, let's make
			// some noise about it. This could be legitimate (maybe the coordinator left), but more
			// likely it's someone who has just wrongfully elected themselves and is trying to tell
			// us what the state of the world is.
			if( this.manifest != null &&
				this.manifest.getManifestVersion() > manifest.getManifestVersion() )
			{
				System.out.println( "//" );
				System.out.println( "// WARNING - DISCARDING MANIFEST" );
				System.out.println( "// Received manifest with earlier version than current one." );
				System.out.println( "// It is likely that a federate has improperly elected "+
				                       "themselves coordinator." );
				System.out.println( "//" );
				System.out.println( "// Existing manifest version: "+this.manifest.getManifestVersion()+
				                    " (co-ordinator: "+this.manifest.getCoordinator()+")" );
				System.out.println( "// Incoming manifest version: "+manifest.getManifestVersion()+
				                    " (co-ordinator: "+manifest.getCoordinator()+")" );
				System.out.println( "//" );
				return;
			}
				
			// Update our local manifest
			manifest.setLocalUUID( this.uuid );
			this.manifest = manifest;
			
			// Push the updated FOM into our LRC State
			if( joinedLRC != null )
				joinedLRC.getState().remoteJGroupsManifestReceivedHack( this.manifest.getFom() );
			
			logger.debug( "Installed new manifest (follows)" );
			logger.debug( manifest );
		}
		catch( Exception e )
		{
			logger.error( "Error installing new manifest: "+e.getMessage(), e );
		}
	}

	public synchronized void receiveCreateFederation( UUID sender, byte[] payload )
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

	public synchronized void receiveJoinFederation( UUID sender, byte[] payload )
	{
		// Only process these messages if we are the coordinator
		if( manifest.isCoordinator() == false )
			return;
		
		//
		// Extract the Join Request from the incoming payload
		//
		String federateName = null;
		List<ObjectModel> joinModules = null;
		try
		{
			JoinRequest joinRequest = (JoinRequest)Util.objectFromByteBuffer( payload );
			federateName = joinRequest.getFederateName();
			joinModules = joinRequest.getFomModules();

			logger.debug( "Received federate join notification: federate="+federateName+
			              ", federation="+fedname+", source="+sender );
		}
		catch( Exception e )
		{
			logger.error( "(Coordinator) Error parsing request to join federation", e );
		}

		//
		// Try to merge the joiners modules with the base FOM. If we can record that they joined.
		//
		try
		{
			ModelMerger.mergeDryRun( manifest.getFom(), joinModules );
			ObjectModel merged = ModelMerger.merge( manifest.getFom(), joinModules );
			manifest.setFom( merged );
			manifest.federateJoined( sender, federateName );
		}
		catch( Exception e )
		{
			logger.error( "(Coordinator) Failed to join federate ["+federateName+"] to federation ["+
			              fedname+"]: Couldn't merge their FOM modules with our base FOM", e );
			return;
		}
		
		//
		// We've updated the manifest, so we should let people know
		//
		this.sendSetManifest();
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
		if( manifest.isJoinedFederate(crashed) )
		{
			String federateName = manifest.getFederateName( crashed );
			logger.warn( "Federate ["+federateName+
			             "] has crashed. Sending fake resignation because it was too rude to." );
			
			receiveGoodbye( crashed, new byte[]{} );
		}
		else
		{
			logger.warn( "Unknown channel member crashed. Don't think it was a federate. Ignoring. uuid="+crashed );
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
