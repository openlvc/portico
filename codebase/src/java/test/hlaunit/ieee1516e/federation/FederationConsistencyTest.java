/*
 *   Copyright 2025 The Portico Project
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
package hlaunit.ieee1516e.federation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;

/**
 * These tests check for consistency errors between federates when participating
 * within federations. Things like handles for the same names mismatching between
 * federates.
 */
@Test(singleThreaded=true, groups={"federationConsistency", "federationManagement"})
public class FederationConsistencyTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private TestFederate thirdFederate;

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
		this.secondFederate = new TestFederate( "secondFederate", this );
		this.secondFederate.quickConnect();
		
		this.thirdFederate = new TestFederate( "thirdFederate", this );
		this.thirdFederate.quickConnect();
	}

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		// create a federation that we can test with //
		//defaultFederate.quickCreate();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResignTolerant();
		secondFederate.quickResignTolerant();
		thirdFederate.quickResignTolerant();
		defaultFederate.quickDestroy();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		secondFederate.quickDisconnect();
		thirdFederate.quickDisconnect();
		super.afterClass();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	// FOM Handle Consistency Tests ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHandlesConsistentBetweenFederates()
	{
		String moduleMain  = "resources/test-data/fom/ieee1516e/restaurant/RestaurantProcesses.xml";
		String moduleFood  = "resources/test-data/fom/ieee1516e/restaurant/RestaurantFood.xml";
		String moduleDrink = "resources/test-data/fom/ieee1516e/restaurant/RestaurantDrinks.xml";
		String moduleSoup  = "resources/test-data/fom/ieee1516e/restaurant/RestaurantSoup.xml";
		
		// 1. Create federation with the base module in the default federate
		defaultFederate.quickCreateWithModules( moduleMain );

		// 2. Join federation with additional modules in default federate
		defaultFederate.quickJoinWithModules( moduleMain, moduleFood );

		// 3. Join federation in second federate with same modules as default, but in different order
		secondFederate.quickJoinWithModules( moduleSoup );
		
		// 4. Repeat for a third federate - different module order again
		thirdFederate.quickJoinWithModules( moduleDrink );

		// 4. Compare handles from each federate to make sure they are the same
		// Object Classes and Attributes
		Map<String,Set<String>> objects = getObjectClassesToTest();
		for( String objectClass : objects.keySet() )
		{
			// check the class handle
			Assert.assertEquals( secondFederate.quickOCHandle(objectClass),
			                     defaultFederate.quickOCHandle(objectClass),
			                     "(first/second) Handles for object class "+objectClass+
			                     " don't match" );

			Assert.assertEquals( thirdFederate.quickOCHandle(objectClass),
			                     defaultFederate.quickOCHandle(objectClass),
			                     "(first/third) Handles for object class "+objectClass+
			                     " don't match" );
			
			Assert.assertEquals( secondFederate.quickOCHandle(objectClass),
			                     thirdFederate.quickOCHandle(objectClass),
			                     "(second/third) Handles for object class "+objectClass+
			                     " don't match" );

			// for each attribute, check the attribute handle
			for( String attribute : objects.get(objectClass) )
			{
				Assert.assertEquals( secondFederate.quickACHandle(objectClass,attribute),
				                     defaultFederate.quickACHandle(objectClass,attribute),
				                     "(first/second) Handles for attribute "+attribute+" in class "+
				                     objectClass+" don't match" );

				Assert.assertEquals( thirdFederate.quickACHandle(objectClass,attribute),
				                     defaultFederate.quickACHandle(objectClass,attribute),
				                     "(first/third) Handles for attribute "+attribute+" in class "+
				                     objectClass+" don't match" );

				Assert.assertEquals( secondFederate.quickACHandle(objectClass,attribute),
				                     thirdFederate.quickACHandle(objectClass,attribute),
				                     "(second/third) Handles for attribute "+attribute+" in class "+
				                     objectClass+" don't match" );
			}
		}

		// Interaction Classes and Parameters
		Map<String,Set<String>> interactions = getInteractionsToTest();
		for( String interactionClass : interactions.keySet() )
		{
			// check the class handle
			Assert.assertEquals( secondFederate.quickICHandle(interactionClass),
			                     defaultFederate.quickICHandle(interactionClass),
			                     "(first/second) Handles for interaction class "+interactionClass+
			                     " don't match" );

			Assert.assertEquals( thirdFederate.quickICHandle(interactionClass),
			                     defaultFederate.quickICHandle(interactionClass),
			                     "(first/third) Handles for interaction class "+interactionClass+
			                     " don't match" );

			Assert.assertEquals( secondFederate.quickICHandle(interactionClass),
			                     thirdFederate.quickICHandle(interactionClass),
			                     "(second/third) Handles for interaction class "+interactionClass+
			                     " don't match" );

			// for each parameter, check the parameter handle
			for( String parameter : interactions.get(interactionClass) )
			{
				Assert.assertEquals( secondFederate.quickPCHandle(interactionClass,parameter),
				                     defaultFederate.quickPCHandle(interactionClass,parameter),
				                     "(first/second) Handles for parameter "+parameter+" in class "+
				                     interactionClass+" don't match" );

				Assert.assertEquals( thirdFederate.quickPCHandle(interactionClass,parameter),
				                     defaultFederate.quickPCHandle(interactionClass,parameter),
				                     "(first/third) Handles for parameter "+parameter+" in class "+
				                     interactionClass+" don't match" );

				Assert.assertEquals( secondFederate.quickPCHandle(interactionClass,parameter),
				                     thirdFederate.quickPCHandle(interactionClass,parameter),
				                     "(second,third) Handles for parameter "+parameter+" in class "+
				                     interactionClass+" don't match" );
			}
		}
	}

	private Map<String,Set<String>> getObjectClassesToTest()
	{
		Map<String,Set<String>> map = new HashMap<>();
		
		Set<String> base = asSet( "privilegeToDelete" );
		Set<String> attributesEmployee = asSet( "PayRate", "YearsOfService", "HomeNumber", "HomeAddress" );
		Set<String> attributesWaiter = asSet( "Efficiency", "Cheerfulness", "State" );
		Set<String> attributesDrink = asSet( "NumberCups" );
		Set<String> attributesSoda = asSet( "Flavor" );
		
		// == RestaurantProcesses
		// ObjectRoot.Customer
		// ObjectRoot.Order
		// ObjectRoot.Employee				>> PayRate, YearsOfService, HomeNumber, HomeAddress, 
		// ObjectRoot.Employee.Greeter		>> (none)
		// ObjectRoot.Employee.Waiter		>> Efficiency, Cheerfulness, State, 
		// ObjectRoot.Employee.Cashier
		// ObjectRoot.Employee.Dishwasher
		// ObjectRoot.Employee.Cook
		map.put( "ObjectRoot.Customer", base );
		map.put( "ObjectRoot.Order", base );
		map.put( "ObjectRoot.Employee", union(base,attributesEmployee) );
		map.put( "ObjectRoot.Employee.Greeter", union(base,attributesEmployee) );
		map.put( "ObjectRoot.Employee.Waiter", union(base,attributesEmployee,attributesWaiter) );
		map.put( "ObjectRoot.Employee.Cashier", union(base,attributesEmployee) );
		map.put( "ObjectRoot.Employee.Dishwasher", union(base,attributesEmployee) );
		map.put( "ObjectRoot.Employee.Cook", union(base,attributesEmployee) );
		
		// == RestaurantFood
		// ObjecRoot.Food
		// ObjecRoot.Food.MainCourse
		// ObjecRoot.Food.Drink				>> NumberCups
		// ObjecRoot.Food.Appetizers
		// ObjecRoot.Food.Appetizers.Soup
		// ObjecRoot.Food.Appetizers.Nachos
		// ObjecRoot.Food.Entree
		// ObjecRoot.Food.Entree.Beef
		// ObjecRoot.Food.Entree.Chicken
		// ObjecRoot.Food.Entree.Seafood
		// ObjecRoot.Food.Entree.Seafood.Fish
		// ObjecRoot.Food.Entree.Seafood.Shrimp
		// ObjecRoot.Food.Entree.Seafood.Lobster
		// ObjecRoot.Food.Entree.Pasta
		// ObjecRoot.Food.SideDish
		// ObjecRoot.Food.SideDish.Corn
		// ObjecRoot.Food.SideDish.Broccoli
		// ObjecRoot.Food.SideDish.BakedPotato
		// ObjecRoot.Food.Dessert
		// ObjecRoot.Food.Dessert.Cake
		// ObjecRoot.Food.Dessert.IceCream
		// ObjecRoot.Food.Dessert.IceCream.Chocolate
		// ObjecRoot.Food.Dessert.IceCream.Vanilla
		map.put( "ObjectRoot.Food", base );
		map.put( "ObjectRoot.Food.MainCourse", base );
		map.put( "ObjectRoot.Food.Drink", union(base,attributesDrink) );
		map.put( "ObjectRoot.Food.Appetizers", base );
		map.put( "ObjectRoot.Food.Appetizers.Soup", base );
		map.put( "ObjectRoot.Food.Appetizers.Nachos", base );
		map.put( "ObjectRoot.Food.Entree", base );
		map.put( "ObjectRoot.Food.Entree.Beef", base );
		map.put( "ObjectRoot.Food.Entree.Chicken", base );
		map.put( "ObjectRoot.Food.Entree.Seafood", base );
		map.put( "ObjectRoot.Food.Entree.Seafood.Fish", base );
		map.put( "ObjectRoot.Food.Entree.Seafood.Shrimp", base );
		map.put( "ObjectRoot.Food.Entree.Seafood.Lobster", base );
		map.put( "ObjectRoot.Food.Entree.Pasta", base );
		map.put( "ObjectRoot.Food.SideDish", base );
		map.put( "ObjectRoot.Food.SideDish.Corn", base );
		map.put( "ObjectRoot.Food.SideDish.Broccoli", base );
		map.put( "ObjectRoot.Food.SideDish.BakedPotato", base );
		map.put( "ObjectRoot.Food.Dessert", base );
		map.put( "ObjectRoot.Food.Dessert.Cake", base );
		map.put( "ObjectRoot.Food.Dessert.IceCream", base );
		map.put( "ObjectRoot.Food.Dessert.IceCream.Chocolate", base );
		map.put( "ObjectRoot.Food.Dessert.IceCream.Vanilla", base );
		
		// == RestaurantDrinks
		// ObjecRoot.Food.Drink.Water
		// ObjecRoot.Food.Drink.Coffee
		// ObjecRoot.Food.Drink.Soda		>> Flavor
		map.put( "ObjectRoot.Food.Drink.Water", union(base,attributesDrink) );
		map.put( "ObjectRoot.Food.Drink.Coffee", union(base,attributesDrink) );
		map.put( "ObjectRoot.Food.Drink.Soda", union(base,attributesDrink,attributesSoda) );
		
		// == RestaurantSoup
		// ObjecRoot.Food.Appetizers.Soup.ClamChowder.Manhattan
		// ObjecRoot.Food.Appetizers.Soup.ClamChowder.NewEngland
		// ObjecRoot.Food.Appetizers.Soup.BeefBarley
		map.put( "ObjectRoot.Food.Appetizers.Soup.ClamChowder.Manhattan", base );
		map.put( "ObjectRoot.Food.Appetizers.Soup.ClamChowder.NewEngland", base );
		map.put( "ObjectRoot.Food.Appetizers.Soup.BeefBarley", base );

		return map;
	}

	private static Map<String,Set<String>> getInteractionsToTest()
	{
		Map<String,Set<String>> map = new HashMap<>();

		Set<String> empty = new HashSet<>();
		Set<String> paramsMainCourse = asSet( "TemperatureOk", "AccuracyOk", "TimlinessOk" );

		// ==== Interactions ====
		// == RestaurantProcesses
		// IR.CustomerTransations
		// IR.CustomerTransations.CustomerSeated
		// IR.CustomerTransations.OrderTaken
		// IR.CustomerTransations.OrderTaken.FromKidsMenu
		// IR.CustomerTransations.OrderTaken.FromAdultMeny ::YES - This is mis-spelt in the FOM
		// IR.CustomerTransations.FoodServed
		// IR.CustomerTransations.FoodServed.DrinkServed
		// IR.CustomerTransations.FoodServed.AppetizerServed
		// IR.CustomerTransations.FoodServed.MainCourseServed	>> TemperatureOk, AccuracyOk, TimlinessOk
		// IR.CustomerTransations.FoodServed.DessertServed
		// IR.CustomerTransations.CustomerPays
		// IR.CustomerTransations.CustomerPays.ByCreditCard
		// IR.CustomerTransations.CustomerPays.ByCash
		// IR.CustomerTransations.CustomerLeaves
		map.put( "InteractionRoot.CustomerTransactions", empty );
		map.put( "InteractionRoot.CustomerTransactions.CustomerSeated", empty );
		map.put( "InteractionRoot.CustomerTransactions.OrderTaken", empty );
		map.put( "InteractionRoot.CustomerTransactions.OrderTaken.FromKidsMenu", empty );
		map.put( "InteractionRoot.CustomerTransactions.OrderTaken.FromAdultMeny", empty );
		map.put( "InteractionRoot.CustomerTransactions.FoodServed", empty );
		map.put( "InteractionRoot.CustomerTransactions.FoodServed.DrinkServed", empty );
		map.put( "InteractionRoot.CustomerTransactions.FoodServed.AppetizerServed", empty );
		map.put( "InteractionRoot.CustomerTransactions.FoodServed.MainCourseServed", paramsMainCourse );
		map.put( "InteractionRoot.CustomerTransactions.FoodServed.DessertServed", empty );
		map.put( "InteractionRoot.CustomerTransactions.CustomerPays", empty );
		map.put( "InteractionRoot.CustomerTransactions.CustomerPays.ByCreditCard", empty );
		map.put( "InteractionRoot.CustomerTransactions.CustomerPays.ByCash", empty );
		map.put( "InteractionRoot.CustomerTransactions.CustomerLeaves", empty );
		
		return map;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private static Set<String> asSet( String... items )
	{
		HashSet<String> set = new HashSet<>();
		for( String item : items )
			set.add( item );
		
		return set;
	}
	
	@SafeVarargs
	private static Set<String> union( Set<String>... sets )
	{
		Set<String> union = new HashSet<>();
		for( Set<String> set : sets )
			union.addAll( set );
		
		return union;
	}
}
