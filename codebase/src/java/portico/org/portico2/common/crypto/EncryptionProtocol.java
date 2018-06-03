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
package org.portico2.common.crypto;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.network.Connection;
import org.portico2.common.network.IProtocol;
import org.portico2.common.network.Message;
import org.portico2.common.network.configuration.CryptoConfiguration;

public class EncryptionProtocol implements IProtocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private CryptoConfiguration configuration;
	private CryptoManager cryptoManager;
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public EncryptionProtocol()
	{
		this.configuration = null;   // set in configure()
		this.cryptoManager = null;   // set in configure(), initialized in open()
		this.logger = null;          // set in configure()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure( Connection hostConnection ) throws JConfigurationException
	{
		this.configuration = hostConnection.getConfiguration().getCryptoConfiguration();
		this.cryptoManager = new CryptoManager( configuration );
		this.logger = hostConnection.getLogger();
	}

	@Override
	public void open()
	{
		if( cryptoManager.isEnabled() )
			cryptoManager.initialize();
	}
	
	@Override
	public void close()
	{
		// no-op
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean down( Message message )
	{
		if( cryptoManager.isEnabled() == false )
			return true;
		
		return true;
	}

	@Override
	public boolean up( Message message )
	{
		if( cryptoManager.isEnabled() == false )
			return true;
		
		return true;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName()
	{
		return "Encrypter";
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
