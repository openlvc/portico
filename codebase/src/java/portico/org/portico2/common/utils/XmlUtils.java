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
package org.portico2.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.portico.lrc.compat.JConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Parse the given file into an XML Document and return. Throws an exception if the 
	 * file cannot be found or there is an exception parsing it (such as format or other
	 * parser exceptions).
	 * 
	 * @param file The file to parse
	 * @return A complete XML Document object
	 * @throws JConfigurationException If the file doesn't exist or can't be parsed
	 */
	public static Document parseXmlFile( File file ) throws JConfigurationException
	{
		if( file.exists() == false )
			throw new JConfigurationException( "File does not exist: "+file.getAbsolutePath() );

		// Parse the XML file into something we can work with
		Document xml = null;
		try
		{
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder builder = factory.newDocumentBuilder();
    		return builder.parse( file );
		}
		catch( SAXException saxe )
		{
			throw new JConfigurationException( "XML format error: "+saxe.getMessage(), saxe );
		}
		catch( ParserConfigurationException | IOException e )
		{
			throw new JConfigurationException( "Error parsing file: "+e.getMessage(), e );
		}
	}

	/**
	 * Return the first found direct child element of the specified parent with the given name.
	 * 
	 * @param parent    The parent element to look under. Will only look at direct children.
	 * @param childName The name of the child element to return
	 * @return          The first child element with the given name
	 * @throws JConfigurationException If there is no direct child with the given name
	 */
	public static Element getChild( Element parent, String childName ) throws JConfigurationException
	{
		return getChild( parent, childName, true );
	}
	

	/**
	 * Return the first found direct child element of the specified parent with the given name.
	 * Can optionally throw an exception if the element is not found depending on the value of
	 * "required". If true, an exception will be thrown if the child is not found. If the value
	 * if false, null is returned instead. 
	 * 
	 * @param parent    The parent element to look under. Will only look at direct children.
	 * @param childName The name of the child element to return
	 * @param required  Is the param required? If not, return null if not found (otherwise; exception)
	 * @return          The first child element with the given name
	 * @throws JConfigurationException If there is no direct child with the given name, but only
	 *                                 if the exception is not optional
	 */
	public static Element getChild( Element parent, String childName, boolean required )
		throws JConfigurationException
	{
		NodeList list = parent.getChildNodes();
		for( int i = 0; i < list.getLength(); i++ )
		{
			Node node = list.item(i);
			if( node.getNodeType() != Node.ELEMENT_NODE )
				continue;
			
			Element element = (Element)node;
			if( element.getTagName().equals(childName) )
				return element;
		}
		
		if( required == false )
			return null;

		throw new JConfigurationException( "Could not find element <%s> (underneath <%s>)",
		                                   childName, parent.getTagName() );
	}
	
	/**
	 * Return a list of all direct child elements that have the given name. This is for direct
	 * children only, there will be no recursion down. If there are nont with the given name,
	 * an empty list is returned.
	 *  
	 * @param parent    The parent element to look under. Will only look at direct children.
	 * @param childName The name of the child element to return
	 * @return          List of all elements of the given tag name under the parent
	 */
	public static List<Element> getChildren( Element parent, String childName )
	{
		List<Element> list = new ArrayList<>();
		NodeList nodes = parent.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ )
		{
			Node node = nodes.item(i);
			if( node.getNodeType() != Node.ELEMENT_NODE )
				continue;
			
			Element element = (Element)node;
			if( element.getTagName().equals(childName) )
				list.add( element );
		}

		return list;
	}

	/**
	 * Returns a list of all direct child ELEMENTS of the given parent. This will not recuse and
	 * will not return items that are not XML elements. If there are none, an empty list will be
	 * returned.
	 * 
	 * @param parent The element to get all the child elements for
	 * @return A list of all the child elements (empty if there are nont).
	 */
	public static List<Element> getChildren( Element parent )
	{
		List<Element> list = new ArrayList<>();
		NodeList nodes = parent.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ )
		{
			Node node = nodes.item(i);
			if( node.getNodeType() == Node.ELEMENT_NODE )
				list.add( (Element)node );
		}

		return list;
	}
	
}
