/*
 *   Copyright 2008 The Portico Project
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

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.time.data.TimeStatus;

/**
 * The MessageQueue class is designed to store incoming messages for later processing by a federate.
 * It combines with the {@link LRCState} to restrict the release of messages based upon the status
 * of the linked LRC. For example, if the LRC is constrained, it will not release timestampped
 * messages until an advance to at least that time has been requested.
 * <p/>
 * This queue contains two queues internally: one for receive-order messages (RO) and one for 
 * timestamp-ordered messages (TSO). The RO queue is maintained on a normal first-in/first-out
 * basis. The TSO queue is sorted based on the timestamp of the incoming message. Those with
 * lower timestamps will be placed at the front of the queue. TSO messages are only released by the
 * queue when the {@link TimeStatus} of the local federate and federation is in an appropriate 
 * state (briefly: when the federation-lbts is greater-or-equal to the timestamp AND the time of
 * the local federate is as well).
 * <p/>
 * Any messages that are placed in the queue and have the
 * {@link PorticoMessage#isImmediateProcessingRequired()} flag set will be automatically routed
 * into the incoming sink of the kernel associated with the LRC rather than storing them. I repeat,
 * messages with this flag will be processed right away and are not stored in the queue.
 */
public class LRCMessageQueue implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRC lrc;
	private LRCState state;
	
	private BlockingQueue<PorticoMessage> roQueue;
	private PriorityBlockingQueue<PorticoMessage> tsoQueue;

	// hla-related properties //
	private TimeStatus timeStatus;
	
	// locking and concurrency //
	private Lock lock;
	private Condition condition;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected LRCMessageQueue( LRCState lrcState )
	{
		this.state = lrcState;
		this.lrc = lrcState.theLRC;
		this.roQueue = new LinkedBlockingQueue<PorticoMessage>();
		this.tsoQueue = new PriorityBlockingQueue<PorticoMessage>( 13, new Sorter() );
		
		// locking and concurrency //
		this.lock = new ReentrantLock();
		this.condition = this.lock.newCondition();
		
		// this will be lazy-loaded
		// when a federate first joins a federation, the time status will be created and
		// put into the LRCState. All access to this variable should be through the local
		// private timeStatus() method which will lazy load this if it is null
		this.timeStatus = null;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * <b>NOTE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!</b>
	 * <p/>
	 * *ANY* access to the local federates {@link TimeStatus} from inside the message queue
	 * should be through this method. The time status is lazy-loaded from the {@link LRCState}
	 * by this method, so it is the only way to absolutely avoid NullPointerExceptions.
	 */
	private TimeStatus timeStatus()
	{
		if( this.timeStatus == null )
			this.timeStatus = state.getTimeStatus();
		
		return this.timeStatus;
	}
	
	/**
	 * Gets the federation-wide LBTS from the TimeManager in the LRCState.
	 */
	private double federationLbts()
	{
		return state.getTimeManager().getLBTS();
	}
	
	/**
	 * Place the message on the queue. Which internal queue it goes on to depends on the current
	 * status of the federation and the timestamp status of the message. Note that messages with
	 * the {@link PorticoMessage#setImmediateProcessingFlag(boolean) immediate processing flag}
	 * set to <code>true</code> will be automatically passed to the incoming message sink of the
	 * kernel associated with the LRC. For other messages, if:
	 * <ul>
	 *   <li><b>Constrained is *NOT* set</b>: The message is placed on the RO queue. If the
	 *       message is TSO, it is converted to an RO message before this happens</li>
	 *   <li><b>Constrained *IS* set</b>: If the message is RO, it is placed on the RO queue. If
	 *       the message is TSO, it is placed on the TSO queue.</li>
	 * </ul>
	 * 
	 * Timestamped messages will be ordered according to their time (lowest to highest). If the
	 * federate is *NOT* constrained, messages will automatically be stored in the RO queue (and
	 * will have their times set to PorticoConstants.NULL_TIME).
	 * 
	 * @return true if the message was successfully added to the queue, false otherwise.
	 */
	public boolean offer( PorticoMessage message )
	{
		if( message == null )
			return false;
		
		lock.lock();
		try
		{
			// if this is a priority message, process it right away (removed for now)
			if( message.isImmediateProcessingRequired() )
			{
				try
				{
					lrc.incoming.process( new MessageContext(message) );
					return true;
				}
				catch( Exception e )
				{
					lrc.logger.info( "Exception processing priority message", e );
					return false;
				}
			}
			
			// FIX: PORT-103: If not constrained, store the message RO and remove the timestamp
			if( timeStatus().isConstrained() == false )
			{
				message.setTimestamp( PorticoConstants.NULL_TIME );
				return this.roQueue.offer( message );
			}
			
			// check if it is RO or TSO
			if( message.isTimestamped() )
			{
				return this.tsoQueue.offer( message );
			}
			else
			{
				return this.roQueue.offer( message );
			}
		}
		finally
		{
			// signal to any threads waiting on the condition
			condition.signalAll();
			// release the lock
			lock.unlock();
		}
	}

	/**
	 * Fetch the next available message from the queue.
	 * <p/>
	 * The algorithm used to determine which message is next goes as follows:
	 * 
	 * <ol>
	 *   <li>If there are any RO messages, they are removed first</li>
	 *   <li>If there are no RO messages the first message of the TSO queue is consulted
	 *       <ol>
	 *         <li>If the timestamp of the first message is *less than or equal to* the current
	 *             federation LBTS and the local requested time, it is released</li>
	 *         <li>If the timestamp of the first message is *greater than* the current release
	 *             time, no message is released and null is returned</li>
	 *       </ol>
	 *   </li>
	 * </ol>
	 *
	 * As the TSO queue is ordered, the message with the lowest timestamp will always be at the
	 * head of the queue. Thus, it will be the first candidate to be released should the current
	 * requested time of the federate be of an appropriate value.
	 */
	public PorticoMessage poll()
	{
		// get the lock //
		lock.lock();
		
		// check for a message //
		try
		{
			TimeStatus localStatus = timeStatus();

			///////////////////////////////
			// check for any RO messages //
			///////////////////////////////
			// is there an RO message for delivery?
			if( this.roQueue.isEmpty() == false )
			{
				// we have an RO message
				////////////////////////////
				// check the async status //
				////////////////////////////////////////////////////////////////////////////
				// if constrained:                                                        //
				//    if asynchronous delivery is NOT enabled:                            //
				//      -Can only deliver callbacks that are NOT "messages" (that is, any //
				//       reflect, interaction or delete instance)                         //
				//    UNLESS: we are in a time advance: then can release any waiting RO   //
				//    if asynchronous delivery IS enabled:                                //
				//      -Can deliver any RO message that is waiting                       //
				// if NOT constrained:                                                    //
				//      -Can deliver any RO message that is waiting                       //
				// Stupid rules? yes. Blame the spec                                      //
				////////////////////////////////////////////////////////////////////////////
				if( localStatus.isConstrained() &&
					localStatus.isAsynchronous() == false &&
					localStatus.isAdvanceRequestOutstanding() == false )
				{
					return this.pollForNonMessage();
				}
				else
				{
					// we don't need to worry about the async status, just deliver the RO message
					return this.roQueue.poll();
				}
			}
			
			////////////////////////////////////////////////////////
			// no RO messages - check for releasable TSO messages //
			////////////////////////////////////////////////////////
			PorticoMessage message = this.tsoQueue.peek();
			if( message != null )
			{
				// There is a message at the head of the set, is it of a releasable time?
				// To be "releasable" its timestamp must be:
				//   1) less than or equal to FEDERATION-wide LBTS ----AND----
				//      less than the local federates current or requested time
				//                           -OR-
				//   2) a time-advance grant message (which is always inserted into the TSO queue
				//      behind all other messages of the same time)
				if( message.isTimeAdvance() )
				{
					// it is! release it - we also need to remove it, so we'll poll
					return this.tsoQueue.poll();
				}
				else if( message.getTimestamp() <= federationLbts() &&
				         message.getTimestamp() <= localStatus.getRequestedTime() )
				{
					// it is! release it - we also need to remove it, so we'll poll
					return this.tsoQueue.poll();
				}
			}
	
			//////////////////////////////////////
			// no messages that can be released //
			//////////////////////////////////////
			return null;
		}
		finally
		{
			// release the lock
			lock.unlock();
		}
	}

	/**
	 * This method is provided to support implementations of the HLA method flushQueueRequest().
	 * It will return a list of all available messages that exist in the RO queue, along with all
	 * those that exist in the TSO queue up to the given time (regardless of what the release time
	 * is). FQR is a strange services indeed. 
	 */
	public List<PorticoMessage> flush( double maxTime )
	{
		// get the lock //
		lock.lock();

		try
		{
			List<PorticoMessage> flushList = new ArrayList<PorticoMessage>();

			// flush out all the RO messages
			roQueue.drainTo( flushList );

			// get all the TSO messages up to the specified time
			PorticoMessage temp = tsoQueue.peek();
			while( temp != null )
			{
				if( temp.getTimestamp() > maxTime )
					break;

				flushList.add( tsoQueue.poll() );
				temp = tsoQueue.peek();
			}

			return flushList;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	/**
	 * All this applies to RO messages only.
	 * <p/>
	 * The HLA specification says that if a federate: a) is constrained and b) does NOT have
	 * asynchronous delivery enabled and c) isn't in the middle of a time advance, then RO
	 * "messages" cannot be released to it. Sadly, the HLA spec also defines "messages" as
	 * (basically) reflections, interactions and removals. Everything else is not a "message" and
	 * thus can be happily delivered RO. 
	 * <p/>
	 * This method will find, remove and return from the RO queue the first non "message" callback
	 * message. Thus, in the situation above, it will return the first queued message that can
	 * happily be delivered, or null, if none exists
	 * 
	 * @return The first RO message in the queue that can be delivered, or null, if none can 
	 */
	private PorticoMessage pollForNonMessage()
	{
		// we are contrained, NOT asynchronous and NOT advancing, we can only
		// release RO messages that are NOT "messages" (according to the spec
		// definition of the word)
		if( roQueue.peek().isSpecDefinedMessage() == false )
		{
			// the first available message can happily be released
			return roQueue.poll();
		}

		// we have to trawl the queue for the first "non-message" message
		// so we can return it
		PorticoMessage winner = null;
		Iterator<PorticoMessage> iterator = this.roQueue.iterator();
		while( iterator.hasNext() )
		{
			PorticoMessage message = iterator.next();
			if( message.isSpecDefinedMessage() == false )
			{
				// found one!
				winner = message;
				break;
			}
		}
		
		// remove the message from the queue and return it, unless we didn't find one
		if( winner != null )
			this.roQueue.remove( winner );

		return winner;
	}

	/**
	 * This method is the same as {@link #poll()} except that if there are no messages available,
	 * it will block until there are (or until the timeout value has been reached). If the blocking
	 * thread is interrupted while waiting, null will be returned.
	 */
	public PorticoMessage poll( long timeoutNanos )
	{
		lock.lock();
		try
		{
			// 1. check to see if we have a message //
			// there is no need to aquire the lock as we are not accessing the queue directly
			// lock is reentrant, so we shouldn't take hit for aquiring it in poll()
			PorticoMessage theMessage = poll();
			if( theMessage != null )
			{
				// there is a message so we can just return it
				return theMessage;
			}
	
			// 2. need to wait for an update to come through //
			// wait on condition
			condition.awaitNanos( timeoutNanos );
			// we have been woken up:
			//  -if by timeout: return null
			//  -if by signal: return available message
			return poll();
		}
		catch( InterruptedException ie )
		{
			return null;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * This method is the same as {@link #poll(long)} except that it will wait without timing
	 * out until a new message is available. At this point it will return the message, or, if
	 * it has been interrupted it will return null.
	 */
	public PorticoMessage pollUntilNextMessage() throws InterruptedException
	{
		lock.lock();
		try
		{
			// 1. check to see if we have a message
			// there is no need to aquire the lock as we are not accessing the queue directly
			// lock is reentrant, so we shouldn't take hit for aquiring it in poll()
			PorticoMessage theMessage = poll();
			if( theMessage != null )
			{
				// there is a message so we can just return it
				return theMessage;
			}
	
			// 2. need to wait for an update to come through
			// wait on condition
			condition.await();
			// we have been woken up:
			//  -if by timeout: return null
			//  -if by signal: return available message
			return poll();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * This method is the same as {@link #poll()} except that if there are no messages available,
	 * it will block until there are (or until the deadline time has been reached). If the blocking
	 * thread is interrupted while waiting, null is returned.
	 */
	public PorticoMessage pollUntil( Date deadline )
	{
		lock.lock();
		try
		{
			// 1. check to see if we have a message
			// there is no need to aquire the lock as we are not accessing the queue directly
			// lock is reentrant, so we shouldn't take hit for aquiring it in poll()
			PorticoMessage theMessage = poll();
			if( theMessage != null )
			{
				// there is a message so we can just return it
				return theMessage;
			}
	
			// 2. need to wait for an update to come through
			condition.awaitUntil( deadline );
			return poll();
		}
		catch( InterruptedException ie )
		{
			return null;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * This method will return the callback message at the head of the TSO queue, but it WILL NOT
	 * REMOVE IT. This method should only be called by certain time-advancement handlers when
	 * processing event-based advance requests. If the TSO queue is empty, null is returned.
	 */
	public PorticoMessage peekTSO()
	{
		if( tsoQueue.isEmpty() )
		{
			return null;
		}
		else
		{
			return tsoQueue.peek();
		}
	}

	/**
	 * @return Returns true if there are no messages queued for delivery (RO or TSO)
	 */
	public boolean isEmpty()
	{
		lock.lock();
		try
		{
			// test the queues //
			return this.roQueue.isEmpty() && this.tsoQueue.isEmpty();
		}
		finally
		{
			lock.unlock();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// HLA Related Properties Methods //////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * When a federate becomes unconstrained, it is no long able to receive messages with
	 * timestamps. This method goes through the existing TSO queue and sets the timestamps of all
	 * contained messages to {@link PorticoConstants#NULL_TIME} before putting them into the RO
	 * queue and clearing the TSO queue.
	 */
	public void becameUnconstrained()
	{
		lock.lock();
		try
		{
			for( PorticoMessage message : this.tsoQueue )
			{
				message.setTimestamp( PorticoConstants.NULL_TIME );
				roQueue.offer( message );
			}

			// clear the tso queue
			this.tsoQueue.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	public int getSize()
	{
		return this.roQueue.size() + this.tsoQueue.size();
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder( "Message Queue (federate:" );
		builder.append( state.getFederateHandle() );
		builder.append( ")\n" );
		
		// Receive Order Queue //
		builder.append( "\t[RO Queue]\n" );
		if( roQueue.isEmpty() )
		{
			builder.append( "\t(empty)\n" );
		}
		else
		{
			PorticoMessage[] roArray = roQueue.toArray( new PorticoMessage[roQueue.size()] );
			for( int i = 0; i < roArray.length; i++ )
			{
				builder.append( "\t[" );
				builder.append( i+1 );
				builder.append( "] message=" );
				builder.append( roArray[i].getClass().getCanonicalName() );
				builder.append( ", src=" );
				builder.append( roArray[i].getSourceFederate() );
				builder.append( ", dst=" );
				builder.append( roArray[i].getTargetFederate() );
				builder.append( "\n" );
			}
		}
		
		// Receive Order Queue //
		builder.append( "\t[TSO Queue]\n" );
		if( tsoQueue.isEmpty() )
		{
			builder.append( "\t(empty)\n" );
		}
		else
		{
			PorticoMessage[] tsoArray = tsoQueue.toArray( new PorticoMessage[tsoQueue.size()] );
			for( int i = 0; i < tsoArray.length; i++ )
			{
				builder.append( "\t[" );
				builder.append( i+1 );
				builder.append( "] message=" );
				builder.append( tsoArray[i].getClass().getCanonicalName() );
				builder.append( " @" );
				builder.append( tsoArray[0].getTimestamp() );
				builder.append( ", src=" );
				builder.append( tsoArray[i].getSourceFederate() );
				builder.append( ", dst=" );
				builder.append( tsoArray[i].getTargetFederate() );
				builder.append( "\n" );
			}
		}
		
		return builder.toString();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		try
		{
			lock.lock();

			// only write the CONTENTS of the tsoQueue. Writing the whole queue means that
    		// we have to write the Sorter, and because it's an inner class it attempts to
    		// write the queue itself (which we don't want to do)
    		ArrayList<PorticoMessage> tsoContents = new ArrayList<PorticoMessage>();
    		tsoQueue.drainTo( tsoContents );
    		output.writeObject( tsoContents );

    		// don't include any Save status messages from the RO-Queue when we write the
    		// contents to disk. this just causes problems on restore when federates think
    		// that a federate has already saved, and the message that caused that comes
    		// back to life due to a restore (to fix PORT-847)
    		//output.writeObject( roQueue );
    		output.writeObject( filterSaveMessages() );
    		
    		output.writeObject( timeStatus );
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Goes through the RO queue and filters out any save messages, returning a *NEW* queue that
	 * has the contents of the existing RO queue, but does not have any of the save messages.
	 */
	private BlockingQueue<PorticoMessage> filterSaveMessages()
	{
		BlockingQueue<PorticoMessage> newQueue = new LinkedBlockingQueue<PorticoMessage>();
		for( PorticoMessage message : roQueue )
		{
			if( message instanceof org.portico.lrc.services.saverestore.msg.SaveBegun ||
				message instanceof org.portico.lrc.services.saverestore.msg.SaveComplete )
			{
				continue;
			}
			else
			{
				newQueue.add( message );
			}
		}
		
		return newQueue;
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		try
		{
			lock.lock();
			
    		ArrayList<PorticoMessage> tsoContents = (ArrayList<PorticoMessage>)input.readObject();
    		this.tsoQueue.addAll( tsoContents );
    
    		this.roQueue = (BlockingQueue<PorticoMessage>)input.readObject();
    		this.timeStatus = (TimeStatus)input.readObject();
		}
		finally
		{
			lock.unlock();
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Private Inner Class //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Comparator used for sorting TSO callback messages such that the callback queue remains
	 * ordered. If the TSO message is a time advance grant (responds <code>true</code> to the
	 * {@link PorticoMessage#isTimeAdvance()}), it is considered LESS than any other
	 * message with the SAME timestamp. This way, it forces it to the bottom of the queue for
	 * that timestamp.
	 */
	private class Sorter implements Comparator<PorticoMessage>
	{
		public int compare( PorticoMessage o1, PorticoMessage o2 )
		{
			double time1 = o1.getTimestamp();
			double time2 = o2.getTimestamp();
			
			if( time1 < time2 )
			{
				return -1;
			}
			else if( time1 > time2 )
			{
				return 1;
			}
			else
			{
				// they appear to be equal, which is fine as long as NEITHER of them
				// is a time advance grant
				if( o1.isTimeAdvance() )
					return 1;
				else if( o2.isTimeAdvance() )
					return -1;
				else
					return 0;
			}
		}
	}
}
