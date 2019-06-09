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
package org.portico2.rti.cli.command;

import java.util.List;

import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.utils.TextDevice;
import org.portico2.rti.RTI;
import org.portico2.rti.cli.RtiCli;
import org.portico2.rti.cli.fs.FSContext;
import org.portico2.rti.cli.fs.FSContextFactory;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.federation.FederateMetrics;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.mom.data.FomModule;

/**
 * Lists MOM information about a federation or federate {@link FSContext}.
 * <p/>
 * If no context argument is provided, the current context is used
 * <p/>
 * <b>Expected Usage:</b> mominfo [context]
 */
public class MomInfo implements ICommand
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void execute( RtiCli container, String... args )
	{
		RTI rti = container.getRti();
		FSContext target = container.getCurrentContext();
		if( args.length > 0 )
			target = FSContextFactory.fromPath( container, args[0] );

		if( !container.isValidContext(target) )
			throw new IllegalArgumentException("Path does not exist: " + FSContext.getContextPath(target) );
		
		switch( target.getType() )
		{
			case Federation:
				printFederationStatus( container, target );
				break;
			case Federate:
				printFederateStatus( container, target );
				break;
			default:
				throw new IllegalArgumentException("Path: " + FSContext.getContextPath(target) + " is not a federation or a federate.");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void printFederationStatus( RtiCli container, FSContext context )
	{
		RTI rti = container.getRti(); 
		Federation federation = rti.getFederationManager().getFederation( context.getName() );
		if( federation == null )
		{
			String contextName = context.getHeirachicalName();
			if( contextName.isEmpty() )
				contextName = "/";
			throw new IllegalArgumentException( FSContext.getContextPath(context) + " is not a federation" );
		}
	
		List<FomModule> modules = federation.getRawFomModules();
		int moduleCount = modules.size();
		String[] designators = new String[moduleCount];
		for( int i = 0 ; i < moduleCount ; ++i )
			designators[i] = modules.get( i ).getDesignator();
		String designatorList = String.join( ",", designators );
		
		// All the MOM properties that we support for the federation
		TextDevice console = container.getConsole();
		paddedPrintln( console, "HLAfederationName:", federation.getFederationName() );
		paddedPrintln( console, "HLAfederatesInFederation:", federation.getFederates().size() );
		paddedPrintln( console, "HLAFOMmoduleDesignatorList:", designatorList );
		paddedPrintln( console, "HLARTIversion:", container.getEnvironmentVariables().get("RTI_VERSION") );
	}
	
	private void printFederateStatus( RtiCli container, FSContext context )
	{
		RTI rti = container.getRti(); 
		Federation federation = rti.getFederationManager().getFederation( context.getParent().getName() );
		if( federation == null )
			throw new IllegalArgumentException( FSContext.getContextPath(context.getParent()) + " is not a federation" );
		
		Federate federate = federation.getFederate( context.getName() );
		if( federate == null )
			throw new IllegalArgumentException( FSContext.getContextPath(context) + " is not a federate" );
		
		List<FomModule> modules = federate.getRawFomModules();
		int moduleCount = modules.size();
		String[] designators = new String[moduleCount];
		for( int i = 0 ; i < moduleCount ; ++i )
			designators[i] = modules.get( i ).getDesignator();
		String designatorList = String.join( ",", designators );
		
		// All the MOM properties that we support for the federate
		TextDevice console = container.getConsole();
		TimeStatus time = federation.getTimeManager().getTimeStatus( federate.getFederateHandle() );
		FederateMetrics metrics = federate.getMetrics();
		
		paddedPrintln( console, "HLAfederateHandle:", federate.getFederateHandle() );
		paddedPrintln( console, "HLAfederateName:", federate.getFederateName() );
		paddedPrintln( console, "HLAfederateType:", federate.getFederateType() );
		paddedPrintln( console, "HLARTIversion:", container.getEnvironmentVariables().get("RTI_VERSION") );
		paddedPrintln( console, "HLAFOMmoduleDesignatorList:", designatorList );
		paddedPrintln( console, "HLAtimeConstrained:", time.isConstrained() );
		paddedPrintln( console, "HLAtimeRegulating:", time.isRegulating() );
		paddedPrintln( console, "HLAasynchronousDelivery:", time.isAsynchronous() );
		//paddedPrintln( console, "HLAfederateState:", );
		//paddedPrintln( console, "HLAtimeManagerState:", );
		paddedPrintln( console, "HLAlogicalTime:", time.currentTime );
		paddedPrintln( console, "HLAlookahead:", time.lookahead );
		paddedPrintln( console, "HLALBTS:", time.lbts );
		//paddedPrintln( console, "HLAGALT:", );
		//paddedPrintln( console, "HLALITS:", );
		//paddedPrintln( console, "HLAROlength:", );
		//paddedPrintln( console, "HLATSOlength:", );
		paddedPrintln( console, "HLAreflectionsReceived:", metrics.getTotalReflectionsReceived() );
		paddedPrintln( console, "HLAupdatesSent:", metrics.getTotalUpdatesSent() );
		paddedPrintln( console, "HLAinteractionsReceived:", metrics.getTotalInteractionsReceived() );
		paddedPrintln( console, "HLAinteractionsSent:", metrics.getTotalInteractionsSent() );
		paddedPrintln( console, "HLAobjectInstancesThatCanBeDeleted:", metrics.getObjectsOwned().size() );
		paddedPrintln( console, "HLAobjectInstancesUpdated:", metrics.getTotalObjectInstancesUpdated() );
		paddedPrintln( console, "HLAobjectInstancesReflected:", metrics.getTotalObjectInstancesReflected() );
		paddedPrintln( console, "HLAobjectInstancesDeleted:", metrics.getObjectsDeleted() );
		paddedPrintln( console, "HLAobjectInstancesRemoved:", metrics.getObjectsRemoved() );
		paddedPrintln( console, "HLAobjectInstancesRegistered:", metrics.getObjectsRegistered() );
		paddedPrintln( console, "HLAobjectInstancesDiscovered:", metrics.getObjectsDiscovered() );
		paddedPrintln( console, "HLAtimeGrantedTime:", time.getCurrentTime() );
		paddedPrintln( console, "HLAtimeAdvancingTime:", time.getRequestedTime() );
		
	}
	
	private void paddedPrintln( TextDevice console, String key, String value )
	{
		console.printf( "%-36s%s\n", key, value );
	}
	
	private void paddedPrintln( TextDevice console, String key, boolean value )
	{
		console.printf( "%-36s%s\n", key, value ? "true" : "false" );
	}
	
	private void paddedPrintln( TextDevice console, String key, int value )
	{
		console.printf( "%-36s%d\n", key, value );
	}
	
	private void paddedPrintln( TextDevice console, String key, double value )
	{
		console.printf( "%-36s%f\n", key, value );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
