
@echo off
echo ========================================
echo   检查服务器端口开放状态
echo   服务器: 8.134.80.158
echo ========================================
echo.

echo [1] 检查 8080 端口 (后端 API)...
powershell -Command "Test-NetConnection -ComputerName 8.134.80.158 -Port 8080 -InformationLevel Quiet"
if %errorlevel% equ 0 (
    echo   ✓ 8080 端口已开放！
) else (
    echo   ✗ 8080 端口未开放
)
echo.

echo [2] 检查 3389 端口 (远程桌面)...
powershell -Command "Test-NetConnection -ComputerName 8.134.80.158 -Port 3389 -InformationLevel Quiet"
if %errorlevel% equ 0 (
    echo   ✓ 3389 端口已开放
) else (
    echo   ✗ 3389 端口未开放
)
echo.

echo ========================================
echo   检查完成！
echo   如果 8080 未开放，请配置阿里云安全组
echo ========================================
echo.
pause
