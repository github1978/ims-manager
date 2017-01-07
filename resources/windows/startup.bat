@echo off
REM powershell -ExecutionPolicy RemoteSigned -noprofile -noninteractive -file startup.ps1
"jre/bin/java" -jar ims-manager.jar setup -mainAppName=omm -maxMemSpace=1024
exit 0