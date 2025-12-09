@echo off
echo Actualizando contraseña del administrador...
cd C:\xampp\mysql\bin
mysql -u root eventos_db < d:\Proyecto-Grupal-Martin-RamaAle\fix_admin_password.sql
echo.
echo Contraseña actualizada. Ahora puedes iniciar sesión con:
echo Email: admin@eventos.com
echo Password: admin123
echo.
pause
