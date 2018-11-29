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
package org.portico2.rti.services;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.Space;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.PorticoConstants;
import org.portico2.common.messaging.IMessageHandler;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.ddm.data.RegionStore;
import org.portico2.common.services.ownership.data.OwnershipManager;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.federation.FederationManager;
import org.portico2.rti.services.mom.data.MomManager;
import org.portico2.rti.services.object.data.Repository;
import org.portico2.rti.services.sync.data.SyncPointManager;
import org.portico2.rti.services.time.data.TimeManager;

public abstract class RTIMessageHandler implements IMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected FederationManager federationManager;
	protected Federation federation;
	protected Logger logger;
	
	protected SyncPointManager syncManager;
	protected InterestManager  interests;
	protected Repository       repository;
	protected RegionStore      regionStore2;
	protected TimeManager      timeManager;
	protected OwnershipManager ownership;
	protected MomManager       momManager;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * @return The class name of this handler (final part, not qualified).
	 */
	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}

	/**
	 * Extracts some key information from the given properties, such as a referce to the
	 * {@link Federation} that it is contained in. Caches a number of important objects in
	 * protected member variable so that the handlers can just refer to them without needing
	 * any special setup. 
	 */
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		if( properties == null )
			throw new JConfigurationException( "Cannot initialize LRC from null property set" );

		this.federation = (Federation)properties.get( IMessageHandler.KEY_RTI_FEDERATION );
		this.logger        = federation.getLogger();
		this.syncManager   = federation.getSyncPointManager();
		this.interests     = federation.getInterestManager();
		this.repository    = federation.getRepository();
		this.regionStore2  = federation.getRegionStore();
		this.timeManager   = federation.getTimeManager();
		this.ownership     = federation.getOwnershipManager();
		this.momManager    = federation.getMomManager();
	}
	
	/**
	 * A message has been received for processing. The request and/or response is contained within
	 * the given {@link MessageContext} object. Take appropriate action and throw any exception
	 * from the compatibility library you need to.
	 * 
	 * @param context The request and/or response holder
	 * @throws JException If there is a problem processing the message
	 */
	@Override
	public abstract void process( MessageContext context ) throws JException;

	/////////////////////////////////////////////////////////////////////////////////////////
	///  Helper Methods  ////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	protected final String federationName()
	{
		return federation.getFederationName();
	}
	
	protected final int federationHandle()
	{
		return federation.getFederationHandle();
	}

	protected final ObjectModel fom()
	{
		return federation.getFOM();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	///  Message Sender Helper Methods  /////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Queue the given control message as one that should be broadcast to all federates
	 */
	protected final void queueBroadcast( PorticoMessage message )
	{
		message.setSourceFederate( PorticoConstants.RTI_HANDLE );
		message.setTargetFederates( PorticoConstants.TARGET_ALL_HANDLE );
		federation.queueControlMessage( message );
	}

	/**
	 * Queue the given control messages for the given federate
	 * 
	 * @param message The message to send
	 * @param federateHandle The federate this message is targeted at
	 */
	protected final void queueUnicast( PorticoMessage message, int federateHandle )
	{
		message.setSourceFederate( PorticoConstants.RTI_HANDLE );
		message.setTargetFederates( federateHandle );
		federation.queueControlMessage( message );
	}

	/**
	 * Queue the given control message and set its target as the given set of federate handles
	 * 
	 * @param message The message to queue for sending
	 * @param federateHandles The handles of the federates that this message is targeted at
	 */
	protected final void queueManycast( PorticoMessage message, int... federateHandles )
	{
		// flag the message as from the RTI
		message.setSourceFederate( PorticoConstants.RTI_HANDLE );
		
		// if the target federates are null or empty, this is effectively a broadcast
		if( federateHandles == null || federateHandles.length == 0 )
		{
			message.setTargetFederates( PorticoConstants.TARGET_ALL_HANDLE );
			queueBroadcast( message );
			return;
		}
		else
		{
    		// if we have a set of target federates, we need to queue individual messages
    		message.setTargetFederates( federateHandles );
    		federation.queueControlMessage( message );
		}
	}
	
	/**
	 * Queue the given control message and set its target as the given set of federate handles.
	 * The source federate for the message will be preserved unless it has not been set, in which
	 * case it will be set to {@link PorticoConstants#RTI_HANDLE}.
	 * 
	 * @param message The message to queue for sending
	 * @param federateHandles The handles of the federates that this message is targeted at
	 */
	protected final void queueManycast( PorticoMessage message, Set<Integer> federateHandles )
	{
		// flag the message as from the RTI unless its already explictly set as something else
		if( message.getSourceFederate() == PorticoConstants.NULL_HANDLE )
			message.setSourceFederate( PorticoConstants.RTI_HANDLE );
		
		// if the target federates are null or empty, this is effectively a broadcast
		if( federateHandles == null || federateHandles.isEmpty() )
		{
			message.setTargetFederates( PorticoConstants.TARGET_ALL_HANDLE );
			queueBroadcast( message );
			return;
		}
		else
		{
    		// if we have a set of target federates, we need to queue individual messages
    		message.setTargetFederates( federateHandles );
    		federation.queueControlMessage( message );
		}
	}

	/**
	 * This method will fill out the given {@link PorticoMessage} with all the information it
	 * requires, such as source and target federates, timestampping and other details.
	 */
	protected <T extends PorticoMessage> T fill( T message, int senderHandle )
	{
		Federate sender = federation.getFederate( senderHandle );
		message.setSourceFederate( senderHandle );
		
		
		// only set the time if it hasn't already been set AND the federate is regulating
		if( (message.getTimestamp() == PorticoConstants.NULL_TIME) &&
			timeManager.isRegulating( senderHandle ) &&
			message.isSpecDefinedMessage() )
		{
			message.setTimestamp( timeManager.getCurrentTime(senderHandle) );
		}
		
		return message;
	}
	
	/**
	 * The same as {@link #fill(PorticoMessage)} except that it allows you to specify a target
	 * federate id for the message.
	 */
//	protected <T extends PorticoMessage> T fill( T instance, int target )
//	{
//		instance.setTargetFederates( target );
//		return fill( instance );
//	}

	/**
	 * This method will queue up a dummy {@link TimeAdvanceRequest} that will force the RTI
	 * to reconsider whether any federates can issued time advance grants or not. Note that
	 * this dummy is never broadcast out over the network. It exists soley for internal RTI
	 * use in situations like this.
	 */
	protected void queueDummyAdvance()
	{
		if( logger.isDebugEnabled() )
			logger.debug( "Process dummy TimeAdvanceReqeust" );
		MessageContext context = new MessageContext( new TimeAdvanceRequest() );
		federation.getIncomingSink().process( context );
		//lrcState.getQueue().offer( new TimeAdvanceRequest() );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	///  Handle and Name Methods  ///////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Convenience method that returns the name of the federate that has the given handle.
	 * If there is no known federate with the given handle, null is returned.
	 * 
	 * @param federateHandle The federate handle to fetch the name for.
	 * @return The name of the federate with the given handle if it is known, or null if it isn't
	 */
	protected String federateName( int federateHandle )
	{
		Federate federate = federation.getFederate( federateHandle );
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
	 * Convenience method that returns the type of the federate that has the given handle.
	 * If there is no known federate with the given handle, null is returned.
	 * 
	 * @param federateHandle The federate handle to fetch the name for.
	 * @return The type of the federate with the given handle if it is known, or null if it isn't
	 */
	protected String federateType( int federateHandle )
	{
		Federate federate = federation.getFederate( federateHandle );
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
			return "unknown"; // TODO FIXME
//			return repository.findObjectName( objectHandle );
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
	protected String acMonkier( Set<ACInstance> attributes )
	{
		if( attributes == null )
			return "";
		
		ArrayList<String> strings = new ArrayList<String>();
		if( PorticoConstants.isPrintHandlesForAttributeClass() )
		{
			for( ACInstance attribute : attributes )
				strings.add( ""+attribute.getHandle() );
		}
		else
		{
			for( ACInstance attribute : attributes )
				strings.add( ""+attribute.getType().getName() );
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
	protected String acMoniker( Set<Integer> attributeHandles )
	{
		if( attributeHandles == null )
			return "";

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

	/**
	 * Checks whether the federate is a member of the context federation, and if not throws a 
	 * {@link JFederateNotExecutionMember} exception 
	 */
	protected void checkIsMember( int federate ) throws JFederateNotExecutionMember
	{
		if( !federation.containsFederate( federate ) )
			throw new JFederateNotExecutionMember();
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
