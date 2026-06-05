
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   Open Windows Firewall Port 8080
echo ========================================
echo.

netsh advfirewall firewall add rule name="Glimmerseed API" dir=in action=allow protocol=TCP localport=8080
if %errorlevel% equ 0 (
    echo   [OK] Firewall rule added!
) else (
    echo   [X] Failed to add firewall rule (may already exist)
)
echo.
pause
