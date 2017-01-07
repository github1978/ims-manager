@echo off
REM powershell -ExecutionPolicy RemoteSigned -noprofile -noninteractive -file shutdown.ps1
"jre/bin/java" -jar ims-manager.jar shutdown
if exist db (cd db&start /b manager-shutdown.bat) 
exit 0