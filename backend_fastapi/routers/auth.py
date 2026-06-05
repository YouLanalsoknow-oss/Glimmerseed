import logging
import os
import uuid

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session

from models.database import get_db
from models.user import User
from schemas.auth import RegisterRequest, LoginRequest, AuthResponse
from services.auth_service import hash_password, verify_password, create_token, decode_token

router = APIRouter(prefix="/api/auth", tags=["认证"])
logger = logging.getLogger(__name__)

AVATAR_DIR = "static/avatars"
os.makedirs(AVATAR_DIR, exist_ok=True)


@router.post("/register", response_model=AuthResponse)
def register(req: RegisterRequest, db: Session = Depends(get_db)):
    logger.info("Register request: username=%s", req.username)

    existing_user = db.query(User).filter(User.username == req.username).first()
    if existing_user:
        return AuthResponse(success=False, message="用户名已被使用")

    if req.email:
        existing_email = db.query(User).filter(User.email == req.email).first()
        if existing_email:
            return AuthResponse(success=False, message="该邮箱已被注册")

    if req.device_id:
        existing_device = db.query(User).filter(User.device_id == req.device_id).first()
        if existing_device:
            return AuthResponse(success=False, message="该设备已注册过账户")

    user = User(
        username=req.username,
        email=req.email,
        password=hash_password(req.password),
        device_id=req.device_id if req.device_id else None,
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_token(user.id, user.email or user.username)
    logger.info("User registered: id=%d username=%s", user.id, user.username)
    avatar_url = f"http://8.134.80.158:8080{user.avatar_url}" if user.avatar_url else None
    return AuthResponse(success=True, message="注册成功", token=token, user_id=user.id, username=user.username, avatar_url=avatar_url)


@router.post("/login", response_model=AuthResponse)
def login(req: LoginRequest, db: Session = Depends(get_db)):
    logger.info("Login request: account=%s", req.account)

    user = db.query(User).filter(
        (User.username == req.account) | (User.email == req.account)
    ).first()

    if not user or not verify_password(req.password, user.password):
        return AuthResponse(success=False, message="用户名/邮箱或密码错误")

    if req.device_id and user.device_id and user.device_id != req.device_id:
        return AuthResponse(success=False, message="该账户已在其他设备登录")

    token = create_token(user.id, user.email or user.username)
    logger.info("User logged in: id=%d username=%s", user.id, user.username)
    avatar_url = f"http://8.134.80.158:8080{user.avatar_url}" if user.avatar_url else None
    return AuthResponse(success=True, message="登录成功", token=token, user_id=user.id, username=user.username, avatar_url=avatar_url)


@router.post("/avatar")
async def upload_avatar(
    token: str = Form(...),
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
):
    payload = decode_token(token)
    if not payload:
        raise HTTPException(status_code=401, detail="无效的认证令牌")

    user_id = payload.get("user_id")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")

    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="只支持图片文件")

    ext = file.filename.rsplit(".", 1)[-1] if "." in (file.filename or "") else "jpg"
    filename = f"{user_id}_{uuid.uuid4().hex[:8]}.{ext}"
    filepath = os.path.join(AVATAR_DIR, filename)

    content = await file.read()
    if len(content) > 5 * 1024 * 1024:
        raise HTTPException(status_code=400, detail="图片大小不能超过5MB")

    with open(filepath, "wb") as f:
        f.write(content)

    user.avatar_url = f"/static/avatars/{filename}"
    db.commit()

    logger.info("Avatar uploaded: user_id=%d url=%s", user_id, user.avatar_url)
    full_url = f"http://8.134.80.158:8080{user.avatar_url}"
    return {"success": True, "message": "头像上传成功", "avatar_url": full_url}