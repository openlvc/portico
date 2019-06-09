/*
 *   Copyright 2006 The Portico Project
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
package org.portico2.common.services.mom.msg;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;
import org.portico2.lrc.LRC;
import org.portico2.lrc.LRCState;

/**
 * This message is a command from the RTI that indicates the destination federate should enable/disable
 * its MOM Exception reporting.
 * <p/>
 * Exception reporting is performed on the LRC side as all parameter and return type information is 
 * available. Additionally it is much easier to catch exceptions on the LRC side.
 * <p/>
 * If MOM Exception reporting is enabled, then service invocation exceptions will be reported to the
 * federation through the <code>HLAmanager.HLAfederate.HLAreport.HLAreportException</code> interaction.
 * 
 * @see LRCState#isExceptionReporting()
 * @see LRC#reportServiceInvocation(String, boolean, Object, String, Object...)
 */
public class SetExceptionReporting extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean reporting;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SetExceptionReporting()
	{
		super();
	}
	
	public SetExceptionReporting( boolean reporting )
	{
		this();
		this.reporting = reporting;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.SetExceptionReporting;
	}
	
	public boolean isExceptionReporting()
	{
		return this.reporting;
	}
	
	public void setExceptionReporting( boolean reporting )
	{
		this.reporting = reporting;
	}
	
	/**
	 * Returns <code>false</code>
	 */
	@Override
	public boolean isSpecDefinedMessage()
	{
		return false;
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	@Override
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.reporting = input.readBoolean();
	}
	
	@Override
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		
		output.writeBoolean( this.reporting );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
