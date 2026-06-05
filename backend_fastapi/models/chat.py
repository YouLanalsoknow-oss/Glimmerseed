from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, func
from models.database import Base


class ChatMessage(Base):
    __tablename__ = "chat_messages"

    id = Column(Integer, primary_key=True, autoincrement=True)
    pet_id = Column(Integer, ForeignKey("pets.id", ondelete="CASCADE"), nullable=False, index=True)
    content = Column(String(5000), nullable=False)
    role = Column(String(20), nullable=False)
    timestamp = Column(DateTime, server_default=func.now())
