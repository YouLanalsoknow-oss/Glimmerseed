
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   Glimmerseed 本地启动
echo ========================================
echo.

cd /d "%~dp0"

echo [检查] 查找 JAR 文件...
for /f %%f in ('dir /b target\*.jar') do set JAR_FILE=target\%%f

if not exist "%JAR_FILE%" (
    echo [X] JAR 文件不存在！
    echo [提示] 请先运行 mvn clean package -DskipTests 编译
    pause
    exit /b 1
)

echo [OK] 找到文件: %JAR_FILE%
echo.
echo [启动] 正在启动...
echo 访问地址: http://localhost:8080
echo 健康检查: http://localhost:8080/actuator/health
echo.
echo 按 Ctrl+C 停止服务
echo ========================================
echo.

java -jar %JAR_FILE%

pause

