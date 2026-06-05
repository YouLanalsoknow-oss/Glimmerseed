@echo off
echo ================================================
echo   Glimmerseed 后端部署包创建工具
echo ================================================
echo.

cd /d "%~dp0"

echo [1/3] 创建临时目录...
if exist "..\Glimmerseed_Deploy" rmdir /s /q "..\Glimmerseed_Deploy"
mkdir "..\Glimmerseed_Deploy"

echo [2/3] 复制文件...
xcopy /E /Y "backend" "..\Glimmerseed_Deploy\backend\"
xcopy /Y "README_WINDOWS.txt" "..\Glimmerseed_Deploy\"
xcopy /Y "README_LINUX.txt" "..\Glimmerseed_Deploy\"
xcopy /Y "MANUAL_GUIDE.md" "..\Glimmerseed_Deploy\"

echo [3/3] 创建ZIP压缩包...
cd ".."
powershell -Command "Compress-Archive -Path 'Glimmerseed_Deploy' -DestinationPath 'Glimmerseed_Deploy_Package.zip' -Force"

if exist "Glimmerseed_Deploy_Package.zip" (
    echo.
    echo ================================================
    echo   部署包创建成功！
    echo ================================================
    echo.
    echo 文件位置：
    realpath "Glimmerseed_Deploy_Package.zip"
    echo.
    echo 文件大小：
    powershell -Command "(Get-Item 'Glimmerseed_Deploy_Package.zip').Length / 1MB"
    echo MB
    echo.
    echo 使用方法：
    echo 1. 上传 Glimmerseed_Deploy_Package.zip 到服务器
    echo 2. 解压到目标目录
    echo 3. 根据服务器系统选择部署方式
) else (
    echo.
    echo [错误] 压缩包创建失败！
)

echo.
pause