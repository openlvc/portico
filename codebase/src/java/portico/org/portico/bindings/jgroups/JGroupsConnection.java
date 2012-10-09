/*
 *   Copyright 2012 The Portico Project
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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.portico.bindings.ConnectedRoster;
import org.portico.bindings.IConnection;
import org.portico.bindings.jgroups.channel.FederationChannel;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.services.federation.msg.CreateFederation;
import org.portico.lrc.services.federation.msg.DestroyFederation;
import org.portico.lrc.services.federation.msg.JoinFederation;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.utils.logging.Log4jConfigurator;
import org.portico.utils.messaging.PorticoMessage;

public class JGroupsConnection implements IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean running;
	protected Logger logger;
	private Map<String,FederationChannel> federations;
	private FederationChannel joinedChannel;

	// the LRC we are providing the connection for
	private LRC lrc;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public JGroupsConnection()
	{
		this.running = false;
		this.logger = null;         // set on configure()
		this.lrc = null;            // set on configure()
		this.joinedChannel = null;  // set on joinFederation(), removed on resignFederation()
		
		this.federations = new HashMap<String,FederationChannel>();
		//this.federateHandle = PorticoConstants.NULL_HANDLE;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Lifecycle Methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * <i>This method is called by the Portico infrastructure during setup.</i>
	 * <p/>
	 * Called right after the connection implementation has been instantiated. This gives the
	 * connection an opportunity to perform any setup it requires. If there is a problem (either
	 * with any configuration data it has located through the properties map or some other
	 * mechanism) then it should throw a {@link JConfigurationException}. The {@link LRC} into
	 * which the connection is being deployed can be obtained via this method.
	 */
	public void configure( LRC lrc, Map<String,Object> properties )
		throws JConfigurationException
	{
		this.lrc = lrc;
		this.logger = Logger.getLogger( "portico.lrc.jgroups" );
		// set the appropriate level for the jgroups logger, by default we'll just turn it
		// off because it is quite noisy. that said, we will allow for it to be turned back
		// on via a configuration property
		String jglevel = System.getProperty( JGroupsProperties.PROP_JGROUPS_LOGLEVEL, "OFF" );
		Log4jConfigurator.setLevel( jglevel, "org.jgroups" );
	}
	
	/**
	 * <i>This method is called by the Portico infrastructure during setup.</i>
	 * <p/>
	 * When it is time for the kernel to connect to the RTI/federate/etc... and to start accepting
	 * incoming messages, while being ready to send outgoing messages, this method is called. The
	 * connection implementations should use it to connect to network (or whatever communications
	 * mechanism is being used). This should include any discovery of remote components (such as
	 * the discovery of an RTI by federates).
	 */
	public void connect() throws JRTIinternalError
	{
		if( this.running )
			return;
		
		this.running = true;
		logger.info( "jgroups connection is up and running" );
	}
	
	/**
	 * <i>This method is called by the Portico infrastructure during shutdown.</i>
	 * <p/>
	 * When the kernel is ready to shutdown, it will call this method, signalling to the connection
	 * that it should disconnect and do any shutdown and cleanup necessary.
	 */
	public void disconnect() throws JRTIinternalError
	{
		if( this.running == false )
		{
			logger.info( "jgroups connection is already disconnected" );
			return;
		}

		logger.info( "jgroups connection is disconnecting..." );
		
		// for each channel we're connected to, disconnect from it
		for( FederationChannel federation : federations.values() )
			federation.disconnect();

		federations.clear();
		logger.info( "jgrousp connection has disconnected" );
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Message sending methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Broadcast the given message out to all participants in a federation asynchronously.
	 * As soon as the message has been received for processing or sent, this method is free
	 * to return. No response will be waited for.
	 * 
	 * @param message The message to broadcast
	 * @throws Exception If there was an error when sending the message
	 */
	public void broadcast( PorticoMessage message ) throws JFederateNotExecutionMember,
	                                                       JRTIinternalError
	{
		validateConnected();
		joinedChannel.send( message );
	}
	
	/**
	 * This method should be used when a message has to be sent and then time for responses to
	 * be broadcast back is needed before moving on. In this case, the connection will broadcast
	 * the message and then sleep for an amount of time appropriate based on the underlying
	 * comms protocol in use. For example, the JVM connection won't sleep for long, but a connection
	 * sending information over a network should wait longer.
	 * 
	 * @param message The message to broadcast
	 * @throws Exception If there was an error when sending the message
	 */
	public void broadcastAndSleep( PorticoMessage message ) throws Exception
	{
		validateConnected();
		joinedChannel.send( message );
		PorticoConstants.sleep( JGroupsProperties.RESPONSE_TIMEOUT );
	}

	/**
	 * Runs a simple check to make sure this connection is connected to a federation. If it isn't
	 * an exception is thrown, if it is, the method will happily return.
	 */
	private void validateConnected() throws JFederateNotExecutionMember
	{
		if( joinedChannel == null )
			throw new JFederateNotExecutionMember( "Connection has not been joined to a federation" );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// bootstrapping methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the {@link FederationChannel} channel for the identified federation. All connections
	 * are cached in a map so that any time we need to work with a channel, we only connect
	 * to it once. This method will return the already existing channel for the given name if
	 * one exists, or it will attempt to create a new connection to the channel and return it.
	 * This method will take care of caching the channel for later use if a new one is created.
	 *  
	 * @param federationName The name of the channel to connect to.
	 * @return The {@link FederationChannel} channel
	 * @throws Exception If there is a problem making the connection
	 */
	private FederationChannel getFederationChannel( String federationName ) throws Exception
	{
		// IMPLEMENTATION NOTE: We maintain all channel connections until this Portico connection
		//                      is told to shutdown (stored in federation). When we create a
		//                      federation we store the "created" flag in the manifest, which
		//                      itself is shared with other connections via JGroups shared state
		//                      sent to them when they join the channel. Now, if we just connected
		//                      to the channel long enough to issue a create, and then left the
		//                      channel, unless someone else was in there the state channel would
		//                      disapper (along with the state) when we left. Not really much of
		//                      a "createFederation" call if the federation disappears right after.
		//                      As such, when we connect to a channel, we stay in there until the
		//                      entire LRC is shutting down. This keeps things open for as long
		//                      as possible. If the LRC exits before anyone else gets into pick
		//                      up the state, well, the federation deserves to vanish.
		if( this.federations.containsKey(federationName) )
			return this.federations.get(federationName);
		
		FederationChannel federation = new FederationChannel( federationName );
		federation.connect();
		this.federations.put( federationName, federation );
		return federation;
	}

	/**
	 * Do any special processing required to create a federation and process the request. If there
	 * is any kind of connection specific problem (or a problem creating the federation in the
	 * first place), throw an exception idicating so.
	 */
	public void createFederation( CreateFederation createMessage ) throws Exception
	{
		// connect to the channel if we are not already
		FederationChannel federation = getFederationChannel( createMessage.getFederationName() );

		// request to the connection that we create an active federation inside it
		federation.createFederation( createMessage.getModel() );
	}

	/**
	 * Do any special processing required to join a federation and process the request. If there
	 * is any kind of connection specific problem (or a problem joining the federation in the
	 * first place), throw an exception idicating so. The return should be an instance of
	 * {@link ConnectedRoster} containing all the necessary information about the federation
	 * (local handle, remote federate handles, fom, etc...)
	 */
	public ConnectedRoster joinFederation( JoinFederation joinMessage ) throws Exception
	{
		// connect to the channel if we are not already
		FederationChannel federation = getFederationChannel( joinMessage.getFederationName() );
		
		// validate that our FOM modules can be merged successfully with the existing FOM first
		// make sure the FOM modules can be successfully merged with the existing model
		logger.debug( "Validate that ["+joinMessage.getJoinModules().size()+
		              "] modules can merge successfully with the existing FOM" );
		ModelMerger.mergeDryRun( federation.getManifest().getFom() , joinMessage.getJoinModules() );
		logger.debug( "Modules can be merged successfully, continue with join" );

		// tell the channel that we're joining the federation
		String joinedName = federation.joinFederation( joinMessage.getFederateName(), this.lrc );
		// the joined name could be different, so update the request to make sure it is correct
		joinMessage.setFederateName( joinedName ); 
		
		// now that we've joined a channel, store it here so we can route messages to it
		this.joinedChannel = federation;
		
		// we have to merge the FOMs together here for us before this returns to the Join
		// handler and a RoleCall is sent out. We have to do this because although we receive
		// our own RoleCall notice (with the additional modules) we won't process it as we
		// can't tell if it's one we sent out because we joined (and thus need to merge) or because
		// someone else joined. Additional modules will only be present if it is a new join, so
		// we could figure it out that way, but that will cause redundant merges for the JVM
		// binding (as all connections share the same object model reference). To cater to the
		// specifics of this connection it's better to put the logic in the connection rather than
		// in the generic-to-all-connections RoleCallHandler. Long way of saying we need to merge
		// in the additional join modules that were provided here. F*** IT! WE'LL DO IT LIVE!
		if( joinMessage.getJoinModules().size() > 0 )
		{
			logger.debug( "Merging "+joinMessage.getJoinModules().size()+
			              " additional FOM modules that we receive with join request" );

			federation.getManifest().getFom().unlock();
			ModelMerger.merge( federation.getManifest().getFom(), joinMessage.getJoinModules() );
			federation.getManifest().getFom().lock();
		}

		// create and return the roster
		return new JGroupsRoster( federation.getManifest().getLocalFederateHandle(),
		                          federation.getManifest().getFederateHandles(),
		                          federation.getManifest().getFom() );
	}

	/**
	 * Do any special processing required to resign from a federation and process the request.
	 * If there is any kind of connection specific problem (or a problem resigning from the
	 * federation in the first place), throw an exception idicating so.
	 */
	public void resignFederation( ResignFederation resignMessage ) throws Exception
	{
		// make sure we're joined to the federation
		if( joinedChannel == null || joinedChannel.getManifest().isLocalFederateJoined() == false )
		{
			throw new JFederateNotExecutionMember( "Federate ["+resignMessage.getFederateName()+
			                                       "] not joined to ["+
			                                       resignMessage.getFederationName()+"]" );
		}
		
		// send out the resign notification
		joinedChannel.resignFederation( resignMessage );
		
		// all happy, as we're no longer joined, set out joined channel to null
		joinedChannel = null;
	}
	
	/**
	 * Do any special processing required to destroy a federation and process the request. If there
	 * is any kind of connection specific problem (or a problem destroying the federation in the
	 * first place), throw an exception idicating so.
	 */
	public void destroyFederation( DestroyFederation destoryMessage ) throws Exception
	{
		// connect to the channel if we are not already
		FederationChannel federation = getFederationChannel( destoryMessage.getFederationName() );
		
		// tell the channel that we want to destroy it (sanity checks are in there)
		federation.destroyFederation();
	}

	/**
	 * Returns a list of all the federations currently active. The manner for fetching this will
	 * be binding-specific. If there is a problem locating this information, throw an exception.
	 */
	public String[] listActiveFederations() throws Exception
	{
		return new String[]{};
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
