/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.common.network.protocol.encryption;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

public class CryptoTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static { Security.addProvider( new BouncyCastleFipsProvider() ); }
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void run( CipherMode mode ) throws Exception
	{
		// Benchmark: We want to know the time of:
		//     - Generating the SecretKey from a given value
		//     - Creating the Cipher
		//     - Initializing the Cipher (generate new IV)
		//     - Finalizing the Encryption
		
		int iterations = 1000000;
		byte[] payload = new byte[256];
		new Random().nextBytes( payload );

		System.out.println( "================================================" );
		System.out.println( "Benchmark Starting: iterations="+iterations+
		                    ", cipher="+mode.getConfigString()+
		                    ", payload="+payload.length );

		System.out.println( "Starting warmup..." );
		
		// Populate the samples database and warm up at the same time
		List<Sample> samples = new ArrayList<Sample>( iterations );
		for( int i = 0; i < iterations; i++ )
			iteration( new Sample(), mode, payload );


		System.out.println( "Starting benchmark..." );
		
		// Do the actual run
		for( int i = 0; i < iterations; i++ )
		{
			Sample sample = new Sample();
			iteration( sample, mode, payload );
			samples.add( sample );
		}
		
		reportBenchmark( iterations, samples );
	}
	
	private final void iteration( Sample sample, CipherMode mode, byte[] payload ) throws Exception
	{
		// Start
		sample.startTime = System.nanoTime();
		
		// Step 1. Key Generation
		SecretKey key = createSecretKey( "evelyn", 128 );
		sample.keyCreate = System.nanoTime();
		
		// Step 2. Create Cipher Object
		Cipher encrypter = Cipher.getInstance( mode.getConfigString(), "BCFIPS" );
		sample.cipherCreate = System.nanoTime();
		
		// Step 3. Initialize Cipher
		encrypter.init( Cipher.ENCRYPT_MODE, key );
		sample.cipherInit = System.nanoTime();

		// Step 4. Encrypt
		encrypter.doFinal( payload, 0, payload.length, payload, 0 );
		sample.doFinal = System.nanoTime();
		if( System.currentTimeMillis() < 0 )
			System.out.println( new String(payload) ); // to prevent it being optimized away
	}
	
	private final void reportBenchmark( int iterations, List<Sample> samples )
	{
		System.out.println( "Calculating results..." );

		Sample master = new Sample();
		for( int i = 0; i < samples.size(); i++ )
		{
			Sample current = samples.get(i);
			master.keyCreate    += TimeUnit.NANOSECONDS.toMicros( current.getKeyCreateTime() );
			master.cipherCreate += TimeUnit.NANOSECONDS.toMicros( current.getCipherCreateTime() );
			master.cipherInit   += TimeUnit.NANOSECONDS.toMicros( current.getCipherInitTime() );
			master.doFinal      += TimeUnit.NANOSECONDS.toMicros( current.getEncryptionTime() );
			
			// abuse the start time as a duration counter, but let's do this one in millis
			master.startTime    += TimeUnit.NANOSECONDS.toMillis( current.getDuration() );
		}
		
		// Get the summary data by section
		double benchmarkKeyCreate    = ((double)master.keyCreate) / ((double)iterations);
		double benchmarkCipherCreate = ((double)master.cipherCreate) / ((double)iterations);
		double benchmarkCipherInit   = ((double)master.cipherInit) / ((double)iterations);
		double benchmarkDoFinal      = ((double)master.doFinal) / ((double)iterations);
		String keyCreate    = String.format( "%6.2f us/iteration", benchmarkKeyCreate );
		String cipherCreate = String.format( "%6.2f us/iteration", benchmarkCipherCreate );
		String cipherInit   = String.format( "%6.2f us/iteration", benchmarkCipherInit );
		String doFinal      = String.format( "%6.2f us/iteration", benchmarkDoFinal );
		
		// Generate the percentage data
		double totalTime = benchmarkKeyCreate + benchmarkCipherCreate + benchmarkCipherInit + benchmarkDoFinal;
		double percentageKeyCreate    = (benchmarkKeyCreate / totalTime) * 100;
		double percentageCipherCreate = (benchmarkCipherCreate / totalTime) * 100;
		double percentageCipherInit   = (benchmarkCipherInit / totalTime) * 100;
		double percentageDoFinal      = (benchmarkDoFinal / totalTime) * 100;
		
		System.out.println( "Benchmark" );
		System.out.println( "{" );
		System.out.println( "  iterations = "+iterations );
		System.out.println( "    duration = "+master.startTime+"ms" );
		System.out.println( "" );

		System.out.println( "   keyCreate = "+keyCreate   +String.format("  (%.0f%%)",percentageKeyCreate) );
		System.out.println( "cipherCreate = "+cipherCreate+String.format("  (%.0f%%)",percentageCipherCreate) );
		System.out.println( "  cipherInit = "+cipherInit  +String.format("  (%.0f%%)",percentageCipherInit) );
		System.out.println( "     doFinal = "+doFinal     +String.format("  (%.0f%%)",percentageDoFinal) );
		System.out.println( "}" );
	}

	private final SecretKey createSecretKey( String key, int bitlength )
	{
		String formatString = "%"+(bitlength/8)+"s";
		String keyString = String.format( formatString, key );
		return new SecretKeySpec( keyString.getBytes(), "AES" );
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args ) throws Exception
	{
		for( CipherMode mode : CipherMode.values() )
			new CryptoTest().run(mode);
	}
	
	public static void doIteration( String input, CipherMode mode, SecretKey key ) throws Exception
	{
		// Cipher Setup
		Cipher encrypter = Cipher.getInstance( mode.getConfigString(), "BCFIPS" );
		encrypter.init( Cipher.ENCRYPT_MODE, key );

		Cipher decrypter = Cipher.getInstance( mode.getConfigString(), "BCFIPS" );
		decrypter.init( Cipher.DECRYPT_MODE, key, new IvParameterSpec(encrypter.getIV()) );
		
		// Encrypt/Decrypt -- Copies
		//byte[] payload   = input.getBytes();
		//byte[] encrypted = encrypter.doFinal(payload);
		//byte[] decrypted = decrypter.doFinal(encrypted);
		//String output    = new String( decrypted );
		
		// Encrypt/Decrypt -- Change in place
		byte[] payload = input.getBytes();
		encrypter.doFinal( payload, 0, payload.length, payload, 0 );
		decrypter.doFinal( payload, 0, payload.length, payload, 0 );
		String output = new String( payload );
		
		// Test
		if( input.equals(output) == false )
			throw new RuntimeException( "Decryption failed" );

		System.out.println( "Input : "+input );
		System.out.println( "Output: "+output );
		System.out.println( "Equal : "+(input.equals(output)) );
		System.out.println( "" );
		System.out.println( "Input    : "+input.length() );
		System.out.println( "Encrypted: "+payload.length );
		System.out.println( "Output   : "+output.length() );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Private Inner Class: Sample   /////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private class Sample
	{
		long startTime;
		long keyCreate;
		long cipherCreate;
		long cipherInit;
		long doFinal;
		
		public long getDuration()         { return doFinal-startTime; }
		public long getKeyCreateTime()    { return keyCreate-startTime; }
		public long getCipherCreateTime() { return cipherCreate-keyCreate; }
		public long getCipherInitTime()   { return cipherInit-cipherCreate; }
		public long getEncryptionTime()   { return doFinal-cipherInit; }
	}

	
}
