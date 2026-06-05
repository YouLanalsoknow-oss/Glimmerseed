@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

:: ============================================================
:: Glimmerseed FastAPI 后端 - 一键部署到阿里云服务器
:: 目标: root@8.134.80.158
:: SSH密钥: C:\Users\YouLa\Downloads\我的电脑.pem
:: ============================================================

set SERVER=root@8.134.80.158
set PEM_KEY="%~dp0deploy\server_key.pem"
set REMOTE_DIR=/opt/glimmerseed-api
set LOCAL_DIR=%~dp0backend_fastapi

echo.
echo ============================================
echo   Glimmerseed FastAPI 一键部署
echo   目标服务器: %SERVER%
echo ========================================
echo.

:: 检查 PEM 文件权限 (Windows 下需要修复)
if not exist %PEM_KEY% (
    echo [错误] 找不到 PEM 密钥文件: %PEM_KEY%
    pause & exit /b 1
)

echo [1/6] 测试 SSH 连接...
ssh -i %PEM_KEY% -o StrictHostKeyChecking=no -o ConnectTimeout=10 %SERVER% "echo '连接成功' && uname -a" || (
    echo [错误] 无法连接到服务器，请检查网络和密钥
    pause & exit /b 1
)

echo.
echo [2/6] 在服务器上创建目录并安装 Python 环境...
ssh -i %PEM_KEY% %SERVER% "mkdir -p %REMOTE_DIR% && which python3 || (apt-get update -qq && apt-get install -y -qq python3 python3-pip python3-venv) && echo 'Python 环境就绪'"

echo.
echo [3/6] 上传 FastAPI 项目文件...
scp -i %PEM_KEY% -o StrictHostKeyChecking=no %LOCAL_DIR%\*.py %SERVER%:%REMOTE_DIR%/
scp -i %PEM_KEY% -o StrictHostKeyChecking=no %LOCAL_DIR%\requirements.txt %SERVER%:%REMOTE_DIR%/
scp -r -i %PEM_KEY% -o StrictHostKeyChecking=no %LOCAL_DIR%\models %SERVER%:%REMOTE_DIR%/
scp -r -i %PEM_KEY% -o StrictHostKeyChecking=no %LOCAL_DIR%\schemas %SERVER%:%REMOTE_DIR%/
scp -r -i %PEM_KEY% -o StrictHostKeyChecking=no %LOCAL_DIR%\routers %SERVER%:%REMOTE_DIR%/
scp -r -i %PEM_KEY% -o StrictHostKeyChecking=no %LOCAL_DIR%\services %SERVER%:%REMOTE_DIR%/

echo.
echo [4/6] 在服务器上安装依赖并创建虚拟环境...
ssh -i %PEM_KEY% %SERVER% "cd %REMOTE_DIR% && python3 -m venv .venv && source .venv/bin/activate && pip install --upgrade pip -q && pip install -r requirements.txt -q && echo '依赖安装完成'"

echo.
echo [5/6] 停止旧进程（如果有）...
ssh -i %PEM_KEY% %SERVER% "pkill -f 'uvicorn main:app' 2>nul; sleep 1; echo '旧进程已清理'"

echo.
echo [6/6] 启动 FastAPI 服务（后台运行）...
ssh -i %PEM_KEY% %SERVER% "cd %REMOTE_DIR% && nohup .venv/bin/python -m uvicorn main:app --host 0.0.0.0 --port 8080 > api.log 2>&1 & sleep 2 && ps aux | grep uvicorn | grep -v grep && echo '' && echo '=== 服务已启动 ===' && curl -s http://localhost:8080/health"

echo.
echo ============================================
echo   部署完成！
echo   API 地址: http://8.134.80.158:8080
echo   API 文档: http://8.134.80.158:8080/docs
echo   日志查看: ssh ... "tail -f %REMOTE_DIR%/api.log"
echo ============================================
pause
