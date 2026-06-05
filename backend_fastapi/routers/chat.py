import logging

from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.orm import Session

from models.database import get_db
from models.pet import Pet
from models.chat import ChatMessage
from schemas.chat import ChatRequest, ChatMessageResponse, ChatSendResponse, ChatListResponse
from services.auth_service import decode_token

router = APIRouter(prefix="/api/chat", tags=["聊天"])
logger = logging.getLogger(__name__)


def _get_user_id(authorization: str | None = Header(None)) -> int:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="未提供认证令牌")
    payload = decode_token(authorization[7:])
    if not payload:
        raise HTTPException(status_code=401, detail="令牌无效或已过期")
    return payload["user_id"]


@router.post("", response_model=ChatSendResponse)
def send_message(req: ChatRequest, user_id: int = Depends(_get_user_id), db: Session = Depends(get_db)):
    pet = db.query(Pet).filter(Pet.id == req.pet_id, Pet.user_id == user_id).first()
    if not pet:
        raise HTTPException(status_code=404, detail="桌宠不存在")

    msg = ChatMessage(pet_id=req.pet_id, content=req.content, role="user")
    db.add(msg)
    db.commit()
    db.refresh(msg)

    logger.info("Message saved: pet=%d user=%d", req.pet_id, user_id)
    return ChatSendResponse(
        success=True,
        message="消息已保存",
        data=ChatMessageResponse(
            id=msg.id,
            petId=msg.pet_id,
            content=msg.content,
            role=msg.role,
            timestamp=None
        )
    )


@router.get("/{pet_id}", response_model=ChatListResponse)
def get_messages(pet_id: int, user_id: int = Depends(_get_user_id), db: Session = Depends(get_db)):
    pet = db.query(Pet).filter(Pet.id == pet_id, Pet.user_id == user_id).first()
    if not pet:
        raise HTTPException(status_code=404, detail="桌宠不存在")

    messages = (
        db.query(ChatMessage)
        .filter(ChatMessage.pet_id == pet_id)
        .order_by(ChatMessage.timestamp)
        .all()
    )

    return ChatListResponse(
        success=True,
        data=[
            ChatMessageResponse(
                id=m.id,
                petId=m.pet_id,
                content=m.content,
                role=m.role,
                timestamp=None
            )
            for m in messages
        ]
    )