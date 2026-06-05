
@echo off
chcp 65001 >nul
echo ========================================
echo   [Step 4/6] Configure Application
echo ========================================
echo.

echo Enter database password (root):
set /p DB_PASS=
echo.

echo Enter SiliconFlow API Key:
set /p API_KEY=
echo.

echo Generating config file...
set "TEMPLATE_FILE=%~dp0application.yml.template"
set "OUTPUT_FILE=backend\src\main\resources\application.yml"

if not exist "%TEMPLATE_FILE%" (
    echo   [X] Config template file not found: %TEMPLATE_FILE%
    pause
    exit /b 1
)

powershell -Command "(Get-Content '%TEMPLATE_FILE%' -Raw) -replace '\{\{DB_PASSWORD\}\}', '%DB_PASS%' -replace '\{\{API_KEY\}\}', '%API_KEY%' | Set-Content '%OUTPUT_FILE%'"

if %ERRORLEVEL% EQU 0 (
    echo   [OK] Config file generated!
) else (
    echo   [X] Failed to generate config file
)
echo.
pause
