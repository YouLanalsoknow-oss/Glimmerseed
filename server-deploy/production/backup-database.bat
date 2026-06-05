
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   MySQL 自动备份脚本
echo ========================================
echo.

set MYSQL_USER=root
set MYSQL_PASS=5-iKje5Q.Dt48fw
set DATABASE=glimmerseed
set BACKUP_DIR=C:\backups
set DATE=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set DATE=%DATE: =0%
set BACKUP_FILE=%BACKUP_DIR%\%DATABASE%_%DATE%.sql

echo [1/3] 创建备份目录...
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
echo   [OK]
echo.

echo [2/3] 正在备份数据库...
mysqldump -u%MYSQL_USER% -p%MYSQL_PASS% %DATABASE% &gt; "%BACKUP_FILE%"

if %errorlevel% equ 0 (
    echo   [OK] 备份成功: %BACKUP_FILE%
) else (
    echo   [X] 备份失败
)
echo.

echo [3/3] 清理 7 天前的备份...
forfiles /p "%BACKUP_DIR%" /m %DATABASE%_*.sql /d -7 /c "cmd /c del @path"
echo   [OK]
echo.

echo ========================================
echo   备份完成！
echo ========================================
echo.
