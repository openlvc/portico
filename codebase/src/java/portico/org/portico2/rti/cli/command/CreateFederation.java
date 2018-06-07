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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.portico.lrc.model.ObjectModel;
import org.portico.utils.fom.FomParser;
import org.portico2.common.messaging.MessageContext;
import org.portico2.rti.cli.RtiCli;
import org.portico2.rti.services.mom.data.FomModule;

/**
 * Creates a new federation given a provided name and fed file
 * <p/>
 * <b>Expected Usage:</b> mkfederation name fedfile
 */
public class CreateFederation implements ICommand
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
		if( args.length != 2 )
		{
			// if there were an incorrect number of arguments, throw an exception
			throw new IllegalArgumentException( "Incorrect number of arguments." +
			                                    " Expected usage: mkfederation name fedfile");
		}
		
		String name = args[0];
		String fed = args[1];
		
		File fedFile = new File( fed );
		URL fedUrl = null;
		try
		{
			fedUrl = fedFile.toURI().toURL();
		}
		catch( MalformedURLException mue )
		{
			throw new IllegalArgumentException( mue );
		}
		
		ObjectModel fom = FomParser.parse( fedUrl );
		ObjectModel.mommify( fom );
		ObjectModel.resolveSymbols( fom );
		
		org.portico2.common.services.federation.msg.CreateFederation create = 
			new org.portico2.common.services.federation.msg.CreateFederation();
		List<FomModule> rawModules = new ArrayList<>();
		rawModules.add( new FomModule(fedUrl) );
		
		create.setFederationName( name );
		create.setModel( fom, rawModules );
		create.setHlaVersion( fom.getHlaVersion() );
		
		MessageContext context = new MessageContext( create );
		container.getRti().getInbox().receiveControlMessage( context, null );
		
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
