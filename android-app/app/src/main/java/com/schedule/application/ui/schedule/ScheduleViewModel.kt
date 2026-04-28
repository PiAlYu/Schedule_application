package com.schedule.application.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schedule.application.data.model.DaySchedule
import com.schedule.application.data.model.Lesson
import com.schedule.application.data.model.PriorityLevel
import com.schedule.application.data.model.WeekDay
import com.schedule.application.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class ScheduleViewModel(
    private val repository: ScheduleRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        ScheduleUiState(selectedDay = LocalDate.now().dayOfWeek.value)
    )
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val groups = repository.fetchAvailableGroups().getOrElse { emptyList() }
            val result = repository.fetchWeekSchedule()

            result.onSuccess { week ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        weekGroup = week.groupName,
                        weekSchedule = week.days,
                        groups = groups,
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to load schedule",
                        groups = groups,
                    )
                }
            }
        }
    }

    fun selectDay(dayOfWeek: Int) {
        _state.update { it.copy(selectedDay = dayOfWeek.coerceIn(1, 7)) }
    }

    fun setPriority(lesson: Lesson, priority: PriorityLevel) {
        viewModelScope.launch {
            repository.updatePriority(
                groupName = lesson.groupName,
                dayOfWeek = lesson.dayOfWeek,
                pairNumber = lesson.pairNumber,
                priority = priority,
            )

            _state.update { uiState ->
                val updatedDays = uiState.weekSchedule.map { day ->
                    if (day.dayOfWeek != lesson.dayOfWeek) return@map day
                    day.copy(
                        lessons = day.lessons.map { current ->
                            if (current.id == lesson.id) current.copy(priority = priority) else current
                        }
                    )
                }
                uiState.copy(weekSchedule = updatedDays)
            }
        }
    }

    fun submitProposal(form: ProposalFormState) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingProposal = true, proposalMessage = null) }

            repository.submitProposal(
                dayOfWeek = form.dayOfWeek,
                pairNumber = form.pairNumber,
                startTime = form.startTime,
                endTime = form.endTime,
                subject = form.subject,
                teacher = form.teacher,
                submittedBy = form.submittedBy,
            ).onSuccess {
                _state.update { it.copy(isSubmittingProposal = false, proposalMessage = "Proposal sent") }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSubmittingProposal = false,
                        proposalMessage = throwable.message ?: "Unable to send proposal",
                    )
                }
            }
        }
    }
}

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val weekGroup: String = "",
    val weekSchedule: List<DaySchedule> = emptyList(),
    val selectedDay: Int = WeekDay.MONDAY.isoDay,
    val groups: List<String> = emptyList(),
    val isSubmittingProposal: Boolean = false,
    val proposalMessage: String? = null,
)

data class ProposalFormState(
    val dayOfWeek: Int,
    val pairNumber: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val teacher: String,
    val submittedBy: String,
)
