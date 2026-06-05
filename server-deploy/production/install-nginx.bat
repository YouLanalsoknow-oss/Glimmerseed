
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   安装 Nginx for Windows
echo ========================================
echo.

echo [1/5] 下载 Nginx...
powershell -Command "Invoke-WebRequest -Uri 'https://nginx.org/download/nginx-1.24.0.zip' -OutFile 'nginx-1.24.0.zip'"
if %errorlevel% neq 0 (
    echo   [X] 下载失败
    pause
    exit /b 1
)
echo   [OK] 下载成功
echo.

echo [2/5] 解压 Nginx...
powershell -Command "Expand-Archive -Path 'nginx-1.24.0.zip' -DestinationPath 'C:\' -Force"
ren "C:\nginx-1.24.0" "nginx"
echo   [OK] 解压成功
echo.

echo [3/5] 创建目录结构...
mkdir "C:\nginx\ssl" 2&gt;nul
mkdir "C:\nginx\logs" 2&gt;nul
mkdir "C:\nginx\conf" 2&gt;nul
echo   [OK] 目录创建成功
echo.

echo [4/5] 复制配置文件...
copy "nginx.conf" "C:\nginx\conf\nginx.conf" /Y
echo   [OK] 配置文件复制成功
echo.

echo [5/5] 创建自签名 SSL 证书...
powershell -Command "openssl req -x509 -nodes -days 3650 -newkey rsa:2048 -keyout 'C:\nginx\ssl\server.key' -out 'C:\nginx\ssl\server.crt' -subj '/C=CN/ST=State/L=City/O=Organization/OU=Unit/CN=8.134.80.158'" 2&gt;nul
echo   [OK] SSL 证书创建成功
echo.

echo ========================================
echo   Nginx 安装完成！
echo ========================================
echo.
echo 下一步：
echo   1. 配置防火墙：开放 443 端口
echo   2. 启动 Nginx: C:\nginx\nginx.exe
echo   3. 配置 Windows 服务（可选）
echo.
pause
