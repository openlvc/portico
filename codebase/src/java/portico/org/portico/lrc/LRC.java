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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.bindings.IConnection;
import org.portico.bindings.jgroups.JGroupsConnection;
import org.portico.bindings.jvm.JVMConnection;
import org.portico.container.Container;
import org.portico.impl.HLAVersion;
import org.portico.impl.ISpecHelper;
import org.portico.lrc.compat.JConcurrentAccessAttempted;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.notifications.NotificationManager;
import org.portico.lrc.notifications.Priority;
import org.portico.utils.ObjectFactory;
import org.portico.utils.messaging.AbstractMessageHandler;
import org.portico.utils.messaging.ErrorResponse;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageSink;
import org.portico.utils.messaging.Module;
import org.portico.utils.messaging.PorticoMessage;
import org.portico.utils.messaging.ResponseMessage;


/**
 * The LRC is the nerve-centre of an HLA federate. It provides the local runtime library that
 * federate code connects to in order to communicate with other federates in the federation. Each
 * LRC contains the necessary messaging infrastructure and a reference to a communications binding
 * implementation.
 * <p/>
 * <b>LRC State</b>
 * <p/>
 * Each LRC requires a bunch of state management entities to both cache the state of remote
 * federates and to keep track of what is happening locally. These entities are aggregated
 * together under the {@link LRCState} which is contained inside each LRC.
 * 
 * <p/>
 * <b>Messaging Infrastructure</b>
 * <p/>
 * Each LRC contains two {@link MessageSink}s. One for messages triggered by the local federate and
 * needing sending to the federation (the outgoing sink) and one for processing messages received
 * from other federates in the federation. Outgoing messages are passed directly to the outgoing
 * sink for processing. From here the handlers can process them, and if necessary, pass them on to
 * the connection for sending to the rest of the federation. When an {@link IConnection} receives a
 * message, it should first be put into a message queue (stored in the LRCState). When the federate
 * calls tick (resulting in one of the tick() methods being called on the LRC), messages are
 * extracted from the queue and passed into the incoming sink.
 * 
 * <p/>
 * <b>Specification Helpers and keeping the LRC HLA-Version Generic</b>
 * <p/>
 * Each specification has its own distinct FederateAmbassador interface. This presents a problem
 * for a "generic" component like the LRC, which we want to remain ignorant to the specific HLA
 * version in use. We get around this problem in a couple of ways.
 * <p/>
 * Firstly, each LRC can have an {@link ISpecHelper} implementation attached to it. ISpecHelper is
 * just a marker interface and each HLA version interface should have its own implementaiton of it.
 * The typical process is to create a class that implements this interface, but to which you can
 * attached the HLA-version specific FederateAmbassador. An instance of this class is then stored
 * with the LRC (as the rather opaque ISpecHelper type). Message handlers that are used for
 * callbacks can then get access to the spec helper through the LRC and cast it to its actual
 * HLA-version type (as those handlers are also specific to that HLA version), thus giving them
 * access to the appropriate FederateAmbassador (or any other version-specific data you choose to
 * include).
 * <p/>
 * ISpecHelper defines only a single method: {@link ISpecHelper#getHlaVersion()}. When the LRC
 * configures its {@link MessageSink}s, it ensures that only those handlers declaring the
 * appropriate keyword (hla13 for the HLA 1.3 specification, etc...) are added to the local sink.
 * This way, only those handlers that declare themselves to be capable of processing messages for
 * a specific HLA interface are added to the sinks if that interface is in use.
 * <p/>
 * <i>For those interested, the above trick is achieved through the
 * {@link org.portico.utils.messaging.Module}.apply() suite of methods. These methods ensure that
 * only those handlers declaring a provided keyword are added to all the MessageSinks they are
 * given<i/>
 * <p/>
 * For each HLA interface, there is a separate suite of callback handlers. These handlers take
 * particular messages and turn them into FederateAmbassador callbacks (naturally, each is specific
 * to a particular HLA interface version). For example, callback handlers that work for IEEE-1516
 * should provide the keyword defined in the static property {@link PorticoConstants#KEYWORD_LRC13}.
 * This way, when an LRC is created for 1516, only those message handlers that define this keyword
 * will be added to the incoming/outgoing sinks. If the spec helper returns
 * {@link HLAVersion#HLA13}, then the LRC will pass the keyword
 * {@link PorticoConstants#KEYWORD_LRC13} during messaging configuration.
 */
public class LRC
{
	// NOTE: Load up the container if it isn't already active (this will also load the RID)
	static
	{
		Container.instance();
	}

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Logger logger;
	private boolean isStarted;
	
	// Messaging Infrastructure Information //
	protected MessageSink outgoing;
	protected MessageSink incoming;
	private Map<String,Object> initializationProperties;
	private IConnection connection;
	
	// Notification Information //
	private NotificationManager notificationManager;
	
	// State Information //
	protected ISpecHelper specHelper;
	protected LRCState state;
	
	// Callback Processing //
	private Thread immediateCallbackDispatcher; 

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new LRC using the given specification helper. This method will initialize the
	 * contained message sinks and create/initialize the contained IConnection.
	 * 
	 * @param specHelper This allows an aribtrary piece of code to be attched to the LRC that can
	 *                   be used in callbacks that are specific to an HLA version. Each version has
	 *                   its own specification helper, stored in the LRC. The callback handlers for
	 *                   each HLA version are expected to supply the appropriate keywords to make
	 *                   sure that they only get applied when being attached to an LRC of the same
	 *                   version. They can then get the spec helper from the LRC and cast it to the
	 *                   proper type (thus giving them access to the proper FederateAmbassador).
	 */
	public LRC( ISpecHelper specHelper ) throws JConfigurationException
	{
		// set up whatever else we need to before the handlers are created and initialized
		this.specHelper = specHelper;
		this.logger = LogManager.getFormatterLogger( "portico.lrc" );
		this.logger.debug( "Creating new LRC" );
		this.logger.debug( "Portico version: "+PorticoConstants.RTI_VERSION );
		this.logger.debug( "Interface: "+specHelper.getHlaVersion() );

		// create the notification manager
		this.notificationManager = NotificationManager.newNotificationManager();
		
		// the immediate callback processing remains null until turned on explictly
		this.immediateCallbackDispatcher = null;

		// create the LRCState component that has most of the state-holding components inside it
		this.state = new LRCState( this );
		
		// register the state as a Notification handler so that it can take the appropriate
		// actions when the location federate joins and resigned from a federation. We only
		// want to do this once, so we don't put it in initializeLrc() as that will get called
		// when we re-join a new federation. Also, we have to register it this way so the 
		// NotificationManager doesn't try to instantiate it!
		this.notificationManager.addListener( Priority.LOW, this.state );
		
		// initialize the parts of the LRC that should be re-initialized whenever the
		// federate attached to it resigns and rejoins
		initializeLrc();
		
		// register the LRC with the container
		Container.instance().registerLrc( this );
	}
	
	/**
	 * So that the messaging components can be properly reinitialized when a federate resigns from
	 * a federation and then joins another (the same LRC) this method contains all the logic to
	 * reinitialize the appropriate internal LRC components. This will wipe out the existing
	 * message sinks and reconfigure them. The connection will remain untouched.
	 */
	private void initializeLrc() throws JConfigurationException
	{
		this.incoming = new MessageSink( "incoming" );
		this.outgoing = new MessageSink( "outgoing" );
		this.incoming.setDefaultHandler( new DefaultMessageHandler() );

		// set up the initizliation properties that will be used both by the connection and
		// messaging frameworks during setup
		this.initializationProperties = new HashMap<String,Object>();
		this.initializationProperties.putAll( PorticoConstants.getSystemPropertiesAsMap() );
		this.initializationProperties.put( LRCProperties.KEY_LRC, this );

		// initialize the connection - but only if we haven't done so before
		if( connection == null )
			initializeConnection( initializationProperties );
		
		// initialize the messaging framework
		initializeMessaging( initializationProperties );
		
		logger.info( "LRC initialized (HLA version: "+specHelper.getHlaVersion()+")" );
		
		// now that we're ready, open the connection so that we can start processing messages
		logger.info( "Opening LRC Connection" );
		startLrc();
	}
	
	/**
	 * Configures the messaging framework for this LRC. This method will get all the messaging
	 * modules from the container and apply each of them in turn to the message sinks inside the
	 * LRC. It will also pass the appropriate component ID depending on which HLA interface is
	 * in use.
	 */
	private void initializeMessaging( Map<String,Object> initializationProperties )
		throws JConfigurationException
	{
		// get the component name from the spec helper
		String component = "";
		if( specHelper.getHlaVersion() == HLAVersion.HLA13 )
			component = PorticoConstants.KEYWORD_LRC13;
		else if( specHelper.getHlaVersion() == HLAVersion.JAVA1 )
			component = PorticoConstants.KEYWORD_LRCJAVA1;
		else if( specHelper.getHlaVersion() == HLAVersion.IEEE1516 )
			component = PorticoConstants.KEYWORD_LRC1516;
		else if( specHelper.getHlaVersion() == HLAVersion.IEEE1516e )
			component = PorticoConstants.KEYWORD_LRC1516e;
		
		// apply the modules to the contained sinks
		logger.trace( "Applying modules using component keyword: " + component );
		for( Module module : Container.instance().getHandlerRegistry().getAllModules() )
		{
			logger.trace( "STARTING Apply module [" +module.getName()+ "] to LRC" );
			
			// apply the module to the two sinks using the component name as the only keyword
			Set<Class<?>> appliedHandlers = module.apply( new MessageSink[]{incoming,outgoing},
			                                              new String[]{component},
			                                              initializationProperties );
			
			if( logger.isTraceEnabled() )
			{
				logger.trace( "Applied ["+appliedHandlers.size()+"/"+
				              module.getAllHandlers().size()+"] handlers" );
			}
		}
		
		logger.debug( "Messaging framework configuration complete" );
	}
	
	/**
	 * This method will determine the connection class to use (through the
	 * {@link #getConnectionImplementation()} method) and then will instantiate and configure it.
	 */
	private void initializeConnection( Map<String,Object> initializationProperties )
		throws JConfigurationException
	{
		Class<? extends IConnection> connectionClass = getConnectionImplementation();
		if( connectionClass == null )
			throw new JConfigurationException( "No connection class defined" );
		
		// connection class has been provided, create and store the connection
		try
		{
			logger.trace( "ATTEMPT create IConnection, class= " + connectionClass );
			this.connection = ObjectFactory.create( connectionClass, IConnection.class );
			logger.trace( "SUCCESS created IConnection, class= " + connectionClass );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Can't create kernel: problem creating connection", e );
		}
		
		// configure the connection
		this.connection.configure( this, initializationProperties );
	}
	
	/**
	 * This method will look at the system property {@link PorticoConstants#PROPERTY_CONNECTION} 
	 * see if a custom LRC connection has been specified. If one has not, the default implementation
	 * as defined in {@link PorticoConstants#CONNECTION_DEFAULT_IMPL} will be used. If one has, this
	 * method will attempt to turn that value into the Class that represents the connection
	 * implementation. The value should be specified as the fully qualified name of the class the
	 * implements {@link IConnection}. However, for common connection types, an alias can be
	 * provided. Currently supported aliases are:
	 * <ul>
	 *   <li>"jvm" for {@link JVMConnection}</li>
	 *   <li>"jgroups" for {@link org.portico.bindings.jgroups.JGroupsConnection JGroups Connection}
	 *       (the default)</li>
	 * </ul>
	 * 
	 * If the class specified in the property cannot be found, or it doesn't implement the
	 * {@link IConnection} interface, an exception will be thrown.
	 */
	private Class<? extends IConnection> getConnectionImplementation() throws JConfigurationException
	{
		// check to see if the property have been provided
		String property = System.getProperty( PorticoConstants.PROPERTY_CONNECTION,
		                                      PorticoConstants.CONNECTION_DEFAULT_IMPL );
		
		logger.trace( "Provided connection implementation is \""+property+"\"" );
		
		// check to see if we have one of the predefined aliases
		if( property.equalsIgnoreCase("jvm") )
			return JVMConnection.class;
		else if( property.equalsIgnoreCase("jgroups") )
			return JGroupsConnection.class;
		
		// we don't have one of the aliases, try and find the class and load it
		logger.trace( "Trying to load connection class: " + property );
		try
		{
			Class<?> clazz = Class.forName( property );
			if( IConnection.class.isAssignableFrom(clazz) == false )
				throw new JRTIinternalError( "Class doesn't implement IConnection: " + property );
			
			return clazz.asSubclass( IConnection.class );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Problem locating connection class: " + property, e );
		}
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This should be called when a federate resigned from a federation. It will clear out all
	 * the existing federation state and reinitialize the messaging infrastructure. Note that this
	 * will have no effect on the connection.
	 */
	public void reinitialize()
	{
		// reinitialize the state
		state.reinitialize();
		
		// reinitialize the kernel
		initializeLrc();
	}
	
	/**
	 * Call this when the LRC is ready to begin processing. It will cause the IConnection to
	 * establish its connection to the federation.
	 */
	public void startLrc()
	{
		if( this.isStarted() )
			return;
		
		try
		{
			this.connection.connect();
		}
		catch( JRTIinternalError rtie )
		{
			logger.error( "Error starting LRC: " + rtie.getMessage(), rtie );
			throw new RuntimeException( rtie );
		}
		
		this.isStarted = true;
	}
	
	/**
	 * Call this when the LRC is finished processing. It will cause the IConnection to
	 * disconnect from the federation.
	 */
	public void stopLrc()
	{
		if( this.isStarted() == false )
			return;

		try
		{
			this.connection.disconnect();
		}
		catch( JRTIinternalError rtie )
		{
			logger.error( "Error stopping LRC: " + rtie.getMessage(), rtie );
			throw new RuntimeException( rtie );
		}
		
		this.isStarted = false;
	}

	public boolean isStarted()
	{
		return this.isStarted;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Basic Get and Set Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ISpecHelper getSpecHelper()
	{
		return this.specHelper;
	}
	
	public Logger getLrcLogger()
	{
		return this.logger;
	}

	public LRCState getState()
	{
		return this.state;
	}
	
	public IConnection getConnection()
	{
		return this.connection;
	}
	
	public NotificationManager getNotificationManager()
	{
		return this.notificationManager;
	}

	public MessageSink getOutgoingSink()
	{
		return this.outgoing;
	}
	
	public MessageSink getIncomingSink()
	{
		return this.incoming;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Tick Processing Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Used in the HLA 1.3 interfaces (and java1): tick()
	 * <p/>
	 * Just calls {@link #tickUntilEmpty(long)}, passing {@link LRCProperties#LRC_TICK_TIMEOUT}
	 */
	public void tick() throws JRTIinternalError, JConcurrentAccessAttempted
	{
		tickUntilEmpty( LRCProperties.LRC_TICK_TIMEOUT );
	}

	/**
	 * This method will process *ALL* remaining messages currently in the queue before it returns.
	 * The number of messages it processed will be returned. If there are no messages in the queue
	 * when it FIRST polls, it will wait for at most <code>nanoWait</code> nano-seconds for one to
	 * turn up. This is only done for the first check. If a message is available or turns up, that
	 * message will be processed and should the queue be empty after that, the method will return. 
	 * 
	 * @param nanoWait Number of nanoseconds to wait for the first message to appear in the queue
	 * @return The number of messages that were processed
	 * @throws JRTIinternalError
	 */
	public int tickUntilEmpty( long nanoWait ) throws JRTIinternalError, JConcurrentAccessAttempted
	{
		// don't process anything if callbacks aren't enabled
		if( state.areCallbacksEnabled() == false )
			return 0;
		
		// check for a concurrent access issue
		state.checkAccess();
		
		// signal that we are not ticking
		state.setTicking( true );

		// On the first iteration, we don't check to see if there are any messages, so
		// the first poll() might block. However, after that, the loop will only continue
		// if the queue isn't empty, so although we use the blocking version of poll() we
		// won't actually have to wait
		int processedCount = 0;
		try
		{
    		do
    		{
    			PorticoMessage message = state.messageQueue.poll( nanoWait );
    			if( message == null )
    				return processedCount;
    			
    			// process the message
    			tickProcess( message );
    			++processedCount;
    		}
    		while( state.messageQueue.isEmpty() == false );
        }
        finally
        {
        	// reset the ticking status flag, this will always execute, even in the case
        	// of an InterruptedException (which returns right away)
        	state.setTicking( false );
        }

		return processedCount;
	}

	/**
	 * Used in the HLA 1.3 interfaces (and java1): tick(min,max)
	 * <p/>
	 * Used in the HLA 1516 interface: evokeMultipleCallbacks(min,max)
	 * <p/>
	 * 
	 * Process incoming messages for a period of time no shorter than the given <code>min</code>
	 * time and no greater than the given <code>max</code> time.
	 * <p/>
	 * <b>Note:</b> This implementation could continue to process messages beyond the given
	 * <code>max</code> value. If the method starts processing a message just before the max time
	 * would be up, that processing could take up long enough to go beyond the max time. No more
	 * messages would be processed after it, but processing could still run beyond the max time.
	 * 
	 * @return Return true if there are more messages waiting to be processed, false otherwise
	 */
	public boolean tick( double minSeconds, double maxSeconds )
		throws JRTIinternalError, JConcurrentAccessAttempted
	{
		// check to make sure people aren't playing funny buggers
		if( minSeconds == 0.0 && maxSeconds == 0.0 )
		{
			throw new JRTIinternalError( "OK, so you called tick(min,max) with 0.0 for both "+
			                             "values. Do we need to talk about how this works?" );
		}

		// don't process anything if callbacks aren't enabled
		if( state.areCallbacksEnabled() == false )
			return true;
		
		// check for a concurrent access issue
		state.checkAccess();

		// signal that we are not ticking
		state.setTicking( true );

		try
		{
			// figure out how long we can tick for under min/max settings
			long startTime = System.currentTimeMillis();
			long minTime = startTime + (long)(minSeconds*1000);
			long maxTime = startTime + (long)(maxSeconds*1000);

			// poll until AT LEAST the minimum time
			Timestamp minimumTimestamp = new Timestamp( minTime );
			while( System.currentTimeMillis() < minTime )
			{
				PorticoMessage message = state.messageQueue.pollUntil( minimumTimestamp );
				if( message == null )
					break;
				else
					tickProcess( message );
			}

			// the min time has now passed, keep processing messages until either
			// the max time expires, or we run out of tasks
			while( System.currentTimeMillis() < maxTime )
			{
				PorticoMessage message = state.messageQueue.poll();
				if( message == null )
					return false;
				else
					tickProcess( message );
			}
			
			// return that there are more messaegs, this could technically be a lie if the message
			// we processed before time ran out was the last message available, but it is likely
			// that there is more work and that if there had been none we'd have gotten back null
			// from poll() and returned false from within the above while()
			return true;
		}
		finally
		{
			state.setTicking( false );
		}
	}
	
	/**
	 * This tick version is used to help implement the HLA flush-queue-request. It will drain the
	 * LRC queue of all its RO messages and all TSO messages up to the specified maxTime and then
	 * process them.
	 * <p/>
	 * The value returned from the this method is either <code>maxTime</code> or the timestamp of
	 * the next TSO message in the queue, whichever is lower.
	 */
	public double tickFlush( double maxTime ) throws JRTIinternalError, JConcurrentAccessAttempted
	{
		// don't process anything if callbacks aren't enabled
		if( state.areCallbacksEnabled() == false )
		{
			PorticoMessage next = state.messageQueue.peekTSO();
			if( next != null && (next.getTimestamp() < maxTime) )
				return next.getTimestamp();
			else
				return maxTime;
		}

		// check for a concurrent access issue
		state.checkAccess();

		// signal that we are not ticking
		state.setTicking( true );

		try
		{
			// Note: If there is an exception, this method will exit and we will lose any
			//       unprocessed messages because they've already come out of the queue but
			//       will never get processed due to the exception. Not sure what to do about
			//       this at the moment, will fix it up later if it ever becomes an issue.
			List<PorticoMessage> messages = state.messageQueue.flush( maxTime );
			for( PorticoMessage message : messages )
			{
				tickProcess( message );
			}
			
			// return the smaller of the timestamp of the next TSO message of maxTime. This should
			// be used as the grant time for the federate that just flushed
			PorticoMessage next = state.messageQueue.peekTSO();
			if( next != null && (next.getTimestamp() < maxTime) )
				return next.getTimestamp();
			else
				return maxTime;
		}
		finally
		{
			state.setTicking( false );
		}
	}
	
	/**
	 * Used by the 1516 interface in: evokeCallback()
	 * 
	 * Try and process a single incoming message. Once a single message has been processed, this
	 * method will return. If there are no messages waiting for attention, wait at most as long
	 * we the provided wait time (in SECONDS) before giving up. Return <code>true</code> if there
	 * are more messages waiting to be processed, <code>false</code> if there are none left.
	 * 
	 * @param wait The time to wait for a single message to be received (in seconds)
	 * @return True if there are more messages waiting to be processed, false otherwise
	 */
	public boolean tickSingle( double wait ) throws JRTIinternalError, JConcurrentAccessAttempted
	{
		if( state.areCallbacksEnabled() == false )
			return !state.messageQueue.isEmpty();

		// check for a concurrent access issue
		state.checkAccess();
		
		// signal that we are not ticking
		state.setTicking( true );
		
		// tick for one message, waiting as long as the given wait time
		try
		{
			// fetch a single incoming message, waiting only as long as we are given
			long timeout = (long)wait;
			PorticoMessage message = state.messageQueue.poll( timeout*1000*1000000 );
			
			// process the message if there is one
			if( message != null )
				tickProcess( message );
		}
		finally
		{
			// reset the ticking status flag
			state.setTicking( false );
		}
		
		// return info about any more available messages
		return !state.messageQueue.isEmpty();
	}
	
	/**
	 * Passes the given message to the incoming sink (wrapped up in a context) for processing. If
	 * there is no response message filled out by the sink, a {@link JRTIinternalError} will be
	 * thrown. If the processing causes an exception, that exception will be logged, but NOT thrown.
	 * See PORT-337 for more information in why that is the case.
	 */
	private void tickProcess( PorticoMessage message ) throws JRTIinternalError
	{
		// pass the message to the callback sink //
		try
		{
			MessageContext context = new MessageContext( message );
			incoming.process( context );
			
			// check the result //
			ResponseMessage response = context.getResponse();
			if( response == null )
			{
				throw new JRTIinternalError( "No response from LRC incoming sink for message [" +
					context.getRequest().getClass().getCanonicalName() + "]" );
			}
			else if( response.isError() )
			{
				throw new JRTIinternalError( ((ErrorResponse)response).getCause() );
			}
		}
		catch( Exception e )
		{
			// log a message if there was a problem, but DON'T throw the exception onwards
			// for an explanation of why, see PORT-337
			//throw new JRTIinternalError( e.getMessage(), e );
			logger.error( "FAILURE Exception encountered while processing incoming message: " +
			              e.getMessage(), e ); 
		}
	}

	/**
	 * The IEEE-1516 and 1516e standards provide facilities to allow the immediate delivery
	 * of callback messages rather than the usual asynchronous/tick delivery mechanism. To
	 * provide support for this, when the mode is enabled the LVCQueue itself will have an
	 * additional thread that will be used to deliver all callbacks immediately, rather than
	 * waiting for tick to be called (although we'll extract callbacks via the same poll()
	 * call to ensure we only release TSO messages at the appropriate time).
	 * <p/>
	 * This call will enable that mode and kick off a separate processing thread.
	 */
	public void enableImmediateCallbackProcessing()
	{
		if( state.isImmediateCallbackDeliveryEnabled() )
			return;
		
		// create the immediate callback delivery processing thread and start it
		this.immediateCallbackDispatcher = new ImmediateCallbackDispatcher();
		this.immediateCallbackDispatcher.start();
		
		// give the dispatch thread just a moment to start
		try{ Thread.sleep( 5 ); } catch( InterruptedException ie ) { /*ignore*/ }
		
		// set the flag on the LRCState to say that we're in this mode now
		state.setImmediateCallbackDelivery( true );
	}
	
	public void disableImmediateCallbackProcessing()
	{
		if( state.isImmediateCallbackDeliveryEnabled() == false )
			return;
		
		// interrupt the callback processing thread and wait for it to stop
		try
		{
			if( this.immediateCallbackDispatcher != null &&
				this.immediateCallbackDispatcher.isAlive() )
			{
				this.immediateCallbackDispatcher.interrupt();
				this.immediateCallbackDispatcher.join();
			}
		}
		catch( InterruptedException ie )
		{
			logger.error( "Received exception while disabling immediate callbacks", ie );
		}
		finally
		{
			// update the state to set the immediate processing flag to off
			this.immediateCallbackDispatcher = null;
			state.setImmediateCallbackDelivery( false );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/////////////////////////////////////////////////////////
	///////// Private Class: DefaultMessageHandler //////////
	/////////////////////////////////////////////////////////
	/**
	 * This class will act as the default handler for the kernels incoming message sink. It will
	 * print a warning that there was no handler to process the message, but will not cause an
	 * error to be thrown. It will mark the context as having been successful so that incoming
	 * requests don't have to be handled if there is no need for them in a given component.
	 */
	private class DefaultMessageHandler extends AbstractMessageHandler
	{
		public void process( MessageContext context )
		{
			String msg = "Ignoring message [" + context.getRequest().getClass() +
						 "]. No registered handler (sink: incoming)";
			logger.debug( msg );
			context.success();
		}
	}
	
	///////////////////////////////////////////////////////////////
	///////// Private Class: ImmediateCallbackDispatcher //////////
	///////////////////////////////////////////////////////////////
	/**
	 * This class provides the logic for the immediate callback processing thread. When immediate
	 * callbacks are enabled, this thread will be started and will continually poll the message
	 * queue for available messages, processing them as they are received until the Thread is
	 * interrupted. Immediate callback processing is turned on via the LRC and not enabled at
	 * startup.
	 */
	private class ImmediateCallbackDispatcher extends Thread
	{
		public ImmediateCallbackDispatcher()
		{
			super( "ImmediateCallbackDispatcher" );
			super.setDaemon( true );
		}
		
		public void run()
		{
			logger.debug( "Starting immediate callback delivery processor" );
			
			// Loop continuously until we are interrupted, polling for messages.
			// When we receive one, proces it and move on to the next.
			while( Thread.interrupted() == false )
			{
				try
				{
					// If callbacks are currently not enabled, sleep for a bit and come back
					// You mean just block!? Yes. I do. It's fine. Really. The stated use case
					// for enabled/disable callbacks it to allow the federate to initiate a
					// block on callbacks temporarily, so if unblocking takes a brief moment,
					// it's really not an issue. Relax, tiger.
					if( state.areCallbacksEnabled() == false )
					{
						Thread.sleep( 500 ); // sleep for half a second
						continue;
					}

					// if callbacks are enabled, get bizzay processing them
					PorticoMessage message = state.messageQueue.pollUntilNextMessage();
					
					if( message != null )
					{
						try
						{
							tickProcess( message );
						}
						catch( Exception e )
						{
							// something went wrong in the callback, log it
							logger.error( "Problem processing callback message: "+e.getMessage(), e );
						}
					}
				}
				catch( InterruptedException ie )
				{
					break;
				}
			}
			
			logger.debug( "Immediate callback delivery processor disabled" );
		}
	}

}
