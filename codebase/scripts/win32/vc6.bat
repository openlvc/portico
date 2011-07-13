@echo off

set CURRENT=%CD%
set VS6_HOME=C:\Program Files\Microsoft Visual Studio
cd %VS6_HOME%\VC98\bin
call vcvars32.bat
cd %CURRENT%
