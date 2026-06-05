
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   [Step 1/6] Check Server Environment
echo ========================================
echo.

echo [1] Checking Java...
java -version
if %errorlevel% neq 0 (
    echo   [X] Java not installed or not in PATH
) else (
    echo   [OK] Java is installed
)
echo.

echo [2] Checking Maven...
mvn -version
if %errorlevel% neq 0 (
    echo   [X] Maven not installed or not in PATH
) else (
    echo   [OK] Maven is installed
)
echo.

echo [3] Checking MySQL...
mysql --version
if %errorlevel% neq 0 (
    echo   [X] MySQL not installed or not in PATH
) else (
    echo   [OK] MySQL is installed
)
echo.

echo ========================================
echo   Check complete!
echo   If anything is missing, install it first
echo ========================================
echo.
pause
