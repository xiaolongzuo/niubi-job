@echo off

setlocal

set "BOOTSTRAP_BAT=bootstrap.bat"
call "%BOOTSTRAP_BAT%" start
echo LIB_DIR:"%LIB_DIR%"
echo JAVA_HOME:"%JAVA_HOME%"
echo niubi-job has been started...

exit