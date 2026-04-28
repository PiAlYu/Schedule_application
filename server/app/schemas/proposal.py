from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.models import ProposalStatus
from app.schemas.schedule import ScheduleEntryBase


class ProposalCreate(ScheduleEntryBase):
    submitted_by: str = Field(default="anonymous", min_length=1, max_length=128)


class ProposalRead(ScheduleEntryBase):
    model_config = ConfigDict(from_attributes=True)

    id: int
    submitted_by: str
    reviewer_comment: str | None
    status: ProposalStatus
    created_at: datetime
    reviewed_at: datetime | None


class ProposalDecision(BaseModel):
    reviewer_comment: str | None = Field(default=None, max_length=2000)
