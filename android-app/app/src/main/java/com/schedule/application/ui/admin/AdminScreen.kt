package com.schedule.application.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schedule.application.data.model.Lesson

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    contentPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        if (state.token == null) {
            item {
                LoginCard(
                    username = state.username,
                    password = state.password,
                    isLoading = state.isLoading,
                    message = state.message,
                    onUsernameChange = viewModel::onUsernameChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onLogin = viewModel::login,
                )
            }
            return@LazyColumn
        }

        item {
            LessonEditorCard(
                isLoading = state.isLoading,
                form = state.form,
                editingLessonId = state.editingLessonId,
                message = state.message,
                onUpdateForm = viewModel::updateForm,
                onSave = viewModel::saveLesson,
                onReset = viewModel::clearForm,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Uploaded schedule entries", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = viewModel::loadSchedules) {
                    Text("Reload")
                }
            }
        }

        items(state.lessons, key = { lesson -> lesson.id }) { lesson ->
            AdminLessonCard(
                lesson = lesson,
                onEdit = { viewModel.editLesson(lesson) },
                onDelete = { viewModel.deleteLesson(lesson.id) },
            )
        }
    }
}

@Composable
private fun LoginCard(
    username: String,
    password: String,
    isLoading: Boolean,
    message: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Superuser authorization", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(onClick = onLogin, enabled = !isLoading && username.isNotBlank() && password.isNotBlank()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text("Login")
            }

            if (!message.isNullOrBlank()) {
                Text(text = message)
            }
        }
    }
}

@Composable
private fun LessonEditorCard(
    isLoading: Boolean,
    form: AdminLessonForm,
    editingLessonId: Int?,
    message: String?,
    onUpdateForm: (AdminLessonForm.() -> AdminLessonForm) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (editingLessonId == null) "Create entry" else "Edit entry #$editingLessonId",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = form.groupName,
                onValueChange = { value -> onUpdateForm { copy(groupName = value) } },
                label = { Text("Group") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.dayOfWeek.toString(),
                    onValueChange = { value ->
                        onUpdateForm {
                            copy(dayOfWeek = value.toIntOrNull()?.coerceIn(1, 7) ?: dayOfWeek)
                        }
                    },
                    label = { Text("Day (1-7)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.pairNumber.toString(),
                    onValueChange = { value ->
                        onUpdateForm {
                            copy(pairNumber = value.toIntOrNull()?.coerceIn(1, 12) ?: pairNumber)
                        }
                    },
                    label = { Text("Pair #") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.startTime,
                    onValueChange = { value -> onUpdateForm { copy(startTime = value) } },
                    label = { Text("Start") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.endTime,
                    onValueChange = { value -> onUpdateForm { copy(endTime = value) } },
                    label = { Text("End") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = form.subject,
                onValueChange = { value -> onUpdateForm { copy(subject = value) } },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.teacher,
                onValueChange = { value -> onUpdateForm { copy(teacher = value) } },
                label = { Text("Teacher") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSave,
                    enabled = !isLoading && form.groupName.isNotBlank() && form.subject.isNotBlank() && form.teacher.isNotBlank(),
                ) {
                    Text(if (editingLessonId == null) "Create/replace" else "Save")
                }
                TextButton(onClick = onReset) {
                    Text("Clear")
                }
            }

            if (!message.isNullOrBlank()) {
                Text(text = message)
            }
        }
    }
}

@Composable
private fun AdminLessonCard(
    lesson: Lesson,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "${lesson.groupName} | Day ${lesson.dayOfWeek} | Pair ${lesson.pairNumber}",
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = "${lesson.startTime}-${lesson.endTime}")
            Text(text = lesson.subject)
            Text(text = lesson.teacher)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}
