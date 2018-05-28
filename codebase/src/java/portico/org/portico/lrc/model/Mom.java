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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.portico.impl.HLAVersion;
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
	private static final Mom INSTANCE = new Mom();
	
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	/**
	 * Types of things that can be stored in the {@link MomHandleTree}
	 */
	private enum MomType
	{
		Object,
		Attribute,
		Interaction,
		Parameter
	}
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private MomHandleTree momTree;
	
	// Lookup tables for mom types
	private Map<Integer,MomTreeNode> objectLookup;
	private Map<Integer,MomTreeNode> attributeLookup;
	private Map<Integer,MomTreeNode> interactionLookup;
	private Map<Integer,MomTreeNode> parameterLookup;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	private Mom()
	{
		// Build interaction hierarchy
		initialize();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Builds the internal representation of metadata for standard MOM types.
	 * <p/>
	 * The tree contains nodes representing the hierarchy of objects, attributes, interactions and 
	 * parameters and can be used to populate an {@link ObjectModel} with {@link OCMetadata}
	 * and {@link ICMetadata} for all MOM types.
	 * <p/>
	 * This method also generates the lookup tables that resolve {@link MomTreeNode} metadata by handle
	 */
	private void initialize()
	{
		HLAVersion[] v13 = { HLAVersion.JAVA1, HLAVersion.HLA13 };
		HLAVersion[] v1516 = { HLAVersion.IEEE1516, HLAVersion.IEEE1516e };
		
		// Declaratively build the tree. The creator methods convert the provided names into a 
		// {@link VersionedName} so that nodes can be resolved based on the naming conventions of all
		// HLA specification versions
		//
		// NOTE: Handles generated here will be consistent across all federations running in the RTI
		MomTreeNodeBuilder b = new MomTreeNodeBuilder();
		
		//
		// Objects
		//
		b.object( "HLAobjectRoot" )
			.object("HLAmanager")
				.object("HLAfederate")
					.attribute( "HLAfederateHandle", "HLAhandle" )
					.attribute( "HLAfederateName", "HLAunicodeString", v1516 )
					.attribute( "HLAfederateType", "HLAunicodeString" )
					.attribute( "HLAfederateHost", "HLAunicodeString" )
					.attribute( "HLARTIversion", "HLAunicodeString" )
					.attribute( "FEDid", "HLAunicodeString", v13 )
					.attribute( "HLAFOMmoduleDesignatorList", "HLAmoduleDesignatorList", HLAVersion.IEEE1516e )
					.attribute( "HLAtimeConstrained", "HLAboolean" )
					.attribute( "HLAtimeRegulating", "HLAboolean" )
					.attribute( "HLAasynchronousDelivery", "HLAboolean" )
					.attribute( "HLAfederateState", "HLAfederateState" )
					.attribute( "HLAtimeManagerState", "HLAtimeState" )
					.attribute( VersionedName.from( "FederateTime", "HLAlogicalTime" ), "HLAlogicalTime" )
					.attribute( "HLAlookahead", "HLAtimeInterval" )
					.attribute( "LBTS", "HLAlogicalTime", v13 )
					.attribute( "HLAGALT", "HLAlogicalTime", v1516 )
					.attribute( VersionedName.from( "MinNextEventTime", "HLALITS" ), "HLAlogicalTime" )
					.attribute( "HLAROlength", "HLAcount" )
					.attribute( "HLATSOlength", "HLAcount" )
					.attribute( "HLAreflectionsReceived", "HLAcount" )
					.attribute( "HLAupdatesSent", "HLAcount" )
					.attribute( "HLAinteractionsReceived", "HLAcount" )
					.attribute( "HLAinteractionsSent", "HLAcount" )
					.attribute( VersionedName.from( "ObjectsOwned", "HLAobjectInstancesThatCanBeDeleted" ), "HLAcount" )
					.attribute( VersionedName.from( "ObjectsUpdated", "HLAobjectInstancesUpdated" ), "HLAcount" )
					.attribute( VersionedName.from( "ObjectsReflected", "HLAobjectInstancesReflected" ), "HLAcount" )
					.attribute( "HLAobjectInstancesDeleted", "HLAcount", v1516 )
					.attribute( "HLAobjectInstancesRemoved", "HLAcount", v1516 )
					.attribute( "HLAobjectInstancesRegistered", "HLAcount", v1516 )
					.attribute( "HLAobjectInstancesDiscovered", "HLAcount", v1516 )
					.attribute( "HLAtimeGrantedTime", "HLAmsec", v1516 )
					.attribute( "HLAtimeAdvancingTime", "HLAmsec", v1516 )
					.attribute( "HLAconveyRegionDesignatorSets", "HLAswitch", HLAVersion.IEEE1516e )
					.attribute( "HLAconveyProducingFederate", "HLAswitch", v1516 )
				.end()
				.object("HLAfederation")
					.attribute( "HLAfederationName", "HLAunicodeString" )
					.attribute( "HLAfederatesInFederation", "HLAhandleList" )
					.attribute( "HLARTIversion", "HLAunicodeString" )
					.attribute( "HLAFEDid", "HLAunicodeString", HLAVersion.JAVA1, HLAVersion.HLA13, HLAVersion.IEEE1516 )
					.attribute( "HLAMIMdesignator", "HLAunicodeString", HLAVersion.IEEE1516e )
					.attribute( "HLAFOMmoduleDesignatorList", "HLAmoduleDesignatorList", HLAVersion.IEEE1516e )
					.attribute( "HLAcurrentFDD", "HLAunicodeString", HLAVersion.IEEE1516e )
					.attribute( "HLAtimeImplementationName", "HLAunicodeString", HLAVersion.IEEE1516e )
					.attribute( "HLAlastSaveName", "HLAunicodeString" )
					.attribute( "HLAlastSaveTime", "HLAlogicalTime" )
					.attribute( "HLAnextSaveName", "HLAunicodeString" )
					.attribute( "HLAnextSaveTime", "HLAlogicalTime" )
					.attribute( "HLAconveyRegionDesignatorSets", "HLAswitch", HLAVersion.IEEE1516 )
					.attribute( "HLAautoProvide", "HLAswitch", v1516 )
				.end()
			.end();
		MomTreeNode objectRoot = b.getContext();
		b.end();
		
		//
		// Interactions
		//
		b.interaction( "HLAinteractionRoot" )
			.interaction( "HLAmanager" )
				.interaction( "HLAfederate")
					.parameter( "HLAfederate", "HLAhandle" )
					.interaction( "HLAadjust" )
						.interaction( "HLAsetTiming")
							.parameter( "HLAreportPeriod", "HLAseconds" )
						.end()
						.interaction( "HLAmodifyAttributeState" )
							.parameter( "HLAobjectInstance", "HLAhandle" )
							.parameter( "HLAattribute", "HLAhandle" )
							.parameter( "HLAattributeState", "HLAownership" )
						.end()
						.interaction( "HLAsetServiceReporting" )
							.parameter( "HLAreportingState", "HLAboolean" )
						.end()
						.interaction( "HLAsetExceptionReporting" )
							.parameter( VersionedName.from("LoggingState", "HLAreportingState"), "HLAboolean" )
						.end()
						.interaction( "HLAsetSwitches", v1516 )
							.parameter( "HLAconveyRegionDesignatorSets", "HLAswitch", HLAVersion.IEEE1516e )
							.parameter( "HLAconveyProducingFederate", "HLAswitch" )
						.end()
					.end()
					.interaction( "HLArequest" )
						.interaction( "HLArequestPublications" ).end()
						.interaction( "HLArequestSubscriptions" ).end()
						.interaction( VersionedName.from("RequestObjectInstancesOwned","HLArequestObjectInstancesThatCanBeDeleted") ).end()
						.interaction( VersionedName.from("RequestObjectsUpdated","HLArequestObjectInstancesUpdated") ).end()
						.interaction( VersionedName.from("RequestObjectsReflected","HLArequestObjectInstancesReflected") ).end()
						.interaction( "HLArequestUpdatesSent" ).end()
						.interaction( "HLArequestInteractionsSent" ).end()
						.interaction( "HLArequestReflectionsReceived" ).end()
						.interaction( "HLArequestInteractionsReceived" ).end()
						.interaction( VersionedName.from("RequestObjectInformation", "HLArequestObjectInstanceInformation") )
							.parameter( "HLAobjectInstance", "HLAhandle" )
						.end()
						.interaction( "HLArequestFOMmoduleData", HLAVersion.IEEE1516e )
							.parameter( "HLAFOMmoduleIndicator", "HLAindex" )
						.end()
					.end()
					.interaction( "HLAreport" )
						.interaction( VersionedName.from("ReportObjectPublication","HLAreportObjectClassPublication") )
							.parameter( "HLAnumberOfClasses", "HLAcount" )
							.parameter( "HLAobjectClass", "HLAhandle" )
							.parameter( "HLAattributeList", "HLAhandleList" )
						.end()
						.interaction( "HLAreportInteractionPublication" )
							.parameter( "HLAinteractionClassList", "HLAhandleList" )
						.end()
						.interaction( VersionedName.from("ReportObjectSubscription","HLAreportObjectClassSubscription") )
							.parameter( "HLAnumberOfClasses", "HLAcount" )
							.parameter( "HLAobjectClass", "HLAhandle" )
							.parameter( "HLAactive", "HLAboolean" )
							.parameter( "HLAmaxUpdateRate", "HLAupdateRateName", v1516 )
							.parameter( "HLAattributeList", "HLAhandleList" )
						.end()
						.interaction( "HLAreportInteractionSubscription" )
							.parameter( "HLAinteractionClassList", "HLAinteractionSubList" )
						.end()
						.interaction( VersionedName.from("ReportObjectsOwned","HLAreportObjectInstancesThatCanBeDeleted") )
							.parameter( VersionedName.from("ObjectCounts","HLAobjectInstanceCounts"), "HLAobjectClassBasedCounts" )
						.end()
						.interaction( VersionedName.from("ReportObjectsUpdated","HLAreportObjectInstancesUpdated") )
							.parameter( VersionedName.from("ObjectCounts","HLAobjectInstanceCounts"), "HLAobjectClassBasedCounts" )
						.end()
						.interaction( VersionedName.from("ReportObjectsReflected","HLAreportObjectInstancesReflected") )
							.parameter( VersionedName.from("ObjectCounts","HLAobjectInstanceCounts"), "HLAobjectClassBasedCounts" )
						.end()
						.interaction( "HLAreportUpdatesSent" )
							.parameter( VersionedName.from("TransportationType", "HLAtransportation"), "HLAtransportationName" )
							.parameter( "HLAupdateCounts", "HLAobjectClassBasedCounts" )
						.end()
						.interaction( "HLAreportReflectionsReceived" )
							.parameter( VersionedName.from("TransportationType", "HLAtransportation"), "HLAtransportationName" )
							.parameter( "HLAreflectCounts", "HLAobjectClassBasedCounts" )
						.end()
						.interaction( "HLAreportInteractionsSent" )
							.parameter( VersionedName.from("TransportationType", "HLAtransportation"), "HLAtransportationName" )
							.parameter( "HLAinteractionCounts", "HLAinteractionCounts" )
						.end()
						.interaction( "HLAreportInteractionsReceived" )
							.parameter( VersionedName.from("TransportationType", "HLAtransportation"), "HLAtransportationName" )
							.parameter( "HLAinteractionCounts", "HLAinteractionCounts" )
						.end()
						.interaction( VersionedName.from("ReportObjectInformation","HLAreportObjectInstanceInformation") )
							.parameter( "HLAobjectInstance", "HLAhandle" )
							.parameter( VersionedName.from("OwnedAttributeList","HLAownedInstanceAttributeList"), "HLAhandleList" )
							.parameter( "HLAregisteredClass", "HLAhandle" )
							.parameter( "HLAknownClass", "HLAhandle" )
						.end()
						.interaction( "Alert", v13 )
							.parameter( "AlertSeverity", "HLAunicodeString" )
							.parameter( "AlertDescription", "HLAunicodeString" )
							.parameter( "AlertID", "HLAunicodeString" )
						.end()
						.interaction( "HLAreportException", v1516 )
							.parameter( "HLAservice", "HLAunicodeString" )
							.parameter( "HLAexception", "HLAunicodeString" )
						.end()
						.interaction( "HLAreportServiceInvocation" )
							.parameter( "HLAservice", "HLAunicodeString" )
							.parameter( "Initiator", "HLAunicodeString", v13 )
							.parameter( "HLAsuccessIndicator", "HLAboolean" )
							.parameter( "HLAsuppliedArguments", "HLAargumentList", v1516 )
							.parameter( "SuppliedArgument1", "HLAunicodeString", v13 )
							.parameter( "SuppliedArgument2", "HLAunicodeString", v13 )
							.parameter( "SuppliedArgument3", "HLAunicodeString", v13 )
							.parameter( "SuppliedArgument4", "HLAunicodeString", v13 )
							.parameter( "SuppliedArgument5", "HLAunicodeString", v13 )
							.parameter( "HLAreturnedArguments", "HLAargumentList", v1516 )
							.parameter( "ReturnedArgument", "HLAunicodeString", v13 )
							.parameter( VersionedName.from("ExceptionDescription","HLAexception"), "HLAunicodeString" )
							.parameter( "ExceptionID", "HLAunicodeString", v13 )
							.parameter( "HLAserialNumber", "HLAcount", v1516 )
						.end()
						.interaction( "HLAreportMOMexception", v1516 )				// TODO 1516e only?
							.parameter( "HLAservice", "HLAunicodeString" )
							.parameter( "HLAexception", "HLAunicodeString" )
							.parameter( "HLAparameterError", "HLAboolean" )
						.end()
						.interaction( "HLAreportFederateLost", HLAVersion.IEEE1516e )
							.parameter( "HLAfederateName", "HLAunicodeString" )
							.parameter( "HLAtimeStamp", "HLAlogicalTime" )
							.parameter( "HLAfaultDescription", "HLAunicodeString" )
						.end()
						.interaction( "HLAreportFOMmoduleData", HLAVersion.IEEE1516e )
							.parameter( "HLAFOMmoduleIndicator", "HLAindex" )
							.parameter( "HLAFOMmoduleData", "HLAunicodeString" )
						.end()
					.end()
					.interaction( "HLAservice" )
						.interaction( "HLAresignFederationExecution" )
							.parameter( "HLAresignAction", "HLAresignAction" )
						.end()
						.interaction( "HLAsynchronizationPointAchieved" )
							.parameter( "HLAlabel", "HLAunicodeString" )
						.end()
						.interaction( "HLAfederateSaveBegun" ).end()
						.interaction( "HLAfederateSaveComplete" )
							.parameter( "HLAsuccessIndicator", "HLAboolean" )
						.end()
						.interaction( "HLAfederateRestoreComplete" )
							.parameter( "HLAsuccessIndicator", "HLAboolean" )
						.end()
						.interaction( VersionedName.from("PublishObjectClass","HLApublishObjectClassAttributes") )
							.parameter( "HLAobjectClass", "HLAhandle" )
							.parameter( "HLAattributeList", "HLAhandleList" )
						.end()
						.interaction( VersionedName.from("UnpublishObjectClass","HLAunpublishObjectClassAttributes") )
							.parameter( "HLAobjectClass", "HLAhandle" )
							.parameter( "HLAattributeList", "HLAhandleList", v1516 )
						.end()
						.interaction( "HLApublishInteractionClass" )
							.parameter( "HLAinteractionClass", "HLAhandle" )
						.end()
						.interaction( "HLAunpublishInteractionClass" )
							.parameter( "HLAinteractionClass", "HLAhandle" )
						.end()
						.interaction( "HLAsubscribeObjectClassAttributes" )
							.parameter( "HLAobjectClass", "HLAhandle" )
							.parameter( "HLAattributeList", "HLAhandleList" )
							.parameter( "HLAactive", "HLAboolean" )
						.end()
						.interaction( VersionedName.from("UnsubscribeObjectClass","HLAunsubscribeObjectClassAttributes") )
							.parameter( "HLAobjectClass", "HLAhandle" )
							.parameter( "HLAattributeList", "HLAhandleList", v1516 )
						.end()
						.interaction( "HLAsubscribeInteractionClass" )
							.parameter( "HLAinteractionClass", "HLAhandle" )
							.parameter( "HLAactive", "HLAboolean" )
						.end()
						.interaction( "HLAunsubscribeInteractionClass" )
							.parameter( "HLAinteractionClass", "HLAhandle" )
						.end()
						.interaction( "HLAdeleteObjectInstance" )
							.parameter( "HLAobjectInstance", "HLAhandle" )
							.parameter( "HLAtag", "HLAopaqueData" )
							.parameter( "HLAtimeStamp", "HLAlogicalTime" )
						.end()
						.interaction( "HLAlocalDeleteObjectInstance" )
							.parameter( "HLAobjectInstance", "HLAhandle" )
						.end()
						.interaction( VersionedName.from("ChangeAttributeTransportationType","HLArequestAttributeTransportationTypeChange") )
							.parameter( "HLAobjectInstance", "HLAhandle" )
							.parameter( "HLAattributeList", "HLAhandleList" )
							.parameter( VersionedName.from("TransportationType", "HLAtransportation"), "HLAtransportationName" )
						.end()
						.interaction( VersionedName.from("ChangeInteractionTransportationType","HLArequestInteractionTransportationTypeChange") )
							.parameter( "HLAinteractionClass", "HLAhandle" )
							.parameter( VersionedName.from("TransportationType", "HLAtransportation"), "HLAtransportationName" )
						.end()
						.interaction( "HLAunconditionalAttributeOwnershipDivestiture" )
							.parameter( "HLAobjectInstance", "HLAhandle" )
							.parameter( "HLAattributeList", "HLAhandleList" )
						.end()
						.interaction( "HLAenableTimeRegulation" )
							.parameter( "FederationTime", "HLAlogicalTime", v13 )
							.parameter( "HLAlookahead", "HLAtimeInterval" )
						.end()
						.interaction( "HLAdisableTimeRegulation" ).end()
						.interaction( "HLAenableTimeConstrained" ).end()
						.interaction( "HLAdisableTimeConstrained" ).end()
						.interaction( "HLAtimeAdvanceRequest" )
							.parameter( VersionedName.from("FederationTime","HLAtimeStamp"), "HLAlogicalTime" )
						.end()
						.interaction( "HLAtimeAdvanceRequestAvailable" )
							.parameter( VersionedName.from("FederationTime","HLAtimeStamp"), "HLAlogicalTime" )
						.end()
						.interaction( VersionedName.from("NextEventRequest","HLAnextMessageRequest") )
							.parameter( VersionedName.from("FederationTime","HLAtimeStamp"), "HLAlogicalTime" )
						.end()
						.interaction( VersionedName.from("NextEventRequestAvailable","HLAnextMessageRequestAvailable") )
							.parameter( VersionedName.from("FederationTime","HLAtimeStamp"), "HLAlogicalTime" )
						.end()
						.interaction( "HLAflushQueueRequest" )
							.parameter( VersionedName.from("FederationTime","HLAtimeStamp"), "HLAlogicalTime" )
						.end()
						.interaction( "HLAenableAsynchronousDelivery" ).end()
						.interaction( "HLAdisableAsynchronousDelivery" ).end()
						.interaction( "HLAmodifyLookahead" )
							.parameter( "HLAlookahead", "HLAtimeInterval" )
						.end()
						.interaction( "HLAchangeAttributeOrderType" )
							.parameter( "HLAobjectInstance", "HLAhandle" )
							.parameter( "HLAattributeList", "HLAhandleList" )
							.parameter( VersionedName.from("OrderingType","HLAsendOrder"), "HLAorderType" )
						.end()
						.interaction( "HLAchangeInteractionOrderType" )
							.parameter( "HLAinteractionClass", "HLAhandle" )
							.parameter( VersionedName.from("OrderingType","HLAsendOrder"), "HLAorderType" )
						.end()
					.end()
				.end()
				.interaction( "HLAfederation", v1516 )
					.interaction( "HLAadjust" )
						.interaction( "HLAsetSwitches" )
							.parameter( "HLAautoProvide", "HLAswitch" )
						.end()
					.end()
					.interaction( "HLArequest" )
						.interaction( "HLArequestSynchronizationPoints" ).end()
						.interaction( "HLArequestSynchronizationPointStatus" )
							.parameter( "HLAsyncPointName", "HLAunicodeString" )	// This param is listed in the spec but not in the MIM xml :(
						.end()
						.interaction( "HLArequestFOMmoduleData" )
							.parameter( "HLAFOMmoduleIndicator", "HLAindex" )
						.end()
						.interaction( "HLArequestMIMdata" ).end()
					.end()
					.interaction( "HLAreport" )
						.interaction( "HLAreportSynchronizationPoints" )
							.parameter( "HLAsyncPoints", "HLAsynchPointList" )
						.end()
						.interaction( "HLAreportSynchronizationPointStatus" )
							.parameter( "HLAsyncPointName", "HLAunicodeString" )
							.parameter( "HLAsyncPointFederates", "HLAsynchPointFederateList" )
						.end()
						.interaction( "HLAreportFOMmoduleData" )
							.parameter( "HLAFOMmoduleIndicator", "HLAindex" )
							.parameter( "HLAFOMmoduleData", "HLAunicodeString" )
						.end()
						.interaction( "HLAreportMIMdata" )
							.parameter( "HLAMIMdata", "HLAunicodeString" )
						.end()
					.end()
				.end()
			.end();
		
		MomTreeNode interactionRoot = b.getContext();
		b.end();
		
		this.momTree = new MomHandleTree( objectRoot, interactionRoot );
		this.objectLookup = b.getObjectLookup();
		this.attributeLookup = b.getAttributeLookup();
		this.interactionLookup = b.getInteractionLookup();
		this.parameterLookup = b.getParameterLookup();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Fetch the handle for the MOM class of the given name.
	 * <p/>
	 * This function expects a fully qualified name be provided in the <code>className</code> parameter,
	 * relative to <code>ObjectRoot</code> (e.g. "Manager.Federate")
	 * <p/>
	 * If the class name is unknown, {@link ObjectModel#INVALID_HANDLE} will be returned.
	 * 
	 * @param version the HLA version of the MOM naming scheme that the <code>className</code> parameter 
	 *                conforms to
	 * @param className the qualified name of the class to resolve the handle for, relative to ObjectRoot
	 * @return the handle of the desired object class, or {@link ObjectModel#INVALID_HANDLE} if no class
	 *         exists with the specified name
	 */
	public static int getMomObjectClassHandle( HLAVersion version, String className )
	{
		MomTreeNode node = INSTANCE.momTree.find( className, 
		                                          version,
		                                          INSTANCE.momTree.objectRoot );
		if( node != null )
		{
			return node.getHandle();
		}
		else
		{
			return ObjectModel.INVALID_HANDLE;
		}
	}

	/**
	 * Fetch the handle for the MOM attribute of the given name that belongs to the MOM class of the given
	 * handle. If the class handle is not recognized or the name isn't a valid attribute,
	 * {@link ObjectModel#INVALID_HANDLE} will be returned.
	 * 
	 * @param version the HLA version of the MOM naming scheme that the <code>name</code> parameter 
	 *                conforms to
	 * @param classHandle the handle of the object class to search for the attribute in
	 * @param the name of the attribute
	 * @return the handle of the desired attribute, or {@link ObjectModel#INVALID_HANDLE} if the specified
	 *         class does not exist or does not contain an attribute with the specified name
	 */
	public static int getMomAttributeHandle( HLAVersion version, int classHandle, String name )
	{
		int attributeHandle = ObjectModel.INVALID_HANDLE;
		
		MomTreeNode objectNode = INSTANCE.momTree.find( MomType.Object,
		                                                classHandle, 
		                                                INSTANCE.momTree.objectRoot );
		
		if( objectNode != null )
		{
			MomTreeNode attributeNode = INSTANCE.momTree.find( name, version, objectNode );
			if( attributeNode != null )
				attributeHandle = attributeNode.getHandle();
		}

		return attributeHandle;
	}
	
	/**
	 * Fetch the name for the MOM attribute that belongs to the MOM class of the given handle. If the 
	 * class handle is not recognized or the attribute handle isn't a valid attribute,
	 * {@link ObjectModel#INVALID_HANDLE} will be returned.
	 * 
	 * @param version the HLA version of the MOM naming scheme to use
	 * @param attributeHandle the handle of the attribute
	 * @return the name of the desired attribute, or <code>null</code> if the specified
	 *         class does not exist or does not contain the specified attribute 
	 */
	public static String getMomAttributeName( HLAVersion version, int attributeHandle )
	{
		String name = null;
		
		MomTreeNode attributeNode = INSTANCE.attributeLookup.get( attributeHandle );
		if( attributeNode != null )
			name = attributeNode.getName( version );
		
		return name;
	}
	
	/**
	 * Fetch the handle for the MOM interaction class of the given name. If the class name is unknown,
	 * {@link ObjectModel#INVALID_HANDLE} will be returned.
	 * <p/>
	 * This function expects a fully qualified name be provided in the <code>className</code> parameter,
	 * relative to <code>InteractionRoot</code> (e.g. "Manager.Federate.Request.RequestSubscriptions")
	 * <p/>
	 * If the class name is unknown, {@link ObjectModel#INVALID_HANDLE} will be returned.
	 * 
	 * @param version the HLA version of the MOM naming scheme that the <code>className</code> parameter 
	 *                conforms to
	 * @param className the qualified name of the interaction class relative to InteractionRoot
	 * @return the handle of the desired interaction class, or {@link ObjectModel#INVALID_HANDLE} if no
	 *         such interaction class exists
	 */
	public static int getMomInteractionHandle( HLAVersion version, String className )
	{
		MomTreeNode node = INSTANCE.momTree.find( className, 
		                                          version,
		                                          INSTANCE.momTree.interactionRoot );
		if( node != null )
		{
			return node.getHandle();
		}
		else
		{
			return ObjectModel.INVALID_HANDLE;
		}
	}
	
	/**
	 * Fetch the name for the MOM interaction class of the given handle. 
	 * <p/>
	 * If the class handle is not recognized, {@link ObjectModel#INVALID_HANDLE} will be returned.
	 * 
	 * @param version the HLA version of the MOM naming scheme to use
	 * @param classHandle the handle of the interaction class
	 * @param qualified <code>true</code> to return the fully qualified name of the interaction class,
	 *                  otherwise <code>false</code> to return the name of the specified interaction class 
	 *                  only
	 * @return the name of the desired interaction class, or <code>null</code> if the specified
	 *         class does not exist 
	 */
	public static String getMomInteractionName( HLAVersion version, int handle, boolean qualified )
	{
		String name = null;
		MomTreeNode interactionNode = INSTANCE.interactionLookup.get( handle );
		
		if( interactionNode != null )
		{
			if( qualified )
				name = interactionNode.getQualifiedName( version );
			else
				name = interactionNode.getName( version );
		}
		
		return name;
	}
	
	/**
	 * Fetch the name of the MOM Interaction Parameter of the specified handle. 
	 * <p/>
	 * If no parameter exists within the specified interaction with the given handle, then 
	 * <code>null</code> is returned.
	 * 
	 * @param version the HLA version of the MOM naming scheme to format the name for
	 * @param parameterHandle the handle of the parameter to fetch the name for
	 */
	public static String getMomParameterName( HLAVersion version, 
	                                          int parameterHandle )
	{
		String name = null;
		MomTreeNode paramNode = INSTANCE.parameterLookup.get( parameterHandle );
		if( paramNode != null )
			name = paramNode.getName( version );
		
		return name;
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
	public static void insertMomHierarchy( ObjectModel model )
	{
		HLAVersion version = model.getHlaVersion();
		
		// MOM Objects
		OCMetadata objectRoot = model.getObjectRoot();
		MomTreeNode objectRootNode = INSTANCE.momTree.objectRoot;
		for( MomTreeNode objectNode : objectRootNode.getChildren() )
			insertMomObject( objectRoot, objectNode, version );

		// MOM Interactions
		ICMetadata interactionRoot = model.getInteractionRoot();
		MomTreeNode interactionRootNode = INSTANCE.momTree.interactionRoot;
		for( MomTreeNode interactionNode : interactionRootNode.getChildren() )
			insertMomInteraction( interactionRoot, interactionNode, version );
	}
	
	private static void insertMomObject( OCMetadata modelParent,
	                                     MomTreeNode objectNode,
	                                     HLAVersion version )
	{
		// Only insert nodes that are for the desired HLA Version
		if( !objectNode.isSupportedVersion(version) )
			return;
		
		ObjectModel model = modelParent.getModel();
		OCMetadata ocMetadata = new OCMetadata( objectNode.getName(version), 
		                                        objectNode.getHandle() );
		ocMetadata.setParent( modelParent );
		model.addObjectClass( ocMetadata );
		
		// Process Children
		for( MomTreeNode child : objectNode.getChildren() )
		{
			String versionName = child.getName( version );
			int handle = child.getHandle();
			
			MomType type = child.getType();
			if( type == MomType.Attribute && child.isSupportedVersion(version) )
			{
				// Child is an attribute
				IDatatype datatype = model.getDatatype( child.getDatatype(version) );
				ACMetadata attribute = new ACMetadata( versionName, datatype, handle );
				attribute.setTransport( Transport.RELIABLE );
				attribute.setOrder( Order.RECEIVE );
				ocMetadata.addAttribute( attribute );
			}
			else if( type == MomType.Object )
			{
				// Child is nested object class
				insertMomObject( ocMetadata, child, version );
			}
		}
	}
	
	private static void insertMomInteraction( ICMetadata modelParent,
	                                          MomTreeNode interactionNode,
	                                          HLAVersion version )
	{
		// Only insert nodes that are for the desired HLA Version
		if( !interactionNode.isSupportedVersion(version) )
			return;
		
		ObjectModel model = modelParent.getModel();
		ICMetadata icMetadata = new ICMetadata( interactionNode.getName(version), 
		                                        interactionNode.getHandle() );
		icMetadata.setParent( modelParent );
		model.addInteractionClass( icMetadata );
		
		// Process Children
		for( MomTreeNode child : interactionNode.getChildren() )
		{
			String versionName = child.getName( version );
			int handle = child.getHandle();
			
			MomType type = child.getType();
			if( type == MomType.Parameter && child.isSupportedVersion(version) )
			{
				// Child is a parameter
				IDatatype datatype = model.getDatatype( child.getDatatype(version) );
				PCMetadata param = new PCMetadata( versionName, datatype, handle );
				icMetadata.addParameter( param );
			}
			else if( type == MomType.Interaction )
			{
				// Child is nested interaction
				insertMomInteraction( icMetadata, child, version );
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// MOM Hierarchy Generation Methods ////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * A helper class that allows declarative construction of the MOM tree.
	 * <p/>
	 * The {@link MomTreeNodeBuilder} class supports chained declarative building by returning a reference
	 * to itself from each of the builder methods (e.g. {@link #object(String)}, 
	 * {@link #attribute(String, String)}) and maintaining an internal context stack. For example an 
	 * object class with attributes and a sub-class could be declared as such:
	 * 
	 * <pre>
	 *  MomTreeNodeBuilder b = new MomTreeNodeBuilder();
	 *  b.object("BaseClass")           // push an object node onto the context stack
	 *    .attribute("baseAttribute")   // add an attribute to the current context (BaseClass)
	 *    .object("ChildClass")         // push another object node onto the context stack
	 *     .attribute("childAttribute") // add an attribute to the current context (BaseClass.ChildClass)
	 *    .end()                        // pop the current context from the stack
	 *  MomTreeNode baseNode = b.get(); // get the current context from the stack (BaseClass)
	 * </pre>
	 */
	private static class MomTreeNodeBuilder
	{
		private int objectCounter = 0;
		private int attributeCounter = 0;
		private int interactionCounter = 0;
		private int parameterCounter = 0;
		private Map<Integer,MomTreeNode> objectLookup;
		private Map<Integer,MomTreeNode> attributeLookup;
		private Map<Integer,MomTreeNode> interactionLookup;
		private Map<Integer,MomTreeNode> parameterLookup;
		
		private Stack<MomTreeNode> context;
		
		public MomTreeNodeBuilder()
		{
			this.context = new Stack<>();
			this.objectLookup = new HashMap<>();
			this.attributeLookup = new HashMap<>();
			this.interactionLookup = new HashMap<>();
			this.parameterLookup = new HashMap<>();
		}
		
		/**
		 * Adds a node to the context current node. 
		 * <p/>
		 * <b>Note:</b> This method does not affect the context stack.
		 *  
		 * @param newNode the node to add to the current context node
		 * @throws IllegalStateException if the context stack is currently empty
		 */
		private void add( MomTreeNode newNode )
		{
			if( !this.context.isEmpty() )
			{
				MomTreeNode currentContext = this.context.peek();
				currentContext.addChild( newNode );
			}
			else
			{
				throw new IllegalStateException( "no context to add to" );
			}
		}
		
		/**
		 * Adds the specified node to the context current node and pushes it onto the context stack. 
		 *  
		 * @param newNode the node to add to the current context node
		 */
		private void addAndPush( MomTreeNode newNode )
		{
			if( !this.context.isEmpty() )
			{
				MomTreeNode currentContext = this.context.peek();
				currentContext.addChild( newNode );
			}
			
			this.context.push( newNode );
		}
		
		/**
		 * Creates an object {@link MomTreeNode} for all HLA Versions and adds it to the current context 
		 * node.
		 * <p/>
		 * <b>Note:</b> The object's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * <p/>
		 * The created node is pushed onto the context stack to become the new context node.
		 * 
		 * @param name the name of the object
		 * @return this
		 */
		public MomTreeNodeBuilder object( String name )
		{
			return object( name, HLAVersion.values() );
		}
		
		/**
		 * Creates an object {@link MomTreeNode} for the specified HLA Versions and adds it to the current 
		 * context node.
		 * <p/>
		 * <b>Note:</b> The object's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * <p/>
		 * The created node is pushed onto the context stack to become the new context node.
		 * 
		 * @param name the name of the object
		 * @param supportedVersions the HLA Versions that the object is supported in
		 * @return this
		 */
		public MomTreeNodeBuilder object( String name, HLAVersion... supportedVersions )
		{
			Set<HLAVersion> versions = new HashSet<>(); 
			for( HLAVersion version : supportedVersions )
				versions.add( version );
			
			int handle = objectCounter++;
			MomTreeNode node = new MomTreeNode( VersionedName.from(name), 
			                                    MomType.Object, 
			                                    handle,
			                                    versions );
			
			this.addAndPush( node );
			objectLookup.put( handle, node );
			return this;
		}
		
		/**
		 * Creates an attribute {@link MomTreeNode} for all HLA Versions and adds it to the current 
		 * context node.
		 * <p/>
		 * <b>Note:</b> The attribute's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * 
		 * @param name the name of the attribute
		 * @param datatype the name of this attribute's datatype
		 * @return this
		 * @throws IllegalStateException if the current context is not an Object node
		 */
		public MomTreeNodeBuilder attribute( String name, String datatype )
		{
			return attribute( VersionedName.from(name), datatype, HLAVersion.values() );
		}
		
		/**
		 * Creates an attribute {@link MomTreeNode} for all HLA Versions and adds it to the current 
		 * context node.
		 * 
		 * @param name the name of the attribute under each HLA version
		 * @param datatype the name of this attribute's datatype
		 * @return this
		 * @throws IllegalStateException if the current context is not an Object node
		 */
		public MomTreeNodeBuilder attribute( VersionedName name, String datatype )
		{
			return attribute( name, datatype, HLAVersion.values() );
		}
		
		/**
		 * Creates an attribute {@link MomTreeNode} for the specified HLA Versions and adds it to the 
		 * current context node.
		 * <p/>
		 * <b>Note:</b> The attribute's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * 
		 * @param name the name of the attribute for each HLA version
		 * @param datatype the name of this attribute's datatype
		 * @param supportedVersions the HLA Versions that support this attribute
		 * @return this
		 * @throws IllegalStateException if the current context is not an Object node
		 */
		public MomTreeNodeBuilder attribute( String name, String datatype, HLAVersion... supportedVersions )
		{
			return attribute( VersionedName.from(name), datatype, supportedVersions );
		}
		
		/**
		 * Creates an attribute {@link MomTreeNode} for the specified HLA Versions and adds it to the 
		 * current context node.
		 * 
		 * @param name the name of the attribute under each HLA version
		 * @param datatype the name of this attribute's datatype
		 * @param supportedVersions the HLA Versions that support this attribute
		 * @return this
		 * @throws IllegalStateException if the current context is not an Object node
		 */
		public MomTreeNodeBuilder attribute( VersionedName name, 
		                                     String datatype, 
		                                     HLAVersion... supportedVersions )
		{
			if( context.size() == 0 )
				throw new IllegalStateException( "attribute cannot be root node" );
			
			if( context.peek().type != MomType.Object )
				throw new IllegalStateException( "attribute can only be added to an object" );
			
			Set<HLAVersion> versions = new HashSet<>(); 
			for( HLAVersion version : supportedVersions )
				versions.add( version );
			
			int handle = attributeCounter++;
			MomTreeNode node = new MomTreeNode( name, 
			                                    MomType.Attribute,
			                                    datatype,
			                                    handle,
			                                    versions );
			this.add( node );
			this.attributeLookup.put( handle, node );
			return this;
		}
		
		/**
		 * Creates an interaction {@link MomTreeNode} for all HLA Versions and adds it to the current 
		 * context node.
		 * <p/>
		 * <b>Note:</b> The interaction's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * <p/>
		 * The created node is pushed onto the context stack to become the new context node.
		 * 
		 * @param name the name of the interaction
		 * @return this
		 */
		public MomTreeNodeBuilder interaction( String name )
		{
			return interaction( name, HLAVersion.values() );
		}
		
		/**
		 * Creates an interaction {@link MomTreeNode} for all HLA Versions and adds it to the current 
		 * context node.
		 * <p/>
		 * The created node is pushed onto the context stack to become the new context node.
		 * 
		 * @param name the name of the interaction under each HLA version
		 * @return this
		 */
		public MomTreeNodeBuilder interaction( VersionedName name )
		{
			return interaction( name, HLAVersion.values() );
		}
		
		/**
		 * Creates an interaction {@link MomTreeNode} for the specified HLA Versions and adds it to the 
		 * current context node.
		 * <p/>
		 * <b>Note:</b> The interaction's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * <p/>
		 * The created node is pushed onto the context stack to become the new context node.
		 * 
		 * @param name the name of the interaction
		 * @param supportedVersions the HLA Versions that support this attribute
		 * @return this
		 */
		public MomTreeNodeBuilder interaction( String name, HLAVersion... supportedVersions )
		{
			return interaction( VersionedName.from(name), supportedVersions );
		}
		
		/**
		 * Creates an interaction {@link MomTreeNode} for the specified HLA Versions and adds it to the 
		 * current context node.
		 * <p/>
		 * <b>Note:</b> The interaction's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * <p/>
		 * The created node is pushed onto the context stack to become the new context node.
		 * 
		 * @param name the name of the interaction under each HLA version
		 * @param supportedVersions the HLA Versions that support this attribute
		 * @return this
		 */
		public MomTreeNodeBuilder interaction( VersionedName name, HLAVersion... supportedVersions )
		{
			Set<HLAVersion> versions = new HashSet<>(); 
			for( HLAVersion version : supportedVersions )
				versions.add( version );
			
			int handle = interactionCounter++;
			MomTreeNode node = new MomTreeNode( name, 
			                                    MomType.Interaction, 
			                                    handle,
			                                    versions );
			
			this.addAndPush( node );
			this.interactionLookup.put( handle, node );
			return this;
		}
		
		/**
		 * Creates a parameter {@link MomTreeNode} for all HLA Versions and adds it to the current 
		 * context node.
		 * <p/>
		 * <b>Note:</b> The parameter's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * 
		 * @param name the name of the parameter
		 * @param datatype the name of this parameter's datatype
		 * @return this
		 * @throws IllegalStateException if the current context is not an Interaction node
		 */
		public MomTreeNodeBuilder parameter( String name, String datatype )
		{
			return parameter( name, datatype, HLAVersion.values() );
		}
		
		/**
		 * Creates a parameter {@link MomTreeNode} for all HLA Versions and adds it to the current 
		 * context node.
		 * <p/>
		 * <b>Note:</b> The parameter's name for each HLA version naming scheme will be automatically 
		 * generated from the <code>name</code> parameter via the simple pattern rule.
		 * 
		 * @param name the name of the parameter under each HLA version
		 * @param datatype the name of this parameter's datatype
		 * @return this
		 * @throws IllegalStateException if the current context is not an Interaction node
		 */
		public MomTreeNodeBuilder parameter( VersionedName name, String datatype )
		{
			return parameter( name, datatype, HLAVersion.values() );
		}
		
		/**
		 * Creates a parameter {@link MomTreeNode} for the specified HLA Versions and adds it to the 
		 * current context node.
		 * 
		 * @param name the name of the parameter
		 * @param datatype the name of this parameter's datatype
		 * @param supportedVersions the HLA Versions that support this parameter
		 * @return this
		 * @throws IllegalStateException if the current context is not an Parameter node
		 */
		public MomTreeNodeBuilder parameter( String name, String datatype, HLAVersion...supportedVersions )
		{
			return parameter( VersionedName.from(name), datatype, supportedVersions );
		}
		
		/**
		 * Creates a parameter {@link MomTreeNode} for the specified HLA Versions and adds it to the 
		 * current context node.
		 * 
		 * @param name the name of the parameter under each HLA version
		 * @param datatype the name of this parameter's datatype
		 * @param supportedVersions the HLA Versions that support this parameter
		 * @return this
		 * @throws IllegalStateException if the current context is not an Parameter node
		 */
		public MomTreeNodeBuilder parameter( VersionedName name, 
		                                     String datatype, 
		                                     HLAVersion...supportedVersions )
		{
			if( context.size() == 0 )
				throw new IllegalStateException( "parameter cannot be root node" );
			
			if( context.peek().type != MomType.Interaction )
				throw new IllegalStateException( "parameter can only be added to an interaction" );
			
			Set<HLAVersion> versions = new HashSet<>(); 
			for( HLAVersion version : supportedVersions )
				versions.add( version );
			
			int handle = parameterCounter++;
			MomTreeNode node = new MomTreeNode( name, 
			                                    MomType.Parameter, 
			                                    datatype, 
			                                    handle,
			                                    versions );
			this.add( node );
			this.parameterLookup.put( handle, node );
			return this;
		}
		
		/**
		 * Marks the end of the current context node, popping it from the stack.
		 * 
		 * @return the previous node on the context stack
		 */
		public MomTreeNodeBuilder end()
		{
			this.context.pop();
			return this;
		}
		
		/**
		 * @return the current context node
		 */
		public MomTreeNode getContext()
		{
			return this.context.peek();
		}
		
		public Map<Integer,MomTreeNode> getObjectLookup()
		{
			return this.objectLookup;
		}
		
		public Map<Integer,MomTreeNode> getAttributeLookup()
		{
			return this.attributeLookup;
		}
		
		public Map<Integer,MomTreeNode> getInteractionLookup()
		{
			return this.interactionLookup;
		}
		
		public Map<Integer,MomTreeNode> getParameterLookup()
		{
			return this.parameterLookup;
		}
	}
	
	/**
	 * A tree to hold the internal representation of metadata for standard MOM types.
	 * <p/>
	 * The tree contains nodes representing the hierarchy of objects, attributes, interactions and 
	 * parameters and can be used to populate an {@link ObjectModel} with {@link OCMetadata}
	 * and {@link ICMetadata} for all MOM types.
	 * <p/>
	 * To allow a cross-version lookup, we store a {@link VersionedName} for each node, so that we can 
	 * resolve MOM type handles regardless of which naming version is being used.
	 * <p/>
	 * Nodes can be fetched from the tree by qualified name using 
	 * {@link #find(String, HLAVersion, MomTreeNode)}, or by handle by calling 
	 * {@link #find(MomType, int, MomTreeNode)}.
	 */
	private static class MomHandleTree
	{
		private MomTreeNode objectRoot;
		private MomTreeNode interactionRoot;
		
		/**
		 * MomHandleTree constructor with specified interaction root
		 * 
		 * @param interactionRoot the tree node representing InteractionRoot
		 */
		private MomHandleTree( MomTreeNode objectRoot, MomTreeNode interactionRoot )
		{
			this.objectRoot = objectRoot;
			this.interactionRoot = interactionRoot;
		}
		
		/**
		 * Finds the tree node that matches the specified qualified name.
		 * 
		 * @param name the qualified name of the node to find e.g. "Manager.Federation.Request"
		 * @param version the HLA version that the <code>name</code> parameter conforms to
		 * @param root the node to begin the search from
		 * @return the node in the tree that matches the specified name, or <code>null</code> if no node
		 *         exists in the tree with that name 
		 */
		public MomTreeNode find( String name, HLAVersion version, MomTreeNode root )
		{
			// Break the qualified name into tokens
			String[] nameTokens = tokenizeName( name );
			
			MomTreeNode context = root;
			for( int i = 0 ; i < nameTokens.length ; ++i )
			{
				// The current token we are searching for
				String matchName = nameTokens[i].toLowerCase();
				MomTreeNode nextContext = null;
				
				// Iterate over all child nodes in the current context, searching for the one that matches 
				// the current token
				List<MomTreeNode> children = context.getChildren();
				for( MomTreeNode child : children )
				{
					String childName = child.getName(version).toLowerCase();
					if( childName.equals(matchName) )
					{
						// This node matches the current token that we're looking for, so it becomes the
						// context for the next token
						nextContext = child;
						break;
					}
				}
				
				if( nextContext == null )
				{
					// Next part in the chain wasn't found, so break from the loop, returning null
					break;
				}
				else
				{
					context = nextContext;
				}
			}
			
			return context;
		}
		
		/**
		 * Finds the tree node with the specified handle
		 * 
		 * @param type the type of node to search for
		 * @param handle the handle of the node to search for
		 * @param version the HLA version that the <code>name</code> parameter conforms to
		 * @param root the node to begin the search from
		 * @return the node in the tree that matches the specified type/handle, or <code>null</code> if no 
		 *         node exists in the tree that matches the specified type/handle 
		 */
		public MomTreeNode find( MomType type, int handle, MomTreeNode root )
		{
			// TODO This could be optimized by caching found results, or creating a lookup tree on
			// startup
			
			MomTreeNode found = null;
			if( root.type == type && root.handle == handle )
			{
				// This is the node!
				found = root;
			}
			else
			{
				// Recurse find over all child nodes
				for( MomTreeNode child : root.children )
				{
					found = find( type, handle, child );
					if( found != null )
						break;
				}
			}
			
			return found;
		}
		
		/**
		 * Splits a qualified name into individual tokens
		 * 
		 * @param name the qualified name to split
		 * @return the name tokens ordered from root to tip
		 */
		private String[] tokenizeName( String name )
		{
			StringTokenizer tokenizer = new StringTokenizer( name, "." );
			String[] tokenArray = new String[tokenizer.countTokens()];
			for( int i = 0 ; i < tokenArray.length ; ++i )
				tokenArray[i] = tokenizer.nextToken();
			
			return tokenArray;
		}
	}
	
	/**
	 * A node in the MomHandleTree.
	 * <p/>
	 * Nodes in the tree may represent Mom Object Classes, Attributes, Interaction Classes or Parameters.
	 * <p/>
	 * To allow a cross-version lookup, we store a {@link VersionedName} for each node, so that we can 
	 * resolve MOM type handles regardless of which naming version is being used.
	 */
	private static class MomTreeNode
	{
		private MomTreeNode parent;
		private List<MomTreeNode> children;
		private VersionedName name;
		private int handle;
		private MomType type;
		private String datatype; // For attributes and parameters, only for 1516+ (all mom types are strings in 1.3)
		private Set<HLAVersion> supportedVersions;
		
		public MomTreeNode( VersionedName name, 
		                    MomType type, 
		                    int handle, 
		                    Collection<HLAVersion> supportedVersions )
		{
			this( name, type, null, handle, supportedVersions );
		}
		
		public MomTreeNode( VersionedName name, 
		                    MomType type, 
		                    String datatype, 
		                    int handle, 
		                    Collection<HLAVersion> supportedVersions )
		{
			this.name = name;
			this.parent = null;
			this.handle = handle;
			this.type = type;
			this.children = new ArrayList<>();
			this.datatype = datatype;
			this.supportedVersions = EnumSet.copyOf( supportedVersions );
		}
		
		public boolean isSupportedVersion( HLAVersion version )
		{
			return this.supportedVersions.contains( version );
		}
		
		public MomTreeNode getParent()
		{
			return this.parent;
		}
		
		public MomType getType()
		{
			return this.type;
		}
		
		public int getHandle()
		{
			return this.handle;
		}
		
		public List<MomTreeNode> getChildren()
		{
			return new ArrayList<>( this.children );
		}
		
		/**
		 * Returns the version-specific name of this node.
		 *  
		 * @param version a HLA specification version
		 * @return the name of the node for the specified HLA version
		 * 
		 * @see #getQualifiedName(HLAVersion)
		 */
		public String getName( HLAVersion version )
		{
			return this.name.get( version );
		}
		
		/**
		 * Returns the fully qualified version-specific name of this node.
		 *  
		 * @param version a HLA specification version
		 * @return the name of the node for the specified HLA version
		 * 
		 * @see #getName(HLAVersion)
		 */
		public String getQualifiedName( HLAVersion version )
		{
			String name = this.getName( version );
			if( this.parent != null )
				name = this.parent.getQualifiedName( version ) + "." + name;
			
			return name;
		}
		
		public String getDatatype( HLAVersion version )
		{
			// The datatype for all MOM attributes/params in HLA13 are plain strings
			if( version == HLAVersion.HLA13 || version == HLAVersion.JAVA1 )
				return "HLAASCIIstring";
			else
				return this.datatype;
		}
		
		public void addChild( MomTreeNode node )
		{
			if( node.parent == null )
			{
				node.parent = this;
				this.children.add( node );
			}
			else
			{
				throw new IllegalArgumentException( node.getName(HLAVersion.IEEE1516e) + 
				                                    " already has a parent" );
			}
		}
		
		public String toString()
		{
			return this.getName( HLAVersion.IEEE1516e );
		}
	}
}
