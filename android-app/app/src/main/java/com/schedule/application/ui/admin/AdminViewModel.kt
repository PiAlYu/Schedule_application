package com.schedule.application.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schedule.application.data.model.Lesson
import com.schedule.application.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: AdminRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value) }
    }

    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            repository.login(
                username = state.value.username,
                password = state.value.password,
            ).onSuccess { token ->
                _state.update { it.copy(isLoading = false, token = token, message = "Authorized") }
                loadSchedules()
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, message = throwable.message ?: "Auth failed") }
            }
        }
    }

    fun loadSchedules() {
        val token = state.value.token ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            repository.listSchedules(token).onSuccess { rows ->
                _state.update { it.copy(isLoading = false, lessons = rows) }
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, message = throwable.message ?: "Load failed") }
            }
        }
    }

    fun editLesson(lesson: Lesson) {
        _state.update {
            it.copy(
                editingLessonId = lesson.id,
                form = AdminLessonForm(
                    groupName = lesson.groupName,
                    dayOfWeek = lesson.dayOfWeek,
                    pairNumber = lesson.pairNumber,
                    startTime = lesson.startTime,
                    endTime = lesson.endTime,
                    subject = lesson.subject,
                    teacher = lesson.teacher,
                )
            )
        }
    }

    fun clearForm() {
        _state.update { it.copy(editingLessonId = null, form = AdminLessonForm()) }
    }

    fun updateForm(update: AdminLessonForm.() -> AdminLessonForm) {
        _state.update { it.copy(form = it.form.update()) }
    }

    fun saveLesson() {
        val token = state.value.token ?: return
        val form = state.value.form

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }

            val result = if (state.value.editingLessonId == null) {
                repository.upsertSchedule(
                    token = token,
                    groupName = form.groupName,
                    dayOfWeek = form.dayOfWeek,
                    pairNumber = form.pairNumber,
                    startTime = form.startTime,
                    endTime = form.endTime,
                    subject = form.subject,
                    teacher = form.teacher,
                )
            } else {
                repository.updateSchedule(
                    token = token,
                    entryId = state.value.editingLessonId!!,
                    groupName = form.groupName,
                    dayOfWeek = form.dayOfWeek,
                    pairNumber = form.pairNumber,
                    startTime = form.startTime,
                    endTime = form.endTime,
                    subject = form.subject,
                    teacher = form.teacher,
                )
            }

            result.onSuccess {
                _state.update { it.copy(isLoading = false, message = "Saved", editingLessonId = null, form = AdminLessonForm()) }
                loadSchedules()
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, message = throwable.message ?: "Save failed") }
            }
        }
    }

    fun deleteLesson(entryId: Int) {
        val token = state.value.token ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            repository.deleteSchedule(token, entryId).onSuccess {
                _state.update { it.copy(isLoading = false, message = "Deleted") }
                loadSchedules()
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, message = throwable.message ?: "Delete failed") }
            }
        }
    }
}

data class AdminUiState(
    val username: String = "",
    val password: String = "",
    val token: String? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val lessons: List<Lesson> = emptyList(),
    val editingLessonId: Int? = null,
    val form: AdminLessonForm = AdminLessonForm(),
)

data class AdminLessonForm(
    val groupName: String = "",
    val dayOfWeek: Int = 1,
    val pairNumber: Int = 1,
    val startTime: String = "09:00",
    val endTime: String = "10:30",
    val subject: String = "",
    val teacher: String = "",
)
