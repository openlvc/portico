@echo off

set CURRENT=%CD%
set VS8_HOME=C:\Program Files\Microsoft Visual Studio 8
cd %VS8_HOME%\VC\bin
call vcvars32.bat
cd %CURRENT%
