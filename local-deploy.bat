
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   Glimmerseed 本机一键部署
echo ========================================
echo.

cd /d "%~dp0"

echo [1/5] 检查环境...
call java -version &gt;nul 2&gt;&amp;1
if %errorLevel% neq 0 (
    echo [X] Java 未安装！
    pause
    exit /b 1
)
echo [OK] Java 已安装

call mvn -version &gt;nul 2&gt;&amp;1
if %errorLevel% neq 0 (
    echo [X] Maven 未安装！
    echo 请先安装 Maven 并配置环境变量
    pause
    exit /b 1
)
echo [OK] Maven 已安装
echo.

echo [2/5] 编译后端项目...
cd backend
call mvn clean package -DskipTests
if %errorLevel% neq 0 (
    echo [X] 编译失败！
    pause
    exit /b 1
)
echo [OK] 编译成功！
echo.

echo [3/5] 检查数据库初始化...
if not exist "init_db.sql" (
    echo [提示] init_db.sql 不存在，请手动初始化数据库
) else (
    echo [OK] init_db.sql 已存在
)
echo.

echo [4/5] 准备启动...
echo.
echo ========================================
echo   部署准备完成！
echo ========================================
echo.
echo 接下来的步骤：
echo.
echo 1. 确保 MySQL 正在运行
echo 2. 初始化数据库（如果还没做）：
echo    mysql -u root -p
echo    source init_db.sql
echo.
echo 3. 配置 application.yml 中的数据库密码
echo.
echo 4. 启动后端服务：
echo    cd backend
echo    start.bat
echo.
echo 5. 测试访问：
echo    http://localhost:8080/actuator/health
echo.
pause

