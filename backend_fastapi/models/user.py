from sqlalchemy import Column, Integer, String, DateTime, func
from models.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    username = Column(String(100), unique=True, nullable=False, index=True)
    email = Column(String(255), nullable=True, default=None)
    password = Column(String(255), nullable=False)
    device_id = Column(String(255), unique=True, nullable=True, default=None)
    avatar_url = Column(String(500), nullable=True, default=None)
    created_at = Column(DateTime, server_default=func.now())