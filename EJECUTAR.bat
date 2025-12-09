@echo off
echo Iniciando Sistema de Gestion de Eventos...
cd /d "%~dp0"
call mvnw.cmd javafx:run
pause
