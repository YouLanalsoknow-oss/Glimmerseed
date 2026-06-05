#!/bin/bash
cd /opt/glimmerseed-api
echo "=== 检查依赖 ==="
.venv/bin/python -c "import fastapi; print('fastapi:', fastapi.__version__)" 2>&1
.venv/bin/python -c "import uvicorn; print('uvicorn: OK')" 2>&1
.venv/bin/python -c "import sqlalchemy; print('sqlalchemy: OK')" 2>&1
.venv/bin/python -c "import httpx; print('httpx: OK')" 2>&1
.venv/bin/python -c "import jose; print('jose: OK')" 2>&1
.venv/bin/python -c "import passlib; print('passlib: OK')" 2>&1
echo ""
echo "=== 启动服务 ==="
pkill -f "uvicorn main:app" 2>/dev/null
sleep 1
nohup .venv/bin/python -m uvicorn main:app --host 0.0.0.0 --port 8080 > api.log 2>&1 &
sleep 3
echo ""
echo "=== 进程状态 ==="
ps aux | grep uvicorn | grep -v grep || echo "未运行"
echo ""
echo "=== 健康检查 ==="
curl -s http://localhost:8080/health || echo "无法连接"
echo ""
echo "=== 日志(最后15行) ==="
tail -15 api.log 2>/dev/null || echo "无日志"
