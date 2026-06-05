from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class CreatePetRequest(BaseModel):
    name: str
    appearance: str
    personality: str
    color: str


class PetResponse(BaseModel):
    id: int
    name: str
    appearance: str
    personality: str
    color: str
    created_at: Optional[datetime] = None
    last_interacted_at: Optional[datetime] = None

    class Config:
        from_attributes = True
