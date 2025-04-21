@echo off

rem # Paths to the VS initialization batch files
set VS_IDE_FILE="C:\Program Files (x86)\Microsoft Visual Studio\2022\Professional\VC\Auxiliary\Build\vcvarsall.bat"
set VS_BUILDTOOLS_FILE="C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Auxiliary\Build\vcvarsall.bat"

rem ##########################################################################
rem Please consult the README file to learn more about this example federate #
rem ##########################################################################

rem ################################
rem # check command line arguments #
rem ################################
:checkargs
if "%1" == "" goto usage

rem ###################
rem # Set up RTI_HOME #
rem ###################
:rtihome
cd ..\..\..
set RTI_HOME=%CD%
cd examples\cpp\hla13
echo RTI_HOME environment variable is set to %RTI_HOME%
goto run

:run
if "%1" == "clean" goto clean
if "%1" == "compile" goto compile
if "%1" == "execute" goto execute

rem ############################################
rem ### (target) clean #########################
rem ############################################
:clean
echo Deleting example federate executable and left over logs
del *.obj
del main.exe
rd /S /Q logs
goto finish

############################################
### (target) compile #######################
############################################
:compile
echo Preparing build environment...

rem Check for Visual Studio IDE
if exist %VS_IDE_FILE% (
    echo Found Visual Studio 2019 Professional
    call %VS_IDE_FILE% amd64
    goto runcompiler
)

rem Check for Visual Studio Build Tools
if exist %VS_BUILDTOOLS_FILE% (
    echo Found Visual Studio 2019 Build Tools
   call %VS_BUILDTOOLS_FILE% amd64
    goto runcompiler
)

echo Visual Studio compiler not found. Need either Visual Studio, or the Build Tools installed
goto finish

:runcompiler
echo Compiling example federate...
cl /I"%RTI_HOME%\include\hla13" /DRTI_USES_STD_FSTREAM /EHsc main.cpp ExampleCPPFederate.cpp ExampleFedAmb.cpp "%RTI_HOME%\lib\vc14_1\libRTI-NG_64.lib" "%RTI_HOME%\lib\vc14_1\libFedTime_64.lib"
goto finish

############################################
### (target) execute #######################
############################################
:execute
SHIFT
set PATH=%RTI_HOME%\jre\bin\server;%RTI_HOME%\bin\vc14_1;%PATH%
main %1 %2 %3 %4 %5 %6 %7 %8 %9
goto finish


:usage
echo usage: win64-vc14_1.bat [compile] [clean] [execute [federate-name]]
echo  note: Script assumes you have Visual Studio, or the Visual Studio Build Tools installed in default location.

:finish
