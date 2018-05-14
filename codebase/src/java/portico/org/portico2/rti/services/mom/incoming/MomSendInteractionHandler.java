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
package org.portico2.rti.services.mom.incoming;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.rti.RtiConnection;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.mom.data.MomEncodingHelpers;
import org.portico2.rti.services.mom.data.SynchPointFederate;
import org.portico2.rti.services.sync.data.SyncPoint;
import org.portico2.rti.services.sync.data.SyncPointManager;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;

/**
 * This handler receives all {@link SendInteraction} messages reflected into the Federation's internal
 * message sink when {@link Federation#queueDataMessage(PorticoMessage, RtiConnection)} is called.
 * <p/>
 * The purpose of the handler is two-fold:
 * <ol>
 *  <li>Collect metrics on interactions sent and received for ALL interactions</code>
 *  <li>Process and respond to MOM interactions</code>
 * </ol>
 * Handler methods for MOM interactions can be registered by calling the 
 * {@link #registerMomInteractionHandler(String, ConsumerWithException)} method 
 */
public class MomSendInteractionHandler extends RTIMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/**
	 * Mom elements have different names based on which version of the spec the sending federate 
	 * implements. To make things more manageable, the manager normalizes all names to a canonical
	 * version for processing.
	 */
	private static final HLAVersion CANONICAL_VERSION = HLAVersion.IEEE1516e;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Integer,ConsumerWithException<Map<String,Object>,MomException>> handlers;
	private boolean supportsMomException;
	private ICMetadata reportMomExceptionMetadata;
	private PCMetadata serviceMetadata;
	private PCMetadata exceptionMetadata;
	private PCMetadata parameterErrorMetadata;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomSendInteractionHandler()
	{
		// Register handlers for requests that we will respond to
		this.handlers = new HashMap<>();
		this.registerMomInteractionHandler( "HLAmanager.HLAfederation.HLArequest.HLArequestSynchronizationPoints", 
		                                    this::handleRequestSynchronizationPoints );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederation.HLArequest.HLArequestSynchronizationPointStatus", 
		                                    this::handleRequestSynchronizationPointStatus );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
		this.cacheReportMomExceptionMetadata();
	}
	
	@Override
	public void process( MessageContext context ) throws JException
	{
		ObjectModel objectModel = federation.getFOM();
		SendInteraction request = context.getRequest( SendInteraction.class );
		
		// Record metrics for the interaction sender
		int interactionId = request.getInteractionId();
		int sender = request.getSourceFederate();
		momManager.interactionSent( sender, interactionId );
		
		// Record metrics for all interaction receivers
		ICMetadata interactionClass = objectModel.getInteractionClass( interactionId );
		InterestManager interests = federation.getInterestManager();
		Set<Integer> subscribers = interests.getAllSubscribers( interactionClass );
		for( int subscriber : subscribers )
		{
			if( subscriber != sender )
				momManager.interactionReceived( subscriber, interactionId );
		}
		
		// If the incoming interaction is a MOM interaction, then handle it
		if( interactionId < ObjectModel.MAX_MOM_HANDLE && !request.isFromRti() )
			processMomInteraction( request );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  MOM Interaction Handlers   ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void handleRequestSynchronizationPoints( Map<String,Object> requestParams )
		throws MomException
	{
		// Get the labels of all syncpoints 
		SyncPointManager syncPointMan = this.federation.getSyncPointManager();
		Collection<SyncPoint> syncPoints = syncPointMan.getAllPoints();
		
		String[] labels = new String[syncPoints.size()];
		int index = 0;
		for( SyncPoint syncPoint : syncPoints )
			labels[index++] = syncPoint.getLabel();
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAsyncPoints", labels );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederation.HLAreport.HLAreportSynchronizationPoints", 
		              responseParams );
	}
	
	private void handleRequestSynchronizationPointStatus( Map<String,Object> requestParams )
		throws MomException
	{
		Map<String,Object> responseParams = new HashMap<>();
		
		String name = (String)requestParams.get( "HLAsyncPointName" );
		responseParams.put( "HLAsyncPointName", name );
		
		// Get the syncpoint status for each federate
		SyncPointManager syncPointMan = this.federation.getSyncPointManager();
		SyncPoint syncPoint = syncPointMan.getPoint( name );
		
		// Spec says that if there is no syncpoint for the name, then the HLAsyncPointFederates response
		// parameter is left undefined
		if( syncPoint != null )
		{
			SynchPointFederate[] syncPointFederates = SynchPointFederate.forSyncPoint( federation, 
			                                                                           syncPoint );
			responseParams.put( "HLAsyncPointFederates", syncPointFederates );
		}
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederation.HLAreport.HLAreportSynchronizationPointStatus", 
		              responseParams );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Helper Convenience Methods   //////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Caches interaction metadata for HLAreportMomException.
	 * <p/>
	 * This interaction is sent if an exception was raised while trying to process MOM request 
	 * interactions from a federate.
	 */
	private void cacheReportMomExceptionMetadata()
	{
		// Get the handle from the canonical name
		int reportExceptionId = Mom.getMomInteractionHandle( CANONICAL_VERSION, 
		                                                     "HLAmanager.HLAfederate.HLAreport.HLAreportMomException" );
		
		// Get the interaction metadata from the federation's object model
		this.reportMomExceptionMetadata = fom().getInteractionClass( reportExceptionId );
		if( reportMomExceptionMetadata != null )
		{
			this.supportsMomException = true;
			
			// Cache parameter metadata
			Set<PCMetadata> reportParamMetadata = reportMomExceptionMetadata.getAllParameters();
			Map<Integer,byte[]> params = new HashMap<Integer,byte[]>();
			for( PCMetadata paramMetadata : reportParamMetadata )
			{
				int paramId = paramMetadata.getHandle();
				String canonicalName = Mom.getMomParameterName( CANONICAL_VERSION, 
				                                                paramId );
				if( canonicalName.equals("HLAservice") )
					this.serviceMetadata = paramMetadata;
				else if( canonicalName.equals("HLAexception") )
					this.exceptionMetadata = paramMetadata;
				else if( canonicalName.equals("HLAparameterError") )
					this.parameterErrorMetadata = paramMetadata;
			}
		}
		else
		{
			// HLAreportMomException not supported under this HLA Version
			this.supportsMomException = false;
		}
	}
	
	/**
	 * Registers a handler for the provided MOM interaction name
	 * @param name the fully qualified, canonical name of the interaction (relative to HLAinteractionRoot)
	 * @param handler the handler for the interaction
	 */
	private void registerMomInteractionHandler( String name, 
	                                            ConsumerWithException<Map<String,Object>,MomException> handler )
	{
		int handle = Mom.getMomInteractionHandle( CANONICAL_VERSION, name );
		if( handle == ObjectModel.INVALID_HANDLE )
			throw new JRTIinternalError( "Not a MOM interaction: " + name );
		
		this.handlers.put( handle, handler );
	}
	
	/**
	 * Processes an incoming MOM interaction.
	 * <p/>
	 * This method will query the internal <code>handlers</code> map for the handler that corresponds to
	 * the interaction provided in the <code>request</code> parameter.
	 * <p/> 
	 * If a handler is found for the interaction, then processing is deferred to it. If a 
	 * {@link MomException} is raised while processing the interaction, a corresponding 
	 * <code>HLAreportMOMexception</code> interaction is sent, detailing the error. 
	 * <p/>
	 * If no handler is found, an appropriate message is logged.
	 * 
	 * @param request the incoming {@link SendInteraction} message to process
	 */
	private void processMomInteraction( SendInteraction request )
	{
		// Is there a handler for this interaction?
		int interactionId = request.getInteractionId();
		ConsumerWithException<Map<String,Object>,MomException> handler = 
			this.handlers.get( interactionId );
		if( handler != null )
		{
			try
			{
				// Decode the incoming parameters. This will attempt to decode all parameter
				// values based on their FOM datatype, and will map them against the parameter's
				// canonical name
				Map<String,Object> params = decodeRequestParameters( interactionId, 
				                                                     request.getParameters() );
				
				// Pass the params to the handler for processing!
				handler.accept( params );
			}
			catch( MomException mie )
			{
				// If an exception occurred, then send a ReportMomException interaction (if the
				// current federation version allows it)
				this.reportMomException( request, mie );
			}
		}
		else
		{
			logger.warn( "No handler for incoming MOM interaction " + interactionId );
		}
	}
	
	private ICMetadata getMomInteractionMetadata( String name )
	{
		int handle = Mom.getMomInteractionHandle( CANONICAL_VERSION, name );
		
		ObjectModel fom = federation.getFOM();
		ICMetadata metadata = fom.getInteractionClass( handle );
		if( metadata == null )
			throw new JRTIinternalError( "invalid MOM interaction: " + name );
		
		return metadata;
	}
	
	/**
	 * Decodes the values of the specified interaction parameters and maps them against their canonical 
	 * parameter names.
	 * <p/>
	 * Values are decoded based on the {@link IDatatype} of their corresponding parameter.
	 * 
	 * @param interactionId the identifier of the interaction that the parameters belong to
	 * @param params the parameters as received from {@link SendInteraction#getParameters()}
	 * @return the decoded parameter values, mapped against the canonical name of their corresponding 
	 *         parameters 
	 * @throws MomException if a parameter value could not be decoded, or if an expected parameter entry 
	 *                      was missing from <code>params</code>
	 */
	private Map<String,Object> decodeRequestParameters( int interactionId, 
	                                                    Map<Integer,byte[]> params )
		throws MomException
	{
		Map<String,Object> decoded = new HashMap<>();
		
		// Iterate over all the parameters that we expect to be provided in the request
		ICMetadata requestMetadata = fom().getInteractionClass( interactionId );
		Set<PCMetadata> paramMetadata = requestMetadata.getAllParameters();
		for( PCMetadata expectedParam : paramMetadata )
		{
			IDatatype type = expectedParam.getDatatype();
			int parameterId = expectedParam.getHandle();
			byte[] rawValue = params.get( parameterId );
			if( rawValue != null )
			{
				// Get the parameter's canonical name
				String name = Mom.getMomParameterName( CANONICAL_VERSION, 
				                                       parameterId );
				if( name == null )
				{
					// If we get here, then it's an internal configuration issue (e.g. we don't have
					// the parameter in our MOM tree
					throw new JRTIinternalError( "could not resolve canonical name for parameter " + 
					                             parameterId + 
					                             " of interaction " + interactionId );
				}
				
				Object value = null;
				try
				{
					value = MomEncodingHelpers.decode( type, rawValue );
				}
				catch( DecoderException de )
				{
					// Could not decode the value
					throw new MomException( "could not decode value for parameter " + 
					                        expectedParam.getName() + "[id=" + parameterId + "]",
					                        true,
					                        de );
				}
				
				decoded.put( name, value );
			}
			else
			{
				// Expected parameter was not provided
				throw new MomException( "no value provided for required parameter " + 
				                        expectedParam.getName() + "[id=" + parameterId + "]",
				                        true );
			}
		}
		
		return decoded;
	}
	
	/**
	 * Encodes the values of the specified response parameters and maps them against their corresponding 
	 * parameter handle
	 * <p/>
	 * Values are encoded based on the {@link IDatatype} of their corresponding parameter.
	 * 
	 * @param interactionMetadata the metadata of the response being sent
	 * @param params the values of the response parameters, mapped against their canonical parameter name
	 * @return the encoded parameter values, mapped against the handle of their corresponding parameters 
	 * @throws EncoderException if a parameter value could not be encoded.
	 */
	private HashMap<Integer,byte[]> encodeResponseParameters( ICMetadata interactionMetadata,
	                                                          Map<String,Object> params )
	{
		HashMap<Integer,byte[]> encoded = new HashMap<>();
		for( PCMetadata paramMetadata : interactionMetadata.getAllParameters() )
		{
			int paramHandle = paramMetadata.getHandle();
			IDatatype datatype = paramMetadata.getDatatype();
			String paramName = Mom.getMomParameterName( CANONICAL_VERSION,
			                                            paramHandle );
			if( paramName == null )
			{
				// Programmer error
				throw new JRTIinternalError( "not a canonical parameter name: " + paramName );
			}

			Object value = params.get( paramName );
			if( value != null )
			{
				byte[] encodedValue = MomEncodingHelpers.encode( datatype, value );
				encoded.put( paramHandle, encodedValue );
			}
			else
			{
				logger.warn( "No parameter value found for " + paramMetadata.getName() );
			}
		}
		
		return encoded;
	}
	
	/**
	 * Reports a {@link MomException} to the federate that sent the request interaction the exception was 
	 * raised in response to. 
	 * 
	 * @param request the request that raised the exception
	 * @param exception the exception that was raised
	 */
	private void reportMomException( SendInteraction request, MomException exception )
	{
		int requestId = request.getInteractionId();
		ICMetadata requestMetadata = fom().getInteractionClass( requestId );
		String service = requestMetadata.getQualifiedName();
		String message = exception.getMessage();
		boolean paramError = exception.isParameterError();
		
		logger.info( "Reporting MOM Exception [Service="+service+",ParamError="+paramError+"]: "+message );
		if( this.supportsMomException )
		{
			HashMap<Integer,byte[]> params = new HashMap<Integer,byte[]>();
			params.put( serviceMetadata.getHandle(), 
			            MomEncodingHelpers.encode(serviceMetadata.getDatatype(), service) );
			params.put( exceptionMetadata.getHandle(), 
			            MomEncodingHelpers.encode(exceptionMetadata.getDatatype(), message) );
			params.put( parameterErrorMetadata.getHandle(), 
			            MomEncodingHelpers.encode(parameterErrorMetadata.getDatatype(), paramError) );
			
			SendInteraction response = new SendInteraction( reportMomExceptionMetadata.getHandle(), 
			                                                null, 
			                                                params );
			response.setIsFromRti( true );
			federation.queueDataMessage( response, null );
		}
		else
		{
			logger.warn( "Did not report MOM Exception as the federation version does not support it" );
		}
	}
	
	/**
	 * Sends the specified response interaction, encoding the provided parameters into wire format.
	 * 
	 * @param responseName the qualified, canonical name of the interaction to send, relative from 
	 *                     <code>HLAinteractionRoot</code>.
	 * @param params the parameter values to send, mapped against their canonical parameter names
	 */
	private void sendResponse( String responseName, Map<String,Object> params )
	{
		ICMetadata responseMetadata = getMomInteractionMetadata( responseName );
		int responseHandle = responseMetadata.getHandle();
		
		// Encode the parameter values into wire format
		HashMap<Integer,byte[]> hlaParams =
		    this.encodeResponseParameters( responseMetadata, params );

		// Create the response object
		SendInteraction response = new SendInteraction( responseHandle, null, hlaParams );
		response.setIsFromRti( true );

		if( logger.isDebugEnabled() )
		{
			logger.debug( "Sent MOM response interaction " +
			              responseMetadata.getQualifiedName() + " with " + hlaParams.size() +
			              " param(s)" );
		}

		// Send the response!
		federation.queueDataMessage( response, null );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Internal exception type. Indicates that an error occurred during the processing of a MOM request
	 * interaction. This exception will be reported back to the federate as a HLAreportMOMexception via
	 * the {@link MomSendInteractionHandler#reportMomException(SendInteraction, MomException)} method.
	 */
	private class MomException extends Exception
	{
		private boolean parameterError;
		
		public MomException( String message, boolean parameterError )
		{
			super( message );
			this.parameterError = parameterError;
		}
		
		public MomException( String message, boolean parameterError, Throwable cause )
		{
			super( message, cause );
			this.parameterError = parameterError;
		}
		
		public boolean isParameterError()
		{
			return this.parameterError;
		}
	}
	
	/**
	 * Consumer extension that allows the consumer method to throw a checked exception.
	 * 
	 * @param <T> the datatype the consumer accepts
	 * @param <E> the checked exception that the consumer may throw
	 */
	@FunctionalInterface
	private interface ConsumerWithException<T,E extends Exception>
	{
		public void accept( T t ) throws E;
	}	
}
