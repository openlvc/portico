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
package hlaunit.ieee1516e.declaration;

import static hlaunit.ieee1516e.common.TypeFactory.*;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TypeFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"PublishObjectClassTest",
                                   "publishObject",
                                   "publish",
                                   "pubsub",
                                   "declarationManagement"})
public class PublishObjectClassTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int aHandle, aaHandle, abHandle, acHandle;
	private int bHandle, baHandle, bbHandle, bcHandle;

	private int blahHandle;
	private int privToDeleteHandle;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		super.beforeClass();
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		
		// cache the handles
		aHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		bHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );
		baHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "ba" );
		bbHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bb" );
		bcHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bc" );
		
		blahHandle = defaultFederate.quickACHandle( "ObjectRoot.BestEffortTest", "blah" );
		privToDeleteHandle = defaultFederate.quickACHandle( "ObjectRoot", "privilegeToDelete" );
	}

	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Helper  Methods /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will ensure that the given attribute handles of the given class have been
	 * published by the given federate. Asserts are used to ensure this, so the test that called
	 * this method will fail if any of the attributes are not being published.
	 */
	private void validatePublished( TestFederate federate,
	                                int expectedClass,
	                                Integer... expectedAttributes )
	{
		// attempt to register an instance of the specified object class
		int oHandle = federate.quickRegister( expectedClass );
		
		// make sure we own the given attributes
		for( int attributeHandle : expectedAttributes )
		{
			Assert.assertTrue( federate.quickIsOwned(oHandle,attributeHandle) );
		}
	}
	
	/**
	 * This method will ensure that the given attribute handles of the given class have *NOT* been
	 * published by the given federate. Asserts are used to ensure this, so the test that called
	 * this method will fail if any of the attributes are being published.
	 */
	private void validateNotPublished( TestFederate federate,
	                                   int expectedClass,
	                                   Integer... unpublishedAttributes )
	{
		// attempt to register an instance of the specified object class
		int oHandle = federate.quickRegister( expectedClass );
		
		// make sure we own the given attributes
		for( int attributeHandle : unpublishedAttributes )
		{
			Assert.assertFalse( federate.quickIsOwned(oHandle,attributeHandle) );
		}
	}
	
	/**
	 * This method is like {@link #validateNotPublished(TestFederate, int, Integer...)}, except
	 * that it will only check that the class itself is not published. The other method needs at
	 * least *some* attributes of the class to be published (so that it can register an instance),
	 * but this method will assert that nothing in the entire class is published.
	 */
	private void validateNotPublished( TestFederate federate, int expectedClass )
	{
		// make sure we fail a registration
		federate.quickRegisterFail( expectedClass );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Publication Methods ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void publishObjectClassAttributes( ObjectClassHandle theClass,
	//                                           AttributeHandleSet attributeList )
	//        throws ObjectClassNotDefined,
	//               AttributeNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	///////////////////////////////////
	// TEST: (valid) testOCPublish() //
	///////////////////////////////////
	@Test
	public void testOCPublish()
	{
		// publish some attributes
		try
		{
			AttributeHandleSet ahs = TypeFactory.newAttributeSet( aaHandle, abHandle );
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing object attributes", e );
		}
		
		// validate the publication
		validatePublished( defaultFederate, aHandle, aaHandle, abHandle );
	}

	//////////////////////////////////////////////////////////
	// TEST: (valid) testOCPublishWithInheritedAttributes() //
	//////////////////////////////////////////////////////////
	@Test
	public void testOCPublishWithInheritedAttributes()
	{
		// publish some attributes
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, abHandle, baHandle, bbHandle );
			// do the publication
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(bHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing object attributes", e );
		}
		
		// validate the publication
		validatePublished( defaultFederate, bHandle, aaHandle, abHandle, baHandle, bbHandle );
		// validate that the other attributes are not being published
		validateNotPublished( defaultFederate, bHandle, acHandle, bcHandle );
	}

	/////////////////////////////////////////////////////
	// TEST: (valid) testOCPublishWithEmptyHandleSet() //
	/////////////////////////////////////////////////////
	/**
	 * This is valid, but acts as an implicit unpublish
	 */
	@Test
	public void testOCPublishWithEmptyHandleSet()
	{
		// do a quick publication and validate it first (so that we can test
		// that the empty set does indeed do an unpublish
		defaultFederate.quickPublish( aHandle, aaHandle, abHandle );
		validatePublished( defaultFederate, aHandle, aaHandle, abHandle );
		
		try
		{
			// issue the publish request with the empty set
			AttributeHandleSet ahs = newAttributeSet();
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing empty attribute set", e );
		}
		
		// ensure that we no longer publish anything
		defaultFederate.quickRegisterFail( aHandle );
	}

	////////////////////////////////////////////////////////
	// TEST: (valid) testOCPublishAddsPrivilegeToDelete() //
	////////////////////////////////////////////////////////
	/**
	 * In this test we publish a set of attributes that DOES NOT include privilege to delete, but
	 * then assert that we are publishing that attribute (as it should be added to any non-empty
	 * attribute handle set implicitly).
	 */
	@Test
	public void testOCPublishAddsPrivilegeToDelete()
	{
		try
		{
			// issue the publish request with the empty set
			AttributeHandleSet ahs = newAttributeSet( aaHandle );
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing empty attribute set", e );
		}
		
		// ensure we are publishing the attribute
		validatePublished( defaultFederate, aHandle, aaHandle, privToDeleteHandle );
	}

	/////////////////////////////////////////////////////////////
	// TEST: (valid) testOCPublishAlreadyPublishedAttributes() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testOCPublishAlreadyPublishedAttributes()
	{
		// publish some handles twice, ensure they remain published
		AttributeHandleSet ahs = newAttributeSet( aaHandle, baHandle );
		
		// do the initial publication
		try
		{
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(bHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing object attributes", e );
		}
		
		// validate
		validatePublished( defaultFederate, bHandle, aaHandle, baHandle );
		
		// to the re-publication
		try
		{
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(bHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "re-publishing object attributes", e );
		}
		
		// validate the publication again
		validatePublished( defaultFederate, bHandle, aaHandle, baHandle );
	}

	/////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCPublishWithSomeAlreadyPublishedAttributes() //
	/////////////////////////////////////////////////////////////////////
	/**
	 * This method will publish a set of handles, then, after they have been validated as being
	 * published, it will re-publish a set of attributes for the class. However, the set of
	 * attributes that is being republished will be different to that which was initially published.
	 * It will contain a new attribute, and will be missing an attribute that was previously
	 * published.
	 */
	@Test
	public void testOCPublishWithSomeAlreadyPublishedAttributes()
	{
		// do the initial publication
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, baHandle );
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(bHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing object attributes", e );
		}
		
		// validate
		validatePublished( defaultFederate, bHandle, aaHandle, baHandle );
		validateNotPublished( defaultFederate, bHandle, bbHandle );
		
		// to the altered re-publication
		try
		{
			AttributeHandleSet alteredSet = newAttributeSet( aaHandle, bbHandle );
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(bHandle),
			                                                     alteredSet );
		}
		catch( Exception e )
		{
			unexpectedException( "re-publishing altered set of object attributes", e );
		}
		
		// validate the publication again
		validatePublished( defaultFederate, bHandle, aaHandle, bbHandle );
		// validate that the original attribute that was published has
		// been implicitly unpublished
		validateNotPublished( defaultFederate, bHandle, baHandle );
	}

	//////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCPublishInheritedAttributesFollowedBySuperClass() //
	//////////////////////////////////////////////////////////////////////////
	/**
	 * This test will first publish an object class, including some inherited attributes. It will
	 * then proceed to publish the parent class of the previously published object class. This is
	 * valid, and should result in the federate publishing both classes.
	 * <p/>
	 * Further, when publishing the parent class, some additional attributes that were not part
	 * of the original publication will be included. This should result in the federate publishing
	 * those attributes only for the parent class, and not the subclass (and vice versa).
	 */
	@Test
	public void testOCPublishInheritedAttributesFollowedBySuperClass()
	{
		// do the initial publication of the child class
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, baHandle );
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(bHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing object class attributes", e );
		}
		
		// validate the publication
		validatePublished( defaultFederate, bHandle, aaHandle, baHandle );
		validateNotPublished( defaultFederate, bHandle, abHandle );
		
		// do the super-class publish
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, abHandle );
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing object class attributes", e );
		}
		
		// validate the super-class publication
		validatePublished( defaultFederate, aHandle, aaHandle, abHandle );
		// validate that the child-class publication hasn't changed
		validatePublished( defaultFederate, bHandle, aaHandle, baHandle );
		validateNotPublished( defaultFederate, bHandle, abHandle );
	}
	
	/////////////////////////////////////////////////////////////
	// TEST: testOCPublishSuperclassWithChildClassAttributes() //
	/////////////////////////////////////////////////////////////
	/**
	 * In this method, we attempt to publish attributes that are of a child class of the object
	 * class we have identified. For example, we attempt a publication identifiying ObjectRoot.A
	 * as the class, but include attributes from ObjectRoot.A.B. Obviously this should fail.
	 */
	@Test
	public void testOCPublishSuperclassWithChildClassAttributes()
	{
		// publish some attributes
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, abHandle, baHandle, bbHandle );
			// do the publication
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     ahs );
			expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotDefined and )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}
	}
	
	///////////////////////////////////////////////////
	// TEST: testOCPublishWithUndefinedObjectClass() //
	///////////////////////////////////////////////////
	@Test
	public void testOCPublishWithUndefinedObjectClass()
	{
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle );
			// do the publication
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(-1), ahs );
			expectedException( ObjectClassNotDefined.class );
		}
		catch( ObjectClassNotDefined ocnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotDefined.class );
		}
	}

	///////////////////////////////////////////////////////
	// TEST: testOCPublishWithUndefinedAttributeHandle() //
	///////////////////////////////////////////////////////
	@Test
	public void testOCPublishWithUndefinedAttributeHandle()
	{
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, 11111111 );
			// do the publication
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     ahs );
			expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotDefined and )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}
	}

	/////////////////////////////////////////////////////
	// TEST: testOCPublishWithInvalidAttributeHandle() //
	/////////////////////////////////////////////////////
	/**
	 * This is like {@link #testOCPublishWithUndefinedAttributeHandle()}, except that the attribute
	 * handle in question IS in the FOM, it just isn't a valid attribute for the object class.
	 */
	@Test
	public void testOCPublishWithInvalidAttributeHandle()
	{
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, blahHandle );
			// do the publication
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     ahs );
			expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotDefined and )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}
	}

	////////////////////////////////////////////
	// TEST: testOCPublishWithNullHandleSet() //
	////////////////////////////////////////////
	@Test
	public void testOCPublishWithNullHandleSet()
	{
		try
		{
			// do the publication
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     null );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}

	////////////////////////////////////////
	// TEST: testOCPublishWhenNotJoined() //
	////////////////////////////////////////
	@Test
	public void testOCPublishWhenNotJoined()
	{
		// resign before we run the test
		defaultFederate.quickResign();
		
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, abHandle );
			// do the publication
			defaultFederate.rtiamb.publishObjectClassAttributes( getObjectClassHandle(aHandle),
			                                                     ahs );
			expectedException( FederateNotExecutionMember.class );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotExecutionMember.class );
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Unpublication Methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void unpublishObjectClass( ObjectClassHandle theClass )
	//        throws ObjectClassNotDefined,
	//               OwnershipAcquisitionPending,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	/////////////////////////////////////
	// TEST: (valid) testOCUnpublish() //
	/////////////////////////////////////
	@Test
	public void testOCUnpublish()
	{
		// setup //
		defaultFederate.quickPublish( aHandle, aaHandle, abHandle );
		validatePublished( defaultFederate, aHandle, aaHandle, abHandle );
		
		// unpublish the object class
		try
		{
			defaultFederate.rtiamb.unpublishObjectClass( getObjectClassHandle(aHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "unpublishing an object class", e );
		}
		
		// validate that the publication no longer stand
		validateNotPublished( defaultFederate, aHandle );
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnpublishChildClassDoesntAffectSuperClassPublication() //
	////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testOCUnpublishChildClassDoesntAffectSuperClassPublication()
	{
		// setup //
		// publish both the child and parent class
		defaultFederate.quickPublish( aHandle, aaHandle, abHandle );
		validatePublished( defaultFederate, aHandle, aaHandle, abHandle );
		defaultFederate.quickPublish( bHandle, aaHandle, baHandle );
		validatePublished( defaultFederate, bHandle, aaHandle, baHandle );
		
		// unpublish the child class
		try
		{
			defaultFederate.rtiamb.unpublishObjectClass( getObjectClassHandle(bHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "unpublishing an object class", e );
		}
		
		// validate that the class has been unpublished
		validateNotPublished( defaultFederate, bHandle );
		// validate that the super-class publication is unaffected
		validatePublished( defaultFederate, aHandle, aaHandle, abHandle );
	}

	///////////////////////////////////////////////////////
	// TEST: (valid) testOCUnpublishThatIsNotPublished() //
	///////////////////////////////////////////////////////
	@Test
	public void testOCUnpublishThatIsNotPublished()
	{
		try
		{
			defaultFederate.rtiamb.unpublishObjectClass( getObjectClassHandle(aHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "Unpublishing non-published object class (valid in 1516)", e );
		}
	}

	///////////////////////////////////////////////////////
	// TEST: testOCUnpublishSuperClassOfPublishedClass() //
	///////////////////////////////////////////////////////
	/**
	 * This test ensures that attempting to unpublish an unpublished parent class of class that
	 * has been published results in an error (and that the initial publication of the sub-class
	 * remains)
	 */
	@Test
	public void testOCUnpublishSuperClassOfPublishedClass()
	{
		// setup //
		// publish the child class
		defaultFederate.quickPublish( bHandle, aaHandle, baHandle );
		validatePublished( defaultFederate, bHandle, aaHandle, baHandle );
		
		// try and unpublish the parent class - should fail
		try
		{
			defaultFederate.rtiamb.unpublishObjectClass( getObjectClassHandle(aHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "Unpublishing super class of published object class " +
			                     "(valid in 1516)", e );
		}
		
		// validate that the initial publication lives on
		validatePublished( defaultFederate, bHandle, aaHandle, baHandle );
	}

	/////////////////////////////////////////////////////
	// TEST: testOCUnpublishWithUndefinedObjectClass() //
	/////////////////////////////////////////////////////
	@Test
	public void testOCUnpublishWithUndefinedObjectClass()
	{
		try
		{
			defaultFederate.rtiamb.unpublishObjectClass( getObjectClassHandle(11111111) );
			expectedException( ObjectClassNotDefined.class );
		}
		catch( ObjectClassNotDefined ocnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotDefined.class );
		}
	}

	//////////////////////////////////////////
	// TEST: testOCUnpublishWhenNotJoined() //
	//////////////////////////////////////////
	@Test
	public void testOCUnpublishWhenNotJoined()
	{
		// resign before we run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.unpublishObjectClass( getObjectClassHandle(aHandle) );
			expectedException( FederateNotExecutionMember.class );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotExecutionMember.class );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}