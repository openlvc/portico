/*
 *   Copyright 2016 The Portico Project
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
package hlaunit.ieee1516e.fom;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

import org.portico.impl.hla1516e.fomparser.FOM;
import org.portico.lrc.compat.JErrorReadingFED;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.fom.FomParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Verify that the 1516e FOM parser is behaving properly. This test was based (copied) from
 * the other parser tests which are more complete. As such, much of it is purposely commented
 * out for now and needs to be rounded out and fixed. We just can't do everything at once.
 * Such is life.
 */
@Test(groups={"Hla1516eFomParserTest","fom","fom1516e"})
public class Hla1516eFomParserTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private URL validFom;
	//private URL validWithoutSpaces;
	//private URL invalidUnbalanced;
	//private URL invalidBadComment;
	//private URL invalidRandomText;
	//private URL invalidAttBadSpace;
	private URL invalidAttBadTransport;
	private URL invalidAttBadOrder;
	//private URL invalidIntBadSpace;
	private URL invalidIntBadTransport;
	private URL invalidIntBadOrder;
	private URL validFomModule;
	private URL validFomModuleNoInteractions;
	private URL validFomModuleNoObjects;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		this.validFom               = ClassLoader.getSystemResource( "fom/testfom.xml" );
		this.validFomModule         = ClassLoader.getSystemResource( "fom/ieee1516e/testfomModule.xml" );
		this.validFomModuleNoInteractions   = ClassLoader.getSystemResource( "fom/ieee1516e/testfomModuleNoInteractions.xml" );
		this.validFomModuleNoObjects   = ClassLoader.getSystemResource( "fom/ieee1516e/testfomModuleNoObjects.xml" );
		//this.validWithoutSpaces     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-nospaces.xml" );
		//this.invalidUnbalanced      = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-unbalanced.xml" );
		//this.invalidBadComment      = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-badcomment.xml" );
		//this.invalidRandomText      = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-randomtext.xml" );
		//this.invalidAttBadSpace     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedAttributeSpace.xml" );
		this.invalidAttBadTransport = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedAttributeTransport.xml" );
		this.invalidAttBadOrder     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedAttributeOrder.xml" );
		//this.invalidIntBadSpace     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedInteractionSpace.xml" );
		this.invalidIntBadTransport = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedInteractionTransport.xml" );
		this.invalidIntBadOrder     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedInteractionOrder.xml" );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Valid FOM Test Methods //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse in a valid HLA 1516e FOM
	 */
	@Test
	public void parseValidHla13FomFull()
	{
		try
		{
			FOM.parseFOM( this.validFom );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	/**
	 * Parse in a valid HLA 1516e FOM where one module has no interactions element.
	 */
	@Test
	public void parseValidHla1516eFomModuleNoInteractions()
	{
		try
		{
			List<ObjectModel> foms = new ArrayList<ObjectModel>();
			foms.add( FomParser.parse(this.validFomModule) );
			foms.add( FomParser.parse(this.validFomModuleNoInteractions) );
			
			ObjectModel combinedFOM = ModelMerger.merge( foms );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
		
		try
		{
			List<ObjectModel> foms = new ArrayList<ObjectModel>();
			foms.add( FomParser.parse(this.validFomModuleNoInteractions) );
			foms.add( FomParser.parse(this.validFomModule) );
			ObjectModel combinedFOM = ModelMerger.merge( foms );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	/**
	 * Parse in a valid HLA 1516e FOM where one module has no objects element.
	 */
	@Test
	public void parseValidHla1516eFomModuleNoObjects()
	{
		try
		{
			List<ObjectModel> foms = new ArrayList<ObjectModel>();
			foms.add( FomParser.parse(this.validFomModuleNoObjects) );
			foms.add( FomParser.parse(this.validFomModule) );
			ObjectModel combinedFOM = ModelMerger.merge( foms );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	/**
	 * Parse a valid FOM that doesn't contain spaces
	 */
//	@Test
//	public void parseValidHla13FomWithoutSpaces()
//	{
//		// testfom-nospaces.fed
//		try
//		{
//			FOM.parseFOM( this.validWithoutSpaces );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Unexpected exception parsing valid FOM (no spaces): "+e.getMessage(), e );
//		}
//	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Invalid FOM Test Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse the XML FOM file that has the wrong number of parenthesis
	 */
//	@Test
//	public void parseInvalidHla13FomWithUnbalancedParenthesis()
//	{
//		// testfom-unbalanced.fed
//		try
//		{
//			FOM.parseFOM( this.invalidUnbalanced );
//			Assert.fail( "Was expecting exception parsing invalid fom (unbalanced parenthesis)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "line 6, column 3" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (unbalanced parenthesis)", e );
//		}
//	}
	
//	@Test
//	public void parseInvalidHla13FomWithMalformedComment()
//	{
//		// testfom-badcomment.fed
//		try
//		{
//			FOM.parseFOM( this.invalidBadComment );
//			Assert.fail( "Was expecting exception parsing invalid fom (bad comment)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "Lexical error at line 9, column 4" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (bad comment)", e );
//		}
//	}
	
//	@Test
//	public void parseInvalidHla13FomWithRandomText()
//	{
//		// testfom-randomtext.fed
//		try
//		{
//			FOM.parseFOM( this.invalidRandomText );
//			Assert.fail( "Was expecting exception parsing invalid fom (random text)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "Encountered \"RANDOM\" at line 9, column 3" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (random text)", e );
//		}
//	}
	
//	@Test
//	public void parseInvalidHla13FomWithUndefinedSpaceForAttribute()
//	{
//		// testfom-undefinedAttributeSpace.fed
//		try
//		{
//			FOM.parseFOM( this.invalidAttBadSpace );
//			Assert.fail( "Was expecting exception parsing invalid fom (undefined space - att)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "uses undefined space \"NoSuchSpace\"" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (undefined space - att)", e );
//		}
//	}
	
	@Test
	public void parseInvalidHla13FomWithUndefinedTransportForAttribute()
	{
		// testfom-undefinedAttributeTransport.fed
		try
		{
			FOM.parseFOM( this.invalidAttBadTransport );

			// NOTE Made the 1516e parser more tolerant to FOM errors by allowing
			//      it to accept missing order children and apply defaults. As such,
			//      this should not fail. Need to add control over strict/non-strict parsing
			//Assert.fail( "Was expecting exception parsing invalid fom (undefined transport - attribute)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains("<transportation>"),
			            "Exception flagged wrong error: "+error.getMessage() );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined transport - att)", e );
		}
	}
	
	@Test
	public void parseInvalidHla13FomWithUndefinedOrderForAttribute()
	{
		// testfom-undefinedAttributeOrder.fed
		try
		{
			FOM.parseFOM( this.invalidAttBadOrder );

			// NOTE Made the 1516e parser more tolerant to FOM errors by allowing
			//      it to accept missing order children and apply defaults. As such,
			//      this should not fail. Need to add control over strict/non-strict parsing
			//Assert.fail( "Was expecting exception parsing invalid fom (undefined order - attribute)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains("<order>"), "Exception flagged wrong error" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined order - att)", e );
		}
	}
	
//	@Test
//	public void parseInvalidHla13FomWithUndefinedSpaceForInteraction()
//	{
//		// testfom-undefinedInteractionSpace.fed
//		try
//		{
//			FOM.parseFOM( this.invalidIntBadSpace );
//			Assert.fail( "Was expecting exception parsing invalid fom (undefined space - int)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "uses undefined space: \"NoSuchSpace\"" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (undefined space - int)", e );
//		}
//	}
	
	@Test
	public void parseInvalidHla13FomWithUndefinedTransportForInteraction()
	{
		// testfom-undefinedInteractionTransport.fed
		try
		{
			FOM.parseFOM( this.invalidIntBadTransport );

			// NOTE Made the 1516e parser more tolerant to FOM errors by allowing
			//      it to accept missing order children and apply defaults. As such,
			//      this should not fail. Need to add control over strict/non-strict parsing
			//Assert.fail( "Was expecting exception parsing invalid fom (undefined transport - interation)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains("<transportation>"),
			            "Exception flagged wrong error" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined transport - int)", e );
		}
	}
	
	@Test
	public void parseInvalidHla13FomWithUndefinedOrderForInteraction()
	{
		// testfom-undefinedInteractionOrder.fed
		try
		{
			FOM.parseFOM( this.invalidIntBadOrder );
			
			// NOTE Made the 1516e parser more tolerant to FOM errors by allowing
			//      it to accept missing order children and apply defaults. As such,
			//      this should not fail. Need to add control over strict/non-strict parsing
			//Assert.fail( "Was expecting exception parsing invalid fom (undefined order - interation)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains("<order>"), "Exception flagged wrong error" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined order - int)", e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
