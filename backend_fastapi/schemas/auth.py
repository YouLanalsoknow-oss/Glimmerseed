from pydantic import BaseModel


class RegisterRequest(BaseModel):
    username: str
    password: str
    email: str | None = None
    device_id: str = ""


class LoginRequest(BaseModel):
    account: str
    password: str
    device_id: str = ""


class AuthResponse(BaseModel):
    success: bool
    message: str = ""
    token: str | None = None
    user_id: int | None = None
    username: str | None = None
    avatar_url: str | None = None