from collections import defaultdict

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.deps import require_read_key, require_submit_key
from app.db.session import get_db
from app.models import Proposal, ScheduleEntry
from app.schemas import DayScheduleResponse, ProposalCreate, ProposalRead, ScheduleEntryRead, WeekScheduleResponse

router = APIRouter(prefix="/api/v1", tags=["public"])


@router.get("/groups", response_model=list[str], dependencies=[Depends(require_read_key)])
def list_groups(db: Session = Depends(get_db)) -> list[str]:
    rows = db.scalars(select(ScheduleEntry.group_name).distinct().order_by(ScheduleEntry.group_name)).all()
    return list(rows)


@router.get("/schedule/day", response_model=DayScheduleResponse, dependencies=[Depends(require_read_key)])
def get_day_schedule(
    group_name: str = Query(min_length=1, max_length=64),
    day_of_week: int = Query(ge=1, le=7),
    db: Session = Depends(get_db),
) -> DayScheduleResponse:
    entries = db.scalars(
        select(ScheduleEntry)
        .where(ScheduleEntry.group_name == group_name, ScheduleEntry.day_of_week == day_of_week)
        .order_by(ScheduleEntry.pair_number)
    ).all()

    return DayScheduleResponse(
        group_name=group_name,
        day_of_week=day_of_week,
        entries=[ScheduleEntryRead.model_validate(entry) for entry in entries],
    )


@router.get("/schedule/week", response_model=WeekScheduleResponse, dependencies=[Depends(require_read_key)])
def get_week_schedule(
    group_name: str = Query(min_length=1, max_length=64),
    db: Session = Depends(get_db),
) -> WeekScheduleResponse:
    entries = db.scalars(
        select(ScheduleEntry)
        .where(ScheduleEntry.group_name == group_name)
        .order_by(ScheduleEntry.day_of_week, ScheduleEntry.pair_number)
    ).all()

    by_day: defaultdict[int, list[ScheduleEntryRead]] = defaultdict(list)
    for entry in entries:
        by_day[entry.day_of_week].append(ScheduleEntryRead.model_validate(entry))

    days = [
        DayScheduleResponse(group_name=group_name, day_of_week=day, entries=by_day.get(day, []))
        for day in range(1, 8)
    ]
    return WeekScheduleResponse(group_name=group_name, days=days)


@router.post(
    "/proposals",
    response_model=ProposalRead,
    status_code=status.HTTP_201_CREATED,
    dependencies=[Depends(require_submit_key)],
)
def create_proposal(payload: ProposalCreate, db: Session = Depends(get_db)) -> ProposalRead:
    proposal = Proposal(**payload.model_dump())
    db.add(proposal)
    db.commit()
    db.refresh(proposal)
    return ProposalRead.model_validate(proposal)
