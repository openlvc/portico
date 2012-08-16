/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.portico.bindings.ConnectedRoster;
import org.portico.lrc.model.ObjectModel;

/**
 * This class contains information about a PTalk channel. It lists all the components that are
 * known to the channel, what their names/tags are and what their federation handle is. If the
 * component is not a federate, it doesn't have to join, and in such a case, it is given the
 * default handle of {@link #STEALTH_HANDLE}.
 * <p/>
 * Within a channel, one component is responsible for the management of the roster. The first
 * component listed in the roster is known as the manager. Should that compnent crash or exit,
 * the next component in the list takes over.
 */
public class Roster implements Serializable, ConnectedRoster
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	public static final int STEALTH_HANDLE = -1;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ArrayList<Member> members;
	private ObjectModel model;
	private Member localMember;
	private long creationTime;
	private long lastUpdate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Roster()
	{
		this.members = new ArrayList<Member>();
		this.localMember = null;
		this.model = null;
		this.creationTime = System.currentTimeMillis();
		this.lastUpdate = System.currentTimeMillis();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Gets the master of the Roster, which is defined as the person at the head of the list.
	 */
	public Member getMaster()
	{
		return members.get( 0 );
	}

	/**
	 * Tells the roster to find the member with the given InetAddress and set is as the local
	 * member (the member in which this Roster instance resides). If found and successfully set,
	 * this method will return the member, otherwise null will be returned.
	 */
	public Member setLocalMember( InetAddress address )
	{
		this.localMember = getMember( address );
		return this.localMember;
	}

	/**
	 * Scans the roster and returns the member with the given address. If there is no member
	 * in the roster with that address, null is returned.
	 */
	public Member getMember( InetAddress address )
	{
		for( int i = 0; i < members.size(); i++ )
		{
			Member member = members.get(i);
			if( member.address.equals(address) )
				return member;
		}
		
		return null;
	}
	
	/**
	 * Scans the roster and returns the FIRST member with the given handle. Many members can have
	 * the handle {@link #STEALTH_HANDLE}. If searching for members with that handle, the first
	 * one is returned. For any other valid federate handle, there can only be a single member,
	 * so they can be happily returned.
	 * 
	 * @param handle Federate handle to find the member for
	 * @return The first member found with the given handle
	 */
	public Member getHandle( int handle )
	{
		for( int i = 0; i < members.size(); i++ )
		{
			Member member = members.get(i);
			if( member.handle == handle )
				return member;
		}
		
		return null;
	}

	/**
	 * Takes the givne member and adds it to the roster
	 */
	public void addMember( Member newMember )
	{
		this.members.add( newMember );
		this.lastUpdate = System.currentTimeMillis();
	}
	
	/**
	 * Creates a new member with the given address, adds it to the roster and returns it.
	 */
	public Member addMember( InetSocketAddress memberAddress )
	{
		Member member = new Member( memberAddress );
		this.members.add( member );
		return member;
	}
	
	public void removeMember( Member member )
	{
		if( this.members.remove(member) )
			this.lastUpdate = System.currentTimeMillis();
	}

	/**
	 * Gets the time (millis since Epoch) when the roster was last updated.
	 * @return
	 */
	public long getLastUpdateTime()
	{
		return this.lastUpdate;
	}

	/**
	 * Gets the creation time (millis since Epoch) of the roster. This is used to settle disputes
	 * about who should be the master when two components try to both become it at the same time.
	 * Each stores their creation time, and if one with a lower creation time comes in, they bow
	 * to it and take the sender of it as the master.
	 */
	public long getCreationTime()
	{
		return this.creationTime;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		String newline = System.getProperty( "line.separator" );

		// write basic information
		builder.append( "Roster ("+members.size()+" members): creationTime=" );
		builder.append( creationTime );
		builder.append( ", lastUpdate=" );
		builder.append( lastUpdate );
		builder.append( newline );
		
		// write information about each member
		for( int i = 0; i < members.size(); i++ )
		{
			Member member = members.get( i );
			builder.append( "  ["+i+"] " );
			builder.append( member );
			if( i < members.size()-1 )
				builder.append( newline );
		}
		
		return builder.toString();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	//                             Connected Roster Methods                             // 
	//////////////////////////////////////////////////////////////////////////////////////
	public int getLocalHandle()
	{
		if( localMember == null )
			return STEALTH_HANDLE;
		else
			return localMember.handle;
	}

	public void setLocalHandle( int localHandle )
	{
		if( localMember != null )
			localMember.setHandle( localHandle );
	}

	public Set<Integer> getRemoteHandles()
	{
		HashSet<Integer> set = new HashSet<Integer>();
		for( Member member : members )
		{
			if( member.handle != STEALTH_HANDLE )
				set.add( member.handle );
		}
		
		return set;
	}

	public ObjectModel getFOM()
	{
		return this.model;
	}

	public void setFOM( ObjectModel fom )
	{
		this.model = fom;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Takes the given roster and turns it into a byte[] for transmission
	 */
	public static byte[] marshal( Roster roster )
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject( roster );
			return baos.toByteArray();
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	/**
	 * Takes the given buffer (from the offset, extending length bytes) and turns it into a
	 * Roster object, which is then returned. If the buffer doesn't contain a valid serialized
	 * Roster, a RuntimeException is thrown.
	 */
	public static Roster unmarshal( byte[] buffer, int offset, int length )
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream( buffer, offset, length );
			ObjectInputStream ois = new ObjectInputStream( bais );
			Roster roster = (Roster)ois.readObject();
			return roster;
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Private Class: Member ///////////////////////////////// 
	/////////////////////////////////////////////////////////////////////////////////////////
	public class Member implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;
		private InetSocketAddress address;
		private String name;
		private int handle;
		
		private Member( InetSocketAddress address )
		{
			this.address = address;
			this.name = "Unknown";
			this.handle = STEALTH_HANDLE;
		}
		
		public InetSocketAddress getAddress()
		{
			return this.address;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public void setName( String name )
		{
			this.name = name;
		}
		
		public int getHandle()
		{
			return this.handle;
		}
		
		public void setHandle( int handle )
		{
			this.handle = handle;
		}
		
		public boolean isStealth()
		{
			return this.handle == STEALTH_HANDLE;
		}
		
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append( address );
			builder.append( " (name=" );
			builder.append( name );
			builder.append( ",handle=" );
			if( handle == STEALTH_HANDLE )
				builder.append( "STEALTH_HANDLE" );
			else
				builder.append( handle );
			builder.append( ")" );
			return builder.toString();
		}
		
		@Override
		public boolean equals( Object object )
		{
			if( object instanceof Member )
			{
				Member other = (Member)object;
				return ((address.equals(other.address)) &&
				        (name.equals(other.name)) &&
				        (handle == other.handle) );
			}
			
			return false;
		}
	}
}
