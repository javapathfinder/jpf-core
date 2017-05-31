REM
REM overly simplified batch file to start JPF from a command prompt
REM

@echo off

REM Set the JPF_HOME directory
set JPF_HOME=%~dp0..

set JVM_FLAGS=-Xmx1024m -ea

java %JVM_FLAGS% -jar "%JPF_HOME%\build\RunJPF.jar" %*

