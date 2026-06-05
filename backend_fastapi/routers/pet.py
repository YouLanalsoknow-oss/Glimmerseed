import logging
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.orm import Session
from sqlalchemy import desc

from models.database import get_db
from models.pet import Pet
from models.user import User
from schemas.pet import CreatePetRequest, PetResponse
from services.auth_service import decode_token

router = APIRouter(prefix="/api/pets", tags=["桌宠"])
logger = logging.getLogger(__name__)


def _get_user_id(authorization: str | None = Header(None)) -> int:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="未提供认证令牌")
    payload = decode_token(authorization[7:])
    if not payload:
        raise HTTPException(status_code=401, detail="令牌无效或已过期")
    return payload["user_id"]


@router.post("", response_model=dict)
def create_pet(
    req: CreatePetRequest,
    user_id: int = Depends(_get_user_id),
    db: Session = Depends(get_db),
):
    pet = Pet(
        name=req.name,
        appearance=req.appearance,
        personality=req.personality,
        color=req.color,
        user_id=user_id,
    )
    db.add(pet)
    db.commit()
    db.refresh(pet)
    logger.info("Pet created: id=%d user_id=%d name=%s", pet.id, user_id, pet.name)
    return {"success": True, "message": "桌宠创建成功", "data": _pet_to_dict(pet)}


@router.get("", response_model=dict)
def get_pets(
    user_id: int = Depends(_get_user_id),
    db: Session = Depends(get_db),
):
    pets = (
        db.query(Pet)
        .filter(Pet.user_id == user_id)
        .order_by(desc(Pet.last_interacted_at))
        .all()
    )
    return {
        "success": True,
        "message": "获取成功",
        "data": [_pet_to_dict(p) for p in pets],
    }


@router.get("/{pet_id}", response_model=dict)
def get_pet(pet_id: int, db: Session = Depends(get_db)):
    pet = db.query(Pet).filter(Pet.id == pet_id).first()
    if not pet:
        return {"success": False, "message": "桌宠不存在"}
    return {"success": True, "message": "获取成功", "data": _pet_to_dict(pet)}


@router.delete("/{pet_id}", response_model=dict)
def delete_pet(pet_id: int, db: Session = Depends(get_db)):
    pet = db.query(Pet).filter(Pet.id == pet_id).first()
    if not pet:
        return {"success": False, "message": "桌宠不存在"}
    db.delete(pet)
    db.commit()
    logger.info("Pet deleted: id=%d", pet_id)
    return {"success": True, "message": "删除成功"}


def _pet_to_dict(pet: Pet) -> dict:
    return {
        "id": pet.id,
        "name": pet.name,
        "appearance": pet.appearance,
        "personality": pet.personality,
        "color": pet.color,
        "created_at": pet.created_at.isoformat() if pet.created_at else None,
        "last_interacted_at": pet.last_interacted_at.isoformat() if pet.last_interacted_at else None,
    }
