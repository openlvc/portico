/*
 *   Copyright 2022 The Portico Project
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
package org.portico3.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Set of static methods that make it simpler to work with the XML DOM.
 */
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Get the text content for an element in a way that strips out all comments and does not
	 * include the text data for child nodes (as many elements are prone to do).
	 * 
	 * @param node The nodoe to get the text content for
	 * @return Just the important text content of a node
	 */
	public static String getTextContent( Node node )
	{
		StringBuilder builder = new StringBuilder();
		NodeList children = node.getChildNodes();
		for( int i = 0; i < children.getLength(); i++ )
		{
			if( children.item(i).getNodeType() == Node.TEXT_NODE )
			{
				String temp = children.item(i).getTextContent().trim();
				if( temp.equals("") == false )
					builder.append( temp );
			}
		}
		
		return builder.toString();
	}

	/**
	 * Find the first Element under the given parent that has the given tag name. This will only
	 * search in the direct children.
	 * 
	 * @param parent  The parent node whose children we should search.
	 * @param tagName The tag name of the element we want to find.
	 * @return The element with the given tag name that is a child of the parent, or null if
	 *         no such element can be found
	 */
	public static Element getFirstChildWithTag( Node parent, String tagName )
	{
		NodeList nodes = parent.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ )
		{
			Node temp = nodes.item(i);
			if( temp.getNodeType() != Node.ELEMENT_NODE )
				continue;

			if( temp.getNodeName().equals(tagName) )
				return (Element)temp;
		}
		
		// couldn't find it
		return null;
	}
	
	/**
	 * Find the first Element under the given parent that has the given tag name. This will
	 * conduct a breadth-first search through the entire tree below the parent node.
	 * 
	 * @param parent  The parent node whose children we should search
	 * @param tagName The tag name of the element we want to find
	 * @return The element with the given tag name that is a child of the parent, or null if
	 *         no such element can be found
	 */
	public static Element getFirstChildWithTagDeep( Node parent, String tagName )
	{
		NodeList nodes = parent.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ )
		{
			// Is this the one we are looking for?
			Node temp = nodes.item(i);
			if( temp.getNodeType() == Node.ELEMENT_NODE && temp.getNodeName().equals(tagName) )
				return (Element)temp;
			
			// We should look at its children to see if they're what we are looking for
			Element descendant = getFirstChildWithTagDeep( temp, tagName );
			if( descendant != null )
				return descendant;
		}
		
		// couldn't find anything anywhere in the tree
		return null;
	}


	/**
	 * Get a list of all child elements of the given parent that have the given tag name.
	 * This will only search direct children (no deeper). If there are none with the requested
	 * name, an empty list is returned.
	 * 
	 * @param parent  The parent node whose children we should search
	 * @param tagName The tag name we want child elements to match
	 * @returns A list of all child elements whose tag name is the same as the given parameter
	 */
	public static List<Element> getChildrenWithTag( Node parent, String tagName )
	{
		List<Element> matchList = new ArrayList<>();
		NodeList nodes = parent.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ )
		{
			Node temp = nodes.item(i);
			if( temp.getNodeType() == Node.ELEMENT_NODE && temp.getNodeName().equals(tagName) )
				matchList.add( (Element)temp );
		}
		
		return matchList;
	}

	/**
	 * Get a list of all the children that are elements (non-elements are skipped). If the node
	 * has no child elements, an empty list is returned.
	 * 
	 * @param parent The parent node to search
	 * @return A list of all Element node children of the parent
	 */
	public static List<Element> getChildElements( Node parent )
	{
		ArrayList<Element> list = new ArrayList<>();
		NodeList nodes = parent.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ )
		{
			if( nodes.item(i) instanceof Element )
				list.add( (Element)nodes.item(i) );
		}
		
		return list;
	}
	
	/**
	 * Return the string value of the first child sub-element we find with a matching tag name.
	 * This will look at all the children, find the first with the specified tag name and then
	 * get that element's text value and return it. If there is no child found, null is returned.
	 * 
	 * @param parent The parent node to search
	 * @param childTag The tag of the child we want to find
	 * @return The string value of any matching child, or null if there is none.
	 */
	public static String getFirstChildValue( Node parent, String childTag )
	{
		Element matchingChild = getFirstChildWithTag( parent, childTag );
		return matchingChild == null ? null : XmlUtils.getTextContent( matchingChild );
	}

	/**
	 * Get the value of the attribute with the given name on the provided element. If the element
	 * does not have the attribute, return the value provided as the default.
	 * 
	 * @param element The element to inspect
	 * @param attributeName Name of the attribute we want to get the value for
	 * @param defaultValue The default value to use if the attribute isn't present
	 * @return Value of the identified attribute, or the default value if there is no attribute
	 */
	public static String getAttributeOrDefault( Element element,
	                                            String attributeName,
	                                            String defaultValue )
	{
		return element.hasAttribute(attributeName) ? element.getAttribute(attributeName) :
		                                             defaultValue;
	}

	/**
	 * Returns all attributes on the given element in a properties map. If the element does not
	 * have any attributes, an empty properties is returned.
	 * 
	 * @param element The element to extract the attributes from
	 * @return Properties map containing the name/value pairs for each of the attributes
	 */
	public static Properties getAttributes( Element element )
	{
		Properties properties = new Properties();
		NamedNodeMap attributes = element.getAttributes();
		for( int i = 0; i < attributes.getLength(); i++ )
		{
			properties.put( attributes.item(i).getNodeName(),
			                attributes.item(i).getNodeValue() );
		}
		
		return properties;
	}
	
	/**
	 * <p>
	 * Returns a Properties instance with the values of all children of the element. The name of
	 * each property will the "." separated combination of the parents element tag names. For
	 * example; if you passed in an element whose tagname was "parent", and it has a child whose
	 * tagname was "child", with the value "value", the properties would contain the property:
	 * <code>parent.child = value</code>.
	 * </p>
	 * 
	 * <p>
	 * Attributes on elements are treated as child nodes whose tagname is the attribute name.
	 * For example, if the element has the tagname "parent" and an attribute called "attribute" 
	 * with the value "value", the returned properties would contain the property:
	 * <code>parent.attribute = value</code>.
	 * </p>
	 * 
	 * <p>
	 * The tagname of the element passed in is always used, so all properties will be prefixed with
	 * its tag. Also note that if there are two enteries that share a property name, only the first
	 * will be retained.
	 * 
	 * @param element The element to turn into some properties
	 * @return The properties that we turned the element into
	 */
	public static Properties getChildrenAsProperties( Element element )
	{
		return getChildrenAsProperties( new Properties(), null, element );
	}

	/**
	 * Internal Use Method.
	 * 
	 * Recursive version of above. Extract full details of all children into a property map.
	 *  
	 * @param properties The properties set to add to
	 * @param prefix The prefix that all properties should have before being added
	 * @param parent The node to be processed
	 * @return
	 */
	private static Properties getChildrenAsProperties( Properties properties,
	                                                   String prefix, 
	                                                   Element parent )
	{
		// Set up a new prefix with the tagname of this element appended
		String newPrefix = prefix == null ? parent.getTagName() : prefix+"."+parent.getTagName();
		
		// Get the TEXT CONTENT of the element
		String textContent = getTextContent( parent );
		if( textContent != null && textContent.trim().equals("") == false )
			properties.put( newPrefix, textContent.trim() );
		
		// Get any ATTRIBUTES of the element
		Properties attributes = XmlUtils.getAttributes( parent );
		attributes.forEach( (key,value) -> properties.put( newPrefix+"."+key,value) );
		
		// Recurse so that CHILDREN also get processed
		for( Element child : getChildElements(parent) )
			getChildrenAsProperties( properties, newPrefix, child );
		
		return properties;
	}

	
	
	
}
