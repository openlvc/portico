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
package org.portico2.common.network.transport.multicast;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.portico2.common.network.configuration.transport.MulticastConfiguration;

/**
 * This class contains properties that are used to set JGroups protocol stack settings.
 * <p/>
 * Before creating a JChannel, instances of this object are configured. A call to the method
 * {@link #copyToSystemProperties()} should be made before creating the channel. This will remove
 * any pre-existing Portico JGroups related properties and add any that have been configured on
 * the local instance.
 * <p/>
 * The JGroups configurator should then pick these up and substitute them into one of of the config
 * templates that are stored in the Portico jar file. 
 */
public class JGroupsConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	//
	// JGroups Substitute Values: Subbed into JGroups XML config files
	//
	private static final String KEY_BIND_ADDRESS = "portico.jg.udp.bindAddress";
	private static final String KEY_IS_MULTICAST = "portico.jg.udp.isMulticast";
	
	private static final String KEY_MC_GROUP_ADDRESS = "portico.jg.multicast.address";
	private static final String KEY_MC_PORT          = "portico.jg.multicast.port";
	private static final String KEY_MC_RECV_BUF_SIZE = "portico.jg.multicast.recv.buffer";
	private static final String KEY_MC_SEND_BUF_SIZE = "portico.jg.multicast.send.buffer";

	private static final String KEY_GMS_JOIN_TIMEOUT = "portico.jg.gms.joinTimeout";
	
	private static final String KEY_FLOW_UC_MAX_CREDITS   = "portico.jg.flow.uc.maxCredits"; 
	private static final String KEY_FLOW_UC_MIN_THRESHOLD = "portico.jg.flow.uc.minThreshold";
	private static final String KEY_FLOW_MC_MAX_CREDITS   = "portico.jd.flow.mc.maxCredits";
	private static final String KEY_FLOW_MC_MIN_THRESHOLD = "portico.jg.flow.mc.minThreshold";

	private static final String KEY_FRAG_SIZE = "portico.jg.frag.size";

	// Property Defaults
	public static String DEFAULT_GMS_JOIN_TIMOUT = "2000";
	public static String DEFAULT_INTERFACE       = "SITE_LOCAL";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Properties props;	
	
	// These properties are stored locally and not passed through to JGroups XML config files
	private String channelName;
	private boolean useDaemonThreads;
	private String loggerPrefix;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public JGroupsConfiguration( String channelName )
	{
		this.props = new Properties();

		this.channelName = channelName;
		this.useDaemonThreads = true;
		this.loggerPrefix = channelName;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Go through all system properties and remove any starting with <code>portico.jg.</code>.
	 * After that, for any local properties that have been set on this instance, push them into
	 * the system properties. After this call you can create a JChannel and the properties here
	 * should cross-reference against any of the JGroups XML configuration templates stored inside
	 * the Portico jar files, replacing values inside them appropriately.
	 * (See <code>resources/jar/portico.jar/jgroups-*.xml</code> for those templated).
	 */
	public void copyToSystemProperties()
	{
		// remove any existing properties
		Set<String> toRemove = new HashSet<>();
		for( String property : System.getProperties().stringPropertyNames() )
		{
			if( property.startsWith("portico.jg.") )
				toRemove.add( property );
		}
		
		for( String property : toRemove )
		{
			System.getProperties().remove( property );
		}
		
		// add in out current set
		for( String property : this.props.stringPropertyNames() )
			System.setProperty( property, this.props.getProperty(property) );
	}
	
	//////////////////////////////////////////////////////////////////////////
	///  Non Pass-Through Mutators   /////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	public String getChannelName()
	{
		return this.channelName;
	}
	
	public boolean useDamonThreads() { return this.useDaemonThreads; }
	public void setUseDaemonThreads( boolean useDaemons ) { this.useDaemonThreads = useDaemons; }
	
	public String getLoggerPrefix() { return this.loggerPrefix; }
	public void setLoggerPrefix( String prefix ) { this.loggerPrefix = prefix; }

	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	public String getBindAddress()               { return props.getProperty(KEY_BIND_ADDRESS); }
	public String getMulticast()                 { return props.getProperty(KEY_IS_MULTICAST); }
	
	public String getMulticastGroupAddress()     { return props.getProperty(KEY_MC_GROUP_ADDRESS); }
	public String getMulticastPort()             { return props.getProperty(KEY_MC_PORT); }
	public String getMulticastReceiveBuffer()    { return props.getProperty(KEY_MC_RECV_BUF_SIZE); }
	public String getMulticastSendBuffer()       { return props.getProperty(KEY_MC_SEND_BUF_SIZE); }

	public long getGmsJoinTimeout()              { return Long.parseLong(props.getProperty(KEY_GMS_JOIN_TIMEOUT,"2000")); }
	
	public String getUnicastFlowMaxCredits()     { return props.getProperty(KEY_FLOW_UC_MAX_CREDITS); }
	public String getUnicastFlowMinThreshold()   { return props.getProperty(KEY_FLOW_UC_MIN_THRESHOLD); }
	public String getMulticastFlowMaxCredits()   { return props.getProperty(KEY_FLOW_MC_MAX_CREDITS); }
	public String getMulticastFlowMinThreshold() { return props.getProperty(KEY_FLOW_MC_MIN_THRESHOLD); }
	
	public String getFragSize()                  { return props.getProperty(KEY_FRAG_SIZE); }


	//////////////////////////////////////////////////////////////////////////
	///  Pass-Through Setters   //////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	public void setBindAddress( String value )   { props.setProperty(KEY_BIND_ADDRESS,sanitizeAddress(value)); }
	public void setMulticast( boolean isMC )     { props.setProperty(KEY_IS_MULTICAST,""+isMC); }
	
	public void setMulticastGroupAddress( String value ) { props.setProperty(KEY_MC_GROUP_ADDRESS,sanitizeAddress(value)); }
	public void setMulticastPort( String value )         { props.setProperty(KEY_MC_PORT,value); }
	public void setMulticastReceiveBuffer( String value ){ props.setProperty(KEY_MC_RECV_BUF_SIZE,value); }
	public void setMulticastSendBuffer( String value )   { props.setProperty(KEY_MC_SEND_BUF_SIZE,value); }
	
	public void setGmsJoinTimeout( String value ) { props.setProperty(KEY_GMS_JOIN_TIMEOUT,value); }
	
	public void setUnicastFlowMaxCredits( String value )      { props.setProperty(KEY_FLOW_UC_MAX_CREDITS,value); }
	public void setUnitcastFlowMinThreshold( String value )   { props.setProperty(KEY_FLOW_UC_MIN_THRESHOLD,value); }
	public void setMulticastFlowMaxCredits( String value )    { props.setProperty(KEY_FLOW_MC_MAX_CREDITS,value); }
	public void setMultitcastFlowMinThreshold( String value ) { props.setProperty(KEY_FLOW_MC_MIN_THRESHOLD,value); }
	
	public void setFragSize( String value ) { props.setProperty(KEY_FRAG_SIZE,value); }
	
	
	private String sanitizeAddress( String address )
	{
		address = address.trim();
		if( address.startsWith("/") )
			address = address.substring( 1 );
		
		return address;
	}

	
	//////////////////////////////////////////////////////////////////////////
	///  Helper Methods   ////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	/**
	 * Set the properties of this configuration object from those contained in the
	 * {@link MulticastConnectionConfiguration} that was pulled from the RID file
	 * 
	 * @param configuration The object to set out configuration options from
	 */
	public void fromRidConfiguration( MulticastConfiguration configuration )
	{
		// start fresh
		this.props.clear();
		
		// copy over settings
		this.setBindAddress( configuration.getNicAddress().getHostAddress() );
		this.setMulticast( true );
		this.setMulticastGroupAddress( configuration.getAddress().toString() );
		this.setMulticastPort( configuration.getPort()+"" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
