from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, func
from sqlalchemy.orm import relationship
from models.database import Base


class Pet(Base):
    __tablename__ = "pets"

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(100), nullable=False)
    appearance = Column(String(200), nullable=False)
    personality = Column(String(200), nullable=False)
    color = Column(String(50), nullable=False)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    created_at = Column(DateTime, server_default=func.now())
    last_interacted_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    user = relationship("User", backref="pets")
    chat_messages = relationship("ChatMessage", backref="pet", cascade="all, delete-orphan")
