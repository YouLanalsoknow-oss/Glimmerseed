import logging
import platform
import os
import secrets
import psutil
import time
from datetime import datetime, timedelta
from functools import lru_cache

from fastapi import APIRouter, Request, Depends, Response, Form
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session
from sqlalchemy import func, desc

from models.database import get_db
from models.user import User
from models.pet import Pet
from models.chat import ChatMessage
from services.auth_service import decode_token

router = APIRouter(prefix="/admin", tags=["管理后台"])
logger = logging.getLogger(__name__)

templates = Jinja2Templates(directory="templates")

ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD", "glimmerseed2026")
SESSION_KEY = secrets.token_hex(16)


def _check_auth(request: Request) -> bool:
    return request.cookies.get("admin_session") == SESSION_KEY


def _get_server_info() -> dict:
    boot_time = datetime.fromtimestamp(psutil.boot_time())
    uptime = datetime.now() - boot_time
    mem = psutil.virtual_memory()
    cpu_percent = psutil.cpu_percent(interval=0.5)
    disk = psutil.disk_usage("/")
    process = psutil.Process(os.getpid())
    proc_mem = process.memory_info().rss / 1024 / 1024

    return {
        "hostname": platform.node(),
        "platform": f"{platform.system()} {platform.release()}",
        "python": platform.python_version(),
        "uptime": str(uptime).split(".")[0],
        "cpu_cores": psutil.cpu_count(logical=True),
        "cpu_usage": round(cpu_percent, 1),
        "mem_total": round(mem.total / 1024 / 1024 / 1024, 1),
        "mem_used": round(mem.used / 1024 / 1024 / 1024, 1),
        "mem_percent": mem.percent,
        "disk_total": round(disk.total / 1024 / 1024 / 1024, 1),
        "disk_used": round(disk.used / 1024 / 1024 / 1024, 1),
        "disk_percent": disk.percent,
        "api_process_mem_mb": round(proc_mem, 1),
        "api_pid": os.getpid(),
        "now": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
    }


@router.get("/login", response_class=HTMLResponse)
async def admin_login(request: Request, error: str = ""):
    return templates.TemplateResponse("admin_login.html", {
        "request": request,
        "error": error,
    })


@router.post("/login")
async def admin_login_post(request: Request, response: Response, password: str = Form(...)):
    if password == ADMIN_PASSWORD:
        response.set_cookie(key="admin_session", value=SESSION_KEY, httponly=True, max_age=86400 * 7)
        return RedirectResponse(url="/admin", status_code=302)
    return RedirectResponse(url="/admin/login?error=invalid", status_code=302)


@router.get("/logout")
async def admin_logout():
    resp = RedirectResponse(url="/admin/login", status_code=302)
    resp.delete_cookie("admin_session")
    return resp


@router.get("", response_class=HTMLResponse)
async def admin_page(request: Request, db: Session = Depends(get_db)):
    if not _check_auth(request):
        return RedirectResponse(url="/admin/login", status_code=302)

    total_users = db.query(func.count(User.id)).scalar()
    total_pets = db.query(func.count(Pet.id)).scalar()
    total_messages = db.query(func.count(ChatMessage.id)).scalar()

    recent_users = (
        db.query(User)
        .order_by(desc(User.created_at))
        .limit(10)
        .all()
    )

    users_with_pets = []
    for u in recent_users:
        pet_count = (
            db.query(func.count(Pet.id))
            .filter(Pet.user_id == u.id)
            .scalar()
        )
        msg_count = (
            db.query(func.count(ChatMessage.id))
            .join(Pet, ChatMessage.pet_id == Pet.id)
            .filter(Pet.user_id == u.id)
            .scalar()
        )
        users_with_pets.append({
            "id": u.id,
            "username": u.username,
            "email": u.email or "-",
            "device_id": (u.device_id[:8] + "...") if u.device_id else "-",
            "avatar_url": u.avatar_url or None,
            "created_at": u.created_at.strftime("%Y-%m-%d %H:%M") if u.created_at else "-",
            "pet_count": pet_count,
            "msg_count": msg_count,
        })

    server = _get_server_info()

    return templates.TemplateResponse("admin.html", {
        "request": request,
        "server": server,
        "stats": {
            "total_users": total_users,
            "total_pets": total_pets,
            "total_messages": total_messages,
        },
        "users": users_with_pets,
    })


@router.get("/api/users")
async def api_users(request: Request, db: Session = Depends(get_db)):
    if not _check_auth(request):
        return {"success": False, "error": "未授权"}
    users = db.query(User).order_by(desc(User.created_at)).all()
    return {
        "success": True,
        "data": [
            {
                "id": u.id,
                "username": u.username,
                "email": u.email or "",
                "device_id": u.device_id or "",
                "avatar_url": u.avatar_url or "",
                "created_at": u.created_at.isoformat() if u.created_at else None,
            }
            for u in users
        ],
    }


@router.get("/api/server")
async def api_server(request: Request):
    if not _check_auth(request):
        return {"success": False, "error": "未授权"}
    return {"success": True, "data": _get_server_info()}


@router.get("/api/stats")
async def api_stats(request: Request, db: Session = Depends(get_db)):
    if not _check_auth(request):
        return {"success": False, "error": "未授权"}
    now = datetime.utcnow()
    today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)
    week_ago = today_start - timedelta(days=7)

    new_users_today = (
        db.query(func.count(User.id))
        .filter(User.created_at >= today_start)
        .scalar()
    )
    new_users_week = (
        db.query(func.count(User.id))
        .filter(User.created_at >= week_ago)
        .scalar()
    )
    messages_today = (
        db.query(func.count(ChatMessage.id))
        .filter(ChatMessage.timestamp >= today_start)
        .scalar()
    )

    return {
        "success": True,
        "data": {
            "total_users": db.query(func.count(User.id)).scalar(),
            "total_pets": db.query(func.count(Pet.id)).scalar(),
            "total_messages": db.query(func.count(ChatMessage.id)).scalar(),
            "new_users_today": new_users_today,
            "new_users_week": new_users_week,
            "messages_today": messages_today,
        },
    }