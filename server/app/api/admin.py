from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.core.deps import get_current_superuser
from app.core.security import create_access_token, verify_password
from app.db.session import get_db
from app.models import Proposal, ProposalStatus, ScheduleEntry, SuperUser
from app.schemas import (
    AdminLoginRequest,
    ProposalDecision,
    ProposalRead,
    ScheduleEntryCreate,
    ScheduleEntryRead,
    ScheduleEntryUpdate,
    TokenResponse,
)

router = APIRouter(prefix="/api/v1/admin", tags=["admin"])


def _upsert_slot(db: Session, payload: ScheduleEntryCreate) -> ScheduleEntry:
    existing = db.scalar(
        select(ScheduleEntry).where(
            ScheduleEntry.group_name == payload.group_name,
            ScheduleEntry.day_of_week == payload.day_of_week,
            ScheduleEntry.pair_number == payload.pair_number,
        )
    )

    if existing is None:
        schedule = ScheduleEntry(**payload.model_dump())
        db.add(schedule)
        db.commit()
        db.refresh(schedule)
        return schedule

    existing.start_time = payload.start_time
    existing.end_time = payload.end_time
    existing.subject = payload.subject
    existing.teacher = payload.teacher
    db.commit()
    db.refresh(existing)
    return existing


@router.post("/login", response_model=TokenResponse)
def admin_login(credentials: AdminLoginRequest, db: Session = Depends(get_db)) -> TokenResponse:
    user = db.scalar(select(SuperUser).where(SuperUser.username == credentials.username))
    if user is None or not verify_password(credentials.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

    token = create_access_token(subject=user.username)
    return TokenResponse(access_token=token)


@router.get("/schedules", response_model=list[ScheduleEntryRead])
def list_schedules(
    _: SuperUser = Depends(get_current_superuser),
    group_name: str | None = Query(default=None),
    day_of_week: int | None = Query(default=None, ge=1, le=7),
    db: Session = Depends(get_db),
) -> list[ScheduleEntryRead]:
    query = select(ScheduleEntry)
    if group_name:
        query = query.where(ScheduleEntry.group_name == group_name)
    if day_of_week:
        query = query.where(ScheduleEntry.day_of_week == day_of_week)

    rows = db.scalars(query.order_by(ScheduleEntry.group_name, ScheduleEntry.day_of_week, ScheduleEntry.pair_number)).all()
    return [ScheduleEntryRead.model_validate(row) for row in rows]


@router.post("/schedules/upsert", response_model=ScheduleEntryRead)
def upsert_schedule(
    payload: ScheduleEntryCreate,
    _: SuperUser = Depends(get_current_superuser),
    db: Session = Depends(get_db),
) -> ScheduleEntryRead:
    try:
        entry = _upsert_slot(db=db, payload=payload)
    except IntegrityError as exc:
        db.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid schedule data") from exc
    return ScheduleEntryRead.model_validate(entry)


@router.put("/schedules/{entry_id}", response_model=ScheduleEntryRead)
def update_schedule(
    entry_id: int,
    payload: ScheduleEntryUpdate,
    _: SuperUser = Depends(get_current_superuser),
    db: Session = Depends(get_db),
) -> ScheduleEntryRead:
    entry = db.get(ScheduleEntry, entry_id)
    if entry is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Schedule entry not found")

    for field_name, value in payload.model_dump(exclude_unset=True).items():
        setattr(entry, field_name, value)

    if entry.start_time >= entry.end_time:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="start_time must be earlier than end_time",
        )

    try:
        db.commit()
        db.refresh(entry)
    except IntegrityError as exc:
        db.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Conflicting schedule slot") from exc

    return ScheduleEntryRead.model_validate(entry)


@router.delete("/schedules/{entry_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_schedule(
    entry_id: int,
    _: SuperUser = Depends(get_current_superuser),
    db: Session = Depends(get_db),
) -> None:
    entry = db.get(ScheduleEntry, entry_id)
    if entry is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Schedule entry not found")
    db.delete(entry)
    db.commit()


@router.get("/proposals", response_model=list[ProposalRead])
def list_proposals(
    _: SuperUser = Depends(get_current_superuser),
    status_filter: ProposalStatus | None = Query(default=None, alias="status"),
    db: Session = Depends(get_db),
) -> list[ProposalRead]:
    query = select(Proposal)
    if status_filter is not None:
        query = query.where(Proposal.status == status_filter)
    rows = db.scalars(query.order_by(Proposal.created_at.desc())).all()
    return [ProposalRead.model_validate(row) for row in rows]


@router.post("/proposals/{proposal_id}/accept", response_model=ScheduleEntryRead)
def accept_proposal(
    proposal_id: int,
    decision: ProposalDecision,
    _: SuperUser = Depends(get_current_superuser),
    db: Session = Depends(get_db),
) -> ScheduleEntryRead:
    proposal = db.get(Proposal, proposal_id)
    if proposal is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Proposal not found")
    if proposal.status != ProposalStatus.pending:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Proposal already processed")

    schedule_payload = ScheduleEntryCreate(
        group_name=proposal.group_name,
        day_of_week=proposal.day_of_week,
        pair_number=proposal.pair_number,
        start_time=proposal.start_time,
        end_time=proposal.end_time,
        subject=proposal.subject,
        teacher=proposal.teacher,
    )
    schedule = _upsert_slot(db=db, payload=schedule_payload)

    proposal.status = ProposalStatus.accepted
    proposal.reviewer_comment = decision.reviewer_comment
    proposal.reviewed_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(schedule)

    return ScheduleEntryRead.model_validate(schedule)


@router.post("/proposals/{proposal_id}/reject", response_model=ProposalRead)
def reject_proposal(
    proposal_id: int,
    decision: ProposalDecision,
    _: SuperUser = Depends(get_current_superuser),
    db: Session = Depends(get_db),
) -> ProposalRead:
    proposal = db.get(Proposal, proposal_id)
    if proposal is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Proposal not found")
    if proposal.status != ProposalStatus.pending:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Proposal already processed")

    proposal.status = ProposalStatus.rejected
    proposal.reviewer_comment = decision.reviewer_comment
    proposal.reviewed_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(proposal)

    return ProposalRead.model_validate(proposal)
