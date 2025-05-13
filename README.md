Welcome to Portico
===================
Portico is an open source [HLA Run-Time Infrastructure](https://en.wikipedia.org/wiki/High-level_architecture)
(RTI) implementation. It is designed to ensure open and free access to the necessary infrastructure
needed to drive HLA federations.

Portico is released under the terms of the
**[Common Distribution and Developer License (CDDL)](https://opensource.org/licenses/CDDL-1.0)**,
which means you can repackage and redistribute it with your applications, and you can modify
the source as long as changes are submitted back to the project.

### Website
Documentation and open source is always difficult. We have a data dump of our older website
which had some useful (but old) documentation. You can find a Git repo containing those 
documents here: <https://github.com/openlvc/portico-oldwebsite>.

This readme will be updated when new documentation is posted to <https://porticoproject.org>.

### Table of Contents
  1. Getting Started - What is in the package
  2. Running Federates - How to use Portico
  3. Writing Federates - How to develop with Portico
  4. Example Federates
  5. Getting Help


1. Getting Started
-------------------
  This guide gives you some quick heads-up information to help you get started with
  the Portico open source RTI. Portico supports several of the standard HLA interfaces:
    - HLA v1.3:   Java and C++
    - IEEE-1516:  Java
    - IEEE-1516e: Java and C++

  **For Java**
  Portico ships with a JRE, but you will need a JDK if you are writing federates.
  Depending on the version of Portico, different versions of Java are supported:

   - Current `master`: Java 21
   - Branch `maintenance-2.2.x`: Java 21
   - Branch `maintenance-2.1.x`: Java 11
   - Branch `maintenance-2.0.x`: Java 8

  **For C++**
  Portico provides C++ libraries for both Windows and Linux, and in 64-bit only. Libries for the 
  following compilers are pre-packaged depending on the operating system you download for:

    - Windows
       - `master`: Visual Studio 2022
       - `v2.2.x`: Visual Studio 2022
       - `v2.1.x`: Visual Studio 2017, 2019, 2022
       - `v2.0.x`: Visual Studio 2005, 2008, 2010 (32/64-bit)
    - Linux
       - `master`: TBC - Currently GCC 11.2
       - `v2.2.x`: GCC 11.2 (RHEL 9.5, Ubuntu 22.04, 24.04)
       - `v2.1.x`: GCC 5.4 (RHEL 8.10, Ubuntu 16.04, 18.04, 20.04)
       - `v2.0.x`: GCC 4.4 (RHEL 6.5, Ubuntu 12.04, 14.04)

  When you have installed Portico, you will get a directory structure like so:

  ```
  [RTI_HOME]
    |-- LICENSE.portico
    |-- README               (This file)
    |-- README-examples      (More information about the example federates)
    |-- SOURCE_CODE          (Details about where to get the source code)
    |-- examples
        `-- java
            `-- hla13        (The Java HLA v1.3 example federate)
            `-- ieee1516e    (The Java IEEE-1516e example federate)
        `-- cpp
            `-- hla13        (The C++ HLA v1.3 example federate)
            `-- ieee1516e    (The C++ IEEE-1516e example federate)
    |-- include
        `-- hla13            (HLA v1.3 headers)
        `-- dlc13            (DLC v1.3 headers) [*nix only]
        `-- ieee1516e        (IEEE-1516e headers)
    |-- lib
        |-- portico.jar      (The main Portico jar file)
        `-- gcc8             (GCCx libraries for C++ interfaces)  [*nix only]
        `-- vc14_3           (VS22 libraries for C++ interfaces)  [windows only]
        `-- vc14_2           (VS19 libraries for C++ interfaces)  [windows only]
        `-- vcXX             (VSxx libraries for C++ interfaces)  [windows only]
    |-- bin
        |-- wanrounter       (Launchers for WAN Router bat/sh)
        `-- vc14_3           (VS22 DLLs)                          [windows only]
        `-- vc14_2           (VS19 DLLs)                          [windows only]
        `-- vcXX_x           (VSxx DLLs)                          [windows only]
  ```

2. Running Federates with Portico
----------------------------------
  For the most part, Portico is a drop-in replacement for any other RTI. To use Portico at
  runtime you need to set your environment variables such that the Portico libraries can be
  found on your path.

  Portico also uses the `RTI_HOME` environment variable as is convention for RTIs. If you are
  only use Java federates, this is the only variable you need to set. The others are necessary
  for C++/native federates as listed:

  ```
  *   (all) RTI_HOME          : This should point to the same directory that this file is in
  *  (java) CLASSPATH         : Add RTI_HOME/lib/portico.jar
  * (win64) PATH              : Add %RTI_HOME%\bin\[compiler] and %RTI_HOME%\jre\bin\server
  * (lin64) LD_LIBRARY_PATH   : Add $RTI_HOME/lib/[compiler] and $RTI_HOME/jre/lib/server
  ```

### The JVM Libraries
  Portico is a Java-based RTI. The C++ interface is a wrapper around the core Java library.
  When you start a C++ federate, the Java Virtual Machine is loaded in the background.
  This is why you must add the second folder to your path as listed above.


3. Writing Federates for Portico
---------------------------------
  Portico ships with some example federates (more below) to both show users how to set up
  their environment correctly, and to provide some sample HLA code to get you started.
  For complete instructions on writing federates, please see the website:

  <https://porticoproject.org/documentation/developer/>

  **NOTE** See the documentation link above referencing the availability of older documentation.


### Writing Java Federates
  All Portico code and all its dependencies are located in a single jar file. Just throw
  `RTI_HOME/lib/portico.jar` on your classpath and you are good to go.

### Writing C++ Federates
  The Portico C++ interfaces are binary drop-in compatible with HLA v1.3 and IEEE-1516e
  federates on Windows and Linux. If you want to compile and link against Portico,
  you will need the following:

  - Header files: Located in `RTI_HOME/include/[HLA-version]`
  - Library files: Located in `RTI_HOME/lib/[compiler]`

#### Compiling on Windows
  When compiling C++ federates on Windows, you will need to have an appropriate version of
  Visual Studio _or_ the Visual Studio Build Tools installed. You can compile through the
  Visual Studio User Interface, or if you prefer the   command line, it is simplest to execute
  inside a Visual Studio Command Prompt.

#### 64-bit Libraries
  Portico provides 64-bit libraries for HLA v1.3 and 1516e. These libraries follow the
  naming convention as defined in the standards:

  - HLA 1.3: `libRTI-NG_64`, `libFedTime_64`
  - IEEE-1516e: `librti1516e64`, `libfedtime1516e64`

#### Debug Libraries
  Portico also ships with debug versions of all libraries. These are suffixed with `d`.
  For example: `librti1516e64d.so`.


4. The Example Federates
-------------------------
  For information on the example federates, see the `README-examples` file.


5. Documentation and Help
-------------------------
  Documentation and open source projects... so how about that ANY OTHER TOPIC.

### Documentation
  Luckily, Portico has been around for some time, so we're building up some devent docs.
  The main website website contains all documentation for the project. This includes
  guides on deploying Portico with common LVC tools, writing federates with Portico,
  how to build Portico for yourself, API references, support information and much more.

  <https://porticoproject.org>

  **NOTE** See the documentation link above referencing the availability of older documentation.

### Bugs and Problems
  If you have found bugs or find that Portico is not working as you would expect, you
  should open an issue via the GitHub project:

  <https://github.com/openlvc/portico>

So there you go, we hope you enjoy using Portico :)

--The Portico Development Team
