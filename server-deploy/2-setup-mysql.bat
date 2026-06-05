
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   [Step 2/6] Initialize Database
echo ========================================
echo.

echo Enter MySQL root password:
set /p MYSQL_PASS=
echo.

echo Creating database...
mysql -u root -p%MYSQL_PASS% &lt; init_db.sql
if %errorlevel% equ 0 (
    echo   [OK] Database initialized successfully!
) else (
    echo   [X] Database initialization failed
)
echo.
pause
