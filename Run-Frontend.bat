@echo off
cd /d "%~dp0"
java -cp ".;lib\mysql-connector-j-9.7.0.jar" Main
pause
