@echo off
echo === Glimmerseed FastAPI 部署 ===

if not exist ".venv" (
    echo 创建虚拟环境...
    python -m venv .venv
)

echo 激活虚拟环境并安装依赖...
call .venv\Scripts\activate.bat
pip install -r requirements.txt --quiet

echo 启动服务...
uvicorn main:app --host 0.0.0.0 --port 8080 --reload
pause
