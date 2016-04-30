@echo off

setlocal

set "BOOTSTRAP_BAT=bootstrap.bat"
call "%BOOTSTRAP_BAT%" stop
echo LIB_DIR:"%LIB_DIR%"
echo JAVA_HOME:"%JAVA_HOME%"
echo niubi-job has been shutdown...

exit