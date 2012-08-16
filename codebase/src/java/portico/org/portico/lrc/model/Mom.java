/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.model;

import java.util.HashMap;
import java.util.HashSet;

import org.portico.lrc.compat.JAttributeNotDefined;

/**
 * This class helps do MOM related stuff (including 1.3/1516 conversions). As a result, it is VERY
 * ugly internally. I can't understand why the IEEE 1516 standards group decided to ENTIRELY BREAK
 * ALL BACKWARDS COMPATIBILITY by prefixing everything with "HLA", but they did. As a result, we
 * have to do all sorts of ugly stuff we wouldn't normally have to do, so get used to it.
 */
public class Mom
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final int ManagerClass    = 1;
	public static final int FederateClass   = 2;
	public static final int FederationClass = 3;
	
	private static final Mom INSTANCE = new Mom();
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	/** Identifiers for the attributes of the Federate MOM class. This is to get around the
	    STUPIDITY that was involved in prefixing all standardized types with "HLA" in 1516.
	    Who ever came up with that bright idea, please be sure never to work with any heavy
	    machinery.
	*/
	public enum Federate
	{
		FederateHandle(4),
		FederateType(5),
		FederateHost(6),
		RtiVersion(7),
		FedID(8),
		TimeConstrained(9),
		TimeRegulating(10),
		AsynchronousDelivery(11),
		FederateState(12),
		TimeManagerState(13),
		LogicalTime(14),
		Lookahead(15),
		LBTS(16), // synonym for LITS
		GALT(17),
		LITS(18), // NextMinEventTime in 1.3,
		ROlength(19),
		TSOlength(20),
		ReflectionsReceived(21),
		UpdatesSent(22),
		InteractionsReceived(22),
		InteractionsSent(23),
		ObjectInstancesThatCanBeDeleted(24), // ObjectsOwned in 1.3
		ObjectInstancesUpdated(25),          // ObjectsUpdated in 1.3
		ObjectInstancesReflected(26),        // ObjectsReflected in 1.3
		ObjectInstancesDeleted(27),
		ObjectInstancesRemoved(28),
		ObjectInstancesRegistered(29),
		ObjectInstancesDiscovered(30),
		TimeGrantedTime(31),
		TimeAdvancingTime(32);
		
		public final int handle;
		private Federate( int handle )
		{
			this.handle = handle;
		}
		
		/**
		 * Find the attribute for the given handle. If no value can be found for that handle,
		 * an exception is thrown.
		 */
		public static Federate forHandle( int handle ) throws JAttributeNotDefined
		{
			for( Federate federate : Federate.values() )
			{
				if( federate.handle == handle )
					return federate;
			}
			
			throw new JAttributeNotDefined( "MOM Federate attribute handle not known: " + handle );
		}
	}
	
	/** Same solution as for Federate above, see that comment for my thoughts */
	public enum Federation
	{
		FederationName(33),
		FederatesInFederation(34),
		RtiVersion(35),
		FedID(36),
		LastSaveName(37),
		LastSaveTime(38),
		NextSaveName(39),
		NextSaveTime(40),
		AutoProvide(41),                // not in 1.3
		ConveyRegionDesignatorSets(42); // not in 1.3
		
		public final int handle;
		private Federation( int handle )
		{
			this.handle = handle;
		}
		
		/**
		 * Find the attribute for the given handle. If no value can be found for that handle,
		 * an exception is thrown.
		 */
		public static Federation forHandle( int handle ) throws JAttributeNotDefined
		{
			for( Federation attribute : Federation.values() )
			{
				if( attribute.handle == handle )
					return attribute;
			}
			
			throw new JAttributeNotDefined( "MOM Federation attribute handle not known: "+handle );
		}
	}
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HashMap<String,Mom.Federate> federate13Attributes;
	private HashMap<String,Mom.Federation> federation13Attributes;
	private HashMap<String,Mom.Federate> federate1516Attributes;
	private HashMap<String,Mom.Federation> federation1516Attributes;
	
	private HashSet<Integer> allFederateHandles;
	private HashSet<Integer> allFederationHandles;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	private Mom()
	{
		this.federate13Attributes     = new HashMap<String,Mom.Federate>();
		this.federation13Attributes   = new HashMap<String,Mom.Federation>();
		this.federate1516Attributes   = new HashMap<String,Mom.Federate>();
		this.federation1516Attributes = new HashMap<String,Mom.Federation>();
		this.allFederateHandles       = new HashSet<Integer>();
		this.allFederationHandles     = new HashSet<Integer>();
		
		////////////////////////////////////////////////////////////////
		////////////////////////// Initialize //////////////////////////
		////////////////////////////////////////////////////////////////
		federate13Attributes.put( "FederateHandle", Federate.FederateHandle );
		federate13Attributes.put( "FederateType", Federate.FederateType );
		federate13Attributes.put( "FederateHost", Federate.FederateHost );
		federate13Attributes.put( "RTIversion", Federate.RtiVersion );
		federate13Attributes.put( "FEDid", Federate.FedID );
		federate13Attributes.put( "TimeConstrained", Federate.TimeConstrained );
		federate13Attributes.put( "TimeRegulating", Federate.TimeRegulating );
		federate13Attributes.put( "AsynchronousDelivery", Federate.AsynchronousDelivery );
		federate13Attributes.put( "FederateState", Federate.FederateState );
		federate13Attributes.put( "TimeManagerState", Federate.TimeManagerState );
		federate13Attributes.put( "FederateTime", Federate.LogicalTime );
		federate13Attributes.put( "Lookahead", Federate.Lookahead );
		federate13Attributes.put( "LBTS", Federate.LBTS );
		federate13Attributes.put( "MinNextEventTime", Federate.LITS );
		federate13Attributes.put( "ROlength", Federate.ROlength );
		federate13Attributes.put( "TSOlength", Federate.TSOlength );
		federate13Attributes.put( "ReflectionsReceived", Federate.ReflectionsReceived );
		federate13Attributes.put( "UpdatesSent", Federate.UpdatesSent );
		federate13Attributes.put( "InteractionsReceived", Federate.InteractionsReceived );
		federate13Attributes.put( "InteractionsSent", Federate.InteractionsSent );
		federate13Attributes.put( "ObjectsOwned", Federate.ObjectInstancesThatCanBeDeleted );
		federate13Attributes.put( "ObjectsUpdated", Federate.ObjectInstancesUpdated );
		federate13Attributes.put( "ObjectsReflected", Federate.ObjectInstancesReflected );
		
		federation13Attributes.put( "FederationName", Federation.FederationName );
		federation13Attributes.put( "FederatesInFederation", Federation.FederatesInFederation );
		federation13Attributes.put( "RTIversion", Federation.RtiVersion );
		federation13Attributes.put( "FEDid", Federation.FedID );
		federation13Attributes.put( "LastSaveName", Federation.LastSaveName );
		federation13Attributes.put( "LastSaveTime", Federation.LastSaveTime );
		federation13Attributes.put( "NextSaveName", Federation.NextSaveName );
		federation13Attributes.put( "NextSaveTime", Federation.NextSaveTime );
		
		// HLA 1516 //
		federate1516Attributes.put( "HLAfederateHandle", Federate.FederateHandle );
		federate1516Attributes.put( "HLAfederateType", Federate.FederateType );
		federate1516Attributes.put( "HLARTIversion", Federate.RtiVersion );
		federate1516Attributes.put( "HLAFDDID", Federate.FedID );
		federate1516Attributes.put( "HLAtimeConstrained", Federate.TimeConstrained );
		federate1516Attributes.put( "HLAtimeRegulating", Federate.TimeRegulating );
		federate1516Attributes.put( "HLAasynchronousDelivery", Federate.AsynchronousDelivery );
		federate1516Attributes.put( "HLAfederateState", Federate.FederateState );
		federate1516Attributes.put( "HLAtimeManagerState", Federate.TimeManagerState );
		federate1516Attributes.put( "HLAlogicalTime", Federate.LogicalTime );
		federate1516Attributes.put( "HLAlookahead", Federate.Lookahead );
		federate1516Attributes.put( "HLAGALT", Federate.GALT );
		federate1516Attributes.put( "HLALITS", Federate.LITS );
		federate1516Attributes.put( "HLAROlength", Federate.ROlength );
		federate1516Attributes.put( "HLATSOlength", Federate.TSOlength );
		federate1516Attributes.put( "HLAreflectionsReceived", Federate.ReflectionsReceived );
		federate1516Attributes.put( "HLAupdatesSent", Federate.UpdatesSent );
		federate1516Attributes.put( "HLAinteractionsReceived", Federate.InteractionsReceived );
		federate1516Attributes.put( "HLAinteractionsSent", Federate.InteractionsSent );
		federate1516Attributes.put( "HLAobjectInstancesThatCanBeDeleted", Federate.ObjectInstancesThatCanBeDeleted );
		federate1516Attributes.put( "HLAobjectInstancesUpdated", Federate.ObjectInstancesUpdated );
		federate1516Attributes.put( "HLAobjectInstancesReflected", Federate.ObjectInstancesReflected );
		federate1516Attributes.put( "HLAobjectInstancesDeleted", Federate.ObjectInstancesDeleted );
		federate1516Attributes.put( "HLAobjectInstancesRemoved", Federate.ObjectInstancesRemoved );
		federate1516Attributes.put( "HLAobjectInstancesRegistered", Federate.ObjectInstancesRegistered );
		federate1516Attributes.put( "HLAobjectInstancesDiscovered", Federate.ObjectInstancesDiscovered );
		federate1516Attributes.put( "HLAtimeGrantedTime", Federate.TimeGrantedTime );
		federate1516Attributes.put( "HLAtimeAdvancingTime", Federate.TimeAdvancingTime );

		federation1516Attributes.put( "HLAfederationName", Federation.FederationName );
		federation1516Attributes.put( "HLAfederatesInFederation", Federation.FederatesInFederation );
		federation1516Attributes.put( "HLARTIversion", Federation.RtiVersion );
		federation1516Attributes.put( "HLAFDDID", Federation.FedID );
		federation1516Attributes.put( "HLAlastSaveName", Federation.LastSaveName );
		federation1516Attributes.put( "HLAlastSaveTime", Federation.LastSaveTime );
		federation1516Attributes.put( "HLAnextSaveName", Federation.NextSaveName );
		federation1516Attributes.put( "HLAnextSaveTime", Federation.NextSaveTime );
		federation1516Attributes.put( "HLAautoProvide", Federation.AutoProvide );
		federation1516Attributes.put( "HLAconveyRegionDesignatorSets", Federation.ConveyRegionDesignatorSets );
		
		// All Handles //
		allFederateHandles.add( Federate.FederateHandle.handle );
		allFederateHandles.add( Federate.FederateType.handle );
		allFederateHandles.add( Federate.FederateHost.handle );
		allFederateHandles.add( Federate.RtiVersion.handle );
		allFederateHandles.add( Federate.FedID.handle );
		allFederateHandles.add( Federate.TimeConstrained.handle );
		allFederateHandles.add( Federate.TimeRegulating.handle );
		allFederateHandles.add( Federate.AsynchronousDelivery.handle );
		allFederateHandles.add( Federate.FederateState.handle );
		allFederateHandles.add( Federate.TimeManagerState.handle );
		allFederateHandles.add( Federate.LogicalTime.handle );
		allFederateHandles.add( Federate.Lookahead.handle );
		allFederateHandles.add( Federate.LBTS.handle );
		allFederateHandles.add( Federate.GALT.handle );
		allFederateHandles.add( Federate.LITS.handle );
		allFederateHandles.add( Federate.ROlength.handle );
		allFederateHandles.add( Federate.TSOlength.handle );
		allFederateHandles.add( Federate.ReflectionsReceived.handle );
		allFederateHandles.add( Federate.UpdatesSent.handle );
		allFederateHandles.add( Federate.InteractionsReceived.handle );
		allFederateHandles.add( Federate.InteractionsSent.handle );
		allFederateHandles.add( Federate.ObjectInstancesThatCanBeDeleted.handle );
		allFederateHandles.add( Federate.ObjectInstancesUpdated.handle );
		allFederateHandles.add( Federate.ObjectInstancesReflected.handle );
		allFederateHandles.add( Federate.ObjectInstancesDeleted.handle );
		allFederateHandles.add( Federate.ObjectInstancesRemoved.handle );
		allFederateHandles.add( Federate.ObjectInstancesRegistered.handle );
		allFederateHandles.add( Federate.ObjectInstancesDiscovered.handle );
		allFederateHandles.add( Federate.TimeGrantedTime.handle );
		allFederateHandles.add( Federate.TimeAdvancingTime.handle );
		
		allFederationHandles.add( Federation.FederationName.handle );
		allFederationHandles.add( Federation.FederatesInFederation.handle );
		allFederationHandles.add( Federation.RtiVersion.handle );
		allFederationHandles.add( Federation.FedID.handle );
		allFederationHandles.add( Federation.LastSaveName.handle );
		allFederationHandles.add( Federation.LastSaveTime.handle );
		allFederationHandles.add( Federation.NextSaveName.handle );
		allFederationHandles.add( Federation.NextSaveTime.handle );
		allFederationHandles.add( Federation.AutoProvide.handle );
		allFederationHandles.add( Federation.ConveyRegionDesignatorSets.handle );
	}
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static HashSet<Integer> getFederateAttributes()
	{
		// return a COPY!!
		return new HashSet<Integer>( INSTANCE.allFederateHandles );
	}
	
	public static HashSet<Integer> getFederationAttributes()
	{
		// return a COPY!!
		return new HashSet<Integer>( INSTANCE.allFederationHandles );
	}
	
	/**
	 * Fetch the handle for the MOM class of the given name. This will work when given either
	 * HLA 1.3 or HLA 1516 style MOM names. If the class name is unknown,
	 * {@link ObjectModel#INVALID_HANDLE} will be returned.
	 */
	public static int getMomClassHandle( String className )
	{
		////////////////////////////////
		// strip out any HLA prefixes //
		////////////////////////////////
		className = className.toLowerCase();
		if( className.startsWith("hla") )
		{
			className = className.replaceFirst( "hla", "" );
		}
		if( className.startsWith("objectroot.") )
		{
			className = className.replaceFirst( "objectroot.", "" );
		}

		/////////////////////////////////////
		// try and find the relevant class //
		/////////////////////////////////////
		if( className.equals("manager") )
		{
			return ManagerClass;
		}
		else if( className.equals("manager.federate") || className.equals("federate") )
		{
			return FederateClass;
		}
		else if( className.equals("manager.federation") || className.equals("federation") )
		{
			return FederationClass;
		}
		else
		{
			// if we get here, we haven't go any sort of valid name! :S
			return ObjectModel.INVALID_HANDLE;
		}
	}

	/**
	 * Fetch the handle for the MOM attribute of the given name in the MOM class of the given
	 * handle. If the handle is not recognized or the name isn't a valid attribute,
	 * {@link ObjectModel#INVALID_HANDLE} will be returned.
	 */
	public static int getMomAttributeHandle( int classHandle, String name )
	{
		// figure out if this is a 1516 or 1.3 request
		if( name.startsWith("HLA") )
		{
			/////////////////////////
			// it is a 1516 handle //
			/////////////////////////
			switch( classHandle )
			{
				case FederateClass:
					return INSTANCE.federate1516Attributes.get(name).handle;
				case FederationClass:
					return INSTANCE.federation1516Attributes.get(name).handle;
				default:
					return ObjectModel.INVALID_HANDLE;
			}
		}
		else
		{
			////////////////////////
			// it is a 1.3 handle //
			////////////////////////
			switch( classHandle )
			{
				case FederateClass:
					return INSTANCE.federate13Attributes.get(name).handle;
				case FederationClass:
					return INSTANCE.federation13Attributes.get(name).handle;
				default:
					return ObjectModel.INVALID_HANDLE;
			}
		}
	}

	/**
	 * This method will remove "HLA" from the front of any provided string and return the resulting
	 * string. If the next letter is lower case, it will be made upper case. This will work on "."
	 * separated names. For example: "HLAobjectRoot.HLAmanager" will become "ObjectRoot.Manager".
	 */
	public static String strip1516Crap( String name )
	{
		// break the string apart
		String[] strings = name.split( "\\." );
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < strings.length; i++ )
		{
			String temp = strings[i];

			// remove the "HLA" if it exists
			if( temp.startsWith("HLA") )
				temp = temp.substring(3);
			
			// make the first letter upper case (if it already is, this will have no effect)
			String firstOnly = "" + temp.charAt( 0 );
			temp = temp.substring(1);
			builder.append( firstOnly.toUpperCase() );
			builder.append( temp );
			
			if( (i+1) != strings.length )
				builder.append( "." );
		}
		
		return builder.toString();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Object Model Update Methods //////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Build the hierarchy of MOM object classes and initialize them with the default handles. This
	 * method will also patch the MOM stuff into the model, adding all the classes and setting the
	 * manager class (for example) as a child of the object root.
	 */
	public static void insertMomHierarchy( ObjectModel theModel )
	{
		// create the manager class //
		OCMetadata manager = new OCMetadata( "HLAmanager", ManagerClass );
		manager.setModel( theModel );
		manager.setParent( theModel.getObjectRoot() );
		
		///////////////////////////////////////////////////////////////
		////////////////// create the federate class //////////////////
		///////////////////////////////////////////////////////////////
		OCMetadata federate = new OCMetadata( "HLAfederate", FederateClass );
		federate.setModel( theModel );
		federate.setParent( manager );
		
		// populate it //
		ACMetadata federateHandle = newAttribute( "HLAfederateHandle",
		                                          Federate.FederateHandle.handle );
		federate.addAttribute( federateHandle );
		
		ACMetadata federateType = newAttribute( "HLAfederateType", Federate.FederateType.handle );
		federate.addAttribute( federateType );
		
		ACMetadata federateHost = newAttribute( "HLAfederateHost", Federate.FederateHost.handle );
		federate.addAttribute( federateHost );
		
		ACMetadata rtiVersion = newAttribute( "HLARTIversion", Federate.RtiVersion.handle );
		federate.addAttribute( rtiVersion );
		
		ACMetadata fddID = newAttribute( "HLAFDDID", Federate.FedID.handle );
		federate.addAttribute( fddID );
		
		ACMetadata timeConstrained = newAttribute( "HLAtimeConstrained",
		                                           Federate.TimeConstrained.handle );
		federate.addAttribute( timeConstrained );
		
		ACMetadata timeRegulating = newAttribute( "HLAtimeRegulating",
		                                          Federate.TimeRegulating.handle );
		federate.addAttribute( timeRegulating );
		
		ACMetadata async = newAttribute( "HLAasynchronousDelivery",
		                                 Federate.AsynchronousDelivery.handle );
		federate.addAttribute( async );
		
		ACMetadata state = newAttribute( "HLAfederateState", Federate.FederateState.handle );
		federate.addAttribute( state );
		
		ACMetadata timeState = newAttribute( "HLAtimeManagerState",
		                                     Federate.TimeManagerState.handle );
		federate.addAttribute( timeState );
		
		ACMetadata logicalTime = newAttribute( "HLAlogicalTime", Federate.LogicalTime.handle );
		federate.addAttribute( logicalTime );
		
		ACMetadata lookahead = newAttribute( "HLAlookahead", Federate.Lookahead.handle );
		federate.addAttribute( lookahead );
		
		ACMetadata lbts = newAttribute( "HLALBTS", Federate.LBTS.handle );
		federate.addAttribute( lbts );

		ACMetadata galt = newAttribute( "HLAGALT", Federate.GALT.handle );
		federate.addAttribute( galt );
		
		ACMetadata lits = newAttribute( "HLALITS", Federate.LITS.handle );
		federate.addAttribute( lits );
		
		ACMetadata roLength = newAttribute( "HLAROlength", Federate.ROlength.handle );
		federate.addAttribute( roLength );
		
		ACMetadata tsoLength = newAttribute( "HLATSOlength", Federate.TSOlength.handle );
		federate.addAttribute( tsoLength );
		
		ACMetadata reflections = newAttribute( "HLAreflectionsReceived",
		                                       Federate.ReflectionsReceived.handle );
		federate.addAttribute( reflections );
		
		ACMetadata updates = newAttribute( "HLAupdatesSent", Federate.UpdatesSent.handle );
		federate.addAttribute( updates );
		
		ACMetadata intReceived = newAttribute( "HLAinteractionsReceived",
		                                       Federate.InteractionsReceived.handle );
		federate.addAttribute( intReceived );
		
		ACMetadata intSent = newAttribute( "HLAinteractionsSent",
		                                   Federate.InteractionsSent.handle );
		federate.addAttribute( intSent );
		
		ACMetadata objDelete = newAttribute( "HLAobjectInstancesThatCanBeDeleted",
		                                     Federate.ObjectInstancesThatCanBeDeleted.handle );
		federate.addAttribute( objDelete );
		
		ACMetadata objUpdate = newAttribute( "HLAobjectInstancesUpdated",
		                                     Federate.ObjectInstancesUpdated.handle );
		federate.addAttribute( objUpdate );
		
		ACMetadata objReflect = newAttribute( "HLAobjectInstancesReflected",
		                                      Federate.ObjectInstancesReflected.handle );
		federate.addAttribute( objReflect );
		
		ACMetadata objDeleted = newAttribute( "HLAobjectInstancesDeleted",
		                                     Federate.ObjectInstancesDeleted.handle );
		federate.addAttribute( objDeleted );
		
		ACMetadata objRemoved = newAttribute( "HLAobjectInstancesRemoved",
		                                      Federate.ObjectInstancesRemoved.handle );
		federate.addAttribute( objRemoved );
		
		ACMetadata objRegistered = newAttribute( "HLAobjectInstancesRegistered",
		                                         Federate.ObjectInstancesRegistered.handle );
		federate.addAttribute( objRegistered );
		
		ACMetadata objDiscovered = newAttribute( "HLAobjectInstancesDiscovered",
		                                         Federate.ObjectInstancesDiscovered.handle );
		federate.addAttribute( objDiscovered );
		
		ACMetadata timeGrant = newAttribute( "HLAtimeGrantedTime",
		                                     Federate.TimeGrantedTime.handle );
		federate.addAttribute( timeGrant );
		
		ACMetadata timeAdv = newAttribute( "HLAtimeAdvancingTime",
		                                   Federate.TimeAdvancingTime.handle );
		federate.addAttribute( timeAdv );
		
		///////////////////////////////////////////////////////////////
		///////////////// create the federation class /////////////////
		///////////////////////////////////////////////////////////////
		OCMetadata federation = new OCMetadata( "HLAfederation", FederationClass );
		federation.setModel( theModel );
		federation.setParent( manager );
		
		// populate it //
		ACMetadata fedName = newAttribute( "HLAfederationName", Federation.FederationName.handle );
		federation.addAttribute( fedName );
		
		ACMetadata fedList = newAttribute( "HLAfederatesInFederation",
		                                   Federation.FederatesInFederation.handle );
		federation.addAttribute( fedList );
		
		ACMetadata rtiVersion2 = newAttribute( "HLARTIversion", Federation.RtiVersion.handle );
		federation.addAttribute( rtiVersion2 );
		
		ACMetadata fddID2 = newAttribute( "HLAFDDID", Federation.FedID.handle );
		federation.addAttribute( fddID2 );
		
		ACMetadata lastSave = newAttribute( "HLAlastSaveName", Federation.LastSaveName.handle );
		federation.addAttribute( lastSave );
		
		ACMetadata lastTime = newAttribute( "HLAlastSaveTime", Federation.LastSaveTime.handle );
		federation.addAttribute( lastTime );
		
		ACMetadata nextSave = newAttribute( "HLAnextSaveName", Federation.NextSaveName.handle );
		federation.addAttribute( nextSave );
		
		ACMetadata nextTime = newAttribute( "HLAnextSaveTime", Federation.NextSaveTime.handle );
		federation.addAttribute( nextTime );
		
		ACMetadata autoProv = newAttribute( "HLAautoProvide", Federation.AutoProvide.handle );
		federation.addAttribute( autoProv );
		
		ACMetadata crds = newAttribute( "HLAconveyRegionDesignatorSets",
		                                Federation.ConveyRegionDesignatorSets.handle );
		federation.addAttribute( crds );
		
		//////////////////////////////////////
		// patch the manager into the model //
		//////////////////////////////////////
		theModel.addObjectClass( manager );
		theModel.addObjectClass( federate );
		theModel.addObjectClass( federation );
	}
	
	private static ACMetadata newAttribute( String name, int handle )
	{
		ACMetadata attribute = new ACMetadata( name, handle );
		attribute.setTransport( Transport.RELIABLE );
		attribute.setOrder( Order.RECEIVE );
		return attribute;
	}
}
