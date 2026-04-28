from datetime import datetime
from typing import Annotated

from fastapi import Depends, HTTPException, status
from fastapi.security import APIKeyHeader, HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.security import TokenPayloadError, decode_access_token
from app.db.session import get_db
from app.models import SuperUser

read_key_header = APIKeyHeader(name="X-Read-Key", auto_error=False)
submit_key_header = APIKeyHeader(name="X-Submit-Key", auto_error=False)
bearer_scheme = HTTPBearer(auto_error=False)


def _reject(message: str) -> HTTPException:
    return HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=message)


def require_read_key(api_key: Annotated[str | None, Depends(read_key_header)] = None) -> None:
    if api_key != settings.read_key:
        raise _reject("Invalid read key")


def require_submit_key(api_key: Annotated[str | None, Depends(submit_key_header)] = None) -> None:
    if api_key != settings.submit_key:
        raise _reject("Invalid submit key")


def get_current_superuser(
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(bearer_scheme)] = None,
    db: Session = Depends(get_db),
) -> SuperUser:
    if credentials is None:
        raise _reject("Missing authorization token")

    token = credentials.credentials
    try:
        username = decode_access_token(token)
    except TokenPayloadError as exc:
        raise _reject("Invalid authorization token") from exc

    user = db.scalar(select(SuperUser).where(SuperUser.username == username))
    if user is None:
        raise _reject("Superuser not found")
    return user
