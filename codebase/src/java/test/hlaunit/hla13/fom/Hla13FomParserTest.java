/*
 *   Copyright 2007 The Portico Project
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
package hlaunit.hla13.fom;

import java.net.URL;

import static org.testng.Assert.*;

import org.portico.impl.hla13.fomparser.FOM;
import org.portico.lrc.compat.JErrorReadingFED;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups={"Hla13FomParserTest","fom","fom13"})
public class Hla13FomParserTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private URL validFom;
	private URL validWithoutSpaces;
	private URL invalidUnbalanced;
	private URL invalidBadComment;
	private URL invalidRandomText;
	private URL invalidAttBadSpace;
	private URL invalidAttBadTransport;
	private URL invalidAttBadOrder;
	private URL invalidIntBadSpace;
	private URL invalidIntBadTransport;
	private URL invalidIntBadOrder;

	private URL pitchFom;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		this.validFom               = ClassLoader.getSystemResource( "fom/testfom.fed" );
		this.validWithoutSpaces     = ClassLoader.getSystemResource( "fom/testfom-nospaces.fed" );
		this.invalidUnbalanced      = ClassLoader.getSystemResource( "fom/testfom-unbalanced.fed" );
		this.invalidBadComment      = ClassLoader.getSystemResource( "fom/testfom-badcomment.fed" );
		this.invalidRandomText      = ClassLoader.getSystemResource( "fom/testfom-randomtext.fed" );
		this.invalidAttBadSpace     = ClassLoader.getSystemResource( "fom/testfom-undefinedAttributeSpace.fed" );
		this.invalidAttBadTransport = ClassLoader.getSystemResource( "fom/testfom-undefinedAttributeTransport.fed" );
		this.invalidAttBadOrder     = ClassLoader.getSystemResource( "fom/testfom-undefinedAttributeOrder.fed" );
		this.invalidIntBadSpace     = ClassLoader.getSystemResource( "fom/testfom-undefinedInteractionSpace.fed" );
		this.invalidIntBadTransport = ClassLoader.getSystemResource( "fom/testfom-undefinedInteractionTransport.fed" );
		this.invalidIntBadOrder     = ClassLoader.getSystemResource( "fom/testfom-undefinedInteractionOrder.fed" );
		this.pitchFom               = ClassLoader.getSystemResource( "fom/testfom-pitch.fed" );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Valid FOM Test Methods //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse in a valid HLA 1.3 FOM
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
	 * Parse a valid FOM that doesn't contain spaces
	 */
	@Test
	public void parseValidHla13FomWithoutSpaces()
	{
		// testfom-nospaces.fed
		try
		{
			FOM.parseFOM( this.validWithoutSpaces );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM (no spaces): "+e.getMessage(), e );
		}
	}
	
	/**
	 * Apparently Pitch support a different style for the "version" part of the FOM (v1_3 rather
	 * than v1.3).
	 */
	@Test
	public void parsePitchFomFormat()
	{
		try
		{
			FOM.parseFOM( this.pitchFom );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing pitch format FOM: " + e.getMessage(), e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Invalid FOM Test Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse the FED file that has the wrong number of parenthesis
	 */
	@Test
	public void parseInvalidHla13FomWithUnbalancedParenthesis()
	{
		// testfom-unbalanced.fed
		try
		{
			FOM.parseFOM( this.invalidUnbalanced );
			Assert.fail( "Was expecting exception parsing invalid fom (unbalanced parenthesis)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "line 6, column 3" ),
			            "Exception occurred in unexpected location" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (unbalanced parenthesis)", e );
		}
	}
	
	@Test
	public void parseInvalidHla13FomWithMalformedComment()
	{
		// testfom-badcomment.fed
		try
		{
			FOM.parseFOM( this.invalidBadComment );
			Assert.fail( "Was expecting exception parsing invalid fom (bad comment)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "Lexical error at line 9, column 4" ),
			            "Exception occurred in unexpected location" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (bad comment)", e );
		}
	}
	
	@Test
	public void parseInvalidHla13FomWithRandomText()
	{
		// testfom-randomtext.fed
		try
		{
			FOM.parseFOM( this.invalidRandomText );
			Assert.fail( "Was expecting exception parsing invalid fom (random text)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "Encountered \"RANDOM\" at line 9, column 3" ),
			            "Exception occurred in unexpected location" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (random text)", e );
		}
	}
	
	@Test
	public void parseInvalidHla13FomWithUndefinedSpaceForAttribute()
	{
		// testfom-undefinedAttributeSpace.fed
		try
		{
			FOM.parseFOM( this.invalidAttBadSpace );
			Assert.fail( "Was expecting exception parsing invalid fom (undefined space - att)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "uses undefined space \"NoSuchSpace\"" ),
			            "Exception occurred in unexpected location" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined space - att)", e );
		}
	}
	
	@Test
	public void parseInvalidHla13FomWithUndefinedTransportForAttribute()
	{
		// testfom-undefinedAttributeTransport.fed
		try
		{
			FOM.parseFOM( this.invalidAttBadTransport );
			Assert.fail( "Was expecting exception parsing invalid fom (undefined transport - att)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "at line 14, column 23" ),
			            "Exception occurred in unexpected location" );
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
			Assert.fail( "Was expecting exception parsing invalid fom (undefined order - att)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "at line 14, column 32" ),
			            "Exception occurred in unexpected location" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined order - att)", e );
		}
	}
	
	@Test
	public void parseInvalidHla13FomWithUndefinedSpaceForInteraction()
	{
		// testfom-undefinedInteractionSpace.fed
		try
		{
			FOM.parseFOM( this.invalidIntBadSpace );
			Assert.fail( "Was expecting exception parsing invalid fom (undefined space - int)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "uses undefined space: \"NoSuchSpace\"" ),
			            "Exception occurred in unexpected location" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined space - int)", e );
		}
	}
	
	@Test
	public void parseInvalidHla13FomWithUndefinedTransportForInteraction()
	{
		// testfom-undefinedInteractionTransport.fed
		try
		{
			FOM.parseFOM( this.invalidIntBadTransport );
			Assert.fail( "Was expecting exception parsing invalid fom (undefined transport - int)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "at line 18, column 16" ),
			            "Exception occurred in unexpected location" );
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
			Assert.fail( "Was expecting exception parsing invalid fom (undefined order - int)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains( "at line 18, column 25" ),
			            "Exception occurred in unexpected location" );
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
