Changelog
====================

Portico is an open source, cross platform, fully support HLA RTI.
This file records the changes made to Portico over successive releases.
For each release we also prepare release notes that are hosted on the 
website:

   `http://www.porticoproject.org/about/status.html#release-notes`

In this document, changes are broken down by their type:

 * `feature`  -- Describes the addition of a new feature
 * `improve`  -- Describes an improvement to existing features
 * `bugfix`   -- Describes the remediation of a defect

At the end of each note is the JIRA ticket number relating to the item,
or the GitHub pull-request number for community-sourced updates.

 * (`PORT-XYZ`) -- JIRA ticket number, see `http://jira.openlvc.org`
 * (`#XYZ`)     -- GitHub pull request number, see `https://github.com/openlvc/portico/pull`


v2.0.1 / 2014-10-22
--------------------

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

