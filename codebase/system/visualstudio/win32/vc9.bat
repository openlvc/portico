@echo off

set CURRENT=%CD%
set VS9_HOME=C:\Program Files\Microsoft Visual Studio 9.0
cd %VS9_HOME%\VC\bin
call vcvars32.bat
cd %CURRENT%
