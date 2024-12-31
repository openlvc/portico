@echo off
rem call "%VS140COMNTOOLS%\vsvars32.bat" -- the old way
rem call "C:\Program Files\Microsoft Visual Studio\2022\Professional\Common7\Tools\vsdevcmd.bat"
call "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat"

rem %1 Solution File | path\to\solution.sln
rem %2 Build Profile | Release
rem %3 Platform      | x64
rem %4 Project       | libPortico


rem Devenv Build (Deprecated)
rem -------------------
rem devenv %1 /build "%2|%3" /project %4  <-- old way, requires active gui terminal session to work

rem Build using MSBuild
rem -------------------
rem Previously we used devenv, but this fails when running headless much of the time.
rem
rem msbuild %1 /t:%4 /p:Configuration="Release" /p:Platform="x64"
rem rebuild -- /t:%4:rebuild
rem clean   -- /t:%4:clean
rem parallel build -- /m
rem thread count -- /p:MPCommandLineOption=%5 (0 for "auto/max")

echo msbuild %1 /t:%4 /p:Configuration="%2" /p:Platform="%3" /m /p:MPCommandLineOption=0
msbuild %1 /t:%4 /p:Configuration="%2" /p:Platform="%3" /m /p:MPCommandLineOption=0
