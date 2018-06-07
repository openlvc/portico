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

import java.util.Set;

import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.common.utils.TextDevice;
import org.portico2.rti.RTI;
import org.portico2.rti.cli.RtiCli;
import org.portico2.rti.cli.fs.FSContext;
import org.portico2.rti.cli.fs.FSContextFactory;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.federation.Federation;

/**
 * Lists a federate's object and interaction publication information
 * <p/>
 * If no context argument is provided, the current context is used
 * <p/>
 * <b>Expected Usage:</b> pubs [context]
 */
public class PubInfo implements ICommand
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
			case Federate:
				printPublications( container, target );
				break;
			default:
				throw new IllegalArgumentException("Path: " + FSContext.getContextPath(target) + " is not a federate.");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void printPublications( RtiCli container, FSContext context )
	{
		RTI rti = container.getRti();
		TextDevice console = container.getConsole();
		
		Federation federation = rti.getFederationManager().getFederation( context.getParent().getName() );
		if( federation == null )
			throw new IllegalArgumentException( FSContext.getContextPath(context.getParent()) + " is not a federation" );
	
		Federate federate = federation.getFederate( context.getName() );
		if( federate == null )
			throw new IllegalArgumentException( FSContext.getContextPath(context) + " is not a federate" );
		
		ObjectModel fom = federation.getFOM();
		int federateHandle = federate.getFederateHandle();
		InterestManager interests = federation.getInterestManager();
		
		Set<Integer> objectClasses = interests.getAllPublishedObjectClasses( federateHandle );
		for( int ocHandle : objectClasses )
		{
			OCMetadata oc = fom.getObjectClass( ocHandle );
			console.printf( "%s (%d)\n", oc.getQualifiedName(), ocHandle );
			Set<Integer> attHandles = interests.getPublishedAttributes( federateHandle, ocHandle );
			for( int attHandle : attHandles )
			{
				ACMetadata ac = oc.getAttribute( attHandle );
				console.printf( "\t%s (%d)\n", ac.getName(), attHandle );
			}
		}
		
		Set<Integer> interactionClasses = interests.getAllPublishedInteractionClasses( federateHandle );
		for( int icHandle : interactionClasses )
		{
			ICMetadata ic = fom.getInteractionClass( icHandle );
			console.printf( "%s (%d)\n", ic.getQualifiedName(), icHandle );
		}
	}
	
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
