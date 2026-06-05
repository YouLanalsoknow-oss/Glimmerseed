
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   配置 Windows 服务
echo ========================================
echo.

echo [1/3] 下载 NSSM (Non-Sucking Service Manager...
powershell -Command "Invoke-WebRequest -Uri 'https://nssm.cc/release/nssm-2.24.zip' -OutFile 'nssm.zip'"
echo   [OK]
echo.

echo [2/3] 解压 NSSM...
powershell -Command "Expand-Archive -Path 'nssm.zip' -DestinationPath 'C:\' -Force"
echo   [OK]
echo.

echo [3/3] 安装 Spring Boot 为 Windows 服务...
set JAR_PATH=C:\Users\Administrator\Desktop\server-deploy\backend\target\glimmerseed-1.0.0.jar

C:\nssm-2.24\win64\nssm.exe install GlimmerseedBackend "C:\Program Files\Java\jdk-21.0.11\bin\java.exe"
C:\nssm-2.24\win64\nssm.exe set GlimmerseedBackend AppDirectory C:\Users\Administrator\Desktop\server-deploy\backend
C:\nssm-2.24\win64\nssm.exe set GlimmerseedBackend AppParameters -jar %JAR_PATH%
C:\nssm-2.24\win64\nssm.exe set GlimmerseedBackend DisplayName "Glimmerseed Backend Service"
C:\nssm-2.24\win64\nssm.exe set GlimmerseedBackend Description "Glimmerseed AI Pet Chat Backend"
C:\nssm-2.24\win64\nssm.exe set GlimmerseedBackend Start SERVICE_AUTO_START
C:\nssm-2.24\win64\nssm.exe set GlimmerseedBackend AppStdout C:\nginx\logs\glimmerseed-stdout.log
C:\nssm-2.24\win64\nssm.exe set GlimmerseedBackend AppStderr C:\nginx\logs\glimmerseed-stderr.log

echo   [OK] 服务安装成功
echo.

echo ========================================
echo   服务配置完成！
echo ========================================
echo.
echo 启动服务: net start GlimmerseedBackend
echo 停止服务: net stop GlimmerseedBackend
echo.
pause
