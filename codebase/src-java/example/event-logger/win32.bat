@echo off

rem ################################
rem # check command line arguments #
rem ################################
:checkargs
if "%1" == "" goto usage

rem ######################
rem # test for JAVA_HOME #
rem ######################
set JAVA=java
set JAVAC=javac
set JAR=jar
if "%JAVA_HOME%" == "" goto nojava

rem # we must have JAVA_HOME set
set JAVA="%JAVA_HOME%\bin\java"
set JAVAC="%JAVA_HOME%\bin\javac"
set JAR="%JAVA_HOME%\bin\jar"
goto rtihometest

:nojava
echo ERROR Your JAVA_HOME environment variable is not set!
goto rtihometest

rem #####################
rem # test for RTI_HOME #
rem #####################
:rtihometest
if "%RTI_HOME%" == "" goto nortihome
if not "%RTI_HOME%" == "" goto run

:nortihome
cd ..\..\..
set RTI_HOME=%CD%
cd examples\java\event-logger
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
echo "deleting example federate jar file and left over logs"
del src\*.class
del event-logger.jar
rd /S /Q logs
goto finish

rem ############################################
rem ### (target) compile #######################
rem ############################################
:compile
echo "compiling example federate"
cd src
%JAVAC% -cp ".;%RTI_HOME%\lib\portico.jar" *.java
%JAR% -cf ..\event-logger.jar *.class
cd ..
goto finish

rem ############################################
rem ### (target) execute #######################
rem ############################################
:execute
SHIFT
%JAVA% -cp "event-logger.jar;%RTI_HOME%\lib\portico.jar" EventLogger %1 %2 %3 %4 %5 %6 %7 %8 %9
goto finish



:usage
echo usage: win32.bat [compile] [clean] [execute [federate-name]]

:finish

