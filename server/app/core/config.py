from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "Schedule API"
    environment: str = "development"
    database_url: str = "sqlite:///./schedule.db"
    read_key: str = "read-key-change-me"
    submit_key: str = "submit-key-change-me"
    jwt_secret: str = "replace-this-secret"
    jwt_algorithm: str = "HS256"
    jwt_expires_minutes: int = 480
    superuser_username: str = "admin"
    superuser_password: str = "admin12345"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")


settings = Settings()
