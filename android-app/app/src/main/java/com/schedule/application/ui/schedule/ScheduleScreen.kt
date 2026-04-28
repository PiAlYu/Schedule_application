package com.schedule.application.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schedule.application.data.model.Lesson
import com.schedule.application.data.model.PriorityLevel
import com.schedule.application.data.model.WeekDay

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    contentPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedDayLessons = state.weekSchedule.firstOrNull { it.dayOfWeek == state.selectedDay }?.lessons.orEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            HeaderSection(
                groupName = state.weekGroup,
                onRefresh = viewModel::refresh,
            )
        }

        item {
            DaySelector(
                selectedDay = state.selectedDay,
                onDaySelected = viewModel::selectDay,
            )
        }

        if (state.isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (state.error != null) {
            item {
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (!state.isLoading && selectedDayLessons.isEmpty()) {
            item {
                Card {
                    Text(
                        text = "No lessons for this day",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        items(selectedDayLessons, key = { lesson -> lesson.id }) { lesson ->
            LessonCard(
                lesson = lesson,
                onPrioritySelected = { priority -> viewModel.setPriority(lesson, priority) },
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        item {
            ProposalSection(
                selectedDay = state.selectedDay,
                isSubmitting = state.isSubmittingProposal,
                message = state.proposalMessage,
                onSubmit = viewModel::submitProposal,
            )
        }
    }
}

@Composable
private fun HeaderSection(
    groupName: String,
    onRefresh: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Current group",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = if (groupName.isBlank()) "Not selected" else groupName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            TextButton(onClick = onRefresh) {
                Text("Refresh")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DaySelector(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        WeekDay.entries.forEachIndexed { index, day ->
            SegmentedButton(
                selected = day.isoDay == selectedDay,
                onClick = { onDaySelected(day.isoDay) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = WeekDay.entries.size),
            ) {
                Text(day.shortTitle)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonCard(
    lesson: Lesson,
    onPrioritySelected: (PriorityLevel) -> Unit,
) {
    val style = priorityStyle(lesson.priority)

    Card(
        colors = CardDefaults.cardColors(containerColor = style.container),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Pair ${lesson.pairNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = style.text,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${lesson.startTime}-${lesson.endTime}",
                    color = style.text,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = lesson.subject, color = style.text, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = lesson.teacher, color = style.text)
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PriorityLevel.entries.forEach { priority ->
                    val chipStyle = priorityStyle(priority)
                    AssistChip(
                        onClick = { onPrioritySelected(priority) },
                        label = { Text(priorityLabel(priority)) },
                        modifier = Modifier.background(
                            color = chipStyle.container,
                            shape = MaterialTheme.shapes.small,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProposalSection(
    selectedDay: Int,
    isSubmitting: Boolean,
    message: String?,
    onSubmit: (ProposalFormState) -> Unit,
) {
    var dayOfWeek by remember(selectedDay) { mutableIntStateOf(selectedDay) }
    var pairNumber by remember { mutableIntStateOf(1) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var subject by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var submittedBy by remember { mutableStateOf("") }

    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Suggest a lesson (uses submit key)",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = dayOfWeek.toString(),
                onValueChange = { dayOfWeek = it.toIntOrNull()?.coerceIn(1, 7) ?: dayOfWeek },
                label = { Text("Day of week (1-7)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = pairNumber.toString(),
                onValueChange = { pairNumber = it.toIntOrNull()?.coerceIn(1, 12) ?: pairNumber },
                label = { Text("Pair number") },
                modifier = Modifier.fillMaxWidth(),
            )
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start HH:MM") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End HH:MM") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("Teacher") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = submittedBy,
                onValueChange = { submittedBy = it },
                label = { Text("Submitted by") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    onSubmit(
                        ProposalFormState(
                            dayOfWeek = dayOfWeek,
                            pairNumber = pairNumber,
                            startTime = startTime,
                            endTime = endTime,
                            subject = subject,
                            teacher = teacher,
                            submittedBy = submittedBy,
                        )
                    )
                },
                enabled = !isSubmitting && subject.isNotBlank() && teacher.isNotBlank(),
            ) {
                Text(if (isSubmitting) "Sending..." else "Send Proposal")
            }

            if (!message.isNullOrBlank()) {
                Text(text = message)
            }
        }
    }
}

private data class PriorityStyle(
    val container: Color,
    val text: Color,
)

private fun priorityStyle(priority: PriorityLevel): PriorityStyle {
    return when (priority) {
        PriorityLevel.RED -> PriorityStyle(container = Color(0xFFFFCDD2), text = Color(0xFF7F0000))
        PriorityLevel.ORANGE -> PriorityStyle(container = Color(0xFFFFE0B2), text = Color(0xFF8A4B00))
        PriorityLevel.YELLOW -> PriorityStyle(container = Color(0xFFFFF9C4), text = Color(0xFF665B00))
        PriorityLevel.DARK_GREEN -> PriorityStyle(container = Color(0xFFC8E6C9), text = Color(0xFF0F4D1A))
        PriorityLevel.LIGHT_GREEN -> PriorityStyle(container = Color(0xFFE8F5E9), text = Color(0xFF2E7D32))
    }
}

private fun priorityLabel(priority: PriorityLevel): String {
    return when (priority) {
        PriorityLevel.RED -> "Red"
        PriorityLevel.ORANGE -> "Orange"
        PriorityLevel.YELLOW -> "Yellow"
        PriorityLevel.DARK_GREEN -> "Dark green"
        PriorityLevel.LIGHT_GREEN -> "Light green"
    }
}
