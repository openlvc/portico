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
package hlaunit.ieee1516.common;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.portico.impl.hla1516.types.HLA1516AttributeHandle;
import org.testng.Assert;

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ResignAction;
import hla.rti1516.RTIambassador;
import hla.rti1516.ParameterHandleValueMap;
import hlaunit.ieee1516.TestSetup;

/**
 * This class represents a federate that can be used for testing purposes. It provides a number
 * of helper methods that speed the process of writing tests
 */
public class TestFederate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public String federateName;
	public int federateHandle;
	public RTIambassador rtiamb;
	public TestFederateAmbassador fedamb;

	public String simpleName;
	public Abstract1516Test test;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public TestFederate( String name, Abstract1516Test test )
	{
		if( name == null || test == null )
		{
			Assert.fail( "Null value given when creating TestFederate, can't continue" );
		}
		
		this.federateName = name;
		this.federateHandle = -1;
		this.test = test;
		this.simpleName = this.test.getClass().getSimpleName();
		this.rtiamb = TestSetup.createRTIambassador();
		this.fedamb = new TestFederateAmbassador( this );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////
	/////////////////// Basic Access Methods ///////////////////
	////////////////////////////////////////////////////////////
	public RTIambassador getRtiAmb()
	{
		return this.rtiamb;
	}
	
	public AttributeHandleSet createAHS( int...handles )
	{
		try
		{
			AttributeHandleSet set = rtiamb.getAttributeHandleSetFactory().create();
			for( int handle : handles )
			{
				set.add( new HLA1516AttributeHandle(handle) );
			}
			return set;
		}
		catch( Exception e )
		{
			// This should hopefully never happen, unless we're stupid and pass negative handles
			Assert.fail( "Couldn't create AttributeHandleSet: " + e.getMessage(), e );
			return null;
		}
	}

	////////////////////////////////////////////////////////////
	//////////////// Create and Destroy Methods ////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Calls {@link #quickCreate(String)}, using the name of the test this federate is associated
	 * with as the name of the federation.
	 */
	public void quickCreate()
	{
		quickCreate( this.simpleName );
	}
	
	/**
	 * Same as {@link #quickCreate()} except that you can specify the name of the federation
	 */
	public void quickCreate( String name )
	{
		try
		{
			URL fom = ClassLoader.getSystemResource( "fom/testfom.xml" );
			this.rtiamb.createFederationExecution( name, fom );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in quickCreate in " + simpleName + " ("+e.getClass()+")", e );
		}
	}
	
	/**
	 * Destroy the federation of the name equal to the fedname protected variable
	 * (which is the simple name of the test class). If the destroy fails, Assert.fail() will
	 * be used rather than throwing an exception.
	 */
	public void quickDestroy()
	{
		quickDestroy( this.simpleName );
	}
	
	/**
	 * Same as {@link #quickDestroy()} except that you can specify the federation name
	 * @param name
	 */
	public void quickDestroy( String name )
	{
		try
		{
			this.rtiamb.destroyFederationExecution( name );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in quickDestroy in " + simpleName + " ("+e.getClass()+")", e );
		}		
	}
	
	/**
	 * Same as {@link #quickDestroyTolerant(String)}, except that it uses the simple name
	 */
	public void quickDestroyTolerant()
	{
		quickDestroyTolerant( this.simpleName );
	}
	
	/**
	 * This method will attemp to destroy the federation of the given name. However, if there is an
	 * error during this process (the federation now existing for example), it is ignored and
	 * processing is allowed to continue.
	 */
	public void quickDestroyTolerant( String name )
	{
		try
		{
			this.rtiamb.destroyFederationExecution( name );
		}
		catch( Exception e )
		{
			// ignore
		}
	}
	
	////////////////////////////////////////////////////////////
	/////////////////////// Join Methods ///////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Joins this federate to the default federation. If there is an error Assert.fail()
	 * will be used
	 */
	public int quickJoin()
	{
		return quickJoin( simpleName );
	}
	
	/**
	 * Joins this federate to the given federation. If there is an error Assert.fail() will be used
	 */
	public int quickJoin( String theFederation )
	{
		try
		{
			this.fedamb = new TestFederateAmbassador( this );
			FederateHandle handle = this.rtiamb.joinFederationExecution( federateName,
			                                                             theFederation,
			                                                             this.fedamb,
			                                                             null );
			this.federateHandle = TypeFactory.getFederateHandle( handle );
			return this.federateHandle;
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in quickJoin in " + simpleName + " ("+e.getClass()+")", e );
			return -1;
		}
	}
	
	////////////////////////////////////////////////////////////
	////////////////////// Resign Methods //////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Centralize the resign processing just to make things easier. If there is a problem while
	 * attempting the resign, this method will use Assert.fail() to fail the test rather than
	 * throw an exception. If the federate is not currently joined (and a
	 * FederateNotExecutionMember exception is thrown), it will be ignored.
	 */
	public void quickResign( ResignAction resignAction )
	{
		try
		{
			this.rtiamb.resignFederationExecution( resignAction );
		}
		catch( FederateNotExecutionMember nem )
		{
			// Ignore this
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in quickResign in " + simpleName + " ("+e.getClass()+")", e );
		}
	}
	
	/**
	 * Same as {@link #quickResign(ResignAction)} with a resign action of
	 * DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES.
	 */
	public void quickResign()
	{
		this.quickResign( ResignAction.DELETE_OBJECTS );
	}

	//////////////////////////////////////////////////////////////
	/////////////// Synchronization Helper Methods ///////////////
	//////////////////////////////////////////////////////////////
	/**
	 * Announce a federation-wide synchronization point with the given label and tag. This method
	 * will block until the synchornization success message has been recevied. If there is any
	 * problem, Assert.fail() will be used rather than throwing an exception.
	 */
	public void quickAnnounce( String label, byte[] tag )
	{
		try
		{
			// announce the sync point //
			rtiamb.registerFederationSynchronizationPoint( label, tag );

			// wait for the success/failure result //
			if( fedamb.waitForSyncResult(label) == false )
			{
				// we didn't get the result we wanted, fail
				Assert.fail( "RTI notified that syncpoint reg [" + label + "] failed" );
			}
			
			// wait for the announcement //
			fedamb.waitForSyncAnnounce( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAnnounce(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This is the same as {@link #quickAnnounce(String, byte[])} except that the given array of
	 * federate handles is used to declare a restricted sync point.
	 */
	public void quickAnnounce( String label, byte[] tag, int... handles )
	{
		// turn the set into the appropriate FederateHandleSet
		FederateHandleSet set = TypeFactory.newFederateSet( handles );
		
		// announce the sync point //
		try
		{
			rtiamb.registerFederationSynchronizationPoint( label, tag, set );

			// wait for the success/failure result //
			if( fedamb.waitForSyncResult(label) == false )
			{
				// we didn't get the result we wanted, fail
				Assert.fail( "RTI notified that syncpoint reg [" + label + "] failed" );
			}
			
			// wait for the announcement //
			fedamb.waitForSyncAnnounce( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAnnounce(): " + e.getMessage(), e );
		}
	}

	/**
	 * This method will signal to the RTI that the federate has reached the given sync point. If
	 * this causes an exception, Assert.fail() will be used to kill the test.
	 */
	public void quickAchieved( String label )
	{
		try
		{
			rtiamb.synchronizationPointAchieved( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAchieved(): " + e.getMessage(), e );
		}
	}

	/////////////////////////////////////////////////////////////
	/////////// Publish and Subscribe helper methods ////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Attempts to publish the given class handle with the given attribute handles. If there is an
	 * error, Assert.fail() is used to kill the test.
	 * <p/>
	 * <b>This method is for Object Classes (obviously - what with this talk of attributes)</b>
	 */
	public void quickPublish( int classHandle, int... attributeHandles )
	{
		try
		{
			// attempt to publish the object class
			rtiamb.publishObjectClassAttributes( TypeFactory.getObjectClassHandle(classHandle),
			                                     TypeFactory.newAttributeSet(attributeHandles) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickPublish(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Attempts to publish the given interaction class handle. If there is an error, Assert.fail()
	 * is used to kill the test.
	 * <p/>
	 * <b>This method is for Interaction Classes only.</b>
	 */
	public void quickPublish( int classHandle )
	{
		try
		{
			rtiamb.publishInteractionClass( TypeFactory.getInteractionHandle(classHandle) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickPublish(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method is the same as {@link #quickPublish(int)} except that it will fetch the
	 * interaction handle on your behalf.
	 */
	public void quickPublish( String className )
	{
		try
		{
			rtiamb.publishInteractionClass( rtiamb.getInteractionClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickPublish(): " + e.getMessage(), e );
		}
	}

	/**
	 * This method is the same as {@link #quickPublish(int, int[])}, except that it will fetch the
	 * handles on your behalf.
	 * <p/>
	 * <b>This method is for Object Classes (obviously - what with this talk of attributes)</b>
	 */
	public void quickPublish( String className, String... attributeNames )
	{
		try
		{
			// get the class handle //
			ObjectClassHandle classHandle = rtiamb.getObjectClassHandle( className );
			
			// get the attribute handles //
			AttributeHandleSet attributeHandles = TypeFactory.newAttributeSet();
			for( String attributeName : attributeNames )
			{
				attributeHandles.add( rtiamb.getAttributeHandle(classHandle, attributeName) );
			}
			
			// pass the call onto the other method //
			rtiamb.publishObjectClassAttributes( classHandle, attributeHandles );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickPublish(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Attempts to subscribe to the given class handle with the given attribute handles. If there
	 * is an error, Assert.fail() is used to kill the test.
	 */
	public void quickSubscribe( int classHandle, int... attributeHandles )
	{
		try
		{
			// attempt to publish the object class
			rtiamb.subscribeObjectClassAttributes( TypeFactory.getObjectClassHandle(classHandle),
			                                       TypeFactory.newAttributeSet(attributeHandles) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribe(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method is the same as {@link #quickSubscribe(int, int[])}, except that it will fetch
	 * the handles on your behalf.
	 */
	public void quickSubscribe( String className, String... attributeNames )
	{
		try
		{
			// get the class handle //
			ObjectClassHandle classHandle = rtiamb.getObjectClassHandle( className );
			
			// get the attribute handles //
			AttributeHandleSet attributeHandles = TypeFactory.newAttributeSet();
			for( String attributeName : attributeNames )
			{
				attributeHandles.add( rtiamb.getAttributeHandle(classHandle,attributeName) );
			}
			
			// pass the call onto the other method //
			rtiamb.subscribeObjectClassAttributes( classHandle, attributeHandles );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribe(): " + e.getMessage(), e );
		}
	}

	/**
	 * This method will attempt to subscribe to the interaction class with the given handle. If
	 * there is an exception during this process, Assert.fail() will be used to kill the test.
	 * <p/>
	 * <b>This method is for interaction classes only</b>
	 */
	public void quickSubscribe( int classHandle )
	{
		try
		{
			rtiamb.subscribeInteractionClass( TypeFactory.getInteractionHandle(classHandle) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribe(): " + e.getMessage(), e );
		}
	}

	/**
	 * This method is the same as {@link #quickSubscribe(int)} except that it will fetch the
	 * interaction class handle on your behalf. Again, if there is an exception during the process
	 * of getting the handle or subscribing to it, an exception will be thrown.
	 * <p/>
	 * <b>This method is for interaction classes only</b>
	 */
	public void quickSubscribe( String className )
	{
		try
		{
			rtiamb.subscribeInteractionClass( rtiamb.getInteractionClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribe(): " + e.getMessage(), e );
		}
	}

	
	/**
	 * Unsubscribe from the given object class, Assert.fail() if there is an error.
	 */
	public void quickUnsubscribe( int objectClassHandle )
	{
		try
		{
			rtiamb.unsubscribeObjectClass( TypeFactory.getObjectClassHandle(objectClassHandle) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickUnsubscribe(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Unsubscribe from the given object class, Assert.fail() if there is an error.
	 */
	public void quickUnsubscribe( String objectClassName )
	{
		quickUnsubscribe( quickOCHandle(objectClassName) );
	}

	/////////////////////////////////////////////////////////////
	///////////// Object Management helper methods //////////////
	/////////////////////////////////////////////////////////////
	/**
	 * This method will attempt to register an object instance of the given class handle. If
	 * successful, the instance handle will be returned, if not, Assert.fail() will be used to
	 * kill the test
	 */
	public int quickRegister( int classHandle )
	{
		try
		{
			ObjectInstanceHandle oHandle = 
				rtiamb.registerObjectInstance( TypeFactory.getObjectClassHandle(classHandle) );
			
			return TypeFactory.getObjectHandle( oHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickRegister(int): " + e.getMessage(), e );
			return -1;
		}
	}

	/**
	 * This method will attempt to register an object instance of the given class handle and
	 * the given name. If successful, the instance handle will be returned, if not, Assert.fail()
	 * will be used to kill the test
	 */
	public int quickRegister( int classHandle, String objectName )
	{
		try
		{
			ObjectInstanceHandle oHandle = 
				rtiamb.registerObjectInstance( TypeFactory.getObjectClassHandle(classHandle),
				                               objectName );
			
			return TypeFactory.getObjectHandle( oHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickRegister(int,String): " + e.getMessage(), e );
			return -1;
		}
	}
	
	/**
	 * This method is much like {@link #quickRegister(int)}, except that it expects *failure*. If
	 * the request to register the instance does not fail, Assert.fail() will be used to kill the
	 * test. If the attempt to register the instance does fail, this method will return as normal.
	 */
	public void quickRegisterFail( int classHandle )
	{
		try
		{
			rtiamb.registerObjectInstance( TypeFactory.getObjectClassHandle(classHandle) );
			Assert.fail( "Was expecting registration of class [" + classHandle + "] would fail" );
		}
		catch( Exception e )
		{
			// success!
		}
	}
	
	/**
	 * This method is the same as {@link #quickRegister(int)}, except that you can pass it the
	 * name of the class you wish to register an instance of (rather than the class handle).
	 */
	public int quickRegister( String className )
	{
		try
		{
			// find the handle for the class
			ObjectClassHandle cHandle = rtiamb.getObjectClassHandle( className );
			ObjectInstanceHandle oHandle = rtiamb.registerObjectInstance( cHandle );
			return TypeFactory.getObjectHandle( oHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickRegister(String): " + e.getMessage(), e );
			return -1;
		}
	}

	/**
	 * This method is the same as {@link #quickRegister(int,String)}, except that you can pass it
	 * the name of the class you wish to register an instance of (rather than the class handle).
	 */
	public int quickRegister( String className, String objectName )
	{
		try
		{
			// find the handle for the class
			ObjectClassHandle cHandle = rtiamb.getObjectClassHandle( className );
			ObjectInstanceHandle oHandle = rtiamb.registerObjectInstance( cHandle, objectName );
			return TypeFactory.getObjectHandle( oHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickRegister(String,String): " + e.getMessage(), e );
			return -1;
		}
	}

	/**
	 * This method will attempt to delete the object instance with the given handle. If the
	 * delete attempt fails, Assert.fai() will be used to kill the test.
	 */
	public void quickDelete( int handle, byte[] tag )
	{
		try
		{
			rtiamb.deleteObjectInstance( TypeFactory.getObjectHandle(handle), tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickDelete(int,byte[]): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Attempt to locally delete the object identified by the given handle. If there is an error
	 * doing this, use Assert.fail() to kill the test. Also note that this will remove the object
	 * instance with the given handle from the store in the federate ambassador so that we don't
	 * think we still know about an object that we no longer do.
	 */
	public void quickLocalDelete( int handle )
	{
		try
		{
			rtiamb.localDeleteObjectInstance( TypeFactory.getObjectHandle(handle) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickLocalDelete(int): " + e.getMessage(), e );
		}
		
		fedamb.removeObjectInstance( TypeFactory.getObjectHandle(handle), "".getBytes(), null );
	}

	/**
	 * This method will send out a request for the update of each of the attributes identified for
	 * the object identified with the given handle.
	 */
	public void quickProvide( int objectHandle, int... attributes )
	{
		try
		{
			rtiamb.requestAttributeValueUpdate( TypeFactory.getObjectHandle(objectHandle),
			                                    TypeFactory.newAttributeSet(attributes),
			                                    new byte[0] );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickProvide(int,int...): " + e.getMessage(), e );
		}
	}

	/**
	 * This method will send out a request for updates for each of the provided attributes of each
	 * of the objects of the identified class. 
	 */
	public void quickProvideClass( int classHandle, int... attributes )
	{
		try
		{
			rtiamb.requestAttributeValueUpdate( TypeFactory.getObjectClassHandle(classHandle),
			                                    TypeFactory.newAttributeSet(attributes),
			                                    new byte[0] );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickProvideClass(int,int...): " + e.getMessage(), e );
		}
	}

	/**
	 * This method fetches the class handle for the object class of the given name. If there is
	 * an exception while this is happening, Assert.fail is used to kill the test.
	 */
	public int quickOCHandle( String className )
	{
		try
		{
			return TypeFactory.getObjectClassHandle( rtiamb.getObjectClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching class handle for [" + className + "]", e );
			return -1;
		}
	}
	
	/**
	 * This method is roughly the same as {@link #quickOCHandle(String)}, except that it is for 
	 * an attribute handle (rather than a class handle)
	 */
	public int quickACHandle( String className, String attName )
	{
		try
		{
			return TypeFactory.getAttributeHandle(
			    rtiamb.getAttributeHandle( rtiamb.getObjectClassHandle(className), attName) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching attribute handle for [" + attName +
			             "] of class [" + className + "]", e );
			return -1;
		}
	}
	
	/**
	 * This method will fetch the handle of the named attribute in the given object class handle.
	 */
	public int quickACHandle( int classHandle, String attName )
	{
		try
		{
			return TypeFactory.getAttributeHandle(
			    rtiamb.getAttributeHandle( TypeFactory.getObjectClassHandle(classHandle), attName ) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching attribute handle for [" + attName +
			             "] of class [" + classHandle + "]", e );
			return -1;
		}
	}
	
	/**
	 * This method fetches the class handle for the interaction class of the given name. If there
	 * is an exception while this is happening, Assert.fail() is used to kill the test.
	 */
	public int quickICHandle( String className )
	{
		try
		{
			return TypeFactory.getInteractionHandle( rtiamb.getInteractionClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fecthing interaction handle for [" + className + "]", e );
			return -1;
		}
	}

	/**
	 * This method is roughly the same as {@link #quickICHandle(String)}, except that it is for
	 * fetching the handle of a parameter (rather than the interaction class). If there is an
	 * exception while carrying out this action, Assert.fail() is used to kill the test.
	 */
	public int quickPCHandle( String className, String paramName )
	{
		try
		{
			return TypeFactory.getParameterHandle( 
			    rtiamb.getParameterHandle(rtiamb.getInteractionClassHandle(className), paramName) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching parameter handle for [" + paramName +
			             "] of class [" + className + "]", e );
			return -1;
		}
	}

	/////////////////////////////////////////////////////////////
	///////////// Object/Interaction Helper Methods /////////////
	/////////////////////////////////////////////////////////////
	/**
	 * This method sends a reflection for the identified object instance, passing the given map of
	 * values as the attributes with the update. The keys for that map should contain the attribute
	 * handles. If there is a problem sending the reflection, Assert.fail() will be used to kill
	 * the test.
	 * 
	 * @param oHandle The handle of the object to send the update for
	 * @param attributes The set of attributes and their values to send with the reflection
	 * @param tag The tag to send with the reflection
	 */
	public void quickReflectWithHandles( int oHandle, Map<Integer,byte[]> attributes, byte[] tag )
	{
		try
		{
			// resolve the attribute names to handles //
			AttributeHandleValueMap map = TypeFactory.newAttributeMap( attributes );
			
			// send the update //
			rtiamb.updateAttributeValues( TypeFactory.getObjectHandle(oHandle), map, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending reflection: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method sends a reflection for the identified object instance, passing the given map of
	 * values as the attributes with the update. The keys for that map should contain the attribute
	 * names (they will be resolved to handles on your behalf). If there is a problem sending the
	 * reflection, Assert.fail() will be used to kill the test.
	 * 
	 * @param oHandle The handle of the object to send the update for
	 * @param attributes The set of attributes and their values to send with the reflection
	 * @param tag The tag to send with the reflection
	 */
	public void quickReflect( int oHandle, Map<String,byte[]> attributes, byte[] tag )
	{
		try
		{
			// get the class of the object instance //
			// we'll need this to resolve the attribute names //
			ObjectInstanceHandle instanceHandle = TypeFactory.getObjectHandle( oHandle );
			ObjectClassHandle classHandle = rtiamb.getKnownObjectClassHandle( instanceHandle );
			
			// resolve the attribute names to handles //
			AttributeHandleValueMap map = TypeFactory.newAttributeMap();
			for( String aName : attributes.keySet() )
			{
				map.put( rtiamb.getAttributeHandle(classHandle,aName), attributes.get(aName) );
			}
			
			// send the update //
			rtiamb.updateAttributeValues( instanceHandle, map, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending reflection: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method is much like {@link #quickReflect(int, Map, byte[])} except that it will
	 * automatically generate the content of the attributes to send. The content will be the
	 * raw bytes for the name of the attribute. Thus, if the name of the attribute was "aa",
	 * the bytes would be equal to "aa".getBytes(). The tag will be the same, using the string
	 * "letag".
	 * <p/>
	 * This method will just call {@link #quickReflect(int, Map, byte[])} after having generated
	 * the necessary values.
	 */
	public void quickReflect( int oHandle, String... attributes )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String attribute : attributes )
			map.put( attribute, attribute.getBytes() );
		
		quickReflect( oHandle, map, "letag".getBytes() );
	}
	
	/**
	 * The same as {@link #quickReflect(int, String...)} except that it uses attribute handles,
	 * rather than names. Thus, the value for an attribute handle 123 is equal to "123".getBytes()
	 */
	public void quickReflect( int oHandle, int... attributes )
	{
		HashMap<Integer,byte[]> map = new HashMap<Integer,byte[]>();
		for( int attribute : attributes )
			map.put( attribute, ("" + attribute).getBytes() );
		
		quickReflectWithHandles( oHandle, map, "letag".getBytes() );
	}
	
	/**
	 * This method is the same as {@link #quickReflect(int, Map, byte[])}, except that it expects
	 * *failure*. This is useful in the publication tests when wanting to ensure that we can't
	 * reflect non-published attributes. If the reflection doesn't fail, Assert.fail() will be used
	 * to kill the set. If the reflection does fail, this method will return as normal.
	 */
	public void quickReflectFail( int oHandle, Map<String,byte[]> attributes, byte[] tag )
	{
		// declare these here because we need them in a different scope to their assignment
		ObjectInstanceHandle instanceHandle = null;
		AttributeHandleValueMap map = null;
		
		// get all the relevant values
		try
		{
			// get the class of the object instance //
			// we'll need this to resolve the attribute names //
			instanceHandle = TypeFactory.getObjectHandle( oHandle );
			ObjectClassHandle classHandle = rtiamb.getKnownObjectClassHandle( instanceHandle );
			
			// resolve the attribute names to handles //
			map = TypeFactory.newAttributeMap();
			for( String aName : attributes.keySet() )
			{
				map.put( rtiamb.getAttributeHandle(classHandle,aName), attributes.get(aName) );
			}
		}
		catch( Exception e )
		{
			Assert.fail("Unexpected exception setting up quickReflectFail: " + e.getMessage(), e );
		}

		// send the reflection, this part SHOULD fail
		try
		{
			// send the update //
			rtiamb.updateAttributeValues( instanceHandle, map, tag );
			Assert.fail( "Was expecting an exception during reflection" );
		}
		catch( Exception e )
		{
			// success!
		}
	}
	
	/**
	 * This method is basically the same as {@link #quickReflect(int, Map, byte[])}, except that
	 * you can specify the time to send with the reflection.
	 */
	public void quickReflect( int oHandle, Map<String,byte[]> attributes, byte[] tag, double time )
	{
		try
		{
			// get the class of the object instance //
			// we'll need this to resolve the attribute names //
			ObjectInstanceHandle instanceHandle = TypeFactory.getObjectHandle( oHandle );
			ObjectClassHandle classHandle = rtiamb.getKnownObjectClassHandle( instanceHandle );
			
			// resolve the attribute names to handles //
			AttributeHandleValueMap map = TypeFactory.newAttributeMap();
			for( String aName : attributes.keySet() )
			{
				map.put( rtiamb.getAttributeHandle(classHandle,aName), attributes.get(aName) );
			}
			
			// send the update //
			rtiamb.updateAttributeValues( instanceHandle, map, tag, TypeFactory.createTime(time) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending reflection: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method handles the process of sending an interaction. The type of interaction send is
	 * identified by the given interaction class name. The parameters sent are supplied in the 
	 * given map, with each key being the name of the parameter. This method will attempt to resolve
	 * the handles for the class and parameters on your behalf. If there is a problem creating or
	 * sending the interaction, Assert.fail() will be used to kill the test.
	 * <p/>
	 * <b>NOTE:</b> If you don't want to send any parameters with the interaction, you can just
	 * pass <code>null</code> for the <code>parameters</code> argument.
	 * 
	 * @param clazz The name of the interaction type you wish to send
	 * @param parameters The set of parameters, with the keys being the parameter names and the
	 * value being the desired values to send out to the federation
	 * @param tag The tag to send with the interaction
	 */
	public void quickSend( String clazz, Map<String,byte[]> parameters, byte[] tag )
	{
		try
		{
			// resolve the class name //
			InteractionClassHandle classHandle = rtiamb.getInteractionClassHandle( clazz );
			
			// resolve the names to handles //
			ParameterHandleValueMap map = TypeFactory.newParameterMap();
			if( parameters != null )
			{
				for( String pName : parameters.keySet() )
				{
					map.put( rtiamb.getParameterHandle(classHandle,pName),
					         parameters.get(pName) );
				}
			}
			
			// send the interaction //
			rtiamb.sendInteraction( classHandle, map, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending interaction: " + e.getMessage(), e );
		}
	}
	
	/**
	 * The same as {@link #quickSend(String, Map, byte[])} except that it works with handles rather
	 * than class/parameter names.
	 */
	public void quickSend( int classHandle, Map<Integer,byte[]> parameters, byte[] tag )
	{
		try
		{
			// create the parameter set to send //
			ParameterHandleValueMap map = TypeFactory.newParameterMap();
			if( parameters != null )
			{
				for( Integer key : parameters.keySet() )
				{
					map.put( TypeFactory.getParameterHandle(key), parameters.get(key) );
				}
			}

			// send the interaction //
			rtiamb.sendInteraction( TypeFactory.getInteractionHandle(classHandle), map, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending interaction: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method is much the same as {@link #quickSend(String, Map, byte[])}, except that it
	 * *expects failure*. If the request to send the interaction does not fail, Assert.fail() is
	 * used to kill the test. If the request does fail, the method returns as normal.
	 */
	public void quickSendFail( String clazz, Map<String,byte[]> parameters, byte[] tag )
	{
		// declare what we need out here as it will be required across two scopes
		InteractionClassHandle classHandle = null;
		ParameterHandleValueMap map = null;
		
		// set up the parameter values to send
		try
		{
			// resolve the class name //
			classHandle = rtiamb.getInteractionClassHandle( clazz );
			
			// resolve the names to handles //
			map = TypeFactory.newParameterMap();
			if( parameters != null )
			{
				for( String pName : parameters.keySet() )
				{
					map.put( rtiamb.getParameterHandle(classHandle,pName), parameters.get(pName) );
				}
			}
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception setting up quickSendFail: " + e.getMessage(), e );
		}
		
		// do the actual sent - this is where we should fail
		try
		{
			// send the interaction //
			rtiamb.sendInteraction( classHandle, map, tag );
			Assert.fail( "Was expecting the sending of interaction [" + clazz + "] to fail" );
		}
		catch( Exception e )
		{
			// success!
		}
	}
	
	/**
	 * This method tries to send an interaction of the given class with the given parameters at
	 * the given time. The value for the parameters is equal to the byte[] value of the parameter
	 * names (as gotten from String.getBytes()). If there is a problem resolving the name to handles
	 * or an exception is thrown while trying to send, the current test will be failed with
	 * Assert.fail().
	 */
	public void quickSend( String clazz, double time, String... parameters )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String parameter : parameters )
			map.put( parameter, parameter.getBytes() );
		
		this.quickSend( clazz, map, "letag".getBytes(), time );
	}
	
	/**
	 * The same as {@link #quickSendFail(String, Map, byte[])}, except that it works with handles,
	 * rather than class/parameter names.
	 */
	public void quickSendFail( int clazz, Map<Integer,byte[]> parameters, byte[] tag )
	{
		// set things up
		ParameterHandleValueMap map = TypeFactory.newParameterMap();
		if( parameters != null )
		{
			for( Integer key : parameters.keySet() )
			{
				map.put( TypeFactory.getParameterHandle(key), parameters.get(key) );
			}
		}
		
		// do the actual sent - this is where we should fail
		try
		{
			
			// send the interaction //
			rtiamb.sendInteraction( TypeFactory.getInteractionHandle(clazz), map, tag );
			Assert.fail( "Was expecting the sending of interaction [" + clazz + "] to fail" );
		}
		catch( Exception e )
		{
			// success!
		}
	}
	
	/**
	 * This method is the same as {@link #quickSend(String, Map, byte[])} except that it
	 * will send the message with the given time value.
	 */
	public void quickSend( String clazz, Map<String,byte[]> params, byte[] tag, double time )
	{
		try
		{
			// resolve the class name //
			InteractionClassHandle classHandle = rtiamb.getInteractionClassHandle( clazz );
			
			// resolve the names to handles //
			ParameterHandleValueMap map = TypeFactory.newParameterMap();
			if( params != null )
			{
				for( String pName : params.keySet() )
				{
					map.put( rtiamb.getParameterHandle(classHandle,pName), params.get(pName) );
				}
			}
			
			// send the interaction //
			rtiamb.sendInteraction( classHandle, map, tag, TypeFactory.createTime(time) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending interaction: " + e.getMessage(), e );
		}		
	}

	/////////////////////////////////////////////////////////////
	///////////////// Ownership helper methods //////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * This method returns true if the federate is the owner of the specified attribute, false
	 * otherwise. It calls <code>isAttributeOwnedByFederate</code> on the RTIambassador and fails
	 * the test if there is a problem invoking it.
	 */
	public boolean quickIsOwned( int objectHandle, int attributeHandle )
	{
		try
		{
			return rtiamb.isAttributeOwnedByFederate( TypeFactory.getObjectHandle(objectHandle),
			                                          TypeFactory.getAttributeHandle(attributeHandle) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during attribute owner lookup: "+e.getMessage(), e );
			return false; // will never get here, but the method needs it anyway
		}
	}

	/////////////////////////////////////////////////////////////
	//////////////// Time related helper methods ////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Turns on time constrained and blocks until either the callback is received or a timeout
	 * occurs. If there is a timeout, Assert.fail is used to kill the test.
	 */
	public void quickEnableConstrained()
	{
		try
		{
			rtiamb.enableTimeConstrained();
			fedamb.waitForConstrainedEnabled();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickEnableConstrained()", e );
		}
	}
	
	/**
	 * Requests that time constrained become enabled (this only issues the request, it will not
	 * wait for the callback). If there is an exception in this call, Assert.fail is used to kill
	 * the test.
	 */
	public void quickEnableConstrainedRequest()
	{
		try
		{
			rtiamb.enableTimeConstrained();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickEnableConstrainedRequest()" );
		}
	}
	
	/**
	 * This method will disable time constrained and set the appropriate value in the federate
	 * ambassador. If there is an exception, Assert.fail() is used to fail the test.
	 */
	public void quickDisableConstrained()
	{
		try
		{
			rtiamb.disableTimeConstrained();
			fedamb.constrained = false; // set manually, there is no callback
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickDisableConstrained()", e );
		}
	}
	
	/**
	 * Turns on time regulation and blocks until either the callback is received, or a timeout
	 * occurs while waiting nfor it. If there is an exception, Assert.fail is used to kill the test
	 */
	public void quickEnableRegulating( double lookahead )
	{
		try
		{
			LogicalTimeInterval interval = TypeFactory.createInterval( lookahead );
			rtiamb.enableTimeRegulation( interval );
			
			// wait for the callback
			fedamb.waitForRegulatingEnabled();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickEnableRegulating()", e );
		}
	}

	/**
	 * This method is much like {@link #quickEnableRegulating(double)}, except that it only issues
	 * the request, it won't wait for regulating to become enabled before returning. If there is
	 * a problem issuing the regulating request, Assert.fail will be used to kill the test.
	 */
	public void quickEnableRegulatingRequest( double lookahead )
	{
		try
		{
			LogicalTimeInterval interval = TypeFactory.createInterval( lookahead );
			rtiamb.enableTimeRegulation( interval );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickEnableRegulatingRequest()", e );
		}
	}
	
	public void quickDisableRegulating()
	{
		try
		{
			rtiamb.disableTimeRegulation();
			fedamb.regulating = false; // set manually, there is no callback
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickDisableRegulating()", e );
		}
	}
	
	public double quickQueryLookahead()
	{
		try
		{
			return TypeFactory.fromInterval( rtiamb.queryLookahead() );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickQueryLookahead()", e );
			return -1.0; // won't actually execute, but need it to keep the compiler happy
		}
	}
	
	public void quickModifyLookahead( double newLookahead )
	{
		try
		{
			rtiamb.modifyLookahead( TypeFactory.createInterval(newLookahead) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickModifyLookahead()", e );
		}
	}
	
	/**
	 * Request a time advance to the given time. If there is a problem, Assert.fail() will be used
	 * to kill the test.
	 */
	public void quickAdvanceRequest( double newTime )
	{
		try
		{
			rtiamb.timeAdvanceRequest( TypeFactory.createTime(newTime) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAdvanceRequest(" + newTime + ")", e );
		}
	}
	
	/**
	 * Same as {@link #quickAdvanceRequest(double)}, except that it calls
	 * <code>timeAdvanceRequestAvailable()</code>, rather than <code>timeAdvanceRequest()</code>.
	 */
	public void quickAdvanceRequestAvailable( double newTime )
	{
		try
		{
			rtiamb.timeAdvanceRequestAvailable( TypeFactory.createTime(newTime) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAdvanceRequestAvailable(" + newTime + ")", e );
		}
	}
	
	/**
	 * Same as {@link #quickAdvanceRequest(double)}, except that it calls
	 * <code>nextEventRequest()</code>, rather than <code>timeAdvanceRequest()</code>.
	 */
	public void quickNextEventRequest( double maxTime )
	{
		try
		{
			rtiamb.nextMessageRequest( TypeFactory.createTime(maxTime) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickNextEventRequest(" + maxTime + ")", e );
		}
	}
	
	/**
	 * This method builds on {@link #quickAdvanceRequest(double)} by making a time advancement
	 * request and then waiting blocking until either the advance comes through, or a timeout
	 * occurs.
	 */
	public void quickAdvanceAndWait( double newTime )
	{
		// make the advancement request
		this.quickAdvanceRequest( newTime );
		
		// wait for the advance to be granted
		fedamb.waitForTimeAdvance( newTime );
	}

	/**
	 * Issues a flushQueueRequest() on the RTIambassador and kills the current test if this call
	 * results in an exception.
	 */
	public void quickFlushQueueRequest( double upToThisTime )
	{
		try
		{
			rtiamb.flushQueueRequest( TypeFactory.createTime(upToThisTime) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during flushQueueRequest("+upToThisTime+")", e );
		}
	}

	////////////////////////////////////////////////////////////
	/////////////// Asynchronous Delivery Methods //////////////
	////////////////////////////////////////////////////////////
	/**
	 * This method enabled asynchronous delivery (because it's annoying). If there is an exception
	 * during this process, Assert.fail() is used to kill the test.
	 */
	public void quickEnableAsyncDelivery()
	{
		try
		{
			rtiamb.enableAsynchronousDelivery();
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception enabling async delivery: " + e.getMessage(), e );
		}
	}
	
	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * This method will call the RTIambassador tick() call. If there is an exception, Assert.fail()
	 * will be used to kill the test.
	 */
	public void quickTick()
	{
		try
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.1 );
		}
		catch( Exception e )
		{
			Assert.fail( "There was an exception while tick()'ing: " + e.getMessage(), e );
		}
	}

	/**
	 * This method will call the RTIambassador tick(double,double) call. If there is an exception,
	 * Assert.fail() will be used to kill the test.
	 */
	public void quickTick( double min, double max )
	{
		try
		{
			rtiamb.evokeMultipleCallbacks( min, max );
		}
		catch( Exception e )
		{
			Assert.fail( "There was an exception while tick(min,max)'ing: " + e.getMessage(), e );
		}
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
