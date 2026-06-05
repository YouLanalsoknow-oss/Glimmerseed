
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   Glimmerseed 本地环境检查
echo ========================================
echo.

echo [1/6] 检查 Java...
java -version &gt;nul 2&gt;&amp;1
if %errorLevel% neq 0 (
    echo   [X] Java 未安装！
) else (
    echo   [OK] Java 已安装
    java -version 2&gt;&amp;1 | findstr /i "version"
)
echo.

echo [2/6] 检查 Maven...
mvn -version &gt;nul 2&gt;&amp;1
if %errorLevel% neq 0 (
    echo   [X] Maven 未安装！
) else (
    echo   [OK] Maven 已安装
    mvn -version | findstr /i "Apache Maven"
)
echo.

echo [3/6] 检查 MySQL...
mysql --version &gt;nul 2&gt;&amp;1
if %errorLevel% neq 0 (
    echo   [X] MySQL 未安装！
    echo   [提示] 如果使用 Docker 或 WSL，请相应调整
) else (
    echo   [OK] MySQL 已安装
    mysql --version
)
echo.

echo [4/6] 检查后端代码...
if exist "..\backend\pom.xml" (
    echo   [OK] 后端代码存在
) else (
    echo   [X] 后端代码未找到！
)
echo.

echo [5/6] 检查端口占用...
netstat -ano | findstr ":8080" &gt;nul
if %errorLevel% neq 0 (
    echo   [OK] 8080 端口空闲
) else (