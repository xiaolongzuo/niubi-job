@echo off

setlocal

set "BOOTSTRAP_BAT=bootstrap.bat"
echo JAVA_HOME:"%JAVA_HOME%"
call "%BOOTSTRAP_BAT%" stop
