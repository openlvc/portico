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

import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.model.datatype.IDatatype;

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
		FederateName(5),                     // 1516e
		FederateType(6),
		FederateHost(7),
		FomModuleDesignatorList(8),          // 1516e
		RtiVersion(9),
		FedID(10),
		TimeConstrained(11),
		TimeRegulating(12),
		AsynchronousDelivery(13),
		FederateState(14),
		TimeManagerState(15),
		LogicalTime(16),
		Lookahead(17),
		LBTS(18), // synonym for LITS
		GALT(19),
		LITS(20), // NextMinEventTime in 1.3,
		ROlength(21),
		TSOlength(22),
		ReflectionsReceived(23),
		UpdatesSent(24),
		InteractionsReceived(25),
		InteractionsSent(26),
		ObjectInstancesThatCanBeDeleted(27), // ObjectsOwned in 1.3
		ObjectInstancesUpdated(28),          // ObjectsUpdated in 1.3
		ObjectInstancesReflected(29),        // ObjectsReflected in 1.3
		ObjectInstancesDeleted(30),
		ObjectInstancesRemoved(31),
		ObjectInstancesRegistered(32),
		ObjectInstancesDiscovered(33),
		TimeGrantedTime(34),
		TimeAdvancingTime(35),
		ConveyRegionDesignatorSets(36),     // Belongs to Federate in 1516e
		ConveyProducingFederate(37); // not in 1.3
		
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
		FederationName(38),
		FederatesInFederation(39),
		RtiVersion(40),
		MimDesignator(41),              // 1516e
		FomModuleDesignatorList(42),    // 1516e
		CurrentFdd(43),                 // 1516e
		FedID(44),
		TimeImplementationName(45),     // 1516e
		LastSaveName(46),
		LastSaveTime(47),
		NextSaveName(48),
		NextSaveTime(49),
		AutoProvide(50),                // not in 1.3
		ConveyRegionDesignatorSets(51); // Belongs to Federation in 1516
		
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
	private HashMap<String,Mom.Federate> federate1516eAttributes;
	private HashMap<String,Mom.Federation> federation1516eAttributes;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	private Mom()
	{
		this.federate13Attributes     = new HashMap<String,Mom.Federate>();
		this.federation13Attributes   = new HashMap<String,Mom.Federation>();
		this.federate1516Attributes   = new HashMap<String,Mom.Federate>();
		this.federation1516Attributes = new HashMap<String,Mom.Federation>();
		this.federate1516eAttributes  = new HashMap<String,Mom.Federate>();
		this.federation1516eAttributes= new HashMap<String,Mom.Federation>();
		
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
		
		
		// HLA 1516-Evolved //
		federate1516eAttributes.put( "HLAfederateHandle", Federate.FederateHandle );
		federate1516eAttributes.put( "HLAfederateName", Federate.FederateName );
		federate1516eAttributes.put( "HLAfederateType", Federate.FederateType );
		federate1516eAttributes.put( "HLAfederateHost", Federate.FederateHost );
		federate1516eAttributes.put( "HLARTIversion", Federate.RtiVersion );
		federate1516eAttributes.put( "HLAFOMmoduleDesignatorList", Federate.FomModuleDesignatorList );
		federate1516eAttributes.put( "HLAtimeConstrained", Federate.TimeConstrained );
		federate1516eAttributes.put( "HLAtimeRegulating", Federate.TimeRegulating );
		federate1516eAttributes.put( "HLAasynchronousDelivery", Federate.AsynchronousDelivery );
		federate1516eAttributes.put( "HLAfederateState", Federate.FederateState );
		federate1516eAttributes.put( "HLAtimeManagerState", Federate.TimeManagerState );
		federate1516eAttributes.put( "HLAlogicalTime", Federate.LogicalTime );
		federate1516eAttributes.put( "HLAlookahead", Federate.Lookahead );
		federate1516eAttributes.put( "HLAGALT", Federate.GALT );
		federate1516eAttributes.put( "HLALITS", Federate.LITS );
		federate1516eAttributes.put( "HLAROlength", Federate.ROlength );
		federate1516eAttributes.put( "HLATSOlength", Federate.TSOlength );
		federate1516eAttributes.put( "HLAreflectionsReceived", Federate.ReflectionsReceived );
		federate1516eAttributes.put( "HLAupdatesSent", Federate.UpdatesSent );
		federate1516eAttributes.put( "HLAinteractionsReceived", Federate.InteractionsReceived );
		federate1516eAttributes.put( "HLAinteractionsSent", Federate.InteractionsSent );
		federate1516eAttributes.put( "HLAobjectInstancesThatCanBeDeleted", Federate.ObjectInstancesThatCanBeDeleted );
		federate1516eAttributes.put( "HLAobjectInstancesUpdated", Federate.ObjectInstancesUpdated );
		federate1516eAttributes.put( "HLAobjectInstancesReflected", Federate.ObjectInstancesReflected );
		federate1516eAttributes.put( "HLAobjectInstancesDeleted", Federate.ObjectInstancesDeleted );
		federate1516eAttributes.put( "HLAobjectInstancesRemoved", Federate.ObjectInstancesRemoved );
		federate1516eAttributes.put( "HLAobjectInstancesRegistered", Federate.ObjectInstancesRegistered );
		federate1516eAttributes.put( "HLAobjectInstancesDiscovered", Federate.ObjectInstancesDiscovered );
		federate1516eAttributes.put( "HLAtimeGrantedTime", Federate.TimeGrantedTime );
		federate1516eAttributes.put( "HLAtimeAdvancingTime", Federate.TimeAdvancingTime );
		federate1516eAttributes.put( "HLAconveyRegionDesignatorSets", Federate.ConveyRegionDesignatorSets );
		federate1516eAttributes.put( "HLAconveyProducingFederate", Federate.ConveyProducingFederate );

		federation1516eAttributes.put( "HLAfederationName", Federation.FederationName );
		federation1516eAttributes.put( "HLAfederatesInFederation", Federation.FederatesInFederation );
		federation1516eAttributes.put( "HLARTIversion", Federation.RtiVersion );
		federation1516eAttributes.put( "HLAMIMdesignator", Federation.MimDesignator );
		federation1516eAttributes.put( "HLAFOMmoduleDesignatorList", Federation.FomModuleDesignatorList );
		federation1516eAttributes.put( "HLAcurrentFDD", Federation.CurrentFdd );
		federation1516eAttributes.put( "HLAtimeImplementationName", Federation.TimeImplementationName );
		federation1516eAttributes.put( "HLAlastSaveName", Federation.LastSaveName );
		federation1516eAttributes.put( "HLAlastSaveTime", Federation.LastSaveTime );
		federation1516eAttributes.put( "HLAnextSaveName", Federation.NextSaveName );
		federation1516eAttributes.put( "HLAnextSaveTime", Federation.NextSaveTime );
		federation1516eAttributes.put( "HLAautoProvide", Federation.AutoProvide );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Fetch the handle for the MOM class of the given name. This will work when given either
	 * HLA 1.3 or HLA 1516 style MOM names. If the class name is unknown,
	 * {@link ObjectModel#INVALID_HANDLE} will be returned.
	 */
	public static int getMomClassHandle( HLAVersion version, String className )
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
	public static int getMomAttributeHandle( HLAVersion version, int classHandle, String name )
	{
		try
		{
    		if( classHandle == FederateClass )
    		{
    			switch( version )
    			{
    				case HLA13:
    					return INSTANCE.federate13Attributes.get(name).handle;
    				case IEEE1516e:
    					return INSTANCE.federate1516eAttributes.get(name).handle;
    				case IEEE1516:
    					return INSTANCE.federate1516Attributes.get(name).handle;
    				default:
    					return ObjectModel.INVALID_HANDLE;
    			}
    		}
    		else if( classHandle == FederationClass )
    		{
    			switch( version )
    			{
    				case HLA13:
    					return INSTANCE.federation13Attributes.get(name).handle;
    				case IEEE1516e:
    					return INSTANCE.federation1516eAttributes.get(name).handle;
    				case IEEE1516:
    					return INSTANCE.federation1516Attributes.get(name).handle;
    				default:
    					return ObjectModel.INVALID_HANDLE;
    			}
    		}
		}
		catch( NullPointerException npe )
		{
			// can happen if the handle name isn't valid for the version type
			// just drop through - this is the same as not having a valid handle at all
		}

		return ObjectModel.INVALID_HANDLE;
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
		// datatypes that will be used throughout
		IDatatype hlaBoolean = theModel.getDatatype( "HLAboolean" );
		IDatatype hlaCount = theModel.getDatatype( "HLAcount" );
		IDatatype hlaFederateState = theModel.getDatatype( "HLAfederateState" );
		IDatatype hlaHandle = theModel.getDatatype( "HLAhandle" );
		IDatatype hlaHandleList = theModel.getDatatype( "HLAhandleList" );
		IDatatype hlaLogicalTime = theModel.getDatatype( "HLAlogicalTime" );
		IDatatype hlaMsec = theModel.getDatatype( "HLAmsec" );
		IDatatype hlaSwitch = theModel.getDatatype( "HLAswitch" );
		IDatatype hlaTimeInterval = theModel.getDatatype( "HLAtimeInterval" );
		IDatatype hlaTimeState = theModel.getDatatype( "HLAtimeState" );
		IDatatype hlaUnicodeString = theModel.getDatatype( "HLAunicodeString" );
		
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
		                                          hlaHandle,
		                                          Federate.FederateHandle.handle );
		federate.addAttribute( federateHandle );
		
		ACMetadata federateName = newAttribute( "HLAfederateName",
		                                        hlaUnicodeString,
		                                        Federate.FederateName.handle );
		federate.addAttribute( federateName );
		
		ACMetadata federateType = newAttribute( "HLAfederateType",
		                                        hlaUnicodeString,
		                                        Federate.FederateType.handle );
		federate.addAttribute( federateType );
		
		ACMetadata federateHost = newAttribute( "HLAfederateHost",
		                                        hlaUnicodeString,
		                                        Federate.FederateHost.handle );
		federate.addAttribute( federateHost );
		
		ACMetadata rtiVersion = newAttribute( "HLARTIversion",
		                                      hlaUnicodeString,
		                                      Federate.RtiVersion.handle );
		federate.addAttribute( rtiVersion );
		
		ACMetadata fddID = newAttribute( "HLAFDDID",
		                                 hlaUnicodeString, 
		                                 Federate.FedID.handle );
		federate.addAttribute( fddID );
		
		ACMetadata timeConstrained = newAttribute( "HLAtimeConstrained",
		                                           hlaBoolean,
		                                           Federate.TimeConstrained.handle );
		federate.addAttribute( timeConstrained );
		
		ACMetadata timeRegulating = newAttribute( "HLAtimeRegulating",
		                                          hlaBoolean,
		                                          Federate.TimeRegulating.handle );
		federate.addAttribute( timeRegulating );
		
		ACMetadata async = newAttribute( "HLAasynchronousDelivery",
		                                 hlaBoolean,
		                                 Federate.AsynchronousDelivery.handle );
		federate.addAttribute( async );
		
		ACMetadata state = newAttribute( "HLAfederateState",
		                                 hlaFederateState,
		                                 Federate.FederateState.handle );
		federate.addAttribute( state );
		
		ACMetadata timeState = newAttribute( "HLAtimeManagerState",
		                                     hlaTimeState,
		                                     Federate.TimeManagerState.handle );
		federate.addAttribute( timeState );
		
		ACMetadata logicalTime = newAttribute( "HLAlogicalTime",
		                                       hlaLogicalTime,
		                                       Federate.LogicalTime.handle );
		federate.addAttribute( logicalTime );
		
		ACMetadata lookahead = newAttribute( "HLAlookahead",
		                                     hlaTimeInterval,
		                                     Federate.Lookahead.handle );
		federate.addAttribute( lookahead );
		
		// TODO: Check the type of this - there is no reference of it in the standard MIM document
		ACMetadata lbts = newAttribute( "HLALBTS",
		                                hlaLogicalTime,
		                                Federate.LBTS.handle );
		federate.addAttribute( lbts );

		ACMetadata galt = newAttribute( "HLAGALT",
		                                hlaLogicalTime,
		                                Federate.GALT.handle );
		federate.addAttribute( galt );
		
		ACMetadata lits = newAttribute( "HLALITS",
		                                hlaLogicalTime,
		                                Federate.LITS.handle );
		federate.addAttribute( lits );
		
		ACMetadata roLength = newAttribute( "HLAROlength",
		                                    hlaCount,
		                                    Federate.ROlength.handle );
		federate.addAttribute( roLength );
		
		ACMetadata tsoLength = newAttribute( "HLATSOlength",
		                                     hlaCount,
		                                     Federate.TSOlength.handle );
		federate.addAttribute( tsoLength );
		
		ACMetadata reflections = newAttribute( "HLAreflectionsReceived",
		                                       hlaCount,
		                                       Federate.ReflectionsReceived.handle );
		federate.addAttribute( reflections );
		
		ACMetadata updates = newAttribute( "HLAupdatesSent",
		                                   hlaCount,
		                                   Federate.UpdatesSent.handle );
		federate.addAttribute( updates );
		
		ACMetadata intReceived = newAttribute( "HLAinteractionsReceived",
		                                       hlaCount,
		                                       Federate.InteractionsReceived.handle );
		federate.addAttribute( intReceived );
		
		ACMetadata intSent = newAttribute( "HLAinteractionsSent",
		                                   hlaCount,
		                                   Federate.InteractionsSent.handle );
		federate.addAttribute( intSent );
		
		ACMetadata objDelete = newAttribute( "HLAobjectInstancesThatCanBeDeleted",
		                                     hlaCount,
		                                     Federate.ObjectInstancesThatCanBeDeleted.handle );
		federate.addAttribute( objDelete );
		
		ACMetadata objUpdate = newAttribute( "HLAobjectInstancesUpdated",
		                                     hlaCount,
		                                     Federate.ObjectInstancesUpdated.handle );
		federate.addAttribute( objUpdate );
		
		ACMetadata objReflect = newAttribute( "HLAobjectInstancesReflected",
		                                      hlaCount,
		                                      Federate.ObjectInstancesReflected.handle );
		federate.addAttribute( objReflect );
		
		ACMetadata objDeleted = newAttribute( "HLAobjectInstancesDeleted",
		                                      hlaCount,
		                                      Federate.ObjectInstancesDeleted.handle );
		federate.addAttribute( objDeleted );
		
		ACMetadata objRemoved = newAttribute( "HLAobjectInstancesRemoved",
		                                      hlaCount,
		                                      Federate.ObjectInstancesRemoved.handle );
		federate.addAttribute( objRemoved );
		
		ACMetadata objRegistered = newAttribute( "HLAobjectInstancesRegistered",
		                                         hlaCount,
		                                         Federate.ObjectInstancesRegistered.handle );
		federate.addAttribute( objRegistered );
		
		ACMetadata objDiscovered = newAttribute( "HLAobjectInstancesDiscovered",
		                                         hlaCount,
		                                         Federate.ObjectInstancesDiscovered.handle );
		federate.addAttribute( objDiscovered );
		
		ACMetadata timeGrant = newAttribute( "HLAtimeGrantedTime",
		                                     hlaMsec,
		                                     Federate.TimeGrantedTime.handle );
		federate.addAttribute( timeGrant );
		
		ACMetadata timeAdv = newAttribute( "HLAtimeAdvancingTime",
		                                   hlaMsec,
		                                   Federate.TimeAdvancingTime.handle );
		federate.addAttribute( timeAdv );
		
		///////////////////////////////////////////////////////////////
		///////////////// create the federation class /////////////////
		///////////////////////////////////////////////////////////////
		OCMetadata federation = new OCMetadata( "HLAfederation", FederationClass );
		federation.setModel( theModel );
		federation.setParent( manager );
		
		// populate it //
		ACMetadata fedName = newAttribute( "HLAfederationName",
		                                   hlaUnicodeString,
		                                   Federation.FederationName.handle );
		federation.addAttribute( fedName );
		
		ACMetadata fedList = newAttribute( "HLAfederatesInFederation",
		                                   hlaHandleList,
		                                   Federation.FederatesInFederation.handle );
		federation.addAttribute( fedList );
		
		ACMetadata rtiVersion2 = newAttribute( "HLARTIversion",
		                                       hlaUnicodeString,
		                                       Federation.RtiVersion.handle );
		federation.addAttribute( rtiVersion2 );
		
		ACMetadata fddID2 = newAttribute( "HLAFDDID",
		                                  hlaUnicodeString,
		                                  Federation.FedID.handle );
		federation.addAttribute( fddID2 );
		
		ACMetadata lastSave = newAttribute( "HLAlastSaveName",
		                                    hlaUnicodeString,
		                                    Federation.LastSaveName.handle );
		federation.addAttribute( lastSave );
		
		ACMetadata lastTime = newAttribute( "HLAlastSaveTime",
		                                    hlaLogicalTime,
		                                    Federation.LastSaveTime.handle );
		federation.addAttribute( lastTime );
		
		ACMetadata nextSave = newAttribute( "HLAnextSaveName",
		                                    hlaUnicodeString,
		                                    Federation.NextSaveName.handle );
		federation.addAttribute( nextSave );
		
		ACMetadata nextTime = newAttribute( "HLAnextSaveTime",
		                                    hlaLogicalTime,
		                                    Federation.NextSaveTime.handle );
		federation.addAttribute( nextTime );
		
		ACMetadata autoProv = newAttribute( "HLAautoProvide",
		                                    hlaSwitch,
		                                    Federation.AutoProvide.handle );
		federation.addAttribute( autoProv );
		
		// 1516 Federation level ConveyRegionDesignatorSets (Belongs to Federate in 1516e)
		ACMetadata crds = newAttribute( "HLAconveyRegionDesignatorSets",
		                                hlaSwitch,
		                                Federation.ConveyRegionDesignatorSets.handle );
		federation.addAttribute( crds );
		
		//////////////////////////////////////
		// patch the manager into the model //
		//////////////////////////////////////
		theModel.addObjectClass( manager );
		theModel.addObjectClass( federate );
		theModel.addObjectClass( federation );
	}

	/**
	 * Patch the supported MOM hierarchy into the given object model. Presumes that any
	 * modification of it has already been stripped out elsewhere so that we can patch
	 * it in clean.
	 */
	public static void insertMomHierarchy1516e( ObjectModel theModel )
	{
		// datatypes that will be used throughout
		IDatatype hlaBoolean = theModel.getDatatype( "HLAboolean" );
		IDatatype hlaCount = theModel.getDatatype( "HLAcount" );
		IDatatype hlaFederateState = theModel.getDatatype( "HLAfederateState" );
		IDatatype hlaHandle = theModel.getDatatype( "HLAhandle" );
		IDatatype hlaHandleList = theModel.getDatatype( "HLAhandleList" );
		IDatatype hlaLogicalTime = theModel.getDatatype( "HLAlogicalTime" );
		IDatatype hlaModuleDesignatorList = theModel.getDatatype( "HLAmoduleDesignatorList" );
		IDatatype hlaMsec = theModel.getDatatype( "HLAmsec" );
		IDatatype hlaSwitch = theModel.getDatatype( "HLAswitch" );
		IDatatype hlaTimeInterval = theModel.getDatatype( "HLAtimeInterval" );
		IDatatype hlaTimeState = theModel.getDatatype( "HLAtimeState" );
		IDatatype hlaUnicodeString = theModel.getDatatype( "HLAunicodeString" );
		
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
		federate.addAttribute( newAttribute("HLAfederateHandle",hlaHandle,Federate.FederateHandle.handle) );
		federate.addAttribute( newAttribute("HLAfederateName",hlaUnicodeString,Federate.FederateName.handle) );
		federate.addAttribute( newAttribute("HLAfederateType",hlaUnicodeString,Federate.FederateType.handle) );
		federate.addAttribute( newAttribute("HLAfederateHost",hlaUnicodeString,Federate.FederateHost.handle) );
		federate.addAttribute( newAttribute("HLARTIversion",hlaUnicodeString,Federate.RtiVersion.handle) );
		federate.addAttribute( newAttribute("HLAFOMmoduleDesignatorList",hlaModuleDesignatorList,Federate.FomModuleDesignatorList.handle) );
		federate.addAttribute( newAttribute("HLAtimeConstrained",hlaBoolean,Federate.TimeConstrained.handle) );
		federate.addAttribute( newAttribute("HLAtimeRegulating",hlaBoolean,Federate.TimeRegulating.handle) );
		federate.addAttribute( newAttribute("HLAasynchronousDelivery",hlaBoolean,Federate.AsynchronousDelivery.handle) );
		federate.addAttribute( newAttribute("HLAfederateState",hlaFederateState,Federate.FederateState.handle) );
		federate.addAttribute( newAttribute("HLAtimeManagerState",hlaTimeState,Federate.TimeManagerState.handle) );
		federate.addAttribute( newAttribute("HLAlogicalTime",hlaLogicalTime,Federate.LogicalTime.handle) );
		federate.addAttribute( newAttribute("HLAlookahead",hlaTimeInterval,Federate.Lookahead.handle) );
		federate.addAttribute( newAttribute("HLAGALT",hlaLogicalTime,Federate.GALT.handle) );
		federate.addAttribute( newAttribute("HLALITS",hlaLogicalTime,Federate.LITS.handle) );
		federate.addAttribute( newAttribute("HLAROlength",hlaCount,Federate.ROlength.handle) );
		federate.addAttribute( newAttribute("HLATSOlength",hlaCount,Federate.TSOlength.handle) );
		federate.addAttribute( newAttribute("HLAreflectionsReceived",hlaCount,Federate.ReflectionsReceived.handle) );
		federate.addAttribute( newAttribute("HLAupdatesSent",hlaCount,Federate.UpdatesSent.handle) );
		federate.addAttribute( newAttribute("HLAinteractionsReceived",hlaCount,Federate.InteractionsReceived.handle) );
		federate.addAttribute( newAttribute("HLAinteractionsSent",hlaCount,Federate.InteractionsSent.handle) );
		federate.addAttribute( newAttribute("HLAobjectInstancesThatCanBeDeleted",hlaCount,Federate.ObjectInstancesThatCanBeDeleted.handle) );
		federate.addAttribute( newAttribute("HLAobjectInstancesUpdated",hlaCount,Federate.ObjectInstancesUpdated.handle) );
		federate.addAttribute( newAttribute("HLAobjectInstancesReflected",hlaCount,Federate.ObjectInstancesReflected.handle) );
		federate.addAttribute( newAttribute("HLAobjectInstancesDeleted",hlaCount,Federate.ObjectInstancesDeleted.handle) );
		federate.addAttribute( newAttribute("HLAobjectInstancesRemoved",hlaCount,Federate.ObjectInstancesRemoved.handle) );
		federate.addAttribute( newAttribute("HLAobjectInstancesRegistered",hlaCount,Federate.ObjectInstancesRegistered.handle) );
		federate.addAttribute( newAttribute("HLAobjectInstancesDiscovered",hlaCount,Federate.ObjectInstancesDiscovered.handle) );
		federate.addAttribute( newAttribute("HLAtimeGrantedTime",hlaMsec,Federate.TimeGrantedTime.handle) );
		federate.addAttribute( newAttribute("HLAtimeAdvancingTime",hlaMsec,Federate.TimeAdvancingTime.handle) );
		federate.addAttribute( newAttribute("HLAconveyRegionDesignatorSets",hlaSwitch,Federate.ConveyRegionDesignatorSets.handle) );
		federate.addAttribute( newAttribute("HLAconveyProducingFederate",hlaSwitch,Federate.ConveyProducingFederate.handle) );

		///////////////////////////////////////////////////////////////
		///////////////// create the federation class /////////////////
		///////////////////////////////////////////////////////////////
		OCMetadata federation = new OCMetadata( "HLAfederation", FederationClass );
		federation.setModel( theModel );
		federation.setParent( manager );
		
		// populate it //
		federation.addAttribute( newAttribute("HLAfederationName",hlaUnicodeString,Federation.FederationName.handle) );
		federation.addAttribute( newAttribute("HLAfederatesInFederation",hlaHandleList,Federation.FederatesInFederation.handle) );
		federation.addAttribute( newAttribute("HLARTIversion",hlaUnicodeString,Federation.RtiVersion.handle) );
		federation.addAttribute( newAttribute("HLAMIMdesignator",hlaUnicodeString,Federation.MimDesignator.handle) );
		federation.addAttribute( newAttribute("HLAFOMmoduleDesignatorList",hlaModuleDesignatorList,Federation.FomModuleDesignatorList.handle) );
		federation.addAttribute( newAttribute("HLAcurrentFDD",hlaUnicodeString,Federation.CurrentFdd.handle) );
		federation.addAttribute( newAttribute("HLAtimeImplementationName",hlaUnicodeString,Federation.TimeImplementationName.handle) );
		federation.addAttribute( newAttribute("HLAlastSaveName",hlaUnicodeString,Federation.LastSaveName.handle) );
		federation.addAttribute( newAttribute("HLAlastSaveTime",hlaUnicodeString,Federation.LastSaveTime.handle) );
		federation.addAttribute( newAttribute("HLAnextSaveName",hlaUnicodeString,Federation.NextSaveName.handle) );
		federation.addAttribute( newAttribute("HLAnextSaveTime",hlaUnicodeString,Federation.NextSaveTime.handle) );
		federation.addAttribute( newAttribute("HLAautoProvide",hlaSwitch,Federation.AutoProvide.handle) );
		
		//////////////////////////////////////
		// patch the manager into the model //
		//////////////////////////////////////
		theModel.addObjectClass( manager );
		theModel.addObjectClass( federate );
		theModel.addObjectClass( federation );
	}
	
	private static ACMetadata newAttribute( String name, IDatatype datatype, int handle )
	{
		// As method is called under controlled circumstances the datatype param should only
		// ever refer to a standard datatype that should already exist in the model
		if( datatype == null )
			throw new IllegalArgumentException( "invalid datatype: " + datatype.getName() );
		
		ACMetadata attribute = new ACMetadata( name, datatype, handle );
		attribute.setTransport( Transport.RELIABLE );
		attribute.setOrder( Order.RECEIVE );
		return attribute;
	}
}
