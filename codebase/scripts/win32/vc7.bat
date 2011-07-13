@echo off

set CURRENT=%CD%
set VS7_HOME=C:\Program Files\Microsoft Visual Studio .NET 2003
cd %VS7_HOME%\VC7\bin
call vcvars32.bat
cd %CURRENT%
