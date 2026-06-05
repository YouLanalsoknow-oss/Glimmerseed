@echo off
echo ================================
echo   安装 Glimmerseed 为 Windows 服务
echo ================================

cd /d "%~dp0target"

if not exist "glimmerseed.exe" (
    echo.
    echo [错误] 未找到 glimmerseed.exe
    echo 请先下载 WinSW: https://github.com/winsw/winsw/releases
    echo 将 WinSW.exe 重命名为 glimmerseed.exe 并放在此目录
    echo.
    pause
    exit /b 1
)

echo.
echo [1/3] 安装服务...
sc stop Glimmerseed 2>nul
sc delete Glimmerseed 2>nul
glimmerseed.exe install

if %ERRORLEVEL% NEQ 0 (
    echo [错误] 服务安装失败！
    pause
    exit /b 1
)

echo.
echo [2/3] 启动服务...
glimmerseed.exe start

echo.
echo [3/3] 检查服务状态...
timeout /t 5 /nobreak >nul
glimmerseed.exe status

echo.
echo ================================
echo   服务安装完成！
echo   
echo   管理命令：
echo   glimmerseed.exe start   - 启动
echo   glimmerseed.exe stop    - 停止
echo   glimmerseed.exe restart - 重启
echo   glimmerseed.exe status  - 状态
echo   glimmerseed.exe uninstall - 卸载
echo ================================

pause