@echo off

setlocal

cd ..
set "CURRENT_DIR=%cd%"
set "LIB_DIR=%CURRENT_DIR%\lib"
set "JAVA_COMMAND=java"
cd "%LIB_DIR%"
%JAVA_COMMAND% -jar niubi-job-cluster.jar %1

exit