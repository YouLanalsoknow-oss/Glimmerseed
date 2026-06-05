from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "Glimmerseed API"
    debug: bool = True

    database_url: str = "sqlite:///./glimmerseed.db"

    jwt_secret: str = "glimmerseed-jwt-secret-key-2024-very-long-and-secure"
    jwt_algorithm: str = "HS256"
    jwt_expiration: int = 86400

    siliconflow_api_key: str = "sk-psahinqeszjuyqlerjswcldalhepyjheenidxrkuvawsyxav"
    siliconflow_base_url: str = "https://api.siliconflow.cn/v1"
    siliconflow_model: str = "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B"

    class Config:
        env_file = ".env"


settings = Settings()
