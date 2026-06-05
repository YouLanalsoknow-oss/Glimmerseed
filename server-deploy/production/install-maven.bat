
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   Maven 自动安装脚本
echo ========================================
echo.

echo [1/5] 检查管理员权限...
net session &gt;nul 2&gt;&amp;1
if %errorLevel% neq 0 (
    echo [!] 请以管理员身份运行此脚本！
    echo [!] 右键点击 -^&gt; 以管理员身份运行
    pause
    exit /b 1
)
echo [OK] 权限检查通过
echo.

echo [2/5] 下载 Maven 3.9.6...
powershell -Command "&amp; { Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile 'apache-maven-3.9.6-bin.zip' }"
if %errorLevel% neq 0 (
    echo [X] 下载失败！
    echo [提示] 可以手动下载：https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip
    pause
    exit /b 1
)
echo [OK] Maven 下载完成
echo.

echo [3/5] 解压 Maven 到 C:\Program Files\Apache\maven...
if not exist "C:\Program Files\Apache" mkdir "C:\Program Files\Apache"
powershell -Command "Expand-Archive -Path 'apache-maven-3.9.6-bin.zip' -DestinationPath 'C:\Program Files\Apache' -Force"
if exist "C:\Program Files\Apache\apache-maven-3.9.6" (
    if exist "C:\Program Files\Apache\maven" rmdir /s /q "C:\Program Files\Apache\maven"
    ren "C:\Program Files\Apache\apache-maven-3.9.6" "maven"
)
echo [OK] 解压完成
echo.

echo [4/5] 配置环境变量...
setx MAVEN_HOME "C:\Program Files\Apache\maven" /M &gt;nul
if %errorLevel% neq 0 (
    echo [!] 环境变量设置失败，尝试另一种方式...
    powershell -Command "[Environment]::SetEnvironmentVariable('MAVEN_HOME', 'C:\Program Files\Apache\maven', 'Machine')"
)
echo [OK] MAVEN_HOME 已设置

for /f "skip=2 tokens=3*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path') do set CURRENT_PATH=%%a %%b
echo %CURRENT_PATH% | findstr /C:"%MAVEN_HOME%\bin" &gt;nul
if %errorLevel% neq 0 (
    setx PATH "%CURRENT_PATH%;%MAVEN_HOME%\bin" /M &gt;nul
    echo [OK] PATH 已更新
) else (
    echo [OK] PATH 已包含 Maven
)
echo.

echo [5/5] 清理临时文件...
del /q apache-maven-3.9.6-bin.zip
echo [OK] 清理完成
echo.

echo ========================================
echo   Maven 安装成功！
echo ========================================
echo.
echo [重要提示]
echo   1. 关闭当前 CMD 窗口
echo   2. 重新打开一个新的 CMD 窗口
echo   3. 运行 mvn -version 验证安装
echo.
echo Maven 安装位置：C:\Program Files\Apache\maven
echo.
pause

