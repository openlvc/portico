@echo off
set DIR=%~dp0
set PORTICO_HOME=%DIR%..
"%PORTICO_HOME%\jre\bin\java" -jar "%PORTICO_HOME%\lib\portico.jar" wanrouter %*

