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
package org.portico.impl.hla1516.fomparser;

import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JCouldNotOpenFED;
import org.portico.lrc.compat.JErrorReadingFED;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.Order;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.model.Transport;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.lrc.model.datatype.linker.DatatypePlaceholder;
import org.portico.utils.fom.FedHelpers;

import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.ErrorReadingFDD;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the main class for the IEEE-1516 XML style FOM parser.
 */
public class FOM
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final ParserWrapper PARSER = new ParserWrapper();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ObjectModel fom;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public FOM()
	{
		this.fom = new ObjectModel( HLAVersion.IEEE1516 );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * The main FOM parser processing method. This takes the root element of a FOM document and
	 * parses it into an {@link ObjectModel} that is returned. If the parser is malformatted in
	 * any way, a {@link JConfigurationException} is thrown.
	 * <p/>
	 * <b>Note:</b> Datatypes, Attributes and Parameters in the returned FOM will contain 
	 * placeholder symbols as the datatypes that they refer to may be declared in another FOM 
	 * module. Once all modules have been merged into a combined FOM, call 
	 * {@link ObjectModel#resolveSymbols(ObjectModel)} to resolve all placeholder datatypes to their
	 * concrete representation
	 */
	public ObjectModel process( Element element ) throws JConfigurationException
	{
		Element datatypesElement = null;
		Element objectsElement = null;
		Element interactionsElement = null;
		
		for( Element temp : FedHelpers.getChildElements(element) )
		{
			String tagName = temp.getTagName();
			if( tagName.equals("dataTypes") )
				datatypesElement = temp;
			else if( tagName.equals("objects") )
				objectsElement = temp;
			else if( tagName.equals("interactions") )
				interactionsElement = temp;
			else
				continue;
		}

		if( datatypesElement != null )
			this.processDatatypes( datatypesElement );
		
		////////////////////////////////
		// process the object classes //
		////////////////////////////////
		// make sure we have some objects
		if( objectsElement != null )
		{
			// process the objects
			OCMetadata objectRoot = this.processObjects( objectsElement );
			this.fom.setObjectRoot( objectRoot );
		}
		else
		{
			throw new JConfigurationException( "<objectModel> missing <objects> child element" );
		}
		
		/////////////////////////////////////
		// process the interaction classes //
		/////////////////////////////////////
		// make sure we have some interactions
		if( interactionsElement != null )
		{
			// process the objects
			ICMetadata interactionRoot = this.processInteractions( interactionsElement );
			this.fom.setInteractionRoot( interactionRoot );
		}
		else
		{
			throw new JConfigurationException("<objectModel> missing <interactions> child element");
		}

		// Mommify the fom
		ObjectModel.mommify( this.fom );
		
		// return the completed FOM
		return this.fom;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Datatype Methods /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void processDatatypes( Element datatypesElement ) throws JConfigurationException
	{
		Set<IDatatype> fedTypes = FedHelpers.extractDatatypes( datatypesElement, 
		                                                       fom.getHlaVersion() );
		
		// Link and add types
		for( IDatatype fedType : fedTypes )
		{
			// We used to link datatypes here on the assumption that FOM modules were 
			// self-contained. However it appears that is not the reality, and that modules can
			// reference datatypes that are only declared in other modules.
			//
			// As such we datatypes regardless of whether they contain placeholder symbols, and
			// resolve them once all modules have been merged
			fom.addDatatype( fedType );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Object Class Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private OCMetadata processObjects( Element element ) throws JConfigurationException
	{
		// find the first objectClass
		NodeList children = element.getChildNodes();
		for( int i = 0; i < children.getLength(); i++ )
		{
			Node node = children.item( i );
			if( node.getNodeType() == Node.ELEMENT_NODE )
			{
				Element temp = (Element)node;
				if( "HLAobjectRoot".equals(temp.getAttribute("name")) )
				{
					return processOC( temp, null );
				}
			}
		}
		
		// if we get here, we didn't find any object classes
		throw new JConfigurationException( "<objects> is missing HLAobjectRoot" );
	}
	
	private OCMetadata processOC( Element element, OCMetadata parent ) throws JConfigurationException
	{
		// get the name of the class //
		String name = element.getAttribute( "name" );
		if( name == null )
		{
			throw new JConfigurationException( "<objectClass> missing \"name\" attribute" );
		}
		
		//////////////////////
		// create the class //
		//////////////////////
		OCMetadata newClass = fom.newObject( name );
		// if this is NOT the object root, set the parent //
		if( "HLAobjectRoot".equals(name) == false )
		{
			newClass.setParent( parent );
		}
		this.fom.addObjectClass( newClass );
		
		//////////////////////////////////////
		// process any child object classes //
		//////////////////////////////////////
		NodeList children = element.getChildNodes();
		for( int i = 0; i < children.getLength(); i++ )
		{
			Node node = children.item( i );
			if( node.getNodeType() != Node.ELEMENT_NODE )
			{
				// not an element, skip it
				continue;
			}
			
			Element temp = (Element)node;
			if( temp.getTagName().equals("objectClass") == false )
			{
				// not an attribute
				continue;
			}
			
			// it's an object, process it
			processOC( temp, newClass );
		}

		////////////////////////////////////////
		// find and attach all the attributes //
		////////////////////////////////////////
		this.attachAttributes( element, newClass );

		return newClass;
	}

	private void attachAttributes( Element oClass, OCMetadata parent ) throws JConfigurationException
	{
		NodeList children = oClass.getChildNodes();
		for( int i = 0; i < children.getLength(); i++ )
		{
			Node node = children.item( i );
			if( node.getNodeType() != Node.ELEMENT_NODE )
			{
				// not an element, skip it
				continue;
			}
			
			Element temp = (Element)node;
			if( temp.getTagName().equals("attribute") == false )
			{
				// not an attribute
				continue;
			}
			
			////////////////////////////////////
			// found an attribute, process it //
			////////////////////////////////////
			// do we have the required attributes?
			if( temp.hasAttribute("name") == false || 
				temp.hasAttribute("dataType") == false ||
				temp.hasAttribute("transportation") == false ||
				temp.hasAttribute("order") == false )
			{
				throw new JConfigurationException( "attribute in class [" + 
				    parent.getQualifiedName() + "] missing name, dataType, transportation or order" );
			}
			
			// create the attribute
			String sName = temp.getAttribute( "name" );
			String sDatatype = temp.getAttribute( "dataType" );
			
			// All attribute datatypes are initially created as placeholders, and resolved once
			// all FOM modules have been merged and the standard MIM has been inserted
			IDatatype datatype = new DatatypePlaceholder( sDatatype );
			
			ACMetadata attribute = this.fom.newAttribute( sName, datatype );
			
			// get the transport
			String sTransport = temp.getAttribute( "transportation" );
			if( sTransport.equals("HLAreliable") )
			{
				attribute.setTransport( Transport.RELIABLE );
			}
			else if( sTransport.equals("HLAbestEffort") )
			{
				attribute.setTransport( Transport.BEST_EFFORT );
			}
			else
			{
				throw new JConfigurationException( "Unknown transport [" + sTransport +
				    "] for attribute " + sName + " in object class " + parent.getQualifiedName() );
			}
			
			// get the order
			String sOrder = temp.getAttribute( "order" );
			if( sOrder.equals("TimeStamp") )
			{
				attribute.setOrder( Order.TIMESTAMP );
			}
			else if( sOrder.equals("Receive") )
			{
				attribute.setOrder( Order.RECEIVE );
			}
			else
			{
				throw new JConfigurationException( "Unknown order [" + sOrder + "] for attribute " +
				    sName + " in object class " + parent.getQualifiedName() );
			}
			
			// bind it to the parent
			parent.addAttribute( attribute );
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Interaction Class Methods ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private ICMetadata processInteractions( Element element ) throws JConfigurationException
	{
		// find the first interactionClass
		NodeList children = element.getChildNodes();
		for( int i = 0; i < children.getLength(); i++ )
		{
			Node node = children.item( i );
			if( node.getNodeType() == Node.ELEMENT_NODE )
			{
				Element temp = (Element)node;
				if( "HLAinteractionRoot".equals(temp.getAttribute("name")) )
				{
					return processIC( temp, null );
				}
			}
		}
		
		// if we get here, we didn't find any interaction classes
		throw new JConfigurationException( "<interactions> is missing HLAinteractionRoot" );
	}
	
	private ICMetadata processIC( Element element, ICMetadata parent ) throws JConfigurationException
	{
		// get the name of the class //
		String name = element.getAttribute( "name" );
		if( name == null )
		{
			throw new JConfigurationException( "<interactionClass> missing \"name\" attribute" );
		}
	
		//////////////////////
		// create the class //
		//////////////////////
		ICMetadata newClass = fom.newInteraction( name );
		newClass.setParent( parent );
		this.fom.addInteractionClass( newClass );

		///////////////////////////////////////////
		// process any child interaction classes //
		///////////////////////////////////////////
		NodeList children = element.getChildNodes();
		for( int i = 0; i < children.getLength(); i++ )
		{
			Node node = children.item( i );
			if( node.getNodeType() != Node.ELEMENT_NODE )
			{
				// not an element, skip it
				continue;
			}
			
			Element temp = (Element)node;
			if( temp.getTagName().equals("interactionClass") == false )
			{
				// not an attribute
				continue;
			}
			
			// it's an object, process it
			processIC( temp, newClass );
		}

		////////////////////////////////////////
		// find and attach all the attributes //
		////////////////////////////////////////
		// this will also do order and transport
		this.attachParameters( element, newClass );

		return newClass;
	}
	
	private void attachParameters( Element iClass, ICMetadata parent ) throws JConfigurationException
	{
		/////////////////////////////////////////
		// fetch the transport and order first //
		/////////////////////////////////////////
		// do we have the required attributes?
		if( iClass.hasAttribute("transportation") == false ||
			iClass.hasAttribute("order") == false )
		{
			throw new JConfigurationException( "interaction class [" + parent.getQualifiedName() +
			                                   "] missing transportation or order" );
		}
		
		// get the transport
		String sTransport = iClass.getAttribute( "transportation" );
		if( sTransport.equals("HLAreliable") )
		{
			parent.setTransport( Transport.RELIABLE );
		}
		else if( sTransport.equals("HLAbestEffort") )
		{
			parent.setTransport( Transport.BEST_EFFORT );
		}
		else
		{
			throw new JConfigurationException( "Unknown transport [" + sTransport +
			    "] for interaction class " + parent.getQualifiedName() );
		}
		
		// get the order
		String sOrder = iClass.getAttribute( "order" );
		if( sOrder.equals("TimeStamp") )
		{
			parent.setOrder( Order.TIMESTAMP );
		}
		else if( sOrder.equals("Receive") )
		{
			parent.setOrder( Order.RECEIVE );
		}
		else
		{
			throw new JConfigurationException( "Unknown order ["+sOrder+"] for interaction class " +
			                                   parent.getQualifiedName() );
		}

		////////////////////////////
		// process for parameters //
		////////////////////////////
		NodeList children = iClass.getChildNodes();
		for( int i = 0; i < children.getLength(); i++ )
		{
			Node node = children.item( i );
			if( node.getNodeType() != Node.ELEMENT_NODE )
			{
				// not an element, skip it
				continue;
			}
			
			Element temp = (Element)node;
			if( temp.getTagName().equals("parameter") == false )
			{
				// not an attribute
				continue;
			}
			
			////////////////////////////////////
			// found an attribute, process it //
			////////////////////////////////////
			// do we have the required attributes?
			if( temp.hasAttribute("name") == false || 
				temp.hasAttribute("dataType") == false ) 
			{
				throw new JConfigurationException( "parameter in class [" + 
				    parent.getQualifiedName() + "] missing name or dataType" );
			}
			
			// create the attribute
			String sName = temp.getAttribute( "name" );
			String sDatatype = temp.getAttribute( "dataType" );
			
			// All parameter datatypes are initially created as placeholders, and resolved once
			// all FOM modules have been merged and the standard MIM has been inserted
			IDatatype datatype = new DatatypePlaceholder( sDatatype );
			
			PCMetadata parameter = this.fom.newParameter( sName, datatype );
			// bind it to the parent
			parent.addParameter( parameter );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This method will take the XML FOM at the given URL and attempt to process it. If there
	 * is a problem locating the URL or fetching the stream with which to read it, a
	 * {@link CouldNotOpenFDD} exception will be thrown. If there is a problem reading the FOM,
	 * or its structure is not correct, an {@link ErrorReadingFDD} exception will be thrown.
	 * <p/>
	 * If the FOM is correct and can be processed successfully, it will be turned into an
	 * {@link ObjectModel} and returned.
	 * 
	 * @param fed The URL location of the fed file (could be local or remote)
	 * @return An {@link ObjectModel} representing the FOM from the given location
	 * @throws CouldNotOpenFDD If the fed file can not be located or there is an error opening it
	 * @throws ErrorReadingFDD If the fed file is invalid or there is a problem reading it from the
	 * stream.
	 */
	public static ObjectModel parseFOM( URL fed ) throws JCouldNotOpenFED, JErrorReadingFED
	{
		////////////////////////////////////////
		// parse the fed file into a DOM-tree //
		////////////////////////////////////////
		if( fed == null )
			throw new JCouldNotOpenFED( "Can't locate fed file: " + fed );

		// try to open a stream to the given URL
		Element rootElement = null;
		try
		{
			// open the file
			InputStream stream = fed.openStream();
			// parse the thing in
			Document document = PARSER.parse( stream );
			document.normalize();
			rootElement = document.getDocumentElement();
			// close off the stream
			stream.close();
		}
		catch( IOException ioex )
		{
			throw new JCouldNotOpenFED( "Error opening fed file: "+ioex.getMessage(), ioex );
		}
		catch( Exception e )
		{
			throw new JErrorReadingFED( "Error reading fed file: " + e.getMessage(), e );
		}
		
		/////////////////////
		// process the FOM //
		/////////////////////
		try
		{
			// create the parser
			FOM parser = new FOM();
			// we'll call the node handler directly because we want to process the root node
			ObjectModel model = parser.process( rootElement );
			model.setFileName( fed.toString() );
			return model;
		}
		catch( JConfigurationException ce )
		{
			throw new JErrorReadingFED( "Error reading ["+fed+"]: "+ce.getMessage() );
		}
	}
	
}
