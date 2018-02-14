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
package org.portico2.common.network.tcp;

/**
 * Represents the type of message this is as used in the {@link TcpChannel} class.
 */
public enum Type
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	WELCOME
	{
		public byte getByteValue() { return 1; }
		public String toString() { return "Welcome"; }
		public boolean isControlMessage() { return false; }
	},
	
	READY
	{
		public byte getByteValue() { return 2; }
		public String toString() { return "Ready"; }
		public boolean isControlMessage() { return false; }
	},

	
	
	DATA_MESSAGE
	{
		public byte getByteValue() { return 10; }
		public String toString() { return "DataMessage"; }
		public boolean isControlMessage() { return false; }
	},

	CONTROL_REQ_SYNC
	{
		public byte getByteValue() { return 11; }
		public String toString() { return "ControlSync"; }
		public boolean isControlMessage() { return true; }
	},

	CONTROL_REQ_ASYNC
	{
		public byte getByteValue() { return 12; }
		public String toString() { return "ControlAsync"; }
		public boolean isControlMessage() { return true; }
	},

	CONTROL_RESP
	{
		public byte getByteValue() { return 13; }
		public String toString() { return "ControlResp"; }
		public boolean isControlMessage() { return true; }
	},

	RTI_PROBE
	{
		public byte getByteValue() { return 14; }
		public String toString() { return "RtiProbe"; }
		public boolean isControlMessage() { return false; }
	},

	
	
	BUNDLE
	{
		public byte getByteValue() { return 127; }
		public String toString() { return "Bundle"; }
		public boolean isControlMessage() { return false; }
	};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public abstract byte getByteValue();
	public abstract String toString();
	public abstract boolean isControlMessage();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static Type fromHeader( byte header )
	{
		switch( header )
		{
			case   1: return WELCOME;
			case   2: return READY;
			case  10: return DATA_MESSAGE;
			case  11: return CONTROL_REQ_SYNC;
			case  12: return CONTROL_REQ_ASYNC;
			case  13: return CONTROL_RESP;
			case  14: return RTI_PROBE;
			case 127: return BUNDLE;
			 default: throw new IllegalArgumentException( "Unknown header value: "+header );
		}
	}

}
