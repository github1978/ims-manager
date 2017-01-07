@echo off
REM powershell -ExecutionPolicy RemoteSigned -noprofile -noninteractive -file startup.ps1
rem start jre/bin/javaw -jar ims-manager.jar backup -action=start -localPath=D:\temp -cron="0 17 11 * * ?"
rem start jre/bin/javaw -jar ims-manager.jar backup -action=start -localPath=D:\temp -remotePath=E:\temp -storeNum=2 -cron="0 */3 * * * ?"
rem start jre/bin/javaw -jar ims-manager.jar backup -action=start -localPath=D:\temp -remotePath=ftp:\\192.168.234.130@work@work@/home/work/temp/ -storeNum=2 -cron="0 43 1 * * ?"
start jre/bin/javaw -jar ims-manager.jar backup -action=start -localPath=D:\temp -remotePath=sftp:\\192.168.234.130@work@work@/home/work/temp/ -storeNum=5 -cron="0 */30 * * * ?"
rem start jre/bin/javaw -jar ims-manager.jar backup -action=start -localPath=D:\temp -remotePath=\\192.168.88.14\ims -cron="0 14 11 * * ?"