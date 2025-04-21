
                (IEEE-1516e) The Portico Java Example Federate
        
This file contains information on the IEEE-1516e Portico Java example federate and how
to compile and run it.

 NOTE: Knowledge of the HLA is assumed, but if you are just getting started out,
       there are some resources contained at the end of this README that you may
	     find helpful.

Table of Contents:
  1. Getting Started
  2. Supported Platforms
  3. Compiling and Running the Example Federate
  4. Writing your own Federate (with Resources)

 
-------------------------------------------------------------------------------
 1. Getting Started
-------------------------------------------------------------------------------
 This example application provides the basic template code for a very simple
 HLA federate. It is not intended to be an impression display of what you can
 do with the HLA, but rather just a display of how to use the most common parts
 of the HLA API.
 
 The example federate consists of three main files:
   * src/ieee1516e/ExampleFederate.java           // the bulk of the federate code
   * src/ieee1516e/ExampleFederateAmbassador.java // example Federate Ambassador (callbacks)
   
 The bulk of the federate code is contained inside the Example13Federate class.
 This class pretty basic structure and flow designed to show you, in very simple
 terms, code for performing common HLA actions. The main workflow of the federate
 is as follows:
 
   a. Create the RTIambassador
   b. Create the Federation (skip if one exists)
   c. Join the Federation
   d. Initialize all handles
   e. Announce a Synchronization Point
   f. Wait for the user to press Enter
   g. Achieve the Synchronization Point
   h. Enable federate's time settings
   i. Publish and Subscribe
   j. Register an Object Instance
   k. Loop 20 times, doing the following each step:
       * Update the object's attributes (timestampped and receive order)
       * Send an Interaction            (timestampped and receive order)
       * Request a time advance of 1.0
   l. Delete the object instance
   m. Resign from the Federation
   n. Destroy the Federation (if possible)

 The actual design of the federate is very basic. It just registers an object
 and loops around 20 times, updating its attributes, sending an interaction and
 moving time forward (this is actually optional).
 
 In addition to publishing information, the federate also subscribes to the same
 data. When it is run in the same federation as another example federate, any
 information it receives it will print to stdout.

-------------------------------------------------------------------------------
 2. Supported Platforms
-------------------------------------------------------------------------------

 The Portico Java example federate is officially supported on:

  * Java JDK 21
  * Ubuntu 24.04 LTS
  * Windows 11

 --- JDK Requirements ---
 Please note that you need to have a JDK installed on your system and have
 the environment variable JAVA_HOME set to point to it in order to compile
 the example federates. Portico ships with a JRE, but this only supports
 running Java applications, not compiling them.

 --- JDK Version ---
 Portico has been built against Java 11. The source compatibility level has
 been set to match this. The federate may work happily with earlier versions
 of Java, but these are not officially supported. If you are targeting earlier
 platforms you may need to recompile Portico from source.

-------------------------------------------------------------------------------
 3. Compiling and Running the Example Federate
-------------------------------------------------------------------------------

 To help with the process of compilng and running the federate, a number
 of helper scripts have been provided:
 
   * windows.bat    // Compile and run on Windows
   * linux.sh       // Compile and run on Ubuntu/Rocky Linux

 Each of the scripts takes the same basic commands:
   
   * compile           // Compile the example federate
   * execute           // Run the example federate
   * clean             // Remove all generated executables

 Running on Windows
 -------------------
 To run on Windows, you must have an appropriate JDK installed and have the
 environment variable JAVA_HOME set up to point to it. To use the helper you
 open a command prompt, change into the example directory and run the batch
 file from there:
 
   * [Open Command Prompt]
   * cd %RTI_HOME%\examples\java\ieee1516e
   * windows compile
   * windows execute

 Running on Unbuntu Linux
 --------------------------
 To run on Ubuntu, ensure you an appropriate JDK installed and have the
 environment variable JAVA_HOME set up to point at it. From a prompt,
 change into the directory, compile and run using the script:

   * cd $RTI_HOME/examples/java/ieee1516e
   * ./linux.sh compile
   * ./linux.sh execute

 Running more than one Federate
 -------------------------------
 To watch more than one federate interact, you can issue a second command to
 the "execute" for each of the helper scripts. This will be the federate name:
 
   * Start the first example federate (don't hit enter when it says so)
   * Start a second example federate
      * windows execute secondFederate
      * ./linux.sh execute secondFederate
   * Wait until the second federate is ready, then hit enter in both screens.
 
 Each example federate will wait until you hit enter to tell the RTI that the
 synchronization point it uses has been achieved. This way, you have time for
 a number of other federates to start and enter the federation. 

-------------------------------------------------------------------------------
 4. Writing Your Own Federate (with Resources)
-------------------------------------------------------------------------------

 Now that you have some grounding, you can start to write your own federates.
 To figure out which commands you need to compile with and properly link to
 the RTI, see the various execution helper scripts. You can also consult the
 documentation online at http://www.porticoproject.org.
 

