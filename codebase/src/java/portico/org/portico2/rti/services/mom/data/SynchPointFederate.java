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
package org.portico2.rti.services.mom.data;

import java.util.Collection;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.sync.data.SyncPoint;

import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;

public class SynchPointFederate implements DataElement
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final int EncodedLength = HLA1516eHandle.EncodedLength + 4;
	
	public enum SynchPointStatus
	{
		NoActivity(0),
		AttemptingToRegisterSynchPoint(1),
		MovingToSynchPoint(2),
		WaitingForRestOfFederation(3);
		
		private int value;
		
		private SynchPointStatus( int value )
		{
			this.value = value;
		}
		
		public int getValue()
		{
			return this.value;
		}
	}

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int federate;
	private SynchPointStatus status;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SynchPointFederate( int federate, SynchPointStatus status )
	{
		this.federate = federate;
		this.status = status;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public int getEncodedLength()
	{
		return EncodedLength;
	}
	
	@Override
	public int getOctetBoundary()
	{
		return EncodedLength;
	}

	@Override
	public byte[] toByteArray() throws EncoderException
	{
		ByteWrapper wrapper = new ByteWrapper( EncodedLength );
		encode( wrapper );
		return wrapper.array();
	}
	
	@Override
	public void encode( ByteWrapper wrapper )
	{
		HLA1516eHandle handle = new HLA1516eHandle( this.federate );
		HLA1516eInteger32BE value = new HLA1516eInteger32BE( this.status.getValue() );
		handle.encode( wrapper );
		value.encode( wrapper );
	}
	
	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString()
	{
		return "Federate=" + this.federate + ",Status=" + this.status;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public int getFederateId()
	{
		return this.federate;
	}
	
	public SynchPointStatus getStatus()
	{
		return this.status;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static SynchPointFederate[] forSyncPoint( Federation federation, SyncPoint syncPoint )
	{
		Collection<Integer> federateHandles = 
			syncPoint.isFederationWide() ? federation.getFederateHandles() 
			                             : syncPoint.getFederates();
		
		SynchPointFederate[] results = new SynchPointFederate[federateHandles.size()];
		int index = 0;
		boolean pointSynced = syncPoint.isSynchronized();
		for( int federateHandle : federateHandles )
		{
			boolean hasAchieved = syncPoint.hasFederateAchieved( federateHandle );
			boolean waiting = !pointSynced && hasAchieved;
			SynchPointStatus status = waiting ? SynchPointStatus.WaitingForRestOfFederation
				                              : SynchPointStatus.NoActivity;
			
			SynchPointFederate record = new SynchPointFederate( federateHandle, status );
			results[index++] = record;
		}
		
		return results;
	}
}
