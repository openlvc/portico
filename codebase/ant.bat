@echo off
rem NOTE: These scripts are just an adaptation of the Linux ones.
rem We here at the labs have an aversion to Windows and only use
rem it for testing and QA purposes. If you want to put some time
rem and effort into some really funky batch files for us, well,
rem first of all you need something else to do with your spare
rem time, but we'd still be more than happy to incorporate them
rem into the main distribution.

set DEVELOPMENT_HOME=.
set ANT_HOME=%DEVELOPMENT_HOME%\system\ant\apache-ant-1.8.4-patched
set ANT_LIB=%DEVELOPMENT_HOME%\system\ant-optional\

"%ANT_HOME%\bin\ant.bat" %* -lib %ANT_LIB%
