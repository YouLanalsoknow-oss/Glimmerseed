
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   [Step 3/6] Build Backend
echo ========================================
echo.

cd backend
call mvn clean package -DskipTests
if %errorlevel% equ 0 (
    echo.
    echo   [OK] Build successful!
) else (
    echo.
    echo   [X] Build failed
)
echo.
cd ..
pause
