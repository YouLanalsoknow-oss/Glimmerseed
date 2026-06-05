import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from models.database import Base, engine
from routers import auth, pet, chat, admin
from config import settings

logging.basicConfig(
    level=logging.DEBUG if settings.debug else logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Creating database tables...")
    Base.metadata.create_all(bind=engine)
    logger.info("Glimmerseed API ready on http://0.0.0.0:%d", 8080)
    yield


app = FastAPI(title=settings.app_name, lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(pet.router)
app.include_router(chat.router)
app.include_router(admin.router)

app.mount("/static", StaticFiles(directory="static"), name="static")


@app.get("/health")
def health():
    return {"status": "ok"}
