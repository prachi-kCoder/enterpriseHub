@echo off
setlocal
cd /d "%~dp0"
start "EnterpriseHub API" /D "%~dp0backend" cmd /k "set SPRING_PROFILES_ACTIVE=local && mvn spring-boot:run"
call frontend\start.bat
endlocal
