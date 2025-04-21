
                (IEEE-1516e) The Portico C++ Example Federate
				
This file contains information on the IEEE-1516e Portico C++ example federate and how
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
   * main.cpp                   // code to kick the federate off
   * ExampleCppFederate.cpp     // the bulk of the federate code
   * ExampleFedAmb.cpp          // example Federate Ambassador

 The bulk of the federate code is contained inside the ExampleCppFederate class.
 This class pretty basic structure and flow designed to show you, in very simple
 terms, code for performing common HLA actions. The main workflow of the federate
 is as follows:
 
   a. Create the RTIambassador
   b. Connect to the RTI
   c. Create the Federation (skip if one exists)
   d. Join the Federation
   e. Initialize all handles
   f. Announce a Synchronization Point
   g. Wait for the user to press Enter
   h. Achieve the Synchronization Point
   i. Enable federate's time settings
   j. Publish and Subscribe
   k. Register an Object Instance
   l. Loop 20 times, doing the following each step:
       * Update the object's attributes (timestampped and receive order)
       * Send an Interaction            (timestampped and receive order)
       * Request a time advance of 1.0
   m. Delete the object instance
   n. Resign from the Federation
   o. Destroy the Federation (if possible)
   p. Disconnect from the RTI

 The actual design of the federate is very basic. It just registers an object
 and loops around 20 times, updating its attributes, sending an interaction and
 moving time forward (this is actually optional).
 
 In addition to publishing information, the federate also subscribes to the same
 data. When it is run in the same federation as another example federate, any
 information it receives it will print to stdout.

-------------------------------------------------------------------------------
 2. Supported Platforms
-------------------------------------------------------------------------------

 The Portico C++ example federate is supported on:

  * Windows 11 with Visual Studio 2022
  * Ubuntu 24.04 LTS or Rocky Linux 9.5 with at least GCC 11.5

-------------------------------------------------------------------------------
 3. Compiling and Running the Example Federate
-------------------------------------------------------------------------------

 To help with the process of compilng and running the federate, a number
 of helper scripts have been provided:
 
   * win64-vc14_3.bat    // Compile and run on Windows 64-bit (Visual Studio 2022)
   * linux64.sh          // Compile and run on Linux 64-bit

 Each of the scripts takes the same basic commands:
   
   * compile           // Compile the example federate
   * execute           // Run the example federate
   * clean             // Remove all generated executables

 Running on Windows
 -------------------
 To run on Windows, you must have Visual Studio 2010 installed. To properly use
 the helper scripts you MUST open a "Visual Studio 2010 Command Prompt" (ensure
 you open the correct one for 32/64-bit depending on what you hope to compile).
 Once the prompt is open, change into the example directory and you can run the
 helper script: 
 
   * [Open Command Prompt]
   * cd %RTI_HOME%\examples\cpp\hla13
   * win64-vcXX compile                        // win64-vc14_3, win64-vc14_2
   * win64-vcXX execute                        // win64-vc14_3, win64-vc14_2                    

 Running on Unbuntu or Rocky Linux
 ----------------------------------
 To run on Linux, ensure you have g++ installed and then open a command prompt,
 change into the directory, compile and run using the script:

   * cd $RTI_HOME/examples/cpp/hla13
   * ./linux64.sh compile
   * ./linux64.sh execute

 Running more than one Federate
 -------------------------------
 To watch more than one federate interact, you can issue a second command to
 the "execute" for each of the helper scripts. This will be the federate name:
 
   * Start the first example federate (don't hit enter when it says so)
   * Start a second example federate
      * win64-vcXX execute secondFederate
      * ./linux64.sh execute secondFederate
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

 Visual Studio Compiler Support
 -------------------------------
 If you want to use a different version of the Visual Studio compiler, you can.
 You should try to match the version of the RTI libraries with the version of
 Visual Studio you use. Libraries for VC14_3 (2022) are provided.
 See the appropriate subdirectories of [RTI_HOME]\bin and lib.

 Resources to get you started
 -----------------------------
 If you are learning the HLA for the first time, there are a number of
 resources available:
 
   * Ask your question on the Portico forums at http://forums.porticoproject.org 
   * Pitch Technologies (Commercial RTI vendor) has produced an "HLA Tutorial"
     that can be freely downloaded: http://www.pitch.se/hlatutorial
