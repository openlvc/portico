@echo off

rem ################################
rem # check command line arguments #
rem ################################
:checkargs
if "%1" == "" goto usage

rem ######################
rem # test for JAVA_HOME #
rem ######################
if "%JAVA_HOME%" == "" goto nojava
goto rtihometest

:nojava
echo ERROR Your JAVA_HOME environment variable is not set!
goto usage

rem #####################
rem # test for RTI_HOME #
rem #####################
:rtihometest
if "%RTI_HOME%" == "" goto nortihome
if not "%RTI_HOME%" == "" goto run

:nortihome
cd ..\..\..
set RTI_HOME=%CD%
cd examples\cpp\cpp13
echo WARNING Your RTI_HOME environment variable is not set, using %RTI_HOME%
goto run

:run
if "%1" == "clean" goto clean
if "%1" == "compile" goto compile
if "%1" == "execute" goto execute

rem ############################################
rem ### (target) clean #########################
rem ############################################
:clean
echo "deleting example federate executable and left over logs"
del *.obj
del main.exe
rd /S /Q logs
goto finish

############################################
### (target) compile #######################
############################################
:compile
echo "compiling example federate"
cl /I"%RTI_HOME%\include\ng6" /DRTI_USES_STD_FSTREAM /GX main.cpp ExampleCPPFederate.cpp ExampleFedAmb.cpp "%RTI_HOME%\lib\libRTI-NG.lib" "%RTI_HOME%\lib\libFedTime.lib"
goto finish

############################################
### (target) execute #######################
############################################
:execute
SHIFT
set PATH=%JAVA_HOME%\jre\bin\client;%RTI_HOME%\bin;%PATH%
main %1 %2 %3 %4 %5 %6 %7 %8 %9
goto finish


:usage
echo usage: win32.bat [compile] [clean] [execute [federate-name]]

:finish
