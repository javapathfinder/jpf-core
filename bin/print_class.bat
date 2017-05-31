
@echo off

REM Set the JPF_HOME directory
set JPF_HOME=%~dp0..

java -classpath "%JPF_HOME%\build\jpf.jar" gov.nasa.jpf.classfile.ClassFilePrinter %*

