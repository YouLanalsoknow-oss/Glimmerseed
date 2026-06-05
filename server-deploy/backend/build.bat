@echo off
echo ================================
echo   Glimmerseed 后端构建脚本
echo ================================

cd /d "%~dp0"

echo.
echo [1/3] 正在清理和编译项目...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [错误] 构建失败！
    pause
    exit /b 1
)

echo.
echo [2/3] 构建成功！
echo.

echo [3/3] JAR 文件位置：
dir target\*.jar /b

echo.
echo ================================
echo   构建完成！
echo   运行以下命令启动服务：
echo   java -jar target\glimmerseed-backend-1.0.0.jar
echo ================================

pause