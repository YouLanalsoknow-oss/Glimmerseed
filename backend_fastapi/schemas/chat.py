from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime


class ChatRequest(BaseModel):
    pet_id: int
    content: str


class ChatMessageResponse(BaseModel):
    id: int
    petId: int = 0
    content: str
    role: str
    timestamp: Optional[datetime] = None

    class Config:
        from_attributes = True


class ChatSendResponse(BaseModel):
    success: bool
    message: str = ""
    data: Optional[ChatMessageResponse] = None


class ChatListResponse(BaseModel):
    success: bool
    data: List[ChatMessageResponse] = []