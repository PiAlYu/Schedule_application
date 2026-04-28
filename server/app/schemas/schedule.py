from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

_TIME_PATTERN = r"^([01][0-9]|2[0-3]):[0-5][0-9]$"


class ScheduleEntryBase(BaseModel):
    group_name: str = Field(min_length=1, max_length=64)
    day_of_week: int = Field(ge=1, le=7)
    pair_number: int = Field(ge=1, le=12)
    start_time: str = Field(pattern=_TIME_PATTERN)
    end_time: str = Field(pattern=_TIME_PATTERN)
    subject: str = Field(min_length=1, max_length=256)
    teacher: str = Field(min_length=1, max_length=256)

    @model_validator(mode="after")
    def validate_time_interval(self) -> "ScheduleEntryBase":
        if self.start_time >= self.end_time:
            raise ValueError("start_time must be earlier than end_time")
        return self


class ScheduleEntryCreate(ScheduleEntryBase):
    pass


class ScheduleEntryUpdate(BaseModel):
    group_name: str | None = Field(default=None, min_length=1, max_length=64)
    day_of_week: int | None = Field(default=None, ge=1, le=7)
    pair_number: int | None = Field(default=None, ge=1, le=12)
    start_time: str | None = Field(default=None, pattern=_TIME_PATTERN)
    end_time: str | None = Field(default=None, pattern=_TIME_PATTERN)
    subject: str | None = Field(default=None, min_length=1, max_length=256)
    teacher: str | None = Field(default=None, min_length=1, max_length=256)

    @model_validator(mode="after")
    def validate_time_interval(self) -> "ScheduleEntryUpdate":
        if self.start_time is not None and self.end_time is not None and self.start_time >= self.end_time:
            raise ValueError("start_time must be earlier than end_time")
        return self


class ScheduleEntryRead(ScheduleEntryBase):
    model_config = ConfigDict(from_attributes=True)

    id: int
    created_at: datetime
    updated_at: datetime


class DayScheduleResponse(BaseModel):
    group_name: str
    day_of_week: int
    entries: list[ScheduleEntryRead]


class WeekScheduleResponse(BaseModel):
    group_name: str
    days: list[DayScheduleResponse]
