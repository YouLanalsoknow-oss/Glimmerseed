
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   [Step 5/6] Start Server
echo ========================================
echo.

cd backend
call start.bat
cd ..
