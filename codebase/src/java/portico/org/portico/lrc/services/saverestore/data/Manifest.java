/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.saverestore.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains a list of all the {@link SaveRestoreTarget}s that should be included in any
 * save or restoration. Instances of this class can be passed to the {@link Serializer} to control
 * which components are included in a save or restore. This class also keeps track of a particular
 * save and restore, listing which components completed successfully and which didn't.
 * <p/>
 * <b>NOTE:</b> It is vitally important that the order and number of targets in a Manifest is
 *              the exact same for both saving and restoration. Each target is given a reference
 *              to an input or output stream from which they read/write as much as they want. If,
 *              for example, they read more than they wrote, that will obviously have implications
 *              on each of the components that come after it. You can tell if a manifest is equal
 *              to the other by using the {@link #hashCode()} method.
 */
public class Manifest implements Iterable<SaveRestoreTarget>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private List<SaveRestoreTarget> targets;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Manifest()
	{
		this.targets = new ArrayList<SaveRestoreTarget>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void addTarget( SaveRestoreTarget target )
	{
		this.targets.add( target );
	}

	public int size()
	{
		return this.targets.size();
	}

	public int hashCode()
	{
		// The hashcode of an array list is dependent on the contents of the list and the order
		// of those elements in the list as well. This will be different if any of the elements
		// are different or the ordering is different.
		return this.targets.hashCode();
	}
	
	public Iterator<SaveRestoreTarget> iterator()
	{
		return this.targets.iterator();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
