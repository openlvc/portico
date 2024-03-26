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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.logging.log4j.Logger;

/**
 * This class contains all the logic that does the actual saving and restoring of LRC state data
 * to and from a file. When you want to save, you give the {@link #save(Manifest, String)} method
 * a {@link Manifest} telling it which components need to save their data, and a file location to
 * save the data to. The Serializer will get an output stream to the file and then give that
 * stream to each {@link SaveRestoreTarget} inside the Manifest, tracking progress.
 * <p/>
 * The process for restoring is exactly the same, only data is read in rather than written out.
 */
public class Serializer
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
	public Serializer( Logger logger )
	{
		this.logger = logger;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Opens the file at the location provided and passes it to each of the targets in the 
	 * given manifest so that they can save their contents out. If any of the targets should
	 * fail to save (as indicated by an exception), the save processing will stop and a
	 * {@link SaveRestoreFailed} will be thrown.
	 * <p/>
	 * If the save file already exists, it will be overwritten. If there is a problem opening
	 * the file or writing to it, a SaveRestoreFailed will be thrown.
	 * 
	 * @param manifest The manifest containing references to the {@link SaveRestoreTarget}s to save
	 * @param fileLocation The location of the file to save to
	 * @throws SaveRestoreFailed If there is a problem opening the file or any of the targets fail
	 *                           when attempting to save their data out.
	 */
	public void save( Manifest manifest, String fileLocation ) throws SaveRestoreFailed
	{
		logger.debug( "Serializer: Save manifest (hash:"+manifest.hashCode()+") to "+fileLocation );
		
		//////////////////////
		// Prepare the file //
		//////////////////////
		// see if the file exists, if it does log that we're about to overwrite it
		File saveFile = new File( fileLocation );
		if( saveFile.exists() )
		{
			logger.warn( "Saving LRC state to file that exists, OVERWRITING ["+fileLocation+"]");
		}
		else
		{
			// create the file and any missing parent directories
			saveFile.getParentFile().mkdirs();
			try
			{
				saveFile.createNewFile();
			}
			catch( Exception e )
			{
				throw new SaveRestoreFailed( "Couldn't create file to save to ["+fileLocation+"]", e );
			}
		}
		
		if( saveFile.canWrite() == false )
			throw new SaveRestoreFailed( "Can't write to file "+fileLocation );
		
		// open and get an output stream around the file
		ObjectOutputStream ostream = null;
		try
		{
			ostream = new ObjectOutputStream( new FileOutputStream(saveFile) );
		}
		catch( Exception e )
		{
			throw new SaveRestoreFailed( "Problem opening file to write state data to", e );
		}
		
		///////////////////////////
		// Save data to the file //
		///////////////////////////
		// loop over each entry in the manifest and write it out
		logger.debug( "ATTEMPT Save "+manifest.size()+" targets in manifest ("+
		              manifest.hashCode()+")" );

		for( SaveRestoreTarget target : manifest )
		{
			String name = target.getClass().getSimpleName();
			
			try
			{
				logger.debug( "...saving target ["+name+"]" );
				target.saveToStream( ostream );
			}
			catch( Exception e )
			{
				throw new SaveRestoreFailed( "Internal save error: exception saving target ["+name+"]", e );
			}
		}
		
		try
		{
			// clean things up
			ostream.close();
		}
		catch( Exception e )
		{
			throw new SaveRestoreFailed( "Error closing save file ["+fileLocation+
			                             "], save not successful", e );
		}
		
		// huzzah!
		logger.debug( "SUCCESS Saved "+manifest.size()+" targets in manifest ("+
		              manifest.hashCode()+")" );
	}
	
	/**
	 * Opens the file at the given location for reading. The input stream is passed to each
	 * {@link SaveRestoreTarget} in the {@link Manifest}, allowing them to read in data that
	 * was previously persisted. If there is a problem during this process (or the file cannot
	 * be located or opened), an exception is thrown.
	 * 
	 * @param manifest Manifest containing references to all the targets that want to restore data
	 * @param fileLocation The location of the file to read LRC state data from
	 * @throws SaveRestoreFailed If there is a problem finding or reading the file, or restoring
	 *                           state data from it
	 */
	public void restore( Manifest manifest, String fileLocation ) throws SaveRestoreFailed
	{
		logger.debug( "Serializer: Restore manifest (hash:"+manifest.hashCode()+") from "+fileLocation );
		
		//////////////////////
		// Prepare the file //
		//////////////////////
		// see if the file exists, if it does log that we're about to overwrite it
		File restoreFile = new File( fileLocation );
		if( restoreFile.exists() == false )
		{
			throw new SaveRestoreFailed( "Can't load internal state data from file ["+fileLocation+
			                             "]: file does not exist" );
		}

		if( restoreFile.canRead() == false )
		{
			throw new SaveRestoreFailed( "Can't load internal state data from file ["+fileLocation+
			                             "]: can't read file" );
		}
		
		// open and get an output stream around the file
		ObjectInputStream istream = null;
		try
		{
			istream = new ObjectInputStream( new FileInputStream(restoreFile) );
		}
		catch( Exception e )
		{
			throw new SaveRestoreFailed( "Problem opening file to restore data from", e );
		}
		
		////////////////////////////////
		// Restore data from the file //
		////////////////////////////////
		// loop over each entry in the manifest and write it out
		logger.debug( "ATTEMPT Restore "+manifest.size()+" targets in manifest ("+
		              manifest.hashCode()+")" );

		for( SaveRestoreTarget target : manifest )
		{
			String name = target.getClass().getSimpleName();
			
			try
			{
				logger.debug( "...restoring target ["+name+"]" );
				target.restoreFromStream( istream );
			}
			catch( Exception e )
			{
				throw new SaveRestoreFailed( "Internal restore error: exception restoring target ["+
				                             name+"]", e );
			}
		}
		
		try
		{
			// clean things up
			istream.close();
		}
		catch( Exception e )
		{
			throw new SaveRestoreFailed( "Error closing file ["+fileLocation+
			                             "], restore not successful", e );
		}
		
		// huzzah!
		logger.debug( "SUCCESS Restored "+manifest.size()+" targets in manifest ("+
		              manifest.hashCode()+")" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
