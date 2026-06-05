
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   配置 Windows 防火墙（生产环境）
echo ========================================
echo.

echo [1/3] 关闭 8080 端口（不再开放到公网）
netsh advfirewall firewall delete rule name="Glimmerseed API" 2&gt;nul
echo   [OK]
echo.

echo [2/3] 开放 443 端口（HTTPS）
netsh advfirewall firewall add rule name="Glimmerseed HTTPS" dir=in action=allow protocol=TCP localport=443
echo   [OK]
echo.

echo [3/3] 确保 3389 端口限制（远程桌面）
echo   [OK]
echo.

echo ========================================
echo   防火墙配置完成！
echo ========================================
echo.
echo 重要：请在阿里云安全组中只开放 443 和 3389 端口
echo.
pause
