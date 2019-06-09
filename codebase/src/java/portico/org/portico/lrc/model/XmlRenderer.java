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
package org.portico.lrc.model; 

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List; 
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.portico.lrc.model.datatype.Alternative;
import org.portico.lrc.model.datatype.ArrayType;
import org.portico.lrc.model.datatype.BasicType;
import org.portico.lrc.model.datatype.DatatypeHelpers;
import org.portico.lrc.model.datatype.EnumeratedType;
import org.portico.lrc.model.datatype.Enumerator;
import org.portico.lrc.model.datatype.Field;
import org.portico.lrc.model.datatype.FixedRecordType;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.lrc.model.datatype.IEnumerator;
import org.portico.lrc.model.datatype.SimpleType;
import org.portico.lrc.model.datatype.VariantRecordType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class will take an {@link ObjectModel} and render it as an XML document.
 * <p/>
 * Kindly donated by EMostafaAli!
 */
public class XmlRenderer
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
	/**
	 * Takes the given {@link ObjectModel} and converts it into an XML Document.
	 */
	public Document renderFOM( ObjectModel model )  
	{		
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
			Document doc = dbBuilder.newDocument();
			Element mainRootElement =
			    doc.createElementNS( "http://standards.ieee.org/IEEE1516-2010", "objectModel" );
			doc.appendChild( mainRootElement );
			mainRootElement.appendChild( doc.createElement( "modelIdentification" ) );
			Node objectsNode = mainRootElement.appendChild( doc.createElement("objects") );
			renderObject( model.getObjectRoot(), doc, objectsNode );
		
			Node intNode = mainRootElement.appendChild( doc.createElement("interactions") );
			renderInteraction( model.getInteractionRoot(), doc, intNode );

			Node dataNode = mainRootElement.appendChild( doc.createElement("dataTypes") );
			renderDataType( model.getDatatypes(), doc, dataNode );

			Node switchesNode = mainRootElement.appendChild( doc.createElement("switches") );
			renderSwitches( doc, switchesNode );
		
			mainRootElement.appendChild( doc.createElement("dimensions") );
			mainRootElement.appendChild( doc.createElement("synchronizations") );
			mainRootElement.appendChild( doc.createElement("transportations") );
			mainRootElement.appendChild( doc.createElement("updateRates") );
			mainRootElement.appendChild( doc.createElement("notes") );
		
			return doc;
	}
		catch( ParserConfigurationException pce )
	{
			throw new IllegalStateException( pce );
		}
	}

	private void renderObject( OCMetadata oclass, Document doc, Node parentNode )
	{
		Node node = parentNode.appendChild( doc.createElement( "objectClass" ) );
		addXMLAttribute( "name", oclass.getLocalName(), doc, node );
		addXMLAttribute( "sharing", "PublishSubscribe", doc, node ); // TODO: use value from ObjectModel when available
		addXMLAttribute( "semantics", "NA", doc, node ); // TODO: use value from ObjectModel when available
		for( ACMetadata attribute : oclass.getDeclaredAttributes() )
		{
			Node attNode = node.appendChild( doc.createElement( "attribute" ) );
			addXMLAttribute( "name", attribute.getName(), doc, attNode );
			Node dataTypeNode = attNode.appendChild( doc.createElement( "dataType" ) );
			dataTypeNode.appendChild( doc.createTextNode( attribute.getDatatype().getName() ) );
			String orderType = "TimeStamp";
			if( attribute.isRO() )
				orderType = "Receive";

			addXMLAttribute( "order", orderType, doc, attNode );
			String transportationType = "HLAreliable";
			if( attribute.getTransport() == Transport.BEST_EFFORT )
				transportationType = "HLAbestEffort";
 
			addXMLAttribute( "transportation", transportationType, doc, attNode );
			addXMLAttribute( "updateType", "Conditional", doc, attNode ); // TODO: use value from ObjectModel when available
			addXMLAttribute( "ownership", "DivestAcquire", doc, attNode ); // TODO: use value from ObjectModel when available
			addXMLAttribute( "sharing", "Neither", doc, attNode ); // TODO: use value from ObjectModel when available
	}
	
		for( OCMetadata subclass : oclass.getChildTypes() )
			renderObject( subclass, doc, node );
	}
	
	private void renderInteraction( ICMetadata iclass, Document doc, Node parentNode )
	{
		Node node = parentNode.appendChild( doc.createElement( "interactionClass" ) );
		addXMLAttribute( "name", iclass.getLocalName(), doc, node );
		addXMLAttribute( "sharing", "PublishSubscribe", doc, node ); // TODO: use value from ObjectModel when available
		String orderType = "TimeStamp";
		if( iclass.isRO() )
			orderType = "Receive";
		
		addXMLAttribute( "order", orderType, doc, node );
		String transportationType = "HLAreliable";
		if( iclass.getTransport() == Transport.BEST_EFFORT )
			transportationType = "HLAbestEffort";
		
		addXMLAttribute( "transportation", transportationType, doc, node );

		for( PCMetadata parameter : iclass.getDeclaredParameters() )
		{
			Node parNode = node.appendChild( doc.createElement( "Parameter" ) );
			addXMLAttribute( "name", parameter.getName(), doc, parNode );
			addXMLAttribute( "dataType", parameter.getDatatype().getName(), doc, parNode );
		}
			
		for( ICMetadata subinteraction : iclass.getChildTypes() )
			renderInteraction( subinteraction, doc, node );
	}
			 
	private void renderDataType( Set<IDatatype> dataTypes, Document doc, Node dataNode )
	{
		Node basicType = dataNode.appendChild( doc.createElement("basicDataRepresentations") );
		Node simpleType = dataNode.appendChild( doc.createElement("simpleDataTypes") );
		Node enumType = dataNode.appendChild( doc.createElement("enumeratedDataTypes") );
		Node arrayType = dataNode.appendChild( doc.createElement("arrayDataTypes") );
		Node fixedType = dataNode.appendChild( doc.createElement("fixedRecordDataTypes") );
		Node variantType = dataNode.appendChild( doc.createElement("variantRecordDataTypes") );
		for( IDatatype dataType : dataTypes )
		{
			switch( dataType.getDatatypeClass() )
			{
				case BASIC:
				{
					BasicType bType = (BasicType)dataType;
					Node bNode = basicType.appendChild( doc.createElement("basicData") );
					Node dataName = bNode.appendChild( doc.createElement("name") );
					dataName.appendChild( doc.createTextNode( bType.getName() ) );
					Node dataSize = bNode.appendChild( doc.createElement("size") );
					dataSize.appendChild( doc.createTextNode(String.valueOf(bType.getSize())) );
					Node dataEndian = bNode.appendChild( doc.createElement("endian") );
					dataEndian.appendChild( doc.createTextNode(bType.getEndianness().name()) );
					break;
				}

				case SIMPLE:
				{
					SimpleType sType = (SimpleType)dataType;
					Node sNode = simpleType.appendChild( doc.createElement("simpleData") );
					Node dataName = sNode.appendChild( doc.createElement("name") );
					dataName.appendChild( doc.createTextNode(sType.getName()) );
					Node dataSize = sNode.appendChild( doc.createElement("representation") );
					dataSize.appendChild( doc.createTextNode(sType.getRepresentation().getName()) );
					break;
				}

				case ENUMERATED:
				{
					EnumeratedType eType = (EnumeratedType)dataType;
					Node sNode = enumType.appendChild( doc.createElement("enumeratedData") );
					Node dataName = sNode.appendChild( doc.createElement("name") );
					dataName.appendChild( doc.createTextNode(eType.getName()) );
					Node dataSize = sNode.appendChild( doc.createElement("representation") );
					dataSize.appendChild( doc.createTextNode(eType.getRepresentation().getName()) );
					for( Enumerator e : eType.getEnumerators() )
					{
						Node enumNode = sNode.appendChild( doc.createElement("enumerator") );
						Node enumName = enumNode.appendChild( doc.createElement("name") );
						enumName.appendChild( doc.createTextNode(e.getName()) );
						Node enumValue = enumNode.appendChild( doc.createElement("value") );
						enumValue.appendChild( doc.createTextNode( e.getValue().toString() ) );
					}
					break;
				}

				case ARRAY:
				{
					ArrayType aType = (ArrayType)dataType;
					Node sNode = arrayType.appendChild( doc.createElement("arrayData") );
					Node dataName = sNode.appendChild( doc.createElement("name") );
					dataName.appendChild( doc.createTextNode(aType.getName()) );
					Node dataTypeNode = sNode.appendChild( doc.createElement( "dataType" ) );
					dataTypeNode.appendChild( doc.createTextNode(aType.getDatatype().getName()) );
					Node cardinality = sNode.appendChild( doc.createElement("cardinality") );
					Node encoding = sNode.appendChild( doc.createElement("encoding") );
					if( aType.isCardinalityDynamic() )
					{
						cardinality.appendChild( doc.createTextNode("Dynamic") );
						encoding.appendChild( doc.createTextNode("HLAvariableArray") );
					}
					else
					{					
						encoding.appendChild( doc.createTextNode("HLAfixedArray") );
						List<String> upperBounds = new ArrayList<String>();
						for( org.portico.lrc.model.datatype.Dimension d : aType.getDimensions() )
							upperBounds.add( String.valueOf( d.getCardinalityUpperBound() ) );
						
						String boundsString = String.join( ",", upperBounds );
						cardinality.appendChild( doc.createTextNode(boundsString) );
					}
					break;
				}

				case FIXEDRECORD:
				{
					FixedRecordType fType = (FixedRecordType)dataType;
					Node sNode = fixedType.appendChild( doc.createElement("fixedRecordData") );
					Node dataName = sNode.appendChild( doc.createElement("name") );
					dataName.appendChild( doc.createTextNode( fType.getName() ) );
					Node encoding = sNode.appendChild( doc.createElement("encoding") );
					encoding.appendChild( doc.createTextNode("HLAfixedRecord") );
					for( Field e : fType.getFields() )
					{
						Node fieldNode = sNode.appendChild( doc.createElement("field") );
						Node fieldName = fieldNode.appendChild( doc.createElement("name") );
						fieldName.appendChild( doc.createTextNode(e.getName()) );
						Node enumValue = fieldNode.appendChild( doc.createElement("dataType") );
						enumValue.appendChild( doc.createTextNode(e.getDatatype().getName()) );
					}
					
					break;
				}

				case VARIANTRECORD:
				{
					VariantRecordType vType = (VariantRecordType)dataType;
					Node sNode =
					    variantType.appendChild( doc.createElement("variantRecordData") );
					Node dataName = sNode.appendChild( doc.createElement("name") );
					dataName.appendChild( doc.createTextNode(vType.getName()) );
					Node encoding = sNode.appendChild( doc.createElement("encoding") );
					encoding.appendChild( doc.createTextNode("HLAvariantRecord") );
					Node discNode = sNode.appendChild( doc.createElement("discriminant") );
					discNode.appendChild( doc.createTextNode(vType.getDiscriminantName()) );
					Node discTypeNode = sNode.appendChild( doc.createElement("dataType") );
					String discName = vType.getDiscriminantDatatype().getName();
					discTypeNode.appendChild( doc.createTextNode(discName) );
					for( Alternative e : vType.getAlternatives() )
					{
						Node fieldNode = sNode.appendChild( doc.createElement("alternative") );
						Node fieldName = fieldNode.appendChild( doc.createElement("name") );
						fieldName.appendChild( doc.createTextNode(e.getName()) );
						Node dataValue = fieldNode.appendChild( doc.createElement("dataType") );
						dataValue.appendChild( doc.createTextNode(e.getDatatype().getName()) );
						Node enumValue = fieldNode.appendChild( doc.createElement("enumerator") );
						List<String> values = new ArrayList<String>();
						for( IEnumerator v : e.getEnumerators() )
							values.add( v.getName() );

						String valueString = String.join( ",", values );
						enumValue.appendChild( doc.createTextNode(valueString) );
					}
					
					break;
				}
				default:
					break;
			}
		}
	}

	private void renderSwitches( Document doc, Node switches )
				{
			 
		Element autoProvide = doc.createElement( "autoProvide" );
		autoProvide.setAttribute( "isEnabled", "true" );
		switches.appendChild( autoProvide );
		
		Element conveyRegionDesignatorSets = doc.createElement( "conveyRegionDesignatorSets" );
		conveyRegionDesignatorSets.setAttribute( "isEnabled", "false" );
		switches.appendChild( conveyRegionDesignatorSets );
		
		Element conveyProducingFederate = doc.createElement( "conveyProducingFederate" );
		conveyProducingFederate.setAttribute( "isEnabled", "false" );
		switches.appendChild( conveyProducingFederate );
		
		Element attributeScopeAdvisory = doc.createElement( "attributeScopeAdvisory" );
		attributeScopeAdvisory.setAttribute( "isEnabled", "false" );
		switches.appendChild( attributeScopeAdvisory );
		
		Element attributeRelevanceAdvisory = doc.createElement( "attributeRelevanceAdvisory" );
		attributeRelevanceAdvisory.setAttribute( "isEnabled", "false" );
		switches.appendChild( attributeRelevanceAdvisory );
		
		Element objectClassRelevanceAdvisory = doc.createElement( "objectClassRelevanceAdvisory" );
		objectClassRelevanceAdvisory.setAttribute( "isEnabled", "true" );
		switches.appendChild( objectClassRelevanceAdvisory );
		
		Element interactionRelevanceAdvisory = doc.createElement( "interactionRelevanceAdvisory" );
		interactionRelevanceAdvisory.setAttribute( "isEnabled", "true" );
		switches.appendChild( interactionRelevanceAdvisory );
		
		Element serviceReporting = doc.createElement( "serviceReporting" );
		serviceReporting.setAttribute( "isEnabled", "false" );
		switches.appendChild( serviceReporting );
					
		Element exceptionReporting = doc.createElement( "exceptionReporting" );
		exceptionReporting.setAttribute( "isEnabled", "false" );
		switches.appendChild( exceptionReporting );
					
		Element delaySubscriptionEvaluation = doc.createElement( "delaySubscriptionEvaluation" );
		delaySubscriptionEvaluation.setAttribute( "isEnabled", "false" );
		switches.appendChild( delaySubscriptionEvaluation );
		
		Element automaticResignAction = doc.createElement( "automaticResignAction" );
		automaticResignAction.setAttribute( "resignAction", "CancelThenDeleteThenDivest" );
		switches.appendChild( automaticResignAction );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private static void addXMLAttribute( String attName,
	                                     String value,
	                                     Document doc,
	                                     Node parentNode )
					{
		Node attNode = parentNode.appendChild( doc.createElement( attName ) );
		attNode.appendChild( doc.createTextNode( value ) );
	}
						
	public static String xmlToString( Document document )
	{
		try
		{
			// Remove white space
			document.normalize();
			XPathFactory xpathFactory = XPathFactory.newInstance();
						
			// XPath to find empty text nodes.
			XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");  
			NodeList emptyTextNodes = (NodeList) 
			xpathExp.evaluate(document, XPathConstants.NODESET);
						
			// Remove each empty text node from document.
			for ( int i = 0; i < emptyTextNodes.getLength(); ++i ) 
						{
				Node emptyTextNode = emptyTextNodes.item(i);
				emptyTextNode.getParentNode().removeChild(emptyTextNode);
						}									
						
			Source source = new DOMSource( document );
			StringWriter writer = new StringWriter();
			Result result = new StreamResult( writer );

			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			xformer.setOutputProperty( OutputKeys.METHOD, "xml" );
			xformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
					
			xformer.transform( source, result );
			return writer.toString();
				}
		catch( TransformerException te )
		{
			// Programmer error, should not be thrown
			throw new IllegalStateException( te );
			}
		catch( XPathExpressionException e ) 
		{
			// Programmer error, should not be thrown
			throw new IllegalArgumentException( e );
		} 
	}

	private static class DatatypeComparator implements Comparator<IDatatype>
	{
		@Override
		public int compare( IDatatype o1, IDatatype o2 )
		{
			int result = o1.getDatatypeClass().compareTo( o2.getDatatypeClass() );
			if( result == 0 )
				result = o1.getName().compareTo( o2.getName() );
			
			return result;
		}		
	}
	
	private static class EnumeratorComparator implements Comparator<IEnumerator>
	{
		@Override
		public int compare( IEnumerator o1, IEnumerator o2 )
		{
			long o1Value = o1.getValue().longValue();
			long o2Value = o2.getValue().longValue();
			
			if( o1Value == o2Value )
				return 0;
			else if( o1Value > o2Value )
				return 1;
			else
				return -1;
		}
		
	}
	
	private static class AlternativeComparator implements Comparator<Alternative>
	{
		private EnumeratorComparator enumCompare;
		
		public AlternativeComparator()
		{
			this.enumCompare = new EnumeratorComparator();
		}
		
		@Override
		public int compare( Alternative o1, Alternative o2 )
		{
			IEnumerator o1Lowest = DatatypeHelpers.getLowestEnumerator( o1.getEnumerators() );
			IEnumerator o2Lowest = DatatypeHelpers.getLowestEnumerator( o2.getEnumerators() );
			
			return enumCompare.compare( o1Lowest, o2Lowest );
		}
	}
}

