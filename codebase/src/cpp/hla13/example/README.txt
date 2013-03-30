
                   (HLA v1.3) The Portico C++ Example Federate
				
This file contains information on the HLA v1.3 Portico C++ example federate and how
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

 The Portico C++ example federate is supported on:

  * Ubuntu 12.04 LTS for 32 and 64-bit
  * Mac OS X Mountain Lion (10.8)+ for 64-bit
  * Windows 7 32/64-bit with Visual Studio 10

-------------------------------------------------------------------------------
 3. Compiling and Running the Example Federate
-------------------------------------------------------------------------------

 To help with the process of compilng and running the federate, a number
 of helper scripts have been provided:
 
   * win32-vc10.bat    // Compile and run on Windows 32-bit
   * win64-vc10.bat    // Compile and run on Windows 64-bit
   * linux32.sh        // Compile and run on Ubuntu 12.04 LTS x86
   * linux64.sh        // Compile and run on Ubuntu 12.04 LTS amd64
   * macos.sh          // Compile and run on Mac OS X 10.8+

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
 
   * [Open Visual Studio 2010 Command Prompt]
   * cd %RTI_HOME%\examples\cpp\hla13
   * winXX-vc10 compile                        // win32-vc10 or win64-vc10
   * winXX-vc10 execute                        // win32-vc10 or win64-vc10                  

 Running on Unbuntu 12.04 LTS
 -----------------------------
 To run on Ubuntu, ensure you have g++ installed and then open a command prompt,
 change into the directory, compile and run using the script:

   * cd $RTI_HOME/examples/cpp/hla13
   * ./linux64.sh compile
   * ./linux64.sh execute

 Running on Mac OS X 10.8+
 --------------------------
 To run on Mac OS X, ensure you have the XCode tools (gcc) installed, open a
 termincal and then run the helper script:

   * cd $RTI_HOME/examples/cpp/hla13
   * ./macos.sh compile
   * ./macos.sh execute

 Running more than one Federate
 -------------------------------
 To watch more than one federate interact, you can issue a second command to
 the "execute" for each of the helper scripts. This will be the federate name:
 
   * Start the first example federate (don't hit enter when it says so)
   * Start a second example federate
      * winXX-vc10 execute secondFederate
      * ./linux64.sh execute secondFederate
      * ./macos.sh execute secondFederate
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
 Visual Studio you use. Libraries for VC10 (2010), VC9 (2008) and VC8 (2005)
 are provided. See the appropriate subdirectories of [RTI_HOME]\bin and lib.

 Resources to get you started
 -----------------------------
 If you are learning the HLA for the first time, there are a number of
 resources available:
 
   * Ask your question on the Portico forums at http://forums.porticoproject.org 
   * Pitch Technologies (Commercial RTI vendor) has produced an "HLA Tutorial"
     that can be freely downloaded: http://www.pitch.se/hlatutorial
