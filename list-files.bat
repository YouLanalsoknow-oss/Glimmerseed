@echo off
REM 在服务器上运行这个批处理文件
echo ========================================
echo   检查服务器上的文件
echo ========================================
echo.

echo [1] 当前用户的下载目录:
echo.
dir "%USERPROFILE%\Downloads" /O:N
echo.

echo [2] C盘根目录:
echo.
dir C:\ /O:N
echo.

echo [3] 查找所有 .exe 文件 (C盘):
echo.
dir C:\*.exe /S /B 2>nul | find /v ".git" | find /v "node_modules" | find /v "ProgramData"
echo.

echo [4] 查找所有 .msi 文件 (C盘):
echo.
dir C:\*.msi /S /B 2>nul | find /v ".git" | find /v "node_modules" | find /v "ProgramData"
echo.

echo ========================================
echo   检查完成！
echo   请把上面的输出复制发给我！
echo ========================================
echo.
pause