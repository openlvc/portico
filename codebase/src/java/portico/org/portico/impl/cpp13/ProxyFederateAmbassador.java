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
package org.portico.impl.cpp13;

import java.util.ArrayList;
import java.util.List;

import org.portico.impl.hla13.types.DoubleTime;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.AttributeHandleSet;
import hla.rti.EventRetractionHandle;
import hla.rti.FederateAmbassador;
import hla.rti.HandleIterator;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;

public class ProxyFederateAmbassador implements FederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateAmbassadorLink tocpp;
	private int ambassadorId;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ProxyFederateAmbassador( int ambassadorId )
	{
		this.tocpp = new FederateAmbassadorLink();
		this.ambassadorId = ambassadorId;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public FederateAmbassadorLink getFederateAmbassadorLink()
	{
		return this.tocpp;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// Helper Methods //////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////
	private int[] fromAHS( AttributeHandleSet ahs )
	{
		int[] handles = new int[ahs.size()];
		HandleIterator iterator = ahs.handles();
		handles[0] = iterator.first();
		for( int i = 1; i < ahs.size(); i++ )
		{
			handles[i] = iterator.next();
		}
		
		return handles;
	}

	private List<Object> fromInteraction( ReceivedInteraction interaction )
	{
		int[] handles = new int[interaction.size()];
		byte[][] values = new byte[interaction.size()][];
		try
		{
			for( int i = 0 ; i < interaction.size() ; ++i )
			{
				handles[i] = interaction.getParameterHandle(i);
				values[i]  = interaction.getValue(i);
			}
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// this should never happen, just print it so at we can at least track it down
			aioob.printStackTrace();
		}

		// package the information for return
		ArrayList<Object> list = new ArrayList<Object>();
		list.add( handles );
		list.add( values );
		return list;
	}
	
	private List<Object> fromReflection( ReflectedAttributes attributes )
	{
		int[] handles = new int[attributes.size()];
		byte[][] values = new byte[attributes.size()][];
		try
		{
			for( int i = 0 ; i < attributes.size() ; ++i )
			{
				handles[i] = attributes.getAttributeHandle(i);
				values[i]  = attributes.getValue(i);
			}
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// this should never happen, just print it so at we can at least track it down
			aioob.printStackTrace();
		}

		// package the information for return
		ArrayList<Object> list = new ArrayList<Object>();
		list.add( handles );
		list.add( values );
		return list;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Federation Management Methods ////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void synchronizationPointRegistrationFailed( String arg0 )
	{
		this.tocpp.synchronizationPointRegistrationFailed( ambassadorId, arg0 );
	}

	public void synchronizationPointRegistrationSucceeded( String arg0 )
	{
		this.tocpp.synchronizationPointRegistrationSucceeded( ambassadorId, arg0 );
	}

	public void announceSynchronizationPoint( String arg0, byte[] arg1 )
	{
		this.tocpp.announceSynchronizationPoint( ambassadorId, arg0, arg1 );
	}

	public void federationSynchronized( String arg0 )
	{
		this.tocpp.federationSynchronized( ambassadorId, arg0 );
	}

	public void requestFederationRestoreFailed( String arg0, String arg1 )
	{
		this.tocpp.requestFederationRestoreFailed( ambassadorId, arg0, arg1 );
	}

	public void requestFederationRestoreSucceeded( String arg0 )
	{
		this.tocpp.requestFederationRestoreSucceeded( ambassadorId, arg0 );
	}

	public void initiateFederateRestore( String arg0, int arg1 )
	{
		this.tocpp.initiateFederateRestore( ambassadorId, arg0, arg1 );
	}

	public void federationRestoreBegun()
	{
		this.tocpp.federationRestoreBegun( ambassadorId );
	}

	public void federationRestored()
	{
		this.tocpp.federationRestored( ambassadorId );
	}

	public void federationNotRestored()
	{
		this.tocpp.federationNotRestored( ambassadorId );
	}

	public void initiateFederateSave( String arg0 )
	{
		this.tocpp.initiateFederateSave( ambassadorId, arg0 );
	}

	public void federationNotSaved()
	{
		this.tocpp.federationNotSaved( ambassadorId );
	}

	public void federationSaved()
	{
		this.tocpp.federationSaved( ambassadorId );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Ownership Management Methods ///////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////
	public void attributeIsNotOwned( int arg0, int arg1 )
	{
		this.tocpp.attributeIsNotOwned( ambassadorId, arg0, arg1);
	}

	public void attributeOwnedByRTI( int arg0, int arg1 )
	{
		this.tocpp.attributeOwnedByRTI( ambassadorId, arg0, arg1);
	}

	public void attributeOwnershipAcquisitionNotification( int arg0, AttributeHandleSet arg1 )
	{
		this.tocpp.attributeOwnershipAcquisitionNotification( ambassadorId, arg0, fromAHS(arg1) );
	}

	public void attributeOwnershipDivestitureNotification( int arg0, AttributeHandleSet arg1 )
	{
		this.tocpp.attributeOwnershipDivestitureNotification( ambassadorId, arg0, fromAHS(arg1) );
	}

	public void attributeOwnershipUnavailable( int arg0, AttributeHandleSet arg1 )
	{
		this.tocpp.attributeOwnershipUnavailable( ambassadorId, arg0, fromAHS(arg1) );
	}

	public void attributesInScope( int arg0, AttributeHandleSet arg1 )
	{
		this.tocpp.attributesInScope( ambassadorId, arg0, fromAHS(arg1) );
	}

	public void attributesOutOfScope( int arg0, AttributeHandleSet arg1 )
	{
		this.tocpp.attributesOutOfScope( ambassadorId, arg0, fromAHS(arg1) );
	}

	public void confirmAttributeOwnershipAcquisitionCancellation( int arg0,
	                                                              AttributeHandleSet arg1 )
	{
		this.tocpp.confirmAttributeOwnershipAcquisitionCancellation( ambassadorId,
		                                                             arg0,
		                                                             fromAHS(arg1) );
	}

	public void informAttributeOwnership( int arg0, int arg1, int arg2 )
	{
		this.tocpp.informAttributeOwnership( this.ambassadorId, arg0, arg1, arg2 );
	}

	public void requestAttributeOwnershipAssumption( int arg0, AttributeHandleSet arg1, byte[] arg2 )
	{
		this.tocpp.requestAttributeOwnershipAssumption( ambassadorId, arg0, fromAHS(arg1), arg2 );

	}

	public void requestAttributeOwnershipRelease( int arg0, AttributeHandleSet arg1, byte[] arg2 )
	{
		this.tocpp.requestAttributeOwnershipRelease( ambassadorId, arg0, fromAHS(arg1), arg2 );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Object Management Methods /////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////
	public void discoverObjectInstance( int arg0, int arg1, String arg2 )
	{
		this.tocpp.discoverObjectInstance( ambassadorId, arg0, arg1, arg2 );
	}

	public void receiveInteraction( int arg0, ReceivedInteraction arg1, byte[] arg2 )
	{
		List<Object> converted = fromInteraction( arg1 );
		int[] handles   = (int[])converted.get(0);
		byte[][] values = (byte[][])converted.get(1);
		
		this.tocpp.receiveInteraction( ambassadorId, arg0, handles, values, arg2 );
	}

	public void receiveInteraction( int arg0,
	                                ReceivedInteraction arg1,
	                                byte[] arg2,
	                                LogicalTime arg3,
	                                EventRetractionHandle arg4 )
	{
		List<Object> converted = fromInteraction( arg1 );
		int[] handles   = (int[])converted.get(0);
		byte[][] values = (byte[][])converted.get(1);
		double theTime = ((DoubleTime)arg3).getTime();
		
		this.tocpp.receiveInteraction( ambassadorId, arg0, handles, values, arg2, theTime, 0 );
	}

	public void reflectAttributeValues( int arg0, ReflectedAttributes arg1, byte[] arg2 )
	{
		List<Object> converted = fromReflection( arg1 );
		int[] handles   = (int[])converted.get(0);
		byte[][] values = (byte[][])converted.get(1);

		this.tocpp.reflectAttributeValues( ambassadorId, arg0, handles, values, arg2 );
	}

	public void reflectAttributeValues( int arg0,
	                                    ReflectedAttributes arg1,
	                                    byte[] arg2,
	                                    LogicalTime arg3,
	                                    EventRetractionHandle arg4 )
	{
		List<Object> converted = fromReflection( arg1 );
		int[] handles   = (int[])converted.get(0);
		byte[][] values = (byte[][])converted.get(1);
		double theTime = ((DoubleTime)arg3).getTime();
		
		this.tocpp.reflectAttributeValues( ambassadorId, arg0, handles, values, arg2, theTime, 0 );
	}

	public void removeObjectInstance( int arg0, byte[] arg1 )
	{
		this.tocpp.removeObjectInstance( ambassadorId, arg0, arg1 );
	}

	public void removeObjectInstance( int arg0,
	                                  byte[] arg1,
	                                  LogicalTime arg2,
	                                  EventRetractionHandle arg3 )
	{
		double time = ((DoubleTime)arg2).getTime();
		this.tocpp.removeObjectInstance( ambassadorId, arg0, arg1, time, 0 );
	}

	public void provideAttributeValueUpdate( int arg0, AttributeHandleSet arg1 )
	{
		try
		{
			this.tocpp.provideAttributeValueUpdate( ambassadorId, arg0, fromAHS(arg1) );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void requestRetraction( EventRetractionHandle arg0 )
	{
		this.tocpp.requestRetraction( ambassadorId, 0 );
	}

	public void startRegistrationForObjectClass( int arg0 )
	{
		this.tocpp.startRegistrationForObjectClass( ambassadorId, arg0 );
	}

	public void stopRegistrationForObjectClass( int arg0 )
	{
		this.tocpp.stopRegistrationForObjectClass( ambassadorId, arg0 );
	}

	public void turnInteractionsOff( int arg0 )
	{
		this.tocpp.turnInteractionsOff( ambassadorId, arg0 );
	}

	public void turnInteractionsOn( int arg0 )
	{
		this.tocpp.turnInteractionsOn( ambassadorId, arg0 );
	}

	public void turnUpdatesOffForObjectInstance( int arg0, AttributeHandleSet arg1 )
	{
		this.tocpp.turnUpdatesOffForObjectInstance( ambassadorId, arg0, fromAHS(arg1) );
	}

	public void turnUpdatesOnForObjectInstance( int arg0, AttributeHandleSet arg1 )
	{
		this.tocpp.turnUpdatesOnForObjectInstance( ambassadorId, arg0, fromAHS(arg1) );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Time Management Methods //////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////

	public void timeAdvanceGrant( LogicalTime arg0 )
	{
		double federationTime = ((DoubleTime)arg0).getTime();
		this.tocpp.timeAdvanceGrant( ambassadorId, federationTime );
	}

	public void timeConstrainedEnabled( LogicalTime arg0 )
	{
		DoubleTime federateTime = (DoubleTime)arg0;
		this.tocpp.timeConstrainedEnabled( ambassadorId, federateTime.getTime() );
	}

	public void timeRegulationEnabled( LogicalTime arg0 )
	{
		DoubleTime federateTime = (DoubleTime)arg0;
		this.tocpp.timeRegulationEnabled( ambassadorId, federateTime.getTime() );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
