@echo off
echo ================================
echo   启动 Glimmerseed 后端服务
echo ================================

cd /d "%~dp0target"

echo.
echo 正在启动服务...
echo 服务地址: http://localhost:8080
echo 按 Ctrl+C 停止服务
echo.

java -jar glimmerseed-backend-1.0.0.jar

pause