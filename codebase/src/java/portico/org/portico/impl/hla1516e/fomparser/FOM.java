/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.hla1516e.fomparser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JCouldNotOpenFED;
import org.portico.lrc.compat.JErrorReadingFED;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.Order;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.model.Transport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The FOM paser for 1516e style federation description documents.
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
		this.fom = new ObjectModel( HLAVersion.IEEE1516e );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * The main FOM parser processing method. This takes the root element of a FOM document and
	 * parses it into an {@link ObjectModel} that is returned. If the parser is malformatted in
	 * any way, a {@link JConfigurationException} is thrown.
	 */
	public ObjectModel process( Element element ) throws JErrorReadingFED
	{
		// locate the major elements we are interested in
		Element objectsElement = null;
		Element interactionsElement = null;
		Element dimensionsElement = null;
		for( Element temp : getChildElements(element) )
		{
			if( temp.getTagName().equals("objects") )
				objectsElement = temp;
			else if( temp.getTagName().equals("interactions") )
				interactionsElement = temp;
			else if( temp.getTagName().equals("dimensions") )
				dimensionsElement = temp;
			else
				continue; // ignore
		}
		
		// extract all the object classes
		OCMetadata objectRoot = null;
		if( objectsElement != null )
		{
			objectRoot = this.extractObjects( objectsElement );
		}
		else
		{
			objectRoot = this.fom.newObject( "HLAobjectRoot" );
			this.fom.addObjectClass( objectRoot );
		}
		this.fom.setObjectRoot( objectRoot);
		
		// extract all the interaction classes
		ICMetadata interactionRoot = null;
		if( interactionsElement != null )
		{
			interactionRoot = this.extractInteractions( interactionsElement );
		}
		else
		{
			interactionRoot = this.fom.newInteraction( "HLAinteractionRoot" );
			this.fom.addInteractionClass( interactionRoot );
		}
		this.fom.setInteractionRoot( interactionRoot );
		
		if( dimensionsElement != null )
		{
			
		}

		// return the completed FOM
		return this.fom;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Object Class Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loop through all the child objects and generate the metadata hierarchy to represent them.
	 * <p/>
	 * The general format of the object element is as follows:
	 * <pre>
     * <objects>
     *    <objectClass>
     *       <name>HLAobjectRoot</name>
     *       <attribute>
     *          <name>name</name>
     *          <transportation>HLAreliable|HLAbestEffort</transportation>
     *          <order>TimeStamp|Receive</order>
     *       </attribute>
     *       ...
     *       <objectClass>
     *          <name>HLAmanager</name>
     *          <attribute/>...
     *       </objectClass>
     *    </objectClass>
     * </objects>
	 * </pre>
	 */
	private OCMetadata extractObjects( Element objectsElement ) throws JErrorReadingFED
	{
		Element objectRootElement = getFirstChildElement( objectsElement, "objectClass" );
		if( objectRootElement == null )
		{
			// no objects to process... OK, must be an extension module
			return null;
		}
		
		// validate that we have an object root
		String name = getChildValue( objectRootElement, "name" );
		if( name.equals("HLAobjectRoot") == false )
		{
			throw new JErrorReadingFED( "First <objectClass> must be HLAobjectRoot, found: "+name );
		}
		
		OCMetadata objectRoot = fom.newObject( "HLAobjectRoot" );
		extractAttributes( objectRoot, objectRootElement );
		fom.addObjectClass( objectRoot );

		// recurse and find all our children
		extractObjects( objectRoot, objectRootElement );
		return objectRoot;
	}

	private void extractObjects( OCMetadata parent, Element parentElement ) throws JErrorReadingFED
	{
		List<Element> children = getAllChildElements( parentElement, "objectClass" );
		for( Element current : children )
		{
			String objectClassName = getChildValue( current, "name" );
			OCMetadata objectClass = fom.newObject( objectClassName );
			extractAttributes( objectClass, current );
			// link us to our parent
			objectClass.setParent( parent );
			fom.addObjectClass( objectClass );
			// recurse and find all our children
			extractObjects( objectClass, current );
		}
	}
	
	/**
	 * This method will extract all the relevant object class attributes (not XML attributes)
	 * from the given "objectClass" element. For each attribute, an {@link ACMetadata} will be
	 * created and stored inside the provided {@link OCMetadata}.
	 */
	private void extractAttributes( OCMetadata clazz, Element element ) throws JErrorReadingFED
	{
		List<Element> attributes = getAllChildElements( element, "attribute" );
		for( Element attributeElement : attributes )
		{
			String attributeName = getChildValue( attributeElement, "name" );
			ACMetadata attribute = fom.newAttribute( attributeName );

			// Order and Transport
			String attributeOrder = getChildValueForgiving( attributeElement, "order", attributeName );
			if( attributeOrder != null )
				attribute.setOrder( Order.fromFomString(attributeOrder) );

			String attributeTransport = getChildValueForgiving( attributeElement, "transportation", attributeName );
			if( attributeTransport != null )
				attribute.setTransport( Transport.fromFomString(attributeTransport) );

			// add the attribute to the containing class
			clazz.addAttribute( attribute );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Interaction Class Methods ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loop through all the child objects and generate the metadata hierarchy to represent them.
	 * <p/>
	 * The general format of the object element is as follows:
	 * <pre>
     * <interactions>
     *    <interactionClass>
     *       <name>HLAinteractionRoot</name>
     *       <transportation>HLAreliable|HLAbestEffort</transportation>
     *       <order>TimeStamp|Receive</order>
     *       <parameter>
     *          <name>name</name>
     *       </parameter>
     *       ...
     *       <interactionClass/>...
     *    </interactionClass>
     * </interactions>
	 * </pre>
	 */
	private ICMetadata extractInteractions( Element element ) throws JErrorReadingFED
	{
		Element interactionRootElement = getFirstChildElement( element, "interactionClass" );
		if( interactionRootElement == null )
		{
			// no interactions to process... OK, must be an extension module
			return null;
		}

		// validate that we have an interaction root
		String name = getChildValue( interactionRootElement, "name" );
		if( name.equals("HLAinteractionRoot") == false )
		{
			throw new JErrorReadingFED( "First <interactionClass> must be "+
			                            "HLAinteractionRoot, found: "+name );
		}
		
		// generate some basic information for the Interaction root
		ICMetadata interactionRoot = fom.newInteraction("HLAinteractionRoot" );

		// get the transport and order
		String interactionOrder = getChildValueForgiving( interactionRootElement, "order", name );
		if( interactionOrder != null )
			interactionRoot.setOrder( Order.fromFomString(interactionOrder) );

		String interactionTransport = getChildValueForgiving( interactionRootElement, "transportation", name );
		if( interactionTransport != null )
			interactionRoot.setTransport( Transport.fromFomString(interactionTransport) );

		// get the parameters
		extractParameters( interactionRoot, interactionRootElement );
		fom.addInteractionClass( interactionRoot );

		// recurse and find all our children
		extractInteractions( interactionRoot, interactionRootElement );
		return interactionRoot;
	}

	private void extractInteractions( ICMetadata parent, Element parentElement )
		throws JErrorReadingFED
	{
		List<Element> children = getAllChildElements( parentElement, "interactionClass" );
		for( Element current : children )
		{
			// create the metadata type
			String interactionClassName = getChildValue( current, "name" );
			ICMetadata interactionClass = fom.newInteraction( interactionClassName );

			// get the transport and order
			String interactionOrder = getChildValueForgiving( current, "order", interactionClassName );
			if( interactionOrder != null )
				interactionClass.setOrder( Order.fromFomString(interactionOrder) );

			String interactionTransport = getChildValueForgiving( current, "transportation", interactionClassName );
			if( interactionTransport != null )
				interactionClass.setTransport( Transport.fromFomString(interactionTransport) );

			// get all the interaction parameters
			extractParameters( interactionClass, current );
			
			// link us to our parent
			interactionClass.setParent( parent );
			fom.addInteractionClass( interactionClass );
			
			// recurse and find all our children
			extractInteractions( interactionClass, current );
		}
	}
	
	/**
	 * This method will extract all the relevant interaction class parameters from the given
	 * "interactionClass" element. For each parameter, a {@link PCMetadata} will be created
	 * and stored inside the provided {@link ICMetadata}.
	 */
	private void extractParameters( ICMetadata clazz, Element element ) throws JErrorReadingFED
	{
		List<Element> parameters = getAllChildElements( element, "parameter" );
		for( Element parameterElement : parameters )
		{
			String parameterName = getChildValue( parameterElement, "name" );
			PCMetadata parameter = fom.newParameter( parameterName );
			clazz.addParameter( parameter );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Private Helper Methods //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Return a list of child element types of the provided node. If the node has no children
	 * or none of the children are element nodes, an empty list is returned.
	 */
	private List<Element> getChildElements( Node node )
	{
		NodeList list = node.getChildNodes();
		ArrayList<Element> elements = new ArrayList<Element>();
		for( int i = 0; i < list.getLength(); i++ )
		{
			Node temp = list.item(i);
			if( temp.getNodeType() == Node.ELEMENT_NODE )
				elements.add( (Element)temp );
		}
		
		return elements;
	}

	/**
	 * Return the first child element under node that has given given tag name.
	 * If there is none that matches, return null.
	 */
	private Element getFirstChildElement( Node node, String tag )
	{
		NodeList list = node.getChildNodes();
		for( int i = 0; i < list.getLength(); i++ )
		{
			Node temp = list.item(i);
			if( temp.getNodeType() != Node.ELEMENT_NODE )
				continue;
			
			Element tempElement = (Element)temp;
			if( tempElement.getTagName().equals(tag) )
				return tempElement;
		}
		
		return null;
	}

	/**
	 * Return a list of all the children elements of node with the given tag name.
	 * If there are no matching elements, an empty list is returned.
	 */
	private List<Element> getAllChildElements( Node node, String tag )
	{
		NodeList list = node.getChildNodes();
		ArrayList<Element> elements = new ArrayList<Element>();
		for( int i = 0; i < list.getLength(); i++ )
		{
			Node temp = list.item(i);
			if( temp.getNodeType() != Node.ELEMENT_NODE )
				continue;
			
			Element tempElement = (Element)temp;
			if( tempElement.getTagName().equals(tag) )
				elements.add( tempElement );
		}
		
		return elements;
	}

	private boolean isElement( Node node )
	{
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	/**
	 * This method searches the given element for the first child element with the identified
	 * tag-name. When it finds it, its text content is returned. If there is no child element
	 * with the given tag-name, an exception is thrown.
	 */
	private String getChildValue( Element element, String name ) throws JErrorReadingFED
	{
		return getChildValue( element, name, null );
	}

	/**
	 * To provide some better context to errors, this method takes an additional `typeName`
	 * parameter that will be used in any exception reporting. This allows pretty generic,
	 * difficult to find errors like "Element <interactionClass> missing child <order>" to
	 * become a bit more descriptive by including the name value of the interaction class
	 * (in this example).
	 * 
	 * @param element  Element to look for the child in
	 * @param name     Name of the child to look for and return its value
	 * @param typeName Name of the type we are looking in if known. `null` will cause the
	 *                 name to not be printed and is still valid.
	 * @return The value of the named sub-element inside the given element.
	 * @throws JErrorReadingFed if the value cannot be found
	 */
	private String getChildValue( Element element, String name, String typeName )
		throws JErrorReadingFED
	{
		if( typeName == null )
			typeName = "unknown";

		// check for the value of an attribute first
		if( element.hasAttribute(name) )
			return element.getAttribute( name );
		
		// if no attribute is present, look for a child element
		Element child = getFirstChildElement( element, name );
		if( child == null )
		{
			String message = String.format( "Element <%s name=\"%s\"> missing child <%s> element",
			                                element.getTagName(),
			                                typeName,
			                                name );
			
			throw new JErrorReadingFED( message );
		}
		else
		{
			return child.getTextContent().trim();
		}
	}

	/**
	 * Same as {@link #getChildValue(Element, String)} except that it returns null if there is
	 * no child rather than throwing an exception.
	 * 
	 * @see {@link #getChildValue(Element, String, String)}
	 */
	private String getChildValueForgiving( Element element, String name, String typeName )
	{
		try
		{
			return getChildValue( element, name, typeName );
		}
		catch( JErrorReadingFED error )
		{
			// let's just ignore this
			return null;
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This method will take the given 1516e XML FOM and attempt to process it.
	 * If there is a problem locating the URL or fetching the stream with which to read it, a
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
		catch( NullPointerException npe )
		{
			throw new JErrorReadingFED( "Problem while reading fed file", npe );
		}
	}
}
