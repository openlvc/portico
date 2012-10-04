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
package org.portico.lrc.model;

import java.util.List;
import org.apache.log4j.Logger;

/**
 * This class provides the logic for merging multiple {@link ObjectModel}s together into a
 * single, unified object model as required by IEEE-1516e. This behaviour will just arbitrarily
 * merge together modules as the 1516e specification demands, even if the models are for earlier
 * HLA versions.
 */
public class ModelMerger
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ModelMerger()
	{
		this.logger = Logger.getLogger( "portico.lrc" );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Model merging is not supported at the moment. For now we just return the first module
	 * in the list and ignore the others.
	 */
	private ObjectModel mergeModels( List<ObjectModel> models )
	{
		// if we only got one model, there's nothing to merge!
		if( models.size() == 1 )
			return validate( models.get(0) );

		logger.debug( "Merging "+models.size()+" different FOM models" );
		logger.warn( "FOM Module merging is not yet supported. Using first module only." );
		return validate( models.get(0) );
	}

	private ObjectModel validate( ObjectModel model )
	{
		// ensure that HLAobjectRoot is present
		// ensure that HLAinteractionRoot is present
		return model;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Model merging is not supported at the moment. For now we just return the first module
	 * in the list and ignore the others.
	 */
	public static ObjectModel merge( List<ObjectModel> models )
	{
		return new ModelMerger().mergeModels( models );
	}
}
