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
package org.portico.lrc.compat;

public class JFederateNameAlreadyInUse extends JException
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
     * Just create an empty exception
     */
    public JFederateNameAlreadyInUse()
    {
	    super();
    }

    /**
     * @param message The message to create the exception with
     */
    public JFederateNameAlreadyInUse( String message )
    {
	    super( message );
    }

    /**
     * @param cause The cause of the exception
     */
    public JFederateNameAlreadyInUse( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public JFederateNameAlreadyInUse( String message, Throwable cause )
    {
	    super( message, cause );
    }
    
    public JFederateNameAlreadyInUse( String formatString, Object... objects )
	{
		super( String.format( formatString, objects ) );
	}
   
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
