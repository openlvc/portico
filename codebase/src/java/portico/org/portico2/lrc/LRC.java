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
package org.portico2.lrc;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.ISpecHelper;
import org.portico.lrc.compat.JConcurrentAccessAttempted;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.Mom;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.PorticoConstants;
import org.portico2.common.configuration.LrcConfiguration;
import org.portico2.common.configuration.RID;
import org.portico2.common.logging.Log4jConfigurator;
import org.portico2.common.messaging.ErrorResponse;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.MessageSink;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.messaging.VetoException;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.rti.services.mom.data.MomEncodingHelpers;

public class LRC
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** Using the HLA API you can't specify a */
	public static RID OVERRIDE_RID = null;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Logger logger;
	private boolean isConnected;
	
	// Configuration Data //
	protected RID rid;
	protected LrcConfiguration configuration;
	
	// Messaging Infrastructure //
	protected MessageSink incoming;
	protected MessageSink outgoing;
	
	// Network Infrastructure //
	private LRCConnection connection2;
	
	// State Information //
	private ISpecHelper specHelper; // contains hlaVersion
	private LRCState state;

	// Callback Processing //
	private Thread immediateCallbackDispatcher;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LRC( ISpecHelper helper )
	{
		this( helper, RID.loadDefaultRid() );
	}
	
	/**
	 * Create a new LRC using the given specification helper. This method will initialize the
	 * contained message sinks and create/initialize the contained IConnection.
	 * 
	 * @param rid        The configuration settings to use for this LRC
	 * @param specHelper Instance of the {@link ISpecHelper} that HLA-version specific code can
	 *                   use to cast down and access arbitrary facilities associated with the
	 *                   implementation of that HLA interface version.
	 */
	public LRC( ISpecHelper helper, RID rid )
	{
		if( rid == null )
			rid = RID.loadDefaultRid();

		// set up whatever else we need to before the handlers are created and initialized
		this.rid = rid;
		this.isConnected = false;
		this.configuration = rid.getLrcConfiguration();
		this.specHelper = helper;
		Log4jConfigurator.activate( this.rid.getLog4jConfiguration() );
		this.logger = LogManager.getFormatterLogger( "portico.lrc" );
		this.logger.info( "Creating new LRC" );
		this.logger.info( "Portico version: "+PorticoConstants.RTI_VERSION );
		this.logger.info( "Interface: "+helper.getHlaVersion() );

		// create the notification manager
//		this.notificationManager = NotificationManager.newNotificationManager();
		
		// the immediate callback processing remains null until turned on explictly
		this.immediateCallbackDispatcher = null;

		// create the LRCState component that has most of the state-holding components inside it
		this.state = new LRCState( this );
		
		// register the state as a Notification handler so that it can take the appropriate
		// actions when the location federate joins and resigned from a federation. We only
		// want to do this once, so we don't put it in initializeLrc() as that will get called
		// when we re-join a new federation. Also, we have to register it this way so the 
		// NotificationManager doesn't try to instantiate it!
//		this.notificationManager.addListener( Priority.LOW, this.state );
		
		// initialize the parts of the LRC that should be re-initialized whenever the
		// federate attached to it resigns and rejoins
		initializeLrc();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This should be called when a federate resigned from a federation. It will clear out all
	 * the existing federation state and reinitialize the messaging infrastructure. Note that this
	 * will have no effect on the connection.
	 */
	protected void reinitialize()
	{
		// reinitialize the state
		state.reinitialize();
		
		// reinitialize the kernel
		initializeLrc();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Configuration Methods   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * So that the messaging components can be properly reinitialized when a federate resigns from
	 * a federation and then joins another (the same LRC) this method contains all the logic to
	 * reinitialize the appropriate internal LRC components. This will wipe out the existing
	 * message sinks and reconfigure them. The connection will remain untouched.
	 */
	private void initializeLrc() throws JConfigurationException
	{
		this.incoming = new MessageSink( "incoming", logger );
		this.outgoing = new MessageSink( "outgoing", logger );

		// initialize the connection - but only if we haven't done so before
		if( connection2 == null )
			connection2 = new LRCConnection( this, configuration.getConnectionConfiguration() );
		
		// initialize the messaging framework
		LRCHandlerRegistry.loadHandlers( this );
		
		logger.info( "LRC initialized (HLA version: %s)", specHelper.getHlaVersion() );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean isConnected()
	{
		return this.isConnected;
	}

	public void connect() throws JRTIinternalError
	{
		if( this.isConnected )
			return;

		// Ask the connection to start
		this.connection2.connect();
		
		// Record that we're connected
		this.isConnected = true;
	}
	
	public void disconnect() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;
		
		// Tell the connection to break
		this.connection2.disconnect();
		
		// Record that we're no longer connected
		this.isConnected = false;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Tick and Callback Processing   ////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Used in the HLA 1.3 interfaces (and java1): tick()
	 * <p/>
	 * Just calls {@link #tickUntilEmpty(long)}, passing {@link LRCProperties#LRC_TICK_TIMEOUT}
	 */
	public void tick() throws JRTIinternalError, JConcurrentAccessAttempted
	{
		tickUntilEmpty( configuration.getTickTimeoutNanos() );
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
		catch( VetoException ve )
		{
			// nothing to see here!
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
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public Logger getLogger()
	{
		return this.logger;
	}

	public ISpecHelper getSpecHelper()
	{
		return this.specHelper;
	}

	public LRCState getState()
	{
		return this.state;
	}
	
	public HLAVersion getHlaVersion()
	{
		return this.specHelper.getHlaVersion();
	}
	
	public LRCConnection getConnection()
	{
		return this.connection2;
	}

	public MessageSink getOutgoingSink()
	{
		return this.outgoing;
	}
	
	public MessageSink getIncomingSink()
	{
		return this.incoming;
	}

	/**
	 * Reports the invocation of a RTIambassador or FederateAmbassador service (whether successful or 
	 * not) to the federation.
	 * <p/>
	 * If the federate this LRC is managing has Service Invocation reporting enabled (See 
	 * {@link LRCState#isServiceReporting()}), then the service invocation will be reported via the
	 * <code>HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation</code> interaction, regardless
	 * of whether the invocation was successful, or resulted in an error.
	 * <p/>
	 * Additionally, if the federate this LRC is managing has Exception reporting enabled (See
	 * {@link LRCState#isExceptionReporting()}), then any service invocations that resulted in an error
	 * will be reported via the <code>HLAmanager.HLAfederate.HLAreport.HLAreportException</code> 
	 * interaction.
	 * <p/>
	 * If neither Service Invocation reporting, or Exception reporting are enabled, calling this method
	 * will result in a noop.
	 * 
	 * @param serviceName The name of the service invoked
	 * @param success <code>true</code> if the service was invoked successfully, otherwise 
	 *                <code>false</code> if the service invocation raised an error
	 * @param returnValue the value that was returned due to a successful invocation of the service. 
	 *                    Ignored if the <code>success</code> parameter is <code>false</code>
	 * @param errorMessage the error message that was raised due to an unsuccessful invocation of the 
	 *                     service. Ignored if the <code>success</code> parameter is <code>true</code>
	 * @param parameters the parameters that the service was invoked with
	 */
	public final void reportServiceInvocation( String serviceName,
	                                           boolean success,
	                                           Object returnValue,
	                                           String errorMessage,
	                                           Object... parameters )
	{
		boolean serviceReporting = this.state.isServiceReporting();
		boolean exceptionReporting = this.state.isExceptionReporting();
		
		// HLAreportException
		if( exceptionReporting && !success )
		{
			Map<String,Object> params = new HashMap<String,Object>();
			params.put( "HLAfederate", state.getFederateHandle() );
			params.put( "HLAservice", serviceName );
			params.put( "HLAexception", errorMessage );
			
			// TODO cache the handle
			int reportHandle =
			    Mom.getMomInteractionHandle( this.specHelper.getHlaVersion(),
			                                 "HLAmanager.HLAfederate.HLAreport.HLAreportException" );
			ICMetadata reportExceptionType = state.getFOM().getInteractionClass( reportHandle );
			HashMap<Integer,byte[]> hlaParams =
			    MomEncodingHelpers.encodeInteractionParameters( specHelper.getHlaVersion(),
			                                                    reportExceptionType, 
			                                                    params );
			
			// Create the report service invocation message
			SendInteraction message = new SendInteraction( reportExceptionType.getHandle(), 
			                                               null, 
			                                               hlaParams );
			message.setIsFromRti( true );
			message.setTargetFederation( state.getFederationHandle() );
			connection2.sendDataMessage( message );
		}
		
		// HLAreportServiceInvocation
		if( serviceReporting )
		{
			// Return Arguments
			Object[] returnArguments = null;
			if( success && returnValue != null )
				returnArguments = new Object[]{ returnValue };
			else
				returnArguments = new Object[0];

			// Exception
			String exception = null;
			if( !success )
				exception = errorMessage;

			int serial = state.getNextServiceInvocationSerial();
			Map<String,Object> params = new HashMap<String,Object>();
			params.put( "HLAfederate", state.getFederateHandle() );
			params.put( "HLAservice", serviceName );
			params.put( "HLAsuccessIndicator", success );
			params.put( "HLAsuppliedArguments", parameters );
			params.put( "HLAreturnedArguments", returnArguments );
			params.put( "HLAexception", exception );
			params.put( "HLAserialNumber", serial );

			// TODO cache the handle
			int rsiHandle =
			    Mom.getMomInteractionHandle( this.specHelper.getHlaVersion(),
			                                 "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
			ICMetadata reportServiceInvocationType = state.getFOM().getInteractionClass( rsiHandle );
			HashMap<Integer,byte[]> hlaParams =
			    MomEncodingHelpers.encodeInteractionParameters( specHelper.getHlaVersion(),
			                                                    reportServiceInvocationType, 
			                                                    params );
			
			// Create the report service invocation message
			SendInteraction message = new SendInteraction( reportServiceInvocationType.getHandle(), 
			                                               null, 
			                                               hlaParams );
			message.setIsFromRti( true );
			message.setTargetFederation( state.getFederationHandle() );
			connection2.sendDataMessage( message );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
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
