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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.portico2.rti.RTI;
import org.portico2.rti.cli.RtiCli;
import org.portico2.rti.cli.fs.FSContext;
import org.portico2.rti.cli.fs.FSContext.ContextType;
import org.portico2.rti.cli.fs.FSContextFactory;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.federation.FederationManager;

/**
 * Lists the contents of a {@link FSContext} node.
 * <p/>
 * If no context argument is provided, the current context is used 
 * <p/>
 * <b>Expected Usage:</b> ls [context]
 */
public class ListContextContents implements ICommand
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
		FSContext context = container.getCurrentContext();
		if( args.length > 0 )
			context = FSContextFactory.fromPath( container, args[0] );
		
		if( !container.isValidContext(context) )
			throw new IllegalArgumentException( "Path does not exist: " + FSContext.getContextPath(context) );
			
		
		List<String> names = new ArrayList<>();
		names.add( "./" );
		names.add( "../" );
		if( context.getType() == ContextType.RTI )
		{
			names.addAll( getFederationNames(rti) );
		}
		else if( context.getType() == ContextType.Federation )
		{
			String federationName = context.getName();
			names.addAll( getFederateNames(rti, federationName) );
		}
		
		for( String name : names )
			container.getConsole().printf( "%s\n", name );
	}

	private List<String> getFederationNames( RTI rti )
	{
		FederationManager fedman = rti.getFederationManager();
		Collection<Federation> federations = fedman.getActiveFederations();
		List<String> names = new ArrayList<>();
		for( Federation federation : federations )
			names.add( federation.getFederationName() + "/" );
		
		Collections.sort( names );
		return names;
	}
	
	private List<String> getFederateNames( RTI rti, String federation )
	{
		FederationManager fedman = rti.getFederationManager();
		Federation rtiFederation = fedman.getFederation( federation );
		if( rtiFederation == null )
			throw new IllegalArgumentException( "Federation does not exist: " + federation );
			
		List<String> names = new ArrayList<>();
		Set<Federate> federates = rtiFederation.getFederates();
		for( Federate federate : federates )
			names.add( federate.getFederateName() );
		
		Collections.sort( names );
		return names;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
