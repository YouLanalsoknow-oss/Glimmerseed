
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   Glimmerseed 一键部署脚本
echo ========================================
echo.
echo 这个脚本会自动执行以下操作：
echo   1. 检查并安装 Maven（如果需要）
echo   2. 编译后端项目
echo   3. 安装 Nginx
echo   4. 配置防火墙
echo.
pause

echo.
echo ========================================
echo 阶段 1: 检查 Maven
echo ========================================
call mvn -version &gt;nul 2&gt;&amp;1
if %errorLevel% neq 0 (
    echo [!] Maven 未安装，正在安装...
    call install-maven.bat
    if %errorLevel% neq 0 (
        echo [X] Maven 安装失败！
        pause
        exit /b 1
    )
    echo [OK] Maven 安装完成！
    echo [!] 请关闭当前窗口，重新打开一个新的 CMD 窗口，然后再次运行此脚本
    pause
    exit /b 0
) else (
    echo [OK] Maven 已安装
)
echo.

echo ========================================
echo 阶段 2: 编译后端项目
echo ========================================
cd ..\backend
if not exist "target" (
    echo [提示] 第一次编译需要下载依赖，可能需要 5-10 分钟...
)
call mvn clean package -DskipTests
if %errorLevel% neq 0 (
    echo [X] 编译失败！
    pause
    exit /b 1
)
echo [OK] 编译成功！
cd ..\production
echo.

echo ========================================
echo 阶段 3: 安装 Nginx
echo ========================================
if not exist "C:\nginx" (
    call install-nginx.bat
) else (
    echo [OK] Nginx 已安装
)
echo.

echo ========================================
echo 阶段 4: 配置防火墙
echo ========================================
call open-firewall-https.bat
echo.

echo ========================================
echo   部署准备完成！
echo ========================================
echo.
echo 接下来的步骤：
echo   1. 确保 MySQL 正在运行
echo   2. 初始化数据库：运行 ..\2-setup-mysql.bat
echo   3. 启动服务：
echo      - cd C:\nginx 然后 start nginx
echo      - cd ..\backend 然后 start.bat
echo   4. 配置阿里云安全组：开放 443 端口
echo.
pause

