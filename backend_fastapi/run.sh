#!/bin/bash
set -e

echo "=== Glimmerseed FastAPI 部署 ==="

VENV_DIR=".venv"
PYTHON="python3"

if [ ! -d "$VENV_DIR" ]; then
    echo "创建虚拟环境..."
    $PYTHON -m venv $VENV_DIR
fi

echo "激活虚拟环境并安装依赖..."
source "$VENV_DIR/bin/activate"
pip install -r requirements.txt --quiet

echo "启动服务..."
uvicorn main:app --host 0.0.0.0 --port 8080 --reload
