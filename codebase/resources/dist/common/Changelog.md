Changelog
====================

Portico is an open source, cross platform, freely redistributable HLA RTI.
This file records the changes made to Portico over successive releases.
For each release we also prepare release notes that are hosted on the 
website:

   `http://www.porticoproject.org/about/status.html#release-notes`

In this document, changes are broken down by their type:

 * `feature`  -- Describes the addition of a new feature
 * `improve`  -- Describes an improvement to existing features
 * `bugfix`   -- Describes the remediation of a defect
 * `note`     -- Special information end-users should pay attention to

At the end of each note is the JIRA ticket number relating to the item,
or the GitHub pull-request number for community-sourced updates.

 * (`PORT-XYZ`) -- JIRA ticket number (no longer used)
 * (`#XYZ`)     -- GitHub issue number, see `https://github.com/openlvc/portico/issues`


v2.1.3 / 2025-05-04
--------------------

#### Special Note: Compiler and Operating System Support
It has been a long time between releases! 

The goal of this release is two fold:
 - Update compiler and operating system support to focus on middle-life or recent EOL versions 
 - Fix a number of small open bugs logged against the earlier releases

From here, support for new compilers and operating systems will not be added. Bug fixes are
also expected to be minimal, and only where critically necessary. The hope is that this 
baseline will give users with established systems version of the RTI that they can use without
having to jump to new compilers/operating systems.

The compilers targeted and test with for this release are:

 - Java 11
 - Visual Studio 2017, 2019 and 2022
 - GCC 5.4

The Operating Systems tested on are:

 - Windows 10 and 11
 - Ubuntu 16.04, 18.04 and 20.04
 - Rocky Linux 8.10 and 9.5

All binary packages are now 64-bit only. If you require 32-bit support for a specific platform,
please contact the development team and we will see what we can do to point you compile one up.

#### Notes

- `note` The bundled Java version is `11.0.21`
- `note` Support for Visual Studio 10, 12, 13 and 15 has been deprecated. Libraries are no longer
         shipped with the installer packages.
- `note` Only 64-bit versions of libraries and installers are shipped.

#### New Features

 - `feature` C++ libraries now shipped for Visual Studio 2017 (`vc14_1`), 2019 (`vc14_2`) and 2022 (`vc14_3`)
 - `feature` Java libraries now compiled against Java 11. The bundled JRE is Java 11.
 - `feature` Builds are now tested (unit tests and example tests) on Rocky Linux as a replacement
             for CentOS, and to validate RHEL compatiblity. Rocky Linux 8.10 and 9.5 have been used.

#### Improvements

 - `improve` The version of Log4j in the Portico jar has been updated to `2.24.3` (`#343`)

#### Bugs

 - `bugfix` Fixed 64-bit C++ library naming convention for HLA v1.3. Convetion below. (`#342`)
    - **HLA v1.3**: libRTI-NG_64.xyz (use _64)
    - **IEEE 1516 (2000)**: librti1516_64.xyz (use _64)
    - **IEEE 1516 (2010)**: librti1516e64.xyz (use 64 with no _)
 - `bugfix` Fixed MOM elements not being added to HLA v1.3 federations that didn't include it in FOM (`#327`)
 - `bugfix` Fixed `ErrorReadingFDD` exception when parsing Variant Records in 1516e FOMs (`#214`)
 - `bugfix` Fixed crash in 1516e C++ interface when reading some tag values (`#202`)
 - `bugfix` Fixed NPE when merging 1516e FOM modules (`#181`)
 - `bugfix` Fixed encoding/decoding errors for 1516e Variant Records (`#179`)
 - `bugfix` Fixed crash in C++ federates if `JAVA_HOME` env var not set (`#137`)
 - `bugfix` Fixed possible double-discovery of objects by late joining federates when they pubsub (`#116`)
 - `bugfix` Fixed NPE when Portico attempts callback for failed sync point registration (`#83`)


v2.1.0 / 2016-05-04
--------------------

#### Notes

 - `note` The bundled JRE version is `1.8.0_66`
 - `note` Support for Visual Studio 8 (2005) and Visual Studio 9 (2008) has been deprecated.
          Libraries generated for these compilers will be removed in the next version.

#### New Features

 - `feature` Added support bridging single federates into a federation via point-to-point link (`#44`)
 - `feature` Added support for connecting clusters of federations via point-to-point link (`#135`)
 - `feature` Updated MOM facilities to work in 1516e as expected (`#55`)
 - `feature` Added Federation Auditor which will log messages exchanged by a federate including some
             metadata about those messages (`#43`)

#### Improvements

 - `improve` Improved the way messages are handled when federate is overloaded, causing reduction
             in lost packets (`#63`)
 - `improve` Improvments to make the loading of federate internal message handlers more robust (`#75`)
 - `improve` Added a summary-mode to the Portico federate message Auditor (`#64`)
 - `improve` Improved logging of Auditor to include current date (`#62`)
 - `improve` Updated README to include updated information for the configuration/deployment of Portico (`#50`)
 - `improve` Improved the Auditor to contain information breakdowns for sent/received Interactions (`#58`)
 - `improve` Updated C++ example federates on Linux to remove warnings (`#160`)
 - `improve` Updated C++ example federate packages on Mac OSX and Linux to provide customized environment
             files for use with GDB (`#162`)
 - `improve` Portico will now appear properly in Add/Remove Programs list on Windows (`#132`)
 - `improve` Added support for parallel C++ compilation with automatic detection of optimal number of
             threads based on system CPU count. Build-time speedup of 30% (`#79`)

#### Bugs

 - `bugfix` Fixed problem with rapid ownership exchange and re-exchange among federates (`#166`)
 - `bugfix` `evokeSingleCallback(double)` will now wait appropriate amount of time if there are
   no messages to immediately process (`#118`)
 - `bugfix` Fixed problem causing truncation of attributes or interaction params over `986b` in size (`#65`)
 - `bugfix` Fixed problem with HLA-Evolved FOM parser not recognizing some transport and order
   properties unless they were defined as child elements (`#163`)
 - `bugfix` Fixed potential hang when calling `disconnect()` in HLA-Evolved interface (`#118`)
 - `bugfix` Fixed sporadic exceptions that sometimes happened when federates resigned (`#54`)
 - `bugfix` Fixed problem with incorrect DLL naming for `64-bit` libraries (`#59`)
 - `bugfix` Fixed a problem that prevented automatic crash identification for lost federates (`#162`, `#126`)
 - `bugfix` Fixed problem initializing Portico in Java-based web applications (`#157`)
 - `bugfix` Fixed `LogicalTimeFactoryFactory` loading of `HLAinteger64Time` (`#142`)
 - `bugfix` Fixed linker error for HLA 1.3 C++ test suite on Windows (`#124`)
 - `bugfix` Fixed bug with incorrect modification of `UpdatesAttributes` objects if JVM communications
   binding is used (`#120`)
 - `bugfix` Fixed compile warnings in HLA 1.3 C++ test suite under Ubuntu 14.04 (GCC 4.8) (`#99`)
 - `bugfix` Fixed problem that was adding erronous null characters to end of string encoded for use
   with MOM attributes in `1516` and `1516e` interfaces (`#105`)
 - `bugfix` Fixed a problem where MOM infrastructure would serve MOM updated even if it was disabled (`#108`)
 - `bugfix` Fixed logging inside federation restore `RTIambassador` method (`#76`)


v2.0.2 / 2015-12-31
--------------------

#### Notes

 * `note` Bundled JRE version has been updated to `1.8.0_66`

#### Improvements

 * `improve` Fixed a problem that made Portico difficult to use in private Maven repos.
             This would manifest as extremely long startup time as a large number of
             non-Portico related jar files were unnecessarily scanned. (`#148`)

 * `improve` Added ability to generate a sandbox that includes a JRE by calling the
             `sandbox.full` target. This is useful when building from source for use
             with `RTI_HOME` directly set to the sandbox directory. (`#130`)

#### Bug Fixes

 * `bugfix` Fixed a bug that would cause the truncation of values for large attribute
            reflections or parameters sent. If a value of an individual attributes or
            parameter was >`989b`, all values after the 989th byte were being trucated
            to `0`. (#65)

 * `bugfix` Fixed a problem preventing use of Portico with time managed federates
            generated by Pitch Developer Studio (`#41`)

 * `bugfix` Fixed a problem where calling `tick(min_time,max_time)` would not tick
            for at least `min_time` if there were messages waiting for processing.
            (`#53`)

 * `bugfix` Fixed a problem with `IEEE-1516e` Java interface referencing wrong
            `LogicalTimeFactory` implementation with Integer-based times. (`#142`)

v2.0.1 / 2014-10-22
--------------------

#### Notes

 * `note` We have updated packaged version of Java to 8u20. The Java 8 stream will
          be the default for development going forward. Generated bytecode is
          currently backwards compatible with Java 6, which reached end-of-life by
          Oracle in February 2013. Java 6 support will be deprecated as of `v2.1`.
          It may still work, but there are no guarantees made beyond that time.

#### Improvements

 * `improve` Standard development environment and packaged JRE has been updated
             to Java 8u20 on all platforms. (`PORT-149`)
 
 * `improve` Patch to allow the setting of java system properties from within
             the C++ interface. (`#30` - JeanPhilippeLebel)

 * `improve` Logging output from the 1516e interface will now be send to a file if the
            `PORTICO_REDIRECT` environment variable is set. (`#39` - raymondfrancis)

#### Bug Fixes

 * `bugfix` Reduced warnings and other compilation problems for c++ interface
            on Ubuntu LTS releases (`PORT-17`)

 * `bugfix` Portico c++ interface and unit tests now compile and complete
            successfully on Mac OS X Mavericks (10.9). (`PORT-146`)

 * `bugfix` Generated java code now backwards compatible to Java 6 (`PORT-139`)

 * `bugfix` Fixed "Insufficient data" exceptions in 1516e C++ encoding helpers (`#34` - lumixen)

 * `bugfix` Fixed encoding errors in the ieee1516e aggregate structs (`#35` - lumixen)

 * `bugfix` Fixed intermittent crash in the 1516e C++ interface that happened when multiple
            threads accessed the `RTIambassador`. Fix was also ported across to the HLA v1.3
            interface. (`#39` - raymondfrancis)

 * `bugfix` Corrected indirection for processing VariableLengthData objects in 1516e
            C++ interface (`#39` - raymondfrancis)

 * `bugfix` Fixed `TimeFactory` compatibility problem in 1516e C++ with code generated from Pitch
            Developer Studio (`#39` - raymondfrancis)

 * `bugfix` Allow absolute file URLs in FOM module specification when creating a federation
            in the 1516e interface (`#39` - raymondfrancis)



v2.0.0 / 2013-04-01
--------------------

#### New Features

 * `feature` Added an IEEE-1516e interface for Java and C++. The interface is
             available across all supported Windows, Linux and Mac OS X versions.
             The initial implementation of the interface includes support for:

     * Modular FOM Support         (`PORT-106`)
     * Synchronization Points      (`PORT-70`, `PORT-71`)
     * Publish and Subscribe       (`PORT-70`, `PORT-71`)
     * Object Management           (`PORT-70`, `PORT-71`)
     * Time Management             (`PORT-70`, `PORT-71`)
     * Ownership Management        (`PORT-70`, `PORT-71`)
     * Various Support Services    (`PORT-70`, `PORT-71`)
     * Encoding Helpers            (`PORT-92`, `PORT-111`)

 * `feature` Added support for immediate callback delivery (`PORT-120`)

 * `feature` All distributions now pre-package their own JRE. The "no-jre"
             builds, also much smaller, were not used. (`PORT-100`)

 * `feature` JGroups communications library bumped to `v3.2.0`. (`PORT-91`)

 * `feature` Individual libraries now packaged for VC8, 9 and 10 on Windows,
             GCC 4.2 on Linux and Mac OS X. Full release and debug libraries
             included with each package. (`PORT-3`)

 * `feature` Each Windows, Linux and Mac OS X package now includes an embedded
             JRE for the platform. The `no-jre` versions, although much lighter,
             were not used and have been removed. (`PORT-34`)

 * `feature` When starting Java with the Portico jar file, it will now print out
             version and system information before exiting. For example, issue
             the following from `RTI_HOME`: `java -jar lib/portico.jar` to get
             output akin to:

  ```
    ##########################################################
    #                   Portico Open RTI                     #
    #            Welcome to Portico for the HLA!             #
    #                                                        #
    #     Portico is distributed by under the terms of       #
    #    the Common Development and Distribution License.    #
    #    For a copy of the license, see the LICENSE file     #
    #     included in the root of the distributable you      #
    #                      downloaded.                       #
    ##########################################################
    #                                                        #
    #                    System Information                  #
    #                                                        #
    # Portico Version:          2.0.1 (build 0)              #
    # Platform Architecture:    amd64                        #
    # CPUs:                     2                            #
    # Operating System:         Linux                        #
    # Operating System Version: 3.13.0-32-generic            #
    # Java Version:             1.7.0_20                     #
    # Java Vendor:              Oracle Corporation           #
    #                                                        #
    # Startup Time:             21/03/2012 - 11:21:28 PM     #
    # RID File:                 RTI.rid                      #
    # Log Level:                WARN                         #
    #                                                        #
    ##########################################################
  ```

#### Improvements

 * `improve` Added explicit calls to clean-up JNI resources at the completion of
             each `RTIambassador` call. Reduces memory footprint and potential
             memory corruption bugs. (`PORT-85`)

#### Bug Fixes

 * `bugfix` Fixed problem where sent ordering was always null for all object
            management callbacks in the 1516e interface. (`PORT-128`)

 * `bugfix` Fixed naming of 1516e 64-bit libraries. (`PORT-125`)

 * `bugfix` The `enableCallbacks()` and `disableCallbacks()` methods now
            work as expected. (`PORT-122`)

 * `bugfix` When calling an unsupported method, Portico will now log a warning but
            will not throw an exception. This mode can be changed via the RID file
            property: `portico.unsupportedExceptions = true` (`PORT-121`)

 * `bugfix` When running the C++ libraries in debug mode, the java side of the
            binding will now load back the debug library also. Previously it just
            always attempted to load-back the release version making viewing
            breakpoints in callbacks a bit tough! (`PORT-123`)

 * `bugfix` Portico now properly delivers success/failure notifications for
            object name reservations. (`PORT-127`)

 * `bugfix` The C++ 1516e interface now returns the correct Portico version when
            the `RTIambassador::rtiVersion()` method is called. (`PORT-83`)

