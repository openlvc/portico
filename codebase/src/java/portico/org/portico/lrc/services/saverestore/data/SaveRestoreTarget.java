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

import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Any component that needs to participate in the save/restore process must implement this
 * interface. The two methods allow each component to control what part of their state is
 * saved and to do any pre- or post-save/restore setup or cleanup.
 * <p/>
 * Additionally, any component that wants to be included in a save/restore set much register
 * itself with the {@link Serializer}, otherwise it won't
 *
 */
public interface SaveRestoreTarget
{
	/**
	 * The target should save any state it needs to be persisted to the given output stream.
	 */
	public void saveToStream( ObjectOutput output ) throws Exception;

	/**
	 * The target should restore any state it needs from the given output stream. It might also
	 * want to use this opportunity to do any re-initialization of non-persisted data items.
	 */
	public void restoreFromStream( ObjectInput input ) throws Exception;
}
