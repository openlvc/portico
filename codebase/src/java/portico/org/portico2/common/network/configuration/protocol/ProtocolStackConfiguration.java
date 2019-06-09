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
package org.portico2.common.network.configuration.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.RID;
import org.portico2.common.network.protocol.ProtocolType;
import org.portico2.common.utils.XmlUtils;
import org.w3c.dom.Element;

public class ProtocolStackConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private List<ProtocolConfiguration> protocols;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ProtocolStackConfiguration()
	{
		this.protocols = new ArrayList<ProtocolConfiguration>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Expected format of the element given to us:
	 * <p/>
	 * 
	 * <protocols>
	 *     <encryption|authorization|... />
	 *     <encryption|authorization|... />
	 * </protocols>
	 * 
	 * @param rid The RID object we are populating
	 * @param protocolsElement The root protocols XML element under which all the protocols live
	 * @throws JConfigurationException If there is a problem parsing any of the protocls in the stack
	 */
	public void parseConfiguration( RID rid, Element protocolsElement ) throws JConfigurationException
	{
		for( Element protocolElement : XmlUtils.getChildren(protocolsElement) )
		{
			ProtocolConfiguration configuration = ProtocolType.newConfiguration( rid, protocolElement );
			protocols.add( configuration );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public List<ProtocolConfiguration> getProtocolList()
	{
		return Collections.unmodifiableList( this.protocols );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
