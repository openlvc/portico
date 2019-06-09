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
package org.portico.lrc;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.portico.bindings.IConnection;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.management.Federate;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.Space;
import org.portico.lrc.notifications.NotificationManager;
import org.portico.lrc.services.mom.data.MomManager;
import org.portico.lrc.services.saverestore.data.RestoreManager;
import org.portico.lrc.services.saverestore.data.SaveManager;
import org.portico.lrc.services.sync.data.SyncPointManager;
import org.portico.utils.messaging.AbstractMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.PorticoMessage;
import org.portico.utils.messaging.VetoException;
import org.portico2.common.services.ddm.data.RegionStore;
import org.portico2.common.services.ownership.data.OwnershipManager;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico2.rti.services.object.data.Repository2;
import org.portico2.rti.services.time.data.TimeManager;

/**
 * The parent class for all LRC message handlers. During the {@link #initialize(Map)} method
 * it will fetch a bunch of useful things from the properties and cache them in instance variables
 * so that they can be accessed by child classes.
 */
public abstract class LRCMessageHandler extends AbstractMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected LRC lrc;
	protected LRCState lrcState;
	protected Logger logger;
	protected IConnection connection;
	protected NotificationManager notificationManager;
	protected InterestManager interests;
	protected RegionStore regions;
	protected Repository2 repository;
	protected OwnershipManager ownership;
	protected TimeManager timeManager;
	protected SyncPointManager syncManager;
	protected MomManager momManager;
	protected SaveManager saveManager;
	protected RestoreManager restoreManager;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void initialize( Map<String,Object> properties ) throws JConfigurationException
	{
		super.initialize( properties );
		this.lrc = (LRC)properties.get( LRCProperties.KEY_LRC );
		this.lrcState = this.lrc.getState();
		this.logger = this.lrc.getLrcLogger();
		this.connection = this.lrc.getConnection();
		this.notificationManager = this.lrc.getNotificationManager();
		this.interests = this.lrcState.getInterestManager();
		this.regions = this.lrcState.getRegionStore();
		this.repository = this.lrcState.getRepository();
		this.ownership = this.lrcState.getOwnershipManager();
		this.timeManager = this.lrcState.getTimeManager();
		this.syncManager = this.lrcState.getSyncPointManager();
		this.momManager = this.lrcState.getMomManager();
		this.saveManager = this.lrcState.getSaveManager();
		this.restoreManager = this.lrcState.getRestoreManager();
	}
	
	public abstract void process( MessageContext context ) throws Exception;

	/**
	 * Vetos processing of the current message. This method just throws a {@link VetoException}.
	 */
	protected void veto() throws VetoException
	{
		throw new VetoException();
	}

	/**
	 * This vetos the current message being processed. This method will take the given message
	 * and turn it into a {@link VetoException}
	 */
	protected void veto( String reason ) throws VetoException
	{
		if( logger.isDebugEnabled() )
			logger.debug( "(veto) " + reason );
		
		throw new VetoException( reason );
	}

	/**
	 * This method will check the {@link PorticoMessage} to see if the kernel is an intended
	 * target of the message. To be a target, the request must have either NO stated target (so
	 * everyone is the target) or the target must have the same handle as the current federate.
	 * If the message is intended for someone else, a {@link VetoException} will be thrown.
	 */
	protected void vetoUnlessForUsOrBroadcast( PorticoMessage request ) throws VetoException
	{
		int target = request.getTargetFederate();
		if( target != PorticoConstants.NULL_HANDLE && target != lrcState.getFederateHandle() )
			throw new VetoException();
	}
	
	/**
	 * This method will check the incoming request to see if the target federate handle is the
	 * same as the handle of the local federate (accessed through the {@link LRCState}).
	 * If the handles differ, a {@link VetoException} will be thrown, ending processing.
	 */
	protected void vetoUnlessForUs( PorticoMessage request ) throws VetoException
	{
		if( request.getTargetFederate() != lrcState.getFederateHandle() )
			throw new VetoException("vetoUnlessForUs(strict)" );
	}
	
	/**
	 * This method will check the source ID of the message and if it is the same as our federate
	 * handle, it will throw a {@link VetoException}, ending processing. This can be used by
	 * handlers if they want to filter out messages that they themselves generated (and thus might
	 * not need to process). 
	 */
	protected void vetoIfMessageFromUs( PorticoMessage request ) throws VetoException
	{
		if( request.getSourceFederate() == lrcState.getFederateHandle() )
			throw new VetoException();
	}
	
	/**
	 * This method will check the source ID of the message and if it ISN'T the same as out federate
	 * handle it will throw a {@link VetoException}, ending processing.
	 */
	protected void vetoUnlessFromUs( PorticoMessage request ) throws VetoException
	{
		if( request.getSourceFederate() != lrcState.getFederateHandle() )
			throw new VetoException( "vetoUnlessFromUs()" );
	}

	/**
	 * Checks to see if the local federate is joined to a federation. If it is not, a VetoException
	 * will be thrown.
	 */
	protected void vetoIfNotJoined() throws VetoException
	{
		if( lrcState.isJoined() == false )
			throw new VetoException( "Can't process message, not currently joined to a federation" );
	}
	
	/**
	 * Checks to see if the local federate knowns about the federate with the given handle. If it
	 * doesn't, a {@link VetoException} is thrown.
	 */
	protected void vetoIfSourceNotJoined( int federateHandle ) throws VetoException
	{
		if( lrcState.getKnownFederate(federateHandle) == null )
			throw new VetoException( "vetoIfSourceNotJoined()" );
	}
	
	/**
	 * This will initialize the various parts of the given instance of the a {@link PorticoMessage}
	 * subclass and return it (so you can chain this call if you want). It will do things like set
	 * the {@link PorticoMessage#setSourceFederate(int) source federate} and
	 * {@link PorticoMessage#setTimestamp(double) timestamp} based on the information from the
	 * {@link LRCState}. Note that the timestamp will only be added if the federate is regulating
	 * (as returned by {@link LRCState#isRegulating()}.
	 */
	protected <T extends PorticoMessage> T fill( T instance )
	{
		instance.setSourceFederate( lrcState.getFederateHandle() );
		// only set the time if it hasn't already been set AND the federate is regulating
		if( (instance.getTimestamp() == PorticoConstants.NULL_TIME) &&
			lrcState.isRegulating() &&
			instance.isSpecDefinedMessage() )
		{
			instance.setTimestamp( lrcState.getCurrentTime() );
		}
		
		return instance;
	}
	
	/**
	 * The same as {@link #fill(PorticoMessage)} except that it allows you to specify a target
	 * federate id for the message.
	 */
	protected <T extends PorticoMessage> T fill( T instance, int target )
	{
		instance.setTargetFederates( target );
		return fill( instance );
	}
	
	/**
	 * This method will take the given message and process it in the incoming message sink. If an
	 * exception is thrown, it will be logged and ignored.
	 */
	protected void reprocessIncoming( PorticoMessage message )
	{
		try
		{
			MessageContext ctx = new MessageContext( message );
			lrc.incoming.process( ctx );
			if( ctx.isSuccessResponse() == false )
				logger.warn( ctx.getErrorResponseException() );
		}
		catch( Exception e )
		{
			logger.warn( e.getMessage(), e );
		}
	}
	
	/**
	 * This method will take the given message and process it in the *outgoing* message sink. If
	 * an exception occurs, it will bubble up and be thrown.
	 */
	protected void reprocessOutgoing( PorticoMessage message ) throws Exception
	{
		MessageContext ctx = new MessageContext( message );
		lrc.outgoing.process( ctx );
		if( ctx.isSuccessResponse() == false )
			logger.warn( ctx.getErrorResponseException() );
	}

	/**
	 * Convenience method that just calls <code>lrcState.getFederateHandle()</code>.
	 */
	protected int federateHandle()
	{
		return lrcState.getFederateHandle();
	}

	/**
	 * Convenience method that calls {@link LRCState#getKnownFederate(int)} passing the given
	 * federate handle. If there is no known federate with the given handle, null is returned.
	 * 
	 * @param federateHandle The federate handle to fetch the name for.
	 * @return The name of the federate with the given handle if it is known, or null if it isn't
	 */
	protected String federateName( int federateHandle )
	{
		Federate federate = lrcState.getKnownFederate( federateHandle );
		if( federate == null )
		{
			if( federateHandle == PorticoConstants.RTI_HANDLE )
				return "RTI";
			else
				return null;
		}
		else
		{
			return federate.getFederateName();
		}
	}
	
	/**
	 * Shortcut to get the name of the local federate.
	 */
	protected String federateName()
	{
		return lrcState.getFederateName();
	}
	
	/**
	 * Convenience method that calls {@link LRCState#getKnownFederate(int)} passing the given
	 * federate handle. If there is no known federate with the given handle, null is returned.
	 * 
	 * @param federateHandle The federate handle to fetch the name for.
	 * @return The type of the federate with the given handle if it is known, or null if it isn't
	 */
	protected String federateType( int federateHandle )
	{
		Federate federate = lrcState.getKnownFederate( federateHandle );
		if( federate == null )
		{
			if( federateHandle == PorticoConstants.RTI_HANDLE )
				return "RTI";
			else
				return null;
		}
		else
		{
			return federate.getFederateType();
		}
	}
	
	/**
	 * Shortcut to get the type of the local federate.
	 */
	protected String federateType()
	{
		return lrcState.getFederateType();
	}
	
	/**
	 * THis method returns a string that contains either the name or the handle of the identified
	 * federate. If logging with names is enabled for federates (see the documentation for
	 * {@link PorticoConstants#isPrintHandlesForFederates()}), the string will contain the federate
	 * name, otherwise it will contain the federate handle.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String moniker( int federateHandle )
	{
		if( PorticoConstants.isPrintHandlesForFederates() )
			return ""+federateHandle;
			//return ""+PorticoConstants.isPrintHandlesForFederates();
		else
			return federateName(federateHandle);
	}

	/**
	 * This is the same as {@link #moniker(int)} except that it assumed the local federate.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String moniker()
	{
		return moniker( federateHandle() );
	}

	/**
	 * Returns the result of calling {@link #moniker(int)} and passing the source federate of the
	 * provided message.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String moniker( PorticoMessage message )
	{
		return moniker( message.getSourceFederate() );
	}

	/**
	 * Returns a string that represents the object instance. If logging with names is enabled
	 * for object instances, the name of the instance will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForObjects()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String objectMoniker( int objectHandle )
	{
		if( PorticoConstants.isPrintHandlesForObjects() )
			return ""+objectHandle;
		else
			return repository.findObjectName( objectHandle );
	}
	
	/**
	 * Returns result of calling {@link #objectMoniker(int)} passing the handle of given instance.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String objectMoniker( OCInstance instance )
	{
		return objectMoniker( instance.getHandle() );
	}
	
	/**
	 * Returns a string that represents the object class. If logging with names is enabled
	 * for object classes, the name of the class will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForObjectClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String ocMoniker( int objectClassHandle )
	{
		if( PorticoConstants.isPrintHandlesForObjectClass() )
		{
			return ""+objectClassHandle;
		}
		else
		{
			String name = fom().getObjectClassName( objectClassHandle );
			if( name == null )
				name = objectClassHandle+" <unknown>";
			return name;
		}
	}
	
	/**
	 * Returns a string that represents the object class. If logging with names is enabled
	 * for object classes, the name of the class will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForObjectClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String ocMoniker( OCMetadata objectClass )
	{
		return ocMoniker( objectClass.getHandle() );
	}

	/**
	 * Returns a string that represents all the attribute handles. If logging with names is
	 * enabled for attributes, the sting will contain the attribute names rather than the handles
	 * (see {@link PorticoConstants#isPrintHandlesForAttributeClass()})
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String acMoniker( Set<Integer> attributeHandles )
	{
		ArrayList<String> attributes = new ArrayList<String>();
		if( PorticoConstants.isPrintHandlesForAttributeClass() )
		{
			for( int handle : attributeHandles )
				attributes.add( ""+handle );
		}
		else
		{
			for( int handle : attributeHandles )
				attributes.add( fom().findAttributeName(handle) );
		}

		return attributes.toString();
	}
	
	/**
	 * Returns a string that represents all the attribute handles. If logging with names is
	 * enabled for attributes, the sting will contain the attribute names rather than the handles
	 * (see {@link PorticoConstants#isPrintHandlesForAttributeClass()})
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String acMoniker( int... attributeHandles )
	{
		ArrayList<String> attributes = new ArrayList<String>();
		if( PorticoConstants.isPrintHandlesForAttributeClass() )
		{
			for( int handle : attributeHandles )
				attributes.add( ""+handle );
		}
		else
		{
			for( int handle : attributeHandles )
				attributes.add( fom().findAttributeName(handle) );
		}

		return attributes.toString();
	}

	/**
	 * Returns a printable string for the given set of attributes that also includes the
	 * size of the values provided in brackets after the name. For example [name(14b),other(4b)].
	 * <p/>
	 * The names are only substituted for handles if set to do so in the FOM.
	 * (see {@link PorticoConstants#isPrintHandlesForAttributeClass()})
	 */
	protected String acMonikerWithSizes( Map<Integer,byte[]> attributes )
	{
		ArrayList<String> printable = new ArrayList<>();
		final boolean printHandles = PorticoConstants.isPrintHandlesForAttributeClass();
		for( Integer handle : attributes.keySet() )
		{
			int size = attributes.get(handle).length;
			if( printHandles )
				printable.add( handle+"("+size+"b)" );
			else
				printable.add( fom().findAttributeName(handle)+"("+size+"b)" );
		}
		
		return printable.toString();
	}
	
	/**
	 * Returns a printable string for the given set of parameters that also includes the
	 * size of the values provided in brackets after the name. For example [name(14b),other(4b)].
	 * <p/>
	 * The names are only substituted for handles if set to do so in the FOM.
	 * (see {@link PorticoConstants#isPrintHandlesForAttributeClass()})
	 */
	protected String pcMonikerWithSizes( Map<Integer,byte[]> parameters )
	{
		ArrayList<String> printable = new ArrayList<>();
		final boolean printHandles = PorticoConstants.isPrintHandlesForParameterClass();
		for( Integer handle : parameters.keySet() )
		{
			int size = parameters.get(handle).length;
			if( printHandles )
				printable.add( handle+"("+size+"b)" );
			else
				printable.add( fom().findParameterName(handle)+"("+size+"b)" );
		}

		return printable.toString();
	}
	
	/**
	 * Returns a string that represents the interaction class. If logging with names is enabled
	 * for interaction classes, the name of the class will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForInteractionClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String icMoniker( int interactionClassHandle )
	{
		if( PorticoConstants.isPrintHandlesForInteractionClass() )
		{
			return ""+interactionClassHandle;
		}
		else
		{
			String name = fom().getInteractionClassName( interactionClassHandle );
			if( name == null )
				name = interactionClassHandle+" <unknown>";
			return name;
		}
	}

	/**
	 * Returns a string that represents all the parameters. If logging with names is enabled
	 * for parameter classes, the names of the parameters will be in the returned string, if not,
	 * the handles will be in the returned string. See
	 * {@link PorticoConstants#isPrintHandlesForParameterClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String pcMoniker( Set<Integer> parameterHandles )
	{
		ArrayList<String> attributes = new ArrayList<String>();
		if( PorticoConstants.isPrintHandlesForParameterClass() )
		{
			for( int handle : parameterHandles )
				attributes.add( ""+handle );
		}
		else
		{
			for( int handle : parameterHandles )
				attributes.add( fom().findParameterName(handle) );
		}

		return attributes.toString();
	}

	/**
	 * Returns a string that represents the space. If logging with names is enabled
	 * for spaces, the name of the space will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForObjectClass()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String spaceMoniker( int spaceHandle )
	{
		if( PorticoConstants.isPrintHandlesForSpaces() )
		{
			return ""+spaceHandle;
		}
		else
		{
			Space space = fom().getSpace( spaceHandle );
			if( space == null )
				return spaceHandle+" <unknown>";
			else
				return space.getName();
		}
	}
	
	/**
	 * Returns a string that represents the dimension. If logging with names is enabled
	 * for dimensions, the name of the dimsneion will be returned, if not, the handle will
	 * be in the returned string. See {@link PorticoConstants#isPrintHandlesForDimensions()}.
	 * <p/>
	 * <b>NOTE:</b> These methods are used to provide some consistency with the way various entities
	 *              are logged. Using the xxmoniker() methods allows the user to define whether to
	 *              use handle or names as a configuration option. 
	 */
	protected String dimensionMoniker( int dimensionHandle )
	{
		if( PorticoConstants.isPrintHandlesForDimensions() )
			return ""+dimensionHandle;
		
		for( Space space : fom().getAllSpaces() )
		{
			if( space.hasDimension(dimensionHandle) )
				return space.getDimension(dimensionHandle).getName();
		}
		
		return dimensionHandle+" <unknown>";
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Time Management Helper Methods /////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Shortcut to get the {@link TimeStatus} object for the current federate.
	 */
	protected TimeStatus timeStatus()
	{
		return lrcState.getTimeStatus();
	}
	
	/**
	 * This method will queue up a dummy {@link TimeAdvanceRequest} that will force the LRC
	 * to reconsider whether any federates can issued time advance grants or not. Note that
	 * this dummy is never broadcast out over the network. It exists soley for inernal LRC
	 * use in situations like this.
	 */
	protected void queueDummyAdvance()
	{
		lrcState.getQueue().offer( new TimeAdvanceRequest() );
		if( logger.isDebugEnabled() )
			logger.debug( "Queued dummy TimeAdvanceReqeust for federate ["+moniker()+"]" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// FOM Helper Methods ///////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Search the FOM for the class with the given class handle and return it. Throw an exception
	 * if the class cannot be found.
	 */
	protected OCMetadata getObjectClass( int classHandle ) throws JObjectClassNotDefined
	{
		OCMetadata theClass = lrcState.getFOM().getObjectClass( classHandle );
		if( theClass == null )
			throw new JObjectClassNotDefined( "Class [" + classHandle + "] not found in FOM" );
		else
			return theClass;
	}
	
	/**
	 * Quick way to write {@link LRCState#getFOM()}
	 */
	protected ObjectModel fom()
	{
		return lrcState.getFOM();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
